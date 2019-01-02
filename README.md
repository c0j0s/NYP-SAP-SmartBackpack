# Smart Backpack
A NYP FYPJ collaboration project with SAP Digital Labs.  
Embedding technologies into a backpack designed for people with asthmatic conditions.

## Overview of Smart Backpack Project
![overview](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/1_integrated_overview.jpeg)

### SmartBackpackIOT
Services writen in Python for Raspberry Pi to handle sensor data and communication with Android App.

### SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

### SmartBackpackML
Pending review

### SAP Cloud Platform
Utilises SAP Cloud Platform Services to power IOT Device.
- SAPUI5 Webapps for dashboard and data analytic presentations.
- HANA DB for online sensor data storage.

---
___Details can be found in README.md in each subfolders.___

## Reference Sources
#### SmartBackpackIOT
- Python + Bluetooth  
http://blog.kevindoran.co/bluetooth-programming-with-python-3/

- GrovePi+  
http://wiki.seeedstudio.com/

#### SmartBackpackApp
- Android + Bluetooth  
https://github.com/googlesamples/android-BluetoothChat

#### SmartBackpackSAP
- SAPUI5  
https://openui5.hana.ondemand.com/#

- SAP HCP IOT  
https://sap.github.io/cloud-s4ext/week-5/unit-1/  
https://blogs.sap.com/2016/11/07/sap-hcp-internet-of-thingsiot-service-cookbook/

- ML & SAP Leonardo Machine Learning Foundation  
https://www.tensorflow.org/tutorials/eager/custom_training_walkthrough  
https://www.sap.com/products/leonardo/machine-learning.html  

- IOT + HANA  
https://blogs.sap.com/2018/07/02/how-to-expose-a-hana-table-via-odata/

- OData  
https://blogs.sap.com/2016/02/08/odata-everything-that-you-need-to-know-part-1/  
https://www.youtube.com/watch?v=Dv3bf91_UfE