package com.nyp.fypj.smartbackpackapp.data.operation;

import java.util.ArrayList;
import java.util.List;

import com.nyp.fypj.smartbackpackapp.mdui.EntitySetListActivity;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;

import com.sap.cloud.mobile.odata.http.HttpHeaders;
import com.sap.cloud.mobile.odata.DataQuery;

import com.nyp.fypj.smartbackpackapp.data.connector.IotDataUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.IotDeviceUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.IotdeviceinfoUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserDevicesUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserinfosUiConnector;
import com.sap.cloud.android.odata.sbp.sbp;

/**
 * Operation implementing OData entity downloads.
 */
public class DownloadOperation extends Operation {

    /**
     * Name of the entity set to download
     */
    private EntitySetListActivity.EntitySetName entitySetName;
    private final int maximumEntities = 200;

    /**
     * Creates a new operation that downloads an entity set using the specified service manager
     * and subsequently calls {@code callback}.
     *
     * @param sapServiceManager Service manager providing OData store access
     * @param callback Callback to call
     * @param entitySetName Name of the entity set to download
     */
    public DownloadOperation(SAPServiceManager sapServiceManager, OnODataOperations callback, EntitySetListActivity.EntitySetName entitySetName) {
        super(sapServiceManager, callback);
        this.entitySetName = entitySetName;
    }

    @Override
    public void execute() {

        try {
            sbp sbp = sapServiceManager.getsbp();

            // Set the maximum number of the downloaded entities
            DataQuery maximumEntityQuery = new DataQuery();
            maximumEntityQuery.setTopCount(maximumEntities);

            sbp.asyncFunction(
                () -> {
                    List<EntityValueUiConnector> itemList = new ArrayList<>();
                    switch (entitySetName) {
                        case IotData:
                            List<com.sap.cloud.android.odata.sbp.IotDataType> iotdata = sbp.getIotData(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.IotDataType i : iotdata) {
                                itemList.add(new IotDataUiConnector(sapServiceManager, i));
                            }
                            break;
                        case IotDevice:
                            List<com.sap.cloud.android.odata.sbp.IotDeviceType> iotdevice = sbp.getIotDevice(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.IotDeviceType i : iotdevice) {
                                itemList.add(new IotDeviceUiConnector(sapServiceManager, i));
                            }
                            break;
                        case Iotdeviceinfo:
                            List<com.sap.cloud.android.odata.sbp.IotdeviceinfoType> iotdeviceinfo = sbp.getIotdeviceinfo(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.IotdeviceinfoType i : iotdeviceinfo) {
                                itemList.add(new IotdeviceinfoUiConnector(sapServiceManager, i));
                            }
                            break;
                        case User:
                            List<com.sap.cloud.android.odata.sbp.UserType> user = sbp.getUser(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.UserType i : user) {
                                itemList.add(new UserUiConnector(sapServiceManager, i));
                            }
                            break;
                        case UserDevices:
                            List<com.sap.cloud.android.odata.sbp.UserDevicesType> userdevices = sbp.getUserDevices(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.UserDevicesType i : userdevices) {
                                itemList.add(new UserDevicesUiConnector(sapServiceManager, i));
                            }
                            break;
                        case Userinfos:
                            List<com.sap.cloud.android.odata.sbp.UserinfosType> userinfos = sbp.getUserinfos(maximumEntityQuery);
                            for (com.sap.cloud.android.odata.sbp.UserinfosType i : userinfos) {
                                itemList.add(new UserinfosUiConnector(sapServiceManager, i));
                            }
                            break;
                    }
                    return itemList;
                },
                (itemList) -> {
                    OperationResult downloadOperationResult;
                    downloadOperationResult = new OperationResult(itemList, OperationResult.Operation.READ);
                    callback.onOperation(downloadOperationResult);
                },
                (error) -> {
                    OperationResult downloadOperationResult;
                    downloadOperationResult = new OperationResult(error, OperationResult.Operation.READ);
                    callback.onOperation(downloadOperationResult);
                }
            );
        } catch (Exception ex) {
            OperationResult downloadOperationResult;
            downloadOperationResult = new OperationResult(ex, OperationResult.Operation.READ);
            callback.onOperation(downloadOperationResult);
        }
    }
}
