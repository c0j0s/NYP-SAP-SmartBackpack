package com.nyp.fypj.smartbackpackapp.mdui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.sap.cloud.mobile.odata.DataType;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.Property;

import java.util.ArrayList;
import java.util.List;

import static com.nyp.fypj.smartbackpackapp.mdui.ItemCreateFragment.convertPropertyForCreate;

public class ItemCreateFragmentAdapter extends
        RecyclerView.Adapter<ItemCreateFragmentAdapter.ViewHolder> {

    private final List<String> propertyNames;
    private final EntityValueUiConnector connector;
    private final boolean isUpdate;

    public ItemCreateFragmentAdapter(EntityValueUiConnector connector, List<String> properties,
                                     boolean isUpdate) {
        this.connector = connector;
        this.propertyNames = properties;
        this.isUpdate = isUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.create_list_element, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String propertyName = propertyNames.get(position);
        viewHolder.getTextView().setText(propertyName);
        viewHolder.getEditText().setHint(propertyName);

        Property property = connector.getConnectedObject().getEntityType().getProperty(propertyName);

        InputFilter filter[] = setInputFilter(property);

        // If no filters were created, do not set the filters
        if (0 != filter.length) {
            viewHolder.getEditText().setFilters(filter);
        }

        viewHolder.getEditText().setRawInputType(getInputType(property));

        // In update screen, key properties aren't allowed to be updated
        if (isUpdate && connector.getKeyPropertyNames().contains(propertyName)) {
            viewHolder.getEditText().setEnabled(false);
        } else {
            viewHolder.getEditText().setEnabled(true);
        }

        // In update screen and after a configuration change (e.g. orientation change)
        // get the values from the model and set the EditTexts
        DataValue propertyDataValue = connector.getConnectedObject().getDataValue(property);
        if (propertyDataValue != null) {
            viewHolder.getEditText().setText(propertyDataValue.toString());
		} else {
           viewHolder.getEditText().setText("");
        }

        // update the data model, when a view lost focus
        viewHolder.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String propertyStringValue = viewHolder.getEditText().getText().toString();
                    DataValue propertyDataValue = convertPropertyForCreate(propertyStringValue, property.getDataType());
                    if (propertyDataValue != null) {
                        connector.getConnectedObject().setDataValue(property, propertyDataValue);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.propertyNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final EditText editText;

        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.property_label);
            editText = v.findViewById(R.id.property_value);
        }

        public EditText getEditText() {
            return editText;
        }

        public TextView getTextView() {
            return textView;
        }
    }

    /**
     * Restrict the allowed characters to be entered based on the @property
     */
    private InputFilter[] setInputFilter(Property property) {
        List<InputFilter> filter = new ArrayList<InputFilter>();
        switch (property.getTypeCode()) {
            case DataType.INT:
            case DataType.INTEGER:
            case DataType.LONG:
            case DataType.UNSIGNED_INT:
                // Create an InputFilter to allow digits only
                filter.add((source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                });
                break;
            case DataType.DECIMAL:
                // Create an InputFilter to allow digits and decimal point only
                filter.add((source, start, end, dest, dstart, dend) -> {
                    // If user entered a . (decimal point) but string already contains it
                    // prevent adding . (decimal ponint) again
                    if (source.length() > 0 && '.' == source.charAt(0) && dest.toString().contains(".")) {
                        return "";
                    }
                    for (int i = start; i < end; i++) {
                        if (!(Character.isDigit(source.charAt(i)) || '.' == source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                });
                break;
            default:
                break;
        }

        // Attr MaxLength is not mandatory
        if (0 != property.getMaxLength()) {
            filter.add(new InputFilter.LengthFilter(property.getMaxLength()));
        }

        return filter.toArray(new InputFilter[filter.size()]);
    }

    /**
     * Get what kind of InputType (keyboard) has to be set for the given @property
     */
    private Integer getInputType(Property property) {
        Integer type;
        switch (property.getDataType().getCode()) {
            case DataType.INT:
            case DataType.LONG:
            case DataType.INTEGER:
            case DataType.UNSIGNED_INT:
                type = InputType.TYPE_CLASS_NUMBER;
                break;
            case DataType.DECIMAL:
                type = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
                break;
            case DataType.STRING:
                type = InputType.TYPE_CLASS_TEXT;
                break;
            case DataType.LOCAL_DATE:
                type = InputType.TYPE_CLASS_DATETIME;
                break;
            default:
                type = InputType.TYPE_CLASS_TEXT;
        }
        return type;
    }
}
