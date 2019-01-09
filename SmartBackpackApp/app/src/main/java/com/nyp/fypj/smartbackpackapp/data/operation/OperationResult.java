package com.nyp.fypj.smartbackpackapp.data.operation;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;

import java.util.List;

/**
 * We can wrap the AsyncTask result in this class
 * As results, we got the list of the Entities or if something error occurs
 * We can store the exception
 */
public class OperationResult {

    /**
     * Operation type indicating which operation lead to this result.
     */
    public static enum Operation {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        DOWNLOAD_MEDIARESOURCE
    }

    /**
     * List of items returned as a result of the operation.
     */
    private final List<EntityValueUiConnector> result;

    /**
     * Error that occurred during the operation.
     */
    private final Exception error;
    private final Operation operation;
    private final byte[] mediaResource;

    /**
     * Creates an operation result for an operation that resulted in a list of entites.
     *
     * @param result Resulting entities
     * @param operation Operation type that resulted in the entites
     */
    public OperationResult(@NonNull List<EntityValueUiConnector> result, @NonNull Operation operation) {
        this(result, null, operation, null);
    }

    /**
     * Creates an operation result for an operation that resulted in an error.
     *
     * @param error Resulting error
     * @param operation Operation type that resulted in the error
     */
    public OperationResult(@NonNull Exception error, @NonNull Operation operation) {
        this(null, error, operation, null);
    }

    /**
     * Creates an operation result for an operation that resulted in a media resource.
     *
     * @param mediaResource Resulting media resource
     * @param operation Operation type that resulted in the error
     */
    public OperationResult(@NonNull byte[] mediaResource, @NonNull Operation operation) {
        this(null, null, operation, mediaResource);
    }

    /**
     * Internal shared constructor for all result cases.
     *
     * @param result Optional successful result
     * @param error Optional error
     * @param operation Operation type that resulted in the error
     * @param mediaResource Optional media resource
     */
    private OperationResult(List<EntityValueUiConnector> result, Exception error, @NonNull Operation operation, byte[] mediaResource) {
        if(result == null && error == null && mediaResource == null) {
            throw new IllegalArgumentException("Either of result, error or mediaResource must be non-null");
        }
        this.result = result;
        this.error = error;
        this.operation = operation;
        this.mediaResource = mediaResource;
    }

    /**
     * Returns the operation type.
     *
     * @return the operation type
     */
    public Operation getOperation() {
        return this.operation;
    }

    /**
     * Returns the operation result.
     *
     * @return The operation result or null, if an error or a media resource is the result
     */
    public List<EntityValueUiConnector> getResult() {
        return result;
    }

    /**
     * Returns the operation error.
     *
     * @return The operation error or null, if a list of entities or a media resource is the result
     */
    public Exception getError() {
        return error;
    }

    /**
     * Returns the operation media resource.
     *
     * @return The media resource or null, if a list of entities or an error is the result
     */
    public byte[] getMedia() { return mediaResource; }
}
