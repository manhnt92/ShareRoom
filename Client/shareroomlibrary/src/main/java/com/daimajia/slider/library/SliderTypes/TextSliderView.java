package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manhnt.shareroomlibrary.R;

/**
 * This is a slider with a description TextView.
 */
public class TextSliderView extends BaseSliderView{

    private Typeface typeface;

    public TextSliderView(Context context, Typeface typeface) {
        super(context);
        this.typeface = typeface;
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.render_type_text,null);
        ImageView target = (ImageView)v.findViewById(R.id.daimajia_slider_image);
        TextView description = (TextView)v.findViewById(R.id.description);
        description.setText(getDescription());
        description.setTypeface(typeface);
        bindEventAndShow(v, target);
        return v;
    }

}
