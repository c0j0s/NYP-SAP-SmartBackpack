package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import java.util.List;

/**
 * OData entity deletion handler that updates the local entity cache on success.
 */
public class OnDeleteOperations extends AbstractOnODataOperations {

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    public OnDeleteOperations(List<EntityValueUiConnector> currentEntityList, OnODataOperations callback) {
        super(currentEntityList, callback);
    }

    @Override
    public void handleOperationResult(@NonNull OperationResult result) {
        List<EntityValueUiConnector> results = result.getResult();
        if (results != null && results.size() > 0) {
            currentEntityList.removeAll(results);
        }
    }
}
