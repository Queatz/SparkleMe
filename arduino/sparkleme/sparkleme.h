#include <Ethernet.h>
#include <EthernetServer.h>
#include <EthernetUdp.h>
#include <Dhcp.h>
#include <EthernetClient.h>
#include <Dns.h>

#include <FastLED.h>

#ifndef __SPARKLEME_H
#define __SPARKLEME_H

#define BAUD_RATE   115200
#define LED_PIN     5
#define NUM_LEDS    300
#define BRIGHTNESS  64
#define LED_TYPE    WS2811
#define COLOR_ORDER GRB
CRGB leds[NUM_LEDS];

#define MAX_MODES   4
#define UPDATES_PER_SECOND 100

class Mode {
  
  public:

  virtual boolean update();
  virtual void changed();
};

class Sparkle {
  
  public:

  Sparkle();
  
  CRGB color;
  String* flickerString;
  boolean alive;
  Mode** modes;
  boolean offOnDisconnect;
  long lastAttention;
  short lastAttentionTimeout = 7000;
  boolean waitingForAttention;
  
  void update();
  void sparkle(String* command);
  void on();
  void off();

  void sendColor();
  void setColor(String color);
  void readCommand(String command);
};

Sparkle* sparkle;

#endif
