# SmartBackpackIOT
Services writen in Python for Raspberry Pi to handle sensor data and communication with Android App.

## Software Architecture of SmartBackpack
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documents/software%20architecture.jpg)

## Components of SmartBackpackIOT
The BT, Sensor and Service monitor processes will startup automatically on boot.

BT Server:  
A bluetooth server that is responsible for communicating with the Android companion app.

Sensor Server:  
Responsible for handling sensor readings.

Service Monitor:  
A monitoring service to ensure the BT and Sensor services are always online.

Data storage:  
redis for short-term real time sensor readings.  
holding_zone for long-term sensor readings in SQL format.

Config.json:  
A configuration file that controls the behaviour of BT and Sensor Server.  
e.g. debug mode, buzzer toogle, reading interval control.  

# Implementation Specifications
## Bluetooth Commands

# SmartBackpackIOT Project Structures
Main Server Scripts:  
- SBP_BT_Server.py  
- SBP_Sensor_Server.py  
- SBP_Service_Monitor.py  

Data and configuration storage:  
- config.json  
- holding_zone

/lib  
contains sensor libraries and wrapper classes.

/log  
contains empty file for logging, require setup in `/etc/rsyslog.d/rsyslog.conf`.

/src  
contains reference source for configuraton.

/utils  
contains scripts to setup the runtime environment.

/unitTesting  
for testing purposes.