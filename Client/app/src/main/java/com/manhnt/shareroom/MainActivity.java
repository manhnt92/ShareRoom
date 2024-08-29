package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.manhnt.adapter.DrawerAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.InitDB;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.WidgetManager;
import com.manhnt.database.ShareRoomDatabase;
import com.manhnt.navigationdrawer.BlurActionBarDrawerToggle;
import com.manhnt.navigationdrawer.BlurDrawerLayout;
import com.manhnt.object.Account;
import com.manhnt.object.DrawerItem;
import com.manhnt.object.Province;
import com.manhnt.service.ChatService;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DrawerAdapter.DrawerItemClickListener{

    private BlurDrawerLayout mBlurDrawerLayout;
    private ListView mDrawerListView;
    private ArrayList<DrawerItem> mListDrawer;
    private DrawerAdapter mDrawerAdapter;
    private ArrayList<Province> ListProvince;
    private String[] ListProvinceName;
    private int position_Province_Id = -1;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getWidget();
        getListProvince();
        getExtraBundle();
    }

    @SuppressWarnings("deprecation")
    private void getWidget(){
        mBlurDrawerLayout = (BlurDrawerLayout) findViewById(R.id.drawer_layout);
        if(mBlurDrawerLayout != null) {
            mBlurDrawerLayout.setScrimColor(Color.TRANSPARENT);
        }
        BlurActionBarDrawerToggle mBlurDrawerToggle = new BlurActionBarDrawerToggle(this, mBlurDrawerLayout,
            R.mipmap.ic_launcher, R.string.app_name, R.string.app_name);
        mBlurDrawerLayout.setDrawerListener(mBlurDrawerToggle);
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);
        mListDrawer = new ArrayList<>();
        mListDrawer.add(new DrawerItem(getString(R.string.account)));
        mListDrawer.add(new DrawerItem(getString(R.string.login), R.mipmap.ic_login));
        mListDrawer.add(new DrawerItem(getString(R.string.application)));
        mListDrawer.add(new DrawerItem(getString(R.string.posted), R.mipmap.ic_post));
        mListDrawer.add(new DrawerItem(getString(R.string.favorite), R.mipmap.ic_favorite));
        mListDrawer.add(new DrawerItem(getString(R.string.messages), R.mipmap.ic_chat));
        mListDrawer.add(new DrawerItem(getString(R.string.about), R.mipmap.ic_about));
        mListDrawer.add(new DrawerItem(getString(R.string.setting)));
        mListDrawer.add(new DrawerItem(getString(R.string.setting), R.mipmap.ic_setting));
        mDrawerAdapter = new DrawerAdapter(this, 0, mListDrawer);
        mDrawerAdapter.setItemClickListener(this);
        mDrawerListView.setAdapter(mDrawerAdapter);

        WidgetManager manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.tv_title, true);
        manager.ImageButton(R.id.btn_drawer, this, true);
        manager.ButtonRectangle(R.id.btn_search_rooms, this, true);
        manager.ButtonRectangle(R.id.btn_post_room, this, true);
    }

    private void getListProvince() {
        ShareRoomDatabase db = ShareRoomDatabase.getInstance(MainActivity.this);
        ListProvince = db.getAllProvince();
        if(ListProvince.size() != 0){
            ListProvinceName = new String[ListProvince.size()];
            for(int i = 0; i<ListProvince.size();i++){
                ListProvinceName[i] = ListProvince.get(i).getName();
            }
        }else {
            new InitDB(MainActivity.this, db, ListProvince, ListProvinceName).execute();
        }
    }

    private void getExtraBundle() {
        if(getIntent().getExtras() != null) {
            int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
            if(from > 0){
                mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
                PreferencesManager.getInstance().setMyAccount(this, mAccount);
                if(mAccount != null) {
                    sendNotifyDrawerLayout(0);
                }
                if(from == Config.POST_ROOM_GET_ADDRESS){
                    position_Province_Id = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
                }
            }
        } else {
            mAccount = PreferencesManager.getInstance().getMyAccount(this);
            if(mAccount != null){
                sendNotifyDrawerLayout(0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_drawer:
                mBlurDrawerLayout.openDrawer(mDrawerListView);
                break;
            case R.id.btn_post_room:
                if(Config.isInternetConnect(this, true)){
                    if(Config.isLogin(this, mAccount, true)){
                        DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_city, ListProvinceName,
                            position_Province_Id, true, true, choiceListener).show();
                    }
                }
                break;
            case R.id.btn_search_rooms:
                intent(SearchRoomActivity.class, mAccount);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDrawerItemClickListener(int index) {
        if (mListDrawer.get(index).getTitle() == null) {
            SelectItem(index);
        }
    }

    private void SelectItem(int position){
        switch (position){
            case 1:
                if(Config.isLogin(this, mAccount, false)){
                    intent(ProfileActivity.class, mAccount);
                } else {
                    intent(LoginActivity.class, null);
                }
                break;
            case 3:
                if(Config.isInternetConnect(this, true)){
                    if(Config.isLogin(this, mAccount, true)){
                        intent(MyRoomsActivity.class, mAccount);
                    }
                }
                break;
            case 4:
                if(Config.isLogin(this, mAccount, true)){
                    intent(MyFavoriteRoomActivity.class, mAccount);
                }
                break;
            case 5:
                if(Config.isLogin(this, mAccount, true)){
                    intent(MyConversationsActivity.class, mAccount);
                }
                break;
            case 6 :
                DialogManager.getInstance().InformationDialog(this, R.string.full_app_name,
                    R.string.about_dialog, true).show();
                break;
            case 8 :
                intent(SettingActivity.class, null);
                break;
            case 9:
                DialogManager.getInstance().YesNoDialog(this, R.string.logout, R.string.question_logout,
                    R.string.OK, R.string.back, logOutListener, true).show();
                break;
            default:
                break;
        }
        mBlurDrawerLayout.closeDrawer(mDrawerListView);
    }

    private void sendNotifyDrawerLayout(int notify){
        switch (notify) {
            case 0:
                if(mListDrawer.size() == 9){
                    mListDrawer.add(new DrawerItem(getString(R.string.logout), R.mipmap.ic_logout));
                }
                String name = mAccount.getFirst_name() + " " + mAccount.getLast_name();
                mListDrawer.get(1).setName(name);
                if(!TextUtils.isEmpty(mAccount.getAvatar()) && !mAccount.getAvatar().equals("null")){
                    mListDrawer.get(1).setUrl(mAccount.getAvatar());
                } else {
                    mListDrawer.get(1).setResId(R.drawable.ic_user_male_press);
                }
                mDrawerAdapter.notifyDataSetChanged();
                break;
            case 1:
                if(mListDrawer.size() == 10){
                    mListDrawer.remove(mListDrawer.size() -1);
                }
                mListDrawer.get(1).setName(getString(R.string.login));
                mListDrawer.get(1).setUrl("");
                mListDrawer.get(1).setResId(R.mipmap.ic_login);
                mDrawerAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private DialogManager.YesNoDialogListener logOutListener = new DialogManager.YesNoDialogListener() {
        @Override
        public void onYes(MaterialDialog dialog) {
            if(mAccount.getAccount_type() == Config.ACCOUNT_FACEBOOK){
                LoginManager.getInstance().logOut();
            }
            sendNotifyDrawerLayout(1);
            mAccount = null;
            PreferencesManager.getInstance().setMyAccount(MainActivity.this, null);
            if(Config.isServiceRunning(MainActivity.this, ChatService.class)){
                ((MyApplication)getApplication()).getSocket().disconnect();
                stopService(new Intent(MainActivity.this, ChatService.class));
            }
            dialog.dismiss();
        }

        @Override
        public void onNo(MaterialDialog dialog) {
            dialog.dismiss();
        }
    };

    private DialogManager.ListOneChoiceDialogListener choiceListener = new DialogManager.ListOneChoiceDialogListener() {
        @Override
        public void onChoice(MaterialDialog dialog, int index) {
            if(index != -1){
                position_Province_Id = index;
                intent(PostRoom_GetAddress.class, mAccount);
            }
        }
    };

    private void intent(Class<? extends Activity> clazz, Account mAccount){
        Intent i = new Intent(MainActivity.this, clazz);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(mAccount != null){
            i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
        }
        if(clazz == PostRoom_GetAddress.class){
            i.putExtra(Config.BUNDLE_PROVINCE, ListProvince.get(position_Province_Id));
            i.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_Id);
        }
        i.putExtra(Config.FROM_ACTIVITY, Config.MAIN_ACTIVITY);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

}