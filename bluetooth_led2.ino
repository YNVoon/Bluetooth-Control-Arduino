#include <SoftwareSerial.h>
#include "RTClib.h"
#include <Wire.h>
#include <ctype.h>
SoftwareSerial mySerial(4, 2); // RX, TX

String command = ""; // Stores response of the HC-06 Bluetooth device

int led_pin = 7;
int led_pin2 = 8;
int pir_switch = 13;
int pir_output = 12;
int pir_val = 0;

String readCharacters = "";
int storeTime = 0;
int minutes = 0;
int hours = 0;
bool timeTrigger = true;
bool isFirst = true;

RTC_Millis rtc;


void setup() {
  // Open serial communications:
  Serial.begin(19200);
  
  // The HC-06 defaults to 9600 according to the datasheet.
  mySerial.begin(9600);

  Wire.begin();
  rtc.begin(DateTime(F(__DATE__), F(__TIME__)));

  pinMode(led_pin, OUTPUT);
  pinMode(led_pin2, OUTPUT);
  pinMode(pir_switch, OUTPUT);
  digitalWrite(led_pin, HIGH);
  digitalWrite(led_pin2, HIGH);
  digitalWrite(pir_switch, HIGH);
}

void loop() {

  DateTime current = rtc.now();
//  Serial.print(current.year(), DEC);
//  Serial.print('/');
//  Serial.print(current.month(), DEC);
//  Serial.print('/');
//  Serial.print(current.day(), DEC);
//  Serial.print(' ');
//  Serial.print(current.hour(), DEC);
//  Serial.print(':');
//  Serial.print(current.minute(), DEC);
//  Serial.print(':');
//  Serial.println(current.second(), DEC);
  
  // Read device output if available.
  if (mySerial.available()) {
    while(mySerial.available()) { // While there is more to be read, keep reading.
      delay(3);
      command = (char)mySerial.read();
      readCharacters += command;
    }
  }

  // Read PIR values
  pir_val = digitalRead(pir_output);

  if (digitalRead(pir_switch)){
    if (pir_val == HIGH){
      digitalWrite(led_pin, HIGH);
      digitalWrite(led_pin2, HIGH);
    }else {
      digitalWrite(led_pin, LOW);
      digitalWrite(led_pin2, LOW);
    }
  }
  
  // Read user input if available.
  if (readCharacters.length() > 0){
    Serial.println(readCharacters);
    if (readCharacters == "TO"){
      digitalWrite(led_pin, HIGH);
    }
    if (readCharacters == "TF"){
      digitalWrite(led_pin, LOW);
    }
    if (readCharacters == "TOO"){
      digitalWrite(led_pin2, HIGH);
    }
    if (readCharacters == "TFF"){
      digitalWrite(led_pin2, LOW);
    }
    if (readCharacters == "PIROFF"){
      digitalWrite(pir_switch, LOW);
    }
    if (readCharacters == "PIRON"){
      digitalWrite(pir_switch, HIGH);
    }
    if (isNumeric(readCharacters)){
      Serial.println("YES GOT NUMBER");
      storeTime = readCharacters.toInt();
      if(isFirst){
        minutes = storeTime;
        isFirst = false;
        Serial.println("This store in minutes");
      }else{
        hours = storeTime;
        isFirst = true;
        Serial.println("This store in hours");
      }
      timeTrigger = true;
    }
    readCharacters = "";
  }

  if (timeTrigger && current.minute() == minutes-1 && current.hour() == hours && current.second() == 53){
    digitalWrite(led_pin, LOW);
    digitalWrite(led_pin2, LOW);
    timeTrigger = false;
    storeTime = 0;
    minutes = 0;
    hours = -1;
    isFirst = true;
  }
}

bool isNumeric(String str){
  for(byte i=0;i<str.length();i++){
    
    if(isDigit(str.charAt(i))) return true;
  }
  return false;
} 
