package com.manhnt.navigationdrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class BlurActionBarDrawerToggle extends ActionBarDrawerToggle {

    private Context context;
    private DrawerLayout mDrawerLayout;
    private ImageView mBlurredImageView;
    private int mBlurRadius = DEFAULT_BLUR_RADIUS;
    public static int DEFAULT_BLUR_RADIUS = 12;
    private float mDownScaleFactor = DEFAULT_DOWN_SCALE_FACTOR;
    public static float DEFAULT_DOWN_SCALE_FACTOR = 5.0f;
    private boolean prepareToRender = true;
    private boolean isOpening = false;

    public BlurActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.context = activity.getBaseContext();
        this.mDrawerLayout = drawerLayout;
        init();
    }

    private void init() {
        mBlurredImageView = new ImageView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBlurredImageView.setLayoutParams(params);
        mBlurredImageView.setClickable(false);
        mBlurredImageView.setVisibility(View.GONE);
        mBlurredImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mDrawerLayout.post(new Runnable() {

            @Override
            public void run() {
                mDrawerLayout.addView(mBlurredImageView, 1);
            }
        });
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);
        isOpening = slideOffset != 0.f;
        render();
        setAlpha(mBlurredImageView, slideOffset, 100);
    }

    @Override
    public void onDrawerClosed(View view) {
        prepareToRender = true;
        mBlurredImageView.setVisibility(View.GONE);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        super.onDrawerStateChanged(newState);
        if (newState == DrawerLayout.STATE_IDLE && !isOpening) {
            handleRecycle();
        }
    }

    private void render() {
        if (prepareToRender) {
            prepareToRender = false;
            Bitmap bitmap = loadBitmapFromView(mDrawerLayout);
            bitmap = scaleBitmap(bitmap);
            bitmap = Blur.fastBlur(context, bitmap, mBlurRadius, true);
            mBlurredImageView.setVisibility(View.VISIBLE);
            mBlurredImageView.setImageBitmap(bitmap);
        }
    }

    public void setRadius(int radius) {
        mBlurRadius = radius < 1 ? 1 : radius;
    }

    public void setDownScaleFactor(float downScaleFactor) {
        mDownScaleFactor = downScaleFactor < 1 ? 1 : downScaleFactor;
    }

    private void setAlpha(View view, float alpha, long durationMillis) {
        if (Build.VERSION.SDK_INT < 11) {
            final AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            view.startAnimation(animation);
        } else {
            view.setAlpha(alpha);
        }
    }

    private Bitmap loadBitmapFromView(View mView) {
        Bitmap b = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        mView.draw(c);
        return b;
    }

    private Bitmap scaleBitmap(Bitmap myBitmap) {
        int width = (int) (myBitmap.getWidth() / mDownScaleFactor);
        int height = (int) (myBitmap.getHeight() / mDownScaleFactor);
        return Bitmap.createScaledBitmap(myBitmap, width, height, false);
    }

    private void handleRecycle() {
        Drawable drawable = mBlurredImageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null)
                bitmap.recycle();
            mBlurredImageView.setImageBitmap(null);
        }
        prepareToRender = true;
    }

}