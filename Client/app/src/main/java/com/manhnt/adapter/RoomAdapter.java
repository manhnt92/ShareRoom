package com.manhnt.adapter;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.manhnt.config.Config;
import com.manhnt.object.Room;
import com.manhnt.shareroom.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class RoomAdapter extends ArrayAdapter<Room> {

	private Context mContext;
	private ArrayList<Room> list_room;
	private DisplayImageOptions	options;
	private ImageLoader	mImageLoader;
	private Spring scaleSpring;
	private View currentView;
	
	public RoomAdapter(Context context, int resource, ArrayList<Room> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.list_room = objects;
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.mipmap.ic_empty_icon)
			.showImageForEmptyUri(R.mipmap.ic_empty_icon)
			.showImageOnFail(R.mipmap.ic_empty_icon)
			.bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888).imageScaleType(ImageScaleType.EXACTLY)
			.displayer(new RoundedBitmapDisplayer(10)).build();
		mImageLoader = ImageLoader.getInstance();
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context)
			.defaultDisplayImageOptions(options)
			.diskCacheExtraOptions(200, 200, null)
			.memoryCache(new WeakMemoryCache()).build());
		scaleSpring = SpringSystem.create().createSpring();
		scaleSpring.setCurrentValue(com.manhnt.config.Config.MAX_SCALE).setAtRest();
		scaleSpring.addListener(new SimpleSpringListener(){

			@Override
			public void onSpringUpdate(Spring spring) {
				super.onSpringUpdate(spring);
				if(currentView != null) {
					currentView.setScaleX((float) spring.getCurrentValue());
					currentView.setScaleY((float) spring.getCurrentValue());
				}
			}

		});
		scaleSpring.setSpringConfig(com.manhnt.config.Config.SCALE_CONFIG);
	}

	public interface  OnItemClick {
		void onItemClickListener(int index);
	}

	private OnItemClick itemClickListener;

	public void setItemClickListener(OnItemClick itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	static class ViewHolder {
        TextView txt_room_state;
        TextView txt_room_address;
        TextView txt_room_rent;
        TextView txt_room_area;
        TextView txt_1, txt_2, txt_3, txt_4;
        ImageView img_room;
		LinearLayout root_view;
    }
	
	@SuppressLint("SetTextI18n")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.room_item, parent, false);
            holder = new ViewHolder();
			holder.root_view = (LinearLayout) convertView.findViewById(R.id.root_view);
            holder.txt_room_state = (TextView) convertView.findViewById(R.id.txt_room_state);
            holder.txt_room_state.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_room_address = (TextView) convertView.findViewById(R.id.txt_room_address);
            holder.txt_room_address.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_room_rent = (TextView) convertView.findViewById(R.id.txt_room_rent);
            holder.txt_room_rent.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_room_area = (TextView) convertView.findViewById(R.id.txt_room_area);
            holder.txt_room_area.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.img_room = (ImageView) convertView.findViewById(R.id.img_room);
            holder.txt_1 = (TextView) convertView.findViewById(R.id.txt_1);
            holder.txt_1.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_2 = (TextView) convertView.findViewById(R.id.txt_2);
            holder.txt_2.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_3 = (TextView) convertView.findViewById(R.id.txt_3);
            holder.txt_3.setTypeface(Config.getTypeface(mContext.getAssets()));
            holder.txt_4 = (TextView) convertView.findViewById(R.id.txt_4);
            holder.txt_4.setTypeface(Config.getTypeface(mContext.getAssets()));
			holder.root_view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int getPosition = (Integer) view.getTag();
					itemClickListener.onItemClickListener(getPosition);
				}
			});
			holder.root_view.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					currentView = view;
					if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
						scaleSpring.setEndValue(com.manhnt.config.Config.MIN_SCALE);
					} else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL){
						scaleSpring.setEndValue(com.manhnt.config.Config.MAX_SCALE);
					}
					return false;
				}
			});
            convertView.setTag(holder);
        }else{
        	holder = (ViewHolder) convertView.getTag();
        }
		holder.root_view.setTag(position);
		holder.txt_room_state.setText(mContext.getResources().getString(R.string.txt_room_state) +" : "+ list_room.get(position).getRoom_properties().getRoom_state());
		holder.txt_room_address.setText(mContext.getResources().getString(R.string.address) +" : "+ list_room.get(position).getRoom_address().getAddress());
		holder.txt_room_rent.setText(mContext.getResources().getString(R.string.txt_rent_per_month) +" : "+ list_room.get(position).getRoom_properties().getRent_per_month() + " "+ mContext.getResources().getString(R.string.million_vnd));
		holder.txt_room_area.setText(mContext.getResources().getString(R.string.txt_area) +" : "+ list_room.get(position).getRoom_properties().getArea() + " m\u00B2");
		
		if(list_room.get(position).getRoom_images().getList_images().size() > 0){
			String path = list_room.get(position).getRoom_images().getList_images().get(0).getPath();
			mImageLoader.displayImage(path, holder.img_room, options);
		} else {
			holder.img_room.setImageResource(R.mipmap.ic_empty_icon);
		}
		return convertView;
	}

}
