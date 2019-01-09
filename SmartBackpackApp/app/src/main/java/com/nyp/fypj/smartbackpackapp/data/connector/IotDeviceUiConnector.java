package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.IotDeviceType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IotDeviceUiConnector extends EntityValueUiConnector {

    private IotDeviceType iotDeviceType;

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
         "ACTUATOR_LED",
         "ACTUATOR_DISPLAY",
         "ACTIVATED_ON"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public IotDeviceUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new IotDeviceType(true));
    }

    public IotDeviceUiConnector(SAPServiceManager sapServiceManager, IotDeviceType entity) {
        super(sapServiceManager);
        this.iotDeviceType = entity;
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
        propertyValues.put("SYSTEM_PLATFORM", iotDeviceType.getSystemPlatform());
        propertyValues.put("ACTUATOR_DISPLAY", iotDeviceType.getActuatorDisplay());
        propertyValues.put("MANUFACTURED_ON", String.valueOf(iotDeviceType.getManufacturedOn()));
        propertyValues.put("SENSOR_HUMIDITY", iotDeviceType.getSensorHumidity());
        propertyValues.put("SENSOR_BUTTON", iotDeviceType.getSensorButton());
        propertyValues.put("ACTUATOR_BUZZER", iotDeviceType.getActuatorBuzzer());
        propertyValues.put("SENSOR_TEMPERATURE", iotDeviceType.getSensorTemperature());
        propertyValues.put("APPLICATION_VERSION", iotDeviceType.getApplicationVersion());
        propertyValues.put("ACTUATOR_LED", iotDeviceType.getActuatorLed());
        propertyValues.put("DEVICE_SN", iotDeviceType.getDeviceSn());
        propertyValues.put("ACTIVATED_ON", String.valueOf(iotDeviceType.getActivatedOn()));
        propertyValues.put("SENSOR_AIR_QUALITY", iotDeviceType.getSensorAirQuality());
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
        return this.iotDeviceType;
    }
}
