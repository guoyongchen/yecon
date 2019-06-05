package com.carocean.t19can;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * ClassName:CanInfo
 * @function:can数据Debug输出
 */
public class CanInfo {

	public static String CAN_RX = "CAN_RX";
	public static String CAN_TX = "CAN_TX";
	public static boolean isDebug = true;

	public static void Rx(byte[] byPacket) {
		if (isDebug) {
			Log.i(CAN_RX, ":" + DataConvert.Bytes2Str(byPacket));
		}
	}

	public static void RxEx(byte[] byPacket, int ilen) {
		if (isDebug) {
			Log.i(CAN_RX, ":" + DataConvert.Bytes2StrEx(byPacket, ilen));
		}
	}

	public static void Tx(byte[] byPacket) {
		if (isDebug) {
			Log.i(CAN_TX, ":" + DataConvert.Bytes2Str(byPacket));
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			Log.e(tag, msg + "");
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			Log.i(tag, msg + "");
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug) {
			Log.w(tag, msg + "");
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug) {
			Log.d(tag, msg + "");
		}
	}

	public static void SWind(Context context, String string) {
		Toast toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
		toast.show();
	}
}
