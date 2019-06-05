package com.carocean.settings.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.utils.sLog;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class tzUtils {

	public static boolean getBuzzerEnable(Context context) {
		return Settings.System.getInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
	}

	public static void setBuzzerEnable(Context context, boolean enable) {
		Settings.System.putInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, enable ? 1 : 0);
	}

	/**
	 * Returns true if Monkey is running.
	 */
	public static boolean isMonkeyRunning() {
		return ActivityManager.isUserAMonkey();
	}

	public static void recoverySystem(Context context, boolean isEraseSdCard) {
		if (isMonkeyRunning()) {
			return;
		}
		if (isEraseSdCard) {
			Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
			intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
			context.startService(intent);
		} else {
			context.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
			// Intent handling is asynchronous -- assume it will happen soon.
		}
	}

	public static void rebootSystem(Context context) {
		// McuMethodManager.getInstance(context).sendSysRestartKeyCMD();
		IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
		try {
			power.reboot(false, "", false);
		} catch (RemoteException e) {
			sLog.e("RemoteException when RebootSystem: ");
		}
	}

	public static void onSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = ApplicationManage.getContext().getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
			sLog.i("getHomes = " + ri.activityInfo.packageName);
		}
		return names;
	}

	public static void killProcess(String packageName) {
		if (null == packageName) {
			return;
		}

		final ActivityManager am = (ActivityManager) ApplicationManage.getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		if (am != null) {
			am.forceStopPackage(packageName);
		}
	}

	public static void showToast(int resid) {
		Context context = ApplicationManage.getContext();
		TextView view = new TextView(context);
		view.setWidth(400);
		view.setHeight(100);
		view.setText(resid);
		view.setTextSize(36f);
		view.setGravity(Gravity.CENTER);
		view.setBackgroundColor(context.getResources().getColor(R.color.lucencyblack));

		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(view);
		toast.show();
	}

	public static int getusb_status() {
		String status = null;
		Object localOb;
		try {
			localOb = new FileReader("/sys/ext_attr/usb_status");
			status = (new BufferedReader((Reader) localOb).readLine());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return status.equalsIgnoreCase("usbstate:1") ? 1 : 0;
	}

}
