package com.nyp.fypj.smartbackpackapp.data.operation;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation implementing OData entity creation.
 */
public class CreateOperation extends Operation {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateOperation.class);

    /**
     * UI connector containing the properties of the item to create.
     */
    private final EntityValueUiConnector itemToCreate;

    /**
     * Creates a new operation that creates a new entity using the specified service manager
     * based on the values in {@code itemToCreate}, and subsequently calls {@code callback}.
     *
     * @param sapServiceManager Service manager providing OData store access
     * @param callback Callback to call
     * @param itemToCreate UI connector providing new values
     */
    public CreateOperation(SAPServiceManager sapServiceManager, OnODataOperations callback, EntityValueUiConnector itemToCreate) {
        super(sapServiceManager, callback);
        this.itemToCreate = itemToCreate;
    }

    @Override
    public void execute() {
        try {
            // Use createEntityAsync instead of createEntity
            sapServiceManager.getsbp().createEntityAsync(
                itemToCreate.getConnectedObject(),
                () -> {
                    // Success handler
                    OperationResult createOperationResult;
                    List<EntityValueUiConnector> itemList = new ArrayList<>();
                    itemList.add(itemToCreate);
                    createOperationResult = new OperationResult(itemList, OperationResult.Operation.CREATE);
                    callback.onOperation(createOperationResult);
                },
                (error) -> {
                    OperationResult createOperationResult;
                    createOperationResult = new OperationResult(error, OperationResult.Operation.CREATE);
                    callback.onOperation(createOperationResult);
                }
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            OperationResult createOperationResult;
            createOperationResult = new OperationResult(e, OperationResult.Operation.CREATE);
            callback.onOperation(createOperationResult);
        }
    }
}