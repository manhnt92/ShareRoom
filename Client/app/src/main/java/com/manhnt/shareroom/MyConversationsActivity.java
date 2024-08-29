package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.adapter.ConversationAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.Conversation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MyConversationsActivity extends Activity implements View.OnClickListener {

    private Socket mSocket;
    private Account mAccount;
    private ArrayList<Conversation> list_conversation;
    private ConversationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_conversations_activity);
        getWidget();
        mSocket = ((MyApplication) getApplication()).getSocket();
        mSocket.off(Config.SOCKET_MY_CONVERSATIONS);
        mAccount = PreferencesManager.getInstance().getMyAccount(this);
        if(mAccount != null) {
            try {
                mSocket.emit(Config.SOCKET_MY_CONVERSATIONS, new JSONObject().put(Config.ID, mAccount.getId()).toString());
                mSocket.on(Config.SOCKET_MY_CONVERSATIONS, MyConversationsListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final Handler handlerPing = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(list_conversation.size() > 0){
                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONArray jArray = new JSONArray();
                        for(int i = 0; i < list_conversation.size(); i++){
                            JSONObject jObj = new JSONObject().put(Config.ID, list_conversation.get(i).getUser_id());
                            jArray.put(jObj);
                        }
                        jsonObject.put(Config.ID, mAccount.getId());
                        jsonObject.put(Config.SOCKET_PING, jArray);
                        mSocket.emit(Config.SOCKET_PING, jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                handlerPing.postDelayed(this, Config.PING_TIME);
            }
        };
        runnable.run();
        mSocket.on(Config.SOCKET_PING, PingListener);
    }

    private void getWidget() {
        WidgetManager manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.title, true);
        manager.ImageButton(R.id.btn_back, this, true);
        RecyclerView lv_conversation = (RecyclerView) findViewById(R.id.lv_conversation);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        lv_conversation.setLayoutManager(llManager);
        list_conversation = new ArrayList<>();
        adapter = new ConversationAdapter(this, list_conversation);
        lv_conversation.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                Intent i = new Intent(MyConversationsActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(MyConversationsActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private Emitter.Listener MyConversationsListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        list_conversation.clear();
                        JSONArray jArray = new JSONArray(args[0].toString());
                        for(int i = 0; i< jArray.length(); i++){
                            Conversation conversation = Config.convertJsonObjectToConversation(jArray.getJSONObject(i));
                            list_conversation.add(conversation);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener PingListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jArray = new JSONArray(args[0].toString());
                        for (int i = 0; i< jArray.length(); i++){
                            JSONObject jObj = jArray.getJSONObject(i);
                            int user_id = jObj.optInt(Config.ID);
                            int status = jObj.optInt(Config.STATUS);
                            for( int j = 0; j < list_conversation.size(); j++){
                                if(user_id == list_conversation.get(j).getUser_id()){
                                    list_conversation.get(j).setStatus(status);
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

}
