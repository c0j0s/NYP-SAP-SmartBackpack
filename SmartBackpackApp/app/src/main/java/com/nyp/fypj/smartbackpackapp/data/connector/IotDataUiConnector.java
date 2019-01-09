package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.IotDataType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IotDataUiConnector extends EntityValueUiConnector {

    private IotDataType iotDataType;

     private final String[] keyNames = {
     "DATA_ID"
      };
     // TODO: Change masterPropertyName value to display the desired property in Item list screen
     private final String masterPropertyName = "DEVICE_SN";
     // ordered, key fields are first
     private final String[] propertyNames = {
         "DATA_ID",
         "DEVICE_SN",
         "USER_ID",
         "RECORDED_ON",
         "HUMIDITY",
         "TEMPERATURE",
         "PM2_5",
         "PM10",
         "PREDICTED_COMFORT_LEVEL",
         "USER_FEEDBACK_COMFORT_LEVEL",
         "ALERT_TRIGGERED",
         "COUNTRY",
         "STATE",
         "CITY",
         "GEO_LAT",
         "GEO_LNG",
         "COUNTRY_CODE"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public IotDataUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new IotDataType(true));
    }

    public IotDataUiConnector(SAPServiceManager sapServiceManager, IotDataType entity) {
        super(sapServiceManager);
        this.iotDataType = entity;
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
        propertyValues.put("PM2_5", String.valueOf(iotDataType.getPm25()));
        propertyValues.put("USER_FEEDBACK_COMFORT_LEVEL", String.valueOf(iotDataType.getUserFeedbackComfortLevel()));
        propertyValues.put("DATA_ID", String.valueOf(iotDataType.getDataId()));
        propertyValues.put("HUMIDITY", String.valueOf(iotDataType.getHumidity()));
        propertyValues.put("USER_ID", iotDataType.getUserId());
        propertyValues.put("RECORDED_ON", String.valueOf(iotDataType.getRecordedOn()));
        propertyValues.put("STATE", iotDataType.getState());
        propertyValues.put("GEO_LNG", String.valueOf(iotDataType.getGeoLng()));
        propertyValues.put("ALERT_TRIGGERED", iotDataType.getAlertTriggered());
        propertyValues.put("PREDICTED_COMFORT_LEVEL", String.valueOf(iotDataType.getPredictedComfortLevel()));
        propertyValues.put("GEO_LAT", String.valueOf(iotDataType.getGeoLat()));
        propertyValues.put("TEMPERATURE", String.valueOf(iotDataType.getTemperature()));
        propertyValues.put("COUNTRY", iotDataType.getCountry());
        propertyValues.put("CITY", iotDataType.getCity());
        propertyValues.put("PM10", String.valueOf(iotDataType.getPm10()));
        propertyValues.put("DEVICE_SN", iotDataType.getDeviceSn());
        propertyValues.put("COUNTRY_CODE", iotDataType.getCountryCode());
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
        return this.iotDataType;
    }
}
