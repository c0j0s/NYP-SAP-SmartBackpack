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