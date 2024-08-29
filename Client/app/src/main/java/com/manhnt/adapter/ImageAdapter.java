package com.manhnt.adapter;

import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.manhnt.config.Config;
import com.manhnt.object.Image;
import com.manhnt.shareroom.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ImageAdapter extends ArrayAdapter<Image> {

	private Context	mContext;
	private ArrayList<Image> list_image;
	private DisplayImageOptions	options;
	private ImageLoader	mImageLoader;
	private ImageAdapterClickListener adapterListener;
	private Spring scaleSpring;
	private View currentView;

	public ImageAdapter(Context context, int resource, ArrayList<Image> list_image) {
		super(context, resource, list_image);
		this.mContext = context;
		this.list_image = list_image;
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
	
	public interface ImageAdapterClickListener {
		void onBtnDeleteClickListener(int index);
		void onBtnNoteClickListener(int index);
		void onImageRoomClickListener(int index);
	}

	public void setAdapterListener(ImageAdapterClickListener adapterListener) {
		this.adapterListener = adapterListener;
	}

	static class GridHolder {
		ImageButton btn_delete,btn_note;
		ImageView	image;
		TextView txt_note_img;
		FrameLayout item_root;
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@SuppressLint("ViewHolder")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		int width = ((GridView) parent).getColumnWidth() - 15;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.image_item, parent, false);
		GridHolder holder = new GridHolder();
		holder.item_root = (FrameLayout) convertView.findViewById(R.id.item_root);
		holder.btn_delete = (ImageButton) convertView.findViewById(R.id.btn_delete);
		holder.btn_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapterListener.onBtnDeleteClickListener(position);
			}
		});
		holder.btn_delete.setOnTouchListener(new View.OnTouchListener() {
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
		holder.btn_note = (ImageButton) convertView.findViewById(R.id.btn_note);
		holder.btn_note.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapterListener.onBtnNoteClickListener(position);
			}
		});
		holder.btn_note.setOnTouchListener(new View.OnTouchListener() {
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
		holder.image = (ImageView) convertView.findViewById(R.id.image);
		if (holder.image.getLayoutParams().height < width) {
			holder.image.getLayoutParams().height = width;
		}
		holder.image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (position == list_image.size() - 1) {
					adapterListener.onImageRoomClickListener(position);
				}
			}
		});
		holder.image.setOnTouchListener(new View.OnTouchListener() {
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
		holder.txt_note_img = (TextView) convertView.findViewById(R.id.text_note_image);
		holder.txt_note_img.setTypeface(Config.getTypeface(mContext.getAssets()));

		if(list_image.get(position).isCloseButton()){
			holder.btn_delete.setVisibility(View.VISIBLE);
			holder.btn_note.setVisibility(View.VISIBLE);
		}else {
			holder.btn_delete.setVisibility(View.GONE);
			holder.btn_note.setVisibility(View.GONE);
		}
		
		if(!TextUtils.isEmpty(list_image.get(position).getNote())){
			holder.txt_note_img.setVisibility(View.VISIBLE);
			holder.txt_note_img.setText(list_image.get(position).getNote());
		}else {
			holder.txt_note_img.setVisibility(View.GONE);
		}
		if(list_image.get(position).getPath() != null){
			if(list_image.get(position).isUrl()){
				mImageLoader.displayImage(list_image.get(position).getPath(), holder.image, options);
				holder.item_root.setBackgroundResource(R.drawable.layout_border_dotted_line_uploaded);
			}else {
				holder.item_root.setBackgroundResource(R.drawable.layout_border_dotted_line);
				mImageLoader.displayImage(Uri.fromFile(new File(list_image.get(position).getPath())).toString(), holder.image, options);
			}
		}else {
			holder.image.setImageResource(R.drawable.ic_camera_pressed);
		}
		return convertView;
	}
}
