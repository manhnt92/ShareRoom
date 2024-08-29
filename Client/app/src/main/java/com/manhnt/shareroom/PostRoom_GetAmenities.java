package com.manhnt.shareroom;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.Amenities;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import com.manhnt.object.Room_Address;
import com.manhnt.object.Room_Amenities;
import com.manhnt.adapter.AmenitiesAdapter;
import com.manhnt.adapter.AmenitiesAdapter.AmenitiesAdapterListener;
import com.manhnt.object.Room_Images;
import com.manhnt.object.Room_Properties;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostRoom_GetAmenities extends Activity implements OnClickListener, AmenitiesAdapterListener {

	private int position_Province_ID, position_District_ID, position_Ward_ID;
	private Account mAccount;
	private Room room;
	private Room_Amenities room_amenities;
	private ImageButton btn_save;
	private MaterialEditText edt_amenities;
	private String other_amenities;
	private ArrayList<Amenities> List_Amenities;
	private MyRooms myRooms;
	private boolean isEdit, isTouch = false;
	private Room_Amenities room_amenities_rollback;
	private int cb_width = 96;
	private boolean isAutoIntentBack = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_room_get_amentities);
		getExtraBundle();
		if(room.getRoom_amenities() != null){
			List_Amenities = room.getRoom_amenities().getList_amenities();
			cloneRoomAmenities(room.getRoom_amenities());
			other_amenities = room.getRoom_amenities().getOther();
		}else {
			initListAmenities();
		}
		getWidget();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		isTouch = ev.getX() <= cb_width;
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public void onClickCheckBox(int width) {
		cb_width = width;
		if(isEdit && isTouch){
			btn_save.setVisibility(View.VISIBLE);
		}
	}
	
	private void cloneRoomAmenities(Room_Amenities room_amenities) {
		try {
			room_amenities_rollback = (Room_Amenities) room_amenities.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private void getExtraBundle() {
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		switch (from) {
			case Config.POST_ROOM_GET_IMAGES:
				room = (Room) getIntent().getExtras().getSerializable(Config.BUNDLE_ROOM);
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
				position_District_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_DISTRICT_ID);
				position_Ward_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_WARD_ID);
				isEdit = false;
				break;
			case Config.ROOM_DETAIL_ACTIVITY:
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
				if(myRooms != null) {
					room = myRooms.getList_room().get(myRooms.getPosition());
				}
				isEdit = true;
				break;
			default:
				break;
		}
	}
	
	private void getWidget() {
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		btn_save = manager.ImageButton(R.id.btn_save, this, true);
		btn_save.setVisibility(View.GONE);
		edt_amenities = manager.MaterialEditText(R.id.edt_amenities, true);
		edt_amenities.setText(other_amenities);
		edt_amenities.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable arg0) {
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		ButtonRectangle btn_post = manager.ButtonRectangle(R.id.btn_post, this, true);
		ListView listView_amenities = (ListView) findViewById(R.id.list_amenities);
		AmenitiesAdapter adapter = new AmenitiesAdapter(this, 0, List_Amenities);
		adapter.setListener(this);
		listView_amenities.setAdapter(adapter);
		if(isEdit){
			btn_post.setVisibility(View.GONE);
		}

	}
	
	private void initListAmenities() {
		String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
		List_Amenities = new ArrayList<>();
		for(int i = 0; i< array_amenities.length; i = i+2){
			List_Amenities.add(new Amenities(array_amenities[i], false, array_amenities[i + 1]));
		}
	}

	@Override
	public void onBackPressed() {
		if(isEdit){
			checkBtnSave();
		}else {
			intentBack();
		}
	}

	private void checkBtnSave() {
		if(btn_save.getVisibility() == View.VISIBLE){
			DialogManager.getInstance().YesNoDialog(this, R.string.room_amenities_title,
				R.string.question_update, R.string.OK, R.string.NO, saveListener, true).show();
		}else {
			intentBackEdit();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			if(isEdit){
				checkBtnSave();
			}else {
				intentBack();
			}
			break;
		case R.id.btn_post:
			other_amenities = edt_amenities.getText().toString();
			room_amenities = new Room_Amenities(List_Amenities, other_amenities);
			room.setRoom_amenities(room_amenities);
			RequestAPI.getInstance().context(this).method(RequestAPI.POST).isParams(true).isAuthorization(true)
				.url(Config.URL_CREATE_ROOM).message(getResources().getString(R.string.waiting))
				.isShowDialog(true).isShowToast(true).execute(createRoomListener);
			break;
		case R.id.btn_save:
			RequestAPI.getInstance().context(this).method(RequestAPI.PUT).isParams(true).isAuthorization(true)
				.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.isShowDialog(true).isShowToast(true).message(getResources().getString(R.string.waiting)).execute(updateRoomAmenitiesListener);
			break;
		default:
			break;
		}
	}

	private RequestAPI.RequestAPIListener createRoomListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			Room_Address room_address = room.getRoom_address();
			Room_Properties room_Properties = room.getRoom_properties();
			Room_Images room_images = room.getRoom_images();
			Room_Amenities room_amenities = room.getRoom_amenities();
			JSONObject jObj = new JSONObject();
			JSONObject jsonAddress = new JSONObject();
			jsonAddress.put(Config.PROVINCE_ID, room_address.getProvince().getProvince_id());
			jsonAddress.put(Config.DISTRICT_ID, room_address.getDistrict().getDistrict_id());
			jsonAddress.put(Config.WARD_ID, room_address.getWard().getWard_id());
			jsonAddress.put(Config.ADDRESS, room_address.getAddress());
			jsonAddress.put(Config.LATITUDE, room_address.getLatitude());
			jsonAddress.put(Config.LONGITUDE, room_address.getLongitude());
			jObj.put(Config.ROOM_ADDRESS, jsonAddress);
			JSONObject jsonProperties = new JSONObject();
			jsonProperties.accumulate(Config.RENT_PER_MONTH, Math.round(room_Properties.getRent_per_month()
					* Config.ROUND)/Config.ROUND);
			jsonProperties.accumulate(Config.ELECTRIC, Math.round(room_Properties.getElectric()
					* Config.ROUND)/Config.ROUND);
			jsonProperties.put(Config.WATER, room_Properties.getWater());
			jsonProperties.put(Config.AREA, room_Properties.getArea());
			jsonProperties.put(Config.NUMBER_PER_ROOM, room_Properties.getNumber_per_room());
			jsonProperties.put(Config.MIN_STAY, room_Properties.getMin_stay());
			jsonProperties.put(Config.AVAILABLE_FROM, room_Properties.getAvailable_from());
			jsonProperties.put(Config.ROOM_TYPE, room_Properties.getRoom_type());
			jsonProperties.put(Config.ROOM_STATE, room_Properties.getRoom_state());
			jObj.put(Config.ROOM_PROPERTIES, jsonProperties);
			JSONArray jsonImages = new JSONArray();
			for( int i = 0 ; i< room_images.getList_images().size() - 1;i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(Config.LINK, room_images.getList_images().get(i).getPath());
				jsonObject.put(Config.NOTE, room_images.getList_images().get(i).getNote());
				jsonImages.put(jsonObject);
			}
			jObj.put(Config.ROOM_IMAGES, jsonImages);
			JSONObject jsonAmenities = new JSONObject();
			for(int i = 0; i< room_amenities.getList_amenities().size(); i++){
				jsonAmenities.put(room_amenities.getList_amenities().get(i).getName_json(),
						room_amenities.getList_amenities().get(i).isSelected());
			}
			jsonAmenities.put(Config.OTHER, room_amenities.getOther());
			jObj.put(Config.ROOM_AMENITIES, jsonAmenities);
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			Intent intent = new Intent(PostRoom_GetAmenities.this, MyRoomsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
			intent.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_AMENITIES);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		@Override
		public void onError(Exception e) {}
	};

	private DialogManager.YesNoDialogListener saveListener = new DialogManager.YesNoDialogListener() {
		@Override
		public void onYes(MaterialDialog dialog) {
			isAutoIntentBack = true;
			RequestAPI.getInstance().context(PostRoom_GetAmenities.this).method(RequestAPI.PUT).isParams(true)
				.isAuthorization(true).url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.isShowDialog(true).isShowToast(true).message(getResources().getString(R.string.waiting)).execute(updateRoomAmenitiesListener);
			dialog.dismiss();
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			myRooms.getList_room().get(myRooms.getPosition()).setRoom_amenities(room_amenities_rollback);
			intentBackEdit();
			dialog.dismiss();
		}

	};

	private RequestAPI.RequestAPIListener updateRoomAmenitiesListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			JSONObject jObj = new JSONObject();
			JSONObject jsonAmenities = new JSONObject();
			for(int i = 0; i< List_Amenities.size(); i++){
				jsonAmenities.put(List_Amenities.get(i).getName_json(), List_Amenities.get(i).isSelected());
			}
			jsonAmenities.put(Config.OTHER, edt_amenities.getText().toString());
			jObj.put(Config.ROOM_AMENITIES, jsonAmenities);
			jObj.put(Config.UPDATE_TYPE, Config.UPDATE_ROOM_AMENITIES);
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			JSONObject jObj = new JSONObject(contentMessage);
			room_amenities = new Room_Amenities();
			String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
			ArrayList<Amenities> List_Amenities = new ArrayList<>();
			for(int i = 0; i< array_amenities.length; i = i+2){
				boolean bool = jObj.optBoolean(array_amenities[i + 1]);
				List_Amenities.add(new Amenities(array_amenities[i], bool, array_amenities[i + 1]));
			}
			room_amenities.setList_amenities(List_Amenities);
			room_amenities.setOther(jObj.optString(Config.OTHER));
			room.setRoom_amenities(room_amenities);
			myRooms.getList_room().set(myRooms.getPosition(), room);
			btn_save.setVisibility(View.GONE);
			if(isAutoIntentBack){
				intentBackEdit();
			} else {
				cloneRoomAmenities(room_amenities);
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intentBack(){
		other_amenities = edt_amenities.getText().toString();
		room_amenities = new Room_Amenities(List_Amenities, other_amenities);
		room.setRoom_amenities(room_amenities);
		Intent intent_back = new Intent(PostRoom_GetAmenities.this, PostRoom_GetImages.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_ROOM, room);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_AMENITIES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
	}

	private void intentBackEdit(){
		Intent intent_back = new Intent(PostRoom_GetAmenities.this, MyRoomDetailActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_AMENITIES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
