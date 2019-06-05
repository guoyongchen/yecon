package com.carocean.radio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.carocean.R;

public class RadioFreqNumberPicker extends NumberPicker {

	public RadioFreqNumberPicker(Context context) {
		super(context);
	}

	public RadioFreqNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RadioFreqNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void addView(View child, int width, int height) {
		super.addView(child, width, height);

		updateView(child);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);

		updateView(child);
	}

	@Override
	public void addView(View child, int index) {
		super.addView(child, index);

		updateView(child);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);

		updateView(child);
	}

	@Override
	public void addView(View child) {
		super.addView(child);

		updateView(child);
	}

	public void updateView(View view) {
		if (view instanceof EditText) {
			// 文字颜色、大小
			((EditText) view).setTextColor(getContext().getResources().getColor(R.color.white));
			((EditText) view).setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.radio_freq_numberpicker_size));
		}
	}

}
