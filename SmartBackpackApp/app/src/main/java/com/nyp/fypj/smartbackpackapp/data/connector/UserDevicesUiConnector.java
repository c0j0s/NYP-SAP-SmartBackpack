package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.UserDevicesType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDevicesUiConnector extends EntityValueUiConnector {

    private UserDevicesType userDevicesType;

     private final String[] keyNames = {
     "USER_ID",
     "DEVICE_SN"
      };
     // TODO: Change masterPropertyName value to display the desired property in Item list screen
     private final String masterPropertyName = "DEVICE_NAME";
     // ordered, key fields are first
     private final String[] propertyNames = {
         "USER_ID",
         "DEVICE_SN",
         "DEVICE_NAME",
         "REGISTERED_ON",
         "CONFIG_ENABLE_BUZZER",
         "CONFIG_ENABLE_LED",
         "MINUTES_TO_RECORD_DATA",
         "LAST_ONLINE",
         "CONFIG_UPDATED_ON"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public UserDevicesUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new UserDevicesType(true));
    }

    public UserDevicesUiConnector(SAPServiceManager sapServiceManager, UserDevicesType entity) {
        super(sapServiceManager);
        this.userDevicesType = entity;
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
        propertyValues.put("CONFIG_ENABLE_LED", userDevicesType.getConfigEnableLed());
        propertyValues.put("CONFIG_UPDATED_ON", String.valueOf(userDevicesType.getConfigUpdatedOn()));
        propertyValues.put("LAST_ONLINE", String.valueOf(userDevicesType.getLastOnline()));
        propertyValues.put("USER_ID", userDevicesType.getUserId());
        propertyValues.put("DEVICE_NAME", userDevicesType.getDeviceName());
        propertyValues.put("MINUTES_TO_RECORD_DATA", String.valueOf(userDevicesType.getMinutesToRecordData()));
        propertyValues.put("DEVICE_SN", userDevicesType.getDeviceSn());
        propertyValues.put("CONFIG_ENABLE_BUZZER", userDevicesType.getConfigEnableBuzzer());
        propertyValues.put("REGISTERED_ON", String.valueOf(userDevicesType.getRegisteredOn()));
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
        return this.userDevicesType;
    }
}
