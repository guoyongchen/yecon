package com.carocean.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Instrumentation;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.floatwindow.FloatWindowService;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.scan.MediaScanService;
import com.carocean.service.BootService;
import com.carocean.settings.screensaver.ScreenSaverService;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.settings.utils.SettingMethodManager;
import com.carocean.settings.utils.timeUtils;
import com.carocean.vsettings.wallpaper.PageWallPaper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.yecon.metazone.YeconMetazone;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.kuwo.autosdk.api.KWAPI;

public class Utils {
	public static final String TAG = "Launcher";
	private static final int MAX_WIDTH = 1280; // 图片高度
	private static final int MAX_HEIGHT = 720; // 图片宽度
	private static final int MAX_COUNT = 12; // 最大缓存: 每一行4张,每页两行,缓存一行
	private static final int PERPIX_SIZE = 4; // 每个像素大小
	private static final int ELEM_SIZE = MAX_WIDTH * MAX_HEIGHT * PERPIX_SIZE; // 单个图片最大大小
	private static KWAPI mKwapi = null;

	public static int LowVelocity[] = { 80, 95, 111, 126, 142, 157, 172, 188, 203, 219, 234 };
	public static int MiddleVelocity[] = { 70, 85, 100, 115, 130, 145, 160, 175, 190, 205, 220 };
	public static int HigtVelocity[] = { 60, 75, 89, 104, 118, 133, 148, 162, 177, 191, 206 };

	public static void RunApp(String packageName, String classname) {
		Context context = ApplicationManage.getContext();
		if (classname != null) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ComponentName cn = new ComponentName(packageName, classname);
			intent.setComponent(cn);
			context.startActivity(intent);
		} else {
			PackageInfo pi;
			try {
				pi = context.getPackageManager().getPackageInfo(packageName, 0);
				Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
				// resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				resolveIntent.setPackage(pi.packageName);
				PackageManager pManager = context.getPackageManager();
				List<ResolveInfo> apps = pManager.queryIntentActivities(resolveIntent, 0);

				ResolveInfo ri = apps.iterator().next();
				if (ri != null) {
					packageName = ri.activityInfo.packageName;
					String className = ri.activityInfo.name;

					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					ComponentName cn = new ComponentName(packageName, className);

					intent.setComponent(cn);
					context.startActivity(intent);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void TransKey(final int iKeyCode) {
		new Thread() {
			@Override
			public void run() {
				try {
					Instrumentation instrKey = new Instrumentation();
					instrKey.sendKeyDownUpSync(iKeyCode);
				} catch (Exception e) {
				}
				super.run();
			}
		}.start();
	}

	public static void showSystemUI(Context context, boolean show) {
		StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
		if (statusBarManager != null) {
			statusBarManager.showSystemUI(show ? 100 : 101);
		}
	}

	public static void setCurrSource(Context context, int source) {
		StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
		if (statusBarManager != null) {
			// statusBarManager.setCurrSource(source);
		}
	}

	public static void initAppAll() {
		Context context = ApplicationManage.getContext();
		SettingMethodManager.getInstance(context).initData();
		initMedia();
		startService();
		mKwapi = KWAPI.createKWAPI(context, "auto");
		timeUtils.initLocation(context);
		// if (!Constants.isDebug) {
		// CrashHandler crashHandler = CrashHandler.getInstance();
		// crashHandler.init(context.getApplicationContext());
		// }
		// if (BTUtils.mBluetooth == null) {
		// BTUtils.mBluetooth = Bluetooth.getInstance();
		// }

	}

	public static void startService() {
		startBTService();
		startCanBusService();
		// startScreenSaverService();
		// startFloatWindowService();
	}

	public static void startScreenSaverService() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent();
		intent.setClass(context, ScreenSaverService.class);
		intent.setAction("com.carocean.ScreensaverService");
		context.startService(intent);
	}

	public static void startBootService() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, BootService.class);
		intent.setAction("com.carocean.BootService");
		context.startService(intent);
	}

	public static void startBTService() {
		Context context = ApplicationManage.getContext();
		Intent serviceIntent = new Intent(context, BTService.class);
		context.startService(serviceIntent);
	}

	public static void startMediaScanService() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent(context, MediaScanService.class);
		intent.putExtra(MediaScanConstans.ACTION, MediaScanConstans.ACTION_SCAN_START);
		context.startService(intent);
	}

	public static void initMedia() {
		Utils.startMediaScanService();
		Context context = ApplicationManage.getContext();
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.error_image).showImageOnFail(R.drawable.error_image)
				.cacheInMemory(false).cacheOnDisc(false).bitmapConfig(Config.ARGB_8888)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context.getApplicationContext())
				.memoryCacheExtraOptions(MAX_WIDTH, MAX_HEIGHT) // max width,
																// max height
				.defaultDisplayImageOptions(defaultOptions).discCacheSize(MAX_COUNT * ELEM_SIZE)
				.discCacheFileCount(MAX_COUNT).build();
		ImageLoader.getInstance().init(config);
	}

	public static void startFloatWindowService() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent();
		intent.setClass(context, FloatWindowService.class);
		intent.setAction("com.carocean.FloatWindowService");
		context.startService(intent);
	}

	private static void startCanBusService() {
		Context context = ApplicationManage.getContext();
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName("com.carocean", "com.carocean.t19can.CanBusService");
		intent.setComponent(componentName);
		context.startService(intent);
	}

	public static boolean isAppTopRunning(String packageName) {
		Context context = ApplicationManage.getContext();
		List<RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getRunningTasks(1);
		if (runningTasks != null) {
			if (runningTasks.size() != 0) {
				RunningTaskInfo taskInfo = runningTasks.get(0);
				if (taskInfo != null) {
					if (taskInfo.topActivity != null) {
						if (taskInfo.topActivity.getPackageName().equals(packageName))
							return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isActivityTopRunning(String className) {
		Context context = ApplicationManage.getContext();
		List<RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getRunningTasks(1);
		if (runningTasks != null) {
			if (runningTasks.size() != 0) {
				RunningTaskInfo taskInfo = runningTasks.get(0);
				if (taskInfo != null) {
					if (taskInfo.topActivity != null) {
						if (taskInfo.topActivity.getClassName().equals(className))
							return true;
					}
				}
			}
		}
		return false;
	}

	public static void setStatusBarVisible(Activity context, boolean visible) {
		WindowManager.LayoutParams p = context.getWindow().getAttributes();
		if (visible) {
			p.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			context.getWindow().setAttributes(p);
			p.systemUiVisibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			context.getWindow().getDecorView().setSystemUiVisibility(p.systemUiVisibility);
		} else {
			p.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			context.getWindow().setAttributes(p);
			p.systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			context.getWindow().getDecorView().setSystemUiVisibility(p.systemUiVisibility);
		}
	}

	public static Bitmap customBMP(Context context) {
		int Metrics[] = getDisplayMetrics();
		int width = Metrics[0];
		int height = Metrics[1];
		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		final Drawable drawable = wallpaperManager.getDrawable();
		Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height);
		return bitmap;
	}

	static int[] getDisplayMetrics() {
		int Metrics[] = new int[2];
		Context context = ApplicationManage.getContext();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		DisplayMetrics metric = new DisplayMetrics();
		d.getMetrics(metric);
		Metrics[0] = metric.widthPixels;
		Metrics[1] = metric.heightPixels;
		return Metrics;
	}

	public static void setDefWallPaper() {
		Context context = ApplicationManage.getContext();
		int Metrics[] = getDisplayMetrics();
		int width = Metrics[0];
		int height = Metrics[1];
		// final Bitmap bitmap = customBMP(context);
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), PageWallPaper.ids[DataShared
				.getInstance(ApplicationManage.getContext()).getInt(SettingConstants.key_ui_wallpaper, 0)]);
		WallpaperManager manager = WallpaperManager.getInstance(context);
		try {
			manager.setBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getHourMinute(boolean is24Hour) {
		String time = "";
		Calendar c = Calendar.getInstance();
		int hour = 0;
		hour = c.get(Calendar.HOUR_OF_DAY);
		if (is24Hour) {
			time += hour < 10 ? "0" + hour : hour;
		} else {
			int tens = hour > 12 ? (hour - 12) / 10 : hour / 10;
			int single = hour > 12 ? (hour - 12) % 10 : hour % 10;
			time = String.valueOf(tens) + String.valueOf(single);
		}
		int minute = c.get(Calendar.MINUTE);
		if (minute < 10) {
			time += ":0" + minute;
		} else
			time += ":" + minute;
		return time;
	}

	public static String getCurrentWeek(Context context) {
		final Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		String[] weekdays = context.getResources().getStringArray(R.array.weekday);
		return weekdays[week - 1];
	}

	public static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date());
	}

	public static String[] getHourMinute(Context context) {
		String[] res = new String[2];
		String time = "";
		Calendar c = Calendar.getInstance();
		int hour = 0;
		hour = c.get(Calendar.HOUR_OF_DAY);
		if (is24HourFormat(context)) {
			time += hour < 10 ? "0" + hour : hour;
		} else {
			int tens = hour > 12 ? (hour - 12) / 10 : hour / 10;
			int single = hour > 12 ? (hour - 12) % 10 : hour % 10;
			time = String.valueOf(tens) + String.valueOf(single);
		}
		res[0] = time;
		int minute = c.get(Calendar.MINUTE);
		if (minute < 10) {
			time = "0" + minute;
		} else
			time = "" + minute;
		res[1] = time;
		return res;
	}

	/**
	 * 
	 * @param context
	 * @return 0: am, 1: pm -1: is24
	 */
	public static int getHourMinute(Context context, String[] ouput) {
		// String[] res = new String[2];
		int ret = -1;
		if (ouput == null || ouput.length < 2) {
			return -1;
		}
		String time = "";
		Calendar c = Calendar.getInstance();
		int hour = 0;
		hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour >= 12) {
			ret = 1;
		} else {
			ret = 0;
		}
		if (is24HourFormat(context)) {
			time += hour < 10 ? "0" + hour : hour;
			ret = -1;
		} else {
			int tens = hour > 12 ? (hour - 12) / 10 : hour / 10;
			int single = hour > 12 ? (hour - 12) % 10 : hour % 10;
			time = String.valueOf(tens) + String.valueOf(single);
		}
		ouput[0] = time;
		int minute = c.get(Calendar.MINUTE);
		if (minute < 10) {
			time = "0" + minute;
		} else
			time = "" + minute;
		ouput[1] = time;
		return ret;
	}

	public static boolean is24HourFormat(Context context) {
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat != null && strTimeFormat.equals("24"))
			return true;
		else
			return false;
	}

    /**
     * 增加亮度
     * @return
     */
	public static void incBrightness(Context context) {
	    int backlight = getBrightness();
	    if (backlight <= 201) {
            backlight += 30;
            if (backlight > 201) {
                backlight = 201;
            }
            setBrightness(context,backlight);
        }
    }

    /**
     * 减小亮度
     * @return
     */
    public static void decBrightness(Context context) {
        int backlight = getBrightness();
        if (backlight >= 1) {
            backlight -= 30;
            if (backlight < 0) {
                backlight = 1;
            }
            setBrightness(context,backlight);
        }
    }

	public static int getBrightness() {
		//int brightness = SystemProperties.getInt("persist.sys.yeconBacklight", 204);
        int brightness = SettingConstants.backlight;
		Log.i(TAG, "backlight---readBrightness="+brightness);
		return brightness;
	}

	public static void setBrightness(Context context, int brightness) {
		if (brightness > 0 && brightness <= 255) {
			Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
			String str_brightness = String.valueOf(brightness);
			//SystemProperties.set("persist.sys.yeconBacklight", str_brightness);//这个属性是保存默认值的，不是当前值，不需要去写
            SettingConstants.backlight = brightness;
			int read_brightness = getBrightness();
			float tmp_brightness = brightness * 100.0f / 255.0f;
			int write_metazone_brightness = (int) tmp_brightness;
			YeconMetazone.SetBacklightness(write_metazone_brightness);// 这个metazone是100最大
			Log.i(TAG, "backlight---setBrightness=" + brightness + ", write_metazone_brightness="
					+ write_metazone_brightness);
		}
	}

	private static Toast mToast;

	public static void showToast(String tips) {
		if (mToast == null) {
			mToast = new Toast(ApplicationManage.getContext());
			LayoutInflater inflate = (LayoutInflater) ApplicationManage.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflate.inflate(R.layout.media_toast_layout, null);
			TextView tv = (TextView) v.findViewById(R.id.media_toast_text);
			tv.setText(tips);
			mToast.setGravity(Gravity.CENTER, 0, 0);
			mToast.setView(v);
			mToast.setDuration(Toast.LENGTH_SHORT);
		} else if (mToast.getView() != null) {
			TextView textView = (TextView) mToast.getView().findViewById(R.id.media_toast_text);
			if (textView != null) {
				textView.setText(tips);
			}
		}
		mToast.show();
	}
	
	private static Toast mOtherToast;
	//重写一个弹窗框，固定最多2行，每行9个字
	public static void showOtherToast(String tips) {
		if (mOtherToast == null) {
			mOtherToast = new Toast(ApplicationManage.getContext());
			LayoutInflater inflate = (LayoutInflater) ApplicationManage.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflate.inflate(R.layout.other_toast_layout, null);
			TextView tv = (TextView) v.findViewById(R.id.other_toast_text);
			tv.setText(tips);
			mOtherToast.setGravity(Gravity.CENTER, 0, 0);
			mOtherToast.setView(v);
			mOtherToast.setDuration(Toast.LENGTH_LONG);
		} else if (mOtherToast.getView() != null) {
			TextView textView = (TextView) mOtherToast.getView().findViewById(R.id.other_toast_text);
			if (textView != null) {
				textView.setText(tips);
			}
		}
		mOtherToast.show();
	}

	// 获取sdcard总容量和可用容量
	public static CardInfo getCardInfo(String path) {

		try {
			android.os.StatFs statfs = new android.os.StatFs(path);

			// 获取SDCard上BLOCK总数
			long nTotalBlocks = statfs.getBlockCount();

			// 获取SDCard上每个block的SIZE
			long nBlocSize = statfs.getBlockSize();

			// 获取可供程序使用的Block的数量
			long nAvailaBlock = statfs.getAvailableBlocks();

			// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
			long nFreeBlock = statfs.getFreeBlocks();

			CardInfo info = new CardInfo();
			// 计算SDCard 总容量大小MB
			info.total = nTotalBlocks * nBlocSize;

			// 计算 SDCard 剩余大小MB
			info.free = nAvailaBlock * nBlocSize;

			return info;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
		}

		return null;
	}

	public static class CardInfo {
		public long total;

		public long free;
	}

	// storage, G M K B
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb << 10;
		long gb = mb << 10;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	public static KWAPI getKwapi() {
		if (mKwapi == null) {
			Context context = ApplicationManage.getContext();
			mKwapi = KWAPI.createKWAPI(context, "auto");
		}
		return mKwapi;
	}

	public static OnThemeChangeListener mThemeChangeListener;

	public static void setOnThemeChangeListener(OnThemeChangeListener itemListener) {
		mThemeChangeListener = itemListener;
	}

	public interface OnThemeChangeListener {
		void onItemClick(int theme);
	}
}
