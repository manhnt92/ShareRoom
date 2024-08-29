package com.manhnt.shareroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.rebound.SpringConfig;
import com.gc.materialdesign.views.Slider;
import com.kyleduo.switchbutton.SwitchButton;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.WidgetManager;

public class SettingActivity extends Activity implements View.OnClickListener {

    private TextView title, txt_animation, txt_animation_content, txt_animation_scale,
        txt_animation_scale_content, txt_animation_friction, txt_animation_friction_content,
        txt_animation_tension, txt_animation_tension_content, txt_font, txt_font_content;
    private String[] fonts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        getWidget();
    }

    @SuppressLint("SetTextI18n")
    private void getWidget(){
        final WidgetManager manager = WidgetManager.getInstance(this);
        title = manager.TextView(R.id.title, true);
        manager.ImageButton(R.id.btn_back, this, true);
        txt_animation = manager.TextView(R.id.txt_animation, true);
        txt_animation_content = manager.TextView(R.id.txt_animation_content, true);
        SwitchButton switchButton = manager.SwitchButton(R.id.sb_animation, true);
        final LinearLayout ll_scale = (LinearLayout) findViewById(R.id.ll_scale);
        txt_animation_scale = manager.TextView(R.id.txt_animation_scale, true);
        txt_animation_scale_content = manager.TextView(R.id.txt_animation_scale_content, true);
        final Slider slider_scale = manager.Slider(R.id.slider_scale, true);
        final LinearLayout ll_friction = (LinearLayout) findViewById(R.id.ll_friction);
        txt_animation_friction = manager.TextView(R.id.txt_animation_friction, true);
        txt_animation_friction_content = manager.TextView(R.id.txt_animation_friction_content, true);
        Slider slider_friction = manager.Slider(R.id.slider_friction, true);
        final LinearLayout ll_tension = (LinearLayout) findViewById(R.id.ll_tension);
        txt_animation_tension = manager.TextView(R.id.txt_animation_tension, true);
        txt_animation_tension_content = manager.TextView(R.id.txt_animation_tension_content, true);
        Slider slider_tension = manager.Slider(R.id.slider_tension, true);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    Config.ANIMATION_ENABLE = true;
                    Config.MIN_SCALE = Config.MIN_SCALE_PREFERENCE;
                    txt_animation_content.setText(getResources().getString(R.string.turn_on_anim));
                    saveAnimationToPreference();
                    slider_scale.setValue((int)(Config.MIN_SCALE * 100));
                    txt_animation_scale_content.setText("" + (int)(Config.MIN_SCALE * 100));
                    ll_scale.setVisibility(View.VISIBLE);
                    ll_friction.setVisibility(View.VISIBLE);
                    ll_tension.setVisibility(View.VISIBLE);
                } else {
                    Config.MIN_SCALE = 1.0f;
                    Config.ANIMATION_ENABLE = false;
                    txt_animation_content.setText(getResources().getString(R.string.turn_off_anim));
                    saveAnimationToPreference();
                    ll_scale.setVisibility(View.GONE);
                    ll_friction.setVisibility(View.GONE);
                    ll_tension.setVisibility(View.GONE);
                }
            }
        });
        switchButton.setChecked(Config.ANIMATION_ENABLE);
        if(Config.ANIMATION_ENABLE) {
            txt_animation_content.setText(getResources().getString(R.string.turn_on_anim));
            ll_scale.setVisibility(View.VISIBLE);
            ll_friction.setVisibility(View.VISIBLE);
            ll_tension.setVisibility(View.VISIBLE);
        } else {
            txt_animation_content.setText(getResources().getString(R.string.turn_off_anim));
            ll_scale.setVisibility(View.GONE);
            ll_friction.setVisibility(View.GONE);
            ll_tension.setVisibility(View.GONE);
        }
        txt_animation_scale_content.setText("" + (int)(Config.MIN_SCALE * 100) + " %");
        txt_animation_friction_content.setText("" + Config.FRICTION);
        txt_animation_tension_content.setText("" + Config.TENSION);
        slider_scale.setValue((int)(Config.MIN_SCALE_PREFERENCE * 100));
        slider_friction.setValue(Config.FRICTION);
        slider_tension.setValue(Config.TENSION);
        slider_scale.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                Config.MIN_SCALE = (float) value / 100;
                Config.MIN_SCALE_PREFERENCE = Config.MIN_SCALE;
                txt_animation_scale_content.setText("" + value + " %");
                saveAnimationToPreference();
            }
        });
        slider_friction.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                Config.FRICTION = value;
                txt_animation_friction_content.setText(""+ value);
                Config.SCALE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(Config.TENSION, Config.FRICTION);
                manager.updateScaleConfig();
                saveAnimationToPreference();
            }
        });
        slider_tension.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                Config.TENSION = value;
                txt_animation_tension_content.setText("" + value);
                Config.SCALE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(Config.TENSION, Config.FRICTION);
                manager.updateScaleConfig();
                saveAnimationToPreference();
            }
        });
        txt_font = manager.TextView(R.id.txt_font, true);
        txt_font_content = manager.TextView(R.id.txt_font_content, this, true);
        txt_font_content.setText(Config.FONT.replace("fonts/","").replace(".ttf",""));
        fonts = getResources().getStringArray(R.array.list_fonts);
    }

    private void saveAnimationToPreference(){
        PreferencesManager.getInstance().setSetting(this, PreferencesManager.SETTING_ANIMATION);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SettingActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                Intent intent_back = new Intent(SettingActivity.this, MainActivity.class);
                intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent_back);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.txt_font_content:
                DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_font,
                    fonts, -1, false, true, SettingFontListener).show();
                break;
            default:
                break;
        }
    }

    private void changeFont(){
        title.setTypeface(Config.getTypeface(getAssets()));
        txt_animation.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_content.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_scale.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_scale_content.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_friction.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_friction_content.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_tension.setTypeface(Config.getTypeface(getAssets()));
        txt_animation_tension_content.setTypeface(Config.getTypeface(getAssets()));
        txt_font.setTypeface(Config.getTypeface(getAssets()));
        txt_font_content.setTypeface(Config.getTypeface(getAssets()));
    }

    private DialogManager.ListOneChoiceDialogListener SettingFontListener = new DialogManager
        .ListOneChoiceDialogListener() {
        @Override
        public void onChoice(MaterialDialog dialog, int index) {
            if(index != -1){
                Config.POSITION_FONT_NAME = index;
                Config.FONT = "fonts/" + fonts[index] + ".ttf";
                txt_font_content.setText(fonts[index]);
                changeFont();
                PreferencesManager.getInstance().setSetting(SettingActivity.this,
                    PreferencesManager.SETTING_FONT);
            }
        }
    };

}
