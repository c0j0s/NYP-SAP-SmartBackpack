package com.nyp.fypj.smartbackpackapp.mdui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.nyp.fypj.smartbackpackapp.R;


/**
 * An activity representing a single Person detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_TYPE = "item_type";
    public static final String ARG_UPDATE = "update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            int position = getIntent().getIntExtra(ItemDetailActivity.ARG_ITEM_ID, -1);
            boolean isUpdate = getIntent().getBooleanExtra(ItemDetailActivity.ARG_UPDATE, false);
            EntitySetListActivity.EntitySetName entitySetName = (EntitySetListActivity.EntitySetName) getIntent().getSerializableExtra(ItemDetailActivity.ARG_ITEM_TYPE);

            if (position == -1 || isUpdate) {
                //CREATE
                Bundle arguments = new Bundle();
                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, entitySetName);
                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, position);
                arguments.putBoolean(ItemDetailActivity.ARG_UPDATE, isUpdate);
                ItemCreateFragment fragment = new ItemCreateFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                //NAVIGATE DOWN
                Bundle arguments = new Bundle();
                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, getIntent().getIntExtra(ItemDetailActivity.ARG_ITEM_ID, 0));
                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, getIntent().getSerializableExtra(ItemDetailActivity.ARG_ITEM_TYPE));

                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                navigateUpTo(new Intent(this, ItemListActivity.class));
                return true;
            default:
                return false;
        }
    }
}
