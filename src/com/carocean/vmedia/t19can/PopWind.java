package com.carocean.vmedia.t19can;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * ClassName:PopWind
 * 
 * @function:弹窗
 * @author Kim
 * @Date: 2016-10-12下午5:32:26
 * @Copyright: Copyright (c) 2016
 * @version 1.0
 */
@SuppressLint("NewApi")
public class PopWind {

	private int mWidth = 0;
	private int mHeight = 0;
	private boolean mbshow = false;
	private WindowManager mWindowManager = null;
	private byte[] mObject = new byte[1];

	public PopWind(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	/**
	 * 
	 * @Title: showPopWind
	 * @Description: TODO
	 * @param context
	 * @return: void
	 */
	public long show(final Context context, final View view) {
		if (mbshow == true) {
			return System.currentTimeMillis();
		}
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		final WindowManager.LayoutParams lparams = new WindowManager.LayoutParams();
		lparams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
		// 设置flag
		int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				// | WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		// 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
		lparams.flags = flags;
		// 不设置这个弹出框的透明遮罩显示为黑色
		// lparams.format = PixelFormat.TRANSLUCENT;
		// FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
		// 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
		// 不设置这个flag的话，home页的划屏会有问题
		DisplayMetrics dMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(dMetrics);
		lparams.width = mWidth;
		lparams.height = mHeight;
		lparams.gravity = Gravity.CENTER;
		synchronized (mObject) {
			if (!mbshow && view != null) {
				mbshow = true;
				mWindowManager.addView(view, lparams);
			}
		}
		return System.currentTimeMillis();
	}

	/**
	 * 
	 * @Title: showEx
	 * @Description: TODO
	 * @param context
	 * @param view
	 * @return: void
	 */
	public long showEx(final Context context, final View view) {
		if (mbshow == true) {
			return System.currentTimeMillis();
		}
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		final WindowManager.LayoutParams lparams = new WindowManager.LayoutParams();
		lparams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;// 在这里设置等级为系统警告

		int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

		lparams.flags = flags;
		lparams.format = PixelFormat.TRANSLUCENT;
		DisplayMetrics dMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(dMetrics);
		lparams.width = dMetrics.widthPixels - mWidth;
		;
		lparams.height = dMetrics.heightPixels - mHeight;
		lparams.gravity = Gravity.CENTER;
		synchronized (mObject) {
			if (!mbshow && view != null) {
				mbshow = true;
				mWindowManager.addView(view, lparams);
			}
		}
		return System.currentTimeMillis();
	}

	/**
	 * 
	 * @Title: hide
	 * @Description: TODO
	 * @return: void
	 */
	public void hide(View view) {
		synchronized (mObject) {
			if (null != view && mbshow) {
				mbshow = false;
				mWindowManager.removeView(view);
			}
		}
	}

	/**
	 * 
	 * @Title: IsVisable
	 * @Description: TODO
	 * @return
	 * @return: boolean
	 */
	public boolean IsVisiable() {
		return mbshow;
	}
}
