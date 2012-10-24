/*
 * Control system for the 
 */

#include <WiShield.h>
//#include <dataflash.h>
#include "wreckroll.h"

#define WIRELESS_MODE_INFRA    1
#define WIRELESS_MODE_ADHOC    2

#define FLASH_SLAVE_SELECT 7
#define WIFI_SLAVE_SELECT  10

// Server configuration parameters ------------------------------------------

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

#define CAR_MOTION_STEPS 10
#define CAR_STOP_STEPS    1
#define CAR_TURN_STEPS    1
#define GUN_MOTION_STEPS 10
#define SMOKE_MOTION_STEPS 10
#define CANOPY_MOTION_STEPS 10

//unsigned char mfg_id[4];

struct wreck_state {
  char movement_left;
  char movement;
  
  char turn_left;
  char turn;
 
  byte    gun_motion_left;
  boolean gun_state;

  byte    smoke_motion_left;
  boolean smoke_state;

  byte    canopy_motion_left;
  boolean canopy_state; 
  
};

wreck_state ws = {0};

void setup()
{
  initShield();
  set_command_handler(handle_command);

//  WiServer.enableVerboseMode(true);
}

// This is the webpage that is served up by the webserver
const prog_char webpage[] PROGMEM = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
									"<html><body style=\"margin:100px\">"
									"<center>"
									"<h1>Flash Mfg ID: 000 000 000</h1>"
									"<form method=post action=\"/upload\" enctype=\"multipart/form-data\">"
									"<b>FS Image Upload</b>"
									"<p>"
									"<input type=file name=i size=40> &nbsp; <input type=submit value=\"Upload\">"
									"</form>"
									"</center>"
									"</body></html>";

void loop()
{ 
  //dflash.read_id(mfg_id);

  WiFi.run();

  //advance any movement/actions that are held in the state.  this will be influenced
  // by commands received over the socket
  turn_wreck();
  move_wreck();
  toggle_gun();
  toggle_smoke();
  toggle_canopy();

}

void initShield()
{
  // there is some contention on the SPI between the flash and wifi chip,
  // so disable both devices at the beginning until they are properly
  // initialized by their respective libraries
  pinMode(FLASH_SLAVE_SELECT, OUTPUT);
  digitalWrite(FLASH_SLAVE_SELECT, HIGH);
  pinMode(WIFI_SLAVE_SELECT, OUTPUT);
  digitalWrite(WIFI_SLAVE_SELECT, HIGH);

  // now init dataflash
//  dflash.init(FLASH_SLAVE_SELECT);

  // now init wifi
  WiFi.init();
  // Enable Serial output and ask WiServer to generate log messages (optional)
  Serial.begin(57600);
  Serial.println("Init'd");
}

void handle_command(char *inputbuffer)
{
  Serial.print("Incoming: ");
  Serial.println(inputbuffer);
  
  update_state(inputbuffer[0]);
}

void update_state(char command) {
  switch(command & ~0x20) { //always uppercase
    case 'F': //forward
      if (ws.movement == 'R') {
        //TODO: status message back to caller
        break;
      }
      ws.movement = 'F';
      ws.movement_left = CAR_MOTION_STEPS;
      break;
    case 'V': //reverse
      if (ws.movement == 'F') {
        //TODO: status message back to caller
        break;
      }
      ws.movement = 'R';
      ws.movement_left = CAR_MOTION_STEPS;
      break;
    case 'S': //stop
      if (ws.movement == 'S') {
        break; //nothing to do
      }
      ws.movement = 'S';
      ws.movement_left = CAR_STOP_STEPS;
      break;
    case 'L':
      ws.turn = 'L';
      ws.turn_left = CAR_TURN_STEPS;
      break;
    case 'R':
      ws.turn = 'R';
      ws.turn_left = CAR_TURN_STEPS;
      break;  
    case 'G': //toggle gun
      if (ws.gun_motion_left == 0) {
        ws.gun_state       = !ws.gun_state;
        ws.gun_motion_left = GUN_MOTION_STEPS;
      }
      break;
    case 'M': //toggle smoke
      if (ws.smoke_motion_left == 0) {
        ws.smoke_state       = !ws.smoke_state;
        ws.smoke_motion_left = SMOKE_MOTION_STEPS;
      }
      break;
    case 'E': //toggle ejector/canopy
      if (ws.canopy_state == 0) {
        ws.canopy_state       = !ws.canopy_state;
        ws.canopy_motion_left = CANOPY_MOTION_STEPS;
      }
      break;
  }
}

void turn_wreck() {
  if (ws.turn_left > 0) {
    Serial.print("Turning the wreck ");
    Serial.println(ws.turn == 'L' ? "left" : "right");
      
    int blinkSpeed = ws.movement == 'L' ? 750 : 1500;
    doBlink(FLASH_SLAVE_SELECT, blinkSpeed);
    ws.movement_left--;
  }
}
//NOTE: includes "stop"
void move_wreck() {
  if (ws.movement_left > 0) {
    if (ws.movement != 'S') {
      Serial.print("Moving the wreck ");
      Serial.println(ws.movement == 'F' ? "forward" : "backward");
      
      int blinkSpeed = ws.movement == 'F' ? 1000 : 2000;
      doBlink(FLASH_SLAVE_SELECT, blinkSpeed);
    } else {
      Serial.println("Stopping the wreck");
      doBlink(FLASH_SLAVE_SELECT, 500);
      doBlink(FLASH_SLAVE_SELECT, 500);
    }      
    ws.movement_left--;
  }
}

void toggle_gun() {
//simulate moving the gun
  if (ws.gun_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.gun_motion_left--;
  }
}

void toggle_smoke() {
//simulate moving the smoke
  if (ws.smoke_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.smoke_motion_left--;
  }
}

void toggle_canopy() {
  if (ws.canopy_motion_left > 0) {
    doBlink(FLASH_SLAVE_SELECT, 250);
    ws.canopy_motion_left--;
  }
}
  
void doBlink(int pin, int timeMS) {
    Serial.println("Blinking");
    digitalWrite(pin, LOW);
    delay(timeMS);
    digitalWrite(pin, HIGH);
    delay(timeMS);
}

