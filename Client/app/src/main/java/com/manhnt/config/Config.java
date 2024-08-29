package com.manhnt.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.rebound.SpringConfig;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.manhnt.adapter.MessagePagerAdapter;
import com.manhnt.object.Account;
import com.manhnt.object.Amenities;
import com.manhnt.object.ChatMessage;
import com.manhnt.object.Comments;
import com.manhnt.object.Conversation;
import com.manhnt.object.District;
import com.manhnt.object.Filter;
import com.manhnt.object.Image;
import com.manhnt.object.Province;
import com.manhnt.object.Room;
import com.manhnt.object.Room_Address;
import com.manhnt.object.Room_Amenities;
import com.manhnt.object.Room_Images;
import com.manhnt.object.Room_Properties;
import com.manhnt.object.Ward;
import com.manhnt.shareroom.R;

public class Config {

	public static boolean ANIMATION_ENABLE;
	public static float MIN_SCALE;
	public static float MIN_SCALE_PREFERENCE;
	public static int TENSION;
	public static int FRICTION;
	public static final float MAX_SCALE = 1f;
	public static SpringConfig SCALE_CONFIG;
	public static String FONT;
	public static int POSITION_FONT_NAME;
	public static LruCache<String,Typeface> typeface_map = new LruCache<>(3);
	public static final int COLOR_DIALOG = Color.parseColor("#80673AB7");
	public static final int ACCOUNT_NORMAL = 0;
	public static final int ACCOUNT_FACEBOOK = 1;

//    public static final String BASE_URL = "http://shareroom-thelord1992.rhcloud.com/v1/";
	public static final String BASE_URL = "http://192.168.11.102/ShareRoom/v1/";
//	public static final String SOCKET_URL = "http://shareroomchat-thelord1992.rhcloud.com/";
	public static final String SOCKET_URL = "http://192.168.11.102:3000";
    public static final String URL_CREATE_ACCOUNT = BASE_URL + "account";
    public static final String URL_LOGIN = BASE_URL + "account/login";
    public static final String URL_UPDATE_ACCOUNT = BASE_URL + "account";
	public static final String URL_SEARCH_ACCOUNT = BASE_URL + "account/search";
    public static final String URL_CREATE_ROOM = BASE_URL + "rooms";
    public static final String URL_GET_MY_ROOMS = BASE_URL + "myrooms";
    public static final String URL_UPDATE_ROOM = BASE_URL + "rooms/";
    public static final String URL_DELETE_ROOM = BASE_URL + "rooms/";
    public static final String URL_SEARCH_ROOM =  BASE_URL + "rooms/search";
	public static final String URL_SEARCH_ROOM_COMMENTS = BASE_URL + "comments/search";
	public static final String URL_COMMENT = BASE_URL + "comments";
	public static final String URL_RATINGS = BASE_URL + "ratings";
	public static final String URL_GET_ROOM_RATINGS = BASE_URL + "ratings/search";
	public static final String URL_SEARCH_FAVORITE_ROOM = BASE_URL + "favoriteroom/search";
	public static final String URL_FAVORITE_ROOM = BASE_URL + "favoriteroom";
    public static final String GOOGLE_DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    public static final String CLOUD_NAME = "thelord1992";
    public static final String CLOUD_API_KEY = "797193516226939";
    public static final String CLOUD_API_SECRET = "45XldUBSbIyiGYNLqJj-DmSzgT0";

    public static final float ROUND = 10f;

    public static final int MAIN_ACTIVITY = 1;
    public static final int LOGIN_ACTIVITY = 2;
    public static final int REGISTER_ACTIVITY = 3;
    public static final int PROFILE_ACTIVITY = 4;
    public static final int POST_ROOM_GET_ADDRESS = 5;
    public static final int POST_ROOM_GET_PROPERTIES = 6;
    public static final int POST_ROOM_GET_IMAGES = 7;
    public static final int POST_ROOM_GET_AMENITIES = 8;
    public static final int MY_ROOMS_ACTIVITY = 9;
    public static final int ROOM_DETAIL_ACTIVITY = 10;
    public static final int SEARCH_ROOM_ACTIVITY = 11;
    public static final int SEARCH_ROOM_VIEW_AS_LIST_ACTIVITY = 12;
	public static final int SEARCH_ROOM_VIEW_DETAIL = 13;
	/*public static final int COMMENTS_ACTIVITY = 14;*/
	/*public static final int SEND_MESSAGE_ACTIVITY = 15;*/
	public static final int POSTER_ACTIVITY = 16;
	public static final int MY_FAVORITE_ROOMS_ACTIVITY = 17;
	public static final int MY_CONVERSATION_ACTIVITY = 18;
	public static final int CHAT_ACTIVITY = 19;
	public static final int CHAT_BROADCAST_RECEIVER = 20;

    public static final String FROM_ACTIVITY = "From_Activity";
    public static final String BUNDLE_EMAIL = "Email";
    public static final String BUNDLE_ACCOUNT = "Account";
    public static final String BUNDLE_PROVINCE = "Province";
	public static final String BUNDLE_POSITION_PROVINCE_ID = "position_Province_ID";
	public static final String BUNDLE_POSITION_DISTRICT_ID = "position_District_ID";
	public static final String BUNDLE_POSITION_WARD_ID = "position_Ward_ID";
	public static final String BUNDLE_ROOM = "Room";
	public static final String BUNDLE_NE_LAT = "ne_lat";
	public static final String BUNDLE_NE_LNG = "ne_lng";
	public static final String BUNDLE_SW_LAT = "sw_lat";
	public static final String BUNDLE_SW_LNG = "sw_lng";
	public static final String BUNDLE_ZOOM = "zoom";
	public static final String BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER = "position_room_in_list_room_after";
	public static final String BUNDLE_IS_FAVORITE_ROOM = "isFavoriteRoom";
	public static final String BUNDLE_MY_ROOMS = "my_rooms";
	public static final String BUNDLE_CHAT_MESSAGE = "Chat_Message";
	public static final String BUNDLE_CALL_SOCKET_ID = "CallSocketID";
	public static final String BUNDLE_CALL_USER_NAME = "CallUserName";
	public static final String BUNDLE_IS_CALLER = "IsCaller";

	public static final String ID = "id";
	public static final String EMAIL = "email";
	public static final String PASSWORD = "password";
	public static final String API_KEY = "api_key";
	public static final String FACEBOOK_ID = "facebook_id";
	public static final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String GENDER = "gender";
	public static final String AGE = "age";
	public static final String BIRTHDAY = "birthday";
	public static final String ADDRESS = "address";
	public static final String OCCUPATION = "occupation";
	public static final String DESCRIPTION = "description";
	public static final String PHONENUMBER = "phonenumber";
	public static final String AVATAR = "avatar";
	public static final String ACCOUNT_TYPE = "account_type";
	public static final String ROOM_ADDRESS = "room_address";
	public static final String PROVINCE_ID = "province_id";
	public static final String DISTRICT_ID = "district_id";
	public static final String WARD_ID = "ward_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ROOM_PROPERTIES = "room_properties";
	public static final String RENT_PER_MONTH = "rent_per_month";
	public static final String ELECTRIC = "electric";
	public static final String WATER = "water";
	public static final String AREA = "area";
	public static final String NUMBER_PER_ROOM = "number_per_room";
	public static final String MIN_STAY ="min_stay";
	public static final String AVAILABLE_FROM = "available_from";
	public static final String ROOM_TYPE = "room_type";
	public static final String ROOM_STATE = "room_state";
	public static final String ROOM_IMAGES = "room_images";
	public static final String LINK = "link";
	public static final String NOTE = "note";
	public static final String ROOM_AMENITIES = "room_amenities";
	public static final String OTHER = "Other";
	public static final String SUCCESS = "success";
	public static final String MESSAGE = "message";
	public static final String CONTENT_MESSAGE = "content";
	public static final String CURRENT_PAGE = "current_page";
	public static final String TOTAL_PAGE = "total_page";

	public static final String PREF = "ShareRoom";
	public static final String PREF_ACCOUNT = "pref_account";
	public static final String PREF_FILTER = "Filter_Result";
	public static final String PREF_SCALE_ANIMATION_ENABLE = "Animation_Enable";
	public static final String PREF_SCALE_ANIMATION_MIN_SCALE = "Min_Scale";
	public static final String PREF_SCALE_ANIMATION_TENSION = "Tension";
	public static final String PREF_SCALE_ANIMATION_FRICTION = "Friction";
	public static final String PREF_FONT_NAME = "Font_Name";
	public static final String PREF_POSITION_FONT_NAME = "Position_Font_Name";

	public static final String RENT_MIN = "rent_min";
	public static final String RENT_MAX = "rent_max";
	public static final String ELECTRIC_MIN = "electric_min";
	public static final String ELECTRIC_MAX = "electric_max";
	public static final String WATER_MIN = "water_min";
	public static final String WATER_MAX = "water_max";
	public static final String AREA_MIN = "area_min";
	public static final String AREA_MAX = "area_max";
	public static final String PERSON_MIN = "person_min";
	public static final String PERSON_MAX = "person_max";

	public static final String USER_ID = "user_id";
	public static final String COMMENT_ID = "comment_id";
	public static final String COMMENT = "comment";
	public static final String CREATED = "created";
	public static final String ROOM_ID = "room_id";
	public static final String RATING = "rating";
	public static final String TRUE = "true";
	public static final int CAMERA = 0;
	public static final int GALLERY = 1;
	public static final String PATH = android.os.Environment.getExternalStorageDirectory()+ "/ShareRoom";
	public static final String PNG = ".png";
	public static final String UPDATE_TYPE = "update_type";

	public static final int UPDATE_ROOM_ADDRESS = 1;
	public static final int UPDATE_ROOM_PROPERTIES = 2;
	public static final int UPDATE_ROOM_IMAGES = 3;
	public static final int UPDATE_ROOM_AMENITIES = 4;

	public static ArrayList<Room> LIST_ROOM_BEFORE = new ArrayList<>();
	public static ArrayList<Room> LIST_ROOM_AFTER = new ArrayList<>();
	public static Account ACCOUNT_POST;
	private static final String START_CHAR = "[";
	private static final String END_CHAR = "]";
	public static final String CHAT_ACTION = "com.manhnt.intent.action.CHAT";

	/*public static final String SOCKET_SERVER_MESSAGE = "ServerMessage";*/
	public static final String SOCKET_USER_INFO = "UserInfo";
	public static final String SOCKET_CONVERSATION = "Conversation";
	public static final String SOCKET_MY_CONVERSATIONS = "MyConversations";
	public static final String SOCKET_PRIVATE_MESSAGE = "PrivateMessage";
	public static final String SOCKET_UN_READ_MESSAGE = "UnReadMessage";
	public static final String SOCKET_PING = "Ping";
	public static final String SOCKET_USER_NAME = "UserName";
	public static final String SOCKET_PING_BEFORE_CALL = "PingBeforeCall";
	public static final String SOCKET_CALL_SOCKET_ID = "ToSocketID";
	public static final String SOCKET_INCOMING_CALL = "InComingCall";
	public static final int PING_TIME = 10000;
	public static final String SOCKET_USER1_ID = "User1_ID";
	public static final String SOCKET_USER2_ID = "User2_ID";
	public static final String SOCKET_GET_PHONE_NUMBER = "GetPhoneNumber";

	/** Call */
	public static final String SOCKET_CALL_TYPE = "CallType";
	public static final int SOCKET_CALL_AUDIO = 0;
	public static final int SOCKET_CALL_VIDEO = 1;
	public static final String SOCKET_CALL_READY = "Ready";
	public static final String SOCKET_CALL_REJECT = "Reject";
	public static final String SOCKET_END_CALL = "EndCall";
	public static final String SOCKET_FROM = "From";
	public static final String SOCKET_TO = "To";
	public static final String SOCKET_CONTENT = "Content";
	public static final String SOCKET_SESSION_DESCRIPTION = "sdp";

	public static final String SOCKET_CALL_MESSAGE = "SocketCallMessage";
	public static final String SOCKET_CALL_EVENT = "Event";

	public static final String SOCKET_CALL_INIT_EVENT = "init";
	public static final String SOCKET_CALL_OFFER_EVENT = "offer";
	public static final String SOCKET_CALL_ANSWER_EVENT = "answer";
	public static final String SOCKET_CALL_CANDIDATE_EVENT = "candidate";

	public static final String DATA = "data";
	public static final String CHAT_ID = "chat_id";
	public static final String FROM_ID = "from_id";
	public static final String TO_ID = "to_id";
	public static final String STATUS = "status";
	public static final int ONLINE = 1;
	public static boolean IS_PUSH_NOTIFICATION = true;
	public static int CHAT_WITH = 0;
	public static ArrayList<ChatMessage> LIST_CHAT_MESSAGE = new ArrayList<>();
	public static MessagePagerAdapter MESSAGE_PAGER_ADAPTER;
	public static TextView MESSAGE_TXT_PAGE;
	public static TextView MESSAGE_TXT_USERNAME;
	public static ViewPager MESSAGE_VIEW_PAGER;
	public static ImageButton MESSAGE_BTN_NEXT;
	public static ChatMessage LAST_CHAT_MESSAGE = new ChatMessage(0, 0, 0, 0, "", "", 0, "");
	public static int LAST_NOTIFICATION_ID;
	public static boolean IS_SHOW_DIALOG = false;

	public static final int MY_COMMENT_EDIT = 0;
	public static final int MY_COMMENT_DELETE = 1;
	public static final int MY_COMMENT_COPY = 2;
	public static final int COMMENT_COPY = 0;

	public static Typeface getTypeface(AssetManager mAssetManager) {
		if(typeface_map.get(FONT) != null){
			return typeface_map.get(FONT);
		}
        Typeface typeface = Typeface.createFromAsset(mAssetManager, FONT);
		typeface_map.put(FONT, typeface);
        return typeface;
	}

	public static void showCustomToast(Context context , int icon , String message){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		@SuppressLint("InflateParams") View view = inflater.inflate(R.layout.custom_toast, null);
		ImageView toast_img = (ImageView) view.findViewById(R.id.toast_img);
		if (icon != 0) {
			toast_img.setVisibility(View.VISIBLE);
			toast_img.setBackgroundResource(icon);
		} else {
			toast_img.setVisibility(View.GONE);
		}
		TextView toast_message = (TextView) view.findViewById(R.id.toast_message);
		toast_message.setTypeface(getTypeface(context.getAssets()));
		toast_message.setText(message);
		toast_message.setTextColor(Color.WHITE);
		Toast toast = new Toast(context);
		toast.setView(view);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}

	public static boolean isInternetConnect(Context context, boolean isShowToast){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(isShowToast && info == null){
			showCustomToast(context, 0, context.getResources().getString(R.string.internet_not_connected));
		}
		return info != null;
	}

	public static boolean isLogin(Context context, Account account, boolean isShowToast){
		if(isShowToast && account == null){
			showCustomToast(context, 0, context.getResources().getString(R.string.require_login));
		}
		return account != null;
	}

	public static String convertBirthDayFacebook(String input){
		try {
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_input = new SimpleDateFormat("MM/dd/yyyy");
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_output = new SimpleDateFormat("dd/MM/yyyy");
			Date d = format_input.parse(input);
			return format_output.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String convertCommentsDateTime(String input){
		try {
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_output = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
			Date d = format_input.parse(input);
			return format_output.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String convertCurrentTimeCommentsDateTime(long currentTimeMillis){
		@SuppressLint("SimpleDateFormat") SimpleDateFormat format_output = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
		Date d = new Date(currentTimeMillis);
		return format_output.format(d);
	}

	private static String convertChatsDateTime(String input){
		try {
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@SuppressLint("SimpleDateFormat") SimpleDateFormat format_output = new SimpleDateFormat("HH:mm dd/MM");
			Date d = format_input.parse(input);
			return format_output.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Account convertJsonToAccount(JSONObject jObj){
		int id = jObj.optInt(ID);
		String email = jObj.optString(EMAIL);
		String password = jObj.optString(PASSWORD);
		String api_key = jObj.optString(API_KEY);
		String facebook_id = jObj.optString(FACEBOOK_ID);
		String access_token = jObj.optString(FACEBOOK_ACCESS_TOKEN);
		String first_name = jObj.optString(FIRST_NAME);
		String last_name = jObj.optString(LAST_NAME);
		String gender = jObj.optString(GENDER);
		String birthday = jObj.optString(BIRTHDAY);
		int age = jObj.optInt(AGE);
		String address = jObj.optString(ADDRESS);
		String occupation = jObj.optString(OCCUPATION);
		String description = jObj.optString(DESCRIPTION);
		String phoneNumber = jObj.optString(PHONENUMBER);
		String avatar = jObj.optString(AVATAR);
		int account_type = jObj.optInt(ACCOUNT_TYPE);
		
		return new Account(id, email, password, api_key, facebook_id,access_token, first_name, last_name, gender,
				birthday, age, address, occupation, description, phoneNumber, avatar, account_type);
	}

	public static String convertAccountToString(Account account){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put(ID, account.getId());
			jObj.put(EMAIL, account.getEmail());
			jObj.put(PASSWORD, account.getPassword());
			jObj.put(API_KEY, account.getApi_key());
			jObj.put(FACEBOOK_ID, account.getFacebook_id());
			jObj.put(FACEBOOK_ACCESS_TOKEN, account.getFacebook_access_token());
			jObj.put(FIRST_NAME, account.getFirst_name());
			jObj.put(LAST_NAME, account.getLast_name());
			jObj.put(GENDER, account.getGender());
			jObj.put(BIRTHDAY, account.getBirthday());
			jObj.put(AGE, account.getAge());
			jObj.put(ADDRESS, account.getAddress());
			jObj.put(OCCUPATION, account.getOccupation());
			jObj.put(DESCRIPTION, account.getDescription());
			jObj.put(PHONENUMBER, account.getPhoneNumber());
			jObj.put(AVATAR, account.getAvatar());
			jObj.put(ACCOUNT_TYPE, account.getAccount_type());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jObj.toString();
	}

	public static Room convertJsonToRoom(JSONObject jObj, String[] array_amenities){
		JSONObject jsonAddress = jObj.optJSONObject(ROOM_ADDRESS);
			Room_Address room_address = new Room_Address();
			room_address.setAddress(jsonAddress.optString(ADDRESS));
			room_address.setLatitude(jsonAddress.optDouble(LATITUDE));
			room_address.setLongitude(jsonAddress.optDouble(LONGITUDE));
			room_address.setProvince(new Province(jsonAddress.optString(PROVINCE_ID)));
			room_address.setDistrict(new District(jsonAddress.optString(DISTRICT_ID)));
			room_address.setWard(new Ward(jsonAddress.optString(WARD_ID)));
		JSONObject jsonProperties = jObj.optJSONObject(ROOM_PROPERTIES);
			Room_Properties room_properties = new Room_Properties();
			room_properties.setRent_per_month((float) jsonProperties.optDouble(RENT_PER_MONTH));
			room_properties.setElectric((float) jsonProperties.optDouble(ELECTRIC));
			room_properties.setWater(jsonProperties.optInt(WATER));
			room_properties.setArea(jsonProperties.optInt(AREA));
			room_properties.setNumber_per_room(jsonProperties.optInt(NUMBER_PER_ROOM));
			room_properties.setMin_stay(jsonProperties.optInt(MIN_STAY));
			room_properties.setAvailable_from(jsonProperties.optString(AVAILABLE_FROM));
			room_properties.setRoom_type(jsonProperties.optString(ROOM_TYPE));
			room_properties.setRoom_state(jsonProperties.optString(ROOM_STATE));
		JSONArray jsonImages = jObj.optJSONArray(ROOM_IMAGES);
			ArrayList<Image> list_img = new ArrayList<>();
			for(int i = 0 ; i < jsonImages.length(); i++){
				list_img.add(new Image(jsonImages.optJSONObject(i).optInt(ID), jsonImages.optJSONObject(i).optString(LINK), jsonImages.optJSONObject(i).optString(NOTE),
						true, true));
			}
			Room_Images room_images = new Room_Images();
			room_images.setList_images(list_img);
		JSONObject jsonAmenities = jObj.optJSONObject(ROOM_AMENITIES);
			Room_Amenities room_amenities = new Room_Amenities();
			ArrayList<Amenities> List_Amenities = new ArrayList<>();
			for(int i = 0; i< array_amenities.length; i = i+2){
				boolean bool = jsonAmenities.optBoolean(array_amenities[i + 1]);
				List_Amenities.add(new Amenities(array_amenities[i], bool, array_amenities[i + 1]));
			}
			room_amenities.setList_amenities(List_Amenities);
			room_amenities.setOther(jsonAmenities.optString(OTHER));

		return new Room(jObj.optInt(ID),room_address, room_properties, room_images, room_amenities);
	}

	public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null){
            result.append(line);
        }
		inputStream.close();
		return result.toString();
    }

	public static double convertDMSToLng(String longitude) {
		double lng;
		longitude = longitude.replace("E", "");
		String[] split = longitude.split(" ");
		lng = (double) (((Float.valueOf(split[2])/60) + Float.valueOf(split[1]))/60)+ Float.valueOf(split[0]);
		return lng;
	}

	public static double convertDMSToLat(String latitude) {
		double lat;
		latitude = latitude.replace("N", "");
		String[] split = latitude.split(" ");
		lat = (double) (((Float.valueOf(split[2])/60) + Float.valueOf(split[1]))/60)+ Float.valueOf(split[0]);
		return lat;
	}

    public static String getRealPathFromURI_AboveAPI11(Context context, Uri contentUri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		String result = null;
		CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();
		if(cursor != null){
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);
		}
		return result;  
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
		if(cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		return null;
    }

	public static void FilterResult(Context context, Filter filter, GoogleMap googleMap, IconGenerator iconFactory){
		LIST_ROOM_AFTER.clear();
		int pos = 0;
		for(int i = 0; i < LIST_ROOM_BEFORE.size(); i++){
			Room_Properties room_properties = LIST_ROOM_BEFORE.get(i).getRoom_properties();
			float rent = room_properties.getRent_per_month();
			float electric = room_properties.getElectric();
			int water = room_properties.getWater();
			int area = room_properties.getArea();
			int person = room_properties.getNumber_per_room();
			if(rent <= filter.getRent_max() && rent >= filter.getRent_min() && electric <= filter.getElectric_max() && electric >= filter.getElectric_min()
				&& water <= filter.getWater_max() && water >= filter.getWater_min() && area <= filter.getArea_max() && area >= filter.getArea_min()
				&& person <= filter.getPerson_max() && person >= filter.getPerson_min()){
				LIST_ROOM_AFTER.add(LIST_ROOM_BEFORE.get(i));
				MarkerOptions markerOptions = new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(rent + " " + context.getResources().getString(R.string.million_vnd), getTypeface(context.getAssets()))))
	                .title(context.getResources().getString(R.string.address) + " : " + LIST_ROOM_BEFORE.get(i).getRoom_address().getAddress()).snippet(""+pos)
	                .position(new LatLng(LIST_ROOM_BEFORE.get(i).getRoom_address().getLatitude(), LIST_ROOM_BEFORE.get(i).getRoom_address().getLongitude()))
	                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
				googleMap.addMarker(markerOptions);
				pos++;
			}
		}
	}

	public static void convertJsonToComments(JSONArray jArray, ArrayList<Comments> list_comments){
		try {
			list_comments.clear();
			for(int i = 0; i < jArray.length(); i++){
				JSONObject jObj = jArray.getJSONObject(i);
				int comment_id = jObj.optInt(COMMENT_ID);
				int user_id = jObj.optInt(USER_ID);
				String userName = jObj.optString(FIRST_NAME) + " " + jObj.optString(LAST_NAME);
				String avatar = jObj.optString(AVATAR);
				String comment = jObj.optString(COMMENT);
				String created = convertCommentsDateTime(jObj.optString(CREATED));
				Comments c = new Comments(comment_id, user_id, comment, avatar, userName, created);
				list_comments.add(c);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static String convertStringToHexString(String input) {
		try {
			byte[] bytes = input.getBytes();
			String str = new String(bytes, Charset.forName("UTF-8"));
			char[] ach = str.toCharArray();
			int[] acp = new int[Character.codePointCount(ach, 0, ach.length)];
			int j = 0;
			for (int i = 0, cp; i < ach.length; i += Character.charCount(cp)) {
				cp = Character.codePointAt(ach, i);
				acp[j++] = cp;
			}
			String output = "";
			for (int anAcp : acp) {
				output += START_CHAR + Integer.toHexString(anAcp) + END_CHAR;
			}
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String convertHexStringToString(String input){
		ArrayList<Integer> list_int = new ArrayList<>();
		int length = input.length();
		int position = 0;
		StringBuilder buffer = new StringBuilder();
		boolean inTag = false;
		if (length <= 0) {
			return null;
		}
		do {
			String c = input.subSequence(position, position + 1).toString();
			if (!inTag && c.equals(START_CHAR)) {
				buffer = new StringBuilder();
				inTag = true;
			}
			if (inTag) {
				buffer.append(c);
				if (c.equals(END_CHAR)) {
					inTag = false;
					String tag = buffer.toString();
					String hexStr = tag.substring(1, tag.length() - 1);
					list_int.add(Integer.parseInt(hexStr, 16));
				}
			}
			position++;
		} while (position < length);
		StringBuilder output = new StringBuilder();
		for( int  i = 0; i< list_int.size(); i++){
			char[] c = Character.toChars(list_int.get(i));
			output.append(new String(c));
		}
		return output.toString();
	}

	public static ChatMessage convertJsonObjectToChatMessage(JSONObject jObj){
		String username = jObj.optString(FIRST_NAME) + " " + jObj.optString(LAST_NAME);
		return new ChatMessage(jObj.optInt(ID), jObj.optInt(CHAT_ID), jObj.optInt(FROM_ID),
			jObj.optInt(TO_ID), jObj.optString(MESSAGE), convertChatsDateTime(jObj.optString(CREATED)),jObj.optInt(STATUS), username);
	}

	public static Conversation convertJsonObjectToConversation(JSONObject jObj){
		return new Conversation(jObj.optInt(ID), jObj.optString(AVATAR),
			jObj.optString(FIRST_NAME) + " " + jObj.optString(LAST_NAME), 0);
	}

	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
