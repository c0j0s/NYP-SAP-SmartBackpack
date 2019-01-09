package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OData entity media resource download handler that logs media resource download failures.
 */
public class OnDownloadMediaResourceOperations extends AbstractOnODataOperations {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OnDownloadMediaResourceOperations.class);

    /**
     * Creates a new operation callback for the specified entity list, and calls the specified callback once
     * it completes.
     *
     * @param currentEntityList List of the currently displayed entites
     * @param callback Callback to call after this handler is executed
     */
    public OnDownloadMediaResourceOperations(@NonNull List<EntityValueUiConnector> currentEntityList, @NonNull OnODataOperations callback) {
        super(currentEntityList, callback);
    }

    @Override
    public void handleOperationResult(@NonNull OperationResult result) {
        if (result.getMedia() == null) {
            LOGGER.error("Unable to download media resource.");
        }
    }
}
