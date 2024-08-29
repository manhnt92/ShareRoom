package com.manhnt.shareroom;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.config.Config;
import com.manhnt.config.PreferencesManager;
import com.manhnt.object.Account;
import com.manhnt.service.ChatService;
import com.manhnt.service.InternetConnectionReceiver;
import java.net.URISyntaxException;

public class MyApplication extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Config.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        Account mAccount = PreferencesManager.getInstance().getMyAccount(base);
        PreferencesManager.getInstance().getSetting(this);
        if(mAccount != null) {
            if (Config.isInternetConnect(base, false)) {
                if (!Config.isServiceRunning(base, ChatService.class)) {
                    base.startService(new Intent(base, ChatService.class));
                }
            } else {
                if (Config.isServiceRunning(base, ChatService.class)) {
                    base.stopService(new Intent(base, ChatService.class));
                }
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new InternetConnectionReceiver(), intentFilter);
    }


    public Socket getSocket(){
        return mSocket;
    }

}
