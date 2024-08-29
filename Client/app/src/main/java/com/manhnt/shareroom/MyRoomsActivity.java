package com.manhnt.shareroom;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.database.ShareRoomDatabase;
import com.manhnt.object.Account;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Province;
import com.manhnt.object.Room;
import com.manhnt.adapter.RoomAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyRoomsActivity extends Activity implements OnClickListener {

	private Account mAccount;
	private ArrayList<Province> ListProvince;
	private String[] ListProvinceName;
	private int position_Province_Id = -1;
	private ListView list_view_my_rooms;
	private RoomAdapter adapter;
	private FrameLayout fl_no_rooms;
	private ArrayList<Room> list_my_room = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_rooms_activity);
		getExtraBundle();
		getWidget();
		getListProvince();
		if(mAccount != null && list_my_room.size() == 0){
			RequestAPI.getInstance().context(this).url(Config.URL_GET_MY_ROOMS).method(RequestAPI.GET)
				.isParams(false).isAuthorization(true).message(getResources().getString(R.string.waiting))
				.isShowDialog(true).isShowToast(false).execute(getMyRoomsListener);
		} else {
			list_view_my_rooms.setVisibility(View.VISIBLE);
			fl_no_rooms.setVisibility(View.GONE);
		}
	}
	
	private void getListProvince() {
		ShareRoomDatabase db = ShareRoomDatabase.getInstance(MyRoomsActivity.this);
		ListProvince = db.getAllProvince();
		ListProvinceName = new String[ListProvince.size()];
		for(int i = 0; i<ListProvince.size();i++){
			ListProvinceName[i] = ListProvince.get(i).getName();
		}
	}
	
	private void getExtraBundle(){
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		switch (from) {
		case Config.POST_ROOM_GET_AMENITIES:
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			break;
		case Config.MAIN_ACTIVITY:
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			break;
		case Config.ROOM_DETAIL_ACTIVITY:
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			MyRooms myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
			if(myRooms != null) {
				list_my_room = myRooms.getList_room();
			}
			break;
		default:
			break;
		}
	}
	
	private void getWidget(){
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		list_view_my_rooms = (ListView) findViewById(R.id.list_view_my_rooms);
		adapter = new RoomAdapter(MyRoomsActivity.this, 0, list_my_room);
		adapter.setItemClickListener(new RoomAdapter.OnItemClick() {
			@Override
			public void onItemClickListener(int index) {
				Intent i = new Intent(MyRoomsActivity.this, MyRoomDetailActivity.class);
				i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
				i.putExtra(Config.FROM_ACTIVITY, Config.MY_ROOMS_ACTIVITY);
				i.putExtra(Config.BUNDLE_MY_ROOMS, new MyRooms(list_my_room, index));
				startActivity(i);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		list_view_my_rooms.setAdapter(adapter);
		fl_no_rooms = (FrameLayout) findViewById(R.id.fl_no_rooms);
		manager.TextView(R.id.txt_no_rooms, true);
		manager.ButtonRectangle(R.id.btn_post_room, this, true);
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
		case R.id.btn_post_room:
			if(Config.isInternetConnect(this, true)){
				if(Config.isLogin(this, mAccount, true)){
					DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_city, ListProvinceName,
						position_Province_Id, true, true, choiceListener).show();
				}
			}
			break;
		default:
			break;
		}
	}

	private RequestAPI.RequestAPIListener getMyRoomsListener = new RequestAPI.RequestAPIListener() {

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
			JSONArray jArray = new JSONArray(contentMessage);
			if(jArray.length() > 0){
				String[] array_amenities = getResources().getStringArray(R.array.array_amenities);
				for( int  i = 0; i < jArray.length(); i++){
					Room room = Config.convertJsonToRoom(jArray.optJSONObject(i), array_amenities);
					list_my_room.add(room);
				}
			}
			if(list_my_room.size() == 0){
				fl_no_rooms.setVisibility(View.VISIBLE);
				list_view_my_rooms.setVisibility(View.GONE);
			} else {
				fl_no_rooms.setVisibility(View.GONE);
				list_view_my_rooms.setVisibility(View.VISIBLE);
			}
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onError(Exception e) {}
	};

	private DialogManager.ListOneChoiceDialogListener choiceListener = new DialogManager.ListOneChoiceDialogListener() {
		@Override
		public void onChoice(MaterialDialog dialog, int index) {
			if(index != -1){
				Province province = ListProvince.get(index);
				position_Province_Id = index;
				Intent i = new Intent(MyRoomsActivity.this, PostRoom_GetAddress.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				i.putExtra(Config.BUNDLE_PROVINCE, province);
				i.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_Id);
				i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
				i.putExtra(Config.FROM_ACTIVITY, Config.MY_ROOMS_ACTIVITY);
				startActivity(i);
				overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
			}
		}
	};

	private void intentBack(){
		Intent i = new Intent(MyRoomsActivity.this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		i.putExtra(Config.FROM_ACTIVITY, Config.MY_ROOMS_ACTIVITY);
		startActivity(i);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
