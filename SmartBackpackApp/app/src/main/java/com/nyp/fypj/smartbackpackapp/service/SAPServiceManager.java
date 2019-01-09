package com.nyp.fypj.smartbackpackapp.service;

import com.sap.cloud.android.odata.sbp.sbp;
import com.nyp.fypj.smartbackpackapp.app.ConfigurationData;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.odata.OnlineODataProvider;
import com.sap.cloud.mobile.odata.core.Action0;
import com.sap.cloud.mobile.odata.http.OKHttpHandler;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;

public class SAPServiceManager {

    private final ConfigurationData configurationData;
    private OnlineODataProvider provider;
    private String serviceRoot;
    private sbp sbp;
    public static final String CONNECTION_ID_SBP = "SmartBackpack";

    public SAPServiceManager(ConfigurationData configurationData) {
        this.configurationData = configurationData;
    }

    public void openODataStore(Action0 callback) {
        if (configurationData.loadData()) {
            String serviceUrl = configurationData.getServiceUrl();
            provider = new OnlineODataProvider("SAPService", serviceUrl + CONNECTION_ID_SBP);
            provider.getNetworkOptions().setHttpHandler(new OKHttpHandler(ClientProvider.get()));
            provider.getServiceOptions().setCheckVersion(false);
            provider.getServiceOptions().setRequiresType(true);
            provider.getServiceOptions().setCacheMetadata(false);
            sbp = new sbp(provider);

        }
        callback.call();
    }

    public String getServiceRoot() {
        if (serviceRoot == null) {
            if (sbp == null) {
                throw new IllegalStateException("SAPServiceManager was not initialized");
            }
            provider = (OnlineODataProvider)sbp.getProvider();
            serviceRoot = provider.getServiceRoot();
        }
        return serviceRoot;
    }

    // This getter is used for the master-detail ui generation
    public sbp getsbp() {
        if (sbp == null) {
            throw new IllegalStateException("SAPServiceManager was not initialized");
        }
        return sbp;
    }

}