package com.nyp.fypj.smartbackpackapp.data.operation;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Operation implementing OData entity updates.
 */
public class UpdateOperation extends Operation {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UpdateOperation.class);

    /**
     * UI connector containing the properties of the item to create.
     */
    private EntityValueUiConnector itemForUpdate;

    /**
     * Creates a new operation that updates an entity using the specified service manager
     * based on the values in {@code itemToCreate}, and subsequently calls {@code callback}.
     *
     * @param sapServiceManager Service manager providing OData store access
     * @param callback Callback to call
     * @param itemForUpdate UI connector providing new values
     */
    public UpdateOperation(SAPServiceManager sapServiceManager, OnODataOperations callback, EntityValueUiConnector itemForUpdate) {
        super(sapServiceManager, callback);
        this.itemForUpdate = itemForUpdate;
    }

    @Override
    public void execute() {
        try {
            // Use updateEntityAsync instead of updateEntity
            sapServiceManager.getsbp().updateEntityAsync(
                itemForUpdate.getConnectedObject(),
                () -> {
                    // Success handler
                    OperationResult updateOperationResult;
                    List<EntityValueUiConnector> itemList = new ArrayList<>();
                    itemList.add(itemForUpdate);
                    updateOperationResult = new OperationResult(itemList, OperationResult.Operation.UPDATE);
                    callback.onOperation(updateOperationResult);
                },
                (error) -> {
                    OperationResult updateOperationResult;
                    updateOperationResult = new OperationResult(error, OperationResult.Operation.UPDATE);
                    callback.onOperation(updateOperationResult);
                }
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            OperationResult updateOperationResult;
            updateOperationResult = new OperationResult(e, OperationResult.Operation.UPDATE);
            callback.onOperation(updateOperationResult);
        }
    }
}
