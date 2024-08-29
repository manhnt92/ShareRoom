package com.manhnt.shareroom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.views.Slider.OnValueChangedListener;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import com.manhnt.object.Room_Properties;
import org.json.JSONException;
import org.json.JSONObject;

public class PostRoom_GetProperties extends FragmentActivity implements OnClickListener, OnDateSetListener {

	private int position_Province_ID, position_District_ID, position_Ward_ID;
	private Account mAccount;
	private Room room;
	private ImageButton btn_save;
	private TextView txt_rent_per_month_content, txt_electric_content, txt_water_content, txt_area_content,
		txt_number_per_room_content, txt_min_stay_content, txt_available_from_content, txt_room_type_content,
		txt_room_state_content;
	private Slider slider_rent_per_month, slider_electric, slider_water, slider_area, slider_number_per_room,
		slider_min_stay;
	private Room_Properties room_properties;
	private float rent_per_month, electric;
	private int water, area, number_per_room, min_stay;
	private MyRooms myRooms;
	private boolean isEdit;
	private Room_Properties room_properties_rollback;
	private boolean isAutoIntentBack = false;
	private TextView curTextView;
	private String[] curStringArr;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_room_get_properties);
		getExtraBundle();
		getWidget();
		room_properties = room.getRoom_properties();
		if(room_properties != null){
			cloneRoomProperties(room_properties);
			rent_per_month = room_properties.getRent_per_month();
			slider_rent_per_month.setValue((int)(rent_per_month * 10));
			electric = room_properties.getElectric();
			slider_electric.setValue((int)(electric * 10));
			water = room_properties.getWater();
			slider_water.setValue(water);
			area = room_properties.getArea();
			slider_area.setValue(area);
			number_per_room = room_properties.getNumber_per_room();
			slider_number_per_room.setValue(number_per_room * 10);
			min_stay = room_properties.getMin_stay();
			slider_min_stay.setValue(min_stay * 8);
			txt_rent_per_month_content.setText("" + rent_per_month + " " + getString(R.string.million_vnd));
			txt_electric_content.setText("" + electric + "00 " + getString(R.string.vnd));
			txt_water_content.setText("" + water + ".000 " + getString(R.string.vnd));
			txt_area_content.setText("" + area + " m\u00B2");
			txt_number_per_room_content.setText("" + number_per_room + " " + getString(R.string.person));
			txt_min_stay_content.setText("" + min_stay + " " + getString(R.string.month));
			txt_available_from_content.setText(room_properties.getAvailable_from());
			txt_room_type_content.setText(room_properties.getRoom_type());
			txt_room_state_content.setText(room_properties.getRoom_state());
		}
	}

	private void cloneRoomProperties(Room_Properties room_properties){
		try {
			room_properties_rollback = (Room_Properties) room_properties.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private void getExtraBundle() {
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		if(from == Config.POST_ROOM_GET_ADDRESS || from == Config.POST_ROOM_GET_IMAGES){
			room = (Room) getIntent().getExtras().getSerializable(Config.BUNDLE_ROOM);
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
			position_District_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_DISTRICT_ID);
			position_Ward_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_WARD_ID);
			isEdit = false;
		} else if(from == Config.ROOM_DETAIL_ACTIVITY){
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
			if(myRooms != null) {
				room = myRooms.getList_room().get(myRooms.getPosition());
			}
			isEdit = true;
		}
	}
	
	private void getWidget() {
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		ImageButton btn_next = manager.ImageButton(R.id.btn_next, this, true);
		btn_save = manager.ImageButton(R.id.btn_save, this, true);
		btn_save.setVisibility(View.GONE);
		if(isEdit){
			btn_next.setVisibility(View.GONE);
		}else {
			btn_next.setVisibility(View.VISIBLE);
		}
		manager.TextView(R.id.txt_rent_per_month, true);
		txt_rent_per_month_content = manager.TextView(R.id.txt_rent_per_month_content, true);
		manager.TextView(R.id.txt_electric, true);
		txt_electric_content = manager.TextView(R.id.txt_electric_content, true);
		manager.TextView(R.id.txt_water, true);
		txt_water_content = manager.TextView(R.id.txt_water_content, true);
		manager.TextView(R.id.txt_area, true);
		txt_area_content = manager.TextView(R.id.txt_area_content, true);
		txt_area_content.setText("0 m\u00B2");
		manager.TextView(R.id.txt_number_per_room, true);
		txt_number_per_room_content = manager.TextView(R.id.txt_number_per_room_content, true);
		manager.TextView(R.id.txt_min_stay, true);
		txt_min_stay_content = manager.TextView(R.id.txt_min_stay_content, true);
		manager.TextView(R.id.txt_available_from, true);
		txt_available_from_content = manager.TextView(R.id.txt_available_from_content, true);
		@SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date d = new Date();
		txt_available_from_content.setText(dateFormat.format(d));
		manager.TextView(R.id.txt_room_type, true);
		txt_room_type_content = manager.TextView(R.id.txt_room_type_content, true);
		manager.TextView(R.id.txt_room_state, true);
		txt_room_state_content = manager.TextView(R.id.txt_room_state_content, true);
		slider_rent_per_month = manager.Slider(R.id.slider_rent_per_month, true);
		slider_electric = manager.Slider(R.id.slider_electric, true);
		slider_water = manager.Slider(R.id.slider_water, true);
		slider_area = manager.Slider(R.id.slider_area, true);
		slider_number_per_room = manager.Slider(R.id.slider_number_per_room, true);
		slider_min_stay = manager.Slider(R.id.slider_min_stay, true);
		manager.ButtonRectangle(R.id.btn_available_from, this, true);
		manager.ButtonRectangle(R.id.btn_room_type, this, true);
		manager.ButtonRectangle(R.id.btn_room_state, this, true);
		slider_rent_per_month.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				rent_per_month = (float)value/10;
				txt_rent_per_month_content.setText("" + rent_per_month + " " + getString(R.string.million_vnd));
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		slider_electric.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				electric = (float) value/10;
				txt_electric_content.setText("" + electric + "00 " + getString(R.string.vnd));
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		slider_water.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				water = value;
				txt_water_content.setText("" + water + ".000 " + getString(R.string.vnd));
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		slider_area.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				area = value;
				txt_area_content.setText("" + area + " m\u00B2");
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		slider_number_per_room.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				number_per_room = value/10;
				txt_number_per_room_content.setText("" + number_per_room + " " + getString(R.string.person));
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		slider_min_stay.setOnValueChangedListener(new OnValueChangedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onValueChanged(int value) {
				min_stay = value/8;
				txt_min_stay_content.setText("" + min_stay + " " + getString(R.string.month));
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(isEdit){
			checkBtnSave();
		}else {
			super.onBackPressed();
			intentBack();
		}
	}

	private void checkBtnSave(){
		if(btn_save.getVisibility() == View.VISIBLE){
			DialogManager.getInstance().YesNoDialog(this, R.string.room_properties_title,
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
		case R.id.btn_next:
			room_properties = new Room_Properties(rent_per_month, electric, water, area, number_per_room,
				min_stay, txt_available_from_content.getText().toString(), txt_room_type_content.getText().toString(),
				txt_room_state_content.getText().toString());
			room.setRoom_properties(room_properties);
			Intent intent_next = new Intent(PostRoom_GetProperties.this, PostRoom_GetImages.class);
			intent_next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent_next.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
			intent_next.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
			intent_next.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
			intent_next.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
			intent_next.putExtra(Config.BUNDLE_ROOM, room);
			intent_next.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_PROPERTIES);
			startActivity(intent_next);
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
			break;
		case R.id.btn_save:
			RequestAPI.getInstance().context(this).method(RequestAPI.PUT).isParams(true).isAuthorization(true)
				.message(getString(R.string.waiting))
				.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.isShowDialog(true).isShowToast(true).execute(updateRoomPropertiesListener);
			break;
		case R.id.btn_available_from:
			Calendar calendar = Calendar.getInstance();
			DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false,
				Config.getTypeface(getAssets()));
			datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getSupportFragmentManager(), "TAG");
			break;
		case R.id.btn_room_type:
			curTextView = txt_room_type_content;
			curStringArr = getResources().getStringArray(R.array.array_room_type);
			DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_room_type, curStringArr,
				-1, false, true, choiceListener).show();
			break;
		case R.id.btn_room_state:
			curTextView = txt_room_state_content;
			curStringArr = getResources().getStringArray(R.array.array_room_state);
			DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_room_state, curStringArr,
				-1, false, true, choiceListener).show();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
		String date ="";
		String link = "/";
		if(month >= 9 && day >= 10){
			date = day + link + (month+1) + link + year;
		} else if (month >= 9 && day < 10){
			date = "0" + day + link + (month+1) + link + year;
		} else if (month < 9 && day >= 10){
			date = day + link + "0" + (month+1) + link + year;
		} else if (month < 9 && day < 10){
			date = "0" + day + link + "0" + (month+1) + link + year;
		}
		txt_available_from_content.setText(date);
		if(isEdit){
			btn_save.setVisibility(View.VISIBLE);
		}
	}

	private DialogManager.ListOneChoiceDialogListener choiceListener = new DialogManager.ListOneChoiceDialogListener() {
		@Override
		public void onChoice(MaterialDialog dialog, int index) {
			if(index != -1){
				curTextView.setText(curStringArr[index]);
				if(isEdit){
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	private DialogManager.YesNoDialogListener saveListener = new DialogManager.YesNoDialogListener() {

		@Override
		public void onYes(MaterialDialog dialog) {
			isAutoIntentBack = true;
			RequestAPI.getInstance().context(PostRoom_GetProperties.this).method(RequestAPI.PUT)
				.isParams(true).isAuthorization(true).message(getString(R.string.waiting))
				.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.isShowDialog(true).isShowToast(true).execute(updateRoomPropertiesListener);
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			myRooms.getList_room().get(myRooms.getPosition()).setRoom_properties(room_properties_rollback);
			intentBackEdit();
			dialog.dismiss();
		}

	};

	private RequestAPI.RequestAPIListener updateRoomPropertiesListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			JSONObject jObj = new JSONObject();
			JSONObject jsonProperties = new JSONObject();
			jsonProperties.accumulate(Config.RENT_PER_MONTH, Math.round(rent_per_month * Config.ROUND)/Config.ROUND);
			jsonProperties.accumulate(Config.ELECTRIC, Math.round(electric * Config.ROUND)/Config.ROUND);
			jsonProperties.put(Config.WATER, water);
			jsonProperties.put(Config.AREA, area);
			jsonProperties.put(Config.NUMBER_PER_ROOM, number_per_room);
			jsonProperties.put(Config.MIN_STAY, min_stay);
			jsonProperties.put(Config.AVAILABLE_FROM, txt_available_from_content.getText().toString());
			jsonProperties.put(Config.ROOM_TYPE, txt_room_type_content.getText().toString());
			jsonProperties.put(Config.ROOM_STATE, txt_room_state_content.getText().toString());
			jObj.put(Config.ROOM_PROPERTIES, jsonProperties);
			jObj.put(Config.UPDATE_TYPE, Config.UPDATE_ROOM_PROPERTIES);
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			JSONObject jObj = new JSONObject(contentMessage);
			room_properties = new Room_Properties();
			room_properties.setRent_per_month((float) jObj.optDouble(Config.RENT_PER_MONTH));
			room_properties.setElectric((float) jObj.optDouble(Config.ELECTRIC));
			room_properties.setWater(jObj.optInt(Config.WATER));
			room_properties.setArea(jObj.optInt(Config.AREA));
			room_properties.setNumber_per_room(jObj.optInt(Config.NUMBER_PER_ROOM));
			room_properties.setMin_stay(jObj.optInt(Config.MIN_STAY));
			room_properties.setAvailable_from(jObj.optString(Config.AVAILABLE_FROM));
			room_properties.setRoom_type(jObj.optString(Config.ROOM_TYPE));
			room_properties.setRoom_state(jObj.optString(Config.ROOM_STATE));
			room.setRoom_properties(room_properties);
			myRooms.getList_room().set(myRooms.getPosition(), room);
			btn_save.setVisibility(View.GONE);
			if(isAutoIntentBack){
				intentBackEdit();
			} else {
				cloneRoomProperties(room_properties);
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intentBack(){
		room_properties = new Room_Properties(rent_per_month, electric, water, area, number_per_room, min_stay, txt_available_from_content.getText().toString(), txt_room_type_content.getText().toString(), txt_room_state_content.getText().toString());
		room.setRoom_properties(room_properties);
		Intent intent_back = new Intent(PostRoom_GetProperties.this, PostRoom_GetAddress.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_ROOM, room);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_PROPERTIES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
	}

	private void intentBackEdit(){
		Intent intent_back = new Intent(PostRoom_GetProperties.this, MyRoomDetailActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_PROPERTIES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
