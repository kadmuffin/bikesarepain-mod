const int hallPin = 2;  // Adjust to the pin where your hall sensor is connected

volatile unsigned int triggerCount = 0;  // Counter for number of sensor triggers
volatile unsigned long lastTriggerTime = 0;  // Time of the last valid trigger
const int DEBOUNCE_THRESHOLD_MS = 60;  // Minimum time between triggers to count as valid (in milliseconds)

bool countingRevolution = false;  // Tracks if we're currently counting a revolution

void setup() {
  Serial.begin(9600);  // Open serial communication at a baud rate of 9600
  pinMode(hallPin, INPUT_PULLUP);  // Set hall sensor pin as input with internal pull-up resistor
  attachInterrupt(digitalPinToInterrupt(hallPin), magnet_detect, FALLING);  // Trigger interrupt on falling signal from hall sensor

  Serial.println("Start rotating the wheel to count triggers per revolution.");
  Serial.println("Use a reference point on the wheel (like a piece of tape).");
  Serial.println("Press 'r' to start counting triggers and 's' to stop and display the count.");
}

void loop() {
  // Manually reset the count and start a new revolution by pressing a key in Serial Monitor
  if (Serial.available() > 0) {
    char input = Serial.read();
    if (input == 'r' || input == 'R') {
      triggerCount = 0;  // Reset trigger count
      countingRevolution = true;
      Serial.println("Counting triggers for one full wheel revolution. Spin the wheel now.");
    } else if (input == 's' || input == 'S') {
      countingRevolution = false;
      Serial.print("Total triggers for one full revolution: ");
      Serial.println(triggerCount);
      Serial.println("Press 'r' to reset and start counting again.");
    }
  }
}

// This function is called every time the magnet passes the hall sensor
void magnet_detect() {
  unsigned long currentTime = millis();  // Get the current time in milliseconds

  // Debounce logic: Only count a trigger if enough time has passed since the last trigger
  if (countingRevolution && (currentTime - lastTriggerTime) > DEBOUNCE_THRESHOLD_MS) {
    triggerCount++;  // Increment trigger count each time the sensor is triggered
    lastTriggerTime = currentTime;  // Update the last trigger time to the current time
  }
}
