package com.nyp.fypj.smartbackpackapp.mdui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.data.DataContentUtilities;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.data.operation.OnODataOperations;

import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i043971 on 2017. 12. 05..
 */

public class ItemDeleteDialog {

    private final DataContentUtilities dataContentUtilities;

    /**
     * Entities which should be deleted.
     */
    private final List<EntityValueUiConnector> selectedValues = new ArrayList<>();
    private final Context context;

    private FioriProgressBar progressBar;

    /**
     * Callback which should handle the result of the ODATA operations.
     */
    private final OnODataOperations callback;

    public ItemDeleteDialog(List<EntityValueUiConnector> entityValueUiConnectors, Context context, OnODataOperations callback) {
        dataContentUtilities = ((SAPWizardApplication)context.getApplicationContext()).getDataContentUtilities();
        if (entityValueUiConnectors != null) {
            selectedValues.addAll(entityValueUiConnectors);
        }
        this.context = context;
        this.callback = callback;
    }

    public void confirmDelete() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogStyle));
        if( selectedValues.size() > 1 ) {
            builder.setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_more_items);
        } else {
            builder.setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_one_item);
        }

        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            // User clicked OK button
            if( null == progressBar ) {
                progressBar = ((Activity)context).getWindow().getDecorView().findViewById(R.id.indeterminateBar);
            }
            progressBar.setVisibility(View.VISIBLE);
            dataContentUtilities.delete(callback, selectedValues);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            // User cancelled the dialog
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
