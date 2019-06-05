package com.carocean.settings.utils;

import static android.constant.YeconConstants.BLUETOOTH_PACKAGE_NAME;
import static android.constant.YeconConstants.BLUETOOTH_START_ACTIVITY;

import com.carocean.ApplicationManage;
import com.carocean.launcher.appinfo.Fragment_FilterApp;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.settings.fragment.Fragment_TimeZone;
import com.carocean.settings.screensaver.FragmentScreenSaver;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class SettingUtils {

	public static void playSoundEffect(View view) {
		view.playSoundEffect(android.view.SoundEffectConstants.CLICK);
	}

	public static void onSet_BTSeting(FragmentManager fm) {
		launcherUtils.startBT();
	}

	public static void onSet_filterAPP(FragmentManager fm) {
		Fragment_FilterApp fragment = new Fragment_FilterApp();
		String strTag = "filter_appinfo";
		if (fm.findFragmentByTag(strTag) == null)
			fragment.show(fm, strTag);
	}

	public static Fragment_TimeZone onSet_timezone(FragmentManager fm, int mTimezoneIndex) {
		Fragment_TimeZone fragment = new Fragment_TimeZone(mTimezoneIndex);
		String strTag = "set_timezone";
		if (fm.findFragmentByTag(strTag) == null)
			fragment.show(fm, strTag);
		return fragment;
	}

	public static FragmentScreenSaver onSet_ScreenSaver(FragmentManager fm) {
		FragmentScreenSaver fragment = new FragmentScreenSaver();
		String strTag = "set_screensaver";
		if (fm.findFragmentByTag(strTag) == null)
			fragment.show(fm, strTag);
		return fragment;
	}

	public static void startBTSet() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ComponentName cn = new ComponentName(BLUETOOTH_PACKAGE_NAME, BLUETOOTH_START_ACTIVITY);
		intent.setComponent(cn);
		intent.putExtra("PageType", 4);
		context.startActivity(intent);
	}



}
