package com.carocean.settings.sound;

import com.carocean.settings.sound.Mtksetting.PEQPresetType;

public class SoundUtils {
	public static final String TAG = "soundsetting";

	public static final String key_eq_enable = "eq_enable";
	public static final String key_eqgain_user = "eq_user_gain";
	public static final String key_eqType = "eqType";
	public static final String key_treble_angle = "treble_angle";
	public static final String key_alto_angle = "alto_angle";
	public static final String key_bass_angle = "bass_angle";
	public static final String key_subwoofer = "subwoofer";
	public static final String key_subwoofer_angle = "subwoofer_angle";
	public static final String key_subwoofer_enable = "subwoofer_enable";
	public static final String key_loudness_enable = "loudness_enable";
	public static final String key_loudness = "loudness";
	public static final String key_reverbCoef = "reverbCoef";
	public static final String key_balance_x = "balance_x";
	public static final String key_balance_y = "balance_y";

	public static boolean bEQEnable = true;
	public static int nReverbCoef = 0;
	public static boolean bLoundness = false;
	public static boolean bSubwooferEnable = true;

	public static PEQPresetType curPEQPresetType = PEQPresetType.AUD_EQ_POP;
	public static int nTreble = 26;
	public static int nAlto = 17;
	public static int nBass = 8;

	public enum AUDParaT {
		TREBLE, ALTO, BASS, SUBWOOFER, LOUNDNESS, BALANCE_Y, BALANCE_X,
	}

	public static int audio[] = { 0, 0, 0, 0, 15, 0, 0 };
	public static final float RANGLE = 128;
	public static float rotateAngle[] = { 0, 0, 0, -RANGLE };

	public static int systemWarning = 0;
	public static int velocity = 0;

	public final static String PERSYS_AUDIO[] = { "persist.sys.audio_treble", "persist.sys.audio_alto", "persist.sys.audio_bass",
			"persist.sys.audio_subwoofer", "persist.sys.audio_loundness", "persist.sys.balance_f_r", "persist.sys.balance_l_r" };
	public final static String PERSYS_SUBWOOFER_ENABLE = "persist.sys.fun.audio.subwoofer";
	public final static String PERSYS_ALTO_ENABLE = "persist.sys.fun.audio.alto";

	public static onEQTypeListener mOnEQTypeListener;

	public interface onEQTypeListener {
		public void onChangeEQType(Mtksetting.PEQPresetType eType);
	}

	public static void setEQTypeListener(onEQTypeListener lisenter) {
		mOnEQTypeListener = lisenter;
	}

	public static onBalanceListener mOnBalanceListener;

	public interface onBalanceListener {
		public void onChangeBalance(int lr, int fr);
	}

	public static void setBalanceListener(onBalanceListener lisenter) {
		mOnBalanceListener = lisenter;
	}

}
