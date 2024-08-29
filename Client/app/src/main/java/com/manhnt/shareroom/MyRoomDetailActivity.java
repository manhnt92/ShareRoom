package com.manhnt.shareroom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cloudinary.Cloudinary;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.manhnt.adapter.RoomAmenitiesAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.database.ShareRoomDatabase;
import com.manhnt.object.Account;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import com.manhnt.widget.ExpandableHeightListView;
import org.json.JSONException;
import org.json.JSONObject;

public class MyRoomDetailActivity extends Activity implements OnClickListener {

	private Account mAccount;
	private MyRooms myRooms;
	private Room currentRoom;
	private ScrollView scrollView;
	private Cloudinary cloudinary;
	private Class<? extends Activity> edit_activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_room_detail_activity);
		getExtraBundle();
		getWidget();
	}

	private void getExtraBundle() {
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		if (from > 0) {
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
			if (myRooms != null) {
				currentRoom = myRooms.getList_room().get(myRooms.getPosition());
			}
		}
	}

	@Override
	public void onBackPressed() {
		intentBack();
	}

	@SuppressLint("SetTextI18n")
	@SuppressWarnings("deprecation")
	private void getWidget() {
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		manager.ImageButton(R.id.btn_delete, this, true);
		manager.ImageButton(R.id.btn_edit_room_images, this, true);
		manager.ImageButton(R.id.btn_edit_room_properties, this, true);
		manager.ImageButton(R.id.btn_edit_room_address, this, true);
		manager.ImageButton(R.id.btn_edit_room_amenities, this, true);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		manager.TextView(R.id.txt_room_images_title, true);
		manager.SliderLayout(R.id.slider, true, currentRoom.getRoom_images().getList_images());
		manager.TextView(R.id.txt_room_address_title, true);
		TextView txt_1 = manager.TextView(R.id.txt_1, true);
		TextView txt_2 = manager.TextView(R.id.txt_2, true);
		TextView txt_3 = manager.TextView(R.id.txt_3, true);
		TextView txt_4 = manager.TextView(R.id.txt_4, true);
		manager.TextView(R.id.dots, true);
		TextView txt_room_address_province = manager.TextView(R.id.txt_room_address_province, true);
		TextView txt_room_address_district = manager.TextView(R.id.txt_room_address_district, true);
		TextView txt_room_address_ward = manager.TextView(R.id.txt_room_address_ward, true);
		TextView txt_room_address_address = manager.TextView(R.id.txt_room_address_address, true);
		ImageView transparentImageView = manager.ImageView(R.id.transparent_image, this, false);
		transparentImageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case MotionEvent.ACTION_DOWN:
						scrollView.requestDisallowInterceptTouchEvent(true);
						return false;
					case MotionEvent.ACTION_UP:
						scrollView.requestDisallowInterceptTouchEvent(false);
						return true;
					case MotionEvent.ACTION_MOVE:
						scrollView.requestDisallowInterceptTouchEvent(true);
						return false;
					default:
						return true;
				}
			}
		});
		GoogleMap googleMap = manager.GoogleMap(R.id.google_map);
		IconGenerator iconFactory = new IconGenerator(MyRoomDetailActivity.this);
		iconFactory.setStyle(IconGenerator.STYLE_GREEN);
		String province_id = currentRoom.getRoom_address().getProvince().getProvince_id();
		String district_id = currentRoom.getRoom_address().getDistrict().getDistrict_id();
		String ward_id = currentRoom.getRoom_address().getWard().getWard_id();
		ShareRoomDatabase db = ShareRoomDatabase.getInstance(this);
		txt_1.setText(" - " + getResources().getString(R.string.province));
		txt_room_address_province.setText(" : " + db.getProvinceName_FromProvinceID(province_id));
		txt_2.setText(" - " + getResources().getString(R.string.district));
		txt_room_address_district.setText(" : " + db.getDistrictName_FromDistrictID(district_id));
		txt_3.setText(" - " + getResources().getString(R.string.ward));
		txt_room_address_ward.setText(" : " + db.getWardName_FromWardID(ward_id));
		txt_4.setText(" - " + getResources().getString(R.string.address));
		txt_room_address_address.setText(currentRoom.getRoom_address().getAddress());
		MarkerOptions markerOptions = new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(getResources().getString(R.string.room_address_title), Config.getTypeface(getAssets()))))
			.draggable(true).visible(true)
			.position(new LatLng(currentRoom.getRoom_address().getLatitude(), currentRoom.getRoom_address().getLongitude())).
			anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
		googleMap.addMarker(markerOptions);
		CameraPosition position = new CameraPosition.Builder().target(new LatLng(currentRoom.getRoom_address().getLatitude(), currentRoom.getRoom_address().getLongitude())).zoom(14).build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
		manager.TextView(R.id.txt_room_properties_title, true);
		TextView txt_5 = manager.TextView(R.id.txt_5, true);
		TextView txt_6 = manager.TextView(R.id.txt_6, true);
		TextView txt_7 = manager.TextView(R.id.txt_7, true);
		TextView txt_8 = manager.TextView(R.id.txt_8, true);
		TextView txt_9 = manager.TextView(R.id.txt_9, true);
		TextView txt_10 = manager.TextView(R.id.txt_10, true);
		TextView txt_11 = manager.TextView(R.id.txt_11, true);
		TextView txt_12 = manager.TextView(R.id.txt_12, true);
		TextView txt_13 = manager.TextView(R.id.txt_13, true);
		TextView txt_room_properties_rent = manager.TextView(R.id.txt_room_properties_rent, true);
		TextView txt_room_properties_electric = manager.TextView(R.id.txt_room_properties_electric, true);
		TextView txt_room_properties_water = manager.TextView(R.id.txt_room_properties_water, true);
		TextView txt_room_properties_area = manager.TextView(R.id.txt_room_properties_area, true);
		TextView txt_room_properties_number = manager.TextView(R.id.txt_room_properties_number, true);
		TextView txt_room_properties_min_stay = manager.TextView(R.id.txt_room_properties_min_stay, true);
		TextView txt_room_properties_available_from = manager.TextView(R.id.txt_room_properties_available_from, true);
		TextView txt_room_properties_room_type = manager.TextView(R.id.txt_room_properties_room_type, true);
		TextView txt_room_properties_room_state = manager.TextView(R.id.txt_room_properties_room_state, true);
		txt_5.setText(" - " + getResources().getString(R.string.txt_rent_per_month));
    	txt_room_properties_rent.setText(" : "+ currentRoom.getRoom_properties().getRent_per_month() + " " +getResources().getString(R.string.million_vnd)); 
    	txt_6.setText(" - " + getResources().getString(R.string.txt_electric));
    	txt_room_properties_electric.setText(" : " + currentRoom.getRoom_properties().getElectric() + "00 " + getResources().getString(R.string.vnd));
    	txt_7.setText(" - " + getResources().getString(R.string.txt_water));
    	txt_room_properties_water.setText(" : " + currentRoom.getRoom_properties().getWater() + ".000" + getResources().getString(R.string.vnd));
    	txt_8.setText(" - " + getResources().getString(R.string.txt_area));
    	txt_room_properties_area.setText(" : " + currentRoom.getRoom_properties().getArea() + " m\u00B2");
    	txt_9.setText(" - " + getResources().getString(R.string.txt_number_per_room));
    	txt_room_properties_number.setText(" : " + currentRoom.getRoom_properties().getNumber_per_room() + " " + getResources().getString(R.string.person));
    	txt_10.setText(" - " + getResources().getString(R.string.txt_min_stay));
    	txt_room_properties_min_stay.setText(" : " + currentRoom.getRoom_properties().getMin_stay() + " " + getResources().getString(R.string.month));
    	txt_11.setText(" - " + getResources().getString(R.string.txt_available_from));
    	txt_room_properties_available_from.setText(" : " + currentRoom.getRoom_properties().getAvailable_from());
    	txt_12.setText(" - " + getResources().getString(R.string.txt_room_type));
    	txt_room_properties_room_type.setText(" : " + currentRoom.getRoom_properties().getRoom_type());
    	txt_13.setText(" - " + getResources().getString(R.string.txt_room_state));
    	txt_room_properties_room_state.setText(" : " + currentRoom.getRoom_properties().getRoom_state());
		manager.TextView(R.id.txt_room_amenities_title, true);
		ExpandableHeightListView lv_amenities = (ExpandableHeightListView) findViewById(R.id.lv_amenities);
    	lv_amenities.setExpanded(true);
    	ArrayList<String> list_amenities = new ArrayList<>();
    	for (int i = 0; i< currentRoom.getRoom_amenities().getList_amenities().size(); i++){
    		if(currentRoom.getRoom_amenities().getList_amenities().get(i).isSelected()){
    			list_amenities.add(currentRoom.getRoom_amenities().getList_amenities().get(i).getName());
    		}
    	}
    	if(currentRoom.getRoom_amenities().getOther().length() > 0){
    		list_amenities.add(currentRoom.getRoom_amenities().getOther());
    	}
    	lv_amenities.setAdapter(new RoomAmenitiesAdapter(this, 0, list_amenities, true));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			intentBack();
			break;
		case R.id.btn_delete:
			DialogManager.getInstance().YesNoDialog(this, R.string.room_detail_title,
				R.string.question_delete_room, R.string.OK, R.string.back, deleteListener, true).show();
			break;
		case R.id.btn_edit_room_images:
			edit_activity = PostRoom_GetImages.class;
			DialogManager.getInstance().YesNoDialog(this, R.string.room_detail_title,
				R.string.question_edit_room_images, R.string.OK, R.string.back, updateListener, true).show();
			break;
		case R.id.btn_edit_room_properties:
			edit_activity = PostRoom_GetProperties.class;
			DialogManager.getInstance().YesNoDialog(this, R.string.room_detail_title,
				R.string.question_edit_room_properties, R.string.OK, R.string.back, updateListener, true).show();
			break;
		case R.id.btn_edit_room_address:
			edit_activity = PostRoom_GetAddress.class;
			DialogManager.getInstance().YesNoDialog(this, R.string.room_detail_title,
				R.string.question_edit_room_address, R.string.OK, R.string.back, updateListener, true).show();
			break;
		case R.id.btn_edit_room_amenities:
			edit_activity = PostRoom_GetAmenities.class;
			DialogManager.getInstance().YesNoDialog(this, R.string.room_detail_title,
				R.string.question_edit_room_amenities, R.string.OK, R.string.back, updateListener, true).show();
			break;
		default:
			break;
		}
	}

	private DialogManager.YesNoDialogListener updateListener = new DialogManager.YesNoDialogListener() {

		@Override
		public void onYes(MaterialDialog dialog) {
			Intent intent = new Intent(MyRoomDetailActivity.this, edit_activity);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.putExtra(Config.FROM_ACTIVITY, Config.ROOM_DETAIL_ACTIVITY);
			intent.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
			intent.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			dialog.dismiss();
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			dialog.dismiss();
		}

	};

	private DialogManager.YesNoDialogListener deleteListener = new DialogManager.YesNoDialogListener() {

		@Override
		public void onYes(MaterialDialog dialog) {
			Map<String, String> config = new HashMap<>();
			config.put("cloud_name", Config.CLOUD_NAME);
			config.put("api_key", Config.CLOUD_API_KEY);
			config.put("api_secret", Config.CLOUD_API_SECRET);
			cloudinary = new Cloudinary(config);
			RequestAPI.getInstance().context(MyRoomDetailActivity.this).method(RequestAPI.DELETE)
				.url(Config.URL_DELETE_ROOM + currentRoom.getId()).message(getString(R.string.waiting))
				.isParams(false).isAuthorization(true).isShowToast(true)
				.isShowDialog(true).execute(deleteRoomListener);
			dialog.dismiss();
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			dialog.dismiss();
		}

	};

	private RequestAPI.RequestAPIListener deleteRoomListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			if(currentRoom.getRoom_images().getList_images().size() > 0){
				for(int i = 0 ; i < currentRoom.getRoom_images().getList_images().size() ; i++){
					String path = currentRoom.getRoom_images().getList_images().get(i).getPath();
					String image_name = (path.split("/")[path.split("/").length - 1]);
					String public_id = image_name.split("\\.")[0];
					try {
						cloudinary.uploader().destroy(public_id, Cloudinary.emptyMap());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			myRooms.getList_room().remove(myRooms.getPosition());
			intentBack();
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intentBack(){
		Intent intent_back = new Intent(MyRoomDetailActivity.this, MyRoomsActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.ROOM_DETAIL_ACTIVITY);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
