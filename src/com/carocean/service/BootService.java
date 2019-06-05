package com.carocean.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.backlight.BacklightControl;
import android.cbm.CBManager;
import android.constant.YeconConstants;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.mcu.McuBaseInfo;
import android.mcu.McuDeviceStatusInfo;
import android.mcu.McuExternalConstant;
import android.mcu.McuListener;
import android.mcu.McuManager;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.autochips.backcar.BackCar;
import com.autochips.storage.EnvironmentATC;
import com.carocean.ApplicationManage;
import com.carocean.KuwoFunc;
import com.carocean.R;
import com.carocean.backcar.BackCarActivity;
import com.carocean.backcar.BackCarView;
import com.carocean.bt.BTUtils;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.media.constants.MediaPlayerContants;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.radio.RadioSearchFragment;
import com.carocean.radio.constants.RadioConstants;
import com.carocean.settings.utils.PersistUtils;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.settings.utils.timeUtils;
import com.carocean.settings.utils.tzUtils;
import com.carocean.settings.utils.verUtils;
import com.carocean.t19can.CanBusService;
import com.carocean.t19can.T19CanRx;
import com.carocean.t19can.T19CanRx.AirInfo;
import com.carocean.t19can.T19CanTx;
import com.carocean.utils.AppManager;
import com.carocean.utils.Constants;
import com.carocean.utils.SourceManager;
import com.carocean.utils.Utils;
import com.carocean.utils.VoiceMsgDefine;
import com.carocean.utils.sLog;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vmedia.bt.PageBT;
import com.carocean.vmedia.bt.PageBT.PageType;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;
import com.yecon.common.YeconEnv;
import com.yecon.metazone.YeconMetazone;
import com.yecon.settings.YeconSettings;

import java.util.Calendar;

import static android.constant.YeconConstants.ACTION_BACKCAR_START;
import static android.constant.YeconConstants.ACTION_BACKCAR_STOP;
import static android.constant.YeconConstants.ACTION_QB_POWEROFF;
import static android.constant.YeconConstants.ACTION_SWITCH_MODE;
import static android.constant.YeconConstants.INTENT_EXTRA_SWITCH_MODE;
import static android.constant.YeconConstants.PROPERTY_BACKCAR_MUTE_EANBLE;
import static android.constant.YeconConstants.PROPERTY_KEY_STARTBACKCAR;
import static android.mcu.McuExternalConstant.K_POWER;
import static android.mcu.McuExternalConstant.MCU_ACTION_ACC_OFF;
import static android.mcu.McuExternalConstant.MCU_DEVICE_STATUS_INFO_TYPE;
import static android.mcu.McuExternalConstant.MCU_SOURCE_OFF;
import static android.mcu.McuExternalConstant.PROPERTY_KEY_BACKOUTKEY;
import static android.mcu.McuExternalConstant.PROPERTY_KEY_POWERKEY;
import static android.mcu.McuExternalConstant.T_BLACKOUT;
import static com.carocean.backcar.BackCarConstants.BACKCAR_STATUS_START;
import static com.carocean.backcar.BackCarConstants.BACKCAR_STATUS_STOP;
import static com.carocean.backcar.BackCarConstants.PERSYS_BACKCAR_ENABLE;

public class BootService extends Service {

	private static final String TAG = "BootService";

	public static final String ACTION_IFLY_VOICE_APP = "action.com.carocean.iflyvoice.app";// 发送打开关闭的广播
	public static final String EXTRA_APP_OPERATION = "operation";// 1 打开// 2 关闭
	public static final String EXTRA_APP_ID = "id";// int 类型

    public static final String ACTION_KUWO_FUNC = "action.com.carocean.kuwo_func";
    public static final String EXTRA_KUWO_CMD = "cmd";// int 类型
    public static final int KUWO_FUNC_CMD_EXIT = 1;//退出酷我

	/**
	 * msg id
	 */
	private static final int MSG_ID_CHECK_ARM2_BACKCAR_STATUS = 1;
	private static final int MSG_ID_START_APP_SOURCE = 2;
	private static final int MSG_ID_CHECK_MEDIA_SOURCE = 3;

	// backcar msg id
	private static final int MSG_BACKCAR_START = 31;
	private static final int MSG_BACKCAR_STOP = 32;
	private static final int MSG_BACKCAR_STARTACTIVITY = 33;
	private static final int MSG_BACKCAR_STOPACTIVITY = 34;
	private static final int MSG_BACKCAR_SIGNAL_READY = 35;
	private static final int MSG_BACKCAR_SIGNAL_LOST = 36;
	private static final int MSG_ON_ANGLE_CHANGE = 37;
	private static final int MSG_ID_BKL_ENABLE = 38;
	private static final int MSG_BKL_POWER_DISENABLE = 39;
	private static final int MSG_BKL_BLACKOUT_DISENABLE = 40;

	/**
	 * delay time
	 */
	private static final int DELAY_ARM2_BACKCAR_STATUS_CHECK = 200;
	private static final int DELAY_START_APP_SOURCE = 1500;

	private static final int DELAY_STARTACTIVITY = 300;
	private static final int DELAY_DISPLAY_NO_SIGNAL = 2000;
	private static final int DELAY_CHECK_MEDIA_SOURCE = 800;
	private static final int BKL_ENABLE_TIMEOUT = 1000;

	// arm2 backcar status
	private static final int ARM2_BACKCAR_STATUS_IN_ARM2 = 1;
	private static final int ARM2_BACKCAR_STATUS_EXIT_ARM2 = 2;

	private static final int PAL_WIDTH = 720; // 720;
	private static final int PAL_HEIGHT = 576; // 576;

	private static final int MEDIA_CHECK_MAX_COUNT = 10;

	private static CBManager gCBM = null;

	private static BootService gInst = null;

	private Context mContext;

	private McuManager mMcuManager;

	// backcar object start
	private BackCar mBackCar;

	private boolean mBackCarNeedInit = true;

	private boolean mConfigBackCarMuted = false;

	private boolean mBackCarStarted = false;
	private boolean mSignalReady = false;
	// private boolean mbNoSignalShow = false;

	private int mBackCarPort = 4;

	private WindowManager mWindowManager = null;
	private View mBackCarMainView = null;
	private BackCarView mBackCarView = null;
	private TextView mTVBackCarNoSignal = null;
	private ImageView mTraceView;

	private boolean mIsBCViewAdded = false;

	private Activity mBackCarActivity;
	private boolean mIsReversing = false;

	private int mWidth;
	private int mHeight;
	// add for ATE
	private int mIsSignal = 0;
	// backcar object end

	private int mMediaCheckCount;
	private EnvironmentATC mEnv = null;
	private AudioManager mAudioManager;
	private boolean mIsMusicActive = false;

	private int mCurrentTrackAngle = 0;
	private final int ID_DRAWABLE_TRACE_LEFT[] = { R.drawable.trace_left_1, R.drawable.trace_left_2,
			R.drawable.trace_left_3, R.drawable.trace_left_4, R.drawable.trace_left_5, R.drawable.trace_left_6,
			R.drawable.trace_left_7, R.drawable.trace_left_8, R.drawable.trace_left_9, R.drawable.trace_left_10,
			R.drawable.trace_left_11, R.drawable.trace_left_12, R.drawable.trace_left_13, R.drawable.trace_left_14,
			R.drawable.trace_left_15, R.drawable.trace_left_16, R.drawable.trace_left_17, R.drawable.trace_left_18,
			R.drawable.trace_left_19, R.drawable.trace_left_20, R.drawable.trace_left_21, R.drawable.trace_left_22,
			R.drawable.trace_left_23, R.drawable.trace_left_24, R.drawable.trace_left_25, R.drawable.trace_left_26,
			R.drawable.trace_left_27, R.drawable.trace_left_28, R.drawable.trace_left_29, R.drawable.trace_left_30,
			R.drawable.trace_left_31, R.drawable.trace_left_32, R.drawable.trace_left_33, R.drawable.trace_left_34,
			R.drawable.trace_left_35, R.drawable.trace_left_36, };

	private final int ID_DRAWABLE_TRACE_RIGHT[] = { R.drawable.trace_right_1, R.drawable.trace_right_2,
			R.drawable.trace_right_3, R.drawable.trace_right_4, R.drawable.trace_right_5, R.drawable.trace_right_6,
			R.drawable.trace_right_7, R.drawable.trace_right_8, R.drawable.trace_right_9, R.drawable.trace_right_10,
			R.drawable.trace_right_11, R.drawable.trace_right_12, R.drawable.trace_right_13, R.drawable.trace_right_14,
			R.drawable.trace_right_15, R.drawable.trace_right_16, R.drawable.trace_right_17, R.drawable.trace_right_18,
			R.drawable.trace_right_19, R.drawable.trace_right_20, R.drawable.trace_right_21, R.drawable.trace_right_22,
			R.drawable.trace_right_23, R.drawable.trace_right_24, R.drawable.trace_right_25, R.drawable.trace_right_26,
			R.drawable.trace_right_27, R.drawable.trace_right_28, R.drawable.trace_right_29, R.drawable.trace_right_30,
			R.drawable.trace_right_31, R.drawable.trace_right_32, R.drawable.trace_right_33, R.drawable.trace_right_34,
			R.drawable.trace_right_35, R.drawable.trace_right_36, };

	// private Drawable[] DRAWABLE_TRACE_LEFT = new
	// Drawable[ID_DRAWABLE_TRACE_LEFT.length];
	// private Drawable[] DRAWABLE_TRACE_RIGHT = new
	// Drawable[ID_DRAWABLE_TRACE_RIGHT.length];

	private McuListener mMcuListener = new McuListener() {

		@Override
		public void onMcuInfoChanged(McuBaseInfo mcuBaseInfo, int infoType) {
			if (mcuBaseInfo == null) {
				return;
			}

			if (infoType == MCU_DEVICE_STATUS_INFO_TYPE) {
				if (!SystemProperties.getBoolean(PERSYS_BACKCAR_ENABLE, true)) {
					return;
				}

				McuDeviceStatusInfo info = mcuBaseInfo.getDeviceStatusInfo();
				if (info == null) {
					return;
				}

				int backcarStatus = info.getBackcarStatus();
				showBackCar(backcarStatus);
			} else if (infoType == McuExternalConstant.MCU_CANBUS_INFO_TYPE) {
				// 倒车轨迹
				Log.e(TAG, "data: " + mcuBaseInfo.getOriginalInfo().getMcuData());
				if (mcuBaseInfo.getOriginalInfo().getMcuData().length > 0) {
					parseTrace(mcuBaseInfo.getOriginalInfo().getMcuData());

				}
			}
		}

	};

	private void parseTrace(byte[] packet) {

		byte bData0 = packet[0];
		byte bData1 = packet[1];
		int sid = bData0 & 0xFF;

		if (sid == 0x03) {
			if (packet.length == 3) {
				int angle = -36 + (bData1 & 0xFF);
				Log.e(TAG, "angle: " + angle);
				if (mCurrentTrackAngle != angle) {
					mCurrentTrackAngle = angle;
					mHandler.sendEmptyMessage(MSG_ON_ANGLE_CHANGE);
				}
			}
		}

	}

	private BackCar.OnSignalListener mBackCarListenerSignal = new BackCar.OnSignalListener() {

		@Override
		public void onSignal(int msg, int param1, int param2) {
			switch (msg) {
			case BackCar.SIGNAL_READY:
				sLog.i(TAG, "BackCar.SIGNAL_READY - BackCar.SIGNAL_READY");
				mIsSignal = 1;
				mWidth = param1;
				mHeight = param2;

				mHandler.removeMessages(MSG_BACKCAR_SIGNAL_LOST);
				mHandler.sendEmptyMessage(MSG_BACKCAR_SIGNAL_READY);
				break;

			case BackCar.SIGNAL_LOST:
				mIsSignal = 0;
				sLog.i(TAG, "BackCar.SIGNAL_LOST - BackCar.SIGNAL_LOST");
				mHandler.removeMessages(MSG_BACKCAR_SIGNAL_READY);
				mHandler.sendEmptyMessageDelayed(MSG_BACKCAR_SIGNAL_LOST, DELAY_DISPLAY_NO_SIGNAL);
				break;

			default:
				return;
			}
		}
	};

	private Handler mHandler = new Handler() {

		@SuppressLint("NewApi")
		private boolean handleBackCarMessage(int what) {
			boolean ret = true;
			switch (what) {
			case MSG_BACKCAR_START:
				mIsReversing = true;
				removeMessages(MSG_BACKCAR_STARTACTIVITY);
				sendEmptyMessageDelayed(MSG_BACKCAR_STARTACTIVITY, DELAY_STARTACTIVITY);
				if (SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_MUTE, false)) {
					requestFocus();
					if (BTUtils.mBluetooth.iscallidle()) {
						try {
							mMcuManager.RPC_SetVolumeMute(true);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				// 倒车降音
				if (SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_REDUCE_VOLUME, true)) {
					if (mAudioManager == null) {
						mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
					}
					int cur_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					boolean bMuted = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);

					if (!bMuted && cur_volume != 31) {
						SystemProperties.set("sys.volume_gain", "sub3");
						Log.i(TAG, "cur_volume = " + cur_volume + " bMuted = " + bMuted);
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cur_volume, AudioManager.ADJUST_SAME);
					}

				}
				break;

			case MSG_BACKCAR_STOP:
				mIsReversing = false;

				mBackCarView.setVisibility(View.INVISIBLE);
				mTVBackCarNoSignal.setVisibility(View.INVISIBLE);
				mTraceView.setVisibility(View.INVISIBLE);
				if (SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_MUTE, false)) {
					abandonFocus();
					try {
						mMcuManager.RPC_SetVolumeMute(false);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				if (mIsBCViewAdded) {
					try {
						mWindowManager.removeView(mBackCarMainView);
					} catch (Exception e) {
						e.printStackTrace();
					}
					mIsBCViewAdded = false;
					sLog.e(TAG, "MSG_BACKCAR_STOP - mWindowManager.removeView");
				} else {
					sLog.e(TAG, "MSG_BACKCAR_STOP - BCView is already removed");
				}
				removeMessages(MSG_BACKCAR_SIGNAL_READY);
				removeMessages(MSG_BACKCAR_SIGNAL_LOST);
				removeMessages(MSG_BACKCAR_STARTACTIVITY);
				removeMessages(MSG_BACKCAR_STOPACTIVITY);

				if (mBackCarActivity != null) {
					mSignalReady = false;

					mBackCarActivity.finish();
					mBackCarActivity = null;
					sLog.e(TAG, "MSG_BACKCAR_STOP - BackCarActivity finish");
				} else {
					sLog.e(TAG, "MSG_BACKCAR_STOP - gInst is null!");
				}

				YeconSettings.initVideoRgb(YeconSettings.RGBTYPE.USB);

				// 倒车降音
				if (SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_REDUCE_VOLUME, true)) {
					if (mAudioManager == null) {
						mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
					}
					int cur_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					boolean bMuted = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);

					if (!bMuted && cur_volume != 31) {
						SystemProperties.set("sys.volume_gain", "sub10");
						Log.i(TAG, "MSG_BACKCAR_STOP----cur_volume = " + cur_volume + " bMuted = " + bMuted);
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cur_volume, AudioManager.ADJUST_SAME);
					}

				}
				break;

			case MSG_BACKCAR_STARTACTIVITY:
				Intent intent = new Intent(mContext, BackCarActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("IsSignalReady", mSignalReady);
				startActivity(intent);

				WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
				// lp.width = mScreenW;
				// lp.height = mScreenH;
				lp.x = 0;
				lp.y = 0;
				lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				lp.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.STATUS_BAR_HIDDEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

				if (!mIsBCViewAdded) {
					mWindowManager.addView(mBackCarMainView, lp);
					mIsBCViewAdded = true;
					sLog.i(TAG, "MSG_BACKCAR_START - mWindowManager.addView");
				} else {
					sLog.i(TAG, "MSG_BACKCAR_START - BCView is already added");
				}

				YeconSettings.initVideoRgb(YeconSettings.RGBTYPE.BACKCAR);
				break;

			case MSG_BACKCAR_STOPACTIVITY:
				removeMessages(MSG_BACKCAR_SIGNAL_READY);
				if (null != mBackCarActivity) {
					mBackCarActivity.finish();
					mBackCarActivity = null;
					sLog.i(TAG, "MSG_BACKCAR_STOPACTIVITY - BackCarActivity finish 2222");
				} else {
					sLog.i(TAG, "MSG_BACKCAR_STOPACTIVITY - mActivity is null 222222!");
				}
				break;

			case MSG_BACKCAR_SIGNAL_READY:
				sLog.i(TAG, "MSG_BACKCAR_SIGNAL_READY - mWidth: " + mWidth + " - mHeight: " + mHeight);
				removeMessages(MSG_BACKCAR_SIGNAL_LOST);

				if (null != mBackCarView) {
					mBackCarView.getHolder().setFixedSize(mWidth, mHeight);
					mBackCarView.requestLayout();
					if (mBackCarView.getVisibility() == View.INVISIBLE) {
						mBackCarView.setVisibility(View.VISIBLE);
					}
				}

				if (null != mTraceView) {
					if (mTraceView.getVisibility() == View.INVISIBLE) {
						mTraceView.setVisibility(View.VISIBLE);
					}
				}

				if (null != mTVBackCarNoSignal) {
					mTVBackCarNoSignal.setVisibility(View.INVISIBLE);
				}
				break;

			case MSG_BACKCAR_SIGNAL_LOST:
				sLog.i(TAG, "MSG_BACKCAR_SIGNAL_LOST - MSG_SIGNAL_LOST");
				removeMessages(MSG_BACKCAR_SIGNAL_READY);

				mBackCarView.setVisibility(View.INVISIBLE);

				mTVBackCarNoSignal.setVisibility(View.VISIBLE);
				mTraceView.setVisibility(View.VISIBLE);
				break;

			case MSG_ON_ANGLE_CHANGE:
				Log.e(TAG, "mCurrentTrackAngle: " + mCurrentTrackAngle);
				if (Math.abs(mCurrentTrackAngle) <= ID_DRAWABLE_TRACE_LEFT.length) {

					if (mCurrentTrackAngle > 0) {

						// if (DRAWABLE_TRACE_RIGHT[mCurrentTrackAngle - 1] ==
						// null) {
						// DRAWABLE_TRACE_RIGHT[mCurrentTrackAngle - 1] =
						// mContext.getResources()
						// .getDrawable(ID_DRAWABLE_TRACE_RIGHT[mCurrentTrackAngle
						// - 1]);
						// }
						// mTraceView.setBackground(DRAWABLE_TRACE_RIGHT[mCurrentTrackAngle
						// - 1]);
						mTraceView.setBackground(
								mContext.getResources().getDrawable(ID_DRAWABLE_TRACE_RIGHT[mCurrentTrackAngle - 1]));

					} else if (mCurrentTrackAngle < 0) {

						// if (DRAWABLE_TRACE_LEFT[(Math.abs(mCurrentTrackAngle)
						// - 1)] == null) {
						// DRAWABLE_TRACE_LEFT[(Math.abs(mCurrentTrackAngle) -
						// 1)] = mContext.getResources()
						// .getDrawable(ID_DRAWABLE_TRACE_LEFT[(Math.abs(mCurrentTrackAngle)
						// - 1)]);
						// }
						// mTraceView.setBackground(DRAWABLE_TRACE_LEFT[(Math.abs(mCurrentTrackAngle)
						// - 1)]);
						mTraceView.setBackground(mContext.getResources()
								.getDrawable(ID_DRAWABLE_TRACE_LEFT[(Math.abs(mCurrentTrackAngle) - 1)]));

					} else if (mCurrentTrackAngle == 0) {
						mTraceView.setBackground(mContext.getResources().getDrawable(R.drawable.trace_bg));
					}
					mTraceView.setVisibility(View.VISIBLE);
				}
				break;

			case MSG_ID_BKL_ENABLE: {
				boolean isPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				if (isPowerKey) {
					sendPowerKeyCMD(false);
				}
				boolean bklEnable = BacklightControl.GetBklEnable();
				Log.e(TAG, "MSG_ID_BKL_ENABLE - bklEnable: " + bklEnable);
				if (!bklEnable) {
					int brightness = BacklightControl.getBrightness();
					int backlightLevel = BacklightControl.getBacklightLevel(brightness);
					Log.e(TAG, "brightness: " + brightness + " backlightLevel: " + backlightLevel);
					setBacklight(1, 1, 0, backlightLevel);
				}
				break;
			}

			case MSG_BKL_POWER_DISENABLE: {
				boolean isPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				Log.e(TAG, "MSG_BKL_POWER_DISENABLE - isPowerKey: " + isPowerKey);
				if (isPowerKey && BTUtils.mBluetooth.iscallidle()) {
					sendPowerKeyCMD(true);
					setBacklight(0, 1, 1, 0);
				}
				break;
			}

			case MSG_BKL_BLACKOUT_DISENABLE: {
				boolean isPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				boolean isBlackoutKey = SystemProperties.getBoolean(PROPERTY_KEY_BACKOUTKEY, false);

				Log.e(TAG, "MSG_BKL_BLACKOUT_DISENABLE - isPowerKey: " + isPowerKey + " - isBlackoutKey: "
						+ isBlackoutKey);

				if (isBlackoutKey && !isPowerKey && BTUtils.mBluetooth.iscallidle()) {
					setBacklight(0, 1, 0, 0);
				}
				break;
			}

			default:
				ret = false;
				break;
			}

			return ret;
		}

		@Override
		public void handleMessage(Message msg) {
			if (handleBackCarMessage(msg.what)) {
				return;
			}

			switch (msg.what) {
			case MSG_ID_CHECK_ARM2_BACKCAR_STATUS:
				handleCheckArm2BackcarStatus();
				break;

			case MSG_ID_START_APP_SOURCE:
				SourceManager.startSourceApp(mContext);
				break;

			case MSG_ID_CHECK_MEDIA_SOURCE:
				handleCheckMediaSource();
				break;
			}
		}

		private void handleCheckArm2BackcarStatus() {
			int arm2BackCarStatus = YeconMetazone.GetBackCarStatus();
			sLog.i(TAG, "BootService - Handler - arm2BackCarStatus: " + arm2BackCarStatus);
			if (arm2BackCarStatus == ARM2_BACKCAR_STATUS_IN_ARM2) {
				removeMessages(MSG_ID_CHECK_ARM2_BACKCAR_STATUS);
				sendEmptyMessageDelayed(MSG_ID_CHECK_ARM2_BACKCAR_STATUS, DELAY_ARM2_BACKCAR_STATUS_CHECK);
			} else if (arm2BackCarStatus == ARM2_BACKCAR_STATUS_EXIT_ARM2) {
				sendEmptyMessageDelayed(MSG_ID_START_APP_SOURCE, DELAY_START_APP_SOURCE);
			}
		}

		private void handleCheckMediaSource() {
			boolean keepCheckMedia = false;
			mMediaCheckCount++;
			sLog.i(TAG, "handleCheckMediaSource - mMediaCheckCount: " + mMediaCheckCount);
			if (mMediaCheckCount < MEDIA_CHECK_MAX_COUNT) {
				keepCheckMedia = true;
			}
			if (keepCheckMedia) {
				String storageState = Environment.getStorageState(YeconEnv.UDISK1_PATH);
				if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
					checkMediaSource(DELAY_CHECK_MEDIA_SOURCE);
					return;
				}
			} else {
				SourceManager.setSource(mContext, Constants.KEY_SRC_MODE_FM);
			}

			mMediaCheckCount = 0;
			SourceManager.startSourceApp(mContext);
		}

	};

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
		static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		static public final String SYSTEM_DIALOG_REASON_BACK_KEY = "backkey";
		static public final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			sLog.d("...........intent.getAction():........." + action);
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals(SYSTEM_DIALOG_REASON_BACK_KEY)
                            || reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        if (Constants.isDebug) {
                            launcherUtils.startLauncher();
                        }
                    }
                }
            } else if (action.equals(ACTION_KUWO_FUNC)) {
			    int cmd = intent.getIntExtra(EXTRA_KUWO_CMD, 0);
			    if (cmd == KUWO_FUNC_CMD_EXIT) {
                    KuwoFunc.exitKuwo();
                }
			} else if (action.equals(ACTION_IFLY_VOICE_APP)) {
				int operation = intent.getIntExtra(EXTRA_APP_OPERATION, 0);
				int extra_app_id = intent.getIntExtra(EXTRA_APP_ID, 0);
				sLog.d("...........action:........." + action + "  operation: " + operation + "   extra_app_id: "
						+ extra_app_id);
				if (!isBackCarorBTPhone()) {
					if (operation == 1) {
						switch (extra_app_id) {
						case Constants.RADIO_ID:
							launcherUtils.startRadio();// 收音
							break;

						case Constants.MUSIC_ID:
							if (PageMedia.getView() != ViewType.ViewUsbMusic
									&& PageMedia.getView() != ViewType.ViewUsbMusicFile) {
								PageMedia.setView(ViewType.ViewUsbMusicFile);
							}
							launcherUtils.startMedia();// 音乐
							break;

						case Constants.VIDEO_ID:
							PageMedia.setView(ViewType.ViewVideoFile);
							launcherUtils.startMedia();// 视频
							break;

						case Constants.BT_ID:
						case Constants.PHONE_ID:
							PageBT.setPageType(PageType.TAB_INDEX_BT_CALL_BOOK);
							launcherUtils.startBT();// 蓝牙
							break;

						case Constants.IMAGE_ID:
							PageMedia.setView(ViewType.ViewPictureFile);
							launcherUtils.startMedia();// 图片
							break;

						case Constants.SETTING_ID:
							launcherUtils.startSettings();// 设置
							break;

						case Constants.IE_ID:
							//launcherUtils.startIe();// 浏览器
							break;

						case Constants.FILEBROWSE_ID:
							//launcherUtils.startFileManager();// 文件管理器
							break;

						case Constants.CALENDAR_ID:
							//launcherUtils.startCalendar();// 日历
							break;

						case Constants.CAR_ID:
							launcherUtils.startCarInfo("is_carInfo");// 车辆设置
							break;

						case Constants.MEDIA_ID:
							launcherUtils.startMedia();// 多媒体
							break;

						case Constants.KUWO_ID:
							KuwoFunc.startKuwo(true);
							break;

						case Constants.KAOLA_ID:
							launcherUtils.startklauto();// 考拉
							break;

						case Constants.NAVI_ID:
							launcherUtils.startNavi();// 导航
							break;

						case Constants.CARPLAY_ID:
							Utils.RunApp("com.zjinnova.zlink", "com.zjinnova.zlink.main.view.SplashActivity");// carplay
							break;

						case Constants.EASYNET_ID:
							Utils.RunApp("net.easyconn", "net.easyconn.WelcomeActivity");// 亿联
							break;
						case Constants.PHONEBOOK1_ID://电话本
						case Constants.PHONEBOOK2_ID:
						case Constants.PHONEBOOK3_ID:
						case Constants.PHONEBOOK4_ID:
							PageBT.setPageType(PageType.TAB_INDEX_BT_CONTACT);
							launcherUtils.startBT();
							break;
						case Constants.CALLHISTORY_ID://进入蓝牙通话记录界面
							PageBT.setPageType(PageType.TAB_INDEX_BT_RECORD);
							launcherUtils.startBT();
							break;
						case Constants.BT_MUSIC_ID:
							// Enter BT Music
							PageMedia.setView(ViewType.ViewBtMusic);
							launcherUtils.startMedia();
							break;
						case Constants.VOICE_ID:
							// Enter Voice
							launcherUtils.startVoice();
							break;
						default:
							break;
						}
					} else if (operation == 2) {
						switch (extra_app_id) {
						case Constants.RADIO_ID:
							// 关闭收音，收音界面处理
							break;

						case Constants.MUSIC_ID: // 关闭音乐
						case Constants.VIDEO_ID: // 关闭视频
						case Constants.BT_ID: // 关闭蓝牙
						case Constants.IMAGE_ID: // 关闭图片
						case Constants.SETTING_ID: // 关闭设置
						case Constants.CAR_ID: // 关闭车辆设置
						case Constants.MEDIA_ID: // 关闭多媒体
							if (MediaActivity.mActivity != null) {
								MediaActivity.mActivity.colsePage(extra_app_id);
							}
							break;

						case Constants.IE_ID:
							AppManager.killProcess(mContext, "com.android.browser");// 关闭浏览器
							break;

						case Constants.FILEBROWSE_ID:
							AppManager.killProcess(mContext, "com.yecon.filemanager");// 关闭文件管理器
							break;

						case Constants.CALENDAR_ID:
							AppManager.killProcess(mContext, "com.yecon.carsetting.Calendar");// 关闭日历
							break;

						case Constants.KUWO_ID:
                            KuwoFunc.exitKuwo();
							break;

						case Constants.KAOLA_ID:
							AppManager.killProcess(mContext, "com.edog.car");// 关闭考拉
							break;

						case Constants.NAVI_ID:
							AppManager.killProcess(mContext, "com.autonavi.amapauto");// 关闭导航
							break;

						case Constants.CARPLAY_ID:
							AppManager.killProcess(mContext, "com.zjinnova.zlink");// 关闭carplay
							break;

						case Constants.EASYNET_ID:
							AppManager.killProcess(mContext, "net.easyconn");// 关闭亿联
							break;
						case Constants.PHONE_ID: 
						case Constants.PHONEBOOK1_ID://电话本
						case Constants.PHONEBOOK2_ID:
						case Constants.PHONEBOOK3_ID:
						case Constants.PHONEBOOK4_ID:
							if (MediaActivity.mActivity != null) {
								MediaActivity.mActivity.colsePage(Constants.BT_ID);
							}
							break;
						case Constants.CALLHISTORY_ID:// 进入蓝牙通话记录界面
							if (MediaActivity.mActivity != null) {
								MediaActivity.mActivity.colsePage(Constants.BT_ID);
							}
							break;
						}
					}
				}

			} else if (action.equals(ACTION_SWITCH_MODE)) {
				int mode = intent.getIntExtra(INTENT_EXTRA_SWITCH_MODE, 0);
				sLog.i("ACTION_SWITCH_MODE - mode: " + mode);
				if (mode != 0) {
					switchSource(mode);
				} else {
					switchSource(modeSwitchFilter(SourceManager.getSource()));
				}
			} else if (action.equals(VoiceMsgDefine.ACTION_IFLY_VOICE_AIR)) {
				// 语音控制空调
				AirInfo mAirInfo = T19CanRx.getInstance().mAirInfo;
				int cmd = intent.getIntExtra(VoiceMsgDefine.EXTRA_AIR_CMD, -1);
				int temp = intent.getIntExtra(VoiceMsgDefine.EXTRA_AIR_TEMP, -1);
				int speed = intent.getIntExtra(VoiceMsgDefine.EXTRA_AIR_SPEED, -1);
				sLog.i("ACTION_IFLY_VOICE_AIR - cmd: " + cmd + "  temp: " + temp + "  speed: " + speed);
				switch (cmd) {
				case VoiceMsgDefine.AIR_CMD_INC_FANSPEED: // 增加风速,自动增加一级
					int add_ACP_BlowerLeverSet = mAirInfo.ACP_BlowerLeverSet + 1;
					if (add_ACP_BlowerLeverSet > 8) {
						add_ACP_BlowerLeverSet = 8;
					}
					mAirInfo.ACP_BlowerLeverSet = add_ACP_BlowerLeverSet;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_INC_FANSPEED - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_DEC_FANSPEED: // 减少风速 自动减小一级
					int reduve_ACP_BlowerLeverSet = mAirInfo.ACP_BlowerLeverSet - 1;
					if (reduve_ACP_BlowerLeverSet < 1) {
						reduve_ACP_BlowerLeverSet = 1;
					}
					mAirInfo.ACP_BlowerLeverSet = reduve_ACP_BlowerLeverSet;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_DEC_FANSPEED - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_INC_NUM_FANSPEED: // 增加多少风速,需要去读speed
					int add_speed = mAirInfo.ACP_BlowerLeverSet + speed;
					if (add_speed > 8) {
						add_speed = 8;
					}
					mAirInfo.ACP_BlowerLeverSet = add_speed;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_INC_NUM_FANSPEED - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_DEC_NUM_FANSPEED: // 减小多少风速,//
																// 需要去读speed
					int reduce_speed = mAirInfo.ACP_BlowerLeverSet - speed;
					if (reduce_speed < 1) {
						reduce_speed = 1;
					}
					mAirInfo.ACP_BlowerLeverSet = reduce_speed;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_DEC_NUM_FANSPEED - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_SET_NUM_FANSPEED: // 风速设为多少,需要去读speed
					if (speed < 1) {
						speed = 1;
					}
					if (speed > 8) {
						speed = 8;
					}
					mAirInfo.ACP_BlowerLeverSet = speed;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_SET_NUM_FANSPEED - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_OPEN_AIR: // 打开空调

					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 2;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_OPEN_AIR - mAirInfo.ACP_BlowerLeverSet: " + mAirInfo.ACP_BlowerLeverSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);

					Intent open_intent = new Intent(CanBusService.SHOWAIRACRION);
					open_intent.putExtra("isShow", true);
					mContext.sendBroadcastAsUser(open_intent, UserHandle.ALL);

					break;
				case VoiceMsgDefine.AIR_CMD_CLOSE_AIR: // 关闭空调

					mAirInfo.ACP_OFF_SwSts = 1;// OFF开关状态

					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AIR - mAirInfo.ACP_OFF_SwSts: " + mAirInfo.ACP_OFF_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);

					Intent close_intent = new Intent(CanBusService.SHOWAIRACRION);
					close_intent.putExtra("isShow", false);
					mContext.sendBroadcastAsUser(close_intent, UserHandle.ALL);

					break;
				case VoiceMsgDefine.AIR_CMD_INC_TEMP: // 空调温度调高
					int add_temp = mAirInfo.ACP_TempSet + 4;
					if (add_temp > 30) {
						add_temp = 30;
					}
					mAirInfo.ACP_TempSet = add_temp;
					sLog.i("AIR_CMD_INC_TEMP - mAirInfo.ACP_TempSet: " + mAirInfo.ACP_TempSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_DEC_TEMP: // 空调温度调低
					int reduce_temp = mAirInfo.ACP_TempSet - 4;
					if (reduce_temp < 2) {
						reduce_temp = 2;
					}
					mAirInfo.ACP_TempSet = reduce_temp;
					sLog.i("AIR_CMD_DEC_TEMP - mAirInfo.ACP_TempSet: " + mAirInfo.ACP_TempSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_INC_NUM_TEMP: // 空调温度调高多少
					int add_temp_num = mAirInfo.ACP_TempSet + temp * 2;
					if (add_temp_num > 30) {
						add_temp_num = 30;
					}
					mAirInfo.ACP_TempSet = add_temp_num;
					sLog.i("AIR_CMD_INC_NUM_TEMP - mAirInfo.ACP_TempSet: " + mAirInfo.ACP_TempSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_DEC_NUM_TEMP: // 空调温度调低多少
					int reduce_temp_num = mAirInfo.ACP_TempSet - temp * 2;
					if (reduce_temp_num < 2) {
						reduce_temp_num = 2;
					}
					mAirInfo.ACP_TempSet = reduce_temp_num;
					sLog.i("AIR_CMD_DEC_NUM_TEMP - mAirInfo.ACP_TempSet: " + mAirInfo.ACP_TempSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_SET_NUM_TEMP: // 空调温度设为多少
					if (temp < 18) {
						temp = 18;
					}
					if (temp > 32) {
						temp = 32;
					}
					mAirInfo.ACP_TempSet = T19CanTx.getInstance().changeTem_to_send(temp, 14, 28) + 2;
					sLog.i("AIR_CMD_DEC_NUM_TEMP - mAirInfo.ACP_TempSet: " + mAirInfo.ACP_TempSet + "  temp： " + temp);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_SCAN_FAN: // 空调扫风

					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_HEAD: // 空调吹头
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_FACE: // 空调吹面
					mAirInfo.ACP_BlowModeSet = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_FACE - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_FOOT: // 空调吹脚
					mAirInfo.ACP_BlowModeSet = 3;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_FOOT - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_FACEFOOT: // 空调吹面吹脚
					mAirInfo.ACP_BlowModeSet = 2;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_FACEFOOT - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_FOOTDEFROST: // 空调吹脚除霜
					mAirInfo.ACP_BlowModeSet = 4;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_FOOTDEFROST - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_DEFROST: // 空调除霜

					mAirInfo.ACP_AQS_SwSts = 1;// AQS开关状态

					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_DEFROST - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_DEFROST_FRONT: // 打开空调前除霜
				case VoiceMsgDefine.AIR_CMD_CLOSE_DEFROST_FRONT:// 关闭空调前除霜
					mAirInfo.ACP_AQS_SwSts = 1;// AQS开关状态，空调前除霜

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_DEFROST - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CHUI_DEFROST_BACK: // 空调后除霜
				case VoiceMsgDefine.AIR_CMD_CLOSE_DEFROST_BACK:// 关闭空调后除霜
					mAirInfo.ACP_RearDfstSwSts = 1;// 后除霜开关状态

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CHUI_DEFROST - mAirInfo.ACP_BlowModeSet: " + mAirInfo.ACP_BlowModeSet);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_OPEN_PTC: // 打开空调制热
					mAirInfo.ACP_PTC_SwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_OPEN_PTC - mAirInfo.ACP_PTC_SwSts: " + mAirInfo.ACP_PTC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_OPEN_AC: // 打开空调制冷
					mAirInfo.ACP_AC_SwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_PTC_SwSts = 0;
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_OPEN_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CLOSE_PTC: // 关闭空调制热
					mAirInfo.ACP_PTC_SwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_PTC - mAirInfo.ACP_PTC_SwSts: " + mAirInfo.ACP_PTC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				case VoiceMsgDefine.AIR_CMD_CLOSE_AC: // 关闭空调制冷
					mAirInfo.ACP_AC_SwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_PTC_SwSts = 0;
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowModeSet = 0;
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;

				case VoiceMsgDefine.AIR_CMD_CYCLE_IN: // 打开内循环
					mAirInfo.ACP_CirculationModeSwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					// airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 31;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;

				case VoiceMsgDefine.AIR_CMD_CYCLE_OUT: // 打开外循环
					mAirInfo.ACP_CirculationModeSwSts = 1;

					mAirInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;

				case VoiceMsgDefine.AIR_CMD_OPEN_AUTO: // //打开自动模式

					mAirInfo.ACP_AUTO_SwSts = 1;
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;

				case VoiceMsgDefine.AIR_CMD_CLOSE_AUTO: // 关闭自动模式

					mAirInfo.ACP_AUTO_SwSts = 1;
					mAirInfo.ACP_AC_SwSts = 0;// AC开关状态
					mAirInfo.ACP_PTC_SwSts = 0;// PTC开关状态
					mAirInfo.ACP_OFF_SwSts = 0;// OFF开关状态
					mAirInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
					mAirInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
					mAirInfo.ACP_AQS_SwSts = 0;// AQS开关状态
					mAirInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
					mAirInfo.ACP_TempSet = 0;// 温度设置
					mAirInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
					mAirInfo.ACP_IHU_Volume = 0;// DVD音量调节
					mAirInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

					sLog.i("AIR_CMD_CLOSE_AC - mAirInfo.ACP_AC_SwSts: " + mAirInfo.ACP_AC_SwSts);
					T19CanTx.getInstance().sendAirInfoData(mAirInfo);
					break;
				default:
					break;
				}

			} else if (action.equals(VoiceMsgDefine.ACTION_IFLY_VOICE_CARCONTROL)) {
				// 语音控制车身
				int cmd = intent.getIntExtra(VoiceMsgDefine.EXTRA_CARCONTROL_CMD, -1);
				int name = intent.getIntExtra(VoiceMsgDefine.EXTRA_CARCONTROL_DEV_ID, -1);
				int level = intent.getIntExtra(VoiceMsgDefine.EXTRA_CARCONTROL_LEVEL, -1);
				sLog.i("ACTION_IFLY_VOICE_CARCONTROL - cmd: " + cmd + "  name: " + name + "  level: " + level);
				switch (name) {
				case VoiceMsgDefine.CARCONTROL_NAME_TIANCHUANG:// 天窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开天窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(2, 0, 0, 0, 0, 0);
					} else if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_CLOSE) {
						// 关闭天窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(3, 0, 0, 0, 0, 0);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_QIAOQI_TIANCHUANG:
                    if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
                        // 翘起天窗
                        T19CanTx.getInstance().sendVoiceCtrlCarData(1, 0, 0, 0, 0, 0);
                    }
                    break;
				case VoiceMsgDefine.CARCONTROL_NAME_TIANCHUANG_LEFT_FRONT:// 左前门车窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						if (level == -1) {
							// 打开左前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 4, 0, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_LARGE) {
							// 打开大一点左前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 3, 0, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_SMALL) {
							// 打开小一点左前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 1, 0, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_MIDDLE) {
							// 打开一半左前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 2, 0, 0, 0);
						}
					} else {
						// 关闭左前门车窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 5, 0, 0, 0);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_TIANCHUANG_RIGHT_FRONT:// 右前门车窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						if (level == -1) {
							// 打开右前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 4, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_LARGE) {
							// 打开大一点右前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 3, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_SMALL) {
							// 打开小一点右前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 1, 0, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_MIDDLE) {
							// 打开一半右前门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 2, 0, 0);
						}
					} else {
						// 关闭天窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 5, 0, 0);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_TIANCHUANG_LEFT_BACK:// 左后门车窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						if (level == -1) {
							// 打开左后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 4, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_LARGE) {
							// 打开大一点左后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 3, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_SMALL) {
							// 打开小一点左后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 1, 0);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_MIDDLE) {
							// 打开一半左后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 2, 0);
						}
					} else {
						// 关闭左后门车窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 5, 0);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_TIANCHUANG_RIGHT_BACK:// 右后门车窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						if (level == -1) {
							// 打开右后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 0, 4);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_LARGE) {
							// 打开大一点右后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 0, 3);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_SMALL) {
							// 打开小一点右后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 0, 1);
						} else if (level == VoiceMsgDefine.CARCONTROL_LEVEL_MIDDLE) {
							// 打开一半右后门车窗
							T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 0, 2);
						}
					} else {
						// 关闭右后门车窗
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 0, 0, 0, 0, 5);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_HOUBEIXIANG:// 后备箱
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开后备箱
					} else {
						// 关闭后备箱
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_JINGUANGDENG:// 近光灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开近光灯
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 1, 0, 0, 0, 0);
					} else {
						// 关闭近光灯
						T19CanTx.getInstance().sendVoiceCtrlCarData(0, 2, 0, 0, 0, 0);
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_YUANGUANGDENG:// 远光灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开远光灯
					} else {
						// 关闭远光灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_WUDENG:// 雾灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开雾灯
					} else {
						// 关闭雾灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_QIANWUDENG:// 前雾灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开前雾灯
					} else {
						// 关闭前雾灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_HOUWUDENG:// 后雾灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开后雾灯
					} else {
						// 关闭后雾灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_SHILANGDENG:// 示廓灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开示廓灯
					} else {
						// 关闭示廓灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_JINGSHIDENG:// 警示灯
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开警示灯
					} else {
						// 关闭警示灯
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_CHECHUANG:// 车窗
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开车窗
					} else {
						// 关闭车窗
					}
					break;
				case VoiceMsgDefine.CARCONTROL_NAME_YUGUAQI:// 雨刮器
					if (cmd == VoiceMsgDefine.CARCONTROL_OPERATION_OPEN) {
						// 打开雨刮器
					} else {
						// 关闭雨刮器
					}
					break;
				default:
				}

            } else if (action.equals(VoiceMsgDefine.ACTION_IFLY_VOICE_SCREEN)) {
                int cmd = intent.getIntExtra(VoiceMsgDefine.EXTRA_SCREEN_CMD, 0);
                switch (cmd) {
                    case VoiceMsgDefine.SCREEN_CMD_INC://增加亮度
                        Utils.incBrightness(context);
                        break;
                    case VoiceMsgDefine.SCREEN_CMD_DEC://减小亮度
                        Utils.decBrightness(context);
                        break;
                    case VoiceMsgDefine.SCREEN_CMD_MAX://最大亮度
                        break;
                    case VoiceMsgDefine.SCREEN_CMD_MIN://最小亮度
                        break;
                    default:
                        break;
                }
			} else if (action.equals(VoiceMsgDefine.ACTION_IFLY_VOICE_VEHICLEINFO)) {
				// 语音控制车身
				int cmd = intent.getIntExtra(VoiceMsgDefine.EXTRA_VEHICLEINFO_CMD, -1);// 暂时没用，可以不管
				int name = intent.getIntExtra(VoiceMsgDefine.EXTRA_VEHICLEINFO_DEV_ID, -1);
				sLog.i("ACTION_IFLY_VOICE_VEHICLEINFO - cmd: " + cmd + "  name: " + name);
				switch (name) {
				case VoiceMsgDefine.VEHICLEINFO_NAME_TRIP:// 油量
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_TPMS:// 胎压
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_ENGINE:// 发动机
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_AIRPORT:// 空调
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_WATER:// 水温
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_BRAKE:// 刹车
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_ZHIDONG:// 制动
					break;
				case VoiceMsgDefine.VEHICLEINFO_NAME_STATUS:// 整体车况
					break;
				default:
					break;
				}

			} else if (action.equals(MediaPlayerContants.ACTION_IFLY_VOICE_MUSIC)) {
			    int music_cmd = intent.getIntExtra(MediaPlayerContants.EXTRA_MUSIC_CMD, 0);
			    switch (music_cmd) {
			    case MediaPlayerContants.MUSIC_CMD_OPEN_MUSICLIST:
			    	PageMedia.setView(ViewType.ViewUsbMusicFile);
					launcherUtils.startMedia();
			    	break;
				case MediaPlayerContants.MUSIC_CMD_PLAY_USB1:
					if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK1_PATH)) {
						switchSource(Constants.KEY_SRC_MODE_USB1);
					}
					break;
				case MediaPlayerContants.MUSIC_CMD_PLAY_USB2:
					if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK2_PATH)) {
						switchSource(Constants.KEY_SRC_MODE_USB2);
					}
					break;
				case MediaPlayerContants.MUSIC_CMD_PLAY_LOCAL:
					if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.EXTERNAL_PATH)) {
						switchSource(Constants.KEY_SRC_MODE_EXTERNAL);
					}
					break;
				case MediaPlayerContants.MUSIC_CMD_PLAY_BTMUSIC:
					switchSource(Constants.KEY_SRC_MODE_BT_MUSIC);
					break;
				default:
					break;
				}
			} else if (action.equals(RadioConstants.ACTION_IFLY_VOICE_RADIO)) {
				int cmd = intent.getIntExtra(RadioConstants.EXTRA_RADIO_CMD, 0);
				String freq_str = intent.getStringExtra(RadioConstants.EXTRA_RADIO_FREQ);
				float freq_value;
				Log.i(TAG, "radio---receive ifly voice msg, cmd=" + cmd + ",freq_str=" + freq_str);

				switch (cmd) {
				case RadioConstants.RADIO_CMD_PLAY_FM:
					freq_value = Float.parseFloat(freq_str);
					freq_value *= 100.0f;
					RadioSearchFragment.sendFreq(mContext, (int) freq_value, RadioConstants.BAND_ID_FM);
					launcherUtils.startRadio();
					break;
				case RadioConstants.RADIO_CMD_PLAY_AM:
					freq_value = Float.parseFloat(freq_str);
					RadioSearchFragment.sendFreq(mContext, (int) freq_value, RadioConstants.BAND_ID_AM);
					launcherUtils.startRadio();
					break;
				case RadioConstants.RADIO_CMD_GOTO_FM:
					RadioSearchFragment.switchBand(mContext, RadioConstants.BAND_ID_FM);
					launcherUtils.startRadio();
					break;
				case RadioConstants.RADIO_CMD_GOTO_AM:
					RadioSearchFragment.switchBand(mContext, RadioConstants.BAND_ID_AM);
					launcherUtils.startRadio();
					break;
				}
			} else if(Constants.ACTION_CAROCEAN_OPEN_UI.equals(action)) {
				int ui_type = intent.getIntExtra(Constants.UI_TYPE, -1); 
				if(ui_type != -1) {
					switch (ui_type) {
					
					case Constants.UI_TYPE_SOUND:
						launcherUtils.startSound();
						break;
						
					case Constants.UI_TYPE_BALANCE:
						launcherUtils.startBalance();
						break;
						
					default:
						break;
					}
				}

			} else if (Constants.ACTION_OPEN_WIFI_SETTING.equals(action)) {
				launcherUtils.startWifi();

			} else if (action.equals(Constants.AUTOMATION_CONTROL_BROADCAST_SEND)) {
				int eventType = intent.getIntExtra("eventType", 0);
				if (eventType == 1) {
					Intent intentSend = new Intent(Constants.AUTOMATION_CONTROL_BROADCAST_RECV);
					intentSend.putExtra("signal", mIsSignal);
					sendBroadcast(intentSend);
				} else if(eventType == 2) {
					tzUtils.recoverySystem(mContext, false);
				}
			} else if (action.equals(McuExternalConstant.MCU_ACTION_VOLUME_MUTE)) {
				boolean bMute = intent.getBooleanExtra("mute", false);
				Log.i(TAG, "MCU_ACTION_VOLUME_MUTE bMute:" + bMute);
				if (bMute) {
					if (!mIsMusicActive && requestFocus()) {
						mIsMusicActive = true;
					}
				} else {
					if (mIsMusicActive && abandonFocus()) {
						mIsMusicActive = false;
					}
				}
			} else if (action.equals(Constants.ACTION_REQUESET_APP_VERSION)) {
				String appVersion = verUtils.getAppBuildString();
				Intent versionIntent = new Intent(Constants.ACTION_RETURN_APP_VERSION);
				versionIntent.putExtra(Constants.APP_VERSION_EXTRA, appVersion);
				sendBroadcast(versionIntent);
			} 
		}

		private int modeSwitchFilter(int mode) {
			int switchMode = 0;
			switch (mode) {
			case Constants.KEY_SRC_MODE_FM:
				switchMode = Constants.KEY_SRC_MODE_AM;
				break;
			case Constants.KEY_SRC_MODE_AM:
				if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK1_PATH)) {
					switchMode = Constants.KEY_SRC_MODE_USB1;
				} else if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK2_PATH)) {
					switchMode = Constants.KEY_SRC_MODE_USB2;
				} else {
					switchMode = Constants.KEY_SRC_MODE_BT_MUSIC;
				}
				break;
			case Constants.KEY_SRC_MODE_USB1:
				if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK2_PATH)) {
					switchMode = Constants.KEY_SRC_MODE_USB2;
				} else {
					switchMode = Constants.KEY_SRC_MODE_BT_MUSIC;
				}
				break;
			case Constants.KEY_SRC_MODE_USB2:
				switchMode = Constants.KEY_SRC_MODE_BT_MUSIC;
				break;
			case Constants.KEY_SRC_MODE_BT_MUSIC:
				if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.getExternalPath())) {
					switchMode = Constants.KEY_SRC_MODE_EXTERNAL;
				}
				break;
			case Constants.KEY_SRC_MODE_EXTERNAL:
				switchMode = Constants.KEY_SRC_MODE_FM;
				break;
			default:
				// goto radio
				switchMode = Constants.KEY_SRC_MODE_FM;
				break;
			}
			return switchMode;
		}

		private void switchSource(int mode) {
			switch (mode) {
			case Constants.KEY_SRC_MODE_FM:
				// 切换到FM
				RadioSearchFragment.switchBand(mContext, RadioConstants.BAND_ID_FM);
				launcherUtils.startRadio();
				break;
			case Constants.KEY_SRC_MODE_AM:
				// 切换到AM
				RadioSearchFragment.switchBand(mContext, RadioConstants.BAND_ID_AM);
				launcherUtils.startRadio();
				break;
			case Constants.KEY_SRC_MODE_USB1:
				startMedia(MediaScanConstans.UDISK1_PATH);
				break;
			case Constants.KEY_SRC_MODE_USB2:
				startMedia(MediaScanConstans.UDISK2_PATH);
				break;
			case Constants.KEY_SRC_MODE_BT_MUSIC:
				PageMedia.setView(ViewType.ViewBtMusic);
				launcherUtils.startMedia();
				break;
			case Constants.KEY_SRC_MODE_EXTERNAL:
				startMedia(MediaScanConstans.getExternalPath());
				break;
			default:
				break;
			}
		}
		
		private void startMedia(String strPath) {
			try {
				MediaActivityProxy.getService().requestAttachStorage(strPath, MediaType.MEDIA_AUDIO);
				if (PageMedia.getView() != ViewType.ViewUsbMusic
						&& PageMedia.getView() != ViewType.ViewUsbMusicFile) {
					PageMedia.setView(ViewType.ViewUsbMusicFile);
				}
				launcherUtils.startMedia();// 音乐
			} catch (Exception e) {

			}
		}
	};

	public boolean isBackCarorBTPhone() {
		boolean flag = false;
		if ("true".equals(SystemProperties.get(YeconConstants.PROPERTY_KEY_STARTBACKCAR))
				|| "true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
			flag = true;

		} else {
			flag = false;
		}
		return flag;
	}

	public static BootService getInstance() {
		return gInst;
	}

	@Override
	public void onCreate() {
		sLog.i(TAG, "BootService - onCreate - start");
		mContext = this;
		gInst = this;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mEnv = new EnvironmentATC(this);
		initBackCar();
		initBackCarView();
		initMcu();
		startCanBusService();
		registerBroadcastReceiver();
		Utils.initAppAll();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterBroadcastReceiver();
		deinitMcu();
		deinitBackCar();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 播放音乐，断B+，拔USB，上B+，插入USB，有时会读不到盘。
		// 当以上操作读不到盘符时，直接进入收音模式
//		int source = SourceManager.getSourceInit(this);
//		boolean flag = false;
//		if (source == SRC_VOLUME_MEDIA) {
//
//			for (int i = 1; i <= 5; i++) {
//				String storageState = Environment.getStorageState("/mnt/udisk" + i);
//				if (storageState.equals(Environment.MEDIA_MOUNTED)) {
//					flag = true;
//					Log.i(TAG, "onStartCommand::Environment /mnt/udisk" + i + " = " + flag);
//					break;
//				}
//			}
//
//			if (!flag) {
//				SourceManager.setSource(mContext, Constants.KEY_SRC_MODE_FM);
//			}
//		}

//		Log.i(TAG, "onStartCommand::Source = " + source);

		// startSourceApp();
        startNavi();

		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void setBackCarActivity(Activity activity) {
		sLog.i(TAG, "setBackCarActivity - mIsReversing: " + mIsReversing);

		if (mBackCarActivity != null && mBackCarActivity != activity) {
			mBackCarActivity.finish();
		}

		mBackCarActivity = activity;

		if (!mIsReversing) {
			mHandler.removeMessages(MSG_BACKCAR_STOPACTIVITY);
			mHandler.sendEmptyMessage(MSG_BACKCAR_STOPACTIVITY);
		}
	}

	private void registerBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		filter.addAction(ACTION_QB_POWEROFF);
		filter.addAction(MCU_ACTION_ACC_OFF);
		filter.addAction(ACTION_IFLY_VOICE_APP);
		filter.addAction(ACTION_KUWO_FUNC);
		filter.addAction(ACTION_SWITCH_MODE);
		filter.addAction(VoiceMsgDefine.ACTION_IFLY_VOICE_AIR);
		filter.addAction(VoiceMsgDefine.ACTION_IFLY_VOICE_CARCONTROL);
		filter.addAction(VoiceMsgDefine.ACTION_IFLY_VOICE_VEHICLEINFO);
		filter.addAction(MediaPlayerContants.ACTION_IFLY_VOICE_MUSIC);
		filter.addAction(RadioConstants.ACTION_IFLY_VOICE_RADIO);
        filter.addAction(VoiceMsgDefine.ACTION_IFLY_VOICE_SCREEN);
		filter.addAction(Constants.ACTION_CAROCEAN_OPEN_UI);
		filter.addAction(Constants.ACTION_OPEN_WIFI_SETTING);
		filter.addAction(Constants.AUTOMATION_CONTROL_BROADCAST_SEND);
		filter.addAction(McuExternalConstant.MCU_ACTION_VOLUME_MUTE);
		filter.addAction(Constants.ACTION_REQUESET_APP_VERSION);
		registerReceiver(mBroadcastReceiver, filter);
	}

	private void unregisterBroadcastReceiver() {
		unregisterReceiver(mBroadcastReceiver);
	}

	private void initMcu() {
		mMcuManager = (McuManager) getSystemService(Context.MCU_SERVICE);
		try {
			mMcuManager.RPC_RequestMcuInfoChangedListener(mMcuListener);

			mMcuManager.RPC_GetStatus();
			
			// 开机启动后通知MCU源状态，防止再收音机源关ACC，开ACC，MCU的源在收音机上下曲按键没有发过来
			mMcuManager.RPC_SetSource(MCU_SOURCE_OFF, 0x00);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void deinitMcu() {
		try {
			mMcuManager.RPC_RemoveMcuInfoChangedListener(mMcuListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void initBackCar() {
		mConfigBackCarMuted = SystemProperties.getBoolean(PROPERTY_BACKCAR_MUTE_EANBLE, true);

		int port = YeconMetazone.GetBackCarPort();
		if (port > 0 && port <= 5) {
			mBackCarPort = port;
		}

		sLog.i(TAG, "initBackCar - mConfigBackCarMuted:" + mConfigBackCarMuted + " - mBackCarPort: " + mBackCarPort);

		if (null == gCBM) {
			gCBM = new CBManager();
		}

		if (mBackCarNeedInit) {
			mBackCarNeedInit = false;

			BackCar.init();
		}

		if (null != gCBM) {
			gCBM.systemReady();
			gCBM = null;
		}

		mBackCar = new BackCar();
		mBackCar.setOnSignalListener(mBackCarListenerSignal);
	}

	private void deinitBackCar() {
		BackCar.Deinit();
	}

	private void initBackCarView() {
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBackCarMainView = inflater.inflate(R.layout.activity_backcar, null);
		mBackCarView = (BackCarView) mBackCarMainView.findViewById(R.id.backcar_bcview);
		mTraceView = (ImageView) mBackCarMainView.findViewById(R.id.trace_iv);
		mBackCarView.getHolder().setFormat(ImageFormat.YV12);
		mBackCarView.getHolder().setFixedSize(PAL_WIDTH, PAL_HEIGHT);

		mTVBackCarNoSignal = (TextView) mBackCarMainView.findViewById(R.id.backcar_tv_no_signal);
		mTVBackCarNoSignal.setVisibility(View.INVISIBLE);
		mTraceView.setVisibility(View.INVISIBLE);
	}

	void sendBackcarStartMsg() {
	    Intent intent = new Intent(ACTION_BACKCAR_START);
	    intent.putExtra("audio_break", false);
	    sendBroadcast(intent);
    }

    void sendBackcarStopMsg() {
        Intent intent = new Intent(ACTION_BACKCAR_STOP);
        intent.putExtra("audio_continue", false);
        sendBroadcast(intent);
    }

	private void backCarStart() {
		if (!mBackCarStarted) {
			mBackCarStarted = true;

			Utils.showSystemUI(mContext, false);

			mHandler.removeMessages(MSG_BACKCAR_STARTACTIVITY);

			mHandler.removeMessages(MSG_BACKCAR_SIGNAL_LOST);
			mHandler.sendEmptyMessageDelayed(MSG_BACKCAR_SIGNAL_LOST, DELAY_DISPLAY_NO_SIGNAL);

			SystemProperties.set(PROPERTY_KEY_STARTBACKCAR, "true");

			BackCar.config(mBackCarPort);
			BackCar.start();

			mHandler.sendEmptyMessage(MSG_BACKCAR_START);
            sendBackcarStartMsg();

			// 背光处理
			mHandler.removeMessages(MSG_ID_BKL_ENABLE);
			mHandler.removeMessages(MSG_ID_BKL_ENABLE);
			mHandler.sendEmptyMessageDelayed(MSG_ID_BKL_ENABLE, BKL_ENABLE_TIMEOUT);

		}
	}

	private void backCarStop() {
		if (mBackCarStarted) {
			mBackCarStarted = false;

			Utils.showSystemUI(mContext, true);

			mHandler.removeMessages(MSG_BACKCAR_STARTACTIVITY);
			mHandler.removeMessages(MSG_BACKCAR_SIGNAL_LOST);
			mHandler.removeMessages(MSG_ON_ANGLE_CHANGE);

			BackCar.stop();

			mHandler.sendEmptyMessage(MSG_BACKCAR_STOP);

			SystemProperties.set(PROPERTY_KEY_STARTBACKCAR, "false");

            sendBackcarStopMsg();

			mHandler.removeMessages(MSG_ID_BKL_ENABLE);

			mHandler.sendEmptyMessage(MSG_BKL_POWER_DISENABLE);

			mHandler.sendEmptyMessage(MSG_BKL_BLACKOUT_DISENABLE);

		}
	}

	private void showBackCar(int backcarStatus) {
		sLog.e(TAG, "showBackCar - backcarStatus: " + backcarStatus + " - mBackCarStarted: " + mBackCarStarted);

		if (backcarStatus == BACKCAR_STATUS_START) {
			//倒车时首先通知关闭空调
			Intent close_intent = new Intent(CanBusService.SHOWAIRACRION);
			close_intent.putExtra("isShow", false);
			mContext.sendBroadcastAsUser(close_intent, UserHandle.ALL);
			backCarStart();
		} else if (backcarStatus == BACKCAR_STATUS_STOP) {
			backCarStop();
		}
	}

	private void startNavi() {
        if (SettingConstants.autonavi)
            launcherUtils.startNavi();
    }

	private void startSourceApp() {
		int arm2BackCarStatus = YeconMetazone.GetBackCarStatus();
		sLog.i(TAG, "BootService - startSourceApp - arm2BackCarStatus: " + arm2BackCarStatus);
		if (arm2BackCarStatus == ARM2_BACKCAR_STATUS_IN_ARM2) {
			mHandler.sendEmptyMessageDelayed(MSG_ID_CHECK_ARM2_BACKCAR_STATUS, DELAY_ARM2_BACKCAR_STATUS_CHECK);
		} else if (arm2BackCarStatus == ARM2_BACKCAR_STATUS_EXIT_ARM2) {
			// do nothing
		}

		if (!Constants.isDebug) {
			/*
			 * int source = SourceManager.getSource(mContext); sLog.i(TAG,
			 * "BootService - startSourceApp - source: " +
			 * SourceManager.SOURCE_LABEL_NAME_MAP.get(source)); if (source ==
			 * SRC_VOLUME_MUSIC || source == SRC_VOLUME_VIDEO || source ==
			 * SRC_VOLUME_MEDIA) { //mMediaCheckCount = 0;
			 * //checkMediaSource(0); Utils.RunApp(MEDIA_PACKAGE_NAME,
			 * MEDIA_START_ACTIVITY); } else {
			 * SourceManager.startSourceApp(mContext); }
			 */
			SourceManager.startSourceApp(mContext);
		}
	}

	private void checkMediaSource(long delay) {
		mHandler.removeMessages(MSG_ID_CHECK_MEDIA_SOURCE);
		mHandler.sendEmptyMessageDelayed(MSG_ID_CHECK_MEDIA_SOURCE, delay);
	}

	private void startCanBusService() {

		Intent intent = new Intent();
		ComponentName componentName = new ComponentName("com.carocean", "com.carocean.t19can.CanBusService");
		intent.setComponent(componentName);
		startService(intent);
	}
	
	public boolean requestFocus() {
		if (mAudioManager != null && mAudioFocusListener != null) {
			return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusListener,
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		}
		return false;
	}

	public boolean abandonFocus() {
		if (mAudioManager != null && mAudioFocusListener != null) {
			return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(mAudioFocusListener);
		}
		return false;
	}

	private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			Log.i(TAG, "onAudioFocusChange focusChange:" + focusChange);
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS:
				int iMusicVolume = SystemProperties.getInt(McuExternalConstant.PROPERTY_KEY_STREAM_MUSIC_VOLUME, 0);
				Log.i(TAG, "AUDIOFOCUS_LOSS iMusicVolume:" + iMusicVolume);
				if (iMusicVolume == 0) {
					Utils.TransKey(KeyEvent.KEYCODE_VOLUME_UP);
				} else {
					Utils.TransKey(KeyEvent.KEYCODE_VOLUME_MUTE);
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				break;
			default:
				break;
			}
		}
	};

	// 发送时间给mcu
	public static void sendTimeMcu(McuManager mMcuManager) {
		int year, month, day, hour, minute, second;
		Calendar c = Calendar.getInstance();

		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		second = c.get(Calendar.SECOND);

		byte[] param = new byte[9];
		param[0] = (byte) 0x02;
		param[1] = (byte) ((((year / 1000) & 0x0F) << 4) + (year / 100 % 10));
		param[2] = (byte) ((((year % 100 / 10) & 0x0F) << 4) + (year % 10));
		param[3] = (byte) ((((month / 10) & 0x0F) << 4) + (month % 10));
		param[4] = (byte) ((((day / 10) & 0x0F) << 4) + (day % 10));
		param[5] = (byte) ((((hour / 10) & 0x0F) << 4) + (hour % 10));
		param[6] = (byte) ((((minute / 10) & 0x0F) << 4) + (minute % 10));
		param[7] = (byte) ((((second / 10) & 0x0F) << 4) + (second % 10));

		if (timeUtils.getAutoState(ApplicationManage.getContext(), Settings.Global.AUTO_TIME)) {
			param[8] = (byte) (0 | ((1 & 0x01) << 1) | 0 | ((1 & 0x01) << 3));
		} else {
			param[8] = (byte) (0 | ((1 & 0x01) << 1) | 0 | ((0 & 0x01) << 3));
		}

		Log.i("setDate", "sendToMcuTimeByGps - year: " + year + " - month: " + month + " - day: " + day + " - hour: "
				+ hour + " - minute: " + minute + " - second: " + second + " -param[8]: " + param[8]);

		try {
			mMcuManager.RPC_SetSysParams(param, 9);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void sendPowerKeyCMD(boolean poweroff) {
		byte[] param = new byte[4];
		param[0] = (byte) (poweroff ? 1 : 0);
		param[1] = (byte) 0x00;
		param[2] = (byte) 0x00;
		param[3] = (byte) 0x00;

		try {
			if (mMcuManager != null) {
				mMcuManager.RPC_KeyCommand(K_POWER, param);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void sendBlackoutKeyCMD(int backlightLevel) {
		byte[] param = new byte[4];
		param[0] = (byte) backlightLevel;
		param[1] = (byte) 0x00;
		param[2] = (byte) 0x00;
		param[3] = (byte) 0x00;

		try {
			if (mMcuManager != null) {
				mMcuManager.RPC_KeyCommand(T_BLACKOUT, param);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setBacklight(int enable, int touch_wakeup_en, int power_key_pressed, int backlightLevel) {
		BacklightControl.SetBklEnable(enable, touch_wakeup_en, power_key_pressed);

		sendBlackoutKeyCMD(backlightLevel);
	}

}
