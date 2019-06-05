package com.carocean.utils;

import android.app.ActivityManager;
import android.constant.YeconConstants;
import android.content.Context;
import android.content.Intent;

/**
 * 负责APP的启动 销毁等
 * 
 * @author xuhonghai
 *
 */
public class AppManager {
	/**
	 * 关闭自己的APK
	 * 
	 * @param context
	 * @param name
	 */
	public static void closeInternalApk(Context context, String name) {
		Intent intent = new Intent(YeconConstants.ACTION_QUIT_APK);
		intent.putExtra("apk_name", name);
		intent.setPackage(name);
		context.sendBroadcast(intent);
	}

	/**
	 * 杀掉某个应用
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void killBackgroudProcesses(Context context, String packageName) {
		try {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			am.killBackgroundProcesses(packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void killProcess(Context context, String packageName) {
		if (null == packageName) {
			return;
		}

		final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		if (am != null) {
			try {
				am.forceStopPackage(packageName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
