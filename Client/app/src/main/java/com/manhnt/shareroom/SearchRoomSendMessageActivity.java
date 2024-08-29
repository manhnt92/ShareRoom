package com.manhnt.shareroom;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.adapter.ChatAdapter;
import com.manhnt.config.Config;
import com.manhnt.object.Account;
import com.manhnt.object.ChatMessage;
import com.manhnt.widget.BlurBehind;
import com.manhnt.widget.EmojiconWidget;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SearchRoomSendMessageActivity extends Activity {

    private Account mAccount;
    private ArrayList<ChatMessage> list_chat;
    private RecyclerView lv;
    private ChatAdapter adapter;
    private int chats_id;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.IS_PUSH_NOTIFICATION = false;
        Config.CHAT_WITH = Config.ACCOUNT_POST.getId();
        setContentView(R.layout.search_room_send_message_activity);
        BlurBehind.getInstance().setBackground(this);
        getExtraBundle();
        getWidget();
        getSocket();
    }

    private void getExtraBundle(){
        mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
    }

    private void getSocket(){
        mSocket = ((MyApplication) getApplicationContext()).getSocket();
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(Config.SOCKET_USER1_ID, mAccount.getId());
            jObj.put(Config.SOCKET_USER2_ID, Config.ACCOUNT_POST.getId());
            mSocket.emit(Config.SOCKET_CONVERSATION, jObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.off(Config.SOCKET_CONVERSATION);
        mSocket.off(Config.SOCKET_PRIVATE_MESSAGE);
        mSocket.on(Config.SOCKET_CONVERSATION, ConversationListener);
        mSocket.on(Config.SOCKET_PRIVATE_MESSAGE, PrivateMessageListener);
    }

    private void getWidget(){
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
                        jObj.put(Config.TO_ID, Config.ACCOUNT_POST.getId());
                        jObj.put(Config.MESSAGE, hex_result);
                        mSocket.emit(Config.SOCKET_PRIVATE_MESSAGE, jObj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void scrollToBottom(){
        lv.smoothScrollToPosition(list_chat.size() - 1);
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
                            Config.showCustomToast(SearchRoomSendMessageActivity.this, 0, message);
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
                        adapter.notifyDataSetChanged();
                        scrollToBottom();
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
