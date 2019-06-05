package com.carocean.can;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class PageView extends HorizontalScrollView {
	private int mBaseScrollX;
	private int mScreenWidth;
	private int mScreenHeight;

	private LinearLayout mContainer;
	private boolean flag;
	private int mPageCount;

	private int mScrollX = 200;

	//Context mContext;

	public PageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//mContext = context;
		DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
		mScreenWidth = 752;
		mScreenHeight = dm.heightPixels;
		setOnTouchListener(mOnTouchListener);
	}

	public void addPage(View page) {
		addPage(page, -1);
	}

	public void addPage(View page, int index) {
		if (!flag) {
			mContainer = (LinearLayout) getChildAt(0);
			flag = true;
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mScreenWidth, mScreenHeight);
		if (index == -1) {
			mContainer.addView(page, params);
		} else {
			mContainer.addView(page, index, params);
		}
		mPageCount++;
	}

	public void removePage(int index) {
		if (mPageCount < 1) {
			return;
		}
		if (index < 0 || index > mPageCount - 1) {
			return;
		}
		mContainer.removeViewAt(index);
		mPageCount--;
	}

	public void removeAllPages() {
		if (mPageCount > 0) {
			mContainer.removeAllViews();
		}
	}

	public int getPageCount() {
		return mPageCount;
	}

	private int getBaseScrollX() {
		return getScrollX() - mBaseScrollX;
	}

	private void baseSmoothScrollTo(int x) {
		smoothScrollTo(x + mBaseScrollX, 0);
	}
	OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		float rawX;
		int mTouchSlop =  ViewConfiguration.get(mContext).getScaledTouchSlop();

		@Override
		public boolean onTouch(View v, MotionEvent event) {
		    switch (event.getActionMasked()) {
		        case MotionEvent.ACTION_DOWN:
		            v.getParent().requestDisallowInterceptTouchEvent(true);
		            rawX = event.getRawX();
		            break;
		        case MotionEvent.ACTION_CANCEL:
		        case MotionEvent.ACTION_UP:
		        {
		            v.getParent().requestDisallowInterceptTouchEvent(false);
		            rawX = 0f;
		            int scrollX = getBaseScrollX();
					if (scrollX > mScrollX) {
						baseSmoothScrollTo(mScreenWidth);
						mBaseScrollX += mScreenWidth;
					}

					else if (scrollX > 0) {
						baseSmoothScrollTo(0);
					}

					else if (scrollX > -mScrollX) {
						baseSmoothScrollTo(0);
					}

					else {
						baseSmoothScrollTo(-mScreenWidth);
						mBaseScrollX -= mScreenWidth;
					}
		        }
		            break;
		        case MotionEvent.ACTION_MOVE:
		            if (Math.abs(rawX - event.getRawX()) > mTouchSlop)
		               v.getParent().requestDisallowInterceptTouchEvent(false);
		            break;
		    }
		    return false;
		}
	};	
}
