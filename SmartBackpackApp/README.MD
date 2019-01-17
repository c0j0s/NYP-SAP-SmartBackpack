# SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

## Software Architecture of SmartBackpackApp
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/3_android_structure.jpeg)

### IOT Communication layer
![IOT Communication layer](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/4_android_bt_flow.jpeg)

### Android App Handler Action For IOT Data Function Codes Received:  
| Function Code  | Description                        | Action      | 
|:-------------- |:-----------------------------------|:--------- |
| 00000 | terminate bluetooth connections             | Toast |
| 10000 | restart device                              | Toast |
| 11000 | restart sensor server                       ||
| 11500 | get the status of sensor server             | Toast |
| 12000 | restart bluetooth server [NOT IMPLEMENTED]  ||
| 12500 | get the status of bluetooth server          | Toast |
| 30000 | get real time sensor reading                | Display in home page |
| 31000 | set user perferences [NOT IMPLEMENTED]      ||
| 32000 | get holding_zone data                       | Upload to Hana DB |
| 32000 | flush holding_zone                          | Toast |
| 41000 | toggle server debug mode                    | Toast |
| 42000 | execute custom shell commands               ||
| 99999 | Error Occurred in IOT Device               | Toast |
