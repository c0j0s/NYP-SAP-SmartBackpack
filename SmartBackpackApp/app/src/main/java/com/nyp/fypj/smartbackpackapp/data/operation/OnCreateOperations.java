package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OData entity creation handler that updates the local entity cache on success.
 */
public class OnCreateOperations extends AbstractOnODataOperations {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OnCreateOperations.class);

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    public OnCreateOperations(List<EntityValueUiConnector> currentEntityList, OnODataOperations callback) {
        super(currentEntityList, callback);
    }

    @Override
    protected void handleOperationResult(@NonNull OperationResult result) {
        List<EntityValueUiConnector> results = result.getResult();
        if (results != null && results.size() == 1) {
            currentEntityList.add(results.get(0));
        } else {
            // this couldn't happen if the operation was successful
            LOGGER.error("Inconsistency in the results.");
        }
    }
}
