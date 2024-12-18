package com.gc.materialdesign.views;

import com.manhnt.shareroomlibrary.R;
import com.gc.materialdesign.utils.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ButtonRectangle extends Button {
	
	TextView textButton;
	
	public ButtonRectangle(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultProperties();
	}
	@Override
	protected void setDefaultProperties(){
		super.minWidth = 80;
		super.minHeight = 54;
		super.background = R.drawable.ic_background_button;
		super.setDefaultProperties();
	}
	
	
	// Set atributtes of XML to View
	@SuppressWarnings("deprecation")
	protected void setAttributes(AttributeSet attrs){
		
		//Set background Color
		// Color by resource
		int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML,"background",-1);
		if(bacgroundColor != -1){
			setBackgroundColor(getResources().getColor(bacgroundColor));
		}else{
			// Color by hexadecimal
			background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
			if (background != -1)
				setBackgroundColor(background);
		}
		
		// Set text button
		String text = null;
		int textResource = attrs.getAttributeResourceValue(ANDROIDXML,"text",-1);
		if(textResource != -1){
			text = getResources().getString(textResource);
		}else{
			text = attrs.getAttributeValue(ANDROIDXML,"text");
		}
		if(text != null){
			textButton = new TextView(getContext());
			textButton.setText(text);
			textButton.setTextSize(14);
			textButton.setTextColor(Color.WHITE);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			params.setMargins(Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()));
			textButton.setLayoutParams(params);			
			addView(textButton);
			int textColor = attrs.getAttributeResourceValue(ANDROIDXML,"textColor",-1);
			if(textColor != -1){
				textButton.setTextColor(textColor);
			}else{
				// Color by hexadecimal
				textColor = attrs.getAttributeIntValue(ANDROIDXML, "textColor", -1);
				if (textColor != -1)
					textButton.setTextColor(textColor);
			}
			int[] array = {android.R.attr.textSize};
			TypedArray values = getContext().obtainStyledAttributes(attrs, array);
	        float textSize = values.getDimension(0, -1);
	        values.recycle();
	        if(textSize != -1)
	        	textButton.setTextSize(textSize);
			
		}
		
		rippleSpeed = attrs.getAttributeFloatValue(MATERIALDESIGNXML,"rippleSpeed", Utils.dpToPx(20, getResources()));
	}
	
	Integer height;
	Integer width;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (x != -1) {
			Rect src = new Rect(0, 0, getWidth(), getHeight());
			Rect dst = new Rect(0, 0, getWidth(), getHeight());
			canvas.drawBitmap(makeCircle(), src, dst, null);
			invalidate();
		}
	}

	public TextView getTextButton(){
		return textButton;
	}
}
