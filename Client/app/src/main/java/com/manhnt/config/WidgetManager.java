package com.manhnt.config;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.internal.MDButton;
import com.appyvet.rangebar.RangeBar;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.login.widget.LoginButton;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.Slider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.kyleduo.switchbutton.SwitchButton;
import com.manhnt.object.Image;
import com.manhnt.shareroom.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import java.util.ArrayList;

import fr.ganfra.materialspinner.MaterialSpinner;

public class WidgetManager implements SpringListener{

    private static WidgetManager instance;
    private static Typeface font;
    private static Activity context;
    private Spring scaleSpring;
    private View currentView;

    private WidgetManager() {
        SpringSystem springSystem = SpringSystem.create();
        scaleSpring = springSystem.createSpring();
        scaleSpring.setCurrentValue(Config.MAX_SCALE).setAtRest();
        scaleSpring.addListener(this);
        scaleSpring.setSpringConfig(Config.SCALE_CONFIG);
    }

    public static synchronized WidgetManager getInstance(Activity context) {
        if (instance == null) {
            instance = new WidgetManager();
        }
        WidgetManager.context = context;
        if(context != null) {
            font = Config.getTypeface(context.getAssets());
        }
        return instance;
    }

    public void setFont(Typeface font){
        WidgetManager.font = font;
    }

    public void updateScaleConfig(){
        scaleSpring.setSpringConfig(Config.SCALE_CONFIG);
    }

    public LinearLayout LinearLayout(int resID, View.OnClickListener onClick, boolean isAnim){
        LinearLayout ll = (LinearLayout) context.findViewById(resID);
        ll.setOnClickListener(onClick);
        if(isAnim){
            ll.setOnTouchListener(onTouch);
        }
        return ll;
    }

    public TextView TextView(int resID, boolean isAnim) {
        TextView tv = (TextView) context.findViewById(resID);
        tv.setTypeface(font);
        if(isAnim){
            tv.setOnClickListener(onClick);
            tv.setOnTouchListener(onTouch);
        }
        return tv;
    }

    public TextView TextView(int resID, View.OnClickListener onClick, boolean isAnim){
        TextView tv = (TextView) context.findViewById(resID);
        tv.setTypeface(font);
        tv.setOnClickListener(onClick);
        if(isAnim){
            tv.setOnTouchListener(onTouch);
        }
        return tv;
    }

    public TextView TextView(View view, int resID, boolean isAnim){
        TextView tv = (TextView) view.findViewById(resID);
        tv.setTypeface(font);
        if(isAnim){
            tv.setOnClickListener(onClick);
            tv.setOnTouchListener(onTouch);
        }
        return tv;
    }

    public EditText EditText(View view, int resID, boolean isAnim){
        EditText edt = (EditText) view.findViewById(resID);
        edt.setTypeface(font);
        if(isAnim){
            edt.setOnClickListener(onClick);
            edt.setOnTouchListener(onTouch);
        }
        return edt;
    }

    public ButtonRectangle ButtonRectangle(int resID, View.OnClickListener onClick, boolean isAnim) {
        ButtonRectangle btn = (ButtonRectangle) context.findViewById(resID);
        btn.getTextButton().setTypeface(font);
        btn.setOnClickListener(onClick);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public ButtonRectangle ButtonRectangle(View view, int resID, View.OnClickListener onClick, boolean isAnim) {
        ButtonRectangle btn = (ButtonRectangle) view.findViewById(resID);
        btn.getTextButton().setTypeface(font);
        btn.setOnClickListener(onClick);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public ImageButton ImageButton(int resID, View.OnClickListener onClick, boolean isAnim) {
        ImageButton btn = (ImageButton) context.findViewById(resID);
        btn.setOnClickListener(onClick);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public ImageButton ImageButton(View view, int resID, boolean isAnim) {
        ImageButton btn = (ImageButton) view.findViewById(resID);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public ImageButton ImageButton(View view, int resID, View.OnClickListener onClick, boolean isAnim) {
        ImageButton btn = (ImageButton) view.findViewById(resID);
        btn.setOnClickListener(onClick);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public MDButton MDButton(View view, int resID, boolean isAnim){
        MDButton btn = (MDButton) view.findViewById(resID);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public MaterialEditText MaterialEditText(int resID, boolean isAnim) {
        MaterialEditText edt = (MaterialEditText) context.findViewById(resID);
        edt.setCustomTypeface(font);
        if (isAnim){
            edt.setOnTouchListener(onTouch);
        }
        return edt;
    }

    public MaterialEditText MaterialEditText(View view, int resID, boolean isAnim) {
        MaterialEditText edt = (MaterialEditText) view.findViewById(resID);
        edt.setCustomTypeface(font);
        if(isAnim){
            edt.setOnTouchListener(onTouch);
        }
        return edt;
    }

    public MaterialEditText MaterialEditText(View view, int resID, View.OnClickListener onClick, boolean isAnim) {
        MaterialEditText edt = (MaterialEditText) view.findViewById(resID);
        edt.setCustomTypeface(font);
        edt.setOnClickListener(onClick);
        if(isAnim){
            edt.setOnTouchListener(onTouch);
        }
        return edt;
    }

    public LoginButton LoginButton(int resID, boolean isAnim) {
        LoginButton btn = (LoginButton) context.findViewById(resID);
        btn.setTypeface(font);
        if(isAnim){
            btn.setOnTouchListener(onTouch);
        }
        return btn;
    }

    public ImageView ImageView(int resID, View.OnClickListener onClick, boolean isAnim) {
        ImageView img = (ImageView) context.findViewById(resID);
        img.setOnClickListener(onClick);
        if(isAnim){
            img.setOnTouchListener(onTouch);
        }
        return img;
    }

    public MaterialSpinner MaterialSpinner(int resID, boolean isAnim){
        MaterialSpinner spinner = (MaterialSpinner) context.findViewById(resID);
        if(isAnim){
            spinner.setOnTouchListener(onTouch);
        }
        return spinner;
    }

    public Slider Slider(int resID, boolean isAnim) {
        Slider slider = (Slider) context.findViewById(resID);
        if(isAnim){
            slider.setOnTouchListener(onTouch);
        }
        return slider;
    }

    public SliderLayout SliderLayout(int resID, boolean isAnim, ArrayList<Image> arr) {
        SliderLayout mSlider = (SliderLayout) context.findViewById(resID);
        if (isAnim) {
            mSlider.setOnClickListener(onClick);
            mSlider.setOnTouchListener(onTouch);
        }
        if (arr.size() > 0) {
            for (int i = 0; i < arr.size(); i++) {
                String path = arr.get(i).getPath();
                TextSliderView textSliderView = new TextSliderView(context, font);
                textSliderView.description(arr.get(i).getNote()).image(path)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
                mSlider.addSlider(textSliderView);
            }
        } else {
            TextSliderView textSliderView = new TextSliderView(context, font);
            textSliderView.description(context.getString(R.string.no_room_image))
                .image(R.mipmap.ic_empty_icon).setScaleType(BaseSliderView.ScaleType.CenterCrop);
            mSlider.addSlider(textSliderView);
        }
        mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(5000);
        return mSlider;
    }

    @SuppressWarnings("deprecation")
    public GoogleMap GoogleMap(int resID) {
        GoogleMap googleMap = ((MapFragment) context.getFragmentManager().findFragmentById(resID)).getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        return googleMap;
    }

    public RangeBar RangeBar(View view, int resID, boolean isAnim){
        RangeBar rangeBar = (RangeBar) view.findViewById(resID);
        rangeBar.setTypeface(font);
        if(isAnim){
            rangeBar.setOnTouchListener(onTouch);
        }
        return rangeBar;
    }

    public SwitchButton SwitchButton(int resID, boolean isAnim){
        SwitchButton switchButton = (SwitchButton) context.findViewById(resID);
        if(isAnim){
            switchButton.setOnTouchListener(onTouch);
        }
        return switchButton;
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {}
    };

    private View.OnTouchListener onTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            currentView = view;
            int action = motionEvent.getAction();
            if(action == MotionEvent.ACTION_DOWN){
                scaleSpring.setEndValue(Config.MIN_SCALE);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
                scaleSpring.setEndValue(Config.MAX_SCALE);
            }
            return false;
        }
    };

    @Override
    public void onSpringUpdate(Spring spring) {
        float currentValue = (float) scaleSpring.getCurrentValue();
        currentView.setScaleX(currentValue);
        currentView.setScaleY(currentValue);
    }

    @Override
    public void onSpringAtRest(Spring spring) {}

    @Override
    public void onSpringActivate(Spring spring) {}

    @Override
    public void onSpringEndStateChange(Spring spring) {}

}
