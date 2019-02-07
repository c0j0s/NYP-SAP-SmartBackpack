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
| 12000 | restart bluetooth server   ||
| 12500 | get the status of bluetooth server          | Toast |
| 30000 | get real time sensor reading                | Display in home page |
| 31000 | set user perferences       ||
| 32000 | get holding_zone data                       | Upload to Hana DB |
| 32000 | flush holding_zone                          | Toast |
| 41000 | toggle server debug mode                    | Toast |
| 42000 | execute custom shell commands               ||
| 43000 | get IOT Device network IP address        ||
| 99999 | Error Occurred in IOT Device               | Toast |

## Wrapper Class Implementation Guide
### BtWrapper
A wrapper class that encapsulates bluetooth command syntax into simple functions for front-end implementation.  

#### Sample usage
```kotlin
    val btWrapper = BtWrapper(<UiHandler>)
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

    fun getNetworkIp()

    fun restartDevice()

    fun exeShCommand(command:String)

    fun elseTest()
```

### IotDeviceConfigManager
A wrapper class that handles functions to change IOT device configs.json and functions to sync the data to cloud database.  

#### Sample usage
```kotlin
    val iotDeviceConfigManager = IotDeviceConfigManager(<BtWrapper>,<SapServiceManager>,<userId>,<deviceSn>)

    //adds the configs to change list
    iotDeviceConfigManager.toggleBuzzer(true);
    .
    .

    //execute the final changes and status feedback will be handled in handlers configured for BtWrapper
    iotDeviceConfigManager.commitChanges()

    //sync to database
    iotDeviceConfigManager.syncConfigToHana({
        //success callback
    },{ e:RuntimeException ->
        //error callback
    })
```

#### Functions
```kotlin
    fun syncConfigToHana(success: () -> Unit,error: (e:RuntimeException) -> Unit)

    fun toggleBuzzer(enable:Boolean)

    fun toggleLed(enable:Boolean)

    fun changeHoldingZoneRecordInterval(minutes:Int)

    fun toggleBuzzerNow(enable:Boolean)

    fun toggleLedNow(enable:Boolean)

    fun commitChanges()
```

### IotDataMLServiceManager
A wrapper class that update user feedback of specific iot data to database, and functions to comunicate with online ml service.
#### Sample usage
```kotlin
    val iotDataMLServiceManager = IotDataMLServiceManager(<SapServiceManager>)

    //set feedback
    iotDataMLServiceManager.setDataFeedback(1)

    //get ml prediction
    val iotDataType = IotDataType()
        iotDataType.humidity = 50.0
        iotDataType.temperature = 30.0
        iotDataType.pm25 = 100.0
        iotDataType.pm10 = 104.0

    iotDataMLServiceManager.getLevelAndSuggestion(userProfile!!,iotDataType,{
        level, suggestion ->
        ...
    },{
        e: RuntimeException ->
        ...
    })
```

#### Functions
```kotlin
    fun setDataFeedback(level:Int,success:() -> Unit,error:(e:RuntimeException) -> Unit)

    fun getFeedbackLabel(level:Int):String

    fun getLevelAndSuggestion(user: UserinfosType, data: IotDataType, success: (level:Int,suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit)

    fun getSuggestions(suggestionQuery: DataQuery,success:(suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit)

    fun predictComfortLevel(data: IotDataType, user: UserinfosType, success: (level:Int) -> Unit, error: (e: IOException) -> Unit)
```