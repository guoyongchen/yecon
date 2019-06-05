package com.carocean.vmedia.t19can.view;

import com.carocean.R;
import com.carocean.vmedia.t19can.T19Air;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.NumberPicker.OnScrollListener;

public class AirTempNumberPicker extends NumberPicker implements OnValueChangeListener, OnScrollListener {

	private String[] mNumber = { "18℃", "18.5℃", "19℃", "19.5℃", "20℃", "20.5℃", "21℃", "21.5℃", "22℃", "22.5℃", "23℃",
			"23.5℃", "24℃", "24.5℃", "25℃", "25.5℃", "26℃", "26.5℃", "27℃", "27.5℃", "28℃", "28.5℃", "29℃", "29.5℃",
			"30℃", "30.5℃", "31℃", "31.5℃", "32℃" };
	private int mSpeedIndex;
	private boolean flag = false;

	public AirTempNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// initData();
		initView();
	}

	public void addView(View child) {
		super.addView(child);
		updateView(child);
	}

	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		updateView(child);
	}

	public void addView(View child, int width, int height) {
		super.addView(child, width, height);
		updateView(child);
	}

	public void addView(View child, int index) {
		super.addView(child, index);
		updateView(child);
	}

	public void addView(View child, LayoutParams params) {
		super.addView(child, params);
		updateView(child);
	}

	private void updateView(View view) {
		Log.i("TEST", "updateView");
		if (view instanceof EditText) {
			((EditText) view).setTextColor(Color.WHITE);
			((EditText) view).setTextSize(26);
			((EditText) view).setEnabled(false);
		}
	}

	private void initView() {
		setMaxValue(mNumber.length - 1);
		setMinValue(0);
		setDisplayedValues(mNumber);
		setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
		setWrapSelectorWheel(false);
		setValue(mSpeedIndex);
		setNumberPickerColor(0);

		setOnValueChangedListener(this);
		setOnScrollListener(this);
	}

	public void resetData() {
		mSpeedIndex = mNumber.length - 1;
		setValue(mSpeedIndex);
	}

	public String getSpeedText() {
		return mNumber[mSpeedIndex];
	}

	public void setSpeedIndex(int index) {
		mSpeedIndex = index;
		setValue(mSpeedIndex);
	}

	public int getSpeedIndex() {
		return mSpeedIndex;
	}

	public void subSpeedIndex() {
		mSpeedIndex = --mSpeedIndex < 0 ? mNumber.length - 1 : mSpeedIndex;
		setValue(mSpeedIndex);
	}

	public void addSpeedIndex() {
		mSpeedIndex = ++mSpeedIndex > mNumber.length - 1 ? 0 : mSpeedIndex;
		setValue(mSpeedIndex);
	}

	public void setNumberPickerColor(int colorType) {
		java.lang.reflect.Field field = null;
		try {
			field = NumberPicker.class.getDeclaredField("mSelectionDivider");
			if (field != null) {
				field.setAccessible(true);
				switch (colorType) {
				case 1:
					field.set(this, new ColorDrawable(getResources().getColor(R.color.white)));
					break;
				case 2:
					field.set(this, new ColorDrawable(getResources().getColor(R.color.white)));
					break;
				default:
					field.set(this, new ColorDrawable(getResources().getColor(R.color.transparent)));
					break;
				}

				invalidate();
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void onValueChange(NumberPicker arg0, int old_value, int new_value) {
		setSpeedIndex(new_value);
		System.out.println("old_value: " + old_value + "  new_value: " + new_value);
		if (!flag) {
			if (old_value > new_value && (old_value - new_value) == 1) {
				T19Air.sendTempSet(new_value + 2);
			} else if (new_value > old_value && (new_value - old_value) == 1) {
				T19Air.sendTempSet(new_value + 2);
			}
		}
	}

	@Override
	public void onScrollStateChange(NumberPicker paramNumberPicker, int scrollState) {
		// TODO Auto-generated method stub

		// scrollState的值
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			flag = false;
			T19Air.sendTempSet(getSpeedIndex() + 2);
			break;

		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			flag = true;
			break;

		case OnScrollListener.SCROLL_STATE_FLING:
			break;

		default:
			break;
		}

	}
}
