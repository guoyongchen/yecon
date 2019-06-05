package com.carocean.settings.utils;

import static android.constant.YeconConstants.PROPERTY_KEY_AUTOMUTE;
import static android.mcu.McuExternalConstant.MCU_ACTION_VOLUME_MUTE;

import com.yecon.settings.YeconSettings;

import android.constant.YeconConstants;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

/**
 * @ClassName: AudioMethordManager
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.07.16
 **/
public class AudioMethordManager {
    private final static String TAG="AudioMethordManager";
	static Context mContext;
	static AudioManager mAudioManager;
	static AudioMethordManager mInstance;

	public AudioMethordManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mAudioManager = getAudioManager(context);
	}

	public static AudioMethordManager getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new AudioMethordManager(context);
		}
		return mInstance;
	}

	public AudioManager getAudioManager(Context context) {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
		return mAudioManager;
	}

	public boolean isStreamMute(Context context, int streamType) {
		return mAudioManager.isStreamMute(streamType);
	}

	public int getStreamVolume(Context context, int streamType) {
		boolean isMute = mAudioManager.isStreamMute(streamType);
		return isMute ? 0 : mAudioManager.getStreamVolume(streamType);
	}

	public void setStreamVolume(Context context, int value) {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_GIS, value, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, value, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, value, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_BACKCAR, value, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RDS, value, 0);
	}

	public void closeMute(Context context) {
		if (SystemProperties.getBoolean(PROPERTY_KEY_AUTOMUTE, false)) {
			return;
		}
		boolean muted = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);
		if (muted) {
			mAudioManager.setYeconVolumeMute(AudioManager.STREAM_MUSIC, false, 0, YeconConstants.SRC_VOLUME_UNMUTE);
		}
	}

	// 当前音量大于等于开机默认时回到默认，小于默认时记忆当前值
	public void adjustPoweronVolume(Context context) {
		if (mAudioManager != null) {
			int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.i(TAG, "volume_xu---curVolume="+curVolume+",SettingConstants.defaultVolume="+SettingConstants.defaultVolume);
			if (curVolume > SettingConstants.defaultVolume) {
				YeconSettings.setDefaultVolume(context, SettingConstants.defaultVolume);
			} else if (0 == curVolume || mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
				// notify statusbar to show mute icon.
				Intent intent = new Intent(MCU_ACTION_VOLUME_MUTE);
				intent.putExtra("mute", true);
				context.sendBroadcast(intent);
			}
		}
	}

}
