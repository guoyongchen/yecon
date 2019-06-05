package com.carocean.settings.utils;

import static android.constant.YeconConstants.PROPERTY_KEY_STARTBACKCAR;

import java.util.Arrays;

import com.carocean.bt.BTUtils;
import com.carocean.bt.BTService;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.mcu.McuMethodManager;
import com.carocean.settings.screensaver.scUtils;
import com.carocean.settings.sound.SoundMethodManager;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;
import com.carocean.utils.sLog;
import com.carocean.vsettings.wifi.WifiUtils;
import com.yecon.metazone.YeconMetazone;
import com.yecon.settings.YeconSettings;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

/**
 * @author liuzhiyuan 20180328
 */
public class SettingMethodManager {
	private final static String TAG = "SettingMethodManager";
	static Context mContext;
	public static SettingMethodManager mInstance;
	public static DataShared mDataShared;
	public static SoundMethodManager mSoundMethodManager;
	public static AudioMethordManager mAudioMethordManager;

	WifiUtils mWifiUtils;

	public SettingMethodManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mDataShared = DataShared.getInstance(context);
		mSoundMethodManager = SoundMethodManager.getInstance(context);
		mAudioMethordManager = AudioMethordManager.getInstance(context);
		mWifiUtils = new WifiUtils(context);
	}

	public static SettingMethodManager getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new SettingMethodManager(context);
		}
		return mInstance;
	}

	void restoreSettingData() {

		for (int i = 0; i < SettingConstants.key_rgb_video.length; i++) {
			SettingConstants.videoPara[i] = mDataShared.getInt(SettingConstants.key_rgb_video[i],
					SettingConstants.videoPara[i]);
			Log.i(TAG, "backlight---restoreSettingData[videoPara]"+i+"="+SettingConstants.videoPara[i]);
		}

		SettingConstants.backlight = mDataShared.getInt(SettingConstants.key_backlight, SettingConstants.backlight);
        Log.i(TAG, "backlight---restoreSettingData[backlight]="+SettingConstants.backlight);

	}

	void resetSettingData() {
		launcherUtils.mTheme = 1;
		scUtils.timesIndex = 4;
		scUtils.imageSelected = 0;
		SettingConstants.autonavi = false;
		SettingConstants.collision = false;
		SettingConstants.brakeassist = false;
		SettingConstants.handlebrake = false;
		SettingConstants.ligth_control = false;
		SettingConstants.ldw = false;
		SettingConstants.intelligent_charging_pile = true;
		SettingConstants.speed_compensation_flag = 0;
		SettingConstants.speed_warning_switch = 0;
		SettingConstants.fatigue_driving_switch = 0;

		setSharedData();
	}

	public void getSharedData() {

		launcherUtils.mTheme = mDataShared.getInt(SettingConstants.key_ui_theme, launcherUtils.mTheme);
		scUtils.timesIndex = mDataShared.getInt(SettingConstants.key_screensaver_time, scUtils.timesIndex);
		scUtils.imageSelected = mDataShared.getInt(SettingConstants.key_screensaver_bg, scUtils.imageSelected);

		SettingConstants.autonavi = mDataShared.getBoolean(SettingConstants.key_autonavi, SettingConstants.autonavi);
		SettingConstants.collision = mDataShared.getBoolean(SettingConstants.key_collision, SettingConstants.collision);
		SettingConstants.brakeassist = mDataShared.getBoolean(SettingConstants.key_brakeassist,
				SettingConstants.brakeassist);
		SettingConstants.handlebrake = mDataShared.getBoolean(SettingConstants.key_handlebrake,
				SettingConstants.handlebrake);
		SettingConstants.ligth_control = mDataShared.getBoolean(SettingConstants.key_ligth_control,
				SettingConstants.ligth_control);
		SettingConstants.ldw = mDataShared.getBoolean(SettingConstants.key_ldw, SettingConstants.ldw);
		SettingConstants.intelligent_charging_pile = mDataShared
				.getBoolean(SettingConstants.key_intelligent_charging_pile, SettingConstants.intelligent_charging_pile);
		SettingConstants.speed_compensation_flag = mDataShared.getInt(SettingConstants.key_speed_compensation_flag,
				SettingConstants.speed_compensation_flag);
		SettingConstants.speed_warning_switch = mDataShared.getInt(SettingConstants.key_speed_warning_switch,
				SettingConstants.speed_warning_switch);
		SettingConstants.fatigue_driving_switch = mDataShared.getInt(SettingConstants.key_fatigue_driving_switch,
				SettingConstants.fatigue_driving_switch);
	}

	public void setSharedData() {
		mDataShared.putInt(SettingConstants.key_ui_theme, launcherUtils.mTheme);
		mDataShared.putInt(SettingConstants.key_screensaver_time, scUtils.timesIndex);
		mDataShared.putInt(SettingConstants.key_screensaver_bg, scUtils.imageSelected);

		mDataShared.getBoolean(SettingConstants.key_autonavi, SettingConstants.autonavi);
		mDataShared.getBoolean(SettingConstants.key_collision, SettingConstants.collision);
		mDataShared.putBoolean(SettingConstants.key_brakeassist, SettingConstants.brakeassist);
		mDataShared.putBoolean(SettingConstants.key_handlebrake, SettingConstants.handlebrake);
		mDataShared.putBoolean(SettingConstants.key_ligth_control, SettingConstants.ligth_control);
		mDataShared.getBoolean(SettingConstants.key_ldw, SettingConstants.ldw);
		mDataShared.getBoolean(SettingConstants.key_intelligent_charging_pile,
				SettingConstants.intelligent_charging_pile);
		mDataShared.putInt(SettingConstants.key_speed_warning_switch, SettingConstants.speed_warning_switch);
		mDataShared.putInt(SettingConstants.key_fatigue_driving_switch, SettingConstants.fatigue_driving_switch);

		for (int i = 0; i < SettingConstants.key_rgb_video.length; i++) {
			mDataShared.putInt(SettingConstants.key_rgb_video[i], SettingConstants.videoPara[i]);
			Log.i(TAG, "backlight---set shared data[rgb_video]"+i+"="+SettingConstants.videoPara[i]);
		}
		mDataShared.putInt(SettingConstants.key_backlight, SettingConstants.backlight);
        Log.i(TAG, "backlight---set shared data[backlight]="+SettingConstants.backlight);

		mDataShared.commit();
	}

	public void saveData() {
		setSharedData();
		writeMetazonePara();
		SoundMethodManager.getInstance(mContext).saveSoundData();
	}

	private void getFactoryData() {
		SettingConstants.defaultVolume = SystemProperties.getInt(PersistUtils.PERSYS_PWRON_VOLUME,
				SettingConstants.defaultVolume);
		SettingConstants.def_volume_switch = SystemProperties.getBoolean(PersistUtils.PERSYS_PWRON_VOLUME_ENABLE, true);
		Log.i(TAG, "volume_xu---defaultVolume=" + SettingConstants.defaultVolume + ", def_volume_switch="
				+ SettingConstants.def_volume_switch);

		for (int i = 0; i < SettingConstants.videoPara.length; i++) {
			SettingConstants.DEFAULT_RGB_VALUES[i] = SystemProperties.getInt(PersistUtils.PERSYS_RGB_VIDEO[0][i],
					SettingConstants.DEFAULT_RGB_VALUES[i]);
			SettingConstants.videoPara[i] = SettingConstants.DEFAULT_RGB_VALUES[i];
		}
		SettingConstants.DEFAULT_BACKLIGHT_VALUE = SystemProperties.getInt(PersistUtils.PERSYS_BKLIGHT[0],
				SettingConstants.DEFAULT_BACKLIGHT_VALUE);
		SettingConstants.backlight = SystemProperties.getInt(PersistUtils.PERSYS_BKLIGHT[0],
				SettingConstants.DEFAULT_BACKLIGHT_VALUE);
		SettingConstants.remix_enable = SystemProperties.getBoolean(PersistUtils.PERSYS_NAVI_REMIX, false);
		SettingConstants.remix_ratio = SystemProperties.getInt(PersistUtils.PERSYS_REMIX_RATIO,
				SettingConstants.remix_ratio);
		SettingConstants.backcar_mute = SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_MUTE,
				SettingConstants.backcar_mute);
		SettingConstants.factory_password = SystemProperties.get(PersistUtils.PERSYS_PASSWORD,
				SettingConstants.factory_password);
		SettingConstants.backcar_reduce_volume = SystemProperties.getBoolean(PersistUtils.PERSYS_BACKCAR_REDUCE_VOLUME,
				SettingConstants.backcar_reduce_volume);
	}

	public void initData() {
		getFactoryData();
		getSharedData();
		sLog.d("....................YeconMetazone.GetPoweroffFlag():" + YeconMetazone.GetPoweroffFlag());
		if (YeconMetazone.GetPoweroffFlag() == 0) {
			// cold boot
		} else {
			restoreSettingData();//xuhh  断ACC后启动，去读一些数值
		}

		setSettings();
		mSoundMethodManager.initSoundData();
	}

	void setSettings() {
		// set wallpaper
		// Utils.setDefWallPaper();

		// set volume
		mAudioMethordManager.closeMute(mContext);
		int poweroffFlag = YeconMetazone.GetPoweroffFlag();
		Log.i(TAG, "volume_xu---poweroffFlag=" + poweroffFlag + ",def_volume_switch="
				+ SettingConstants.def_volume_switch);
		if (poweroffFlag == 0) {
			YeconSettings.setDefaultVolume(mContext);
		} else {
			if (SettingConstants.def_volume_switch) {
				YeconSettings.adjustPoweronVolume(mContext);
			}
		}
	}

	public void recoveryFactoryPara() {
		resetSettingData();
		mSoundMethodManager.recoverySoundData();
	}

	public void WifiSwitch() {
		mWifiUtils.WifiSwitch();
	}

	public void BTSwitch() {
		boolean enable = BTUtils.mBluetooth.isbtopened();
		BTUtils.mBluetooth.switchbt(!enable);
	}

	public boolean isBackCar() {
		return SystemProperties.getBoolean(PROPERTY_KEY_STARTBACKCAR, false);
	}

	/**
	 * acc off 写参数到mzone, 先对比，有不同才写
	 */
	public void writeMetazonePara() {
		int mzone_ui_brightness = YeconMetazone.GetUIBrightness();
		int mzone_ui_constrast = YeconMetazone.GetUIConstrast();
		int mzone_ui_hue = YeconMetazone.GetUIHue();
		int mzone_ui_saturation = YeconMetazone.GetUISaturation();

		Log.i(TAG, "backlight---writeMetazonePara, mzone_ui_brightness="+mzone_ui_brightness
                +",mzone_ui_constrast="+mzone_ui_constrast
                +",mzone_ui_hue="+mzone_ui_hue
                +",mzone_ui_saturation="+mzone_ui_saturation);

        Log.i(TAG, "backlight---writeMetazonePara, SettingConstants.videoPara[0]="+SettingConstants.videoPara[0]
                +",SettingConstants.videoPara[1]="+SettingConstants.videoPara[1]
                +",SettingConstants.videoPara[2]="+SettingConstants.videoPara[2]
                +",SettingConstants.videoPara[3]="+SettingConstants.videoPara[3]);

		if (SettingConstants.videoPara[0] != mzone_ui_brightness || SettingConstants.videoPara[1] != mzone_ui_constrast
				|| SettingConstants.videoPara[2] != mzone_ui_hue
				|| SettingConstants.videoPara[3] != mzone_ui_saturation) {

			YeconMetazone.MetaSetUIVideoPara(SettingConstants.videoPara[0], SettingConstants.videoPara[1],
					SettingConstants.videoPara[2], SettingConstants.videoPara[3]);

            Log.i(TAG, "backlight---YeconMetazone.MetaSetUIVideoPara:"
					+ Arrays.toString(SettingConstants.videoPara));
			// setMcuSystemPara();
		}
	}

	private void setMcuSystemPara() {
		McuMethodManager.SystemParam para = new McuMethodManager.SystemParam();
		para.brightness = SettingConstants.videoPara[0];
		para.contrast = SettingConstants.videoPara[1];
		para.hue = SettingConstants.videoPara[2];
		para.saturation = SettingConstants.videoPara[3];
		para.backlight = SettingConstants.videoPara[4];
		para.backcarMirror = SettingConstants.backcar_mirror;
		McuMethodManager.getInstance(mContext).setMcuSystemParam(para);
	}

}
