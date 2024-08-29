package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.adapter.ChatAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.ChatMessage;
import com.manhnt.object.Conversation;
import com.manhnt.widget.EmojiconWidget;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class ChatActivity extends Activity implements View.OnClickListener {

    private RecyclerView lv;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> list_chat;
    private Account mAccount;
    private Socket mSocket;
    private int To_User_ID, To_User_Status;
    private String To_User_Name, To_Phone_Number;
    private int chats_id;
    private int call_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.IS_PUSH_NOTIFICATION = false;
        setContentView(R.layout.chat_activity);
        mAccount = PreferencesManager.getInstance().getMyAccount(this);
        getExtraBundle();
        getSocket();
        getWidget();
    }

    private void getSocket(){
        mSocket = ((MyApplication) getApplication()).getSocket();
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(Config.SOCKET_USER1_ID, mAccount.getId());
            jObj.put(Config.SOCKET_USER2_ID, To_User_ID);
            mSocket.emit(Config.SOCKET_CONVERSATION, jObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.off(Config.SOCKET_CONVERSATION);
        mSocket.off(Config.SOCKET_PRIVATE_MESSAGE);
        mSocket.off(Config.SOCKET_PING_BEFORE_CALL);
        mSocket.on(Config.SOCKET_CONVERSATION, ConversationListener);
        mSocket.on(Config.SOCKET_PRIVATE_MESSAGE, PrivateMessageListener);
        mSocket.on(Config.SOCKET_PING_BEFORE_CALL, PingBeforeCallListener);

        mSocket.emit(Config.SOCKET_GET_PHONE_NUMBER, To_User_ID);
        mSocket.off(Config.SOCKET_GET_PHONE_NUMBER);
        mSocket.on(Config.SOCKET_GET_PHONE_NUMBER, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jObj = (JSONObject) args[0];
                        To_Phone_Number = jObj.optString(Config.PHONENUMBER);
                    }
                });
            }
        });
    }

    private void getExtraBundle(){
        int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
        switch (from){
            case Config.MY_CONVERSATION_ACTIVITY:
                Conversation conversation = (Conversation) getIntent().getExtras()
                    .getSerializable(Config.SOCKET_CONVERSATION);
                assert conversation != null;
                To_User_ID = conversation.getUser_id();
                To_User_Name = conversation.getUser_name();
                To_User_Status = conversation.getStatus();
                Config.CHAT_WITH = To_User_ID;
                break;
            case Config.CHAT_BROADCAST_RECEIVER:
                ChatMessage chatMessage = (ChatMessage) getIntent().getExtras()
                    .getSerializable(Config.BUNDLE_CHAT_MESSAGE);
                assert chatMessage != null;
                To_User_ID = chatMessage.getFrom_id();
                To_User_Name = chatMessage.getUserName();
                To_User_Status = 1;
                Config.CHAT_WITH = To_User_ID;
                break;
            default:
                break;
        }
    }

    private void getWidget(){
        WidgetManager manager = WidgetManager.getInstance(this);
        TextView txt_user_name = manager.TextView(R.id.txt_username, true);
        txt_user_name.setText(To_User_Name);
        TextView txt_status = manager.TextView(R.id.txt_status, true);
        if(To_User_Status == Config.ONLINE){
            txt_status.setText(getResources().getString(R.string.online));
        } else {
            txt_status.setText(getResources().getString(R.string.offline));
        }
        manager.ImageButton(R.id.btn_back, this, true);
        manager.ImageButton(R.id.btn_call, this, true);
        lv = (RecyclerView) findViewById(R.id.messagesListView);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        lv.setLayoutManager(llManager);
        list_chat = new ArrayList<>();
        adapter = new ChatAdapter(this, list_chat, mAccount);
        lv.setAdapter(adapter);

        final EmojiconWidget widget = new EmojiconWidget(this, EmojiconWidget.LINEAR_LAYOUT, R.id.root_view,
            R.id.btn_emojicon, R.id.edt_message, R.id.btn_send_message, true);
        widget.setSendListener(new EmojiconWidget.SendListener() {
            @Override
            public void onBtnSendClick(View view) {
                MaterialEditText edt_message = widget.getEditText();
                String message = edt_message.getText().toString();
                String hex_result = Config.convertStringToHexString(message);
                edt_message.setText("");
                if(!TextUtils.isEmpty(hex_result)){
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put(Config.CHAT_ID, chats_id);
                        jObj.put(Config.FROM_ID, mAccount.getId());
                        jObj.put(Config.TO_ID, To_User_ID);
                        jObj.put(Config.MESSAGE, hex_result);
                        mSocket.emit(Config.SOCKET_PRIVATE_MESSAGE, jObj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                Intent i = new Intent(ChatActivity.this, MyConversationsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                Config.IS_PUSH_NOTIFICATION = true;
                Config.CHAT_WITH = 0;
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
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ChatActivity.this, MyConversationsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void scrollToBottom(){
        if(list_chat.size() > 0) {
            lv.smoothScrollToPosition(list_chat.size() - 1);
        }
    }

    private void CallViaPhoneNumber(){
        if(Config.isLogin(ChatActivity.this, mAccount, true)) {
            if(!TextUtils.isEmpty(To_Phone_Number) && !To_Phone_Number.equalsIgnoreCase("null")
                    && !To_Phone_Number.equalsIgnoreCase(getString(R.string.no_content))) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + To_Phone_Number));
                startActivity(callIntent);
            } else {
                Config.showCustomToast(ChatActivity.this, 0, getString(R.string.no_info) + " " +
                    getString(R.string.phonenumber) + " " + To_User_Name);
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
                jObj.put(Config.TO_ID, To_User_ID);
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
                jObj.put(Config.TO_ID, To_User_ID);
                mSocket.emit(Config.SOCKET_PING_BEFORE_CALL, jObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Emitter.Listener ConversationListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = new JSONObject(args[0].toString());
                        if(jObj.optBoolean(Config.DATA)){
                            JSONArray jArray = jObj.optJSONArray(Config.MESSAGE);
                            for( int i = 0; i< jArray.length(); i++) {
                                JSONObject obj = jArray.optJSONObject(i);
                                ChatMessage chatMessage = Config.convertJsonObjectToChatMessage(obj);
                                list_chat.add(chatMessage);
                            }
                            chats_id = list_chat.get(0).getChat_id();
                            adapter.notifyDataSetChanged();
                            scrollToBottom();
                        } else {
                            chats_id = jObj.optInt(Config.CHAT_ID);
                            String message = jObj.optString(Config.MESSAGE);
                            Config.showCustomToast(ChatActivity.this, 0, message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener PrivateMessageListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = new JSONObject(args[0].toString());
                        ChatMessage chatMessage = Config.convertJsonObjectToChatMessage(jObj);
                        boolean isUpdate = false;
                        for(int i = 0; i < list_chat.size(); i++){
                            if(list_chat.get(i).getId() == chatMessage.getId()){
                                list_chat.set(i, chatMessage);
                                isUpdate = true;
                                break;
                            }
                        }
                        if(!isUpdate){
                            list_chat.add(chatMessage);
                        }
                        scrollToBottom();
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
                            Intent i = new Intent(ChatActivity.this, clazz);
                            i.putExtra(Config.FROM_ACTIVITY, Config.CHAT_ACTIVITY);
                            i.putExtra(Config.BUNDLE_CALL_SOCKET_ID, To_socketID);
                            i.putExtra(Config.BUNDLE_CALL_USER_NAME, To_User_Name);
                            i.putExtra(Config.BUNDLE_IS_CALLER, true);
                            startActivity(i);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            String message = getString(R.string.call_not_establish) + " " + To_User_Name;
                            Config.showCustomToast(ChatActivity.this, 0, message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        Config.IS_PUSH_NOTIFICATION = true;
        Config.CHAT_WITH = 0;
        super.onDestroy();
    }

}
