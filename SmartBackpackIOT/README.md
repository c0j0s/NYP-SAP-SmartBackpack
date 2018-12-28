# SmartBackpackIOT
Services writen in Python for Raspberry Pi to handle sensor data and communication with Android App.

## Software Architecture of SmartBackpackIOT
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documents/2_iot_software_architecture.jpeg)

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

# SmartBackpackIOT Specifications
## Runtime Environment
Environment:  
- Python3
- Redis
- ...
	
Python packages: (install through pip3)  
- GrovePi
- Pybluez
- Redis
- ...
	
Others:
- SSH Connections
- SFTP Connections

## Start Services Manually
1. Change to service folder directory
    ```sh
    $ cd /home/pi/Smartbackpack
    ```
2. Execute following command
    ```sh
    $ Python3 SBP_Sensor_Server.py
    $ sudo Python3 SBP_BT_Server.py
    $ Python3 SBP_Service_Monitor.py
    ```
## Config.json Specifications
```json
{
    "device":{
        //Device Configurations
    },
    "env":{
        //Environment Configurations
    },
    "settings":{
        //Service options
        "device_sn":"SBPSG000001",
        "debug":true,
        "BT_Server_Settings":{
            "clear_holding_zone_after_sync":1
        },
        "Sensor_Server_Settings":{
            //User perference options, can be customised
            "enable_buzzer":0,
            "enable_led":0,
            "minute_to_record_data":0.2,
            "seconds_to_update_data":5,
            "humidity_range":{
                //Not final
            }
        }
    }
}
```

## Bluetooth Commands
| BT Commands              | Functionality                               | Sample Output  |
| -------------------------|:---------------------------------------     | --------------------:|
| get_sensor_status        | get the status of sensor server             | Active/dead |
| get_bt_status            | get the status of bluetooth server          | Active/dead |
| get_sensor_data          | get real time sensor reading                | JSON:  {humidity:0,temperature:0}|
| set_user_config_(JSON)   | set user perferences [NOT IMPLEMENTED]      |Success/fail|
| sync_holding_zone        | get holding_zone SQL data                   | INSERT INTO...|
| cmd_reboot_now           | restart device                              | - |
| cmd_reboot_sensor_server | restart sensor server                       | Active/dead |
| cmd_reboot_bt_server     | restart bluetooth server [NOT IMPLEMENTED]  | - |
| cmd_disconnect           | disconnect bluetooth connections            | Disconnected |
| cmd_toggle_debug         | toggle server debug mode                    | Debug:True/False |
| sh_(Bash commands)       | execute custom shell commands               | [Command output] |

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