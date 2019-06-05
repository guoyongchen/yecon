package com.carocean;

import java.util.LinkedList;
import java.util.List;

import com.carocean.service.BootService;
import com.carocean.utils.Constants;
import com.carocean.utils.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @ClassName: ApplicationManage
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.03.28
 **/
public class ApplicationManage extends Application {
	private static final String TAG = "ApplicationManage";

	public static ApplicationManage mInstance;

	public static Context context;

	private List<Activity> activityList = new LinkedList<Activity>();

	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		String processName = getProcessName(this, android.os.Process.myPid());
		Log.i(TAG, "volume_xu---on launcher Create, processName=" + processName);
		if (processName != null) {
			boolean defaultProcess = processName.equals("com.carocean");
			if (defaultProcess) {
				// 当前应用的初始化
				mInstance = this;
				if(Constants.isDebug){
					Intent intent = new Intent(this, BootService.class);
					intent.setAction("com.carocean.BootService");
					startService(intent);
					Utils.startBTService();
				}
			}
		}
	}

	public static ApplicationManage getInstance() {
		if (null == mInstance) {
			mInstance = new ApplicationManage();
		}
		return mInstance;
	}

	public static Context getContext() {
		return context;
	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		// System.exit(0);
	}

}
