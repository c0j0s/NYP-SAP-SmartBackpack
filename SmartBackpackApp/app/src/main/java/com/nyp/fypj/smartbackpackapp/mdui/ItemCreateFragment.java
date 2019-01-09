package com.nyp.fypj.smartbackpackapp.mdui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.app.ErrorHandler;
import com.nyp.fypj.smartbackpackapp.app.ErrorMessage;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.data.DataContentUtilities;
import com.nyp.fypj.smartbackpackapp.data.connector.IotDataUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.IotDeviceUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.IotdeviceinfoUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserDevicesUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.UserinfosUiConnector;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.data.operation.OnODataOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OperationResult;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.mobile.odata.BooleanValue;
import com.sap.cloud.mobile.odata.DataType;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.DecimalValue;
import com.sap.cloud.mobile.odata.DoubleValue;
import com.sap.cloud.mobile.odata.FloatValue;
import com.sap.cloud.mobile.odata.GeographyValue;
import com.sap.cloud.mobile.odata.GlobalDateTime;
import com.sap.cloud.mobile.odata.GuidValue;
import com.sap.cloud.mobile.odata.IntValue;
import com.sap.cloud.mobile.odata.LocalDate;
import com.sap.cloud.mobile.odata.LocalDateTime;
import com.sap.cloud.mobile.odata.LongValue;
import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.ShortValue;
import com.sap.cloud.mobile.odata.StringValue;
import com.sap.cloud.mobile.odata.UnsignedByte;
import com.sap.cloud.mobile.odata.core.GUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import com.sap.cloud.mobile.odata.EnumType;
import com.sap.cloud.mobile.odata.EnumValue;
import com.sap.cloud.mobile.odata.EnumValueList;

public class ItemCreateFragment extends Fragment implements OnODataOperations {

    /**
     * Logger for logging the events
     */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemCreateFragment.class);

    private SAPServiceManager sapServiceManager;

    private DataContentUtilities dataContentUtilities;

    private ErrorHandler errorHandler;


    /**
     * Recycler View
     */
    private RecyclerView recyclerView;

    /**
     * Item adapter for the Recycler View
     */
    private ItemCreateFragmentAdapter itemAdapter;

    /**
     * Layout manager of the Recycler View
     */
    private RecyclerView.LayoutManager layoutManager;

    /**
     * Property name available of the given entity
     */
    private List<String> propertyNames;

    /**
     * Name of the connector-set.
     */
    private EntitySetListActivity.EntitySetName entitySetName;

    /**
     * Flag to differentiate update and create scenarios
     */
    private boolean isUpdate;

    /**
     * EntityValueUiConnector data object behind the UI.
     */
    private EntityValueUiConnector connector;

    /**
     * Entity id of the updated item.
     */
    private int entityId;

    /**
     * Fiori Progressbar to display background process is running.
     */
    private FioriProgressBar progressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemCreateFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sapServiceManager = ((SAPWizardApplication)getActivity().getApplication()).getSAPServiceManager();
        dataContentUtilities = ((SAPWizardApplication)getActivity().getApplication()).getDataContentUtilities();
        errorHandler = ((SAPWizardApplication)getActivity().getApplication()).getErrorHandler();

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            entitySetName = (EntitySetListActivity.EntitySetName) getArguments().getSerializable(ItemDetailActivity.ARG_ITEM_TYPE);
            isUpdate = getArguments().getBoolean(ItemDetailActivity.ARG_UPDATE);
            entityId = getArguments().getInt(ItemDetailActivity.ARG_ITEM_ID);
            if (entityId != -1) {
                connector = dataContentUtilities.getItems().get(entityId);
            }
        }
        if (!isUpdate) {
            connector = createEntityValueUiConnector(entitySetName);
        }
        if( null != connector ) {
            propertyNames = connector.getPropertyNames();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_item_create, container, false);

        // hide the object header
        ObjectHeader objectHeader = this.getActivity().findViewById(R.id.objectHeader);
        if (objectHeader != null) {
            objectHeader.setVisibility(View.GONE);
        }

        // Setting up the RecyclerView and it's adapter
        recyclerView = rootView.findViewById(R.id.add_item_detail_list);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        itemAdapter = new ItemCreateFragmentAdapter(connector, propertyNames, isUpdate);
        recyclerView.setAdapter(itemAdapter);

        // Set the Activity (Toolbar) title
        if (entitySetName != null) {
            Activity activity = this.getActivity();
            if (activity != null) {
                String title;
                if (isUpdate) {
                    title = getResources().getString(R.string.title_update_fragment);
                    activity.setTitle(title + " " + entitySetName.name());
                } else {
                    title = activity.getApplicationContext().getString(R.string.title_create_fragment, entitySetName.name());
                    activity.setTitle(title);
                }
            }
        }
        return rootView;
    }

    private EntityValueUiConnector createEntityValueUiConnector(EntitySetListActivity.EntitySetName entitySetName) {

        EntityValueUiConnector connector = null;
        switch (entitySetName) {
            case IotData:
              connector = new IotDataUiConnector(sapServiceManager);
              break;
            case IotDevice:
              connector = new IotDeviceUiConnector(sapServiceManager);
              break;
            case Iotdeviceinfo:
              connector = new IotdeviceinfoUiConnector(sapServiceManager);
              break;
            case User:
              connector = new UserUiConnector(sapServiceManager);
              break;
            case UserDevices:
              connector = new UserDevicesUiConnector(sapServiceManager);
              break;
            case Userinfos:
              connector = new UserinfosUiConnector(sapServiceManager);
              break;
        }
        return connector;
    }

    /**
     * Simple validataion: checks the presence of mandatory fields.
     * @param property property
     * @param value string value from the UI
     * @return isValid
     */
    public static boolean isValidProperty(Property property, String value) {
        boolean isValid = true;
        if (!property.isNullable()) {
            if (value == null || value.length() == 0) {
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    public void onOperation(OperationResult result) {
        progressBar.setVisibility(View.INVISIBLE);
        if (result.getError() != null) {
            OperationResult.Operation op = result.getOperation();
            Exception ex = result.getError();
            ErrorMessage errorMessage = null;
            switch (op) {
                case UPDATE:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.update_failed), getResources().getString(R.string.update_failed_detail), ex, false);
                    break;
                case DELETE:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.delete_failed), getResources().getString(R.string.delete_failed_detail), ex, false);
                    break;
                case CREATE:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.create_failed), getResources().getString(R.string.create_failed_detail), ex, false);
                    break;
                case READ:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, true);
                    break;
                case DOWNLOAD_MEDIARESOURCE:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, true);
                    break;
            }
            errorHandler.sendErrorMessage(errorMessage);
		} else {
            if (result.getResult() != null) {
                switch (result.getOperation()) {
                    case UPDATE:
                        if (getActivity() instanceof ItemListActivity) {
                            // two-pane
                            View view = getActivity().getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            Bundle arguments = new Bundle();
                            arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, entityId);
                            arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                            ItemDetailFragment fragment = new ItemDetailFragment();
                            fragment.setArguments(arguments);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_item_detail, fragment)
                                    .commit();
                            ((ItemListActivity) getActivity()).refreshItemList();
                        } else {
                            this.getActivity().finish();
                        }
                        break;
                    case CREATE:
                        if (getActivity() instanceof ItemListActivity) {
                            View view = getActivity().getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            // restart the activity
                            Intent intent = getActivity().getIntent();
                            getActivity().finish();
                            startActivity(intent);
                        } else {
                            this.getActivity().finish();
                        }
                        break;
                }
            }
        }
    }

    /**
     * Converts the string property value to the proper DataValue type.
     * @param stringValue read from the UI
     * @param dataType type of the Property
     * @return the proper DataValue fitting to the type information
     */
    public static DataValue convertPropertyForCreate(String stringValue, DataType dataType) {

        DataValue result = null;
        try {
            switch (dataType.getCode()) {
                case DataType.STRING:
                    result = StringValue.of(stringValue);
                    break;
                case DataType.INT:
                case DataType.INTEGER:
                    result = IntValue.of(Integer.valueOf(stringValue));
                    break;
                case DataType.GLOBAL_DATE_TIME:
                    result = GlobalDateTime.parse(stringValue);
                    break;
                case DataType.DECIMAL:
                    result = DecimalValue.of(BigDecimal.valueOf(Double.valueOf(stringValue)));
                    break;
                case DataType.FLOAT:
                    result = FloatValue.of(Float.valueOf(stringValue));
                    break;
                case DataType.GUID_VALUE:
                    result = GuidValue.of(GUID.fromString(stringValue));
                    break;
                case DataType.LONG:
                    result = LongValue.of(Long.valueOf(stringValue));
                    break;
                case DataType.LOCAL_DATE:
                    result = LocalDate.parse(stringValue);
                    break;
                case DataType.LOCAL_DATE_TIME:
                    if ("now".equals(stringValue)) {
                        result = LocalDateTime.now();
                    } else {
                        result = LocalDateTime.parse(stringValue);
                    }
                    break;
                case DataType.UNSIGNED_BYTE:
                    result = UnsignedByte.of(Integer.valueOf(stringValue));
                    break;
                case DataType.BOOLEAN:
                    result = BooleanValue.of(Boolean.valueOf(stringValue));
                    break;
                case DataType.SHORT:
                    result = ShortValue.of(Short.valueOf(stringValue));
                    break;
                case DataType.DOUBLE:
                    result = DoubleValue.of(Double.valueOf(stringValue));
                    break;
                case DataType.GEOGRAPHY_POINT:
                    result = GeographyValue.parseAnyWKT(stringValue);
                    break;
                case DataType.ENUM_VALUE:
                    result = getDataValueForEnum(dataType, stringValue);
                    break;
                default:
                    // Unknown data type handle as string
                    result = StringValue.of(stringValue);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.itemlist_edit_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                if (null == progressBar) {
                    progressBar = getActivity().findViewById(R.id.indeterminateBar);
                }
                progressBar.setVisibility(View.VISIBLE);

                boolean isValid = true;
                if (null == recyclerView) {
                    recyclerView = getView().findViewById(R.id.add_item_detail_list);
                }

                updateAndValidate();

                return true;
            default:
                // User didn't trigger any button listed above, let the superclass handle this action
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAndValidate() {
        boolean isValid = true;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View listItem = recyclerView.getChildAt(i);
            TextView propertyLabel = listItem.findViewById(R.id.property_label);
            EditText propertyInput = listItem.findViewById(R.id.property_value);

            String propertyName = propertyLabel.getText().toString();
            Property property = connector.getConnectedObject().getEntityType().getProperty(propertyName);
            String stringValue = propertyInput.getText().toString();
            DataValue propertyDataValue = convertPropertyForCreate(stringValue, property.getDataType());
            if (!isValidProperty(property, stringValue)) {
                propertyInput.setError(getResources().getString(R.string.mandatory_warning));
                isValid = false;
            }
            if (propertyDataValue != null) {
                connector.getConnectedObject().setDataValue(property, propertyDataValue);
            }
        }
        if (isValid) {
            if (isUpdate) {
                dataContentUtilities.update(ItemCreateFragment.this, connector);
            } else {
                dataContentUtilities.create(ItemCreateFragment.this, connector);
            }
        }
    }

    private static DataValue getDataValueForEnum(DataType dataType, String actualMember) {
        EnumValueList enumValueList = ((EnumType) dataType).getMemberList();
        if (enumValueList != null && enumValueList.length() != 0) {
            for (EnumValue enumValue : enumValueList) {
                if (enumValue != null && enumValue.getName().equalsIgnoreCase(actualMember)) {
                    return enumValue;
                }
            }
        }
        return null;
    }

}
