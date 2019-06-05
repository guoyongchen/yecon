package com.carocean.floatwindow;

import static android.constant.YeconConstants.BLUETOOTH_START_ACTIVITY;
import static android.constant.YeconConstants.FMRADIO_START_ACTIVITY;
import static android.constant.YeconConstants.MEDIA_START_ACTIVITY;
import static android.mcu.McuExternalConstant.PROPERTY_KEY_POWERKEY;

import com.carocean.floatwindow.fUtils.FLOAT_WINDOW_TYPE;
import com.carocean.utils.Utils;
import com.carocean.utils.sLog;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;
import com.yecon.settings.YeconSettings;

import android.app.Service;
import android.constant.YeconConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
/**
 * @ClassName: FloatWindowService
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class FloatWindowService extends Service {

	private static final String TAG = "FloatWindowService";
	private final static int handler_hide_floatwindow = 300;
	private FloatWindowManager mFloatWindowManager = null;

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
		static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		static public final String SYSTEM_DIALOG_REASON_BACK_KEY = "backkey";
		static public final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
		private int mSaveNaviScreenMode = YeconSettings.SCREEN_MODE_FULL_BOTTOM;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reason != null) {
					if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals(SYSTEM_DIALOG_REASON_BACK_KEY)
							|| reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
						if (mFloatWindowManager != null) {
							mFloatWindowManager.removeFloatWindow();
						}
					}
				}
			} else if (action.equals(YeconSettings.ACTION_SHOW_LAUNCHER_FLOATWINDOW)) {
				boolean bShow = intent.getBooleanExtra("window_show", false);
				if (bShow) {
					int index = intent.getIntExtra("window_type", FLOAT_WINDOW_TYPE.FLOAT_MEDIA.ordinal());
					
					if (index == FLOAT_WINDOW_TYPE.FLOAT_MEDIA.ordinal()) {
						ViewType viewType = PageMedia.getInstance().getView();
						if (viewType == ViewType.ViewUsbMusic || viewType == ViewType.ViewUsbMusicFile) {
							mFloatWindowManager.createFloatWindow(FLOAT_WINDOW_TYPE.values()[index]);
							if (YeconSettings.getAutoNaviScreenMode() != YeconSettings.SCREEN_MODE_SPLIT_BOTTOM) {
								YeconSettings.setAutoNaviScreenMode(context, YeconSettings.SCREEN_MODE_SPLIT_BOTTOM);
							}
						} else {
							mFloatWindowManager.removeFloatWindow();
						}
					} else if (index == FLOAT_WINDOW_TYPE.FLOAT_BT.ordinal() || index == FLOAT_WINDOW_TYPE.FLOAT_RADIO.ordinal()){
						mFloatWindowManager.createFloatWindow(FLOAT_WINDOW_TYPE.values()[index]);
						if (YeconSettings.getAutoNaviScreenMode() != YeconSettings.SCREEN_MODE_SPLIT_BOTTOM) {
							YeconSettings.setAutoNaviScreenMode(context, YeconSettings.SCREEN_MODE_SPLIT_BOTTOM);
						}
					} else {
						mFloatWindowManager.removeFloatWindow();
					}
				}
			} else if (action.equals(fUtils.ACTION_CLOSE_FLOATWINDOW)) {
				if (mFloatWindowManager != null) {
					mFloatWindowManager.removeFloatWindow();
				}
			} else if (action.equals(fUtils.ACTIVITY_CHANGE)) {
				Bundle bundle = intent.getExtras();
				String szState = bundle.getString("state");
				String szPkg = bundle.getString("package");
				String szClass = bundle.getString("class");
				StringBuffer log = new StringBuffer();
				log.append("state: ").append(szState).append(" - szPkg: ").append(szPkg).append(" - class: ")
						.append(szClass);
				sLog.e(TAG, "....ACTIVITY_CHANGE......................................" + log.toString());
				if (szState.equals(fUtils.ACTIVITY_FG)) {
					if (szPkg.equalsIgnoreCase(fUtils.MAP_PACKAGE_NAME_DEFAULT)) {
						if (mFloatWindowManager.isExistFloatWindow()) {
							YeconSettings.setAutoNaviScreenMode(context, YeconSettings.SCREEN_MODE_SPLIT_BOTTOM);
						} else {
							YeconSettings.setAutoNaviScreenMode(context, YeconSettings.SCREEN_MODE_FULL_BOTTOM);
						}
					} else {
						if (mHandler.hasMessages(handler_hide_floatwindow))
							mHandler.removeMessages(handler_hide_floatwindow);
						if (szClass.equalsIgnoreCase(MEDIA_START_ACTIVITY)
								|| szClass.equalsIgnoreCase(FMRADIO_START_ACTIVITY)
								|| szClass.equalsIgnoreCase(BLUETOOTH_START_ACTIVITY)) {
							// mHandler.sendEmptyMessageDelayed(handler_hide_floatwindow,
							// 300);
						} else {
							mFloatWindowManager.removeFloatWindow();
						}
					}
				}
			} else if (action.equals(fUtils.ACTION_TOUCH_CALIBRATION)) {
			} else if (action.equals("AUTONAVI_STANDARD_BROADCAST_SEND")) {
				int keytype = intent.getIntExtra("KEY_TYPE", 0);
				if (keytype == 10019) {
					int naviStatus = intent.getIntExtra("EXTRA_STATE", 0);
					if (naviStatus == 0 || naviStatus == 2 || naviStatus == 3 || naviStatus == 4)
						YeconSettings.setAutoNaviState(naviStatus);
					sLog.d(TAG, "............AUTONAVI_STANDARD_BROADCAST_SEND.....EXTRA_STATE....." + naviStatus);
				}
			} else if (action.equals(YeconConstants.ACTION_BACKCAR_START)) {
				// fUtils.showNavigationBar(true);
				boolean bPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				if (!bPowerKey) {
					mSaveNaviScreenMode = YeconSettings.getAutoNaviScreenMode();
					YeconSettings.setAutoNaviScreenMode(context, YeconSettings.SCREEN_MODE_SPLIT_BOTTOM);
				}
			} else if (action.equals(YeconConstants.ACTION_BACKCAR_STOP)) {
				boolean bPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				if (!bPowerKey) {
					YeconSettings.setAutoNaviScreenMode(context, mSaveNaviScreenMode);
				}
			}
		};
	};

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case handler_hide_floatwindow:
				if (!Utils.isAppTopRunning(fUtils.MAP_PACKAGE_NAME_DEFAULT)) {
					mFloatWindowManager.removeFloatWindow();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction(fUtils.ACTION_CLOSE_FLOATWINDOW);
		filter.addAction(fUtils.ACTIVITY_CHANGE);
		filter.addAction(fUtils.ACTION_TOUCH_CALIBRATION);
		filter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
		filter.addAction("com.autonavi.amapauto.intent.action.CMD_NAVI2CCS");
		filter.addAction(YeconConstants.ACTION_BACKCAR_START);
		filter.addAction(YeconConstants.ACTION_BACKCAR_STOP);
		filter.addAction(YeconSettings.ACTION_SHOW_LAUNCHER_FLOATWINDOW);
		registerReceiver(mBroadcastReceiver, filter);

		mFloatWindowManager = FloatWindowManager.getInstance();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mFloatWindowManager != null) {
			mFloatWindowManager.removeFloatWindow();
		}
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
