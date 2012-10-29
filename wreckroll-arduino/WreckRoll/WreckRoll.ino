/*
 * Control system for the 
 */

#include <WiShield.h>
//#include <WiServer.h>
//#include <dataflash.h>
#include "registrar.h"
#include "wreckroll.h"
#include "debug.h"

#define WIRELESS_MODE_INFRA    1
#define WIRELESS_MODE_ADHOC    2

#define FLASH_SLAVE_SELECT 7
#define WIFI_SLAVE_SELECT  10

// WreckRoll configuration parameters ------------------------------------------

unsigned char registrar_ip[]    = {192,168,1,250};	// IP address of WiShield
short registrar_port = 8001;

unsigned short port = 9000;

//---------------------------------------------------------------------------


// Wireless configuration parameters ----------------------------------------
unsigned char local_ip[]    = {192,168,1,2};	// IP address of WiShield
unsigned char gateway_ip[]  = {192,168,1,1};	// router or gateway IP address
unsigned char subnet_mask[] = {255,255,255,0};	// subnet mask for the local network

const prog_char ssid[] PROGMEM = {"dd-wrt"};	// max 32 bytes

unsigned char security_type = 0;	// 0 - open; 1 - WEP; 2 - WPA; 3 - WPA2

// WPA/WPA2 passphrase
const prog_char security_passphrase[] PROGMEM = {"12345678"};	// max 64 characters

// WEP 128-bit keys
// sample HEX keys
prog_uchar wep_keys[] PROGMEM = {	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// Key 0
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00,	// Key 1
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00,	// Key 2
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00	// Key 3
				};

// setup the wireless mode
// infrastructure - connect to AP
// adhoc - connect to another WiFi device
unsigned char wireless_mode = WIRELESS_MODE_INFRA;

unsigned char ssid_len;
unsigned char security_passphrase_len;
//---------------------------------------------------------------------------

#define SUBSYS_STOWED 0
#define SUBSYS_DEPLOYED 1

#define CAR_MOTION_MS    2000
#define CAR_STOP_MS      300
#define CAR_TURN_MS      200
#define GUN_MOTION_MS    200
#define SMOKE_MOTION_MS  200
#define CANOPY_MOTION_MS 200

//unsigned char mfg_id[4];

struct wreck_state {
  unsigned long last_time;
  unsigned long elapsed_time;

  short movement_left;
  char movement;
  
  short turn_left;
  char turn;
 
  byte    gun_motion_left;
  boolean gun_state;

  byte    smoke_motion_left;
  boolean smoke_state;

  byte    canopy_motion_left;
  boolean canopy_state; 
  
  boolean emergency_stop;
  
  int speedPM;
  int speedDir;
  
  int turnPM;
  int turnDir;
};

// Pins
int speedDirPin = 7;
int speedPMPin = 5;

int turnDirPin = 8;
int turnPMPin = 9;

int rightSensorPin = 2;
int leftSensorPin = 3;

wreck_state ws = {0};

void debug(char *msg) {
  Serial.println(msg);
}

void setup()
{
  initShield();
  set_command_handler(handle_command);
  ws.speedPM = 0;
  ws.speedDir = 0;
  ws.turnDir = 0;
  ws.turnPM = 0;
  updateToPins();
}

int loop_cnt = 0;
void loop()
{ 

  if (loop_cnt == 0) {
    register_me();
    loop_cnt = 1;
  }

  WiFi.run();
  
  unsigned long this_time = millis();
  if (ws.last_time != 0) {
    ws.elapsed_time = this_time - ws.last_time;
  }

  if (get_run_state() == STATE_RUNNING) {
    if (loop_cnt == 1) {
      Serial.println("We're running");
      loop_cnt = 2;
    }
    //advance any movement/actions that are held in the state.  this will be influenced
    // by commands received over the socket
    if (ws.emergency_stop) {
      all_stop();
    }
    turn_wreck();
    move_wreck();
    toggle_gun();
    toggle_smoke();
    toggle_canopy();
  }
//  updateToPins();

  //use the time captured above..that means that an operation that takes 200ms will account for time performing the operation
  ws.last_time = this_time;

}

void initShield()
{
  // Enable Serial output and ask WiServer to generate log messages (optional)
  Serial.begin(57600);

  // there is some contention on the SPI between the flash and wifi chip,
  // so disable both devices at the beginning until they are properly
  // initialized by their respective libraries
  pinMode(FLASH_SLAVE_SELECT, OUTPUT);
  digitalWrite(FLASH_SLAVE_SELECT, HIGH);
  pinMode(WIFI_SLAVE_SELECT, OUTPUT);
  digitalWrite(WIFI_SLAVE_SELECT, HIGH);

  pinMode(speedDirPin, OUTPUT);
  pinMode(speedPMPin, OUTPUT);
  analogWrite(speedPMPin, 0);
  
  // now init dataflash
//  dflash.init(FLASH_SLAVE_SELECT);

  // now init wifi
  WiFi.init();
  Serial.println("Init'd");
}

void handle_command(char *inputbuffer)
{
  Serial.print("Incoming: ");
  Serial.println(inputbuffer);
  
  update_state(inputbuffer[0]);
}

void update_state(char command) {
  Serial.print("Incoming command: ");
  char buf[2];
  sprintf(buf, "%c", command);
  Serial.println(buf);
  switch(command & ~0x20) { //always uppercase
    case 'F': //forward
      if (ws.movement == 'R') {
        ws.movement = 'S';
        ws.movement_left = CAR_MOTION_MS;

        //TODO: status message back to caller
        break;
      }
      ws.movement = 'F';
      ws.movement_left = CAR_MOTION_MS;
      break;
    case 'V': //reverse
      if (ws.movement == 'F') {
        ws.movement = 'S';
        ws.movement_left = CAR_MOTION_MS;
        //TODO: status message back to caller
        break;
      }
      ws.movement = 'V';
      ws.movement_left = CAR_MOTION_MS;
      break;
    case 'S': //stop
      if (ws.movement == 'S') {
        break; //nothing to do
      }
      ws.movement = 'S';
      ws.movement_left = CAR_STOP_MS;
      break;
    case 'L':
      ws.turn = 'L';
      ws.turn_left = CAR_TURN_MS;
      break;
    case 'R':
      ws.turn = 'R';
      ws.turn_left = CAR_TURN_MS;
      break;  
    case 'G': //toggle gun
      if (ws.gun_motion_left == 0) {
        ws.gun_state       = !ws.gun_state;
        ws.gun_motion_left = GUN_MOTION_MS;
      }
      break;
    case 'M': //toggle smoke
      if (ws.smoke_motion_left == 0) {
        ws.smoke_state       = !ws.smoke_state;
        ws.smoke_motion_left = SMOKE_MOTION_MS;
      }
      break;
    case 'E': //toggle ejector/canopy
      if (ws.canopy_state == 0) {
        ws.canopy_state       = !ws.canopy_state;
        ws.canopy_motion_left = CANOPY_MOTION_MS;
      }
      break;
    case 'X': //emergency stop
      ws.emergency_stop = true;
      break;
  }
}

void all_stop() {
   if (ws.emergency_stop && ws.speedPM > 0) {
     ws.speedPM  = 0;
     ws.speedDir = ws.speedDir == LOW ? HIGH : LOW;
     updateToPins();
   } 
}

void turn_wreck() {
  if (ws.turn_left > 0) {
    Serial.print("Turning the wreck ");
    Serial.println(ws.turn == 'L' ? "left" : "right");
    Serial.println(digitalRead(rightSensorPin)==LOW ? "LOW" : "HIGH");
    Serial.println(digitalRead(leftSensorPin)==LOW ? "leftLOW" : "leftHIGH");
    if (ws.turn == 'L' && digitalRead(rightSensorPin) == LOW){
      Serial.println("Left");
      ws.turnPM = 255;
    } else if (ws.turn == 'L'){
      Serial.println("NOT Left");
      ws.turnPM = 0;
    } else if (ws.turn == 'R' && digitalRead(leftSensorPin) == LOW){
      Serial.println("Right");
      ws.turnPM = 255;
    } else {
      Serial.println("NOT Right");
      ws.turnPM = 0;
    }
    
    ws.turnDir = ws.turn =='L'? LOW : HIGH;

    analogWrite(turnPMPin, ws.turnPM);
    digitalWrite(turnDirPin, ws.turnDir);
      
    ws.turn_left -= ws.elapsed_time;
  }
}
//NOTE: includes "stop"
void move_wreck() {
  boolean changed = false;
  if (ws.movement_left > 0) {
    if (ws.movement != 'S') {
      Serial.print("Moving the wreck ");
      Serial.println(ws.movement == 'F' ? "forward" : "backward");
      if (ws.movement == 'F')
        ws.speedDir = LOW;
      else 
        ws.speedDir = HIGH;
      increaseSpeedPM();
      changed = true;
//      int blinkSpeed = ws.movement == 'F' ? 1000 : 2000;
//      doBlink(FLASH_SLAVE_SELECT, blinkSpeed);
    } else {
      changed = true;
      decreaseSpeedPM();
      Serial.println("Stopping the wreck");
//      doBlink(FLASH_SLAVE_SELECT, 500);
//      doBlink(FLASH_SLAVE_SELECT, 500);
    }      
    ws.movement_left -= ws.elapsed_time;
  } else {
    if (ws.speedPM > 0) {
      changed = true;
      ws.movement = 'S';
      ws.movement_left = CAR_STOP_MS;
    }
//    decreaseSpeedPM();
  }
  updateToPins();
}

int change = 0;
void updateToPins(){
  
  char buf[32];
  sprintf(buf, "%d, %d", ws.speedPM, ws.speedDir);
  //Serial.println(buf);
  digitalWrite(speedDirPin, ws.speedDir);
//  delay(200);
  analogWrite(speedPMPin, ws.speedPM);
}

void increaseSpeedPM(){
  ws.speedPM = 255;
//  ws.speedPM = (ws.speedPM+1) *2;
//  if (ws.speedPM > 255)
//    ws.speedPM = 255;
}

void decreaseSpeedPM(){
  ws.speedPM = 0;
//  ws.speedPM = (int)ws.speedPM * ((float)ws.movement_left / CAR_STOP_MS);
//  if (ws.speedPM < .10 * 255)
//    ws.speedPM = 0; 
}

void toggle_gun() {
//simulate moving the gun
  if (ws.gun_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.gun_motion_left -= ws.elapsed_time;
  }
}

void toggle_smoke() {
//simulate moving the smoke
  if (ws.smoke_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.smoke_motion_left -= ws.elapsed_time;
  }
}

void toggle_canopy() {
  if (ws.canopy_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.canopy_motion_left -= ws.elapsed_time;
  }
}
  
void doBlink(int pin, int timeMS) {
    Serial.println("Blinking");
    digitalWrite(pin, LOW);
    delay(timeMS);
    digitalWrite(pin, HIGH);
    delay(timeMS);
}

