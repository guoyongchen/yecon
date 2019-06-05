package com.carocean.vsettings.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class BaseBtn extends Button {
	final int ANDROID_MODE = 0;
	final int FYT_CUSTOM_MODE = 1;
	protected int clickMode = 1;
	protected boolean longClick = false;
	Runnable refresh = new Runnable() {
		public void run() {
			if (isPressed()) {
				setPressed(false);
			}
		}
	};

	public BaseBtn(Context paramContext) {
		super(paramContext);
	}

	public BaseBtn(Context paramContext, AttributeSet paramAttributeSet) {
		this(paramContext, paramAttributeSet, 0);
	}

	public BaseBtn(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		// TypedArray localTypedArray =
		// paramContext.obtainStyledAttributes(paramAttributeSet,
		// R.styleable.BaseBtn,
		// paramInt, 0);
		// clickMode = localTypedArray.getInt(0, 1);
		// localTypedArray.recycle();
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		boolean bool = true;
		if ((longClick) || (clickMode == 0)) {
			bool = super.onTouchEvent(paramMotionEvent);
			return bool;
		}
		switch (0xFF & paramMotionEvent.getAction()) {
		case 2:
		default:
			// return bool;
		case 0:
			setPressed(bool);
			performClick();
			postDelayed(refresh, 500L);
			// return bool;
		}
		setPressed(false);
		return bool;
	}

	public void setClickMode(int paramInt) {
		clickMode = paramInt;
	}

	public void setOnLongClickListener(View.OnLongClickListener paramOnLongClickListener) {
		if (paramOnLongClickListener != null) {
			longClick = true;
		}
		super.setOnLongClickListener(paramOnLongClickListener);
	}
}
