package com.nyp.fypj.smartbackpackapp.data.operation;


import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base handler for all OData calls implementing shared behavior.
 */
public abstract class AbstractOnODataOperations implements OnODataOperations {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AbstractOnODataOperations.class);
    /**
     * List of cached entities associated with the entity set that is currently being displayed in
     * the application.
     */
    protected final List<EntityValueUiConnector> currentEntityList;

    /**
     * Callback from consuming code that should be called after common operation handling has been performed.
     */
    protected final OnODataOperations callback;

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    AbstractOnODataOperations(@NonNull List<EntityValueUiConnector> currentEntityList, @NonNull OnODataOperations callback) {
        Objects.requireNonNull(currentEntityList);
        Objects.requireNonNull(callback);

        this.currentEntityList = currentEntityList;
        this.callback = callback;
    }

    @Override
    public void onOperation(@NonNull OperationResult result) {
        Objects.requireNonNull(result);

        if (result.getError() != null) {
            LOGGER.error(result.getError().getMessage());
            callback.onOperation(result);
        } else {
            try {
                handleOperationResult(result);
            } finally {
                callback.onOperation(result);
            }
        }
    }

    /**
     * Template method containing operation-specific code, such as updating the entity set cache.
     * This method is called whenever an operation completes successfully, and is guaranteed to
     * only be called with a non-null result.
     *
     * @param result Result of the current operation
     */
    abstract protected void handleOperationResult(@NonNull OperationResult result);
}
