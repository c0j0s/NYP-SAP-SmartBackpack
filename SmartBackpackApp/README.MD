# SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

## Software Architecture of SmartBackpackApp
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/3_android_structure.jpeg)

### IOT Communication layer
![IOT Communication layer](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/4_android_bt_flow.jpeg)

### Android App Handler Action For IOT Data Function Codes Received  
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

## Wrapper Class Implementation Guide
### BtWrapper
A wrapper class that encapsulates bluetooth command syntax into simple functions for front-end implementation.  

#### Sample usage
```kotlin
    var btWrapper = BtWrapper(<UiHandler>)
    btWrapper.getSensorData()
```

#### Functions
```kotlin
    fun connectDevice(deviceBluetoothAddress:String)

    fun disconnectDevice()

    fun getSensorData()

    fun getSensorStatus()

    fun restartSensorService()

    fun getBluetoothStatus()

    fun restartBluetoothService()

    fun syncHoldingZone()

    fun flushHoldingZone()

    fun changeDeviceSettings(key:String,value:String)

    fun changeDeviceMultipleSettings(list:HashMap<String,String>)

    fun toggleDebug()

    fun restartDevice()

    fun exeShCommand(command:String)

    fun elseTest()
```

### IotDeviceConfigManager
A wrapper class that handles functions to change IOT device configs.json and functions to sync the data to cloud database.  

#### Sample usage
```kotlin
    var iotDeviceConfigManager = IotDeviceConfigManager(<BtWrapper>,<SapServiceManager>,<userId>,<deviceSn>)

    //adds the configs to change list
    iotDeviceConfigManager!!.toggleBuzzer(true);
    .
    .

    //execute the final changes and status feedback will be handled in handlers configured for BtWrapper
    iotDeviceConfigManager!!.commitChanges()

    //sync to database
    iotDeviceConfigManager!!.syncConfigToHana(
        {
            //success callback
        },
        { e:RuntimeException ->
            //error callback
        })
```

#### Functions
```kotlin
    fun syncConfigToHana(success: () -> Unit,error: (e:RuntimeException) -> Unit)

    fun toggleBuzzer(enable:Boolean)

    fun toggleLed(enable:Boolean)

    fun changeHoldingZoneRecordInterval(minutes:Int)

    fun commitChanges()
```

### IotDataFeedbackManager
A wrapper class that update user feedback of specific iot data to database.
#### Sample usage
```kotlin
    var iotDataFeedbackManager = IotDataFeedbackManager(<SapServiceManager>,<userId>,<dataId>)

    iotDataFeedbackManager!!.setDataFeedback(1)
```

#### Functions
```kotlin
    fun setDataFeedback(level:Int,success:() -> Unit,error:(e:RuntimeException) -> Unit)

    fun getFeedbackLabel(level:Int):String

    fun getSuggestions(suggestionQuery: DataQuery,success:(suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit)
```