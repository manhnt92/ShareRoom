package com.manhnt.navigationdrawer;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

import com.manhnt.shareroom.R;

public class BlurDrawerLayout extends DrawerLayout {

    private BlurActionBarDrawerToggle blurActionBarDrawerToggle;

    public BlurDrawerLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("deprecation")
    public BlurDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BlurDrawerLayout, 0, 0);
        try {
            int blurRadius = ta.getInteger(R.styleable.BlurDrawerLayout_blurRadius, BlurActionBarDrawerToggle.DEFAULT_BLUR_RADIUS);
            float downScaleFactor = ta.getFloat(R.styleable.BlurDrawerLayout_downScaleFactor, BlurActionBarDrawerToggle.DEFAULT_DOWN_SCALE_FACTOR);
            blurActionBarDrawerToggle = new BlurActionBarDrawerToggle((Activity) context, this, R.mipmap.ic_menu, R.string.app_name, R.string.app_name);
            blurActionBarDrawerToggle.setRadius(blurRadius);
            blurActionBarDrawerToggle.setDownScaleFactor(downScaleFactor);
            setDrawerListener(blurActionBarDrawerToggle);
            post(new Runnable() {
                @SuppressWarnings("deprecation")
				@Override
                public void run() {
                    blurActionBarDrawerToggle.syncState();
                }
            });

        } finally {
            ta.recycle();
        }
    }

    public BlurDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
