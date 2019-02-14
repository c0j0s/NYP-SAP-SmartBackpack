# SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

## Software Architecture of SmartBackpackApp
![software architecture](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/3_android_structure.jpeg)

### IOT Communication layer (Back-end)
![IOT Communication layer](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/4_android_bt_flow.jpeg)

### Android App Handler Action For IOT Data Function Codes Received  
| Function Code  | Description                        | Action      | 
|:-------------- |:-----------------------------------|:--------- |
| 00000 | terminate Bluetooth connections             | Toast |
| 10000 | restart device                              | Toast |
| 11000 | restart sensor server                       ||
| 11500 | get the status of sensor server             | Toast |
| 12000 | restart Bluetooth server   ||
| 12500 | get the status of Bluetooth server          | Toast |
| 30000 | get real time sensor reading                | Display in home page |
| 31000 | set user preferences       ||
| 32000 | get holding_zone data                       | Upload to Hana DB |
| 32500 | flush holding_zone                          | Toast |
| 41000 | toggle server debug mode                    | Toast |
| 42000 | execute custom shell commands               ||
| 43000 | get IOT Device network IP address        ||
| 44000 | activate the buzzer for 1 second        ||
| 99999 | Error Occurred in IOT Device               | Toast |

## Back-end Wrapper Class Implementation Guide
### BtWrapper
A wrapper class that encapsulates Bluetooth command syntax into simple functions for front-end implementation.  

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
    iotDeviceConfigManager.toggleBuzzerNow(true);
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
A wrapper class that update user feedback of specific iot data to database, and functions to communicate with online ml service. Here you will need to provide the hosted ml access url and login credentials
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

# SAP Cloud Platform Mobile SDK Implementation
The follow is the documentation for implementing SAP SCP Mobile SDK

## Initial Setup
A SAP Project Creation Wizard is provided by the SDK to create the project. The urls can be found in the mobile service portal in SAP Cloud Platform under important links. Here you can create a new mobile project of create from predefined projects in the mobile service portal.  

![wizard 1](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/MobileSDK/1_setup_wizard.png)  

Here you will supply the database OData access url for the wizard to retrieve the metadata file and generate all entity classes based on the tables defined in OData, the entity classes is needed when interacting with the database.  

![wizard 2](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/MobileSDK/2_setup_wizard.png)  
the metadata file can be found in android > res > raw > [OData name].xml  

## SAP Services
The following are some of the functionalities and services provided by the SDK for app development, the full list of services can be found in the official SDK website, here I am only describing some of the services I research and implemented during development.

__SDK Services:__  
- __User Authentication and App Security:__  
SAP Cloud Platform account authentication  
App lock with fingerprint capabilities

- __BasicAuthPersistentCredentialStore__:  
This object contains user login credentials like user email and password, this can be use if you require authentication in other component of the mobile app, for example retrieving data from database using the user email.

- __SAPServiceManager__:  
This is a utility class to establish session with the database. By using the openODataStore() function, this creates an database connection session where you can execute the database query to retrieve your result. You can also retrieve your database connection url using the get<Name>() function which is needed to execute database queries.

- __SecureStoreManager__:  
This object is a utility class that provide secure key-value pair data storage for the mobile app.

- __ConfigurationData__:  
This object contains configuration data such as service url. (SAP mobile service url)

## Database Usage
When interacting with the database, always open a connection session using the openODataStore() function in SAPServiceManager, else null exception will be thrown. Also always using asynchronies function for database operations.  

__Query__   
```kotlin
sapServiceManager.openODataStore {

    /*
    Retrieve user profile from database
    */
    val USER_ID = "example"
    val userProfileQuery = DataQuery()
                            .filter(<GeneratedEntityClass>.userId.equal(USER_ID))

    sapServiceManager.getsbp().getUserinfosAsync(userProfileQuery,
    {userInfos:List<<GeneratedEntityClass>>->
        //Success callback
    },
    {re:RuntimeException->
        //Error callback
    })
}
```

__Create and Update__
```kotlin
sapServiceManager.openODataStore {

    /*
    Create

    <Generated Odata Function> = Odata name
    e.g. getsbp() when name is sbp
    */
    val demoEntity = GeneratedEntityClass()
    demoEntity.field = "example"

    sapServiceManager.get<Generated Odata Function>().createEntityAsync(demoEntity, {
        //Success callback
    }, { e: RuntimeException ->
        //Success callback
    })

    /*
    Update
    */
    demoEntity.field = "update example"

    sapServiceManager.get<Generated Odata Function>().updateEntityAsync(demoEntity, {
        //Success callback
    }, { e: RuntimeException ->
        //Success callback
    })
}
```

__Delete can be found under OData API section in official documentation__

## SAP Fiori UI Component
Here are some of the Fiori components used in the Mobile App
- ObjectHeader
- ProfileHeader
- FioriProgressBar
- GridTableRow
- Form
  - SimplePropertyFormCell
  - SwitchFormCell
  - SliderFormCell

__Details can be found in the official SDK documentation__  
Official Mobile SDK Documentation:  
https://help.sap.com/doc/c2d571df73104f72b9f1b73e06c5609a/Latest/en-US/docs/index.html 

__SDK Version Implemented: 1.1.14__