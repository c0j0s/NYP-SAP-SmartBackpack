package com.nyp.fypj.smartbackpackapp.data.operation;

/**
 * Generic callback interface for OData operations such as CRUD or media download.
 */
public interface OnODataOperations {
    /**
     * OData operation callback that is invoked once the corresponding operation completes.
     *
     * @param result The result of the current OData call
     */
    void onOperation(OperationResult result);

}
