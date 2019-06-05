package com.carocean.settings.sound;

import com.autochips.settings.AtcSettings;
import com.autochips.settings.AudFuncOption;
import com.carocean.settings.sound.Mtksetting.PEQPresetType;
import com.carocean.settings.sound.SoundUtils.AUDParaT;
import com.carocean.utils.DataShared;
import com.carocean.utils.sLog;
import com.yecon.metazone.YeconMetazone;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.Log;

/**
 * @author liuzhiyuan
 *
 */
public class SoundMethodManager {

	private static boolean mDvpSettingfag = false;
	public Mtksetting mMtksetting;
	public static SoundMethodManager mInstance;
	public static DataShared mDataShared;
	static Context mContext;

	public SoundMethodManager(Context context) {
		// TODO Auto-generated constructor stub
		if (mMtksetting == null) {
			mContext = context;
			mMtksetting = new Mtksetting();
			mDataShared = DataShared.getInstance(context);
		}
	}

	public static SoundMethodManager getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new SoundMethodManager(context);
		}
		return mInstance;
	}

	public void initSoundData() {
		initFromSettings(mContext);
		getFactorySoundData();
		if (YeconMetazone.GetPoweroffFlag() == 0) {
			saveSoundData();
		} else {
			restoreSoundData();
		}
		setSoundEffect();
	}

	public void recoverySoundData() {
		getFactorySoundData();
		saveSoundData();
		setSoundEffect();
	}

	private void getFactorySoundData() {
		for (int i = 0; i < SoundUtils.PERSYS_AUDIO.length; i++) {
			SoundUtils.audio[i] = SystemProperties.getInt(SoundUtils.PERSYS_AUDIO[i], SoundUtils.audio[i]);
		}
		for (int i = 0; i < SoundUtils.rotateAngle.length - 1; i++) {
			SoundUtils.rotateAngle[i] = SoundUtils.audio[i] * SoundUtils.RANGLE / 14;
		}
		SoundUtils.rotateAngle[AUDParaT.SUBWOOFER.ordinal()] = SoundUtils.audio[AUDParaT.SUBWOOFER.ordinal()] * SoundUtils.RANGLE / 20
				- SoundUtils.RANGLE;
		SoundUtils.curPEQPresetType = Mtksetting.PEQPresetType.AUD_EQ_TYPE_OFF;
		SoundUtils.bSubwooferEnable = SystemProperties.getBoolean(SoundUtils.PERSYS_SUBWOOFER_ENABLE, true);
		SoundUtils.nReverbCoef = 0;
		SoundUtils.bLoundness = false;
	}

	public void resetSoundEffect() {
		getFactorySoundData();
		setSoundEffect();
	}

	public int SetPEQPresetType(PEQPresetType eType) {
		return mMtksetting.SetPEQPresetType(eType);
	}

	public void setSoundEffect() {
		// eq
		mMtksetting.SetPEQPresetType(SoundUtils.bEQEnable ? SoundUtils.curPEQPresetType : PEQPresetType.AUD_EQ_TYPE_OFF);
		// subwoofer
		mMtksetting.EnableSubwoofer(SoundUtils.bSubwooferEnable);
		if (SoundUtils.bSubwooferEnable)
			mMtksetting.SetSubwoofer(SoundUtils.audio[AUDParaT.SUBWOOFER.ordinal()]);
		// scene
		mMtksetting.setReverbCoef(SoundUtils.nReverbCoef);
		// loundness
		int nLoudNessType = SoundUtils.bLoundness ? SoundUtils.audio[AUDParaT.LOUNDNESS.ordinal()] : 0;
		mMtksetting.SetLoudNess(nLoudNessType, SoundArray.LoudNess_gLoudNessGain[nLoudNessType]);
		// balance
		mMtksetting.setBalance(SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()], SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()]);
	}

	public void saveSoundData() {

		mDataShared.putBoolean(SoundUtils.key_eq_enable, SoundUtils.bEQEnable);
		mDataShared.putInt(SoundUtils.key_eqType, SoundUtils.curPEQPresetType.ordinal());
		mDataShared.saveUserEQGain(SoundArray.gEQTypePos31[Mtksetting.PEQPresetType.AUD_EQ_USER.ordinal()], SoundUtils.key_eqgain_user);
		SoundUtils.audio[AUDParaT.TREBLE.ordinal()] = SoundArray.gEQTypePos31[SoundUtils.curPEQPresetType.ordinal()][SoundUtils.nTreble];
		SoundUtils.audio[AUDParaT.ALTO.ordinal()] = SoundArray.gEQTypePos31[SoundUtils.curPEQPresetType.ordinal()][SoundUtils.nAlto];
		SoundUtils.audio[AUDParaT.BASS.ordinal()] = SoundArray.gEQTypePos31[SoundUtils.curPEQPresetType.ordinal()][SoundUtils.nBass];
		mDataShared.putFloat(SoundUtils.key_treble_angle, SoundUtils.audio[AUDParaT.TREBLE.ordinal()] * SoundUtils.RANGLE / 14);
		mDataShared.putFloat(SoundUtils.key_alto_angle, SoundUtils.audio[AUDParaT.ALTO.ordinal()] * SoundUtils.RANGLE / 14);
		mDataShared.putFloat(SoundUtils.key_bass_angle, SoundUtils.audio[AUDParaT.BASS.ordinal()] * SoundUtils.RANGLE / 14);
		mDataShared.putFloat(SoundUtils.key_subwoofer_angle, SoundUtils.rotateAngle[AUDParaT.SUBWOOFER.ordinal()]);
		mDataShared.putInt(SoundUtils.key_subwoofer, SoundUtils.audio[AUDParaT.SUBWOOFER.ordinal()]);
		mDataShared.putInt(SoundUtils.key_loudness, SoundUtils.audio[AUDParaT.LOUNDNESS.ordinal()]);
		mDataShared.putBoolean(SoundUtils.key_loudness_enable, SoundUtils.bLoundness);
		mDataShared.putInt(SoundUtils.key_reverbCoef, SoundUtils.nReverbCoef);
		mDataShared.putInt(SoundUtils.key_balance_x, SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()]);
		mDataShared.putInt(SoundUtils.key_balance_y, SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()]);
		mDataShared.commit();
	}

	public void restoreSoundData() {
		SoundUtils.bEQEnable = mDataShared.getBoolean(SoundUtils.key_eq_enable, SoundUtils.bEQEnable);
		int typeEQ = mDataShared.getInt(SoundUtils.key_eqType, Mtksetting.PEQPresetType.AUD_EQ_TYPE_OFF.ordinal());
		SoundUtils.curPEQPresetType = Mtksetting.PEQPresetType.values()[typeEQ];
		if (SoundUtils.curPEQPresetType == Mtksetting.PEQPresetType.AUD_EQ_USER) {
			if (mDataShared.getUserEQGain(SoundUtils.key_eqgain_user) != null) {
				SoundArray.gEQTypePos31[Mtksetting.PEQPresetType.AUD_EQ_USER.ordinal()] = mDataShared.getUserEQGain(SoundUtils.key_eqgain_user);
			}
		}
		SoundUtils.rotateAngle[AUDParaT.TREBLE.ordinal()] = mDataShared.getFloat(SoundUtils.key_treble_angle,
				SoundUtils.rotateAngle[AUDParaT.TREBLE.ordinal()]);
		SoundUtils.rotateAngle[AUDParaT.ALTO.ordinal()] = mDataShared.getFloat(SoundUtils.key_alto_angle,
				SoundUtils.rotateAngle[AUDParaT.ALTO.ordinal()]);
		SoundUtils.rotateAngle[AUDParaT.BASS.ordinal()] = mDataShared.getFloat(SoundUtils.key_bass_angle,
				SoundUtils.rotateAngle[AUDParaT.SUBWOOFER.ordinal()]);
		SoundUtils.rotateAngle[AUDParaT.SUBWOOFER.ordinal()] = mDataShared.getFloat(SoundUtils.key_subwoofer_angle, -SoundUtils.RANGLE);
		SoundUtils.audio[AUDParaT.SUBWOOFER.ordinal()] = mDataShared.getInt(SoundUtils.key_subwoofer, SoundUtils.audio[AUDParaT.SUBWOOFER.ordinal()]);
		SoundUtils.audio[AUDParaT.LOUNDNESS.ordinal()] = mDataShared.getInt(SoundUtils.key_loudness, SoundUtils.audio[AUDParaT.LOUNDNESS.ordinal()]);
		SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()] = mDataShared.getInt(SoundUtils.key_balance_x, SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()]);
		SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()] = mDataShared.getInt(SoundUtils.key_balance_y, SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()]);
		SoundUtils.bSubwooferEnable = SystemProperties.getBoolean(SoundUtils.PERSYS_SUBWOOFER_ENABLE, SoundUtils.bSubwooferEnable);
		SoundUtils.bLoundness = mDataShared.getBoolean(SoundUtils.key_loudness_enable, SoundUtils.bLoundness);
		SoundUtils.nReverbCoef = mDataShared.getInt(SoundUtils.key_reverbCoef, SoundUtils.nReverbCoef);
	}

	private void initFromSettings(Context context) {
		// Audio
		int bass;
		int treble;
		int balance;
		int loudness;
		int EQ;
		int reverb;
		int pl2;
		int upmix;
		int spdifout;
		int srsstate;
		int srsmode;
		int phantom;
		int fullband;
		int truebass;

		int speakertype;
		int speakersize;
		int testspeaker;

		int tvtype;
		int caption;
		int screensaver;
		int lasemem;
		int dialog;
		int dolbymode;
		int dolbydyn;
		int tvdisplay;
		int pbc;
		int audiolan;
		int sublan;
		int menulan;
		int parental;

		int DACType = YeconMetazone.GetAudioDACType();
		Log.d("WYD", "DAC TYPE = " + DACType);
		AtcSettings.Audio.SelectDAC(0, DACType, 2);
		AtcSettings.Audio.SetDspMixCh(0x7F);
                

        AtcSettings.Audio.SetPrimaryMic(0);
        //by lzy 
		bass = mDataShared.getInt("bass", 0);
		treble = mDataShared.getInt("treble", 0);
		balance = mDataShared.getInt("balance", 0);
		loudness = mDataShared.getInt("loudness", 0);
		EQ = mDataShared.getInt("eq_key", 0);
		reverb = mDataShared.getInt("reverb_key", 0);
		pl2 = mDataShared.getInt("pl2_key", 0);
		upmix = mDataShared.getInt("mix_key", 0);
		spdifout = mDataShared.getInt("spdif_key", 0);
		srsstate = mDataShared.getInt("srsswitch_key", 0);
		srsmode = mDataShared.getInt("srsmode_key", -1);
		phantom = mDataShared.getInt("srsphantom_key", 0);
		fullband = mDataShared.getInt("srsfullband_key", 0);
		truebass = mDataShared.getInt("srstruebass_key", 0);
		testspeaker = mDataShared.getInt("testspeaker_key", 0);
		speakertype = mDataShared.getInt("speakerslayouttype_key", 384);
		speakersize = mDataShared.getInt("speakerslayoutsize_key", 31);

		// DVD
		tvdisplay = mDataShared.getInt("display", 0);
		caption = mDataShared.getInt("caption", 1);
		screensaver = mDataShared.getInt("screensaver_key", 0);
		lasemem = mDataShared.getInt("lastmemorytype", 0);
		dialog = mDataShared.getInt("dialogtype", 0);
		dolbymode = mDataShared.getInt("dualmonotype", 0);
		dolbydyn = mDataShared.getInt("dynamictype", 8);
		tvtype = mDataShared.getInt("tvtype_key", 2);
		pbc = mDataShared.getInt("pbctype_key", 0);
		audiolan = mDataShared.getInt("audiolantype_key", 0);
		sublan = mDataShared.getInt("sublantype_key", 0);
		menulan = mDataShared.getInt("menulantype_key", 0);
		parental = mDataShared.getInt("parentaltype_key", 8);

		setAudio_Jni(bass, treble, balance, loudness, EQ, reverb, pl2, upmix, spdifout, srsstate, srsmode, phantom, fullband, truebass, testspeaker,
				speakertype, speakersize);

		SetDvd_Jni(tvdisplay, caption, screensaver, lasemem, dialog, dolbymode, dolbydyn, tvtype, pbc, audiolan, sublan, menulan, parental);
	}

	private void setAudio_Jni(int bass, int treble, int balance, int loudness, int EQ, int reverb, int pl2, int upmix, int spdifout, int srsstate,
			int srsmode, int phantom, int fullband, int truebass, int testspeaker, int speakertype, int speakersize) {

		String init = mDataShared.getString("init", "");
		if (init.equals("")) {
			int SPEAKER_LAYOUT_LRLSRS = 0x0001 << 6;
			int SPEAKER_LAYOUT_LRCLSRS = 0x0001 << 7; // L/R/C/LS/RS
			int SPEAKER_LAYOUT_SUBWOOFER = 0x0001 << 8; // Sub

			int SPEAKER_LAYOUT_C_LARGE = 0x0001 << 0; // Center channel large
			int SPEAKER_LAYOUT_L_LARGE = 0x0001 << 1; // Left channel large
			int SPEAKER_LAYOUT_R_LARGE = 0x0001 << 2; // Right channel large
			int SPEAKER_LAYOUT_LS_LARGE = 0x0001 << 3; // Left surround channel
														// large
			int SPEAKER_LAYOUT_RS_LARGE = 0x0001 << 4; // Right surround channel
														// large

			speakertype = SPEAKER_LAYOUT_LRLSRS;
			speakersize = SPEAKER_LAYOUT_L_LARGE | SPEAKER_LAYOUT_R_LARGE | SPEAKER_LAYOUT_LS_LARGE | SPEAKER_LAYOUT_RS_LARGE;

			SharedPreferences.Editor mADECEditor = mDataShared.editor;

			mADECEditor.putInt("mix_key", 1);
			mADECEditor.putInt("frontspeaker_key", 0);
			mADECEditor.putInt("centerspeakeron_key", 0);
			mADECEditor.putInt("surroundon_key", 0);
			mADECEditor.putInt("subwooferspeaker_key", 1);
			mADECEditor.putInt("speakerslayouttype_key", speakertype);
			mADECEditor.putInt("speakerslayoutsize_key", speakersize);

			mADECEditor.putInt("domnmix_state", 2);
			mADECEditor.putInt("centerspeaker_state", 0);
			mADECEditor.putInt("surround_state", 0);

			mADECEditor.putString("init", "init");
			mADECEditor.commit();
			sLog.w("BootCompletedReceiver", "FirstInit, speakertype:" + speakertype + ", speakersize:" + speakersize);

		} else {
			sLog.w("BootCompletedReceiver", "Not FirstInit, speakertype:" + speakertype + ", speakersize:" + speakersize);
		}
		// mmtksetting.SetSpeakerLayout(speakertype, speakersize);

		// modify by lzy 20180131
		AudFuncOption afo = new AudFuncOption();
		afo.u4FuncOption0 |= 0x1F3C;
		mMtksetting.SetAudFuncOption(afo);

		// modify by lzy 2015.9.21
		// int[] EQGain = new int[11];
		// for (int idx = 0; idx < 11; idx++) {
		// int temp = 0;

		//
		// if (idx >= 1 && idx <= 3) {
		// temp = Array.gEQTypePos[EQ][idx] + bass;
		// } else if (idx >= 8 && idx <= 10) {
		// temp = Array.gEQTypePos[EQ][idx] + treble;
		// } else {
		// temp = Array.gEQTypePos[EQ][idx];
		// }
		//
		// if (temp > 14) {
		// temp = 14;
		// } else if (temp < -14) {
		// temp = -14;
		// }
		//
		// if (idx == 0) {
		// EQGain[idx] = Array.g_dryValues[temp + 14];
		// } else {
		// EQGain[idx] = Array.g_ganValues[temp + 14];
		// }
		// }
		// if (mmtksetting == null) {
		// } else {
		// mmtksetting.SetEQValues(EQGain);
		// }

		// int i4RightValue = balance + 20;
		// int i4LeftValue = 40 - i4RightValue;
		// mmtksetting.SetBalance(Array.Balance_au4TrimValue[i4LeftValue],
		// 0);
		// mmtksetting.SetBalance(Array.Balance_au4TrimValue[i4LeftValue],
		// 2);
		// mmtksetting.SetBalance(Array.Balance_au4TrimValue[i4RightValue],
		// 1);
		// mmtksetting.SetBalance(Array.Balance_au4TrimValue[i4RightValue],
		// 3);
		// int[] rLoudNessGain = Array.LoudNess_gLoudNessGain[loudness];
		// mmtksetting.SetLoudNess(loudness, rLoudNessGain);

		// int ReverbCoef[] = null;
		// if (reverb == 0) {
		// ReverbCoef = Array.ReverbCoef_off;
		// } else if (reverb == 1) {
		// ReverbCoef = Array.ReverbCoef_live;
		// } else if (reverb == 2) {
		// ReverbCoef = Array.ReverbCoef_hall;
		// } else if (reverb == 3) {
		// ReverbCoef = Array.ReverbCoef_concert;
		// } else if (reverb == 4) {
		// ReverbCoef = Array.ReverbCoef_cave;
		// } else if (reverb == 5) {
		// ReverbCoef = Array.ReverbCoef_bathroom;
		// } else {
		//
		// ReverbCoef = Array.ReverbCoef_arena;
		// }
		// mmtksetting.SetReverbType(reverb, ReverbCoef);

		upmix = mDataShared.getInt("mix_key", 0);
		int[] rUpmixGain = null;
		if (upmix == 0) {
			rUpmixGain = SoundArray.rUpmixGain_0;
		} else {
			rUpmixGain = SoundArray.rUpmixGain_1;
		}
		mMtksetting.SetUpMix(upmix, rUpmixGain);

		double mspeakerSize = 2;
		int centerspeak = mDataShared.getInt("centerspeaker_state", 0);
		int surroundspeak = mDataShared.getInt("surround_state", 0);
		int subwoofer = mDataShared.getInt("subwooferspeaker_key", 0);
		if (centerspeak != 2)
			mspeakerSize += 2;
		if (surroundspeak != 2)
			mspeakerSize += 2;
		if (subwoofer == 1)
			mspeakerSize += 1;

		int enableSrs = mDataShared.getInt("Share_SRS", 0);
		int enablePl2 = mDataShared.getInt("Share_PL2", 0);
		if (enablePl2 == 0 && enableSrs == 0) {
			Log.d("AdvanceAudioSettings", "Set mSrs and mPl2");
		} else if (enablePl2 == 0 && enableSrs == 1) {
			Log.d("AdvanceAudioSettings", "Set mSrs");
			if (mspeakerSize > 5) {
				mMtksetting.SetSRSSwitch(srsstate);
				mMtksetting.SetSRSMode(srsmode);
				mMtksetting.SetSRSPhantom(phantom);
				mMtksetting.SetSRSFullBand(fullband);
				mMtksetting.SetSRSTrueBass(truebass);
			}
		} else if (enablePl2 == 1 && enableSrs == 0) {
			Log.d("AdvanceAudioSettings", "Set Pl2");
			if (mspeakerSize > 2) {
				mMtksetting.SetPL2(pl2);
				mDvpSettingfag = true;
			}
		}

		// i4RightValue = balance;
		// i4LeftValue = -balance;
		// int rValues[] = new int[6];
		// rValues[0] = 0;
		// rValues[1] = i4LeftValue;
		// rValues[2] = i4RightValue;
		// rValues[3] = i4LeftValue;
		// rValues[4] = i4RightValue;
		// rValues[5] = 0;
		// int rvalue[] = new int[]{0, bass, bass, bass, 0, 0, 0, 0, treble,
		// treble, treble};
		// int mVolumeValue = mDataShared.getInt("volume_value", 0);
		// int value = 0;
		// if( mVolumeValue == 1 ) {
		// value = 1;
		// int soundEffict =
		// Settings.System.getInt(mContext.getContentResolver(),
		// Settings.System.SOUND_EFFECTS_ENABLED, 0);
		// if( soundEffict == 1 ) {
		// Settings.System.putInt(mContext.getContentResolver(),
		// Settings.System.SOUND_EFFECTS_ENABLED, 0);
		// }
		// }else {
		// value = 0;
		// int soundEffict =
		// Settings.System.getInt(mContext.getContentResolver(),
		// Settings.System.SOUND_EFFECTS_ENABLED, 0);
		// if( soundEffict == 1 ) {
		// }
		// }
		// mmtksetting.SetMute(value);
		// SharedPreferences.Editor mMuteEditor = mDataShared.edit();
		// mMuteEditor.putInt("volume_value", value);
		// mMuteEditor.commit();
	}

	private void SetDvd_Jni(int tvdisplay, int caption, int screencsver, int lastmen, int dialog, int dolbymode, int dolbydyn, int tvtype, int pbc,
			int audiolan, int sublan, int menulan, int parental) {
		// mmtksetting.GOpenDVPSet();
		mMtksetting.SetDisplayOutputType(tvdisplay);
		mMtksetting.SetTVType(tvtype);
		mMtksetting.SetPBCOn(pbc);
		mMtksetting.SetAudioLanType(audiolan);
		mMtksetting.SetSubLanType(sublan);
		mMtksetting.SetMenuLanType(menulan);
		mMtksetting.SetParentalType(parental);
		mMtksetting.SetPwdModeType(0);
		// mmtksetting.DVP_GSetSpeakerLayout(0, 6);
		mMtksetting.SetDialogType(dialog);
		mMtksetting.SetSpdifOutputType(1);
		// mmtksetting.DVP_GSetEQType(0);
		if (mDvpSettingfag == false) {
			// mmtksetting.DVP_GSetPL2(0);
		}
		// mmtksetting.DVP_GSetReverbType(0);
		mMtksetting.SetDualMonoType(dolbymode);
		mMtksetting.SetDynamicType(dolbydyn);
		// mmtksetting.DVP_GSetVolume(50);
		// mmtksetting.DVP_GSetRearVolume(50);
		// mmtksetting.DVP_GSetEQValue(0, 0);
		// mmtksetting.DVP_GSetBalance(1, 0);
		// mmtksetting.GCloseDVPSet();
	}

}
