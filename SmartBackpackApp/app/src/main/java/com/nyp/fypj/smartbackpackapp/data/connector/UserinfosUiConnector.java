package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.UserinfosType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserinfosUiConnector extends EntityValueUiConnector {

    private UserinfosType userinfosType;

     private final String[] keyNames = {
     "USER_ID"
      };
     // TODO: Change masterPropertyName value to display the desired property in Item list screen
     private final String masterPropertyName = "ROLE";
     // ordered, key fields are first
     private final String[] propertyNames = {
         "USER_ID",
         "ROLE",
         "NAME",
         "GENDER",
         "DOB",
         "RACE",
         "ASTHMATIC_LEVEL",
         "CONTACT_NO",
         "EMAIL",
         "REGISTERED_ON",
         "USER_CITY",
         "USER_STATE",
         "USER_COUNTRY",
         "USER_COUNTRY_CODE",
         "AGE"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public UserinfosUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new UserinfosType(true));
    }

    public UserinfosUiConnector(SAPServiceManager sapServiceManager, UserinfosType entity) {
        super(sapServiceManager);
        this.userinfosType = entity;
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
        propertyValues.put("CONTACT_NO", userinfosType.getContactNo());
        propertyValues.put("USER_COUNTRY_CODE", userinfosType.getUserCountryCode());
        propertyValues.put("ASTHMATIC_LEVEL", String.valueOf(userinfosType.getAsthmaticLevel()));
        propertyValues.put("USER_ID", userinfosType.getUserId());
        propertyValues.put("USER_COUNTRY", userinfosType.getUserCountry());
        propertyValues.put("USER_STATE", userinfosType.getUserState());
        propertyValues.put("EMAIL", userinfosType.getEmail());
        propertyValues.put("NAME", userinfosType.getName());
        propertyValues.put("REGISTERED_ON", String.valueOf(userinfosType.getRegisteredOn()));
        propertyValues.put("USER_CITY", userinfosType.getUserCity());
        propertyValues.put("ROLE", userinfosType.getRole());
        propertyValues.put("DOB", String.valueOf(userinfosType.getDob()));
        propertyValues.put("RACE", userinfosType.getRace());
        propertyValues.put("GENDER", userinfosType.getGender());
        propertyValues.put("AGE", String.valueOf(userinfosType.getAge()));
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
        return this.userinfosType;
    }
}
