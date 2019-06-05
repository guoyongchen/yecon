package com.carocean.utils;

import java.io.IOException;

import android.util.Log;

public class sLog {
	public static String TAG = "carocean.launcher";
	public static boolean isDebug = true;

	public static void i(String msg) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.i(TAG, msg);
	}

	public static void d(String msg) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.d(TAG, msg);
	}

	public static void e(String msg) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.e(TAG, msg);
	}

	public static void e(String msg, IOException e) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.e(TAG, msg, e);
	}

	public static void v(String msg) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.v(TAG, msg);
	}

	public static void w(String msg) {
		if (msg == null) {
			return;
		}

		if (isDebug)
			Log.w(TAG, msg);
	}

	public static void i(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}

		if (isDebug)
			Log.i(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}

		if (isDebug)
			Log.d(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}

		if (isDebug)
			Log.e(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}

		if (isDebug)
			Log.v(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}

		if (isDebug)
			Log.w(tag, msg);
	}

	public static void i(String tag, String msg, boolean debug) {
		if (tag == null || msg == null || !debug) {
			return;
		}

		if (isDebug)
			Log.i(tag, msg);
	}

	public static void d(String tag, String msg, boolean debug) {
		if (tag == null || msg == null || !debug) {
			return;
		}

		if (isDebug)
			Log.d(tag, msg);
	}

	public static void e(String tag, String msg, boolean debug) {
		if (tag == null || msg == null || !debug) {
			return;
		}

		if (isDebug)
			Log.e(tag, msg);
	}

	public static void v(String tag, String msg, boolean debug) {
		if (tag == null || msg == null || !debug) {
			return;
		}

		if (isDebug)
			Log.v(tag, msg);
	}

	public static void w(String tag, String msg, boolean debug) {
		if (tag == null || msg == null || !debug) {
			return;
		}

		if (isDebug)
			Log.w(tag, msg);
	}
}
