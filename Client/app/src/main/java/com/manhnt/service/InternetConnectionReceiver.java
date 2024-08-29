package com.manhnt.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.manhnt.config.Config;
import com.manhnt.object.Account;

import org.json.JSONException;
import org.json.JSONObject;

public class InternetConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Account mAccount = getSharedPreference(context);
        if(mAccount != null) {
            if (Config.isInternetConnect(context, false)) {
                if (!Config.isServiceRunning(context, ChatService.class)) {
                    context.startService(new Intent(context, ChatService.class));
                }
            } else {
                if (Config.isServiceRunning(context, ChatService.class)) {
                    context.stopService(new Intent(context, ChatService.class));
                }
            }
        }
    }

    private Account getSharedPreference(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        String jObj = pref.getString(Config.PREF_ACCOUNT, null);
        if(jObj != null){
            try {
                return Config.convertJsonToAccount(new JSONObject(jObj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
