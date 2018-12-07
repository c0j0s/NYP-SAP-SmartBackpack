# Smart Backpack
A NYP FYPJ collaboration project with SAP Digital Labs.  
Embedding technologies into a backpack designed for people with asmatic conditions.

## Software Architecture of SmartBackpack
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documents/software%20architecture.jpg)

## SmartBackpackIOT
Python server scripts for Raspberry Pi.

### Components of SmartBackpackIOT
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

## SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

## SAP 
Utilises SAP HANA DB for online sensor data storage.

