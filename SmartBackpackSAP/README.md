# SAP HANA Database 
HANA_Database repo contains archives for database configuration, structures and data.

## Database Structure 
![Database Structure ](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/0_hana_database_structure.jpeg)

## Database Tables
USER_PROFILE:  
Table for user profile information

USER_DEVICES:  
Table for user and devices relation information

IOT_DEVICE:  
Table for list of IOT devices

IOT_DATA:  
Table for all IOT sensor data

SUGGESTIONS:  
Table for all comfort level suggestions

## HANA XS OData
Database OData access API:  
```
https://<subaccountid>.ap1.hana.ondemand.com/NYPFYPJ_SMARTBACKPACK/sbp.xsodata/<TableName>
```

### Exposed OData
Table access:  
- user
- userDevices
- iotDevice
- iotData
- suggestions

Attribute view access:  
- userinfos  
  - User detail view with computed attributes e.g. __age__
- iotdeviceinfo
  - Iot device detail view with device configurations values for each user
- incident
  - Data view for alert triggered data

Analytic view access:  
- incidentMap
  - Data view with geographical information

## Configuration of SAP HANA Development tool for Eclipse IDE 
### Prerequisites
- Eclipse Photon  
download from official Eclipse website
- SAP HANA Development Tools  
 https://tools.hana.ondemand.com/Neon  
__Old version__ is used as new version removed some options related to creating connection with SAP Cloud
__"Create Cloud System"__ option is not visible in the new version
- Install SAP HANA and cloud platform plugins 

### Procedures
1. Change Perspective
2. Change to system tab
3. Right Click "Create Cloud System"
4. Enter SAP Cloud Platform credentials  
(Subaccount name can be found in SAP Cloud Platform Cockpit)
5. Enter database credentials
6. Eclipse IDE setup complete
	
# SAPUI5 Webapp 
HANA_Webapp is a backup repo for the admin portal webapps  

## Webapps Features
Dashboard:
- Incident view
- List of users
- List of device
- List of IOT data

Admin Controls:  
- Users and user profile overview  
- User device details and IOT data view
- Data analytical view 
