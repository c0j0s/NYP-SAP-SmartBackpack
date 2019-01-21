# SmartBackpackML
The machine learning component of the entire SmartBackpack solution. 

Due to technical competency level and time constraint, this component might remain in ideation, design and planing phase.  

The resources in this folder might be the remains of experimentations and technical research projects, therefore it would not have any implementation values.

## Proposed Implementation Plan for SmartBackpackML
![Proposed Implementation Plan for SmartBackpackML](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/5_ml_implementation_overview.jpeg)

Dataset Consist of 2 components, __user profile__ and __sensor reading__.

__user profile:__  
Age, Gender, Asthmatic condition severity and Race

__sensor reading:__  
Humidity, Temperature, PM2.5 and PM10

## Proposed Values to User
Through __Machine Learning__ and __Big Data__, the model should output comfort levels based on user profile and sensor readings, to inform the user whether the environment is suitable for his/her ashtmatic conditions. 

Next, the service can provide useful informations such as "how to prevent/protect him/herself from the environment, what to bring for his trip... etc", to the user through the Android Companion App

## Demo Model
The demo model will be trained using generated datasets with limited input features.  

__Input features:__  
Asthmatic condition severity, Humidity, Temperature, PM2.5 and PM10  

Due to the limited input features, the defined target audiance will be limited to adults(Both gender) with different serverity of asthmatic condition.

__Generated Dataset:__  
All input features except user feedback comfort level will be generated randonly with defined limits configured in the manifest file.

user feedback comfort level will be determinded by inputing the random values generated through handcrafted conditions to output level value.