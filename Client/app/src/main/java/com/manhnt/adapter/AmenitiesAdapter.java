package com.manhnt.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.gc.materialdesign.views.CheckBox;
import com.manhnt.config.Config;
import com.manhnt.object.Amenities;
import com.manhnt.shareroom.R;

public class AmenitiesAdapter extends ArrayAdapter<Amenities> {

	private ArrayList<Amenities> list_amenities;
	private Context mContext;
	private AmenitiesAdapterListener listener;
	private Spring scaleSpring;
	private View currentView;

	public AmenitiesAdapter(Context context, int resource, ArrayList<Amenities> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.list_amenities = objects;
		/** Init Spring animation*/
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

	static class ViewHolder {
		/** Check box tiện ích */
		CheckBox cb;
		/** Tên tiện ích */
		TextView tv;
		/** Root View xử lý spring animation */
		RelativeLayout relative_row;
	}

	public interface AmenitiesAdapterListener{
		void onClickCheckBox(int width);
	}

	public void setListener(AmenitiesAdapterListener listener) {
		this.listener = listener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.amenities_item, parent, false);
			holder = new ViewHolder();
			holder.relative_row = (RelativeLayout) convertView.findViewById(R.id.relative_row);
			holder.cb = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_amen);
			holder.tv.setTypeface(Config.getTypeface(mContext.getAssets()));
			holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int getPosition = (Integer) buttonView.getTag();
					list_amenities.get(getPosition).setSelected(buttonView.isChecked());
					listener.onClickCheckBox(buttonView.getWidth());
				}
			});
			holder.relative_row.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

				}
			});
			holder.relative_row.setOnTouchListener(new View.OnTouchListener() {
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
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.cb.setTag(position);
		holder.tv.setText(list_amenities.get(position).getName());
		holder.cb.setChecked(list_amenities.get(position).isSelected());
		return convertView;
	}

}
