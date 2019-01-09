package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import java.util.Collections;
import java.util.List;

/**
 * OData entity download handler that sorts results by master property value and updates
 * the local entity cache on success.
 */
public class OnDownloadOperations extends AbstractOnODataOperations {

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    public OnDownloadOperations(@NonNull List<EntityValueUiConnector> currentEntityList, @NonNull OnODataOperations callback) {
        super(currentEntityList, callback);
    }

    @Override
    public void handleOperationResult(@NonNull OperationResult result) {
        List<EntityValueUiConnector> results = result.getResult();
        if (results != null && results.size() > 0) {
            this.currentEntityList.clear();
            Collections.sort(results, (o1, o2) -> {
                String firstPropertyValue = o1.getPropertiesWithValues().get(o1.getMasterPropertyName());
                String secondPropertyValue = o2.getPropertiesWithValues().get(o2.getMasterPropertyName());
                if(null == firstPropertyValue && null == secondPropertyValue) {
                    return 0;
                }
                if(null == firstPropertyValue) {
                    return 1;
                }
                if(null == secondPropertyValue) {
                    return -1;
                }
                return firstPropertyValue.compareTo(secondPropertyValue);
            });
            currentEntityList.addAll(results);
        }
    }
}
