// This is the main Arduino sketch that I used for my Uno board
// In my case, my fitness bike has a switch that is triggered by a magnet on the wheel
// Everytime the magnet passes the switch, the hall sensor triggers an "interrupt"
// Different bikes may require different configurations, so you may need to code everything from scratch
// The only thing that the mod expects at this commit is that the arduino sends the
// data in a specific format. The format is as follows:
// #<speed>;<timeSinceLastTrigger>;<wheelRadius>*
// The speed is in km/h, timeSinceLastTrigger is in hours and wheelRadius is in meters
// It is important to keep the "#" and "*" characters as they are used to identify the start and end of the data
// Also, keep in mind that you should tell the mod when the bike is stopped by sending a speed of 0.00 with the same format.
// The mod uses "timeSinceLastTrigger" for calories calculations, but when the speed is zero it is ignored.

// This is the pin your hall sensor is connected to
// Assuming you don't need to adjust this code for your specific bike,
// you can connect the hall sensor to pin 2 and the ground
// The sensor in my bike has only two pins, so I connected the other pin to the ground
const int hallPin = 2;  // Adjust to your actual pin

// Adjust 'triggersPerRevolution' based on how many times the hall sensor triggers per wheel revolution
// In my case, my fitness bike triggers twice per wheel revolution
// To do that, you can do this:
// 1. Put a piece of color tape on the border of the wheel
// 2. Load into your arduino a sketch that prints the time between triggers (you can use the sketch named "hall_sensor_test.ino")
// 3. Spin the wheel very slowly and see how many times you see a high value between low values (so low-high-high-low still counts as 1 trigger)
// 4. Once you have the number of triggers per revolution, replace the value below
const double triggersPerRevolution = 2;  // Replace with your measured value

// Adjust 'radius' based on your bike's actual wheel radius in meters
// You can use a measuring tape, and measure how "high" the wheel is from the bottom to the top
// Then, divide by 2 to get the radius (since the diameter is twice the radius)
// Remember to convert your result to meters
const float radius = 0.135;
// This is used to calculate the distance moved by the bike
// The short math is that the circumference of a circle is 2*PI*radius
// Since the hall sensor triggers twice per revolution, we divide the circumference by 2
const double wheelCircumference = 2*PI*radius;
// Each wheel revolution triggers twice; divide circumference by 2
const float distancePerTrigger = (wheelCircumference / triggersPerRevolution);

volatile unsigned long lastTriggerTime = 0;  // Timestamp of the last magnetic trigger
volatile unsigned long triggerCount = 0;     // Count of the total wheel triggers
float speed = 0;                             // Current speed of the bike in km/h

// For the debounce:
// For example, in my case I don't get one single "HIGH" value, but a few of them
// So, unless the time between the last HIGH value and the current HIGH value is greater than 50ms, I ignore it
// This is something I fine-tuned in my bike, it might be different for you
// Though most likely it is not necessary to change this value
const int DEBOUNCE_THRESHOLD_MS = 50;  // Minimum time between triggers to count as valid

// This is something I also fine-tuned in my bike
// I tried a high value like 2000ms, then after pedaling and viewing the bike moving
// I suddenly stop and saw if it stopped moving "in a reasonable time"
// Then I adjusted this value until I was happy with the result
const int STOP_THRESHOLD_MS = 450;  // Time threshold to assume bike has stopped

void setup() {
  pinMode(hallPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(hallPin), magnet_detect, FALLING);
  // Take a close look at the baud rate; this is the value you will need to set in the mod
  Serial.begin(31250);
}

void loop() {
  unsigned long currentTime = millis();
  unsigned long timeSinceLastTrigger = currentTime - lastTriggerTime;


  if (lastTriggerTime == 0) {
    lastTriggerTime = currentTime;
  }

  if (currentTime - lastTriggerTime > STOP_THRESHOLD_MS && timeSinceLastTrigger > STOP_THRESHOLD_MS) {
     // Negative one will make the mod skip that value.
     Serial.print('#');
     Serial.print("0.00;-1.00;-1.00");
     Serial.print('*');

     // Minecraft processes at 20 ticks per second
     // or about 50ms per tick
     delay(50);
  }
}

// Function triggered by Hall sensor when the magnet passes
// Updates speed, distance, and calories, and sends data via serial communication
void magnet_detect() {
  unsigned long currentTime = millis();
  unsigned long timeSinceLastTrigger = currentTime - lastTriggerTime;

  // Debounce: ignore triggers that occur within DEBOUNCE_THRESHOLD_MS of the previous trigger
  if (timeSinceLastTrigger > DEBOUNCE_THRESHOLD_MS) {
    triggerCount++;

    float speed = (distancePerTrigger / timeSinceLastTrigger) * 3600;

    double hoursSinceLastTrigger = timeSinceLastTrigger / 3600000.0;  // Convert milliseconds to hours

    Serial.print('#');
    Serial.print(speed);
    Serial.print(";");
    Serial.print(hoursSinceLastTrigger);
    Serial.print(";");
    Serial.print(radius);
    Serial.print('*');

    lastTriggerTime = currentTime;
  }
}

/* Now calculated client-side. Leaving them here for future reference
const float bodyMass = 50.0;  // Your body mass in kg (this value is just a placeholder)

// Calculates the Metabolic Equivalent of Task (MET) based on speed in km/h
// MET is used to estimate the energy expenditure during cycling
float calculateMET(float speed) {
  // MET values based on cycling speed (km/h)
  // Though technically, these are based for a normal bicycle
  // which has a different resistance, and more relevant for this file
  // a different wheel radius. In my case, the speed is always less than 16 km/h
  // I will end up just doing this on the mod side so one can use any wheel size as reference.
  // I got these values from the Compendium of Physical Activities
  // https://pacompendium.com/bicycling/
  if (speed <= 15) return 5.8;
  else if (speed <= 19) return 6.8;
  else if (speed <= 22) return 8.0;
  else if (speed <= 25) return 10.0;
  else if (speed <= 30) return 12.0;
  else return 16.8;
}

// Calculates the calories burned using body weight, MET value, and time passed
float calculateCalories(float weight, float met, float hours) {
  // Calorie calculation formula: Calories = MET * weight (kg) * time (hours) * 1.05
  // To be honest, I don't remember where I got the 1.05 from, but it seems to work
  return met * weight * hours * 1.05;
}
*/