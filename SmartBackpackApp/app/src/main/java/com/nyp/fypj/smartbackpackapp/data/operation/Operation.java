package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;

/**
 * Abstract base type implementing shared behavior of all OData-based operations such as CRUD or
 * media resource downloads.
 */
public abstract class Operation {

    /**
     * Callback to call after this operation completes.
     */
    protected final OnODataOperations callback;

    /**
     * Service manager used to access the underlying OData stores.
     */
    protected final SAPServiceManager sapServiceManager;

    /**
     * Creates a new {@code Operation} with the specified service manager and callback.
     *
     * @param sapServiceManager Service manager providing access to OData service
     * @param callback Callback to call after the operation completes
     */
    public Operation(@NonNull SAPServiceManager sapServiceManager, @NonNull OnODataOperations callback) {
        this.sapServiceManager = sapServiceManager;
        this.callback = callback;
    }

    /**
     * Executes the behavior implemented by this operation.
     */
    public abstract void execute();

}
