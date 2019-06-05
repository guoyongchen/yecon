package com.carocean.settings.screensaver;

import static android.constant.YeconConstants.*;
import static android.mcu.McuExternalConstant.*;

import java.util.ArrayList;

import com.carocean.settings.utils.SettingMethodManager;
import com.carocean.utils.Utils;
import com.carocean.utils.sLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.constant.YeconConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class ScreenSaverService extends Service {
	public static final String TAG = ScreenSaverService.class.getSimpleName();
	public static final String ACTION_RESET_SCREEN_SAVER_TIME = "ACTION_RESET_SCREEN_SAVER_TIME";
	public static final int HANDLER_SETTIMER = 100;
	public static final int HANDLER_SCREENSAVER = 101;

	private static ScreenSaverService gInst = null;
	private Activity mActivity = null;
	private ArrayList<String> mBlackList = new ArrayList<String>();
	private OpHandler mOpHandler = new OpHandler();
	private ResettimerBroadcastReceiver mResettimerBroadcastReceiver = null;
	public ScreensaverImpBind mScreensaverImpBind = new ScreensaverImpBind();
	private int mWaitTime = 0;

	public static ScreenSaverService getInstance() {
		return (gInst);
	}

	private boolean isAllowShowScreensaverPackageTop() {
		for (int i = 0; i < mBlackList.size(); i++) {
			if ((Utils.isActivityTopRunning(mBlackList.get(i))))
				return false;
		}
		return true;
	}

	private void startScreenSaver() {
		Intent localIntent = new Intent();
		localIntent.setClass(this, ScreenSaverActivity.class);
		localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(localIntent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mScreensaverImpBind;
	}

	public void onCreate() {
		super.onCreate();
		gInst = this;
		mBlackList.add(YeconConstants.VIDEO_START_ACTIVITY);
		mResettimerBroadcastReceiver = new ResettimerBroadcastReceiver();
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(ACTION_RESET_SCREEN_SAVER_TIME);
		localIntentFilter.addAction(MCU_ACTION_ACC_OFF);
		localIntentFilter.addAction(MCU_ACTION_ACC_ON);
		localIntentFilter.addAction(ACTION_QB_POWEROFF);
		localIntentFilter.addAction(ACTION_QB_POWERON);
		localIntentFilter.addAction(ACTION_BACKCAR_START);
		localIntentFilter.addAction(ACTION_BACKCAR_STOP);
		registerReceiver(this.mResettimerBroadcastReceiver, localIntentFilter);

		localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		localIntentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		localIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		localIntentFilter.addDataScheme("file");
		registerReceiver(mMediaDeviceReceiver, localIntentFilter);

		mScreensaverImpBind.resetTime();
	}

	public void onDestroy() {
		unregisterReceiver(mResettimerBroadcastReceiver);
		super.onDestroy();
	}

	@SuppressLint("HandlerLeak")
	class OpHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_SETTIMER:
				mScreensaverImpBind.resetTime();
				break;
			case HANDLER_SCREENSAVER:
				sLog.d(TAG, ".................HANDLER_SCREENSAVER");
				if (isAllowShowScreensaverPackageTop() && !SettingMethodManager.getInstance(gInst).isBackCar()) {
					if (mActivity == null) {
						startScreenSaver();
					} else {
						ActivityManager am = (ActivityManager) gInst.getSystemService(Context.ACTIVITY_SERVICE);
						am.moveTaskToFront(mActivity.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
					}
				}
				break;
			default:
				break;
			}
		}
	}

	class ResettimerBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			sLog.d(TAG, "......onReceive:" + action);
			if (action.equals(ACTION_RESET_SCREEN_SAVER_TIME)) {
				if (mOpHandler.hasMessages(HANDLER_SETTIMER))
					mOpHandler.removeMessages(HANDLER_SETTIMER);
				if (mOpHandler.hasMessages(HANDLER_SCREENSAVER))
					mOpHandler.removeMessages(HANDLER_SCREENSAVER);
				mOpHandler.sendEmptyMessageDelayed(HANDLER_SETTIMER, 500);
			} else if (action.equals(MCU_ACTION_ACC_OFF) || action.equals(ACTION_QB_POWEROFF) || action.equals(ACTION_BACKCAR_START)) {
				if (mOpHandler.hasMessages(HANDLER_SETTIMER))
					mOpHandler.removeMessages(HANDLER_SETTIMER);
				if (mOpHandler.hasMessages(HANDLER_SCREENSAVER))
					mOpHandler.removeMessages(HANDLER_SCREENSAVER);
			} else if (action.equals(MCU_ACTION_ACC_ON) || action.equals(ACTION_QB_POWERON) || action.equals(ACTION_BACKCAR_STOP)) {
				mScreensaverImpBind.resetTime();
			}
		}
	}

	private BroadcastReceiver mMediaDeviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String path = intent.getData().getPath();
			sLog.i(TAG, "mMediaDeviceReceiver - action: " + action + " - path: " + path);
			if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {

			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				mScreensaverImpBind.detachActivity();
				mScreensaverImpBind.resetTime();
			}
		}
	};

	class ScreensaverImpBind extends Binder implements IScreenSaverControl {

		@Override
		public void attachActivity(Activity paramActivity) {
			// TODO Auto-generated method stub
			if ((null != mActivity) && (mActivity != paramActivity)) {
				mActivity.finish();
			}
			mActivity = paramActivity;
		}

		@Override
		public void detachActivity() {
			// TODO Auto-generated method stub
			if (mActivity != null) {
				mActivity.finish();
				mActivity = null;
			}
		}

		@Override
		public void resetTime() {
			if (mOpHandler.hasMessages(HANDLER_SCREENSAVER))
				mOpHandler.removeMessages(HANDLER_SCREENSAVER);
			mWaitTime = getScreenSaverTimer();
			sLog.d(TAG, ".................resetTime:" + mWaitTime);
			if (mWaitTime > 0) {
				mOpHandler.sendEmptyMessageDelayed(HANDLER_SCREENSAVER, mWaitTime);
			}
		}
	}

	int getScreenSaverTimer() {
		return scUtils.ScreenSaverTimes[scUtils.timesIndex];
	}

}