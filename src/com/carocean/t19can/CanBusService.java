package com.carocean.t19can;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.t19can.T19CanRx.AirInfo;
import com.carocean.t19can.T19CanRx.CarHelpAirInfo;
import com.carocean.t19can.T19CanRx.CarStatusInfo;
import com.carocean.t19can.T19CanRx.CarbodyInfo;
import com.carocean.t19can.T19CanRx.DashboardConfigInfo1;
import com.carocean.t19can.T19CanRx.DashboardInfo;
import com.carocean.t19can.T19CanRx.EnergyInfo;
import com.carocean.t19can.T19CanRx.LowPowerInfo;
import com.carocean.t19can.T19CanRx.PedestrianInfo;
import com.carocean.t19can.T19CanRx.PowerInfo;
import com.carocean.t19can.T19CanRx.TipsInfo;
import com.carocean.t19can.T19CanRx.VcuInfo;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;
import com.carocean.vmedia.t19can.T19Air;
import com.carocean.vmedia.t19can.T19CanPopup;
import com.carocean.vmedia.t19can.T19NaviPopup;
import android.annotation.SuppressLint;
import android.app.Service;
import android.constant.YeconConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mcu.McuExternalConstant;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;

import static android.constant.YeconConstants.PROPERTY_KEY_STARTBACKCAR;

// 此服务由BootService中的onCreate方法启动

public class CanBusService extends Service {
	private final String TAG = "CanBusService";
	public final static String SHOWAIRACRION = "com.carocean.showAir.action";
	public final static String SETSPEEDCOMPENSATIONACRION = "com.carocean.set_speed_compensation.action";// 车速发送
	public final static String send_external_temp_action = "com.carocean.send_external_temp.action";// 发送室外温度

	private static CanBusService gInst = null;
	private Context mContext;
	private T19Air mT19Air;
	private T19CanPopup mT19CanPopup;
	private T19NaviPopup mT19NaviPopup;
	private AirInfo mAirInfo;
	private TipsInfo mTipsInfo;
	private VcuInfo mVcuInfo;
	private CarHelpAirInfo mCarHelpAirInfo;

	private Callback mCallback;

	private AudioManager mAudioManager;

	public static CanBusService getInstance() {
		return (gInst);
	}

	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		mContext = this;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SHOWAIRACRION);
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);

	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		gInst = this;
		initCan();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mT19Air = new T19Air(inflater, mContext, mHandler);
		mT19CanPopup = new T19CanPopup(inflater, mContext, mHandler);
		mT19NaviPopup = new T19NaviPopup(inflater, mContext, mHandler);
		mAirInfo = new AirInfo();
		mTipsInfo = new TipsInfo();

		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		uinitCan();
	}

	private void initCan() {
		Log.i(TAG, "initCan");
		T19CanRx.getInstance().init(this);
		T19CanTx.getInstance().init(this);
	}

	private void uinitCan() {
		Log.i(TAG, "uinitCan");
		T19CanRx.getInstance().onDestroy();
	}

	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return new CanBusBinder();
	}

	public void sendToHandler(int what, int arg1, int arg2, Object object, long time) {
		Message msg = mHandler.obtainMessage();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = object;
		mHandler.sendMessageDelayed(msg, time);
	}

	public class CanBusBinder extends Binder {
		public void sendData() {
			if (mCallback != null) {
				mCallback.onDataChange(T19CanRx.getInstance().mCarStatusInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mEnergyInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mLowPowerInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mPedestrianInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mDashboardInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mCarbodyInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mCarHelpAirInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mPowerInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mAirInfo);
				mCallback.onDataChange(T19CanRx.getInstance().mDashboardConfigInfo1);
				mCallback.onDataChange(T19CanRx.getInstance().mVcuInfo);
			}
		}

		public CanBusService getCanBusService() {
			return CanBusService.this;
		}
	}

	public void setCallback(Callback callback) {
		this.mCallback = callback;
	}

	public Callback getCallback() {
		return mCallback;
	}

	public static interface Callback {

		void onDataChange(CarStatusInfo mCarStatusInfo);

		void onDataChange(EnergyInfo mLowPowerInfo);

		void onDataChange(LowPowerInfo mEnergyInfo);

		void onDataChange(PedestrianInfo mPedestrianInfo);

		void onDataChange(DashboardInfo mDashboardInfo);

		void onDataChange(CarbodyInfo mCarbodyInfo);

		void onDataChange(CarHelpAirInfo mCarHelpAirInfo);

		void onDataChange(PowerInfo mPowerInfo);

		void onDataChange(AirInfo mAirInfo);

		void onDataChange(DashboardConfigInfo1 mDashboardConfigInfo1);

		void onDataChange(VcuInfo mVcuInfo);

	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			Log.i(TAG, "ACTION_SYSTEMUI_AIR = " + intent.getAction());
			if (intent.getAction().equals(SHOWAIRACRION)) {
				boolean isShow = intent.getBooleanExtra("isShow", false);
				if (null != mAirInfo) {
					if (isShow) {
						showAir(mAirInfo, isShow, true);
					} else {
						if (mT19Air != null) {
							mT19Air.Hide();
						}
					}

				}
			}
		}
	};

	public void showAir(AirInfo mAirInfo, boolean isShow, boolean isHide) {

		if (SystemProperties.getBoolean(YeconConstants.PROPERTY_KEY_STARTBACKCAR, false)
				|| "true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
			if (null != mT19Air) {
				if (isShow) {
					mT19Air.show(mAirInfo, false);
				} else {
					if (isHide) {
						mT19Air.Hide();
					} else {
						mT19Air.show(mAirInfo, false);
					}
				}
			}
		} else {
			if (null != mT19Air) {
				if (isShow) {
					mT19Air.show(mAirInfo, true);
				} else {
					if (isHide) {
						mT19Air.Hide();
					} else {
						mT19Air.show(mAirInfo, false);
					}

				}
			}
		}
	}

	public void showT19CanPopup(TipsInfo mTipsInfo) {
		if (!SystemProperties.getBoolean(YeconConstants.PROPERTY_KEY_STARTBACKCAR, false)) {
			if (null != mTipsInfo) {
				mT19CanPopup.show(mTipsInfo, true);
			}
		}
	}

	private List<onSpeedChangeListener> mSpeedListener = new ArrayList<onSpeedChangeListener>();
	private int miCurSpeed = 0;
	
	public void registerSpeedHandler(onSpeedChangeListener listener) {
		if (!mSpeedListener.contains(listener)) {
			mSpeedListener.add(listener);
		}
	}
	
	public void unRegisgerSpeedHandler(onSpeedChangeListener listener) {
		if (mSpeedListener.contains(listener)) {
			mSpeedListener.remove(listener);
		}
	}
	
	public interface onSpeedChangeListener {
		void onSpeedChange(int speed);
	}
	
	public int getCurSpeed() {
		return miCurSpeed;
	}
	
	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == T19CanRx.MSG_CAN_DATA) {
				switch (msg.arg1) {

				case T19CanRx.carstatusInfoType:
					if (mCallback != null) {
						CarStatusInfo mCarStatusInfo = (CarStatusInfo) msg.obj;
						mCallback.onDataChange(mCarStatusInfo);

						int speed = miCurSpeed = mCarStatusInfo.VCU_VehSpd;
						Log.i(TAG, " carstatusInfoType miCurSpeed:" + miCurSpeed);
						for (onSpeedChangeListener callback : mSpeedListener) {
							if(null != callback) {
								try {
									callback.onSpeedChange(miCurSpeed);
								}catch(Exception e) {
									
								}
							}
						}
						int speed_level = DataShared.getInstance(mContext)
								.getInt(SettingConstants.key_speed_compensation_flag, 0);
						int cur_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						boolean bMuted = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);

						if (bMuted || cur_volume == 31) {
							return;
						}

						int iSpeedIndex = 0;
						switch (speed_level) {
						case 1:
							if (speed >= Utils.LowVelocity[Utils.LowVelocity.length - 1]) {
								iSpeedIndex = Utils.LowVelocity.length;
							} else if (speed >= Utils.LowVelocity[0]) {
								for (int i = 0; i < Utils.LowVelocity.length - 1; i++) {
									if (speed >= Utils.LowVelocity[i] && speed < Utils.LowVelocity[i + 1]) {
										iSpeedIndex = i + 1;
										break;
									}
								}
							}
							break;

						case 2:
							if (speed >= Utils.MiddleVelocity[Utils.MiddleVelocity.length - 1]) {
								iSpeedIndex = Utils.MiddleVelocity.length;
							} else if (speed >= Utils.MiddleVelocity[0]) {
								for (int i = 0; i < Utils.MiddleVelocity.length - 1; i++) {
									if (speed >= Utils.MiddleVelocity[i] && speed < Utils.MiddleVelocity[i + 1]) {
										iSpeedIndex = i + 1;
										break;
									}
								}
							}
							break;

						case 3:
							if (speed >= Utils.HigtVelocity[Utils.HigtVelocity.length - 1]) {
								iSpeedIndex = Utils.HigtVelocity.length;
							} else if (speed >= Utils.HigtVelocity[0]) {
								for (int i = 0; i < Utils.HigtVelocity.length - 1; i++) {
									if (speed >= Utils.HigtVelocity[i] && speed < Utils.HigtVelocity[i + 1]) {
										iSpeedIndex = i + 1;
										break;
									}
								}
							}
							break;

						default:
							break;
						}
						int iVolumeGain = iSpeedIndex;
						String oldGain = SystemProperties.get("sys.volume_gain", "add0");
						String newGain = "add" + iVolumeGain;
                        boolean backCarStartFlag = SystemProperties.getBoolean(PROPERTY_KEY_STARTBACKCAR, false);
						//xuhh:不同才去设，不然会非常频繁的设置音量
						Log.i(TAG, "speed---oldGain="+oldGain+",newGain="+newGain+",speedIndex="+iSpeedIndex+",backCarStartFlag="+backCarStartFlag);
						if (!oldGain.equals(newGain) && !backCarStartFlag) {
                            SystemProperties.set("sys.volume_gain", "add" + iVolumeGain);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cur_volume, AudioManager.ADJUST_SAME);
                            Log.i(TAG, "speed---inc volume gain.");
                        }
					}
					break;

				case T19CanRx.energyInfoType:
					if (mCallback != null) {
						mCallback.onDataChange((EnergyInfo) msg.obj);
					}
					break;

				case T19CanRx.lowPowerInfoType:
					LowPowerInfo mLowPowerInfo = (LowPowerInfo) msg.obj;
					if (null != mLowPowerInfo) {
						if (mLowPowerInfo.ICM_LowSOC_LampSts == 1) {
							// 如果充电桩智能开关开启且15分钟之内没有弹出过提示
							if (DataShared.getInstance(mContext)
									.getBoolean(SettingConstants.key_intelligent_charging_pile, false)) {
								if (!DataShared.getInstance(mContext).getBoolean(SettingConstants.key_show_navi_flag,
										false)) {
									if (mT19NaviPopup != null) {
										mT19NaviPopup.show(true);
									}
								}

							}
						} else {
							// 没有电量不足时，恢复可以弹窗
							DataShared.getInstance(mContext).putBoolean(SettingConstants.key_show_navi_flag, false);
						}
					}
					break;

				case T19CanRx.pedestrianInfoType:
					PedestrianInfo mPedestrianInfo = (PedestrianInfo) msg.obj;

					if (mPedestrianInfo != null) {
						Intent intent = new Intent();
						if (mPedestrianInfo.AVAS_SwSts == 1) {
							intent.putExtra("isShow", true);
						} else if (mPedestrianInfo.AVAS_SwSts == 0) {
							intent.putExtra("isShow", false);
						}
						intent.setAction("com.carocean.refresh.pedestrianReminder");
						mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
					}

					if (mCallback != null) {
						mCallback.onDataChange(mPedestrianInfo);
					}
					break;

				case T19CanRx.dashboardInfoType:
					if (mCallback != null) {
						mCallback.onDataChange((DashboardInfo) msg.obj);
					}
					break;

				case T19CanRx.carbodyInfoType:
					if (mCallback != null) {
						mCallback.onDataChange((CarbodyInfo) msg.obj);
					}
					break;

				case T19CanRx.carHelpAirInfoType:
					mCarHelpAirInfo = (CarHelpAirInfo) msg.obj;
					if (mCarHelpAirInfo != null) {
						if (mCallback != null) {
							mCallback.onDataChange(mCarHelpAirInfo);
						}

						if (mCarHelpAirInfo.ACCM_AutoCleanActiveSts == 1
								&& mCarHelpAirInfo.ACCM_AutoBlowActiveSts == 1) {
							Utils.showOtherToast(
									mContext.getResources().getString(R.string.carhelp_air_auto_cleaning_toast) + "\n"
											+ mContext.getResources().getString(R.string.carhelp_air_auto_blow_toast));
						} else {
							if (mCarHelpAirInfo.ACCM_AutoCleanActiveSts == 1) {
								Utils.showOtherToast(
										mContext.getResources().getString(R.string.carhelp_air_auto_cleaning_toast));
							}
							if (mCarHelpAirInfo.ACCM_AutoBlowActiveSts == 1) {
								Utils.showOtherToast(
										mContext.getResources().getString(R.string.carhelp_air_auto_blow_toast));
							}
						}
					}
					break;

				case T19CanRx.powerInfoType:
					if (mCallback != null) {
						mCallback.onDataChange((PowerInfo) msg.obj);
					}
					break;

				case T19CanRx.airInfoType:
					mAirInfo = (AirInfo) msg.obj;
					int flag = msg.arg2;
					Log.e(TAG, "flag: " + flag);
					if (null != mAirInfo) {
						if (flag == 1) {
							showAir(mAirInfo, true,false);
						} else if (flag == 0) {
							showAir(mAirInfo, false,false);
						}

					}
					break;

				case T19CanRx.tipsInfoType:
					mTipsInfo = (TipsInfo) msg.obj;
					if (null != mT19CanPopup) {
						showT19CanPopup(mTipsInfo);
					}
					break;
				case T19CanRx.vcuInfoType:
					mVcuInfo = (VcuInfo) msg.obj;
					if (mCallback != null) {
						mCallback.onDataChange(mVcuInfo);
					}
					if (mVcuInfo != null) {
						Intent intent = new Intent(send_external_temp_action);
						intent.putExtra("VCU_ExternalTempDisp", mVcuInfo.VCU_ExternalTempDisp);
						mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
					}
					break;
				case T19CanRx.dashboardConfigInfo1Type:
					if (mCallback != null) {
						mCallback.onDataChange((DashboardConfigInfo1) msg.obj);
					}
					break;

				default:
					break;
				}
			}
		}
	};

}
