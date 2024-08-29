package com.manhnt.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.rebound.SpringConfig;
import com.manhnt.object.Account;
import com.manhnt.object.Filter;

import org.json.JSONException;
import org.json.JSONObject;

public class PreferencesManager {

    private static PreferencesManager instance;
    public static final int SETTING_ANIMATION = 0;
    public static final int SETTING_FONT = 1;

    private PreferencesManager(){}

    public static synchronized PreferencesManager getInstance(){
        if(instance == null){
            instance = new PreferencesManager();
        }
        return instance;
    }

    public Account getMyAccount(Context context){
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

    @SuppressLint("CommitPrefEdits")
    public void setMyAccount(Context context, Account account){
        SharedPreferences pref = context.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = pref.edit();
        edt.putString(Config.PREF_ACCOUNT, account != null ? Config.convertAccountToString(account) : null);
        edt.commit();
    }

    public void getSetting(Context context){
        SharedPreferences pref = context.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        Config.ANIMATION_ENABLE = pref.getBoolean(Config.PREF_SCALE_ANIMATION_ENABLE, true);
        Config.MIN_SCALE_PREFERENCE = pref.getFloat(Config.PREF_SCALE_ANIMATION_MIN_SCALE, 0.95f);
        if(Config.ANIMATION_ENABLE) {
            Config.MIN_SCALE = Config.MIN_SCALE_PREFERENCE;
        } else {
            Config.MIN_SCALE = 1.0f;
        }
        Config.TENSION = pref.getInt(Config.PREF_SCALE_ANIMATION_TENSION, 80);
        Config.FRICTION = pref.getInt(Config.PREF_SCALE_ANIMATION_FRICTION, 3);
        Config.SCALE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(Config.TENSION, Config.FRICTION);
        Config.FONT = pref.getString(Config.PREF_FONT_NAME, "fonts/Default.ttf");
        Config.POSITION_FONT_NAME = pref.getInt(Config.PREF_POSITION_FONT_NAME, 0);
    }

    @SuppressLint("CommitPrefEdits")
    public void setSetting(Context context, int type){
        SharedPreferences pref = context.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(type == SETTING_ANIMATION) {
            editor.putBoolean(Config.PREF_SCALE_ANIMATION_ENABLE, Config.ANIMATION_ENABLE);
            editor.putFloat(Config.PREF_SCALE_ANIMATION_MIN_SCALE, Config.MIN_SCALE_PREFERENCE);
            editor.putInt(Config.PREF_SCALE_ANIMATION_FRICTION, Config.FRICTION);
            editor.putInt(Config.PREF_SCALE_ANIMATION_TENSION, Config.TENSION);
        } else if (type == SETTING_FONT){
            editor.putString(Config.PREF_FONT_NAME, Config.FONT);
            editor.putInt(Config.PREF_POSITION_FONT_NAME, Config.POSITION_FONT_NAME);
        }
        editor.commit();
    }

    public Filter getFilterResult(Context context){
        SharedPreferences pref = context.getSharedPreferences(Config.PREF_FILTER, Context.MODE_PRIVATE);
        float rent_min = pref.getFloat(Config.RENT_MIN, 0.0f);
        float rent_max = pref.getFloat(Config.RENT_MAX, 10.0f);
        float electric_min = pref.getFloat(Config.ELECTRIC_MIN, 0.0f);
        float electric_max = pref.getFloat(Config.ELECTRIC_MAX, 10.0f);
        int water_min = pref.getInt(Config.WATER_MIN, 0);
        int water_max = pref.getInt(Config.WATER_MAX, 100);
        int area_min = pref.getInt(Config.AREA_MIN, 0);
        int area_max = pref.getInt(Config.AREA_MAX, 100);
        int person_min = pref.getInt(Config.PERSON_MIN, 0);
        int person_max = pref.getInt(Config.PERSON_MAX, 10);
        return new Filter(rent_min, rent_max, electric_min, electric_max, water_min, water_max,
            area_min, area_max, person_min, person_max);
    }

    @SuppressLint("CommitPrefEdits")
    public void setFilterResult(Context context, float rent_min, float rent_max, float electric_min,
        float electric_max, int water_min, int water_max, int area_min, int area_max, int person_min,
        int person_max){
        SharedPreferences pref = context.getSharedPreferences(Config.PREF_FILTER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putFloat(Config.RENT_MIN, rent_min);
        editor.putFloat(Config.RENT_MAX, rent_max);
        editor.putFloat(Config.ELECTRIC_MIN, electric_min);
        editor.putFloat(Config.ELECTRIC_MAX, electric_max);
        editor.putInt(Config.WATER_MIN, water_min);
        editor.putInt(Config.WATER_MAX, water_max);
        editor.putInt(Config.AREA_MIN, area_min);
        editor.putInt(Config.AREA_MAX, area_max);
        editor.putInt(Config.PERSON_MIN, person_min);
        editor.putInt(Config.PERSON_MAX, person_max);
        editor.commit();
    }

}
