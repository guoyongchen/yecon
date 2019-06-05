package com.carocean.launcher.utils;

import com.carocean.ApplicationManage;
import com.carocean.utils.Utils;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vmedia.setting.PageSetting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;

public class launcherUtils {
    private final static String TAG="launcherUtils";
	public static int mTheme = 1;
	public static int mWallpaper = 0;
	public final static String START_SETTINGS_ACTION = "com.carocean.action.startSettings";
	public final static String START_THEME_ACTION = "com.carocean.action.startTheme";
	public final static String START_WALLPAPER_ACTION = "com.carocean.action.startWallpaper";
	public final static String START_SET_VOLUME_ACTION = "com.carocean.action.setVolume";
	public final static String BACK_PAGECOMMOM_ACTION = "com.carocean.action.backPageCommon";
	public final static String START_CARHELP_SETTIME_ACTION = "com.carocean.action.carhelp.settime";
	public final static String HOME_OR_BACK_SHOW_ACTION = "com.carocean.action.home_or_back_show";
	public final static String OPEN_WIFI_SETTING_ACTION = "com.carocean.action.open_wifi_setting";
	public final static String SHOW_BACKSTAGE_ACTION = "com.carocean.show.backstage";

	public static void startLauncher() {
		Utils.RunApp("com.carocean", "com.carocean.MainActivity");
        Log.i(TAG, "start launcher");
	}

	public static void startMedia() {
		// Utils.TransKey(KeyEvent.KEYCODE_YECON_MUSIC);
		// Utils.RunApp("com.carocean", "com.carocean.media.MediaActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "media");
		context.startActivity(intent);
	}

	public static void startMusic() {
		Utils.RunApp("com.carocean", "com.carocean.media.music.MusicHallActivity");
	}

	public static void startPhoto() {
		Utils.RunApp("com.carocean", "com.carocean.media.picture.PictureActivity");
	}

	public static void startRadio() {
		// Utils.RunApp("com.carocean", "com.carocean.radio.RadioActivity");
		// Intent intent = new Intent(Intent.ACTION_MAIN);
		// intent.setComponent(new ComponentName("com.carocean",
		// "com.carocean.vmedia.MainActivity"));
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "radio");
		context.startActivity(intent);
	}

	public static void startBT() {
		// Utils.RunApp("com.carocean", "com.carocean.bt.BTActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "bt");
		context.startActivity(intent);
	}

	public static void startAllapp() {
		// Utils.RunApp("com.carocean", "com.carocean.bt.BTActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "allapp");
		context.startActivity(intent);
	}

	public static void startCarInfo(String switchType) {
		// Utils.RunApp("com.carocean", "com.carocean.bt.BTActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "carinfo");
		intent.putExtra("switchType", switchType);
		context.startActivity(intent);
	}

	public static void startCAN() {
		Utils.RunApp("com.carocean", "com.carocean.can.CANActivity");
	}

	public static void startScreenLink() {
		Utils.RunApp("com.carocean", "com.carocean.screenlink.ScreenLinkActivity");
	}

	public static void startOperateIntro() {
		Utils.RunApp("com.carocean", "com.carocean.operateintro.OperateIntroActivity");
	}

	public static void startPageSettings() {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "setting");
		context.startActivity(intent);
	}

	// 指定到wifi界面
	public static void startWifi() {
		PageSetting.setPageId(10003);
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("mediaType", "setting");
		intent.putExtra("switchType", "iswifi");
		context.startActivity(intent);
	}

	public static void startSound() {
		PageSetting.setPageId(10001);
		startPageSettings();
	}

	public static void startBalance() {
		PageSetting.setPageId(10002);
		startPageSettings();
	}

	public static void startSettings() {
		PageSetting.setPageId(10003);
		startPageSettings();
	}

	public static void startCarHelpSetTime(boolean isShow) {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(START_CARHELP_SETTIME_ACTION);
		intent.putExtra("isShow", isShow);
		context.sendBroadcastAsUser(intent, UserHandle.ALL);

	}

	public static void startTheme() {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(START_THEME_ACTION);
		context.sendBroadcast(intent);

	}

	public static void startWallpaper() {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(START_WALLPAPER_ACTION);
		context.sendBroadcast(intent);

	}

	public static void startSetVolume() {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(START_SET_VOLUME_ACTION);
		context.sendBroadcast(intent);

	}

	public static void backPageCommon() {
		// Utils.RunApp("com.carocean", "com.carocean.vsettings.MainActivity");
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(BACK_PAGECOMMOM_ACTION);
		context.sendBroadcast(intent);

	}

	public static void startBackCar() {
		Utils.RunApp("com.carocean", "com.carocean.backcar.BackCarActivity");
	}

	public static void startAirCleaner() {
		Utils.RunApp("com.carocean", "com.carocean.aircleaner.AirCleanerActivity");
	}

	public static void startScreenSaver() {
		Utils.RunApp("com.carocean", "com.carocean.settings.screensaver.ScreenSaverActivity");
	}

	public static void startNavi() {
		// Utils.TransKey(KeyEvent.KEYCODE_YECON_NAVI);
		Utils.RunApp("com.autonavi.amapauto", "com.autonavi.auto.remote.fill.UsbFillActivity");
	}

	public static void startVoice() {
		Utils.TransKey(KeyEvent.KEYCODE_YECON_ISSSR);
	}

	public static void startCarSetting() {
		Utils.RunApp("com.yecon.carsetting", "com.yecon.carsetting.FragmentTabAcitivity");
	}

	// public static void startSetting {
	// Utils.RunApp("com.carocean",
	// "com.carocean.settings.screensaver.ScreenSaverActivity");
	// }

	public static void startkwmusic() {
		Utils.RunApp("cn.kuwo.kwmusiccar", "cn.kuwo.kwmusiccar.WelcomeActivity");
	}

	public static void startklauto() {
		Utils.RunApp("com.edog.car", "com.kaolafm.auto.home.HubActivity");
	}

	public static void startFileManager() {
		Utils.RunApp("com.yecon.filemanager", "com.yecon.filemanager.LauncherActivity");
	}

	public static void startCalendar() {
		Utils.RunApp("com.yecon.carsetting", "com.yecon.carsetting.Calendar");
	}

	public static void startIe() {
		Utils.RunApp("com.android.browser", "com.android.browser.BrowserActivity");
	}

	public static void startiflytek() {
		try {
			Context context = ApplicationManage.getContext();
			Intent intent = new Intent();
			intent.setComponent(
					new ComponentName("com.iflytek.cutefly.speechclient", "com.iflytek.autofly.SpeechClientService"));
			/* ?????????????????? */
			intent.putExtra("fromservice", "com.carocean");
			context.startService(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
