# SmartBackpackML
The machine learning component of the entire SmartBackpack solution. 

Due to technical competency level and time constraint, this component might remain in prototype phase.  

The resources in this folder might be the remains of experimentations and technical research projects, therefore it would not have any implementation values.

## Proposed Implementation Plan for SmartBackpackML
![Proposed Implementation Plan for SmartBackpackML](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/5_ml_implementation_overview.jpeg)

Dataset Consist of 2 components, __user profile__ and __sensor reading__.

__user profile:__  
Age, Gender, Asthmatic condition severity and Race

__sensor reading:__  
Humidity, Temperature, PM2.5 and PM10

## Proposed Values to User
Through __Machine Learning__ and __Big Data__, the model should output comfort levels based on user profile and sensor readings, to inform the user whether the environment is suitable for his/her asthmatic conditions. 

Next, the service can provide useful information such as "how to prevent/protect him/herself from the environment, what to bring for his trip... etc", to the user through the Android Companion App

## Demo Model
The demo model will be trained using generated datasets with limited input features.  

__Input features:__  
Asthmatic condition severity, Humidity, Temperature, PM2.5 and PM10  

Due to the limited input features, the defined target audience will be limited to adults(Both gender) with different severity of asthmatic condition.

__Generated Dataset:__  
All input features except user feedback comfort level will be generated randomly with defined limits configured in the manifest file.

user feedback comfort level will be determined by inputting the random values generated through handcrafted conditions to output level value.

__Training and Testing Demo Model__  
SmartBackpack_ML_Keras_Training and SmartBackpack_ML_Keras_Predict is the model training and testing scripts respectively  

__Deploying Demo Model__  
SmartBackpack_ML_Keras_Server is a python flask server that loads pre-trained demo models and expose the inputs to http requestes for SmartBackpackApp to interact with.  

To start flask service:  
```sh
    cd SmartBackpackIOT/
    sudo python3 SmartBackpack_ML_Keras_Server.py >/dev/null 2>&1 &
```

# Deploying Model
Due to the technical difficulties for deploying the model directly in the mobile app using tflite format, the demo model is been deployed in a virtual machine instance running on Google Cloud Platform. If needed the model can be deployed at other machine as well.

__Deployment environment:__  
Python 3 with following packages
- flask
- flask_httpauth
- tensorflow

__Start up Server:__  
You will have to upload SmartbackpackML into virtual machine.  
```sh
$ cd SmartbackpackML
#for production
$ sudo python3 SmartBackpack_ML_Keras_Server.py >/dev/null 2>&1 &

#for debugging
$ sudo python3 SmartBackpack_ML_Keras_Server.py
```
__Service Access__
```
http://<vm static ip>/predict
method:post

authentcation:basic
mlservice
passwd

body:json
{
    "HUMIDITY":0,
    "TEMPERATURE":0,
    "PM2_5":0,
    "PM10":0,
    "ASTHMATIC_LEVEL":0
}
```

# Connect SmartBackpackApp to deployed model
In order to connect the app to the ml server, the access url in the app must be updated.

File to change: service > IotDataMlServiceManager.kt
```kotlin
class IotDataMLServiceManager(...) {

    ...
    private val mlServiceUrl = "http://<vm static ip>/predict"
    private val mlServiceAccount = "mlservice"
    private val mlServicePassword = "passwd"
    ...

}
```