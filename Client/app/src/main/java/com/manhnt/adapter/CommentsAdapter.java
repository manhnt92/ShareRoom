package com.manhnt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.manhnt.config.Config;
import com.manhnt.object.Comments;
import com.manhnt.shareroom.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import java.util.ArrayList;
import java.util.HashMap;
import github.ankushsachdeva.emojicon.EmojiconTextView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentHolder>{

    private ArrayList<Comments> list_comment;
    private ImageLoader mImageLoader;
    private HashMap<Integer, Bitmap> hmAvatar;
    private Typeface font;

    public CommentsAdapter(Context context, ArrayList<Comments> objects) {
        this.list_comment = objects;
        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_user_male_press)
            .showImageForEmptyUri(R.drawable.ic_user_male_press)
            .showImageOnFail(R.drawable.ic_user_male_press)
            .bitmapConfig(Bitmap.Config.ARGB_8888).imageScaleType(ImageScaleType.EXACTLY)
            .displayer(new RoundedBitmapDisplayer(10)).build();
        mImageLoader = ImageLoader.getInstance();
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context)
            .defaultDisplayImageOptions(options)
            .diskCacheExtraOptions(200, 200, null)
            .memoryCache(new WeakMemoryCache()).build());
        hmAvatar = new HashMap<>();
        font = Config.getTypeface(context.getAssets());
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_item, parent, false);
        return new CommentHolder(itemLayoutView);
    }

    public interface OnLongItemClickListener{
        void onLongClick(int index);
    }

    private OnLongItemClickListener onLongItemClickListener;

    public void setOnLongItemClickListener(OnLongItemClickListener onLongItemClickListener) {
        this.onLongItemClickListener = onLongItemClickListener;
    }

    @Override
    public void onBindViewHolder(final CommentHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.ll_root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongItemClickListener.onLongClick(position);
                return true;
            }
        });
        holder.comment_username.setText(list_comment.get(position).getUserName());
        holder.comment.setText(Config.convertHexStringToString(list_comment.get(position).getComment()));
        holder.comment_time.setText(list_comment.get(position).getCommentTime());
        if(!list_comment.get(position).getAvatar().equalsIgnoreCase("")
            && !list_comment.get(position).getAvatar().equalsIgnoreCase("null")){
            Bitmap bm = hmAvatar.get(list_comment.get(position).getUser_id());
            if(bm != null){
                holder.comment_avatar.setImageBitmap(bm);
            } else {
                mImageLoader.loadImage(list_comment.get(position).getAvatar(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {}

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {}

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        hmAvatar.put(list_comment.get(position).getUser_id(), bitmap);
                        holder.comment_avatar.setImageBitmap(bitmap);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {}
                });
            }
        } else {
            holder.comment_avatar.setImageResource(R.drawable.ic_user_male_press);
        }
    }

    @Override
    public int getItemCount() {
        return list_comment.size();
    }

    public class CommentHolder extends RecyclerView.ViewHolder{
        LinearLayout ll_root;
        TextView comment_username;
        TextView comment_time;
        EmojiconTextView comment;
        ImageView comment_avatar;

        public CommentHolder(View itemView) {
            super(itemView);
            ll_root = (LinearLayout) itemView.findViewById(R.id.ll_root);
            comment_username = (TextView) itemView.findViewById(R.id.comment_username);
            comment_username.setTypeface(font);
            comment = (EmojiconTextView) itemView.findViewById(R.id.comment);
            comment.setTypeface(font);
            comment_time = (TextView) itemView.findViewById(R.id.comment_time);
            comment_time.setTypeface(font);
            comment_avatar = (ImageView) itemView.findViewById(R.id.comment_avatar);
        }
    }

}
