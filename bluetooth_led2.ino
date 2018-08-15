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
int pwm_pin = 10;

String readCharacters = "";
int storeTime = 0;
int minutes = 0;
int hours = 0;
bool timeTrigger = true;
bool isFirst = true;
float newValue;

RTC_DS1307 rtc;


void setup() {
  // Open serial communications:
  Serial.begin(19200);
  
  // The HC-06 defaults to 9600 according to the datasheet.
  mySerial.begin(9600);

  Wire.begin();
  rtc.begin();
  if (!rtc.isrunning()) {
    Serial.println("RTC is NOT running!");
    // following line sets the RTC to the date & time this sketch was compiled
    rtc.adjust(DateTime(__DATE__, __TIME__));
  }
  pinMode(led_pin, OUTPUT);
  pinMode(led_pin2, OUTPUT);
  pinMode(pir_switch, OUTPUT);
  digitalWrite(led_pin, HIGH);
  digitalWrite(led_pin2, HIGH);
  digitalWrite(pir_switch, HIGH);
  analogWrite(pwm_pin, 127.5);
//  for (int i = 0; i <= 255; i += 5){
//    analogWrite(pwm_pin, i);
//    delay(30);  
//  }
  
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
//  Serial.print("minute is ");
//  Serial.println(minutes);
//  Serial.print("hour is ");
//  Serial.println(hours);
  
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
      if(!(storeTime>=69)){
        if(isFirst){
          minutes = storeTime;
          isFirst = false;
          if(minutes > 59){
            isFirst = true;
          }
          Serial.println("This store in minutes");
        }else{
          hours = storeTime;
          isFirst = true;
          Serial.println("This store in hours");
        }
      timeTrigger = true;
      }else{
        Serial.println("This is from seekbar");
        
        newValue = ((storeTime-70.00)/(170.00-70.00))*255.00; 
        analogWrite(pwm_pin,newValue);
        Serial.print("storeTime:");
        Serial.println(storeTime);
        Serial.println(newValue);
      }
    }
    readCharacters = "";
  }

  // The offset of the RTC is around 29 minutes and 29 secons
  // This if statement is to add the minute and second manually
  // to match with the current time.
  if(minutes <= 30){
    if (timeTrigger && current.minute() == minutes+29 && current.hour() == hours && current.second() == 33){
      digitalWrite(led_pin, LOW);
      digitalWrite(led_pin2, LOW);
      timeTrigger = false;
      storeTime = 0;
      minutes = 0;
      hours = -1;
      isFirst = true;
    }
  }else {
    // During 11pm, it has to override to 0am manually
    if (hours != 23){
      if (timeTrigger && current.minute() == minutes+29-60 && current.hour() == hours + 1 && current.second() == 33){
        digitalWrite(led_pin, LOW);
        digitalWrite(led_pin2, LOW);
        timeTrigger = false;
        storeTime = 0;
        minutes = 0;
        hours = -1;
        isFirst = true;
      }
      // During other hours than 11pm
    }else {
      if (timeTrigger && current.minute() == minutes+29-60 && current.hour() == 0 && current.second() == 33){
        digitalWrite(led_pin, LOW);
        digitalWrite(led_pin2, LOW);
        timeTrigger = false;
        storeTime = 0;
        minutes = 0;
        hours = -1;
        isFirst = true;
      }
    } 
  }
}

bool isNumeric(String str){
  for(byte i=0;i<str.length();i++){
    
    if(isDigit(str.charAt(i))) return true;
  }
  return false;
} 
