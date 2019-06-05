package com.carocean.settings.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.launcher.utils.Weather;
import com.carocean.service.BootService;
import com.carocean.t19can.T19CanTx;
import com.carocean.utils.DataShared;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.mcu.McuManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;

public class timeUtils {

	private static McuManager mMcuManager;
	private static final String HOURS_12 = "12";
	private static final String HOURS_24 = "24";

	public static String getHourMinute(boolean is24Hour) {
		String time = "";
		Calendar c = Calendar.getInstance();
		int hour = 0;
		hour = c.get(Calendar.HOUR_OF_DAY);
		if (is24Hour) {
			time += hour < 10 ? "0" + hour : hour;
		} else {
			if (hour == 0)
				hour = 12;
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

	// public static String[] getHourMinute(Context context) {
	// String[] res = new String[2];
	// String time = "";
	// Calendar c = Calendar.getInstance();
	// int hour = 0;
	// hour = c.get(Calendar.HOUR_OF_DAY);
	// if (is24HourFormat(context)) {
	// time += hour < 10 ? "0" + hour : hour;
	// } else {
	// int tens = hour > 12 ? (hour - 12) / 10 : hour / 10;
	// int single = hour > 12 ? (hour - 12) % 10 : hour % 10;
	// time = String.valueOf(tens) + String.valueOf(single);
	// }
	// res[0] = time;
	// int minute = c.get(Calendar.MINUTE);
	// if (minute < 10) {
	// time = "0" + minute;
	// } else
	// time = "" + minute;
	// res[1] = time;
	// return res;
	// }

	public static String[] getHourMinute(Context context) {
		String am = " " + context.getResources().getString(R.string.setting_time_am);
		String pm = " " + context.getResources().getString(R.string.setting_time_pm);
		String[] res = new String[3];
		String time = "";
		Calendar c = Calendar.getInstance();
		int hour = 0;
		String tag = "";
		hour = c.get(Calendar.HOUR_OF_DAY);
		if (is24HourFormat(context)) {
			time += hour < 10 ? "0" + hour : hour;
			tag = "";
		} else {
			tag = hour > 11 ? pm : am;
			if (hour == 0)
				hour = 12;
			int tens = hour > 12 ? (hour - 12) / 10 : hour / 10;
			int single = hour > 12 ? (hour - 12) % 10 : hour % 10;
			time = (tens > 0 ? String.valueOf(tens) : "") + String.valueOf(single);
		}
		res[0] = time;
		int minute = c.get(Calendar.MINUTE);
		if (minute < 10) {
			time = "0" + minute;
		} else
			time = "" + minute;
		res[1] = time;
		res[2] = tag;
		return res;
	}

	public static void setTime(Context context, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}

	public static String getCurrentWeek(Context context) {
		final Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		String[] weekdays = context.getResources().getStringArray(R.array.weekday_values);
		return weekdays[week - 1];
	}

	public static String getCurrentWeek(Context context, String[] weekdays) {
		final Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		return weekdays[week - 1];
	}

	public static void timeUpdated(Context context) {
		Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
		context.sendBroadcast(timeChanged);
	}

	public static void set24Hour(Context context, boolean is24Hour) {
		Settings.System.putString(context.getContentResolver(), Settings.System.TIME_12_24,
				is24Hour ? HOURS_24 : HOURS_12);
		timeUpdated(context);
	}

	public static boolean is24HourFormat(Context context) {
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat != null && strTimeFormat.equals(HOURS_24))
			return true;
		else
			return false;
	}

	public static String getDateFormat(Context context) {
		String strDateFormat = Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT);
		if (TextUtils.isEmpty(strDateFormat)) {
			strDateFormat = "yyyy-MM-dd";
			setDateFormat(context, strDateFormat);
		}
		return strDateFormat;
	}

	public static void setDateFormat(Context context, String format) {
		Settings.System.putString(context.getContentResolver(), Settings.System.DATE_FORMAT, format);
		timeUpdated(context);
	}

	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDate(Context context) {
		String strDateFormat = getDateFormat(context);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(strDateFormat);
		return dateFormatter.format(Calendar.getInstance().getTime());
	}

	public static void setDate(Context context, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}

	public static void setDateTime(Context context, int year, int month, int day, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		long when = c.getTimeInMillis();

		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}

		int times = 0;
		while (times < 3) {
			sendTimeData();
			times++;
			SystemClock.sleep(300);
		}

	}

	public static String getCurrentTime(Context context) {
		String strTimes[] = getHourMinute(context);
		StringBuffer strDatetime = new StringBuffer();
		strDatetime.append(strTimes[0]);
		strDatetime.append(":");
		strDatetime.append(strTimes[1]);
		return strDatetime.toString();
	}

	public static String getCurrentTimeTips(Context context) {
		return getHourMinute(context)[2];
	}

	public static Weather parseWeatherJson(String jsonString) {
		Weather weather = null;
		try {
			JSONObject jsonObj = new JSONObject(jsonString);
			int code = Integer.valueOf(jsonObj.getString("code"));
			String low = jsonObj.getString("low");
			String high = jsonObj.getString("high");
			String unit = jsonObj.getString("temp_unit");
			String cityname = jsonObj.getString("city");
			String conditions = jsonObj.getString("conditions");
			weather = new Weather();
			weather.condition = conditions;
			weather.code = code;
			weather.highTemp = high;
			weather.lowTemp = low;
			weather.unit = unit;
			weather.cityname = cityname;
		} catch (JSONException e) {
		} catch (NullPointerException e) {
		}
		return weather;
	}

	public static boolean getAutoState(Context context, String name) {
		try {
			return Settings.Global.getInt(context.getContentResolver(), name) > 0;
		} catch (SettingNotFoundException snfe) {
			return false;
		}
	}

	// GPS time
	public static final int update_gps_time = 15000;
	public static LocationManager mLocationManager = null;

	public static void setDateTime(Context context, Location mLication) {
		if (null == mLication)
			return;
		Calendar c = new GregorianCalendar(TimeZone.getDefault());
		c.setTimeInMillis(mLication.getTime());

		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
		sendTimeData();
	}

	public static LocationListener mLocationListener = new LocationListener() {
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		public void onProviderEnabled(String provider) {
			setDateTime(ApplicationManage.getContext(), mLocationManager.getLastKnownLocation(provider));
		}

		public void onProviderDisabled(String provider) {

		}

		public void onLocationChanged(Location location) {
			location = getBestLocation(mLocationManager);
			setDateTime(ApplicationManage.getContext(), location);

		}
	};

	public static LocationManager getLocationManager(Context context) {
		if (mLocationManager == null)
			mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return mLocationManager;
	}

	public static void initLocation(final Context context) {

		mLocationManager = getLocationManager(context);
		Location mLocation = getBestLocation(mLocationManager);
		setDateTime(context, mLocation);

		if (getAutoState(context, Settings.Global.AUTO_TIME)) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, update_gps_time, 0,
					mLocationListener);
		}

		setLocationListener(new onLocationListener() {

			@Override
			public void LocationListener(boolean bOpen) {
				// TODO Auto-generated method stub
				if (bOpen) {
					if (mLocationManager != null && mLocationListener != null)
						mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, update_gps_time, 0,
								mLocationListener);
				} else {
					if (mLocationManager != null && mLocationListener != null)
						mLocationManager.removeUpdates(mLocationListener);
				}
			}
		});
	}

	private static Location getBestLocation(LocationManager locationManager) {
		Location result = null;
		if (locationManager != null) {
			result = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (result != null) {
				DataShared.getInstance(ApplicationManage.getContext()).putString(SettingConstants.key_gps_longitude,
						result.getLongitude() + "");
				DataShared.getInstance(ApplicationManage.getContext()).commit();
				DataShared.getInstance(ApplicationManage.getContext()).putString(SettingConstants.key_gps_altitude,
						result.getAltitude() + "");
				DataShared.getInstance(ApplicationManage.getContext()).commit();
				Log.e("timeUtils",
						"getLongitude: "
								+ DataShared.getInstance(ApplicationManage.getContext())
										.getString(SettingConstants.key_gps_longitude, null)
								+ "  getAltitude: " + DataShared.getInstance(ApplicationManage.getContext())
										.getString(SettingConstants.key_gps_altitude, null));
				return result;
			} else {
				result = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				return result;
			}
		}
		return result;
	}

	public interface onLocationListener {
		public void LocationListener(boolean bOpen);
	}

	public static onLocationListener mLocaltionListener;

	public static void setLocationListener(onLocationListener listener) {
		mLocaltionListener = listener;
	}

	public static String byteToString(byte[] data) {
		StringBuffer str = new StringBuffer();
		for (byte d : data) {
			str.append(String.format("%02X ", d));
		}
		return str.toString();
	}

	// 发送GPS时间
	public static void sendTimeData() {
		int year, month, day, hour, minute, second;
		Calendar c = Calendar.getInstance();

		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		second = c.get(Calendar.SECOND);

		Log.i("setDate", "sendTimeDashboard - year: " + year + " - month: " + month + " - day: " + day + " - hour: "
				+ hour + " - minute: " + minute + " - second: " + second);

		T19CanTx.getInstance().sendGpsTimeData(year - 2000, month, day, hour, minute, second, 0);// 发送GPS

		// 发送给仪表时间
		byte[] data = new byte[7];
		data[0] = (byte) 0x12;
		data[1] = (byte) hour;
		data[2] = (byte) minute;
		data[3] = (byte) 0;
		data[4] = (byte) 0;
		data[5] = (byte) 0;
		data[6] = (byte) 0;

		if (mMcuManager == null) {
			mMcuManager = (McuManager) ApplicationManage.getContext().getSystemService(Context.MCU_SERVICE);
		}
		try {
			Log.i("setDate",
					"sendDashboardTimeData ---- Send::length = " + data.length + " data = " + byteToString(data));
			mMcuManager.RPC_SendCANInfo(data, data.length);
			BootService.sendTimeMcu(mMcuManager);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
