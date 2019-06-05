package com.carocean.settings.utils;

import java.lang.reflect.Field;

import android.content.Context;

public class ScreenSpec {
	
	public static int mScreenWidth = 768;
	public static int mScreenHeight = 1024;
	public static int mStatusBarHeight = 0;
	public static float mScreenDensity = 1.0F;
	
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;

		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return sbar;
	}
}
