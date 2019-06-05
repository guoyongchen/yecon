package com.carocean.vsettings.settings;

import java.io.IOException;

import com.carocean.ApplicationManage;
import com.carocean.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: Utils
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
@SuppressLint("NewApi")
public class Utils {
	@SuppressLint("NewApi")
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

	public static void setWallPaper(Context context, int resid) {
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resid);
		WallpaperManager manager = WallpaperManager.getInstance(context);
		try {
			manager.setBitmap(Bitmap.createScaledBitmap(bitmap, 768, 1024, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap customBMP(Context context, int x, int y, int width, int height) {
		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		final Drawable drawable = wallpaperManager.getDrawable();
		Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = Bitmap.createBitmap(bm, x, y, width, height);
		return bitmap;
	}

	public static Bitmap customDrawble(Context context, int w, int h) {
		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		final Drawable drawable = wallpaperManager.getDrawable();
		// final Bitmap bmp =
		// BitmapFactory.decodeResource(context.getResources(),
		// R.drawable.default_wallpaper);
		// int w = drawable.getIntrinsicWidth();
		// int h = drawable.getIntrinsicHeight();
		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(bitmap);
		Paint mPaint = new Paint();
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Rect srcRect = new Rect(0, 883, w, 883 + h);
		Rect dstRect = new Rect(0, 0, w, h);
		canvas.drawBitmap(bd.getBitmap(), srcRect, dstRect, mPaint);
		return bitmap;
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
		final Bitmap bitmap = customBMP(context);
		WallpaperManager manager = WallpaperManager.getInstance(context);
		try {
			manager.setBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setWallPaper(int rid) {
		Context context = ApplicationManage.getContext();
		int Metrics[] = getDisplayMetrics();
		int width = Metrics[0];
		int height = Metrics[1];
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), rid);
		WallpaperManager manager = WallpaperManager.getInstance(context);
		try {
			manager.setBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Toast mToast;
	public static void showToast(String tips) {
		if (mToast == null) {
			mToast = new Toast(ApplicationManage.getContext());
            LayoutInflater inflate = (LayoutInflater) ApplicationManage.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

}
