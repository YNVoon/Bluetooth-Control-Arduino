#include <SoftwareSerial.h>
SoftwareSerial mySerial(4, 2); // RX, TX

String command = ""; // Stores response of the HC-06 Bluetooth device

int led_pin = 7;
int led_pin2 = 8;
int pir_switch = 13;
int pir_output = 12;
int pir_val = 0;

String readCharacters = "";


void setup() {
  // Open serial communications:
  Serial.begin(19200);
  
  // The HC-06 defaults to 9600 according to the datasheet.
  mySerial.begin(9600);

  pinMode(led_pin, OUTPUT);
  pinMode(led_pin2, OUTPUT);
  pinMode(pir_switch, OUTPUT);
  digitalWrite(led_pin, HIGH);
  digitalWrite(led_pin2, HIGH);
  digitalWrite(pir_switch, HIGH);
}

void loop() {
  // Read device output if available.
  if (mySerial.available()) {
    while(mySerial.available()) { // While there is more to be read, keep reading.
      delay(3);
      command = (char)mySerial.read();
      readCharacters += command;
    }
  }

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
    if (readCharacters == "TO2"){
      digitalWrite(led_pin2, HIGH);
    }
    if (readCharacters == "TF2"){
      digitalWrite(led_pin2, LOW);
    }
    if (readCharacters == "PIROFF"){
      digitalWrite(pir_switch, LOW);
    }
    if (readCharacters == "PIRON"){
      digitalWrite(pir_switch, HIGH);
    }
    readCharacters = "";
  }
}
