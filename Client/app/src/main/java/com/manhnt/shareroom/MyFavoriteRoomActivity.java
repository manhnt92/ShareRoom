package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.manhnt.adapter.RoomAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MyFavoriteRoomActivity extends Activity implements View.OnClickListener {

    private Account mAccount;
    private ListView list_view_my_favorite_rooms;
    private RoomAdapter adapter;
    private FrameLayout fl_no_favorite_rooms;
    private ArrayList<Room> list_my_favorite_room = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_favorite_room_activity);
        getExtraBundle();
        getWidget();
        if(list_my_favorite_room.size() == 0){
            RequestAPI.getInstance().context(this).message(getString(R.string.waiting))
                .method(RequestAPI.GET).url(Config.URL_FAVORITE_ROOM).isParams(false).isAuthorization(true)
                .isShowDialog(true).isShowToast(false).execute(getMyFavoriteRoomListener);
        } else {
            list_view_my_favorite_rooms.setVisibility(View.VISIBLE);
            fl_no_favorite_rooms.setVisibility(View.GONE);
        }
    }

    private void getExtraBundle(){
        int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
        switch (from) {
            case Config.MAIN_ACTIVITY:
                mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
                break;
            case Config.SEARCH_ROOM_VIEW_DETAIL:
                mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
                MyRooms myFavoriteRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
                if(myFavoriteRooms != null) {
                    list_my_favorite_room = myFavoriteRooms.getList_room();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        intentBack();
    }

    private void getWidget(){
        WidgetManager manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.title, true);
        manager.ImageButton(R.id.btn_back, this, true);
        list_view_my_favorite_rooms = (ListView) findViewById(R.id.list_view_my_favorite_rooms);
        adapter = new RoomAdapter(MyFavoriteRoomActivity.this, 0, list_my_favorite_room);
        adapter.setItemClickListener(new RoomAdapter.OnItemClick() {
            @Override
            public void onItemClickListener(int index) {
                Intent i = new Intent(MyFavoriteRoomActivity.this, SearchRoomsViewDetail.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
                i.putExtra(Config.BUNDLE_MY_ROOMS, new MyRooms(list_my_favorite_room, index));
                i.putExtra(Config.FROM_ACTIVITY, Config.MY_FAVORITE_ROOMS_ACTIVITY);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        list_view_my_favorite_rooms.setAdapter(adapter);
        fl_no_favorite_rooms = (FrameLayout) findViewById(R.id.fl_no_favorite_rooms);
        manager.TextView(R.id.txt_no_favorite_rooms, true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                intentBack();
                break;
            default:
                break;
        }
    }

    private RequestAPI.RequestAPIListener getMyFavoriteRoomListener = new RequestAPI.RequestAPIListener() {
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
                    list_my_favorite_room.add(room);
                }
            }
            if(list_my_favorite_room.size() == 0){
                fl_no_favorite_rooms.setVisibility(View.VISIBLE);
                list_view_my_favorite_rooms.setVisibility(View.GONE);
            } else {
                fl_no_favorite_rooms.setVisibility(View.GONE);
                list_view_my_favorite_rooms.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Exception e) {}
    };

    private void intentBack(){
        Intent i = new Intent(MyFavoriteRoomActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
        i.putExtra(Config.FROM_ACTIVITY, Config.MY_FAVORITE_ROOMS_ACTIVITY);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
