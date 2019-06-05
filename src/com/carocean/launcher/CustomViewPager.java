package com.carocean.launcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2017/5/19.
 */

public class CustomViewPager extends ViewPager {

	private boolean isCanScroll = true;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setCanScroll(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isCanScroll && super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return isCanScroll && super.onTouchEvent(ev);

	}
}