package com.carocean.vsettings.time;

import java.util.Calendar;

import com.carocean.R;
import com.carocean.page.IPage;
import com.carocean.settings.utils.DateTimePicker;
import com.carocean.settings.utils.timeUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * @ClassName: vHeaderLayout
 * @Description: TODO
 * @author: LZY
 * @date: 2018.04.24
 **/
public class PageTime implements IPage,OnClickListener, OnTouchListener, OnTimeChangedListener, OnDateChangedListener {
	private Context mContext;
	private ViewGroup mRootView;
	private PolicyHandler mHandler;

	int ID_BUTTON_DT[] = { R.id.btn_year_cut, R.id.btn_month_cut, R.id.btn_day_cut, R.id.btn_hour_cut,
			R.id.btn_minute_cut, R.id.btn_year_add, R.id.btn_month_add, R.id.btn_day_add, R.id.btn_hour_add,
			R.id.btn_minute_add, };
	Button mButtonDT[] = new Button[ID_BUTTON_DT.length];

	int ID_BUTTON[] = { R.id.data_time_gps, R.id.data_time_confirm, R.id.data_time_cancel };
	Button mButton[] = new Button[ID_BUTTON.length];

	private DatePicker mDatePicker;
	private TimePicker mTimePicker;
	private int mYear, mMonth, mDay, mHour, mMinute;
	private boolean mbAutoTime;

	void init(Context context) {
		mHandler = new PolicyHandler();

		initDateTime();
	}

	void initDateTime() {
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
	}

	void initView(Context context, ViewGroup rootView) {
		mDatePicker = (DatePicker) rootView.findViewById(R.id.datepicker);
		mTimePicker = (TimePicker) rootView.findViewById(R.id.timepicker);
		setStyle_DatePickerAndTimePicker(context);
		mDatePicker.init(mYear, mMonth, mDay, this);
		mTimePicker.setCurrentHour(mHour);
		mTimePicker.setCurrentMinute(mMinute);
		mTimePicker.setOnTimeChangedListener(this);

		for (int i = 0; i < ID_BUTTON_DT.length; i++) {
			mButtonDT[i] = (Button) rootView.findViewById(ID_BUTTON_DT[i]);
			mButtonDT[i].setOnClickListener(this);
			mButtonDT[i].setOnTouchListener(this);
		}

		for (int i = 0; i < ID_BUTTON.length; i++) {
			mButton[i] = (Button) rootView.findViewById(ID_BUTTON[i]);
			mButton[i].setOnClickListener(this);
		}

		mbAutoTime = timeUtils.getAutoState(mContext, Settings.Global.AUTO_TIME);
		setSwitchOnUI(context, mButton[0], mbAutoTime);
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_time, null));
			init(context);
			initView(context, mRootView);
		}
		return mRootView;
	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub

	}

	private void setStyle_DatePickerAndTimePicker(Context context) {
		mTimePicker.setIs24HourView(timeUtils.is24HourFormat(context));
		DateTimePicker.setcolorfortimepickerdivider(mTimePicker, Color.TRANSPARENT);
		DateTimePicker.setcolorfordatepickerdivider(mDatePicker, Color.TRANSPARENT);
		float size = context.getResources().getDimensionPixelOffset(R.dimen.TEXT_SIZE_M);
		DateTimePicker.settextsizefortimepicker(mTimePicker, size);
		DateTimePicker.settextsizefordatepicker(mDatePicker, size);

		// Change DatePicker layout
		// 0 : LinearLayout; 1 : CalendarView
		LinearLayout dpContainer = (LinearLayout) mDatePicker.getChildAt(0);
		LinearLayout dpSpinner = (LinearLayout) dpContainer.getChildAt(0);
		for (int i = 0; i < dpSpinner.getChildCount(); i++) {
			NumberPicker numPicker = (NumberPicker) dpSpinner.getChildAt(i);
			// 0-2 : NumberPicker
			int width = context.getResources().getDimensionPixelOffset(R.dimen.DatePicker_NumberPicker_width);
			int height = context.getResources().getDimensionPixelOffset(R.dimen.DatePicker_NumberPicker_height);
			LayoutParams params1 = new LayoutParams(width, height);
			params1.leftMargin = 0;
			params1.rightMargin = 0;
			numPicker.setLayoutParams(params1);

			// EditText cusET = (EditText)numPicker.getChildAt(0); //
			// CustomEditText
			// cusET.setTextSize(14);
			// cusET.setWidth(70);
		}

		// Change TimePicker layout
		// 0 : LinearLayout; 1 : CalendarView
		LinearLayout tpContainer = (LinearLayout) mTimePicker.getChildAt(0);
		LinearLayout tpSpinner = (LinearLayout) tpContainer.getChildAt(0);
		for (int i = 0; i < tpSpinner.getChildCount(); i++) {
			// child(1) is a TextView ( : )
			if (i == 1) {
				// TextView tv = (TextView) tpSpinner.getChildAt(i);
				// tv.setWidth(120);
				continue;
			}
			// 0 : NumberPicker; 1 : TextView; 2 : NumberPicker
			NumberPicker numPicker = (NumberPicker) tpSpinner.getChildAt(i);
			int width = context.getResources().getDimensionPixelOffset(R.dimen.TimePicker_NumberPicker_width);
			int height = context.getResources().getDimensionPixelOffset(R.dimen.TimePicker_NumberPicker_height);
			LayoutParams params3 = new LayoutParams(width, height);
			// LayoutParams params3 = new LayoutParams(width,
			// LayoutParams.WRAP_CONTENT);
			params3.leftMargin = 0;
			params3.rightMargin = 0;
			numPicker.setLayoutParams(params3);

			// EditText cusET = (EditText)numPicker.getChildAt(0); //
			// cusET.setTextSize(14);
			// cusET.setWidth(120);
		}

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_year_cut:
			mYear -= 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_month_cut:
			mMonth -= 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_day_cut:
			mDay -= 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_hour_cut:
			mHour -= 1;
			mTimePicker.setCurrentHour(mHour);
			break;
		case R.id.btn_minute_cut:
			mMinute -= 1;
			mTimePicker.setCurrentMinute(mMinute);
			break;
		case R.id.btn_year_add:
			mYear += 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_month_add:
			mMonth += 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_day_add:
			mDay += 1;
			mDatePicker.init(mYear, mMonth, mDay, this);
			break;
		case R.id.btn_hour_add:
			mHour += 1;
			mTimePicker.setCurrentHour(mHour);
			break;
		case R.id.btn_minute_add:
			mMinute += 1;
			mTimePicker.setCurrentMinute(mMinute);
			break;
		case R.id.data_time_confirm:
			timeUtils.setDateTime(mContext, mYear, mMonth, mDay, mHour, mMinute);
			break;
		case R.id.data_time_cancel:
			initDateTime();
			mDatePicker.init(mYear, mMonth, mDay, this);
			mTimePicker.setCurrentHour(mHour);
			mTimePicker.setCurrentMinute(mMinute);
			break;
		case R.id.data_time_gps:
			mbAutoTime = !mbAutoTime;
			setSwitchOnUI(mContext, mButton[0], mbAutoTime);
			Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, mbAutoTime ? 1 : 0);
			if (timeUtils.mLocaltionListener != null)
				timeUtils.mLocaltionListener.LocationListener(mbAutoTime);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			mHandler.sendEmptyMessageDelayed(view.getId(), 100);
			break;
		case MotionEvent.ACTION_UP:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			break;
		default:
			break;
		}
		return false;
	}

	private class PolicyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (mHandler.hasMessages(msg.what))
				mHandler.removeMessages(msg.what);
			mHandler.sendEmptyMessageDelayed(msg.what, 100);
			onClick((Button) mRootView.findViewById(msg.what));
		}
	}

	void setDatePicker() {
		mDatePicker.init(mYear, mMonth, mDay, this);
	}

	@Override
	public void onDateChanged(DatePicker arg0, int year, int month, int day) {
		// TODO Auto-generated method stub
		mYear = year;
		mMonth = month;
		mDay = day;
	}

	@Override
	public void onTimeChanged(TimePicker arg0, int hour, int minute) {
		// TODO Auto-generated method stub
		mHour = hour;
		mMinute = minute;
	}

	private void setSwitchOnUI(Context context, Button button, boolean bSwitch) {
		Drawable drawableOn = context.getResources().getDrawable(R.drawable.check_carinfo_open);
		drawableOn.setBounds(0, 0, drawableOn.getMinimumWidth(), drawableOn.getMinimumHeight());
		Drawable drawableOff = context.getResources().getDrawable(R.drawable.check_carinfo_close);
		drawableOff.setBounds(0, 0, drawableOff.getMinimumWidth(), drawableOff.getMinimumHeight());
		// button.setCompoundDrawables(null, bSwitch ? drawablePause :
		// drawablePlay, null, null);
		button.setBackground(bSwitch ? drawableOn : drawableOff);
		setDisableUI(!bSwitch);
	}

	private void setDisableUI(boolean enabled) {
		for (int i = 0; i < mButtonDT.length; i++) {
			mButtonDT[i].setEnabled(enabled);
		}
		mDatePicker.setEnabled(enabled);
		mTimePicker.setEnabled(enabled);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}
}
