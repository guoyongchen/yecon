package com.carocean.floatwindow;

import com.carocean.ApplicationManage;
import com.carocean.settings.utils.ScreenSpec;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
/**
 * @ClassName: fUtils
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class fUtils {

	public final static String ACTIVITY_CHANGE = "android.activity.action.STATE_CHANGED";
	public final static String ACTIVITY_FG = "foreground";
	public final static String ACTIVITY_BG = "background";
	public final static String ACTION_CLOSE_FLOATWINDOW = "com.yecon.action.closeFloatWindow";
	public final static String ACTION_TOUCH_CALIBRATION = "com.yecon.action.ACTION_TOUCH_CALIBRATION";

	public static FLOAT_WINDOW_TYPE floatwindowType = FLOAT_WINDOW_TYPE.FLOAT_MEDIA;
	public final static String MAP_PACKAGE_NAME_DEFAULT = "com.autonavi.amapauto";

	public static enum FLOAT_WINDOW_TYPE {
		FLOAT_MEDIA, FLOAT_BT, FLOAT_RADIO,
	}

	public static FLOAT_WINDOW_TYPE getFloatWindowType() {
		return floatwindowType;
	}

	public static void setFloatWindowType(FLOAT_WINDOW_TYPE _floatType) {
		floatwindowType = _floatType;
	}

	// YeconSettings.sendCloseSystemWindows(YeconSettings.SYSTEM_DIALOG_REASON_HOME_KEY);
	private static Bitmap bmpWallpaper(Context context, int x, int y, int width, int height) {
		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		final Drawable drawable = wallpaperManager.getDrawable();
		Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = Bitmap.createBitmap(bm, x, y, width, height);
		return bitmap;
	}

	public static void setBackground(View view) {
		Context context = ApplicationManage.getContext();
		int x = 0;
		int y = ScreenSpec.getStatusBarHeight(context);
		int w = ScreenSpec.mScreenWidth;
		int h = 254;
		Bitmap bitmap = bmpWallpaper(context, x, y, w, h);
		Drawable drawable = new BitmapDrawable(bitmap);
		view.setBackground(drawable);
	}
}
