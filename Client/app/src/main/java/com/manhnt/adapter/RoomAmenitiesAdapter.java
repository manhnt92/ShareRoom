package com.manhnt.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.manhnt.config.WidgetManager;
import com.manhnt.shareroom.R;
import java.util.ArrayList;

public class RoomAmenitiesAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> list_amenities;
    private boolean isAnim;

    public RoomAmenitiesAdapter(Context context, int resource, ArrayList<String> objects, boolean isAnim) {
        super(context, resource, objects);
        this.mContext = context;
        this.list_amenities = objects;
        this.isAnim = isAnim;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.room_detail_amenities_item, parent, false);
        TextView tv_amen = WidgetManager.getInstance((Activity) mContext)
            .TextView(convertView, R.id.tv_amen, isAnim);
        tv_amen.setText(" - " + list_amenities.get(position));
        return convertView;
    }
}
