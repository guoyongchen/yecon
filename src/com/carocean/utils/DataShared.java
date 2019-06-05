package com.carocean.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DataShared {
	public SharedPreferences.Editor editor;
	public SharedPreferences uiState;
	public static DataShared mInstance;
	static Context mContext;

	public DataShared(Context context) {
		mContext = context;
		setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(context));
	}

	public static DataShared getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new DataShared(context);
		}
		return mInstance;
	}

	public void setSharedPreferences(SharedPreferences mS) {
		uiState = mS;
		editor = uiState.edit();
	}

	/**
	 * sound effect
	 * 
	 * @param tag
	 * @return
	 */
	public int[] getUserEQGain(String tag) {
		String mValue = uiState.getString(tag, null);
		if (mValue == null)
			return null;
		String[] sValues = mValue.split("#");
		int[] iValues = new int[sValues.length];
		for (int i = 0; i < sValues.length; i++) {
			iValues[i] = Integer.parseInt(sValues[i]);
		}
		return iValues;
	}

	public void saveUserEQGain(int[] value, String tag) {
		StringBuffer mValues = new StringBuffer();
		for (int i = 0; i < value.length; i++) {
			mValues.append(value[i] + "#");
		}
		editor.putString(tag, mValues.toString());
		editor.commit();
	}

	public void putString(String tag, String value) {
		editor.putString(tag, value);
	}

	public String getString(String tag, String value) {
		return uiState.getString(tag, value);
	}

	public void putInt(String tag, int value) {
		editor.putInt(tag, value);
	}

	public int getInt(String tag, int value) {
		return uiState.getInt(tag, value);
	}

	public void putFloat(String tag, float f) {
		editor.putFloat(tag, f);
	}

	public float getFloat(String tag, float f) {
		return uiState.getFloat(tag, f);
	}

	public void putBoolean(String tag, boolean b) {
		editor.putBoolean(tag, b);
	}

	public boolean getBoolean(String tag, boolean b) {
		return uiState.getBoolean(tag, b);
	}

	public boolean commit() {
		return editor.commit();
	}

}
