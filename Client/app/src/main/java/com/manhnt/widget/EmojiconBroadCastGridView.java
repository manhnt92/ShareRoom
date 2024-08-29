package com.manhnt.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import com.manhnt.adapter.EmojiBroadCastAdapter;
import java.util.Arrays;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import github.ankushsachdeva.emojicon.emoji.People;

@SuppressWarnings("SuspiciousToArrayCall")
public class EmojiconBroadCastGridView {

    public View rootView;
    Emojicon[] mData;

    @SuppressLint("InflateParams")
    public EmojiconBroadCastGridView(Context context, Emojicon[] emojicons) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(com.manhnt.shareroomlibrary.R.layout.emojicon_grid, null);
        GridView gridView = (GridView) rootView.findViewById(com.manhnt.shareroomlibrary.R.id.Emoji_GridView);
        if (emojicons== null) {
            mData = People.DATA;
        } else {
            mData = Arrays.asList(emojicons).toArray(new Emojicon[emojicons.length]);
        }
        EmojiBroadCastAdapter mAdapter = new EmojiBroadCastAdapter(rootView.getContext(), mData);
        mAdapter.setListener(new EmojiBroadCastAdapter.OnEmojiconBroadCastClickedListener() {
            @Override
            public void onEmojiconBroadCastClicked(Emojicon emojicon) {
                listener.onEmojiconBroadCastClicked(emojicon);
            }
        });
        gridView.setAdapter(mAdapter);
    }

    public interface OnEmojiconBroadCastClickedListener {
        void onEmojiconBroadCastClicked(Emojicon emojicon);
    }

    private OnEmojiconBroadCastClickedListener listener;

    public void setListener(OnEmojiconBroadCastClickedListener listener) {
        this.listener = listener;
    }
}
