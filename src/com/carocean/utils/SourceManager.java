package com.carocean.utils;

import android.content.Context;

public class SourceManager {

	private static final String TAG = "SourceManager";
	
	private static int iSource = Constants.KEY_SRC_MODE_FM;
	private static int iUnRegisterSource = Constants.KEY_SRC_MODE_FM; // 注销源此状态不改变

	public static void startSourceApp(Context context) {
		int source = getSource();
		sLog.i(TAG, "startSourceApp - source: " + source);
	}

	public static void acquireSource(Context context, int source) {
		sLog.i(TAG, "acquireSource - source: " + source);
		setSource(context,source);
	}

	public static void setSource(Context context, int source) {
		sLog.i(TAG, "setSource - source: " + source);
		if (source != iSource) {
			DataShared.getInstance(context).putInt(Constants.SP_KEY_SOURCE, source);
			DataShared.getInstance(context).commit();
			iSource = source;
			if (source != 0) {
				iUnRegisterSource = iSource;
			}
			SoundSourceInfoUtils.updateSourceInfo(source, iUnRegisterSource);
		}
	}

	public static void unregisterSource(Context context, int source) {
		sLog.i(TAG, "unregisterSource - source: " + source);
		if (iSource == source) {
			setSource(context, 0);
		}
	}
	
	public static int getSource() {
		return iSource;
	}

	public static int getUnRegisterSource() {
		return iUnRegisterSource;
	}
	
	public static int getSourceInit(Context context) {
		iSource = DataShared.getInstance(context).getInt(Constants.SP_KEY_SOURCE, iSource);
		iUnRegisterSource = iSource;
		return iSource;
	}
	
	public static boolean isMediaSource() {
		if (iSource == Constants.KEY_SRC_MODE_USB1 || iSource == Constants.KEY_SRC_MODE_USB2 || iSource == Constants.KEY_SRC_MODE_EXTERNAL) {
			return true;
		} else {
			return false;
		}
	}
}
