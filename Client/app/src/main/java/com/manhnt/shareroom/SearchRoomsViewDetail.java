package com.manhnt.shareroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.views.starbar.StarBar;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.manhnt.widget.BlurBehind;
import com.manhnt.widget.ExpandableHeightListView;
import com.manhnt.widget.OnBlurCompleteListener;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SearchRoomsViewDetail extends Activity implements View.OnClickListener {

	private Account mAccount;
	private LatLngBounds latLngBounds;
	private float zoom;
	private int position_room_in_list_room_after, activity_intent_back;
	private ScrollView scrollView;
	private ImageButton btn_favorite;
	private ImageView poster_avatar;
	private TextView poster_name;
	private MyRooms myFavoriteRooms;
	private String[] ratings_content;
	private Room currentRoom;
	private boolean isFavoriteRoom = false;
	private int rate;
	private int call_type;
	private Socket mSocket;
	private WidgetManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_room_view_detail);
		getExtraBundle();
		getSocket();
		getWidget();
	}

	private void getExtraBundle() {
		if(getIntent().getExtras() != null) {
			int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
			if(from > 0) {
				if(from == Config.MY_FAVORITE_ROOMS_ACTIVITY){
					mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
					myFavoriteRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
					activity_intent_back = from;
				} else {
					position_room_in_list_room_after = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER);
					mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
					double ne_lat = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LAT);
					double ne_lng = getIntent().getExtras().getDouble(Config.BUNDLE_NE_LNG);
					double sw_lat = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LAT);
					double sw_lng = getIntent().getExtras().getDouble(Config.BUNDLE_SW_LNG);
					latLngBounds = new LatLngBounds(new LatLng(sw_lat, sw_lng), new LatLng(ne_lat, ne_lng));
					zoom = getIntent().getExtras().getFloat(Config.BUNDLE_ZOOM);
					activity_intent_back = from;
				}
			}
		}
	}

	private void getSocket(){
		mSocket = ((MyApplication) getApplication()).getSocket();
		mSocket.off(Config.SOCKET_PING_BEFORE_CALL);
		mSocket.on(Config.SOCKET_PING_BEFORE_CALL, PingBeforeCallListener);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("SetTextI18n")
	private void getWidget() {
		Typeface font = Config.getTypeface(getAssets());
		manager = WidgetManager.getInstance(this);

		if(activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY){
			currentRoom = myFavoriteRooms.getList_room().get(myFavoriteRooms.getPosition());
		} else {
			currentRoom = Config.LIST_ROOM_AFTER.get(position_room_in_list_room_after);
		}
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		btn_favorite = manager.ImageButton(R.id.btn_favorite, this, true);
		manager.LinearLayout(R.id.ll_poster, this, true);
		poster_avatar = manager.ImageView(R.id.poster_avatar, this,  true);
		poster_name = manager.TextView(R.id.poster_name, true);
		manager.ImageButton(R.id.btn_call, this, true);
		manager.ImageButton(R.id.btn_send_email, this, true);
		manager.ImageButton(R.id.btn_chat, this, true);
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
		transparentImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getAction();
				switch (action) {
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
		IconGenerator iconFactory = new IconGenerator(SearchRoomsViewDetail.this);
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
			.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(getResources().getString(R.string.room_address_title), font)))
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
		txt_room_properties_water.setText(" : " + currentRoom.getRoom_properties().getWater() + ".000 " + getResources().getString(R.string.vnd));
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

		RequestAPI.newInstance().context(this).method(RequestAPI.GET).isParams(false).isAuthorization(true)
			.url(Config.URL_SEARCH_ACCOUNT + "?room_id=" + currentRoom.getId()).isShowToast(false)
			.isShowDialog(false).execute(GetAccountListener);
		if(mAccount != null) {
			RequestAPI.newInstance().context(this).method(RequestAPI.GET).isParams(false).isAuthorization(true)
				.url(Config.URL_GET_ROOM_RATINGS + "?room_id=" + currentRoom.getId())
				.isShowDialog(false).isShowToast(false).execute(GetRoomRatingListener);
			RequestAPI.newInstance().context(this).method(RequestAPI.GET).isParams(false).isAuthorization(true)
				.url(Config.URL_SEARCH_FAVORITE_ROOM + "?room_id=" + currentRoom.getId())
				.isShowDialog(false).isShowToast(false).execute(SearchFavoriteRoomListener);
		}
		manager.TextView(R.id.txt_rating, this, true);
		manager.TextView(R.id.txt_comment, this, true);
		manager.TextView(R.id.txt_share, this, true);
	}

	@Override
	public void onBackPressed() {
		if(activity_intent_back == Config.SEARCH_ROOM_VIEW_AS_LIST_ACTIVITY){
			intentBack(SearchRoomsViewAsListActivity.class);
		} else if (activity_intent_back == Config.SEARCH_ROOM_ACTIVITY){
			intentBack(SearchRoomActivity.class);
		} else if ( activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY){
			intentBack();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_back:
				if(activity_intent_back == Config.SEARCH_ROOM_VIEW_AS_LIST_ACTIVITY){
					intentBack(SearchRoomsViewAsListActivity.class);
				} else if (activity_intent_back == Config.SEARCH_ROOM_ACTIVITY){
					intentBack(SearchRoomActivity.class);
				} else if ( activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY){
					intentBack();
				}
				break;
			case R.id.txt_rating:
				if(Config.isLogin(this, mAccount, true)) {
					DialogManager.getInstance().CustomViewDialog(this, R.string.rating, R.layout.ratings_dialog,
						true, RatingListener).show();
				}
				break;
			case R.id.txt_comment:
				if(Config.isLogin(this, mAccount, true)) {
					BlurBehind.getInstance().execute(SearchRoomsViewDetail.this, new OnBlurCompleteListener() {
						@Override
						public void onBlurComplete() {
							Intent i = new Intent(SearchRoomsViewDetail.this, SearchRoomCommentsActivity.class);
							i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
							if(activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY){
								i.putExtra(Config.BUNDLE_MY_ROOMS, myFavoriteRooms);
								i.putExtra(Config.BUNDLE_IS_FAVORITE_ROOM, true);
							} else {
								i.putExtra(Config.BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER, position_room_in_list_room_after);
								i.putExtra(Config.BUNDLE_IS_FAVORITE_ROOM, false);
							}
							startActivityForResult(i, 0);
						}
					});
				}
				break;
			case R.id.txt_share:
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = "Here is the share content body";
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));
				break;
			case R.id.btn_favorite:
				if(Config.isLogin(this, mAccount, true)) {
					if (!isFavoriteRoom) {
						RequestAPI.getInstance().context(this).message(getString(R.string.waiting))
							.url(Config.URL_FAVORITE_ROOM).method(RequestAPI.POST).isParams(true)
							.isShowDialog(true).isShowToast(true).isAuthorization(true).execute(CreateFavoriteRoomListener);
					} else {
						String url = Config.URL_FAVORITE_ROOM + "/";
						if(activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY){
							url += myFavoriteRooms.getList_room().get(myFavoriteRooms.getPosition()).getId();
						} else {
							url += Config.LIST_ROOM_AFTER.get(position_room_in_list_room_after).getId();
						}
						RequestAPI.getInstance().context(this).message(getString(R.string.waiting))
							.url(url).method(RequestAPI.DELETE).isParams(false)
							.isShowDialog(true).isShowToast(true).isAuthorization(true).execute(DeleteFavoriteRoomListener);
					}
				}
				break;
			case R.id.ll_poster:
				Intent i = new Intent(SearchRoomsViewDetail.this, PosterActivity.class);
				i.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_VIEW_DETAIL);
				startActivityForResult(i, Config.POSTER_ACTIVITY);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case R.id.btn_call:
				String[] arr_call = getResources().getStringArray(R.array.arr_call);
				DialogManager.getInstance().ListOneChoiceDialog(this, R.string.call_dialog_title, arr_call,
					-1, false, true, new DialogManager.ListOneChoiceDialogListener() {
						@Override
						public void onChoice(MaterialDialog dialog, int index) {
							if(index == 0){
								CallViaPhoneNumber();
							} else if (index == 1){
								AudioCallViaShareRoom();
							} else if (index == 2) {
								VideoCallViaShareRoom();
							}else {
								dialog.dismiss();
							}
						}
					}).show();
				break;
			case R.id.btn_send_email:
				if(Config.isLogin(this, mAccount, true)) {
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Config.ACCOUNT_POST.getEmail()});
					sendIntent.setType("application/octet-stream");
					startActivity(sendIntent);
				}
				break;
			case R.id.btn_chat:
				if(Config.isLogin(this, mAccount, true)) {
					BlurBehind.getInstance().execute(SearchRoomsViewDetail.this, new OnBlurCompleteListener() {
						@Override
						public void onBlurComplete() {
							Intent i = new Intent(SearchRoomsViewDetail.this, SearchRoomSendMessageActivity.class);
							i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
							startActivityForResult(i, 0);
						}
					});
				}
				break;
			default:
				break;
		}
	}

	private void CallViaPhoneNumber(){
		if(Config.isLogin(this, mAccount, true)) {
			String To_Phone_Number = Config.ACCOUNT_POST.getPhoneNumber();
			if(!TextUtils.isEmpty(To_Phone_Number) && !To_Phone_Number.equalsIgnoreCase("null")
					&& !To_Phone_Number.equalsIgnoreCase(getString(R.string.no_content))) {
				Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + To_Phone_Number));
				startActivity(callIntent);
			} else {
				Config.showCustomToast(this, 0, getString(R.string.no_info) + " " +
					getString(R.string.phonenumber) + " " + Config.ACCOUNT_POST.getFirst_name() + " " + Config.ACCOUNT_POST.getLast_name());
			}
		}
	}

	private void AudioCallViaShareRoom(){
		if(Config.isInternetConnect(this, true)) {
			try {
				call_type = Config.SOCKET_CALL_AUDIO;
				JSONObject jObj = new JSONObject();
				jObj.put(Config.ID, mAccount.getId());
				jObj.put(Config.SOCKET_CALL_TYPE, call_type);
				jObj.put(Config.SOCKET_USER_NAME, mAccount.getFirst_name() + " " + mAccount.getLast_name());
				jObj.put(Config.TO_ID, Config.ACCOUNT_POST.getId());
				mSocket.emit(Config.SOCKET_PING_BEFORE_CALL, jObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void VideoCallViaShareRoom(){
		if(Config.isInternetConnect(this, true)) {
			try {
				call_type = Config.SOCKET_CALL_VIDEO;
				JSONObject jObj = new JSONObject();
				jObj.put(Config.ID, mAccount.getId());
				jObj.put(Config.SOCKET_CALL_TYPE, call_type);
				jObj.put(Config.SOCKET_USER_NAME, mAccount.getFirst_name() + " " + mAccount.getLast_name());
				jObj.put(Config.TO_ID, Config.ACCOUNT_POST.getId());
				mSocket.emit(Config.SOCKET_PING_BEFORE_CALL, jObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private DialogManager.CustomViewListener RatingListener = new DialogManager.CustomViewListener() {
		@Override
		public void onAttachCustomView(View view) {
			StarBar starBar = (StarBar) view.findViewById(R.id.starBar);
			final TextView txt_rating_content = manager.TextView(view, R.id.txt_rating_content, true);
			ratings_content = getResources().getStringArray(R.array.ratings_array_content);
			txt_rating_content.setText(ratings_content[rate]);
			starBar.setCurrentStar(rate);
			starBar.setOnRatingSliderChangeListener(new StarBar.OnRatingSliderChangeListener() {
				@Override
				public boolean onStartRating() {
					return true;
				}

				@Override
				public void onPendingRating(int rating) {
					txt_rating_content.setText(ratings_content[rating]);
				}

				@Override
				public void onFinalRating(int rating, boolean swipe) {
					rate = rating;
				}

				@Override
				public void onCancelRating() {}
			});
		}

		@Override
		public void onOK(MaterialDialog dialog) {
			RequestAPI.getInstance().context(SearchRoomsViewDetail.this).message(getString(R.string.waiting))
				.method(RequestAPI.POST).url(Config.URL_RATINGS).isParams(true).isAuthorization(true)
				.isShowDialog(true).isShowToast(true).execute(SendRatingListener);
		}

		@Override
		public void onCancel(MaterialDialog dialog) {
			dialog.dismiss();
		}
	};

	private RequestAPI.RequestAPIListener GetAccountListener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@SuppressLint("SetTextI18n")
		@Override
		public void onResult(String contentMessage) throws JSONException {
			DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.mipmap.ic_empty_icon)
				.showImageForEmptyUri(R.mipmap.ic_empty_icon)
				.showImageOnFail(R.mipmap.ic_empty_icon)
				.bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888).imageScaleType(ImageScaleType.EXACTLY)
				.displayer(new RoundedBitmapDisplayer(10)).build();
			ImageLoader mImageLoader = ImageLoader.getInstance();
			ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(SearchRoomsViewDetail.this)
				.defaultDisplayImageOptions(options)
				.diskCacheExtraOptions(200, 200, null)
				.memoryCache(new WeakMemoryCache()).build());
			Config.ACCOUNT_POST = Config.convertJsonToAccount(new JSONObject(contentMessage));
			if(!Config.ACCOUNT_POST.getAvatar().isEmpty() && !Config.ACCOUNT_POST.getAvatar().equalsIgnoreCase("null")) {
				mImageLoader.displayImage(Config.ACCOUNT_POST.getAvatar(), poster_avatar, options);
			} else {
				poster_avatar.setImageResource(R.drawable.ic_user_male_press);
			}
			poster_name.setText(Config.ACCOUNT_POST.getFirst_name() + " " + Config.ACCOUNT_POST.getLast_name());
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener GetRoomRatingListener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			JSONObject jObj = new JSONObject(contentMessage);
			rate = jObj.optInt(Config.RATING);
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener SendRatingListener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			JSONObject jObj = new JSONObject();
			jObj.put(Config.RATING, rate);
			jObj.put(Config.ROOM_ID, currentRoom.getId());
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {

		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener SearchFavoriteRoomListener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			if(contentMessage.equalsIgnoreCase(Config.TRUE)){
				btn_favorite.setImageResource(R.mipmap.ic_favorite);
				isFavoriteRoom = true;
			} else {
				btn_favorite.setImageResource(R.mipmap.ic_un_favorite);
				isFavoriteRoom = false;
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener CreateFavoriteRoomListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			JSONObject jObj = new JSONObject();
			if(activity_intent_back == Config.MY_FAVORITE_ROOMS_ACTIVITY) {
				jObj.put(Config.ROOM_ID, myFavoriteRooms.getList_room().get(myFavoriteRooms.getPosition()).getId());
			} else {
				jObj.put(Config.ROOM_ID, Config.LIST_ROOM_AFTER.get(position_room_in_list_room_after).getId());
			}
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			btn_favorite.setImageResource(R.mipmap.ic_favorite);
			isFavoriteRoom = true;
		}

		@Override
		public void onError(Exception e) {}
	};

	private RequestAPI.RequestAPIListener DeleteFavoriteRoomListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			return null;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			btn_favorite.setImageResource(R.mipmap.ic_un_favorite);
			isFavoriteRoom = false;
		}

		@Override
		public void onError(Exception e) {}
	};

	private Emitter.Listener PingBeforeCallListener = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					JSONObject jObj = (JSONObject) args[0];
					try {
						boolean isSuccess = jObj.getBoolean(Config.SUCCESS);
						if(isSuccess){
							String To_socketID = jObj.optString(Config.SOCKET_CALL_SOCKET_ID);
							Class clazz = (call_type == Config.SOCKET_CALL_AUDIO) ? CallActivity.class : VideoCallActivity.class;
							Intent i = new Intent(SearchRoomsViewDetail.this, clazz);
							i.putExtra(Config.FROM_ACTIVITY, Config.CHAT_ACTIVITY);
							i.putExtra(Config.BUNDLE_CALL_SOCKET_ID, To_socketID);
							i.putExtra(Config.BUNDLE_CALL_USER_NAME, Config.ACCOUNT_POST.getFirst_name() + " " + Config.ACCOUNT_POST.getLast_name());
							i.putExtra(Config.BUNDLE_IS_CALLER, true);
							startActivity(i);
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						} else {
							String message = getString(R.string.call_not_establish) + " " + Config.ACCOUNT_POST.getFirst_name() + " " + Config.ACCOUNT_POST.getLast_name();
							Config.showCustomToast(SearchRoomsViewDetail.this, 0, message);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}
	};

	private void intentBack(Class<? extends Activity> clazz){
		Intent intent_back = new Intent(SearchRoomsViewDetail.this, clazz);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_NE_LAT, latLngBounds.northeast.latitude);
		intent_back.putExtra(Config.BUNDLE_NE_LNG, latLngBounds.northeast.longitude);
		intent_back.putExtra(Config.BUNDLE_SW_LAT, latLngBounds.southwest.latitude);
		intent_back.putExtra(Config.BUNDLE_SW_LNG, latLngBounds.southwest.longitude);
		intent_back.putExtra(Config.BUNDLE_ZOOM, zoom);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_VIEW_DETAIL);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void intentBack(){
		Intent intent_back = new Intent(SearchRoomsViewDetail.this, MyFavoriteRoomActivity.class);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myFavoriteRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.SEARCH_ROOM_VIEW_DETAIL);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
