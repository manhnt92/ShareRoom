package com.manhnt.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.manhnt.config.Config;
import com.manhnt.object.Conversation;
import com.manhnt.shareroom.ChatActivity;
import com.manhnt.shareroom.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationHolder>{

    private Context mContext;
    private ArrayList<Conversation> list_conversation;
    private DisplayImageOptions	options;
    private ImageLoader	mImageLoader;
    private Spring scaleSpring;
    private View currentView;

    public ConversationAdapter(Context context, ArrayList<Conversation> list_conversation){
        this.mContext = context;
        this.list_conversation = list_conversation;
        options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.ic_empty_icon)
            .showImageForEmptyUri(R.mipmap.ic_empty_icon)
            .showImageOnFail(R.mipmap.ic_empty_icon)
            .cacheInMemory(true).cacheOnDisk(true)
            .bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888).imageScaleType(ImageScaleType.EXACTLY)
            .displayer(new RoundedBitmapDisplayer(10)).build();
        mImageLoader = ImageLoader.getInstance();
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context)
            .defaultDisplayImageOptions(options)
            .diskCacheExtraOptions(200, 200, null)
            .memoryCache(new WeakMemoryCache()).build());
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
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_conversation_recycle_view_item, parent, false);
        return new ConversationHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, int position) {
        holder.txt_username.setText(list_conversation.get(position).getUser_name());
        if(list_conversation.get(position).getStatus() == Config.ONLINE) {
            holder.img_status.setImageResource(R.mipmap.ic_online);
        } else {
            holder.img_status.setImageResource(R.mipmap.ic_offline);
        }
        if(!list_conversation.get(position).getUser_avatar().isEmpty() && !list_conversation.get(position).getUser_avatar().equalsIgnoreCase("null")){
            mImageLoader.displayImage(list_conversation.get(position).getUser_avatar(), holder.user_avatar, options);
        } else {
            holder.user_avatar.setImageResource(R.drawable.ic_user_male_press);
        }
    }

    @Override
    public int getItemCount() {
        return list_conversation.size();
    }

    public class ConversationHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout root_view;
        ImageView user_avatar, img_status;
        TextView txt_username;

        public ConversationHolder(View itemView) {
            super(itemView);
            root_view = (LinearLayout) itemView.findViewById(R.id.root_view);
            user_avatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            txt_username = (TextView) itemView.findViewById(R.id.txt_username);
            txt_username.setTypeface(Config.getTypeface(mContext.getAssets()));
            img_status = (ImageView) itemView.findViewById(R.id.img_status);
            root_view.setOnClickListener(this);
            root_view.setOnTouchListener(new View.OnTouchListener() {
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
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View view) {
            Intent i = new Intent(mContext, ChatActivity.class);
            int position = this.getPosition();
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra(Config.SOCKET_CONVERSATION, list_conversation.get(position));
            i.putExtra(Config.FROM_ACTIVITY, Config.MY_CONVERSATION_ACTIVITY);
            mContext.startActivity(i);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
