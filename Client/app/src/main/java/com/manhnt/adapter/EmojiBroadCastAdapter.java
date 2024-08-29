package com.manhnt.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

public class EmojiBroadCastAdapter extends ArrayAdapter<Emojicon> {

    public EmojiBroadCastAdapter(Context context, Emojicon[] data) {
        super(context, com.manhnt.shareroomlibrary.R.layout.emojicon_item, data);
    }

    public interface OnEmojiconBroadCastClickedListener {
        void onEmojiconBroadCastClicked(Emojicon emojicon);
    }

    private OnEmojiconBroadCastClickedListener listener;

    public void setListener(OnEmojiconBroadCastClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), com.manhnt.shareroomlibrary.R.layout.emojicon_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = (TextView) v.findViewById(com.manhnt.shareroomlibrary.R.id.emojicon_icon);
            v.setTag(holder);
        }
        Emojicon emoji = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.icon.setText(emoji.getEmoji());
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEmojiconBroadCastClicked(getItem(position));
            }
        });
        return v;
    }

    class ViewHolder {
        TextView icon;
    }
}
