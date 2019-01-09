package com.nyp.fypj.smartbackpackapp.mdui;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.data.DataContentUtilities;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.data.operation.OnODataOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OperationResult;
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements OnODataOperations {

    /**
    * Logger for logging the events
    */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemDetailFragment.class);

    private DataContentUtilities dataContentUtilities;

    /**
     * The item and item type for the presented entity here.
     */
    private EntityValueUiConnector entityValueUiConnector;
    private EntitySetListActivity.EntitySetName entitySetName;
    private int entityId;
    private ObjectHeader objectHeader;
    private FioriProgressBar progressBar;

    private Context context;
    private ItemAdapter itemAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataContentUtilities = ((SAPWizardApplication)getActivity().getApplication()).getDataContentUtilities();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.itemlist_view_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_item:
                int fragmentId;
                Bundle arguments = new Bundle();
                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                arguments.putBoolean(ItemDetailActivity.ARG_UPDATE, true);
                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, entityId);
                ItemCreateFragment fragment = new ItemCreateFragment();
                fragment.setArguments(arguments);
                if( getActivity().findViewById(R.id.item_detail_container) != null ) {
                    fragmentId = R.id.item_detail_container;
                } else {
                    fragmentId = R.id.fragment_item_detail;
                }
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(fragmentId, fragment)
                        .commit();
                return true;
            case R.id.delete_item:
                List<EntityValueUiConnector> selectedValues = new ArrayList<>();
                selectedValues.add(entityValueUiConnector);
                ItemDeleteDialog dDialog = new ItemDeleteDialog(selectedValues, context, ItemDetailFragment.this);
                dDialog.confirmDelete();
                return true;
            default:
                // User didn't trigger any button listed above, let the superclass handle this action
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ItemDetailActivity.ARG_ITEM_ID) && bundle.containsKey(ItemDetailActivity.ARG_ITEM_TYPE)) {
                // Load the data content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.

                entityId = bundle.getInt(ItemDetailActivity.ARG_ITEM_ID);
                entitySetName = (EntitySetListActivity.EntitySetName) bundle.getSerializable(ItemDetailActivity.ARG_ITEM_TYPE);

                if (0 <= entityId && entityId < dataContentUtilities.getItems().size()) {
                    entityValueUiConnector = dataContentUtilities.getItems().get(entityId);

                    Activity activity = this.getActivity();
                    if (activity != null) {
                         // Set toolbar title
                         String masterPropertyName = entityValueUiConnector.getMasterPropertyName();
                         activity.setTitle(entityValueUiConnector.getConnectedObject().getEntityType().getLocalName());

                         // initialize the object-header
                         objectHeader = this.getActivity().findViewById(R.id.objectHeader);
                         if (objectHeader != null) {
                             objectHeader.setHeadline(entityValueUiConnector.getPropertiesWithValues().get(masterPropertyName));
                             List<String> keys = entityValueUiConnector.getKeyPropertyNames();
                             StringBuilder subHeadLineBuilder = new StringBuilder();
                             for (String key: keys) {
                                 String keyValue = entityValueUiConnector.getPropertiesWithValues().get(key);
                                 subHeadLineBuilder.append(key).append(": ").append(keyValue).append("\n");
                             }
                             objectHeader.setSubheadline(subHeadLineBuilder.toString());
                             objectHeader.setTag("#tag1", 0);
                             objectHeader.setTag("#tag3", 2);
                             objectHeader.setTag("#tag2", 1);

                             objectHeader.setBody("You can set the header body text here.");
                             objectHeader.setFootnote("You can set the header footnote here.");
                             objectHeader.setDescription("You can add a detailed item description here.");

                             if (entityValueUiConnector.hasMediaResource()) {
                                  // Instead of using Glide, show how downloadMediaAsync and downloadStreamAsync
                                  // can be used to retrieved media resource from either media entity or named
                                  // resources
                                  objectHeader.prepareDetailImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                  dataContentUtilities.downloadMediaResource(this, entityValueUiConnector);
                             }
                        }
                    }
                }
            }
        }

        // Show the data content as text in a TextView.
        if (entityValueUiConnector != null) {
            // collection of all of the properties
            List<String> allProperties = entityValueUiConnector.getPropertyNames();
            itemAdapter = new ItemAdapter(this.getActivity(), R.layout.fragment_item_list, allProperties);
            final ListView listView = rootView.findViewById(R.id.item_detail_list);
            listView.setAdapter(itemAdapter);
        }
        return rootView;
    }

    @Override
    public void onOperation(OperationResult result) {
        if( null == progressBar ) {
            progressBar = ((Activity)context).getWindow().getDecorView().findViewById(R.id.indeterminateBar);
        }
        progressBar.setVisibility(View.INVISIBLE);
        OperationResult.Operation op = result.getOperation();
        switch (op) {
            case DOWNLOAD_MEDIARESOURCE:
                if( isAdded() ) {
                    byte[] data = result.getMedia();
                    if (data != null && data.length > 0) {
                        objectHeader.setDetailImage(new BitmapDrawable(
                            this.getResources().getSystem(),
                            BitmapFactory.decodeByteArray(data, 0, data.length))
                        );
                    } else {
                        // Nothing will be shown on UI
                    }
                }
                break;
            case UPDATE:
                List<EntityValueUiConnector> itemList = result.getResult();
                if (itemList != null && itemList.size() == 1) {
                    entityValueUiConnector = itemList.get(0);
                    itemAdapter.notifyDataSetChanged();
                } else {
                }
                break;
            case DELETE:
				if (this.getActivity() instanceof ItemListActivity) {
					// in two-pane mode just refresh the list
					((ItemListActivity) this.getActivity()).updateItemList();
                } else {
					this.getActivity().finish();
                }
                break;
			default:
				// invalid operation
                LOGGER.error("Current operation is " + op + ". Only DOWNLOAD_MEDIARESOURCE, UPDATE and DELETE are valid.");
        }
    }

    public class ItemAdapter extends ArrayAdapter<String> {

        public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> properties) {
            super(context, resource, properties);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return entityValueUiConnector.getPropertyListItem(position, convertView, parent, getContext());
        }
    }
}
