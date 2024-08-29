package com.manhnt.shareroom;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appyvet.rangebar.RangeBar;
import com.appyvet.rangebar.RangeBar.OnRangeBarChangeListener;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.Filter;
import com.manhnt.object.Room;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchRoomActivity extends FragmentActivity implements OnClickListener, OnMapLongClickListener,
	OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
	LocationListener, OnCameraChangeListener {

	private Account mAccount;
	private LatLngBounds latLngBounds;
	private float zoom;
	private boolean isMoveCameraToCurrentLocation;
	private ButtonRectangle btn_search_here;
	private GoogleMap googleMap;
	private IconGenerator iconFactory;
	ArrayList<String> list_university_name = new ArrayList<>();
	ArrayList<String> list_university_place_id = new ArrayList<>();
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private LatLng mCurrentLocation;
	private static final int ZOOM_LEVEL_1 = 12;
	private static final int ZOOM_LEVEL_2 = 14;
	private float rent_min, rent_max, electric_min, electric_max;
	private int water_min, water_max, area_min, area_max, person_min, person_max;
	private Marker marker_direction;
	private enum SearchState {
		SEARCH_FULL_TEXT, SEARCH_RADIUS, SEARCH_NEAR_UNIVERSITY
	}
	private SearchState searchState;
	private Polyline polylineDirection;
	private PolylineOptions direction_PolylineOptions;
	private Marker marker;
	private float radius;
	private WidgetManager manager;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_room_activity);
		getExtraBundle();
		getSharedPreference();
		getWidget();
		if (isGooglePlayServicesAvailable()) {
			initCurrentLocation();
		}
		if (!isMoveCameraToCurrentLocation) {
			LatLng pos = latLngBounds.getCenter();
			CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(zoom).build();
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			Filter filter = new Filter(rent_min, rent_max, electric_min, electric_max, water_min, water_max, area_min, area_max, person_min, person_max);
			Config.FilterResult(SearchRoomActivity.this, filter, googleMap, iconFactory);
//			if (marker != null) {
//				googleMap.addCircle(new CircleOptions().center(marker.getPosition()).radius(radius * 1000)
//						.strokeColor(getResources().getColor(R.color.color_opacity)));
//			}
//			if (polylineDirection != null && direction_PolylineOptions != null) {
//				polylineDirection = googleMap.addPolyline(direction_PolylineOptions);
//			}
		}
	}

	private void getExtraBundle() {
		if (getIntent().getExtras() != null) {
			int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
			if(from == Config.MAIN_ACTIVITY){
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				isMoveCameraToCurrentLocation = true;
			} else {
				mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
				double ne_lat = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LAT);
				double ne_lng = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LNG);
				double sw_lat = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LAT);
				double sw_lng = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LNG);
				zoom = getIntent().getExtras().getFloat(Config.BUNDLE_ZOOM);
				latLngBounds = new LatLngBounds(new LatLng(sw_lat, sw_lng), new LatLng(ne_lat, ne_lng));
				isMoveCameraToCurrentLocation = false;
			}
		}
	}

	private void getSharedPreference() {
		Filter filter = PreferencesManager.getInstance().getFilterResult(this);
		rent_min = filter.getRent_min();
		rent_max = filter.getRent_max();
		electric_min = filter.getElectric_min();
		electric_max = filter.getElectric_max();
		water_min = filter.getWater_min();
		water_max = filter.getWater_max();
		area_min = filter.getArea_min();
		area_max = filter.getArea_max();
		person_min = filter.getPerson_min();
		person_max = filter.getPerson_max();
	}

	@SuppressLint("CommitPrefEdits")
	private void setSharedPreference() {
		PreferencesManager.getInstance().setFilterResult(this, rent_min, rent_max, electric_min, electric_max,
			water_min, water_max, area_min, area_max, person_min, person_max);
	}

	@SuppressWarnings("deprecation")
	private void getWidget() {
		manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		manager.ImageButton(R.id.btn_view_as_list, this, true);
		manager.ImageButton(R.id.btn_search_full_text, this, true);
		manager.ImageButton(R.id.btn_search_radius, this, true);
		manager.ImageButton(R.id.btn_search_near_university, this, true);
		manager.ImageButton(R.id.btn_direction, this, true);
		manager.ImageButton(R.id.btn_filter_result, this, true);
		btn_search_here = manager.ButtonRectangle(R.id.btn_search_here, this, true);
		if (googleMap == null) {
			googleMap = manager.GoogleMap(R.id.google_map);
			googleMap.setOnMapLongClickListener(this);
			googleMap.setOnMyLocationButtonClickListener(this);
			googleMap.setOnCameraChangeListener(this);
			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					if(marker.getTitle() == null){
						return null;
					}
					@SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.info_window, null);
					TextView txt_address = (TextView) view.findViewById(R.id.txt_address);
					txt_address.setTypeface(Config.getTypeface(getAssets()));
					txt_address.setText(marker.getTitle());
					return view;
				}

				@Override
				public View getInfoContents(Marker marker) {return null;}
			});
			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					latLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
					zoom = googleMap.getCameraPosition().zoom;
					intent(SearchRoomsViewDetail.class, Integer.parseInt(marker.getSnippet()));
				}
			});
			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker arg0) {
					marker_direction = arg0;
					return false;
				}
			});
			iconFactory = new IconGenerator(SearchRoomActivity.this);
			iconFactory.setStyle(IconGenerator.STYLE_GREEN);
		}

		String[] arr_university_ha_noi = getResources().getStringArray(R.array.arr_university_ha_noi);
		for (int i = 0; i < arr_university_ha_noi.length; i = i + 2) {
			list_university_name.add(arr_university_ha_noi[i]);
			list_university_place_id.add(arr_university_ha_noi[i + 1]);
		}
	}

	@Override
	public void onBackPressed() {
		intentBack();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				intentBack();
				break;
			case R.id.btn_view_as_list:
				latLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
				zoom = googleMap.getCameraPosition().zoom;
				intent(SearchRoomsViewAsListActivity.class, -1);
				break;
			case R.id.btn_search_here:
				searchRectangle();
				break;
			case R.id.btn_search_full_text:
				if (searchState != SearchState.SEARCH_FULL_TEXT) {
					clearMap();
				}
				searchState = SearchState.SEARCH_FULL_TEXT;
				DialogManager.getInstance().InputDialog(this, R.string.search_room_title, InputType.TYPE_CLASS_TEXT,
				getString(R.string.hint_search_full_text), "", true, fullTextListener).show();
				break;
			case R.id.btn_search_radius:
				if (searchState != SearchState.SEARCH_RADIUS) {
					clearMap();
				}
				searchState = SearchState.SEARCH_RADIUS;
				Config.showCustomToast(SearchRoomActivity.this, 0, getString(R.string.txt_make_marker));
				break;
			case R.id.btn_search_near_university:
				if (searchState != SearchState.SEARCH_NEAR_UNIVERSITY) {
					clearMap();
				}
				searchState = SearchState.SEARCH_NEAR_UNIVERSITY;
				String[] arr = new String[list_university_name.size()];
				arr = list_university_name.toArray(arr);
				DialogManager.getInstance().ListOneChoiceDialog(this, R.string.search_room_title,
					arr, -1, true, true, universityListener).show();
				break;
			case R.id.btn_direction:
				if (marker_direction != null && mCurrentLocation != null) {
					new RequestGoogleDirection(SearchRoomActivity.this, googleMap, mCurrentLocation,
						marker_direction.getPosition()).execute();
				} else {
					String message = (mCurrentLocation == null) ? getString(R.string.cannot_getCurLocation) :
						getString(R.string.txt_err_direction);
					Config.showCustomToast(SearchRoomActivity.this, 0, message);
				}
				break;
			case R.id.btn_filter_result:
				showFilterDialog();
				break;
			default:
				break;
		}
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		if (searchState == SearchState.SEARCH_RADIUS) {
			MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconFactory
				.makeIcon(getString(R.string.search_position), Config.getTypeface(getAssets()))))
				.position(arg0).anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
			marker = googleMap.addMarker(markerOptions);
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					handler.removeCallbacksAndMessages(null);
					DialogManager.getInstance().InputDialog(SearchRoomActivity.this, R.string.search_room_title,
					InputType.TYPE_CLASS_NUMBER, getString(R.string.txt_hint_radius), "", true, radiusListener).show();
				}
			}, 500);
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		return false;
	}

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
				.addConnectionCallbacks(this).addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).addApi(Places.GEO_DATA_API).build();
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult arg0) {

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
	public void onConnectionSuspended(int arg0) {
		
	}

	@Override
	public void onLocationChanged(Location arg0) {
		if(mCurrentLocation == null){
			mCurrentLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
			if(isMoveCameraToCurrentLocation){
				CameraPosition position = new CameraPosition.Builder().target(new LatLng(arg0.getLatitude(),
					arg0.getLongitude())).zoom(ZOOM_LEVEL_1).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
			}
		}
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
		if (arg0.zoom > ZOOM_LEVEL_2) {
			btn_search_here.setVisibility(View.VISIBLE);
		} else {
			btn_search_here.setVisibility(View.GONE);
		}
	}
	
	private void clearMap() {
		googleMap.clear();
		Config.LIST_ROOM_BEFORE.clear();
		Config.LIST_ROOM_AFTER.clear();
		if(marker_direction != null){
			marker_direction = null;
		}
		polylineDirection = null;
		direction_PolylineOptions = null;
		marker = null;
	}
	
	@SuppressLint("SetTextI18n")
	private void showFilterDialog() {
		DialogManager.getInstance().CustomViewDialog(this, R.string.filter_result, R.layout.filter_dialog,
			true, new DialogManager.CustomViewListener() {
				@Override
				public void onAttachCustomView(View view) {
					initCustomDialogView(view, R.id.txt_rent, R.id.txt_dot_1, R.id.txt_rent_min,
						R.id.txt_rent_max, R.id.slider_rent, 1);
					initCustomDialogView(view, R.id.txt_electric, R.id.txt_dot_2, R.id.txt_electric_min,
						R.id.txt_electric_max, R.id.slider_electric, 2);
					initCustomDialogView(view, R.id.txt_water, R.id.txt_dot_3, R.id.txt_water_min,
						R.id.txt_water_max, R.id.slider_water, 3);
					initCustomDialogView(view, R.id.txt_area, R.id.txt_dot_4, R.id.txt_area_min,
						R.id.txt_area_max, R.id.slider_area, 4);
					initCustomDialogView(view, R.id.txt_person, R.id.txt_dot_5, R.id.txt_person_min,
						R.id.txt_person_max, R.id.slider_person, 5);
				}

				@SuppressLint("NewApi")
				@Override
				public void onOK(MaterialDialog dialog) {
					setSharedPreference();
						if(Config.LIST_ROOM_BEFORE.size() > 0){
							googleMap.clear();
							Filter filter = new Filter(rent_min, rent_max, electric_min, electric_max, water_min,
								water_max, area_min, area_max, person_min, person_max);
							Config.FilterResult(SearchRoomActivity.this, filter, googleMap, iconFactory);
							if(marker != null && searchState == SearchState.SEARCH_RADIUS){
								googleMap.addCircle(new CircleOptions().center(marker.getPosition())
									.radius(radius * 1000).strokeColor(getColor(R.color.color_opacity)));
							}
						}
					dialog.dismiss();
					}

				@Override
				public void onCancel(MaterialDialog dialog) {
					dialog.dismiss();
				}
			}).show();
	}

	@SuppressLint("SetTextI18n")
	private void initCustomDialogView(View view, int resID, int resDotID, int resID_min,int resID_max,int sliderID, int type){
		manager.TextView(view, resID, true);
		manager.TextView(view, resDotID, true);
		final TextView txt_min = manager.TextView(view, resID_min, true);
		final TextView txt_max = manager.TextView(view, resID_max, true);
		RangeBar slider = manager.RangeBar(view, sliderID, true);
		if(type == 1) {
			txt_min.setText(rent_min + " " + getString(R.string.million));
			txt_max.setText(rent_max + " " + getString(R.string.million));
			slider.setmLeftIndex((int)(rent_min * 10));
			slider.setmRightIndex((int)(rent_max * 10));
			slider.setOnRangeBarChangeListener(new OnRangeBarChangeListener() {
				@Override
				public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
					String leftPinValue, String rightPinValue) {
					rent_min = Float.parseFloat(leftPinValue)/10;
					rent_max = Float.parseFloat(rightPinValue)/10;
					txt_min.setText(rent_min + " " + getString(R.string.million));
					txt_max.setText(rent_max + " " + getString(R.string.million));
				}
			});
		} else if (type == 2){
			slider.setmLeftIndex((int)(electric_min * 10));
			slider.setmRightIndex((int)(electric_max * 10));
			txt_min.setText(electric_min + "00");
			txt_max.setText(electric_max + "00");
			slider.setOnRangeBarChangeListener(new OnRangeBarChangeListener() {
				@Override
				public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
					String leftPinValue, String rightPinValue) {
					electric_min = Float.parseFloat(leftPinValue)/10;
					electric_max = Float.parseFloat(rightPinValue)/10;
					txt_min.setText(electric_min + "00");
					txt_max.setText(electric_max + "00");
				}
			});
		} else if (type == 3){
			slider.setmLeftIndex(water_min);
			slider.setmRightIndex(water_max);
			txt_min.setText(water_min + ".000");
			txt_max.setText(water_max + ".000");
			slider.setOnRangeBarChangeListener(new OnRangeBarChangeListener() {
				@Override
				public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
					String leftPinValue, String rightPinValue) {
					water_min = Integer.parseInt(leftPinValue);
					water_max = Integer.parseInt(rightPinValue);
					txt_min.setText(water_min + ".000");
					txt_max.setText(water_max + ".000");
				}
			});
		} else if (type == 4){
			slider.setmLeftIndex(area_min);
			slider.setmRightIndex(area_max);
			txt_min.setText(area_min + " m\u00B2");
			txt_max.setText(area_max + " m\u00B2");
			slider.setOnRangeBarChangeListener(new OnRangeBarChangeListener() {
				@Override
				public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
					String leftPinValue, String rightPinValue) {
					area_min = Integer.parseInt(leftPinValue);
					area_max = Integer.parseInt(rightPinValue);
					txt_min.setText(area_min + " m\u00B2");
					txt_max.setText(area_max + " m\u00B2");
				}
			});
		} else if (type == 5){
			slider.setmLeftIndex(person_min * 10);
			slider.setmRightIndex(person_max * 10);
			txt_min.setText(person_min + " " + getString(R.string.person));
			txt_max.setText(person_max + " " + getString(R.string.person));
			slider.setOnRangeBarChangeListener(new OnRangeBarChangeListener() {
				@Override
				public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
					String leftPinValue, String rightPinValue) {
					person_min = Integer.parseInt(leftPinValue)/10;
					person_max = Integer.parseInt(rightPinValue)/10;
					txt_min.setText(person_min + " " + getString(R.string.person));
					txt_max.setText(person_max + " " + getString(R.string.person));
				}
			});
		}
	}

	private void searchRectangle(){
		LatLngBounds screen_search = googleMap.getProjection().getVisibleRegion().latLngBounds;
		double sw_lat = screen_search.southwest.latitude;
		double sw_lng = screen_search.southwest.longitude;
		double ne_lat = screen_search.northeast.latitude;
		double ne_lng = screen_search.northeast.longitude;
		RequestAPI.getInstance().context(this).method(RequestAPI.GET).isParams(false).isAuthorization(false)
			.isShowToast(false).message(getString(R.string.waiting)).url(Config.URL_SEARCH_ROOM
			+ "?sw_lat=" + sw_lat + "&sw_lng=" + sw_lng + "&ne_lat=" + ne_lat
			+ "&ne_lng=" + ne_lng + "&type=2").isShowDialog(true).execute(SearchRectangleListener);
	}

	private DialogManager.ListOneChoiceDialogListener universityListener = new DialogManager.ListOneChoiceDialogListener() {
		@Override
		public void onChoice(MaterialDialog dialog, int index) {
			if (index != -1) {
				String place_id = list_university_place_id.get(index);
				Places.GeoDataApi.getPlaceById(mGoogleApiClient, place_id)
					.setResultCallback(callback);
			}
		}
	};

	private DialogManager.InputDialogListener fullTextListener = new DialogManager.InputDialogListener() {
		@Override
		public void onInput(MaterialDialog dialog, CharSequence input) {
			if (!TextUtils.isEmpty(input)) {
				String q = "";
				try {
					q = URLEncoder.encode(input.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				RequestAPI.getInstance().context(SearchRoomActivity.this).isParams(false)
					.isAuthorization(false).isShowToast(false).url(Config.URL_SEARCH_ROOM + "?query="
					+ q + "&type=0").message(getString(R.string.waiting))
					.isShowDialog(true).method(RequestAPI.GET).execute(SearchFullTextListener);
			}
			dialog.dismiss();
		}
	};

	private DialogManager.InputDialogListener radiusListener = new DialogManager.InputDialogListener() {
		@Override
		public void onInput(MaterialDialog dialog, CharSequence input) {
			if (!TextUtils.isEmpty(input)) {
				radius = Float.parseFloat(input.toString());
				if (radius > 0 && radius < 100) {
					double latitude = marker.getPosition().latitude;
					double longitude = marker.getPosition().longitude;
					RequestAPI.getInstance().context(SearchRoomActivity.this).isParams(false)
						.isAuthorization(false).isShowToast(false).url(Config.URL_SEARCH_ROOM
						+ "?latitude=" + latitude + "&longitude="+ longitude + "&radius="
						+ radius + "&type=1")
						.message(getString(R.string.waiting)).method(RequestAPI.GET)
						.isShowDialog(true).execute(SearchRadiusListener);
					dialog.dismiss();
				} else {
					Config.showCustomToast(SearchRoomActivity.this, 0, getString(R.string.txt_invalid_radius));
				}
			}
		}
	};

	private ResultCallback<PlaceBuffer> callback = new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(@NonNull PlaceBuffer places) {
			if (places.getStatus().isSuccess() && places.getCount() > 0) {
				CameraPosition position = new CameraPosition.Builder()
						.target(places.get(0).getLatLng()).zoom(ZOOM_LEVEL_2).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000,
					new CancelableCallback() {
						@Override
						public void onFinish() {
							searchRectangle();
						}

						@Override
						public void onCancel() {}
					});
			}
			places.release();
		}
	};

	private RequestAPI.RequestAPIListener SearchRectangleListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return null;
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			googleMap.clear();
			JSONArray jArray = new JSONArray(contentMessage);
			if(jArray.length() > 0){
				Config.LIST_ROOM_BEFORE.clear();
				String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
				for( int  i = 0; i < jArray.length(); i++){
					Room room = Config.convertJsonToRoom(jArray.optJSONObject(i), array_amenities);
					Config.LIST_ROOM_BEFORE.add(room);
				}
				Filter filter = new Filter(rent_min, rent_max, electric_min, electric_max, water_min, water_max,
					area_min, area_max, person_min, person_max);
				Config.FilterResult(SearchRoomActivity.this, filter, googleMap, iconFactory);
				if(Config.LIST_ROOM_BEFORE.size() > 0){
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Room room : Config.LIST_ROOM_BEFORE) {
						builder.include(new LatLng(room.getRoom_address().getLatitude(),room.getRoom_address().getLongitude()));
					}
					LatLngBounds bounds = builder.build();
					googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
				}
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener SearchRadiusListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onResult(String contentMessage) throws JSONException {
			googleMap.clear();
			JSONArray jArray = new JSONArray(contentMessage);
			if(jArray.length() > 0){
				Config.LIST_ROOM_BEFORE.clear();
				String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
				for( int  i = 0; i < jArray.length(); i++){
					Room room = Config.convertJsonToRoom(jArray.optJSONObject(i), array_amenities);
					Config.LIST_ROOM_BEFORE.add(room);
				}
				Filter filter = new Filter(rent_min, rent_max, electric_min, electric_max, water_min,
					water_max, area_min, area_max, person_min, person_max);
				Config.FilterResult(SearchRoomActivity.this, filter, googleMap, iconFactory);
			}
			googleMap.addCircle(new CircleOptions().center(marker.getPosition()).radius(radius * 1000)
				.strokeColor(getResources().getColor(R.color.color_opacity)));
			LatLngBounds bounds = boundsWithCenterAndLatLngDistance(marker.getPosition(), radius * 1000, radius * 1000);
			googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener SearchFullTextListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return null;
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			googleMap.clear();
			JSONArray jArray = new JSONArray(contentMessage);
			if(jArray.length() > 0){
				Config.LIST_ROOM_BEFORE.clear();
				String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
				for( int  i = 0; i < jArray.length(); i++){
					Room room = Config.convertJsonToRoom(jArray.optJSONObject(i), array_amenities);
					Config.LIST_ROOM_BEFORE.add(room);
				}
				Filter filter = new Filter(rent_min, rent_max, electric_min, electric_max, water_min, water_max,
					area_min, area_max, person_min, person_max);
				Config.FilterResult(SearchRoomActivity.this, filter, googleMap, iconFactory);
				if(Config.LIST_ROOM_BEFORE.size() > 0){
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Room room : Config.LIST_ROOM_BEFORE) {
						builder.include(new LatLng(room.getRoom_address().getLatitude(),room.getRoom_address().getLongitude()));
					}
					LatLngBounds bounds = builder.build();
					googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
				}
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intent(Class<? extends Activity> clazz, int position_room_in_list_room_after){
		Intent i = new Intent(SearchRoomActivity.this, clazz);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		if(position_room_in_list_room_after > -1){
			i.putExtra(Config.BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER, position_room_in_list_room_after);
		}
		i.putExtra(Config.BUNDLE_NE_LAT, latLngBounds.northeast.latitude);
		i.putExtra(Config.BUNDLE_NE_LNG, latLngBounds.northeast.longitude);
		i.putExtra(Config.BUNDLE_SW_LAT, latLngBounds.southwest.latitude);
		i.putExtra(Config.BUNDLE_SW_LNG, latLngBounds.southwest.longitude);
		i.putExtra(Config.BUNDLE_ZOOM, zoom);
		i.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_ACTIVITY);
		startActivity(i);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

	}

	private void intentBack(){
		Intent intent_back = new Intent(SearchRoomActivity.this, MainActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_ACTIVITY);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private LatLngBounds boundsWithCenterAndLatLngDistance(LatLng center, float latDistanceInMeters, float lngDistanceInMeters) {
		LatLngBounds.Builder builder = LatLngBounds.builder();
		float[] distance = new float[1];
		double ASSUMED_INIT_LATLNG_DIFF = 1.0;
		float ACCURACY = 0.01f;
		{
			boolean foundMax = false;
			double foundMinLngDiff = 0;
			double assumedLngDiff = ASSUMED_INIT_LATLNG_DIFF;
			do {
				Location.distanceBetween(center.latitude, center.longitude, center.latitude, center.longitude + assumedLngDiff, distance);
				float distanceDiff = distance[0] - lngDistanceInMeters;
				if (distanceDiff < 0) {
					if (!foundMax) {
						foundMinLngDiff = assumedLngDiff;
						assumedLngDiff *= 2;
					} else {
						double tmp = assumedLngDiff;
						assumedLngDiff += (assumedLngDiff - foundMinLngDiff) / 2;
						foundMinLngDiff = tmp;
					}
				} else {
					assumedLngDiff -= (assumedLngDiff - foundMinLngDiff) / 2;
					foundMax = true;
				}
			} while (Math.abs(distance[0] - lngDistanceInMeters) > lngDistanceInMeters * ACCURACY);
			LatLng east = new LatLng(center.latitude, center.longitude + assumedLngDiff);
			builder.include(east);
			LatLng west = new LatLng(center.latitude, center.longitude - assumedLngDiff);
			builder.include(west);
		}
		{
			boolean foundMax = false;
			double foundMinLatDiff = 0;
			double assumedLatDiffNorth = ASSUMED_INIT_LATLNG_DIFF;
			do {
				Location.distanceBetween(center.latitude, center.longitude, center.latitude + assumedLatDiffNorth, center.longitude, distance);
				float distanceDiff = distance[0] - latDistanceInMeters;
				if (distanceDiff < 0) {
					if (!foundMax) {
						foundMinLatDiff = assumedLatDiffNorth;
						assumedLatDiffNorth *= 2;
					} else {
						double tmp = assumedLatDiffNorth;
						assumedLatDiffNorth += (assumedLatDiffNorth - foundMinLatDiff) / 2;
						foundMinLatDiff = tmp;
					}
				} else {
					assumedLatDiffNorth -= (assumedLatDiffNorth - foundMinLatDiff) / 2;
					foundMax = true;
				}
			} while (Math.abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY);
			LatLng north = new LatLng(center.latitude + assumedLatDiffNorth, center.longitude);
			builder.include(north);
		}
		{
			boolean foundMax = false;
			double foundMinLatDiff = 0;
			double assumedLatDiffSouth = ASSUMED_INIT_LATLNG_DIFF;
			do {
				Location.distanceBetween(center.latitude, center.longitude, center.latitude - assumedLatDiffSouth, center.longitude, distance);
				float distanceDiff = distance[0] - latDistanceInMeters;
				if (distanceDiff < 0) {
					if (!foundMax) {
						foundMinLatDiff = assumedLatDiffSouth;
						assumedLatDiffSouth *= 2;
					} else {
						double tmp = assumedLatDiffSouth;
						assumedLatDiffSouth += (assumedLatDiffSouth - foundMinLatDiff) / 2;
						foundMinLatDiff = tmp;
					}
				} else {
					assumedLatDiffSouth -= (assumedLatDiffSouth - foundMinLatDiff) / 2;
					foundMax = true;
				}
			} while (Math.abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY);
			LatLng south = new LatLng(center.latitude - assumedLatDiffSouth, center.longitude);
			builder.include(south);
		}
		return builder.build();
	}

	private class RequestGoogleDirection extends AsyncTask<Void, Void, String> {

		private Context mContext;
		private MaterialDialog mDialog;
		private GoogleMap googleMap;
		private LatLng origin, destination;

		public RequestGoogleDirection(Context context, GoogleMap googleMap, LatLng origin, LatLng destination){
			this.mContext = context;
			this.googleMap = googleMap;
			this.origin = origin;
			this.destination = destination;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(mContext, mContext.getResources().getString(R.string.waiting));
			mDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			String url = Config.GOOGLE_DIRECTION_URL + "origin=" + origin.latitude + "," + origin.longitude
					+ "&destination=" + destination.latitude + "," + destination.longitude + "&sensor=false";
			try{
				URL mUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				conn.connect();
				InputStream inputStream = new BufferedInputStream(conn.getInputStream());
				return Config.convertInputStreamToString(inputStream);
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			try {
				ArrayList<LatLng> points;
				direction_PolylineOptions = null;
				ArrayList<ArrayList<HashMap<String, String>>> rs = parse(new JSONObject(result));
				for( int i = 0; i< rs.size(); i++){
					points = new ArrayList<>();
					direction_PolylineOptions = new PolylineOptions();
					ArrayList<HashMap<String, String>> path = rs.get(i);
					for( int j = 0; j < path.size(); j ++){
						HashMap<String,String> point = path.get(j);
						double lat = Double.parseDouble(point.get("lat"));
						double lng = Double.parseDouble(point.get("lng"));
						LatLng position = new LatLng(lat, lng);
						points.add(position);
					}
					direction_PolylineOptions.addAll(points);
					direction_PolylineOptions.width(5);
					direction_PolylineOptions.color(R.color.color_widget);
				}
				if(polylineDirection != null){
					polylineDirection.remove();
				}
				polylineDirection = googleMap.addPolyline(direction_PolylineOptions);
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				builder.include(origin);
				builder.include(destination);
				LatLngBounds bounds = builder.build();
				googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		private ArrayList<ArrayList<HashMap<String, String>>> parse(JSONObject jObject) {
			ArrayList<ArrayList<HashMap<String, String>>> routes = new ArrayList<>();
			JSONArray jSteps;
			try {
				JSONArray jRoutes = jObject.getJSONArray("routes");
				for (int i = 0; i < jRoutes.length(); i++) {
					JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					ArrayList<HashMap<String, String>> path = new ArrayList<>();
					for (int j = 0; j < jLegs.length(); j++) {
						jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
						for (int k = 0; k < jSteps.length(); k++) {
							String polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
							ArrayList<LatLng> list = decodePoly(polyline);
							for (int l = 0; l < list.size(); l++) {
								HashMap<String, String> hm = new HashMap<>();
								hm.put("lat", Double.toString((list.get(l)).latitude));
								hm.put("lng",Double.toString((list.get(l)).longitude));
								path.add(hm);
							}
						}
						routes.add(path);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		private ArrayList<LatLng> decodePoly(String encoded) {
			ArrayList<LatLng> poly = new ArrayList<>();
			int index = 0, len = encoded.length();
			int lat = 0, lng = 0;
			while (index < len) {
				int b, shift = 0, result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dLat;
				shift = 0;result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dLng;
				LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
				poly.add(p);
			}
			return poly;
		}
	}

}
