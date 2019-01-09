package com.nyp.fypj.smartbackpackapp.mdui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager;
import com.sap.cloud.mobile.fiori.object.ObjectCell;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nyp.fypj.smartbackpackapp.R;


public class EntitySetListActivity extends AppCompatActivity {

	private static final int SETTINGS_SCREEN_ITEM = 200;
	private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetListActivity.class);
	private static final int BLUE_ANDROID_ICON = R.drawable.ic_android_blue;
	private static final int WHITE_ANDROID_ICON = R.drawable.ic_android_white;

	public enum EntitySetName {
			IotData("IotData", R.string.eset_iotdata,BLUE_ANDROID_ICON),
			IotDevice("IotDevice", R.string.eset_iotdevice,WHITE_ANDROID_ICON),
			Iotdeviceinfo("Iotdeviceinfo", R.string.eset_iotdeviceinfo,BLUE_ANDROID_ICON),
			User("User", R.string.eset_user,WHITE_ANDROID_ICON),
			UserDevices("UserDevices", R.string.eset_userdevices,BLUE_ANDROID_ICON),
			Userinfos("Userinfos", R.string.eset_userinfos,WHITE_ANDROID_ICON);

		private int titleId;
		private int iconId;
		private String entitySetName;

		EntitySetName(String name, int titleId, int iconId) {
				this.entitySetName = name;
				this.titleId = titleId;
				this.iconId = iconId;
		}

		public int getTitleId() {
				return this.titleId;
		}

		public String getEntitySetName() {
				return this.entitySetName;
		}
	}

	private final List<String> entitySetNames = new ArrayList<>();
	private final Map<String, EntitySetName> entitySetNameMap = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_entity_list);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		entitySetNames.clear();
		entitySetNameMap.clear();
		for (EntitySetName entitySet : EntitySetName.values()) {
			String entitySetTitle = getResources().getString(entitySet.getTitleId());
			entitySetNames.add(entitySetTitle);
			entitySetNameMap.put(entitySetTitle, entitySet);
		}

		final ListView listView = findViewById(R.id.entity_list);
		final EntitySetListAdapter adapter = new EntitySetListAdapter(this, R.layout.entity_list_element, entitySetNames);

		listView.setAdapter(adapter);

		listView.setOnItemClickListener((parent, view, position, id) -> {

            EntitySetName entitySetName = entitySetNameMap.get(adapter.getItem(position));
            Context context = EntitySetListActivity.this;
            Intent intent = new Intent(context, ItemListActivity.class);
            intent.putExtra(ItemListActivity.ARG_ITEM_SET, entitySetName);
            context.startActivity(intent);
		});
			
	}

	public class EntitySetListAdapter extends ArrayAdapter<String> {

		EntitySetListAdapter(@NonNull Context context, int resource, List<String> entitySetNames) {
			super(context, resource, entitySetNames);
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			EntitySetName entitySetName = entitySetNameMap.get(getItem(position));
			if (convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.entity_list_element, parent, false);
			}
			ObjectCell entitySetCell = convertView.findViewById(R.id.entity_set_name);
			entitySetCell.setHeadline(entitySetName.entitySetName);
			entitySetCell.setDetailImage(entitySetName.iconId);
			return convertView;
		}
	}
				
	@Override
	public void onBackPressed() {
			moveTaskToBack(true);
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SETTINGS_SCREEN_ITEM, 0, R.string.menu_item_settings);
		return true;
	}

	@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		LOGGER.debug("onOptionsItemSelected: " + item.getTitle());
		if (item.getItemId() == SETTINGS_SCREEN_ITEM) {
			LOGGER.debug("settings screen menu item selected.");
			Intent intent = new Intent(this, SettingsActivity.class);
			this.startActivityForResult(intent, SETTINGS_SCREEN_ITEM);
			return true;
        }
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LOGGER.debug("EntitySetListActivity::onActivityResult, request code: " + requestCode + " result code: " + resultCode);
		if (requestCode == SETTINGS_SCREEN_ITEM) {
			LOGGER.debug("Calling AppState to retrieve settings after settings screen is closed.");
		}
	}
}
