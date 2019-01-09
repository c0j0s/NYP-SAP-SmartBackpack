package com.nyp.fypj.smartbackpackapp.data.operation;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;

import com.sap.cloud.mobile.odata.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Operation implementing OData entity deletion.
 */
public class DeleteOperation extends Operation {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteOperation.class);

    /**
     * List of UI connectors containing the entities to delete.
     */
    private List<EntityValueUiConnector> selectedValues;

    /**
    * Creates a new operation that deletes entities using the specified service manager
    * and subsequently calls {@code callback}.
    *
    * @param sapServiceManager Service manager providing OData store access
    * @param callback Callback to call
    * @param selectedValues UI connectors pointing to items to delete
    */
    public DeleteOperation(SAPServiceManager sapServiceManager, OnODataOperations callback, List<EntityValueUiConnector> selectedValues) {
       super(sapServiceManager, callback);
       this.selectedValues = selectedValues;
    }

    @Override
    public void execute() {
        try {
            ChangeSet deletesChangeSet = new ChangeSet();
            for (EntityValueUiConnector entityValueUiConnector: selectedValues) {
                deletesChangeSet.deleteEntity(entityValueUiConnector.getConnectedObject());
            }
            sapServiceManager.getsbp().applyChangesAsync(
                deletesChangeSet,
                () -> {
                    OperationResult deleteOperationResult;
                    List<EntityValueUiConnector> itemList = new ArrayList<>();
                    itemList.addAll(selectedValues);
                    deleteOperationResult = new OperationResult(itemList, OperationResult.Operation.DELETE);
                    callback.onOperation(deleteOperationResult);
                },
                (error) -> {
                    OperationResult deleteOperationResult;
                    deleteOperationResult = new OperationResult(error, OperationResult.Operation.DELETE);
                    callback.onOperation(deleteOperationResult);
                }
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            OperationResult deleteOperationResult;
            deleteOperationResult = new OperationResult(e, OperationResult.Operation.DELETE);
            callback.onOperation(deleteOperationResult);
        }
    }
}







