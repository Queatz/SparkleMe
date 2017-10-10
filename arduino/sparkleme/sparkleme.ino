#include "sparkleme.h"
#include "modes.h"

/* Sparkle */

Sparkle::Sparkle() {
  flickerString = new String("");
  alive = false;
//  modes = new Mode*[MAX_MODES] {
//    new SparkleMode(this),
//    NULL,
//    NULL,
//    NULL
//  };

//  modes = new Mode*[MAX_MODES] {
//    new FadeMode(this),
//    new SparkleMode(this),
//    (new DanceMode(this))->add(Beat(400, CRGB(255 / 2, 128 / 2, 0), 128, 2))
//        ->add(Beat(400, CRGB(0, 0, 0), 24, 4))
//        ->add(Beat(400, CRGB(255 / 2, 128 / 2, 0), 128, 2))
//        ->add(Beat(400, CRGB(0, 0, 0), 24, 4))
//        ->add(Beat(400, CRGB(255 / 2, 0, 128 / 2), 128, 2))
//        ->add(Beat(400, CRGB(0, 0, 0), 68, 4))
//        ->add(Beat(600, CRGB(255 / 2, 128 / 2, 0), 128, 2))
//        ->add(Beat(200, CRGB(0, 0, 0), 120, 4)),
//    new JuiceMode(this)
//  };

  modes = new Mode*[MAX_MODES] {
    new SolidMode(this),
    NULL,
    NULL,
    NULL
  };
  
  Serial.begin(BAUD_RATE);
  
  delay(2000); // power-up safety delay
  FastLED.addLeds<LED_TYPE, LED_PIN, COLOR_ORDER>(leds, NUM_LEDS).setCorrection( TypicalLEDStrip );
  FastLED.setBrightness(BRIGHTNESS);
  on();
}

void Sparkle::update() {
  if (this->alive) {
    boolean needsUpdate = false;
    
    for (byte i = 0; i < MAX_MODES; i++) {
      if (!modes[i]) {
        break;
      }

      needsUpdate = needsUpdate || modes[i]->update();
    }

    if (needsUpdate) {
      FastLED.show();
      FastLED.delay(1000 / UPDATES_PER_SECOND);
    }
  }

  if (Serial.available()) {
    lastAttention = millis();
    waitingForAttention = false;

    if (!alive) {
      on();
    }
  }
  
  if (offOnDisconnect) {
    if (millis() > lastAttention + lastAttentionTimeout) {
      if (waitingForAttention) {
        if (millis() > lastAttention + lastAttentionTimeout * 2) {
          if (alive) {
            off();
          }
        }
      } else {
        waitingForAttention = true;
        Serial.write("+a=");
        Serial.flush();
      }
    }
  } else if (!alive) {
    on();
  }

  while(Serial.available()) {
    char b = Serial.read();
    
    if (b == '=') {
      this->sparkle(this->flickerString);
      this->flickerString->remove(0, this->flickerString->length());
    } else {
      if (b == '+') {
        this->flickerString->remove(0, this->flickerString->length());
      }
      
      this->flickerString->concat(b);
    }
  }
}

void Sparkle::sparkle(String* s) {
  if (s->length() < 3 || s->charAt(0) != '+') {
    return;
  }

  String str = s->substring(2, s->length());

  switch(s->charAt(1)) {
    case 'c':
      this->setColor(str);
      break;
    case 'v':
      this->sendColor();
      break;
    case 'x':
      this->readCommand(str);
      break;
  }
}

void Sparkle::readCommand(String str) {
  if (str.charAt(0) == 'd') {
    if (str.length() == 2) {
      switch(str.charAt(1)) {
        case '0':
          offOnDisconnect = false;
          break;
        case '1':
          offOnDisconnect = true;
          break;
      }

      String response = "+d";
      response.concat(offOnDisconnect ? "1" : "0");
      response.concat("=");
      Serial.write(response.c_str());
      Serial.flush();
    }
  }
}

void Sparkle::sendColor() {
  String string = "+c";
  string.concat(color.r);
  string.concat(':');
  string.concat(color.g);
  string.concat(':');
  string.concat(color.b);
  string.concat('=');
  Serial.write(string.c_str());
}

void Sparkle::setColor(String s) {
  int commaIndex = -1;

  for (int i = 0; i < 3; i++) {
    int nextCommaIndex = s.indexOf(':', commaIndex + 1);

    if (nextCommaIndex == -1) {
      nextCommaIndex = s.length();
    }

    this->color[i] = s.substring(commaIndex + 1, nextCommaIndex).toInt();
    
    commaIndex = nextCommaIndex;
  }

  for (byte i = 0; i < MAX_MODES; i++) {
    if (!modes[i]) {
      break;
    }

    modes[i]->changed();
  }
}

void Sparkle::on() {
  alive = true;
  sparkle(new String("+c128:128:128"));
}

void Sparkle::off() {
  alive = false;
  FastLED.clear();
  FastLED.show();
}

/* Initialize */

void setup() {
  sparkle = new Sparkle();
}

void loop() {
  sparkle->update();
}
