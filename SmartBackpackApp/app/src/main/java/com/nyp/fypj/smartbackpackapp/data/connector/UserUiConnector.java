package com.nyp.fypj.smartbackpackapp.data.connector;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.android.odata.sbp.UserType;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserUiConnector extends EntityValueUiConnector {

    private UserType userType;

     private final String[] keyNames = {
     "USER_ID"
      };
     // TODO: Change masterPropertyName value to display the desired property in Item list screen
     private final String masterPropertyName = "DOB";
     // ordered, key fields are first
     private final String[] propertyNames = {
         "USER_ID",
         "DOB",
         "RACE",
         "ASTHMATIC_LEVEL",
         "CONTACT_NO",
         "EMAIL",
         "ROLE",
         "REGISTERED_ON",
         "NAME",
         "GENDER",
         "CITY",
         "STATE",
         "COUNTRY",
         "COUNTRY_CODE"
        };

    private Map<String, String> propertyValues = new HashMap<>();

    public UserUiConnector(SAPServiceManager sapServiceManager) {
        this(sapServiceManager, new UserType(true));
    }

    public UserUiConnector(SAPServiceManager sapServiceManager, UserType entity) {
        super(sapServiceManager);
        this.userType = entity;
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
        propertyValues.put("CONTACT_NO", userType.getContactNo());
        propertyValues.put("ASTHMATIC_LEVEL", String.valueOf(userType.getAsthmaticLevel()));
        propertyValues.put("USER_ID", userType.getUserId());
        propertyValues.put("STATE", userType.getState());
        propertyValues.put("EMAIL", userType.getEmail());
        propertyValues.put("REGISTERED_ON", String.valueOf(userType.getRegisteredOn()));
        propertyValues.put("NAME", userType.getName());
        propertyValues.put("ROLE", userType.getRole());
        propertyValues.put("CITY", userType.getCity());
        propertyValues.put("COUNTRY", userType.getCountry());
        propertyValues.put("DOB", String.valueOf(userType.getDob()));
        propertyValues.put("RACE", userType.getRace());
        propertyValues.put("GENDER", userType.getGender());
        propertyValues.put("COUNTRY_CODE", userType.getCountryCode());
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
        return this.userType;
    }
}
