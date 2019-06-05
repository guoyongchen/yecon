package com.carocean.settings.screensaver;

import com.carocean.utils.Utils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchCheckFrameLayout extends FrameLayout {
	static final int DELAY_TIME = 200;
	static final int HAND_COUNT = 1;
	public static final String ACTION_HAND_TOUCH = "action.hand.touch";
	Context mContext = null;
	static int mTouchPointCount = 0;
	Handler mHandler = new Handler();
	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sendHandMessage();
		}
	};

	public TouchCheckFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TouchCheckFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TouchCheckFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	/**
	 */
	void sendHandMessage() {
		if (mContext != null && mTouchPointCount >= HAND_COUNT) {
			Utils.showSystemUI(mContext, true);
			if (null != ScreenSaverService.getInstance()) {
				ScreenSaverService.getInstance().mScreensaverImpBind.detachActivity();
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		mTouchPointCount = ev.getPointerCount();
		if ((mTouchPointCount >= HAND_COUNT) && (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_DOWN)) {
			if (mHandler != null && mRunnable != null) {
				mHandler.removeCallbacks(mRunnable);
				mHandler.postDelayed(mRunnable, DELAY_TIME);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		mTouchPointCount = event.getPointerCount();
		if ((mTouchPointCount >= HAND_COUNT) && (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_DOWN)) {
			if (mHandler != null && mRunnable != null) {
				mHandler.removeCallbacks(mRunnable);
				mHandler.postDelayed(mRunnable, DELAY_TIME);
			}
			return false;
		}
		return true;
	}
}
