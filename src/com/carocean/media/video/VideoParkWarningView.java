package com.carocean.media.video;


import com.carocean.ApplicationManage;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.t19can.CanBusService;
import com.carocean.t19can.CanBusService.onSpeedChangeListener;
import com.carocean.utils.DataShared;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class VideoParkWarningView extends LinearLayout {
	private static final String TAG = "VideoParkWarningView";
	private boolean mbParkingEnable; // 行车禁止视频
	private boolean mbUnderParking; // 驻车状态
	private Context mContext;
	private final int SPEED_LIMITE = 15;

	private onSpeedChangeListener speedListener = new onSpeedChangeListener() {
		@Override
		public void onSpeedChange(int speed) {
			mbUnderParking = speed < SPEED_LIMITE; 
			displayParking();
		}
	};

	public VideoParkWarningView(Context context) {
		this(context, null);
	}

	public VideoParkWarningView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoParkWarningView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public void updateParkProperty() {
		mbParkingEnable = DataShared.getInstance(ApplicationManage.context).getBoolean(SettingConstants.key_handlebrake, false);//SystemProperties.getBoolean("persist.sys.parking_enable", false);
		mbUnderParking = CanBusService.getInstance().getCurSpeed() < SPEED_LIMITE;
		Log.e(TAG, "parking_enable:" + mbParkingEnable + " mbUnderParking:" + mbUnderParking);
		displayParking();
	}

	private void displayParking() {
		if (mbParkingEnable && !mbUnderParking) {
			setVisibility(View.VISIBLE);
		} else {
			setVisibility(View.GONE);
		}
	}

	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			updateParkProperty();
		}
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		CanBusService.getInstance().registerSpeedHandler(speedListener);
		Log.e(TAG, "registerReceiver:mParkingListener");
		updateParkProperty();
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		CanBusService.getInstance().unRegisgerSpeedHandler(speedListener);
	}
}
