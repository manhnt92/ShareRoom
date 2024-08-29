package com.manhnt.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.manhnt.widget.EmojiconBroadCastGridView;
import java.util.List;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

public class EmojiBroadCastPagerAdapter extends PagerAdapter {

    private List<EmojiconBroadCastGridView> views;

    public EmojiBroadCastPagerAdapter(List<EmojiconBroadCastGridView> views) {
        super();
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position).rootView;
        views.get(position).setListener(new EmojiconBroadCastGridView.OnEmojiconBroadCastClickedListener() {
            @Override
            public void onEmojiconBroadCastClicked(Emojicon emojicon) {
                listener.onEmojiconClick(emojicon);
            }
        });
        container.addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View)view);
    }

    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }

    public interface Listener {
        void onEmojiconClick(Emojicon emojicon);
    }
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
