package com.manhnt.adapter;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.manhnt.object.DrawerItem;
import com.manhnt.shareroom.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

	private Context context;
	private List<DrawerItem> list_data;
	private DisplayImageOptions options;
	private ImageLoader mImageLoader;
	private DrawerItemClickListener itemClickListener;
	private Spring scaleSpring;
	private View currentView;

	public DrawerAdapter(Context context, int resource, List<DrawerItem> objects) {
		super(context, resource, objects);
		this.context = context;
		this.list_data = objects;
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.mipmap.ic_empty_icon)
			.showImageForEmptyUri(R.mipmap.ic_empty_icon)
			.showImageOnFail(R.mipmap.ic_empty_icon).cacheInMemory(false)
			.cacheOnDisk(true)
			.bitmapConfig(Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(1000)).build();
		mImageLoader = ImageLoader.getInstance();
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context)
			.defaultDisplayImageOptions(options)
			.diskCacheExtraOptions(50, 50, null)
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

	public interface DrawerItemClickListener{

		void onDrawerItemClickListener(int index);

	}

	public void setItemClickListener(DrawerItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	private static class ViewHolder {
		ImageView icon;
		TextView name, title;
		LinearLayout ll_title, ll_item;
		RelativeLayout root_view;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final ViewHolder drawer_holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawer_holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.drawer_item, parent, false);
			drawer_holder.name = (TextView) convertView.findViewById(R.id.name);
			drawer_holder.name.setTypeface(com.manhnt.config.Config.getTypeface(context.getAssets()));
			drawer_holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			drawer_holder.title = (TextView) convertView.findViewById(R.id.title);
			drawer_holder.title.setTypeface(com.manhnt.config.Config.getTypeface(context.getAssets()));
			drawer_holder.ll_title = (LinearLayout) convertView.findViewById(R.id.ll_title);
			drawer_holder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
			drawer_holder.root_view = (RelativeLayout) convertView.findViewById(R.id.root_view);

			drawer_holder.root_view.setOnTouchListener(new View.OnTouchListener() {
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
			drawer_holder.root_view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					itemClickListener.onDrawerItemClickListener(position);
				}

			});
			convertView.setTag(drawer_holder);
		} else {
			drawer_holder = (ViewHolder) convertView.getTag();
		}
		DrawerItem dItem = list_data.get(position);
		if (dItem.getTitle() != null) {
			if(!dItem.getTitle().equals("")){
			drawer_holder.ll_item.setVisibility(LinearLayout.VISIBLE);
			drawer_holder.ll_item.setVisibility(LinearLayout.GONE);
			drawer_holder.title.setText(dItem.getTitle());
			drawer_holder.title.setTextColor(Color.WHITE);
			}else {
				drawer_holder.ll_title.setVisibility(LinearLayout.GONE);
				drawer_holder.ll_item.setVisibility(LinearLayout.GONE);
			}
		} else {
			if(TextUtils.isEmpty(dItem.getUrl())){
				drawer_holder.ll_title.setVisibility(LinearLayout.GONE);
				drawer_holder.ll_item.setVisibility(LinearLayout.VISIBLE);
				drawer_holder.icon.setImageDrawable(convertView.getResources().getDrawable(dItem.getResId()));
				drawer_holder.name.setText(dItem.getName());
			}else {
				drawer_holder.ll_title.setVisibility(LinearLayout.GONE);
				drawer_holder.ll_item.setVisibility(LinearLayout.VISIBLE);
				mImageLoader.displayImage(dItem.getUrl(),
					drawer_holder.icon, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {

						}

						@Override
						public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

						}
					}, new ImageLoadingProgressListener() {
						@Override
						public void onProgressUpdate(String imageUri, View view, int current, int total) {

						}
					});
				drawer_holder.name.setText(dItem.getName());
			}
		}
		return convertView;
	}

}
