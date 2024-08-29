package com.manhnt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.manhnt.config.Config;
import com.manhnt.object.ChatMessage;
import com.manhnt.shareroom.R;
import java.util.ArrayList;


public class MessagePagerAdapter extends PagerAdapter{

    private Context mContext;
    private ArrayList<ChatMessage> list_chat_message;
    private Spring scaleSpring;
    private View currentView;

    public MessagePagerAdapter(Context context, ArrayList<ChatMessage> list_chat_message){
        this.mContext = context;
        this.list_chat_message = list_chat_message;
        SpringSystem springSystem = SpringSystem.create();
        scaleSpring = springSystem.createSpring();
        scaleSpring.setCurrentValue(Config.MAX_SCALE).setAtRest();
        scaleSpring.addListener(new SimpleSpringListener(){

            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                float currentValue = (float) spring.getCurrentValue();
                currentView.setScaleX(currentValue);
                currentView.setScaleY(currentValue);
            }

        });
        scaleSpring.setSpringConfig(Config.SCALE_CONFIG);
    }

    @Override
    public int getCount() {
        return list_chat_message.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.message_dialog_item, collection, false);
        TextView txt_message = (TextView) layout.findViewById(R.id.txt_message);
        txt_message.setTypeface(Config.getTypeface(mContext.getAssets()));
        txt_message.setText(Config.convertHexStringToString(list_chat_message.get(position).getMessage()));
        TextView txt_created = (TextView) layout.findViewById(R.id.txt_created);
        txt_created.setTypeface(Config.getTypeface(mContext.getAssets()));
        txt_created.setText(list_chat_message.get(position).getCreated());
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                currentView = view;
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    scaleSpring.setEndValue(Config.MIN_SCALE);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL){
                    scaleSpring.setEndValue(Config.MAX_SCALE);
                }
                return false;
            }
        });
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}