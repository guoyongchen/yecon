package com.carocean.media.bt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class MySeekBar extends SeekBar {

	public MySeekBar(Context context) {
		super(context);
	}
	
    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
