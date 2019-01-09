package com.nyp.fypj.smartbackpackapp.data.connector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.mobile.odata.EntityType;
import com.sap.cloud.mobile.odata.EntityValue;

import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.StreamLink;
import java.util.List;
import java.util.Map;

/**
 * This class, more preciselly, its children encapsulate the entity set data for the UI. Its
 * getters are used to fill the screens with entity set specific data. It provides a dedicated
 * method to access the wrapped data object, as well.
 */
public abstract class EntityValueUiConnector {

    private final SAPServiceManager sapServiceManager;

    public EntityValueUiConnector(SAPServiceManager sapServiceManager) {
        this.sapServiceManager = sapServiceManager;
    }

    /**
     * Returns the names of the key properties.
     * @return
     */
    public abstract List<String> getKeyPropertyNames();

    /**
     * Returns the name of the master property (first non-key property).
     * @return
     */
    public abstract String getMasterPropertyName();

    /**
     * Returns the names of all the properties.
     * @return
     */
    public abstract List<String> getPropertyNames();

    /**
     * Returns the all the properties with their values.
     * @return
     */
    public abstract Map<String, String> getPropertiesWithValues();

    /**
     * Returns the connected ODATA object.
     * @return
     */
    public abstract EntityValue getConnectedObject();

    /**
    * Returns a view element which represents the corresponding property on the detail view. If an
    * entity set needs a special look-and-feel for one of its properties, then this implementation should
    * be overridden in the entity set specific child class.
    */
    public View getPropertyListItem(int position, View convertView, ViewGroup parent, Context context) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.detail_list_element, parent, false);
        }

        TextView propertyLabel = convertView.findViewById(R.id.textView1);
        TextView propertyValueView = convertView.findViewById(R.id.textView2);

        List<String> propertyNames = getPropertyNames();
        Map<String, String> valueMap = getPropertiesWithValues();
        String propertyName = propertyNames.get(position);
        String propertyValue = valueMap.get(propertyName);

        propertyLabel.setText(propertyName);

        if (propertyValue != null) {
            propertyValueView.setText(propertyValue);
        } else {
            propertyValueView.setText("");
        }
        return convertView;
    }

    public boolean hasMediaResource() {
        EntityType entityType = getConnectedObject().getEntityType();
        return entityType.isMedia() || (entityType.getStreamProperties().length() > 0);
    }

    public String getMediaResourceUrl() {
        EntityValue entityValue = getConnectedObject();
        // Check for media entity
        if (entityValue.getEntityType().isMedia()) {
            String mediaLink = entityValue.getMediaStream().getReadLink();
            if (mediaLink != null) {
                return sapServiceManager.getServiceRoot() + mediaLink;
            }
            // falls through and return null
        } else {
            // Check for named resources i.e. properties with Edm.Stream type
            if (entityValue.getEntityType().getStreamProperties().length() > 0) {
                // We have one or more named resources or properties of Edm.Stream
                // Since there is no suitable heuristics, always take the first property
                Property namedResourceProp = entityValue.getEntityType().getStreamProperties().first();
                StreamLink streamLink = namedResourceProp.getStreamLink(entityValue);
                String mediaLink = streamLink.getReadLink();
                if (mediaLink != null) {
                    // Entities are fetched with odata.metadata parameter set to full for V4
                    // so readLink should have proper values
                    return sapServiceManager.getServiceRoot() + mediaLink;
                }
            }
        }
        // Has no media resources
        return null;
    }
}
