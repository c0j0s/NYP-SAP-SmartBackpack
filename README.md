# Smart Backpack
A NYP FYPJ collaboration project with SAP Digital Labs.  
Embedding technologies into a backpack designed for hikers people with asthmatic conditions.

## Overview of Smart Backpack Project
![service overview](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/0_service_overview.jpegg)  

__Technical Overview__  
![technical overview](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/1_integrated_overview.jpeg)


### SmartBackpackIOT
Daemon Services writen in Python for Raspberry Pi to handle sensor data and communication with Android App.

### SmartBackpackApp
An Android companion app that is responsible for user interactions with SmartBackpackIOT, and SAP Services as well.

### SmartBackpackML
A simple tensorflow keras model trained on generated data to demonstrate the complete Smart Backpack solution

### SAP Cloud Platform
Utilises SAP Cloud Platform Services to power IOT Solution.
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
- SAP Cloud Platform Android mobile SDK  
https://help.sap.com/doc/c2d571df73104f72b9f1b73e06c5609a/Latest/en-US/docs/index.html 

- Android + Bluetooth  
https://github.com/googlesamples/android-BluetoothChat

- Android Fragments + Tabbed Layout  
https://medium.com/@oluwabukunmi.aluko/bottom-navigation-view-with-fragments-a074bfd08711

#### SmartBackpackML
- Tensorflow  
https://www.tensorflow.org/tutorials/eager/custom_training_walkthrough    
https://www.tensorflow.org/guide/keras 

- Reference  
https://www.health.nsw.gov.au/environment/air/Pages/who-is-affected.aspx  

#### SmartBackpackSAP
- SAPUI5  
https://openui5.hana.ondemand.com/#

- HANA + OData  
https://blogs.sap.com/2018/07/02/how-to-expose-a-hana-table-via-odata/  
https://blogs.sap.com/2016/02/08/odata-everything-that-you-need-to-know-part-1/  
https://www.youtube.com/watch?v=Dv3bf91_UfE