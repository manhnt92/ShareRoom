package com.manhnt.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.config.Config;
import com.manhnt.config.PreferencesManager;
import com.manhnt.object.Account;
import com.manhnt.object.ChatMessage;
import com.manhnt.shareroom.CallActivity;
import com.manhnt.shareroom.MyApplication;
import com.manhnt.shareroom.VideoCallActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatService extends Service {

    private Account mAccount;
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSocket = ((MyApplication)getApplication()).getSocket();
        mAccount = PreferencesManager.getInstance().getMyAccount(this);
        if(!mSocket.connected()){
            mSocket.connect();
        }
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(Config.ID, mAccount.getId());
            mSocket.emit(Config.SOCKET_USER_INFO, jObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.off(Config.SOCKET_PRIVATE_MESSAGE);
        mSocket.off(Config.SOCKET_UN_READ_MESSAGE);
        mSocket.off(Config.SOCKET_INCOMING_CALL);
        final Handler handlerUserInfo = new Handler();
        Runnable runnableUserInfo = new Runnable() {
            @Override
            public void run() {
                if(mAccount != null && mSocket.connected()){
                    handlerUserInfo.removeCallbacks(this);
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put(Config.ID, mAccount.getId());
                        mSocket.emit(Config.SOCKET_UN_READ_MESSAGE, jObj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.on(Config.SOCKET_PRIVATE_MESSAGE, PrivateMessageListener);
                    mSocket.on(Config.SOCKET_UN_READ_MESSAGE, UnreadMessageListener);
                    mSocket.on(Config.SOCKET_INCOMING_CALL, IncomingCallListener);
                } else {
                    handlerUserInfo.postDelayed(this, 1000);
                }
            }
        };
        runnableUserInfo.run();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mAccount = PreferencesManager.getInstance().getMyAccount(this);
        if(mAccount != null) {
            if(mSocket.connected()) {
                mSocket.disconnect();
            }
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Emitter.Listener PrivateMessageListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = new JSONObject(args[0].toString());
                        ChatMessage chatMessage = Config.convertJsonObjectToChatMessage(jObj);
                        Intent i = new Intent(Config.CHAT_ACTION);
                        i.putExtra(Config.BUNDLE_CHAT_MESSAGE, chatMessage);
                        i.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
                        sendBroadcast(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.run();
        }
    };

    private Emitter.Listener UnreadMessageListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jArray = new JSONArray(args[0].toString());
                        for (int i = 0; i< jArray.length(); i++) {
                            ChatMessage chatMessage = Config.convertJsonObjectToChatMessage(jArray.getJSONObject(i));
                            Intent intent = new Intent(Config.CHAT_ACTION);
                            intent.putExtra(Config.BUNDLE_CHAT_MESSAGE, chatMessage);
                            intent.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
                            sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.run();
        }
    };

    private Emitter.Listener IncomingCallListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Runnable() {
                @Override
                public void run() {
                    JSONObject jObj = (JSONObject) args[0];
                    int call_type = jObj.optInt(Config.SOCKET_CALL_TYPE);
                    Class clazz = (call_type == Config.SOCKET_CALL_AUDIO) ? CallActivity.class : VideoCallActivity.class;
                    Intent i = new Intent(ChatService.this, clazz);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(Config.BUNDLE_CALL_SOCKET_ID, jObj.optString(Config.SOCKET_FROM));
                    i.putExtra(Config.BUNDLE_CALL_USER_NAME, jObj.optString(Config.SOCKET_USER_NAME));
                    i.putExtra(Config.BUNDLE_IS_CALLER, false);
                    startActivity(i);
                }
            }.run();
        }
    };
}
