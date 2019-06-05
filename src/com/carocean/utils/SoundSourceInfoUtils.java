package com.carocean.utils;

import java.util.ArrayList;
import java.util.List;


public class SoundSourceInfoUtils {
	private static List<SourceInfoInterface> mCallback = new ArrayList<SourceInfoInterface>();

	public static void setCallback(SourceInfoInterface callback) {
		synchronized (mCallback) {
			mCallback.add(callback);
		}
	}

	public static void RegisterSourceInfo(SourceInfoInterface callback) {
		synchronized (mCallback) {
			if (!mCallback.contains(callback)) {
				mCallback.add(callback);
			}
		}
	}

	public static void UnRegisterSourceInfo(SourceInfoInterface callback) {
		synchronized (mCallback) {
			if (mCallback.contains(callback)) {
				mCallback.remove(callback);
			}
		}
	}
	
	public static void updateSourceInfo(int source, int iUnRegisterSource) {
		synchronized (mCallback) {
			if (mCallback != null && mCallback.size() > 0) {
				for (SourceInfoInterface sourceInfoInterface : mCallback) {
					if (sourceInfoInterface != null) {
						sourceInfoInterface.updateSourceInfo(source, iUnRegisterSource);
					}
				}
			}
		}
	}
	
	public static void updateRadioPlayStatus(boolean bPlay) {
		synchronized (mCallback) {
			if (mCallback != null && mCallback.size() > 0) {
				for (SourceInfoInterface sourceInfoInterface : mCallback) {
					if (sourceInfoInterface != null) {
						sourceInfoInterface.updateRadioPlayStatus(bPlay);
					}
				}
			}
		}
	}
}
