package com.nyp.fypj.smartbackpackapp.data;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.mdui.EntitySetListActivity;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.data.operation.CreateOperation;
import com.nyp.fypj.smartbackpackapp.data.operation.DeleteOperation;
import com.nyp.fypj.smartbackpackapp.data.operation.DownloadMediaResourceOperation;
import com.nyp.fypj.smartbackpackapp.data.operation.DownloadOperation;
import com.nyp.fypj.smartbackpackapp.data.operation.OnCreateOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OnDeleteOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OnDownloadMediaResourceOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OnDownloadOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OnUpdateOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OnODataOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OperationResult;
import com.nyp.fypj.smartbackpackapp.data.operation.UpdateOperation;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Singleton class to handle ODATA operations on the entity sets. If the entity set were
 * changed the instance should be re-initiated.
 */
public class DataContentUtilities {

    /**
     * Name of the currently displayed entity set.
     */
    private EntitySetListActivity.EntitySetName currentEntitySetName;

    /**
     * Storage for all the entity set data, where the key is the name of the entity
     * set.
     */
    private Map<EntitySetListActivity.EntitySetName, List<EntityValueUiConnector>> entitySetCache = new HashMap<>();

    /**
     * An array for storing the data items of the actual entity set.
     */
    private List<EntityValueUiConnector> currentEntityList = null;

    /**
     * Service manager used to access the underlying OData stores.
     */
    private final SAPServiceManager sapServiceManager;

    /**
     * Creates a new data content utility using the specified service manager to access the underlying
     * OData stores.
     *
     * @param sapServiceManager The service manager
     */
    public DataContentUtilities(@NonNull SAPServiceManager sapServiceManager) {
        Objects.requireNonNull(sapServiceManager);

        this.sapServiceManager = sapServiceManager;
    }

    /**
     * Configures the data content utilities for the specified entity set. If data is available
     * from previous invocations, it will be reloaded, otherwise an empty entity list is created.
     *
     * @param entitySetName Name of the entity set to load
     */
    public void setCachedEntitySet(EntitySetListActivity.EntitySetName entitySetName) {
        this.currentEntitySetName = entitySetName;
        this.currentEntityList = entitySetCache.get(entitySetName);
        if (currentEntityList == null) {
            currentEntityList = new ArrayList<>();
            entitySetCache.put(entitySetName, currentEntityList);
        }
    }

    /**
     * Returns the items stored in the current entity list.
     *
     * @return The items available for the current entity set.
     */
    public List<EntityValueUiConnector> getItems() {
        return currentEntityList;
    }

    /**
     * Triggers a download operation. Notification about the outcome will be
     * sent on the {@link OnODataOperations} callback.
     *
     * @param callback Callback to call
     */
    public void download(@NonNull OnODataOperations callback) {
        Objects.requireNonNull(callback);
        new DownloadOperation(sapServiceManager, new OnDownloadOperations(currentEntityList, callback), currentEntitySetName)
                .execute();
    }

    /**
     * Triggers a create operation. Notification about the outcome will be
     * sent on the {@link OnODataOperations} callback.
     *
     * @param callback Callback to call
     * @param itemToCreate UI connector containing the new value
     */
    public void create(@NonNull OnODataOperations callback, @NonNull EntityValueUiConnector itemToCreate) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(itemToCreate);
        new CreateOperation(sapServiceManager, new OnCreateOperations(currentEntityList, callback), itemToCreate)
                .execute();
    }

    /**
     * Triggers an update operation. Notification about the outcome will be
     * sent on the {@link OnODataOperations} callback.
     *
     * @param callback Callback to call
     * @param itemForUpdate UI connector containing the new value
     */
    public void update(@NonNull OnODataOperations callback, @NonNull EntityValueUiConnector itemForUpdate) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(itemForUpdate);
        new UpdateOperation(sapServiceManager, new OnUpdateOperations(itemForUpdate, currentEntityList, callback), itemForUpdate)
                .execute();
    }

    /**
     * Triggers a delete operation. Notification about the outcome will be
     * sent on the {@link OnODataOperations} callback.
     *
     * @param callback Callback to call
     * @param entityValueUiConnectors List of items to delete
     */
    public void delete(@NonNull OnODataOperations callback, @NonNull List<EntityValueUiConnector> entityValueUiConnectors) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(entityValueUiConnectors);
        new DeleteOperation(sapServiceManager, new OnDeleteOperations(currentEntityList, callback), entityValueUiConnectors)
                .execute();
    }

    /**
     * Triggers a media resource download operation. Notification about the outcome will be
     * sent on the {@link OnODataOperations} callback.
     *
     * @param callback Callback to call
     * @param itemToDownload Value owning the media resource(s) that should be downloaded
     */
    public void downloadMediaResource(OnODataOperations callback, EntityValueUiConnector itemToDownload) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(itemToDownload);
        new DownloadMediaResourceOperation(sapServiceManager, new OnDownloadMediaResourceOperations(currentEntityList, callback), itemToDownload)
                .execute();
    }
}
