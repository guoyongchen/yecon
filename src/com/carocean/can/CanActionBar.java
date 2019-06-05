package com.carocean.can;

import com.carocean.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class CanActionBar extends RelativeLayout implements OnCheckedChangeListener {
	private View mTuhmbImage;
	private int mCurrentIndex = -1;
	private RadioGroup mActionBar;
	private int mOrientation = -1;
	private OnSelectedChangeListener mOnSelectedChangeListener;

	public CanActionBar(Context context) {
		super(context);
	}

	public CanActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void onFinishInflate() {
		mActionBar = (RadioGroup) findViewById(R.id.can_action_bar_parent);
		mActionBar.setOnCheckedChangeListener(this);
		mTuhmbImage = findViewById(R.id.can_actionbar_thumb_id);

		setCurrentItem(0);
	}
	private int findChildIndex(View v) {
		int index = 0;

		int count = mActionBar.getChildCount();

		for (int i = 0; i < count; i++) {
			View child = mActionBar.getChildAt(i);

			if (child.equals(v)) {
				index = i;
				break;
			}
		}

		return index;
	}
	private int findChildIndex(int id) {
		int index = 0;

		int count = mActionBar.getChildCount();

		for (int i = 0; i < count; i++) {
			View child = mActionBar.getChildAt(i);

			if (id == child.getId()) {
				index = i;
				break;
			}
		}

		return index;
	}
	public void onCheckedChanged(RadioGroup group, int id) {
		int index = findChildIndex(id);
		setCurrentItem(index);
		if (null != mOnSelectedChangeListener) {
			mOnSelectedChangeListener.OnSelectedChange(index);
		}
	}
	public void onClick(View v) {
		int index = findChildIndex(v);

		setCurrentItem(index);
		if (null != mOnSelectedChangeListener) {
			mOnSelectedChangeListener.OnSelectedChange(index);
		}
	}

	public void setCurrentItem(int item) {
		if (item == mCurrentIndex) {
			return;
		}
		if (-1 == mCurrentIndex) {
			mCurrentIndex = 0;
		}
		if(item < mActionBar.getChildCount()) {
			RadioButton btn = (RadioButton)mActionBar.getChildAt(item);
			
			btn.setChecked(true);
		}
		View CurView = mActionBar.getChildAt(mCurrentIndex);
		View NextView = mActionBar.getChildAt(item);
		int curWidth = CurView.getWidth();
		int nextWidth = NextView.getWidth();
		int thumbWidth = mTuhmbImage.getWidth();
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) 
				CurView.getLayoutParams();
		//int padding = layoutParams.leftMargin;
		int curLeft = CurView.getLeft() + (curWidth - thumbWidth)/2;
		layoutParams = (LinearLayout.LayoutParams)NextView.getLayoutParams();
		//padding = layoutParams.leftMargin;
		int nextLeft = NextView.getLeft() + (nextWidth - thumbWidth)/2;
		int offY = nextLeft - curLeft;

		mCurrentIndex = item;

		if (LinearLayout.VERTICAL == mOrientation) {
			mTuhmbImage.animate().translationYBy(offY).setDuration(100);
		} else {
			mTuhmbImage.animate().translationXBy(offY).setDuration(100);
		}
	}

	public void setOnSelectedChangeListener(OnSelectedChangeListener listener) {
		mOnSelectedChangeListener = listener;
	}

	public interface OnSelectedChangeListener {
		void OnSelectedChange(int index);
	}
}
