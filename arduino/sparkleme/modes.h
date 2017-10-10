
/* Mode: Juice */

class JuiceMode : public Mode {
  
  public:

  short position = 0;
  
  Sparkle* sparkle;

  JuiceMode(Sparkle* sparkle) {
    this->sparkle = sparkle;
  }

  boolean update() {
    short p = (short) abs(position * NUM_LEDS);
    position = (position + 1) % NUM_LEDS;
    p = (short) (position);
    leds[p] = this->sparkle->color;
    
    return true;
  }
  
  void changed() {
    
  }
};

/* Mode: Sparkle */

class SparkleMode : public Mode {

  public:
  
  long flickerStart = 0;
  int flickerDuration = 1000;
  int flickerFill = 64;
  Sparkle* sparkle;

  SparkleMode(Sparkle* sparkle) {
    this->sparkle = sparkle;
  }

  SparkleMode* setup(int flickerDuration, int flickerFill) {
    this->flickerDuration = flickerDuration;
    this->flickerFill = flickerFill;
    return this;
  }

  boolean update() {
    long t = millis();

    if (t > this->flickerStart + this->flickerDuration) {
      return false;
    }
    
    float flicker = pow(1 - (((float) (t - this->flickerStart)) / this->flickerDuration), 4);
    int a = flickerFill * flicker;
    for (int i = 0; i < a; i++) {
      leds[random16() % NUM_LEDS] = this->sparkle->color;
    }
    
    return true;
  }

  void changed() {
    this->flickerStart = millis();
  }

  private:

  boolean finish() {
      boolean dead = true;
      for (int i = 0; i < NUM_LEDS; i++) {
        if (leds[i]) {
          leds[i][0] *= .95;
          leds[i][1] *= .95;
          leds[i][2] *= .95;
          dead = false;
        }
      }

      return !dead;
  }
};

/* Mode: Dance */

struct Beat {
  
  short duration;
  CRGB color;
  short fill;
  byte size;

  Beat(short duration, CRGB color, short fill, byte size) {
    this->duration = duration;
    this->color = color;
    this->fill = fill;
    this->size = size;
  }
};

class DanceMode : public Mode {

  public:
  
  long nextBeat = 0;
  int beatIndex = 0;
  int beatCount = 0;
  
  Beat beats[16] = {
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0),
    Beat(1000, CRGB(255, 128, 0), 0, 0)
  };
  
  
  Sparkle* sparkle;

  DanceMode(Sparkle* sparkle) {
    this->sparkle = sparkle;
  }

  DanceMode* add(Beat beat) {
    if (beatCount >= 16) {
      return this;
    }
    
    this->beats[beatCount] = beat;
    beatCount++;
    
    return this;
  }

  boolean update() {
    long t = millis();

    if (t > nextBeat) {
      beatIndex = (beatIndex + 1) % beatCount;
      nextBeat += beats[beatIndex].duration;

      if (!beats[beatIndex].size) {
        for (int i = 0; i < NUM_LEDS; i++) {
          leds[i] = beats[beatIndex].color;
        }
      } else {
        for (int i = 0; i < beats[beatIndex].fill; i++) {
          int x = random16() % NUM_LEDS;
          
          for (int s = 0; s < beats[beatIndex].size; s++) {
            leds[(x + s) % NUM_LEDS] = beats[beatIndex].color;
          }
        }
      }
    }

    return true;
  }

  void changed() { }
};

/* Mode: Meteor */

class MeteorMode : public Mode {

  public:
  
  int meteorPosition;
  int meteorDirection;
  int meteorCoverage;
  
  long meteorDuration;
  long meteorDelay;
  long meteorStartTime;

  CRGB color;
  
  Sparkle* sparkle;

  MeteorMode(Sparkle* sparkle) {
    this->sparkle = sparkle;

    meteorPosition = 0;
    meteorDirection = 1;
    meteorCoverage = NUM_LEDS * 2;
    meteorDuration = 8000;
    meteorDelay = 4000;
    meteorStartTime = 0;
    color = CRGB::White;
  }

  MeteorMode* setup(int meteorCoverage, int meteorDuration, int meteorDelay, CRGB color) {
    this->meteorCoverage = meteorCoverage;
    this->meteorDuration = meteorDuration;
    this->meteorDelay = meteorDelay;
    this->color = color;
    
    return this;
  }

  boolean update() {
    long t = millis();

    if (t > meteorStartTime + meteorDuration + meteorDelay) {
      meteorPosition = random16() % (NUM_LEDS - 1);
      meteorDirection *= -1;
      meteorStartTime = t;
    }
    
    if (t > meteorStartTime && t < meteorStartTime + meteorDuration) {
      int position = (meteorPosition + (int) (((float) (t - meteorStartTime) / (float) meteorDuration) * meteorDirection * meteorCoverage)) % NUM_LEDS;
      if (position < 0) position += NUM_LEDS;

      leds[position] = color;
    }

    return true;
  }

  void changed() { }
};

/* Mode: Fade */

class FadeMode : public Mode {

  public:
  
  float sustain = .75;
  
  Sparkle* sparkle;

  FadeMode(Sparkle* sparkle) {
    this->sparkle = sparkle;
  }

  boolean update() {
    boolean changed = false;
    
   for (int i = 0; i < NUM_LEDS; i++) {
      if (leds[i]) {
        leds[i][0] *= sustain;
        leds[i][1] *= sustain;
        leds[i][2] *= sustain;
        changed = true;
      }
    }

    return changed;
  }

  void changed() { }
};

/* Mode: Solid */

class SolidMode : public Mode {

  public:
  
  Sparkle* sparkle;
  boolean needsUpdate;

  SolidMode(Sparkle* sparkle) {
    this->sparkle = sparkle;
  }

  boolean update() {
    if (needsUpdate) {
      needsUpdate = false;
      return true;
    }
    return false;
  }

  void changed() {
    for (int i = 0; i < NUM_LEDS; i++) {
        leds[i] = sparkle->color;
    }
    needsUpdate = true;
  }
};
