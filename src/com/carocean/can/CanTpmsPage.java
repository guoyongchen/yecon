package com.carocean.can;

import com.carocean.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CanTpmsPage extends LinearLayout{

	private int      mPressureTvIds[] = {R.id.can_front_left_tire_pressure,R.id.can_front_right_tire_pressure,
			   							 R.id.can_rear_left_tire_pressure,R.id.can_rear_right_tire_pressure};
	private int      mTemperatureTvIds[] = {R.id.can_front_left_tire_temperature,R.id.can_front_right_tire_temperature,
				 						    R.id.can_rear_left_tire_temperature,R.id.can_rear_right_tire_temperature};
	private int      mExceptionPic[]     = {R.id.can_abnormal_tire_fl,R.id.can_abnormal_tire_fr,
			                                R.id.can_abnormal_tire_rl,R.id.can_abnormal_tire_rr};
	private int      mPressureData[] = new int [mPressureTvIds.length];
	private int      mTemperatureData[] = new int [mTemperatureTvIds.length];
	private          ProcessHandler   mProcessHandler = null;
	public CanTpmsPage(Context context) {
		super(context);
	}

	public CanTpmsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		mProcessHandler = new ProcessHandler(this);
	}

	public void onFinishInflate() {

	}

	public void OnPressureChange(int location, int pressure) {
		if(location < mPressureData.length) {
			mPressureData[location] = pressure;
		}
		
		mProcessHandler.sendEmptyMessage(0);
	};

	public void OnTemperatureChange(int location, int temperature) {
		if(location < mTemperatureData.length) {
			mTemperatureData[location] = temperature;
		}
		mProcessHandler.sendEmptyMessage(0);
	};
	public void updateDates() {
		for(int i = 0; i < mPressureTvIds.length; i ++) {
			TextView view = (TextView)findViewById(mPressureTvIds[i]);
			String text = String.valueOf(mPressureData[i]);
			text += getResources().getString(R.string.air_pressure_unit);
			
			if(null != view) {
				view.setText(text);
			}
			View v = findViewById(mExceptionPic[i]);
			int visible = View.GONE;
			int color = Color.WHITE;
			if(mPressureData[i] > 320 || mPressureData[i] < 280) {
				visible = View.VISIBLE;
				color = Color.RED;
			}
			view.setTextColor(color);
			if(null != v) {
				v.setVisibility(visible);
			} 
		}
		
		for(int i = 0; i < mTemperatureTvIds.length; i ++) {
			TextView view = (TextView)findViewById(mTemperatureTvIds[i]);
			String text = String.valueOf(mTemperatureData[i]);
			text += getResources().getString(R.string.air_temperature_unit);
			if(null != view) {
				view.setText(text);
			}
			
			View v = findViewById(mExceptionPic[i]);
			int color = Color.WHITE;
			
			if(mTemperatureData[i] > 90) {
				if(null != v && View.GONE == v.getVisibility()) {
					v.setVisibility(View.VISIBLE);
				} 
				color = Color.RED;
			}
			view.setTextColor(color);
		}
	}
	static class ProcessHandler extends Handler{
		private CanTpmsPage mCanTpmsPage;
		ProcessHandler(CanTpmsPage parent){
			mCanTpmsPage = parent;
		}
		public void handleMessage(Message msg) {
			if(null != mCanTpmsPage) {
				mCanTpmsPage.updateDates();
			}
		}
	}
}
