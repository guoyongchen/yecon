package com.carocean.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

public class ActivityMonitor {

	public interface ActivityMonitorLisenter{
		void onForeGround();
		void onBackGround();
	};
	
	private static final String ACTIVITY_CHANGE = "android.activity.action.STATE_CHANGED";
	
	private static final String HOVER_PACKAGE[] = new String[] {
//		YeconConstants.CANBUS_PACKAGE_NAME,
//		YeconConstants.CAR_SETTING_PACKAGE_NAME
		"com.can.activity",
        "com.yecon.backcar",
        "com.yecon.usbcamera"
//		"com.yecon.carsetting",
//		"com.hcn.autoeq"
//		"com.yecon.backcar.BackCarActivity",
//		"com.autochips.bluetooth.PhoneCallActivity"
	};
	
	private static final String TAG = "ActivityMonitor";
	
	private String mMonitorPacketName="";
	
	private boolean mbForeground = true;
	
	private ActivityMonitorLisenter mActivityMonitorLisenter;
	public void setActivityMonitorLisenter(String PacketName, ActivityMonitorLisenter ActivityMonitorLisenter){
		this.mActivityMonitorLisenter = ActivityMonitorLisenter;
		mMonitorPacketName = PacketName.toLowerCase();
	}
		
	public ActivityMonitor(Context context) {
		super();
		this.context = context;
	}
	
	private static final int MAX_TASK = 3;
	
	public boolean isForeground() {
		//if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
//		if (android.os.Build.VERSION.SDK_INT < 21) {
//			return PreLolipop();
//		} else {
//			return UnderLolipop();
//		}
		return beforeall();
	}
	
	private boolean PreLolipop() {
		int iMonitorPacketLevel = -1;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> tasksAll = am.getRecentTasks(MAX_TASK, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		if (tasksAll != null) {
			for (RecentTaskInfo recentTaskInfo : tasksAll) {
				if (recentTaskInfo.id > -1) {
					boolean bHoverPacket = false;
					if (recentTaskInfo.baseIntent == null) {
						iMonitorPacketLevel++;
					} else {
						String baseintent = recentTaskInfo.baseIntent.toString().toLowerCase();
						for (String hoverPacket : HOVER_PACKAGE) {
							if (baseintent.contains(hoverPacket)) {
								bHoverPacket = true;
								break;
							}
						}
						if (!bHoverPacket) {
							iMonitorPacketLevel++;
							if (baseintent.contains(mMonitorPacketName)) {
								break;
							}
						}
					}
				}
			}
			Log.e(TAG + ".isForeground", "iMonitorPacketLevel:" + iMonitorPacketLevel);
		}
		return (iMonitorPacketLevel == 0);
	}

	private boolean beforeall() {
		int iMonitorPacketLevel = -1;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksAll = am.getRunningTasks(MAX_TASK);
		if (tasksAll != null) {
			boolean bfound = false;
			for (RunningTaskInfo recentTaskInfo : tasksAll) {
				boolean bHoverPacket = false;
				String baseintent = recentTaskInfo.baseActivity.getPackageName().toLowerCase();
				String activityname = recentTaskInfo.topActivity.toString().toLowerCase();
				Log.e(TAG, "8317:baseintent="+baseintent);
				for (String hoverPacket : HOVER_PACKAGE) {
					if (baseintent.contains(hoverPacket)) {
						bHoverPacket = true;
						break;
					}
				}
				if (!bHoverPacket) {
					iMonitorPacketLevel++;
					if (baseintent.contains(mMonitorPacketName)) {
						bfound = true;
						break;
					}
				}
			}
			if (!bfound) {
				iMonitorPacketLevel = -1;
			}
			Log.e(TAG + ".isForeground", "8317:iMonitorPacketLevel:" + iMonitorPacketLevel + "  mMonitorPacketName:"+mMonitorPacketName);
		}
		return (iMonitorPacketLevel == 0);
	}
	
	private boolean UnderLolipop() {
		int iMonitorPacketLevel = -1;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> tasksAll = am.getRunningAppProcesses();
		if (tasksAll != null) {
			for (RunningAppProcessInfo recentTaskInfo : tasksAll) {
				boolean bHoverPacket = false;
				String baseintent = recentTaskInfo.processName.toLowerCase();
				Log.e(TAG, "8317:baseintent="+baseintent);
				for (String hoverPacket : HOVER_PACKAGE) {
					if (baseintent.contains(hoverPacket)) {
						bHoverPacket = true;
						break;
					}
				}
				if (!bHoverPacket) {
					iMonitorPacketLevel++;
					if (baseintent.contains(mMonitorPacketName)) {
						break;
					}
				}
			}
			Log.e(TAG + ".isForeground", "8317:iMonitorPacketLevel:" + iMonitorPacketLevel + "  mMonitorPacketName:"+mMonitorPacketName);
		}
		return (iMonitorPacketLevel == 0);
	}
	
	RecentTaskInfo task_top_precall = null;
	public void save_task_top_precall(){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
   /*     PackageManager pm = context.getPackageManager();
        List<RunningTaskInfo> runningTasks = am.getRunningTasks(3);
        
        if (runningTasks == null || runningTasks.size() <= 0) {
            return;
        } else {
        	for (int i = 0; i < runningTasks.size(); i++) {
        		ComponentName cpName = runningTasks.get(i).topActivity;
        		if (cpName != null) {
        			String packageName = cpName.getPackageName();
            		String className = cpName.getClassName();
            		Log.d(TAG, "runningTask[" + i + "] => Package:" + packageName + " TopActivity:" + className);					
				}
			}
        }
        */
        
		List<RecentTaskInfo> recentTasks = am.getRecentTasks(1, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		Log.e(TAG, "save_task_top_precall recentTasks.size=" + recentTasks.size());
		if (recentTasks.size() <= 0) {
			task_top_precall = null;
			return;
		}
		RecentTaskInfo taskInfo =recentTasks.get(0); 
		ComponentName cpName = taskInfo.origActivity;
		Log.e(TAG, "save_task_top_precall ComponentName:" + cpName + 
				" id:" + taskInfo.id + 
				" persistentId:" + taskInfo.persistentId + 
				" intent:" + taskInfo.baseIntent + 
				" description:" + taskInfo.description);
		if (taskInfo.baseIntent.toString().contains("com.carocean/.vmedia.MediaActivity")) {
			task_top_precall = null;
			return;
		}else{
			task_top_precall = taskInfo;
		}
	}
	
	public void resume_task_top_precall(){
		Log.e(TAG, "resume_task_top_precall task_top_precall=" + task_top_precall);
		if (task_top_precall == null) {
			return;
		}
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		am.moveTaskToFront(task_top_precall.id, ActivityManager.MOVE_TASK_NO_USER_ACTION);
		task_top_precall = null;
	}
	
	
	private Context context;
	private BroadcastReceiver receiver;
	public void init(){
		IntentFilter filter = new IntentFilter();
        filter.addAction(ACTIVITY_CHANGE);
        receiver = new Receiver();
        context.registerReceiver(receiver, filter);
	}
	
	public void deinit(){
		context.unregisterReceiver(receiver);
	}

	public static boolean isOpen(Context context, String packageName) {
		if (packageName.equals("") | packageName == null)
			return false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = am
		.getRunningAppProcesses();
		for (RunningAppProcessInfo runinfo : runningAppProcesses) {
			String pn = runinfo.processName;
			if (pn.equals(packageName) 
					&& runinfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
				return true;
		}
		return false;
	}
	
	class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			synchronized (TAG) {
				if (ACTIVITY_CHANGE.equals(action)
						&& mActivityMonitorLisenter != null) {
					if (isForeground()) {
						if (!mbForeground) {
							mbForeground = true;
							mActivityMonitorLisenter.onForeGround();
						}
					} else {
						if (mbForeground) {
							mbForeground = false;
							mActivityMonitorLisenter.onBackGround();
						}
					}
				}
			}
		}
	}
}
