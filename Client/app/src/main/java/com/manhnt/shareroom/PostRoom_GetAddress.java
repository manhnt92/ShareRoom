package com.manhnt.shareroom;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.database.ShareRoomDatabase;
import com.manhnt.object.Account;
import com.manhnt.object.District;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Province;
import com.manhnt.object.Room;
import com.manhnt.object.Room_Address;
import com.manhnt.object.Ward;
import com.manhnt.adapter.SpinnerAdapter;
import com.rengwuxian.materialedittext.MaterialEditText;
import fr.ganfra.materialspinner.MaterialSpinner;

public class PostRoom_GetAddress extends FragmentActivity implements OnClickListener, OnItemSelectedListener,
	LocationListener, OnMapLongClickListener, OnMyLocationButtonClickListener, OnMarkerDragListener,
	GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
	com.google.android.gms.location.LocationListener {

	private Province curProvince;
	private int position_Province_ID, position_District_ID, position_Ward_ID;
	private Account mAccount;
	private Room room = new Room();
	private ImageButton btn_save;
	private ShareRoomDatabase db;
	private MaterialEditText edt_address;
	private MaterialSpinner spinner_district, spinner_ward;
	private SpinnerAdapter ward_adapter;
	private ArrayList<District> list_district;
	private ArrayList<Ward> list_ward;
	private ArrayList<String> list_district_name, list_ward_name;
	private District curDistrict;
	private Ward curWard;
	private GoogleMap googleMap;
	private IconGenerator iconFactory;
	private Marker marker;
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private LatLng mCurrentLocation;
	private static final int ZOOM_LEVEL_1 = 12;
	private static final int ZOOM_LEVEL_2 = 13;
	private static final int ZOOM_LEVEL_3 = 14;
	private MyRooms myRooms;
	private boolean isEdit;
	private boolean isTouch = false;
	private Room_Address room_address_rollback;
	private Typeface font;
	private JSONObject json_Update_RoomAddress;
	private boolean isAutoIntentBack = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.post_room_get_address);
		getExtraBundle();
		getListData();
		getWidget();
		if (isGooglePlayServicesAvailable()) {
			initCurrentLocation();
		}
		if (room.getRoom_address() == null) {
			new RequestGetLatLngFromAddress(ZOOM_LEVEL_1, false).execute(curProvince.getName());
		} else {
			spinner_district.setSelection(position_District_ID);
			curDistrict = list_district.get(position_District_ID);
			list_ward.clear();
			list_ward_name.clear();
			list_ward = db.getListWard(curDistrict.getDistrict_id());
			for (int i = 0; i < list_ward.size(); i++) {
				list_ward_name.add(list_ward.get(i).getName());
			}
			ward_adapter.notifyDataSetChanged();
			spinner_ward.setSelection(position_Ward_ID);
			curWard = list_ward.get(position_Ward_ID);
			edt_address.setText(room.getRoom_address().getAddress());

			String title = getString(R.string.room_address_title);
			double lat = room.getRoom_address().getLatitude();
			double lon = room.getRoom_address().getLongitude();
			MarkerOptions markerOptions = new MarkerOptions()
				.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title, font)))
				.draggable(true).visible(true).position(new LatLng(lat, lon))
				.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
			marker = googleMap.addMarker(markerOptions);
			CameraPosition position = new CameraPosition.Builder().target(new LatLng(lat, lon))
				.zoom(ZOOM_LEVEL_3).build();
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
		}
	}

	private void cloneRoomAddress(Room_Address room_address) {
		try {
			room_address_rollback = (Room_Address) room_address.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		isTouch = true;
		return super.dispatchTouchEvent(ev);
	}

	private void getListData() {
		list_district_name = new ArrayList<>();
		list_ward_name = new ArrayList<>();
		db = ShareRoomDatabase.getInstance(this);
		if (isEdit) {
			Room_Address room_address = room.getRoom_address();
			cloneRoomAddress(room_address);
			String districtId = room_address.getDistrict().getDistrict_id();
			String wardId = room_address.getWard().getWard_id();
			list_district = db.getListDistrict(curProvince.getProvince_id());
			for (int i = 0; i < list_district.size(); i++) {
				list_district_name.add(list_district.get(i).getName());
			}
			if (!TextUtils.isEmpty(districtId)) {
				for (int i = 0; i < list_district.size(); i++) {
					if (list_district.get(i).getDistrict_id().equalsIgnoreCase(districtId)) {
						position_District_ID = i;
						break;
					}
				}
				list_ward = db.getListWard(String.valueOf(districtId));
				for (int i = 0; i < list_ward.size(); i++) {
					list_ward_name.add(list_ward.get(i).getName());
				}
				if (!TextUtils.isEmpty(wardId)) {
					for (int i = 0; i < list_ward.size(); i++) {
						if (list_ward.get(i).getWard_id().equalsIgnoreCase(wardId)) {
							position_Ward_ID = i;
							break;
						}
					}
				} else {
					position_Ward_ID = 0;
				}
			} else {
				position_District_ID = 0;
				position_Ward_ID = 0;
				list_ward = db.getListWard(String.valueOf(-1));
				for (int i = 0; i < list_ward.size(); i++) {
					list_ward_name.add(list_ward.get(i).getName());
				}
			}
		} else {
			list_district = db.getListDistrict(curProvince.getProvince_id());
			for (int i = 0; i < list_district.size(); i++) {
				list_district_name.add(list_district.get(i).getName());
			}
			curDistrict = list_district.get(0);
			list_ward = db.getListWard(String.valueOf(-1));
			for (int i = 0; i < list_ward.size(); i++) {
				list_ward_name.add(list_ward.get(i).getName());
			}
			curWard = list_ward.get(0);
		}
	}

	private void getWidget() {
		font = Config.getTypeface(getAssets());
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		ImageButton btn_next = manager.ImageButton(R.id.btn_next, this, true);
		btn_save = manager.ImageButton(R.id.btn_save, this, true);
		btn_save.setVisibility(View.GONE);
		if (isEdit) {
			btn_next.setVisibility(View.GONE);
		} else {
			btn_next.setVisibility(View.VISIBLE);
		}
		edt_address = manager.MaterialEditText(R.id.edt_address, true);
		edt_address.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (isEdit && isTouch) {
					btn_save.setVisibility(View.VISIBLE);
				}
			}
		});
		manager.ButtonRectangle(R.id.btn_search, this, true);
		SpinnerAdapter district_adapter = new SpinnerAdapter(this, 0, list_district_name);
		ward_adapter = new SpinnerAdapter(this, 0, list_ward_name);
		spinner_district = manager.MaterialSpinner(R.id.spinner_district, true);
		spinner_district.setAdapter(district_adapter);
		spinner_district.setOnItemSelectedListener(this);
		spinner_ward = manager.MaterialSpinner(R.id.spinner_ward, true);
		spinner_ward.setAdapter(ward_adapter);
		spinner_ward.setOnItemSelectedListener(this);
		googleMap = manager.GoogleMap(R.id.google_map);
		googleMap.setOnMarkerDragListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMyLocationButtonClickListener(this);
		iconFactory = new IconGenerator(PostRoom_GetAddress.this);
		iconFactory.setStyle(IconGenerator.STYLE_GREEN);
	}

	private void getExtraBundle() {
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		switch (from) {
			case Config.MAIN_ACTIVITY:
				curProvince = (Province) getIntent().getExtras().getSerializable(Config.BUNDLE_PROVINCE);
				position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				isEdit = false;
				break;
			case Config.POST_ROOM_GET_PROPERTIES:
				room = (Room) getIntent().getExtras().getSerializable(Config.BUNDLE_ROOM);
				if (room != null) {
					curProvince = room.getRoom_address().getProvince();
				}
				position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
				position_District_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_DISTRICT_ID);
				position_Ward_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_WARD_ID);
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				isEdit = false;
				break;
			case Config.MY_ROOMS_ACTIVITY:
				curProvince = (Province) getIntent().getExtras().getSerializable(Config.BUNDLE_PROVINCE);
				position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				isEdit = false;
				break;
			case Config.ROOM_DETAIL_ACTIVITY:
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
				if (myRooms != null) {
					room = myRooms.getList_room().get(myRooms.getPosition());
					if (room != null) {
						curProvince = room.getRoom_address().getProvince();
					}
				}
				isEdit = true;
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (isEdit) {
			checkBtnSave();
		} else {
			intentBack();
		}
	}

	private void checkBtnSave() {
		if (btn_save.getVisibility() == View.VISIBLE) {
			DialogManager.getInstance().YesNoDialog(this, R.string.room_address_title,
				R.string.question_update, R.string.OK, R.string.NO, saveListener, true).show();
		} else {
			intentBackEdit();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_search:
				String address = edt_address.getText().toString();
				if (!TextUtils.isEmpty(address)) {
					new RequestGetLatLngFromAddress(ZOOM_LEVEL_3, true).execute(address);
				}
				break;
			case R.id.btn_back:
				if (isEdit) {
					checkBtnSave();
				} else {
					intentBack();
				}
				break;
			case R.id.btn_next:
				if (!TextUtils.isEmpty(edt_address.getText().toString()) && marker != null) {
					double latitude = marker.getPosition().latitude;
					double longitude = marker.getPosition().longitude;
					Room_Address room_address = new Room_Address(curProvince, curDistrict, curWard,
						edt_address.getText().toString(), latitude, longitude);
					Intent intent_next = new Intent(PostRoom_GetAddress.this, PostRoom_GetProperties.class);
					room.setRoom_address(room_address);
					intent_next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					intent_next.putExtra(Config.BUNDLE_ROOM, room);
					intent_next.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
					intent_next.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
					intent_next.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
					intent_next.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
					intent_next.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_ADDRESS);
					startActivity(intent_next);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				} else {
					String message = "";
					if (TextUtils.isEmpty(edt_address.getText().toString()) && marker == null) {
						message = getString(R.string.missing_address_and_marker);
					} else if (TextUtils.isEmpty(edt_address.getText().toString()) && marker != null) {
						message = getString(R.string.missing_address);
					} else if (!TextUtils.isEmpty(edt_address.getText().toString()) && marker == null) {
						message = getString(R.string.missing_marker);
					}
					Config.showCustomToast(PostRoom_GetAddress.this, R.mipmap.ic_toast_error, message);
				}
				break;
			case R.id.btn_save:
				try {
					json_Update_RoomAddress = new JSONObject();
					JSONObject jsonAddress = new JSONObject();
					jsonAddress.put(Config.PROVINCE_ID, curProvince.getProvince_id());
					jsonAddress.put(Config.DISTRICT_ID, curDistrict.getDistrict_id());
					jsonAddress.put(Config.WARD_ID, curWard.getWard_id());
					jsonAddress.put(Config.ADDRESS, edt_address.getText().toString());
					jsonAddress.put(Config.LATITUDE, marker.getPosition().latitude);
					jsonAddress.put(Config.LONGITUDE, marker.getPosition().longitude);
					json_Update_RoomAddress.put(Config.ROOM_ADDRESS, jsonAddress);
					json_Update_RoomAddress.put(Config.UPDATE_TYPE, Config.UPDATE_ROOM_ADDRESS);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				RequestAPI.getInstance().context(PostRoom_GetAddress.this).method(RequestAPI.PUT)
					.message(getString(R.string.waiting)).isParams(true).isAuthorization(true)
					.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
					.isShowDialog(true).isShowToast(true).execute(updateRoomAddressListener);
				break;
			default:
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (isTouch) {
			switch (parent.getId()) {
				case R.id.spinner_district:
					curDistrict = list_district.get(position);
					position_District_ID = position;
					list_ward_name.clear();
					list_ward = db.getListWard(curDistrict.getDistrict_id());
					for (int i = 0; i < list_ward.size(); i++) {
						list_ward_name.add(list_ward.get(i).getName());
					}
					spinner_ward.setSelection(0);
					curWard = list_ward.get(0);
					ward_adapter.notifyDataSetChanged();
					if (position != 0) {
						if (!curDistrict.getLocation().equalsIgnoreCase("0")) {
							moveCamera(curDistrict.getLocation(), ZOOM_LEVEL_2);
						} else {
							String address_name = curDistrict.getName() + ", " + curProvince.getName();
							new RequestGetLatLngFromAddress(ZOOM_LEVEL_2, false).execute(address_name);
						}
					}
					break;
				case R.id.spinner_ward:
					curWard = list_ward.get(position);
					position_Ward_ID = position;
					if (position != 0) {
						if (!curWard.getLocation().equalsIgnoreCase("0")) {
							moveCamera(curWard.getLocation(), ZOOM_LEVEL_3);
						} else {
							String address_name = curWard.getName() + ", " + curDistrict.getName() + ", "
								+ curProvince.getName();
							new RequestGetLatLngFromAddress(ZOOM_LEVEL_3, false).execute(address_name);
						}
					}
					break;
				default:
					break;
			}
		}
		if (isEdit && isTouch) {
			btn_save.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

	@Override
	public void onMarkerDrag(Marker arg0) {}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		new RequestGetAddressFromLatLng().execute(arg0.getPosition());
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		Config.showCustomToast(this, 0, getString(R.string.move_market));
	}

	@Override
	public boolean onMyLocationButtonClick() {
		if (mCurrentLocation != null) {
			CameraPosition position = new CameraPosition.Builder()
				.target(new LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude))
				.zoom(ZOOM_LEVEL_1).build();
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
			new RequestGetAddressFromLatLng().execute(mCurrentLocation);
			if (marker != null) {
				marker.remove();
			}
			String title = getString(R.string.room_address_title);
			MarkerOptions markerOptions = new MarkerOptions()
				.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title, font)))
				.draggable(true).visible(true).position(mCurrentLocation)
				.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
			marker = googleMap.addMarker(markerOptions);
		} else {
			String message = getString(R.string.cannot_getCurLocation);
			Config.showCustomToast(PostRoom_GetAddress.this, 0, message);
		}
		return true;
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		if (marker != null) {
			marker.remove();
		}
		String title = getString(R.string.room_address_title);
		MarkerOptions markerOptions = new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title, font)))
			.draggable(true).visible(true).position(arg0)
			.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
		marker = googleMap.addMarker(markerOptions);
		new RequestGetAddressFromLatLng().execute(arg0);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		mCurrentLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	@SuppressWarnings("deprecation")
	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	private void initCurrentLocation() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(1000 * 10);
		mLocationRequest.setFastestInterval(1000 * 5);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
			mGoogleApiClient.connect();
		}
	}

	private void moveCamera(String location, int zoom) {
		double lat = Config.convertDMSToLat(location.split(", ")[0]);
		double lng = Config.convertDMSToLng(location.split(", ")[1]);
		CameraPosition position = new CameraPosition.Builder()
			.target(new LatLng(lat, lng)).zoom(zoom).build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
	}

	private class RequestGetLatLngFromAddress extends AsyncTask<String, Void, LatLng> {

		private MaterialDialog mDialog;
		private int zoom;
		private boolean isAddMarker;

		public RequestGetLatLngFromAddress(int zoom, boolean isAddMarker) {
			this.zoom = zoom;
			this.isAddMarker = isAddMarker;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(PostRoom_GetAddress.this, getString(R.string.waiting));
			mDialog.show();
		}

		@Override
		protected LatLng doInBackground(String... params) {
			try {
				String address = URLEncoder.encode(params[0], "UTF-8");
				String url = "http://maps.google.com/maps/api/geocode/json?address=" + address
					+ "&sensor=false";
				URL mUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.connect();
				InputStream inputStream = new BufferedInputStream(conn.getInputStream());
				String rs = Config.convertInputStreamToString(inputStream);
				JSONObject jObj = new JSONObject(rs);
				JSONArray jArray = jObj.optJSONArray("results");
				double lng = jArray.getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location").getDouble("lng");
				double lat = jArray.getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location").getDouble("lat");
				return new LatLng(lat, lng);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(LatLng result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			if (result != null) {
				CameraPosition position = new CameraPosition.Builder()
					.target(new LatLng(result.latitude, result.longitude)).zoom(zoom).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
				if (isAddMarker) {
					if (marker != null) {
						marker.remove();
					}
					String title = getString(R.string.room_address_title);
					MarkerOptions markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title, font)))
						.draggable(true).visible(true).position(result)
						.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
					marker = googleMap.addMarker(markerOptions);
					if (isEdit && isTouch) {
						btn_save.setVisibility(View.VISIBLE);
					}
				}
			}
		}

	}

	private class RequestGetAddressFromLatLng extends AsyncTask<LatLng, Void, String> {

		private MaterialDialog mDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(PostRoom_GetAddress.this, getString(R.string.waiting));
			mDialog.show();
		}

		@Override
		protected String doInBackground(LatLng... params) {
			try {
				String url = "http://maps.google.com/maps/api/geocode/json?latlng=" + params[0].latitude + ","
					+ params[0].longitude + "&sensor=false";
				url = url.replace(" ", "%20");
				URL mUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.connect();
				InputStream inputStream = new BufferedInputStream(conn.getInputStream());
				String rs = Config.convertInputStreamToString(inputStream);
				JSONObject jObj = new JSONObject(rs);
				JSONArray jArray = jObj.optJSONArray("results");
				String address = jArray.getJSONObject(0).getString("formatted_address");
				address = address.replace(", " + address.split(", ")[address.split(", ").length - 1], "");
				return address;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			edt_address.setText(result);
			if (isEdit && isTouch) {
				btn_save.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public void onConnected(@Nullable Bundle arg0) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
			PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
			Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult arg0) {}

	private DialogManager.YesNoDialogListener saveListener =  new DialogManager.YesNoDialogListener() {
		@Override
		public void onYes(MaterialDialog dialog) {
			isAutoIntentBack = true;
			try {
				json_Update_RoomAddress = new JSONObject();
				JSONObject jsonAddress = new JSONObject();
				jsonAddress.put(Config.PROVINCE_ID, curProvince.getProvince_id());
				jsonAddress.put(Config.DISTRICT_ID, curDistrict.getDistrict_id());
				jsonAddress.put(Config.WARD_ID, curWard.getWard_id());
				jsonAddress.put(Config.ADDRESS, edt_address.getText().toString());
				jsonAddress.put(Config.LATITUDE, marker.getPosition().latitude);
				jsonAddress.put(Config.LONGITUDE, marker.getPosition().longitude);
				json_Update_RoomAddress.put(Config.ROOM_ADDRESS, jsonAddress);
				json_Update_RoomAddress.put(Config.UPDATE_TYPE, Config.UPDATE_ROOM_ADDRESS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			RequestAPI.getInstance().context(PostRoom_GetAddress.this).method(RequestAPI.PUT)
				.message(getString(R.string.waiting)).isParams(true).isAuthorization(true)
				.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.isShowDialog(true).isShowToast(true).execute(updateRoomAddressListener);
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			myRooms.getList_room().get(myRooms.getPosition()).setRoom_address(room_address_rollback);
			intentBackEdit();
			dialog.dismiss();
		}

	};

	private RequestAPI.RequestAPIListener updateRoomAddressListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return json_Update_RoomAddress;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			JSONObject jsonAddress = new JSONObject(contentMessage);
			Room_Address room_address = new Room_Address();
			room_address.setAddress(jsonAddress.optString(Config.ADDRESS));
			room_address.setLatitude(jsonAddress.optDouble(Config.LATITUDE));
			room_address.setLongitude(jsonAddress.optDouble(Config.LONGITUDE));
			room_address.setProvince(new Province(jsonAddress.optString(Config.PROVINCE_ID)));
			room_address.setDistrict(new District(jsonAddress.optString(Config.DISTRICT_ID)));
			room_address.setWard(new Ward(jsonAddress.optString(Config.WARD_ID)));
			room.setRoom_address(room_address);
			myRooms.getList_room().set(myRooms.getPosition(), room);
			btn_save.setVisibility(View.GONE);
			if(isAutoIntentBack){
				intentBackEdit();
			} else {
				cloneRoomAddress(room_address);
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intentBack(){
		Intent intent_back = new Intent(PostRoom_GetAddress.this, MainActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_ADDRESS);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void intentBackEdit(){
		Intent intent_back = new Intent(PostRoom_GetAddress.this, MyRoomDetailActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_ADDRESS);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
