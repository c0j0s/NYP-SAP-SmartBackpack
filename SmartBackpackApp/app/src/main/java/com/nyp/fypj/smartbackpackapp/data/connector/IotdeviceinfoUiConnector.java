package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IotdeviceinfoUiConnector extends EntityValueUiConnector {

    private IotdeviceinfoType iotdeviceinfoType;

     private final String[] keyNames = {
     "DEVICE_SN"
      };
     // TODO: Change masterPropertyName value to display the desired property in Item list screen
     private final String masterPropertyName = "MANUFACTURED_ON";
     // ordered, key fields are first
     private final String[] propertyNames = {
         "DEVICE_SN",
         "MANUFACTURED_ON",
         "SYSTEM_PLATFORM",
         "APPLICATION_VERSION",
         "SENSOR_HUMIDITY",
         "SENSOR_TEMPERATURE",
         "SENSOR_BUTTON",
         "SENSOR_AIR_QUALITY",
         "ACTUATOR_BUZZER",
         "ACTUATOR_DISPLAY",
         "ACTUATOR_LED",
         "USER_ID",
         "DEVICE_NAME",
         "REGISTERED_ON",
         "CONFIG_ENABLE_BUZZER",
         "CONFIG_ENABLE_LED",
         "MINUTES_TO_RECORD_DATA",
         "LAST_ONLINE",
         "CONFIG_UPDATED_ON",
         "ACTIVATED_ON"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public IotdeviceinfoUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new IotdeviceinfoType(true));
    }

    public IotdeviceinfoUiConnector(SAPServiceManager sapServiceManager, IotdeviceinfoType entity) {
        super(sapServiceManager);
        this.iotdeviceinfoType = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMasterPropertyName() {
        return masterPropertyName;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeyPropertyNames() {
        return Arrays.asList(keyNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPropertiesWithValues() {
            Map<String, String> propertyValues = new HashMap<>();
        propertyValues.put("CONFIG_ENABLE_LED", iotdeviceinfoType.getConfigEnableLed());
        propertyValues.put("SYSTEM_PLATFORM", iotdeviceinfoType.getSystemPlatform());
        propertyValues.put("ACTUATOR_DISPLAY", iotdeviceinfoType.getActuatorDisplay());
        propertyValues.put("MANUFACTURED_ON", String.valueOf(iotdeviceinfoType.getManufacturedOn()));
        propertyValues.put("SENSOR_HUMIDITY", iotdeviceinfoType.getSensorHumidity());
        propertyValues.put("CONFIG_UPDATED_ON", String.valueOf(iotdeviceinfoType.getConfigUpdatedOn()));
        propertyValues.put("SENSOR_BUTTON", iotdeviceinfoType.getSensorButton());
        propertyValues.put("USER_ID", iotdeviceinfoType.getUserId());
        propertyValues.put("DEVICE_NAME", iotdeviceinfoType.getDeviceName());
        propertyValues.put("ACTUATOR_BUZZER", iotdeviceinfoType.getActuatorBuzzer());
        propertyValues.put("CONFIG_ENABLE_BUZZER", iotdeviceinfoType.getConfigEnableBuzzer());
        propertyValues.put("SENSOR_TEMPERATURE", iotdeviceinfoType.getSensorTemperature());
        propertyValues.put("REGISTERED_ON", String.valueOf(iotdeviceinfoType.getRegisteredOn()));
        propertyValues.put("LAST_ONLINE", String.valueOf(iotdeviceinfoType.getLastOnline()));
        propertyValues.put("APPLICATION_VERSION", iotdeviceinfoType.getApplicationVersion());
        propertyValues.put("MINUTES_TO_RECORD_DATA", String.valueOf(iotdeviceinfoType.getMinutesToRecordData()));
        propertyValues.put("ACTUATOR_LED", iotdeviceinfoType.getActuatorLed());
        propertyValues.put("DEVICE_SN", iotdeviceinfoType.getDeviceSn());
        propertyValues.put("ACTIVATED_ON", String.valueOf(iotdeviceinfoType.getActivatedOn()));
        propertyValues.put("SENSOR_AIR_QUALITY", iotdeviceinfoType.getSensorAirQuality());
      return propertyValues;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public List<String> getPropertyNames() {
         return Arrays.asList(propertyNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityValue getConnectedObject() {
        return this.iotdeviceinfoType;
    }
}
