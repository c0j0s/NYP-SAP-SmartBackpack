# SAP HANA Database 
HANA_Database repo contains archives for database configuration, structures and data.

## Database Struture 
![Database Struture ](https://github.com/c0j0s/SmartBackpack/blob/master/Documentations/0_hana_database_structure.jpeg)

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
Database Odata access API:  
```
https://<subaccountid>.ap1.hana.ondemand.com/NYPFYPJ_SMARTBACKPACK/sbp.xsodata/<TableName>
```

### Exposed Odata
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
