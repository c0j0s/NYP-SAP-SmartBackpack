package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OData entity update handler that updates the local entity cache on success.
 */
public class OnUpdateOperations extends AbstractOnODataOperations {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OnUpdateOperations.class);

    /**
     * UI connector pointing to the entity that is currently being modified.
     */
    private final EntityValueUiConnector itemForUpdate;

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param itemForUpdate UI connector pointing to the entity that is currently being modified
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    public OnUpdateOperations(@NonNull EntityValueUiConnector itemForUpdate,
                              @NonNull List<EntityValueUiConnector> currentEntityList,
                              @NonNull OnODataOperations callback) {
        super(currentEntityList, callback);
        this.itemForUpdate = itemForUpdate;
    }

    @Override
    public void handleOperationResult(@NonNull OperationResult result) {
        List<EntityValueUiConnector> results = result.getResult();
        if (results != null && results.size() == 1) {
            EntityValueUiConnector updatedValue = results.get(0);
            currentEntityList.set(currentEntityList.indexOf(itemForUpdate), updatedValue);
        } else {
            // this couldn't happen if the operation was successful
            LOGGER.error("Inconsistency in the results.");
        }
    }
}
