package com.carocean.can;

import java.util.Random;

import com.carocean.R;
import com.carocean.can.CanServiceClient.OnConditionerStatusChangeListener;
import com.carocean.can.CanServiceClient.OnSeatHeatLvlChangeListener;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AirConditionerActivity extends FragmentActivity
		implements OnSeatHeatLvlChangeListener, OnConditionerStatusChangeListener {
	private final String TAG = "AirConditionerFragment";

	private int mLifeCyclePercent = 100;
	private int mCurrentPMValue = 301;
	private ProcessDataHandler mHandler;
	private ImageView mConditionerModeImg;
	private ImageView mAcAutoDtatusImg;
	private TextView mAcStatusTv;
	private CustomSeekbar mAirVolumeBar;
	private CustomSeekbar mAirTemperatureBar;
	private int mSeatheatingImgs[] = { R.id.air_condition_seatheating_lf_flg_one,
			R.id.air_condition_seatheating_lf_flg_two, R.id.air_condition_seatheating_rf_flg_one,
			R.id.air_condition_seatheating_rf_flg_two };
	private int mSeatheatingStatus[] = new int[mSeatheatingImgs.length];
	private int mHVACPowerStatus = 0;
	private int mHVACAutoIndicationStatus = 0;
	private int mHVACOnRequestIndication = 0;
	private int mHVACCycleMode = 0;
	private int mHVACMode = 0;
	private int mHVACAirVolume = 0;
	private int mHVACSetTemperature = 17;
	private int mConditionerModeIcons[] = { R.drawable.air_conditioner_mode_blow_up,
			R.drawable.air_conditioner_mode_blow_updown, R.drawable.air_conditioner_mode_blow_down,
			R.drawable.air_conditioner_mode_blow_windowdown, R.drawable.air_conditioner_mode_blow_window,

			R.drawable.air_conditioner_mode_blow_up, R.drawable.air_conditioner_mode_blow_window,
			R.drawable.air_conditioner_mode_blow_windowdown, R.drawable.air_conditioner_mode_blow_down,
			R.drawable.air_conditioner_mode_blow_updown, };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conditioner);
		initViews();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}

	private void initViews() {
		mHandler = new ProcessDataHandler(this);
		mConditionerModeImg = (ImageView) findViewById(R.id.air_condition_mode_icon);
		mAcAutoDtatusImg = (ImageView) findViewById(R.id.air_condition_mode_icon_tips);
		mAcStatusTv = (TextView) findViewById(R.id.air_conditioner_ac_status);
		mAirVolumeBar = (CustomSeekbar) findViewById(R.id.air_condition_airvolume_bar);
		mAirTemperatureBar = (CustomSeekbar) findViewById(R.id.air_conditioner_temperature_bar);
		handler.postDelayed(runnable, 5000);
	}

	private void onPMValueChange(int value) {
		mCurrentPMValue = value;
		mHandler.sendEmptyMessage(0);
	}

	void processPMData(boolean start) {
		int value = mCurrentPMValue;
		Log.v(TAG, "processPMData." + "start:" + start + ",value:" + value);
	}

	public void onClick(View v) {

	}

	static class ProcessDataHandler extends Handler {
		private AirConditionerActivity mActivity;

		ProcessDataHandler(AirConditionerActivity context) {
			mActivity = (AirConditionerActivity) context;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mActivity.processPMData(true);
				mActivity.processSeatHeatLvlChange();
				CanServiceClient client = CanServiceClient.getInstance(mActivity);

				if (null != client) {
					client.setOnSeatHeatLvlChangeListener(mActivity);
					client.setOnConditionerStatusChangeListener(mActivity);
				}
				break;
			case 1:
				mActivity.processSeatHeatLvlChange();
				break;
			case 2:
				mActivity.processACStatusChange();
				break;
			}
		}
	}

	final Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case 1:

				int max = 180;
				int min = 10;
				Random random = new Random();
				int value = random.nextInt(max) % (max - min + 1) + min;
				onPMValueChange(value);

				postDelayed(runnable, 20000);

				break;

			}

			super.handleMessage(msg);

		}

	};

	private Runnable runnable = new Runnable() {

		public void run() {

			Message message = handler.obtainMessage();

			message.what = 1;

			handler.sendMessage(message);

		}

	};

	private void switchMode(AirConditionerMode mode) {
		int index = 0;
		switch (mode) {
		case MANULA_BLOW_UP:
			break;
		case MANULA_BLOW_UP_DOWN:
			index = 1;
			break;
		case MANULA_BLOW_DOWN:
			index = 2;
			break;
		case MANULA_BLOW_WIN_DOWN:
			index = 3;
			break;
		case MANULA_BLOW_WIN:
			index = 4;
			break;
		}

		if (null != mConditionerModeImg) {
			mConditionerModeImg.setBackgroundResource(mConditionerModeIcons[index]);
		}
	}

	public enum AirConditionerMode {
		MANULA_BLOW_UP, MANULA_BLOW_UP_DOWN, MANULA_BLOW_DOWN, MANULA_BLOW_WIN_DOWN, MANULA_BLOW_WIN
	}

	private void processSeatHeatLvlChange() {
		for (int i = 0; i < mSeatheatingStatus.length; i++) {
			View v = findViewById(mSeatheatingImgs[i]);

			if (null != v) {
				int resid = R.drawable.air_conditioner_seatheating_dis;
				if (mSeatheatingStatus[i] > 0) {
					resid = R.drawable.air_conditioner_seatheating_f;
				}

				v.setBackgroundResource(resid);
			}
		}
	}

	public void OnSeatHeatLvlChange(int fl, int fr, int rl, int rr) {
		mSeatheatingStatus[0] = fl;
		mSeatheatingStatus[1] = fr;
		mSeatheatingStatus[2] = rl;
		mSeatheatingStatus[3] = rr;
		mHandler.sendEmptyMessage(1);
	}

	private void processACStatusChange() {
		if (0 == mHVACPowerStatus) {
			return;
		}
		int id = R.drawable.air_conditioner_cyclemode_inside;

		if (0 != mHVACAutoIndicationStatus) {
			id = R.drawable.air_conditioner_cyclemode_outside;
		}

		if (null != mAcAutoDtatusImg) {
			mAcAutoDtatusImg.setBackgroundResource(id);
		}

		if (null != mAcStatusTv) {
			int color = Color.GRAY;

			if (0 != mHVACOnRequestIndication) {
				color = Color.WHITE;
			}

			mAcStatusTv.setTextColor(color);
		}

		id = R.drawable.air_conditioner_cyclemode_inside;
		if (1 == mHVACCycleMode) {
			id = R.drawable.air_conditioner_cyclemode_outside;
		} else if (2 == mHVACCycleMode) {
			id = R.drawable.air_conditioner_acmode_auto;
		}
		if (null != mAcAutoDtatusImg) {
			mAcAutoDtatusImg.setBackgroundResource(id);
		}

		AirConditionerMode mode = AirConditionerMode.values()[mHVACMode];
		switchMode(mode);

		if (null != mAirVolumeBar) {
			mAirVolumeBar.setProgress(mHVACAirVolume);
		}

		if (null != mAirTemperatureBar) {
			mAirTemperatureBar.setProgress(mHVACSetTemperature - 17);
		}
	}

	public void OnACStatusChange(int power, int auto, int ac, int cycleMode, int acMode, int airVolume,
			int temperature) {
		mHVACPowerStatus = power;
		mHVACAutoIndicationStatus = auto;
		mHVACOnRequestIndication = ac;
		mHVACCycleMode = cycleMode;
		mHVACMode = acMode;
		mHVACAirVolume = airVolume;
		mHVACSetTemperature = temperature;
		mHandler.sendEmptyMessage(2);
	}
}
