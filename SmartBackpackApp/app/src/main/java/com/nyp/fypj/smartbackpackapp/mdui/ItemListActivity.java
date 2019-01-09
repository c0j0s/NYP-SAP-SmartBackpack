package com.nyp.fypj.smartbackpackapp.mdui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.app.ErrorHandler;
import com.nyp.fypj.smartbackpackapp.app.ErrorMessage;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.data.DataContentUtilities;
import com.nyp.fypj.smartbackpackapp.data.connector.EntityValueUiConnector;
import com.nyp.fypj.smartbackpackapp.data.operation.OnODataOperations;
import com.nyp.fypj.smartbackpackapp.data.operation.OperationResult;
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.Property;
import java.util.ArrayList;
import java.util.List;

import static com.nyp.fypj.smartbackpackapp.mdui.ItemDetailActivity.ARG_ITEM_ID;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements OnODataOperations {

    public static final String ARG_ITEM_SET = "entityset";
    private boolean isError;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemListActivity.class);

    private DataContentUtilities dataContentUtilities;

    private ErrorHandler errorHandler;

    private SimpleItemRecyclerViewAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;

    /**
     * Entity set which is shown on the list.
     */
    private EntitySetListActivity.EntitySetName entitySetName;

    private FioriProgressBar progressBar;
    /**
     * SAP Fiori Standard Theme Primary Color: 'Global Dark Base'
     */
    private static final int FIORI_STANDARD_THEME_GLOBAL_DARK_BASE = Color.rgb(63, 81, 96);

    /**
     * SAP Fiori Standard Theme Primary Color: 'Background'
     */
    private static final int FIORI_STANDARD_THEME_BACKGROUND = Color.rgb(250, 250, 250);

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter.actionMode != null) {
            adapter.actionMode.finish();
        }
        adapter.notifyDataSetChanged();

        if (!isError) {
            dataContentUtilities.setCachedEntitySet(entitySetName);
            refreshLayout.setColorSchemeColors(FIORI_STANDARD_THEME_GLOBAL_DARK_BASE);
            refreshLayout.setProgressBackgroundColorSchemeColor(FIORI_STANDARD_THEME_BACKGROUND);
            refreshLayout.setRefreshing(true);
            dataContentUtilities.download(this);
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itemlist_menu, menu);
        return true;
    }

    /*
    * Listen for option item selections so that we receive a notification
    * when the user requests a refresh by selecting the refresh action bar item.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Check if user triggered a refresh:
            case R.id.menu_refresh:
                LOGGER.info(entitySetName + " list starting to refresh.");
                // Signal SwipeRefreshLayout to start the progress indicator
                refreshLayout.setRefreshing(true);

                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                updateItemList();

                return true;
            default:
                // User didn't trigger any button listed above, let the superclass handle this action
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateItemList() {
		dataContentUtilities.download(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataContentUtilities = ((SAPWizardApplication)getApplication()).getDataContentUtilities();
        errorHandler = ((SAPWizardApplication)getApplication()).getErrorHandler();

        setContentView(R.layout.activity_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent startIntent = getIntent();
        entitySetName = (EntitySetListActivity.EntitySetName) startIntent.getSerializableExtra(ARG_ITEM_SET);
        if (entitySetName != null) {
            dataContentUtilities.setCachedEntitySet(entitySetName);
            String title = getResources().getString(entitySetName.getTitleId());
            setTitle(title);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (twoPane) {
                Bundle arguments = new Bundle();
                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                ItemCreateFragment fragment = new ItemCreateFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_item_detail, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                context.startActivity(intent);
            }
        });

        View recyclerView = findViewById(R.id.item_list);
        if (recyclerView == null) throw new AssertionError();
        setupRecyclerView((RecyclerView) recyclerView, this);

        if (findViewById(R.id.fragment_item_detail) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        refreshLayout = findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(
            this::updateItemList
        );
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, Context context) {
        this.adapter = new SimpleItemRecyclerViewAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onOperation(OperationResult result) {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
        if( null == progressBar ) {
            progressBar = getWindow().getDecorView().findViewById(R.id.indeterminateBar);
        }
        progressBar.setVisibility(View.INVISIBLE);

        if (result.getError() != null) {
            // handle error
            this.isError = true;
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
                    errorMessage = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, false);
                    break;
                case DOWNLOAD_MEDIARESOURCE:
                    errorMessage = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, false);
                    break;
            }
            errorHandler.sendErrorMessage(errorMessage);
        } else {
            // ok
			this.isError = false;

            if (twoPane && adapter.getItemCount() > 0) {
                // show the first element in tablet mode
                adapter.activeItemPosition = 0;
				// show the details of the first item
                Bundle arguments = new Bundle();
                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, 0);
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_item_detail, fragment)
                        .commit();
            }
            adapter.notifyDataSetChanged();
        }
        if (adapter.actionMode != null) {
            adapter.actionMode.finish();
        }
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private Context context;
        private ActionMode actionMode;

        /**
         * Entity values are stored in a list, the selected ones in a separate one.
         */
        private List<EntityValueUiConnector> values = dataContentUtilities.getItems();
        private List<EntityValueUiConnector> selectedValues = new ArrayList<>();

        /**
         * In two-pane mode the active item has to be maintained.
         */
        private int activeItemPosition = -1;
        private View activeItemView;
        private EntityValueUiConnector activeEntity;

        private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startSupportActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.itemlist_view_options, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.save_item:
                        int entityId = values.indexOf(selectedValues.get(0));
                        RecyclerView recyclerView = findViewById(R.id.add_item_detail_list);
                        EntityValueUiConnector connector = dataContentUtilities.getItems().get(entityId);

                        if (null == progressBar) {
                            progressBar = findViewById(R.id.indeterminateBar);
                        }
                        progressBar.setVisibility(View.VISIBLE);

                        updateAndValidate(recyclerView, connector);

                        return true;
                    case R.id.update_item:
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        if (selectedValues.size() == 1) {
                            int position = values.indexOf(selectedValues.get(0));
                            if (twoPane) {
                                Bundle arguments = new Bundle();
                                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, position);
                                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                                arguments.putBoolean(ItemDetailActivity.ARG_UPDATE, true);
                                ItemCreateFragment fragment = new ItemCreateFragment();
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_item_detail, fragment)
                                        .commit();
                                mode.getMenu().setGroupVisible(R.id.group_create, true);
                                mode.getMenu().setGroupVisible(R.id.group_display, false);
                            } else {
                                intent.putExtra(ARG_ITEM_ID, position);
                                intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                                intent.putExtra(ItemDetailActivity.ARG_UPDATE, true);
                                context.startActivity(intent);
                            }
                        }
                        return true;
                    case R.id.delete_item:
                        ItemDeleteDialog deleteDialog = new ItemDeleteDialog(selectedValues, context, ItemListActivity.this);
                        deleteDialog.confirmDelete();
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                selectedValues.clear();
                notifyDataSetChanged();
            }
        };

        public SimpleItemRecyclerViewAdapter(Context context) {
            this.context = context;
			
            if (values.size() > 0) {
				activeItemPosition = 0;
                activeEntity = values.get(0);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_list_element, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
		
            if (position == activeItemPosition) {
                activeItemView = holder.view;
            }

            final EntityValueUiConnector entityValueUiConnector = values.get(holder.getAdapterPosition());
            String masterPropertyName = entityValueUiConnector.getMasterPropertyName();
            String masterPropertyValue = entityValueUiConnector.getPropertiesWithValues().get(masterPropertyName);

            holder.contentView.setHeadline(masterPropertyValue);
            holder.contentView.setDetailImage(null);
            if (masterPropertyValue != null && masterPropertyValue.length() != 0) {
                holder.contentView.setDetailImageCharacter(masterPropertyValue.substring(0, 1));
            } else {
                holder.contentView.setDetailImageCharacter("?");
            }

            if (entityValueUiConnector.hasMediaResource()) {
                // Glide offers caching in addition to fetching the images
                holder.contentView.prepareDetailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context)
                    .load(entityValueUiConnector.getMediaResourceUrl())
                    .apply( new RequestOptions().fitCenter() )
                    .transition( DrawableTransitionOptions.withCrossFade() )
                    .into(holder.contentView.prepareDetailImageView());
            }

            holder.contentView.setSubheadline("Subheadline goes here");
            holder.contentView.setFootnote("Footnote goes here");
            holder.contentView.setIcon(Integer.toString(position + 1), 0);
            holder.contentView.setIcon(R.drawable.default_dot, 1, R.string.attachment_item_content_desc);
            holder.contentView.setIcon("!", 2);

            holder.position = holder.getAdapterPosition();

            if (selectedValues.contains(entityValueUiConnector)) {
                if (holder.position == activeItemPosition) {
                    holder.view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_active_selected));
                } else {
                    holder.view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_selected));
                }
                holder.checkBox.setChecked(true);
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                if (holder.position == activeItemPosition) {
                    holder.view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_active));
                } else {
                    holder.view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_default));
                }
                holder.checkBox.setChecked(false);
                holder.checkBox.setVisibility(View.INVISIBLE);
            }

            holder.view.setOnClickListener(view -> {
                if (twoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ARG_ITEM_ID, holder.getAdapterPosition());
                    arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_item_detail, fragment)
                            .commit();
                    if (activeItemView != null) {
                        setItemBackground(activeItemView, activeEntity, false);
                    }
                    setItemBackground(holder.view, entityValueUiConnector, true);
                    activeItemView = holder.view;
                    activeEntity = entityValueUiConnector;
                    activeItemPosition = holder.position;
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ARG_ITEM_ID, holder.getAdapterPosition());
                    intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                    context.startActivity(intent);
                }
            });

            holder.view.setOnLongClickListener(view -> {
                if (actionMode == null) {
                    actionMode = ItemListActivity.this.startSupportActionMode(actionModeCallback);
                }

                CheckBox box = holder.checkBox;
                if (box.isChecked()) {
                    box.setChecked(false);
                    box.setVisibility(View.INVISIBLE);
                } else {
                    box.setChecked(true);
                    box.setVisibility(View.VISIBLE);
                }
                return true;
            });

            holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    selectedValues.add(entityValueUiConnector);
                    if (selectedValues.size() > 1) {
                        if (actionMode != null) {
                            actionMode.getMenu().setGroupVisible(R.id.group_create, false);
                            actionMode.getMenu().setGroupVisible(R.id.group_display, true);
                            actionMode.getMenu().findItem(R.id.update_item).setVisible(false);
                        }
                    }
                } else {
                    holder.checkBox.setVisibility(View.INVISIBLE);
                    selectedValues.remove(entityValueUiConnector);
                    if (actionMode != null) {
                        switch (selectedValues.size()) {
                            case 1:
                                actionMode.getMenu().setGroupVisible(R.id.group_create, false);
                                actionMode.getMenu().setGroupVisible(R.id.group_display, true);
                                actionMode.getMenu().findItem(R.id.update_item).setVisible(true);
                                break;
                            case 0:
                                actionMode.finish();
                                break;
                            default:
                        }
                    }
                }
                if (actionMode != null) {
                    actionMode.setTitle(String.valueOf(selectedValues.size()));
                }
                boolean isActive = holder.position == activeItemPosition;
                setItemBackground(holder.view, entityValueUiConnector, isActive);
            });
        }

        private void setItemBackground(View view, EntityValueUiConnector entityValueUiConnector, boolean isActive) {
            if (selectedValues.contains(entityValueUiConnector)) {
                if (isActive) {
                    view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_active_selected));
                } else {
                    view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_selected));
                }
            } else {
                if (isActive) {
                    view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_active));
                } else {
                    view.setBackground(ContextCompat.getDrawable(this.context, R.drawable.list_item_default));
                }
            }
        }

        @Override
        public int getItemCount() {
            return values.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;
            public final ObjectCell contentView;
            public final CheckBox checkBox;
            public int position;

            public ViewHolder(@NonNull View view) {
                super(view);

                this.view = view;
                contentView = view.findViewById(R.id.content);
                checkBox = view.findViewById(R.id.cbx);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + contentView.getDescription() + "'";
            }
        }
    }

    private void updateAndValidate(RecyclerView recyclerView, EntityValueUiConnector connector) {
        boolean isValid = true;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View listItem = recyclerView.getChildAt(i);
            TextView propertyLabel = listItem.findViewById(R.id.property_label);
            EditText propertyInput = listItem.findViewById(R.id.property_value);

            String propertyName = propertyLabel.getText().toString();
            Property property = connector.getConnectedObject().getEntityType().getProperty(propertyName);
            String stringValue = propertyInput.getText().toString();
            DataValue propertyDataValue = ItemCreateFragment.convertPropertyForCreate(stringValue, property.getDataType());
            if (!ItemCreateFragment.isValidProperty(property, stringValue)) {
                propertyInput.setError(getResources().getString(R.string.mandatory_warning));
                isValid = false;
            }
            if (propertyDataValue != null) {
                connector.getConnectedObject().setDataValue(property, propertyDataValue);
            }
        }

        if (isValid) {
            if (twoPane) {
                dataContentUtilities.update(ItemListActivity.this, connector);
            } else {
                dataContentUtilities.create(ItemListActivity.this, connector);
            }
        }
    }

    public void refreshItemList() {
        adapter.notifyDataSetChanged();
    }
}
