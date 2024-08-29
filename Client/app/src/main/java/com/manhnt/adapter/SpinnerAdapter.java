package com.manhnt.adapter;

import java.util.ArrayList;
import com.manhnt.config.Config;
import com.manhnt.shareroom.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerAdapter extends ArrayAdapter<String>{

	private Context mContext;
	private ArrayList<String> list;

	public SpinnerAdapter(Context context, int resource, ArrayList<String> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.list = objects;
	}

	private class ViewHolder{
		TextView txt_name;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.spinner_item, parent, false);
			holder = new ViewHolder();
			holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
			holder.txt_name.setTypeface(Config.getTypeface(mContext.getAssets()));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txt_name.setText(list.get(position));
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.spinner_drop_down_item, parent, false);
			holder = new ViewHolder();
			holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
			holder.txt_name.setTypeface(Config.getTypeface(mContext.getAssets()));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txt_name.setText(list.get(position));
		return convertView;
	}

}
