/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carocean.settings.sound;

import java.util.Arrays;

import com.autochips.settings.AtcSettings;
import com.autochips.settings.AtcSettings.Audio.SpeakerLayout;
import com.autochips.settings.AtcSettings.Audio.SpeakerSize;
import com.autochips.settings.AudFuncOption;
import com.carocean.ApplicationManage;
import com.carocean.utils.DataShared;
import com.carocean.utils.sLog;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class Mtksetting {

	public enum PEQPresetType {
		AUD_EQ_TYPE_OFF, AUD_EQ_USER, AUD_EQ_POP, AUD_EQ_CLASSICAL, AUD_EQ_JAZZ, AUD_EQ_DANCE, AUD_EQ_FOLK, AUD_EQ_VOICE, AUD_EQ_ROCK, AUD_EQ_BASS_BOOST, AUD_EQ_TREBLE_BOOST, AUD_EQ_UNDEF_TYPE,
	}

	public Mtksetting() {
		// TODO Auto-generated constructor stub
		for (int i = 0; i < SoundArray.mid_freqs.length; i++) {
			for (int j = 0; j < SoundArray.mid_freqs[i].length; j++) {
				SoundArray.mid_freqs[i][j] = SoundArray.freqValue31[i];
			}
		}

		for (int i = 0; i < SoundArray.mid_qvalues.length; i++) {
			for (int j = 0; j < SoundArray.mid_qvalues[i].length; j++) {
				SoundArray.mid_qvalues[i][j] = 1.414f;
			}
		}
		int result = AtcSettings.Audio.SetPEQOnOff(true, 31, 0);
		sLog.d("SetPEQOnOff : " + result);
		result = AtcSettings.Audio.SetPEQQ_Freq(SoundArray.mid_qvalues, SoundArray.mid_freqs);
		sLog.d("SetPEQQ_Freq : " + result);
	}

	public void OnSetEQValues(int uEQType, int uTreble, int uBass, int uAlto) {
		int[] EQGain = new int[11];
		for (int idx = 0; idx < 11; idx++) {
			int temp = 0;

			if (idx >= 1 && idx <= 3) {
				temp = SoundArray.gEQTypePos[uEQType][idx] + uBass;
			} else if (idx >= 8 && idx <= 10) {
				temp = SoundArray.gEQTypePos[uEQType][idx] + uTreble;
			} else if (idx >= 4 && idx <= 7) {
				temp = SoundArray.gEQTypePos[uEQType][idx] + uAlto;
			} else {
				temp = SoundArray.gEQTypePos[uEQType][idx];
			}

			if (temp > 14) {
				temp = 14;
			} else if (temp < -14) {
				temp = -14;
			}

			if (idx == 0) {
				EQGain[idx] = SoundArray.g_dryValues[temp + 14];
			} else {
				EQGain[idx] = SoundArray.g_ganValues[temp + 14];
			}
		}
		AtcSettings.Audio.SetEQValues(EQGain);
	}

	public void SetEQValues(int index) {
		// float uBass = uiState.getFloat(Utils.key_bass_angle, 0) * RANGLE /
		// 14;
		// float uTreble = uiState.getInt(Utils.key_treble_angle, 0) * RANGLE /
		// 14;
		// float uAlto = uiState.getInt(Utils.key_alto_angle, 0) * RANGLE / 14;
		// OnSetEQValues(index, (int) uTreble, (int) uBass, (int) uAlto);
	}

	public void EnableSubwoofer(boolean bEnable) {
		int u4SpeakerLayoutType = SpeakerLayout.SPEAKER_LAYOUT_LRCLSRS;
		int u4SpeakerSize = SpeakerSize.SPEAKER_LAYOUT_L_LARGE | SpeakerSize.SPEAKER_LAYOUT_R_LARGE | SpeakerSize.SPEAKER_LAYOUT_C_LARGE
				| SpeakerSize.SPEAKER_LAYOUT_LS_LARGE | SpeakerSize.SPEAKER_LAYOUT_RS_LARGE;

		if (bEnable) {
			u4SpeakerLayoutType |= SpeakerLayout.SPEAKER_LAYOUT_SUBWOOFER;
		}

		// �������ȹرգ���Ϊ�����������?
		// AudFuncOption afo = new AudFuncOption();
		// afo.u4FuncOption0 = 1 << 4;
		// AtcSettings.Audio.SetAudFuncOption(afo);

		AtcSettings.DVP.SetSpeakerLayout(u4SpeakerLayoutType, u4SpeakerSize);
		AtcSettings.Audio.SetSpeakerLayout(u4SpeakerLayoutType, u4SpeakerSize);

		// if(bEnable)
		// {
		// // subwoofer��ֵ, 0 ~ 40 (TRIM_LEVEL_MAX), ���ﲻ�������ֵȡ�м��?20.
		// SetSubwoofer(20);
		// }
	}

	public void SetSubwoofer(int Value) {
		AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(AtcSettings.Audio.BalanceType.SUB_WOOFER.ordinal()),
				SoundArray.Balance_au4TrimValue[Value] * 5);
		Log.i("lzy", "...........................uSubwoofer = " + Value);
	}

	public void setReverbCoef(int value) {
		int ReverbCoef[] = null;
		if (value == 0) {
			ReverbCoef = SoundArray.ReverbCoef_off;
		} else if (value == 1) {
			ReverbCoef = SoundArray.ReverbCoef_live;
		} else if (value == 2) {
			ReverbCoef = SoundArray.ReverbCoef_hall;
		} else if (value == 3) {
			ReverbCoef = SoundArray.ReverbCoef_concert;
		} else if (value == 4) {
			ReverbCoef = SoundArray.ReverbCoef_cave;
		} else if (value == 5) {
			ReverbCoef = SoundArray.ReverbCoef_bathroom;
		} else {
			ReverbCoef = SoundArray.ReverbCoef_arena;
		}
		SetReverbType(value, ReverbCoef);
	}

	// balance
	/**
	 * 0 front_left 1 fron_right 2 rear_left 3 rear_right
	 */
	public void setBalance(int valueX, int valueY) {

		final int MAX_EQ_BALANCE_LEVEL = 40;

		valueX = Math.min(valueX, MAX_EQ_BALANCE_LEVEL / 2);
		valueX = Math.max(valueX, -(MAX_EQ_BALANCE_LEVEL / 2));
		valueY = Math.min(valueY, MAX_EQ_BALANCE_LEVEL / 2);
		valueY = Math.max(valueY, -(MAX_EQ_BALANCE_LEVEL / 2));

		int attenuate_fl = 0;
		int attenuate_fr = 0;
		int attenuate_rl = 0;
		int attenuate_rr = 0;
		if (valueX >= 0) {
			attenuate_fl = valueX;
			attenuate_rl = valueX;
		} else {
			attenuate_fr = Math.abs(valueX);
			attenuate_rr = Math.abs(valueX);
		}

		if (valueY >= 0) {
			attenuate_rl = Math.max(attenuate_rl, valueY);
			attenuate_rr = Math.max(attenuate_rr, valueY);
		} else {
			attenuate_fl = Math.max(attenuate_fl, Math.abs(valueY));
			attenuate_fr = Math.max(attenuate_fr, Math.abs(valueY));
		}

		int i4FLeftValue = MAX_EQ_BALANCE_LEVEL - attenuate_fl * 2;
		int i4FRightValue = MAX_EQ_BALANCE_LEVEL - attenuate_fr * 2;
		int i4RLeftValue = MAX_EQ_BALANCE_LEVEL - attenuate_rl * 2;
		int i4RRightValue = MAX_EQ_BALANCE_LEVEL - attenuate_rr * 2;

		AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(AtcSettings.Audio.BalanceType.FRONT_LEFT.ordinal()),
				SoundArray.Balance_au4TrimValue[i4FLeftValue]);

		AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(AtcSettings.Audio.BalanceType.FRONT_RIGHT.ordinal()),
				SoundArray.Balance_au4TrimValue[i4FRightValue]);

		AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(AtcSettings.Audio.BalanceType.REAR_LEFT.ordinal()),
				SoundArray.Balance_au4TrimValue[i4RLeftValue]);

		AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(AtcSettings.Audio.BalanceType.REAR_RIGHT.ordinal()),
				SoundArray.Balance_au4TrimValue[i4RRightValue]);
	}

	void syncBrightnessToHW(Context context) {
		int backlightValue = DataShared.getInstance(context).uiState.getInt("backlight", 55); // backlight
		// saved: 0 ~100
		sLog.d("mtk kkk3 syncBrightnessToHW backlightValue=" + backlightValue);
		Settings.System.putInt(ApplicationManage.getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
				backlightValue * 235 / 100 + 20);
		if (backlightValue < 5) {
			AtcSettings.Display.SetBackLightLevel(5);
		} else {
			AtcSettings.Display.SetBackLightLevel(backlightValue);
			// backlight value setto HW:5~100
		}
	}

	/** BT_Volume setting */
	public int SetBTHFPVolume(int u4Vol) {
		sLog.d("SetBTHFPVolume");
		return AtcSettings.Audio.SetBTHFPVolume(u4Vol);
	}

	/** Display setting */
	public int GSetBrightnessLevel(int level) {
		sLog.d("GSetBrightnessLevel");
		return AtcSettings.Display.SetBrightnessLevel(level);
	}

	public int GSetContrastLevel(int level) {
		sLog.d("GSetContrastLevel");
		return AtcSettings.Display.SetContrastLevel(level);
	}

	public int GSetBackLightLevel(int level) {
		sLog.d("GSetBackLightLevel");
		return AtcSettings.Display.SetBackLightLevel(level);
	}

	public int GSetHueLevel(int level) {
		sLog.d("GSetHueLevel");
		return AtcSettings.Display.SetHueLevel(level);
	}

	public int GSetSaturationLevel(int level) {
		sLog.d("GSetSaturationLevel");
		return AtcSettings.Display.SetSaturationLevel(level);
	}

	/** Audio setting */
	public int SetMute(int eMute) {
		sLog.d("SetMute");
		boolean isMute = true;
		if (eMute == 0) {
			isMute = false;
		} else {
			isMute = true;
		}
		return AtcSettings.Audio.SetMute(isMute);
	}

	public int SelectDAC(int Output, int Type, int Pin) {
		sLog.d("SelectDAC");
		return AtcSettings.Audio.SelectDAC(Output, Type, Pin);
	}

	public int SetVolume(int u4Vol) {
		sLog.d("SetVolume");
		return AtcSettings.Audio.SetVolume(u4Vol);
	}

	public int SetRearVolume(int u4Vol) {
		sLog.d("SetRearVolume");
		return AtcSettings.Audio.SetRearVolume(u4Vol);
	}

	public int SetBalance(int u4Values, int eBalanceType) {
		sLog.d("SetBalance");
		return AtcSettings.Audio.SetBalance(AtcSettings.Audio.BalanceType.nativeToType(eBalanceType), u4Values);
	}

	public int SetReverbType(int eReverbType, int[] ReverbCoef) {
		sLog.d("SetReverbType");
		return AtcSettings.Audio.SetReverb(AtcSettings.Audio.ReverbType.nativeToType(eReverbType), ReverbCoef);
	}

	public int SetTestTone(int eTestTone, int eTestToneType) {
		sLog.d("SetTestTone");
		return AtcSettings.Audio.SetTestTone(AtcSettings.Audio.TestToneMainType.nativeToType(eTestTone),
				AtcSettings.Audio.TestToneSubType.nativeToType(eTestToneType));
	}

	public int SetUpMix(int eUpMixType, int[] aiUpMixGains) {
		sLog.d("SetUpMix");
		boolean isOn = true;

		if (eUpMixType == 0) {
			isOn = false;
		} else {
			isOn = true;
		}

		return AtcSettings.Audio.SetUpMix(isOn, aiUpMixGains);
	}

	public int SetLoudNess(int uLoudNessType, int[] aiLoudNessGains) {
		sLog.d("SetLoudNess");
		return AtcSettings.Audio.SetLoudness(AtcSettings.Audio.LoudnessMode.nativeToType(uLoudNessType), aiLoudNessGains);
	}

	public int SetEQValues(int[] rValues) {
		sLog.d("SetEQValues");
		return AtcSettings.Audio.SetEQValues(rValues);
	}

	public int SetAudFeature(int eAudFeatur) {
		sLog.d("SetAudFeature");
		return AtcSettings.Audio.SetDecFeatureType(AtcSettings.Audio.DecFeatureType.nativeToType(eAudFeatur));
	}

	public int SetAudFuncOption(AudFuncOption Afo) {
		sLog.d("SetAudFuncOption");
		return AtcSettings.Audio.SetAudFuncOption(Afo);
	}

	public int SetSRSSwitch(int eCSIISwitch) {
		sLog.d("SetSRSSwitch");
		return AtcSettings.Audio.SetSRSSwitchType(AtcSettings.Audio.SRSSwitchType.nativeToType(eCSIISwitch));
	}

	public int SetSRSMode(int eCSIIMode) {
		sLog.d("SetSRSMode");
		return AtcSettings.Audio.SetSRSMode(AtcSettings.Audio.SRSMode.nativeToType(eCSIIMode));
	}

	public int SetSRSPhantom(int eCSIISwitch) {
		sLog.d("SetSRSPhantom");
		boolean isOn = false;

		if (eCSIISwitch == 0) {
			isOn = false;
		} else {
			isOn = true;
		}

		return AtcSettings.Audio.SetSRSPhantomOn(isOn);
	}

	public int SetSRSFullBand(int eCSIISwitch) {
		sLog.d("SetSRSFullBand");
		boolean isOn = false;

		if (eCSIISwitch == 0) {
			isOn = false;
		} else {
			isOn = true;
		}

		return AtcSettings.Audio.SetSRSFullBandOn(isOn);
	}

	public int SetSRSFocus(int eFocus, int eCSIISwitch) {
		sLog.d("SetSRSFocus");
		boolean isOn = false;

		if (eCSIISwitch == 0) {
			isOn = false;
		} else {
			isOn = true;
		}

		return AtcSettings.Audio.SetSRSFocusOn(AtcSettings.Audio.SRSFocusType.nativeToType(eFocus), isOn);
	}

	public int SetSRSTrueBass(int eCSIISwitch) {
		sLog.d("SetSRSTrueBass");

		boolean isOn = false;

		if (eCSIISwitch == 0) {
			isOn = false;
		} else {
			isOn = true;
		}

		return AtcSettings.Audio.SetSRSTrueBassOn(isOn);
	}

	public int SetSRSTrueBassSize(int eTBSS, int eCSIITBSS) {
		sLog.d("SetSRSTrueBassSize");
		return AtcSettings.Audio.SetSRSTrueBassSize(AtcSettings.Audio.SRSTrueBassSizeType.nativeToType(eTBSS), eCSIITBSS);
	}

	public int SetPL2(int ePL2Type) {
		sLog.d("SetPL2");
		return AtcSettings.Audio.SetPL2(AtcSettings.Audio.PL2Type.nativeToType(ePL2Type));
	}

	public int ChooseSpdifOutput(int eOutType) {
		sLog.d("ChooseSpdifOutput");
		return AtcSettings.Audio.ChooseSpdifOutput(AtcSettings.Audio.SpdifOutputType.nativeToType(eOutType));
	}

	/** DVP setting */
	public int SetDisplayOutputType(int eTVDisplayType) {
		sLog.d("DVP_GSetDisplayType");
		return AtcSettings.DVP.SetDisplayType(eTVDisplayType);
	}

	public int SetTVType(int eTVType) {
		sLog.d("DVP_GSetTVType");
		return AtcSettings.DVP.SetTVType(eTVType);
	}

	public int SetPBCOn(int ePBCType) {
		sLog.d("DVP_GSetPBCType");
		return AtcSettings.DVP.SetPBCType(ePBCType);
	}

	public int SetAudioLanType(int eAudioLanType) {
		sLog.d("DVP_GSetAudioLanType");
		return AtcSettings.DVP.SetAudioLanType(eAudioLanType);
	}

	public int SetSubLanType(int eSubLanType) {
		sLog.d("DVP_GSetSubLanType");
		return AtcSettings.DVP.SetSubLanType(eSubLanType);
	}

	public int SetMenuLanType(int eMenuLanType) {
		sLog.d("DVP_GSetMenuLanType");
		return AtcSettings.DVP.SetMenuLanType(eMenuLanType);
	}

	public int SetParentalType(int eParentalType) {
		sLog.d("DVP_GSetParentalType");
		return AtcSettings.DVP.SetParentalType(eParentalType);
	}

	public int SetPwdModeType(int ePwdModeType) {
		sLog.d("DVP_GSetPwdModeType");
		return AtcSettings.DVP.SetPwdModeType(ePwdModeType);
	}

	public int SetDialogType(int eDialogType) {
		sLog.d("DVP_GSetDialogType");
		return AtcSettings.DVP.SetDialogType(eDialogType);
	}

	public int SetSpdifOutputType(int eSpdifOutputType) {
		sLog.d("DVP_GSetSpdifOutputType");
		return AtcSettings.DVP.SetSpdifOutputType(eSpdifOutputType);
	}

	public int SetDualMonoType(int eDualMonoType) {
		sLog.d("DVP_GSetDualMonoType");
		return AtcSettings.DVP.SetDualMonoType(eDualMonoType);
	}

	public int SetDynamicType(int eDynamicType) {
		sLog.d("DVP_GSetDynamicType");
		return AtcSettings.DVP.SetDynamicType(eDynamicType);
	}

	public int SetPEQPresetType(PEQPresetType eType) {
		int[][] sendArg = new int[32][5];
		for (int i = 0; i < sendArg[i].length; i++) {
			sendArg[0][i] = SoundArray.g_dryValues[14];
		}
		for (int i = 1; i < sendArg.length; i++) {
			for (int j = 0; j < sendArg[i].length; j++) {
				sendArg[i][j] = SoundArray.g_ganValues[SoundArray.gEQTypePos31[eType.ordinal()][i - 1] + 14];
			}
		}
		int result = AtcSettings.Audio.SetPEQGain(sendArg);
		sLog.d("SoundArray.gEQTypePos31: " + Arrays.deepToString(SoundArray.gEQTypePos31));
		return result;
	}

	public void setUserEQTypePos31(int index, int value) {
		for (int i = 0; i < SoundArray.gEQTypePos31[SoundUtils.curPEQPresetType.ordinal()].length; i++) {
			SoundArray.gEQTypePos31[PEQPresetType.AUD_EQ_USER.ordinal()][i] = SoundArray.gEQTypePos31[SoundUtils.curPEQPresetType.ordinal()][i];
		}
		SoundArray.gEQTypePos31[PEQPresetType.AUD_EQ_USER.ordinal()][index] = value;
		SoundUtils.curPEQPresetType = PEQPresetType.AUD_EQ_USER;
		SetPEQPresetType(PEQPresetType.AUD_EQ_USER);
	}

}

/*
 * class Native { private final static String TAG = "mtksetting native";
 * 
 * static { // The runtime will add "lib" on the front and ".o" on the end of //
 * the name supplied to loadLibrary. Log.i(TAG,
 * "System.load(\"libmtkset.so\");"); System.loadLibrary("mtkset");
 * Log.i(TAG,"load ok"); }
 * 
 * static native int add(int a, int b); static native int
 * GSetBrightnessLevel(int level); static native int GSetContrastLevel(int
 * level); static native int GSetBackLightLevel(int level); static native int
 * GSetHueLevel(int level); static native int GSetSaturationLevel(int level);
 * 
 * 
 * static native int GSetMute(int eMute); static native int GSetVolume(int
 * u4Vol); static native int GSetRearVolume(int u4Vol); static native int
 * GSetEQValue(int i4Band, int i4Value); static native int
 * GClientSetEQValues(int eEQType, int[] rEQValues, int[] rEQGain, boolean
 * fgSetBassOrTreble); static native int GClientSetBalance(int u4Values, int
 * eBalanceType); static native int GClientSetLoudNess(int uLoudNessType, int[]
 * rLoudNessGain); static native int GClientSetVolume(int u4Vol); static native
 * int GClientSetRearVolume(int u4Vol); static native int GClientSetEQType(int
 * eEQType, int[] rEQTypeValues, int[] rEQValues, boolean fgSetBassOrTreble);
 * static native int GClientSetReverbType(int eReverbType, int[] ReverbCoef);
 * static native int GClientSetTestTone(int eTestTone, int eTestToneType);
 * static native int GClientSetUpMix(int eUpMixType, int[] rUpmixGain); static
 * native int GSetEQValues(int eEQType, int[] rValues); static native int
 * GSetTestTone(int eTestTone); static native int GSetAudFeature(int
 * eAudFeatur); static native int GSetBalance(int u4Values,int eBalanceType);
 * static native int GSetSRSSwitch(int eCSIISwitch); static native int
 * GSetSRSMode(int eCSIIMode); static native int GSetSRSPhantom(int
 * eCSIISwitch); static native int GSetSRSFullBand(int eCSIISwitch); static
 * native int GSetSRSFocus(int eFocus,int eCSIISwitch); static native int
 * GSetSRSTrueBass(int eCSIISwitch); static native int GSetSRSTrueBassSize(int
 * eTBSS,int eCSIITBSS); static native int GSetSpeakerLayout(int
 * u4SpeakerLayoutType, int u4SpeakerSize); static native int GSetEQType(int[]
 * rValues, int eEQType); static native int GSetPL2(int ePL2Type); static native
 * int GSetReverbType(int eReverbType); static native int GSetUpMix(int
 * eUpMixType); static native int GSetLoudNess(int uLoudNessType); static native
 * int GChooseSpdifOutput(int eOutType);
 * 
 * 
 * static native int DVP_GSetDisplayType(int eTVDisplayType); static native int
 * DVP_GSetCaptionsType(int eCaptionsType); static native int
 * DVP_GSetScreenSaverType(int eScreenSaverType); static native int
 * DVP_GSetLastMemType(int eLastMemType); static native int DVP_GSetTVType(int
 * eTVType); static native int DVP_GSetPBCType(int ePBCType); static native int
 * DVP_GSetAudioLanType(int eAudioLanType); static native int
 * DVP_GSetSubLanType(int eSubLanType); static native int
 * DVP_GSetMenuLanType(int eMenuLanType); static native int
 * DVP_GSetParentalType(int eParentalType); static native int
 * DVP_GSetPwdModeType(int ePwdModeType); static native int
 * DVP_GSetSpeakerLayout(int u4SpeakerLayoutType, int u4SpeakerSize); static
 * native int DVP_GSetDialogType(int eDialogType); static native int
 * DVP_GSetSpdifOutputType(int eSpdifOutputType); static native int
 * DVP_GSetLpcmOutType(int eLpcmOutType); static native int
 * DVP_GSetCdelayCType(int eCoelayCType); static native int
 * DVP_GSetCdelaySubType(int eCoelaySubType); static native int
 * DVP_GSetCdelayLSType(int eCoelayLSType); static native int
 * DVP_GSetCdelayRSType(int eCoelayRSType); static native int DVP_GSetEQType(int
 * eEQType); static native int DVP_GSetPL2(int ePL2Type); static native int
 * DVP_GSetReverbType(int eReverbType); static native int
 * DVP_GSetDualMonoType(int eDualMonoType); static native int
 * DVP_GSetDynamicType(int eDynamicType); static native int DVP_GSetVolume(int
 * u4Vol); static native int DVP_GSetRearVolume(int u4Vol); static native int
 * DVP_GSetEQValue(int u4Band, int i4Value); static native int
 * DVP_GSetBalance(int u4Values, int eBalanceType); }
 */
