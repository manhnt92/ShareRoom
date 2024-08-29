package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.manhnt.config.Config;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.adapter.RoomAdapter;

public class SearchRoomsViewAsListActivity extends Activity implements OnClickListener {

	private Account mAccount;
	private LatLngBounds latLngBounds;
	private float zoom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_rooms_activity);
		getExtraBundle();
		getWidget();
	}

	private void getWidget() {
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		ListView list_view_rooms = (ListView) findViewById(R.id.list_view_my_rooms);
		list_view_rooms.setVisibility(View.VISIBLE);
		RoomAdapter roomAdapter = new RoomAdapter(SearchRoomsViewAsListActivity.this, 0, Config.LIST_ROOM_AFTER);
		roomAdapter.setItemClickListener(new RoomAdapter.OnItemClick() {
			@Override
			public void onItemClickListener(int index) {
				intent(SearchRoomsViewDetail.class, index);
			}
		});
		list_view_rooms.setAdapter(roomAdapter);
	}

	private void getExtraBundle() {
		if(getIntent().getExtras() != null) {
			int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
			if (from > 0) {
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				double ne_lat = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LAT);
				double ne_lng = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LNG);
				double sw_lat = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LAT);
				double sw_lng = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LNG);
				latLngBounds = new LatLngBounds(new LatLng(sw_lat, sw_lng), new LatLng(ne_lat, ne_lng));
				zoom = getIntent().getExtras().getFloat(Config.BUNDLE_ZOOM);
			}
		}
	}

	@Override
	public void onBackPressed() {
		intent(SearchRoomActivity.class, -1);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			intent(SearchRoomActivity.class, -1);
			break;
		default:
			break;
		}
	}

	private void intent(Class<? extends Activity> clazz, int pos_room_in_list_room_after){
		Intent i = new Intent(SearchRoomsViewAsListActivity.this, clazz);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		if(pos_room_in_list_room_after > -1){
			i.putExtra(Config.BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER, pos_room_in_list_room_after);
		}
		i.putExtra(Config.BUNDLE_NE_LAT, latLngBounds.northeast.latitude);
		i.putExtra(Config.BUNDLE_NE_LNG, latLngBounds.northeast.longitude);
		i.putExtra(Config.BUNDLE_SW_LAT, latLngBounds.southwest.latitude);
		i.putExtra(Config.BUNDLE_SW_LNG, latLngBounds.southwest.longitude);
		i.putExtra(Config.BUNDLE_ZOOM, zoom);
		i.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_VIEW_AS_LIST_ACTIVITY);
		startActivity(i);
		if(pos_room_in_list_room_after > -1) {
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		} else {
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
	}

}
