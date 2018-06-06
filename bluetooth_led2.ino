#include <SoftwareSerial.h>
SoftwareSerial mySerial(4, 2); // RX, TX

String command = ""; // Stores response of the HC-06 Bluetooth device

int led_pin = 7;

String readCharacters = "";


void setup() {
  // Open serial communications:
  Serial.begin(19200);
  
  // The HC-06 defaults to 9600 according to the datasheet.
  mySerial.begin(9600);

  pinMode(led_pin,OUTPUT);
  digitalWrite(led_pin, HIGH);
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
  
  // Read user input if available.
  if (readCharacters.length() > 0){
    Serial.println(readCharacters);
    if (readCharacters == "TO"){
      digitalWrite(led_pin, HIGH);
    }
    if (readCharacters == "TF"){
      digitalWrite(led_pin, LOW);
    }
    readCharacters = "";
  }
}
