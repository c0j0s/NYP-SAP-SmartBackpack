package com.nyp.fypj.smartbackpackapp.data.operation;

import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.StreamLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation implementing OData media resource downloads.
 */
public class DownloadMediaResourceOperation extends Operation {

    private static final Logger LOGGER =  (Logger) LoggerFactory.getLogger(DownloadMediaResourceOperation.class);

    /**
     * UI connector pointing to the item owning the media resources to download.
     */
    private EntityValueUiConnector itemToDownload;

    /**
     * Creates a new operation that downloads media resources associated with an entity using
     * the specified service manager, and subsequently calls {@code callback}.
     *
     * @param sapServiceManager Service manager providing OData store access
     * @param callback Callback to call
     * @param itemToDownload UI connector pointing to the owning entity
     */
    public DownloadMediaResourceOperation(SAPServiceManager sapServiceManager, OnODataOperations callback, EntityValueUiConnector itemToDownload) {
        super(sapServiceManager, callback);
        this.itemToDownload = itemToDownload;
    }

    @Override
    public void execute() {
        try {
            if (itemToDownload.getConnectedObject().getEntityType().isMedia()) {
                // Media entity takes precedence
                sapServiceManager.getsbp().downloadMediaAsync(
                    itemToDownload.getConnectedObject(), this::downloadSuccess, this::downloadError);
            } else {
                // Named resources
                Property namedResourceProp = itemToDownload.getConnectedObject().getEntityType().getStreamProperties().first();
                StreamLink streamLink = namedResourceProp.getStreamLink(itemToDownload.getConnectedObject());
                sapServiceManager.getsbp().downloadStreamAsync(itemToDownload.getConnectedObject(),
                        streamLink, this::downloadSuccess, this::downloadError);
            }
        } catch (Exception e) {
            //TODO handle the error
            LOGGER.error(e.getMessage());
            downloadError(e);
        }
    }

    /**
     * Success handler for resource downloads. Calls the {@code callback} with the corresponding
     * {@code OperationResult}
     *
     * @param mediaResource The downloaded media data
     */
    private void downloadSuccess(byte[] mediaResource) {
        OperationResult mediaResourceOperationResult;
        mediaResourceOperationResult = new OperationResult(mediaResource,
                OperationResult.Operation.DOWNLOAD_MEDIARESOURCE);
        callback.onOperation(mediaResourceOperationResult);
    }

    /**
     * Error handler for resource downloads. Calls the {@code callback} with the corresponding
     * {@code OperationResult}
     *
     * @param error The download error
     */
    private void downloadError(Exception error) {
        OperationResult mediaResourceOperationResult;
        mediaResourceOperationResult = new OperationResult(error,
                OperationResult.Operation.DOWNLOAD_MEDIARESOURCE);
        callback.onOperation(mediaResourceOperationResult);
    }
}