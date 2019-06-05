package com.carocean.settings.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.utils.sLog;
import com.yecon.metazone.YeconMetazone;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

/**
 * @ClassName: verUtils
 * @Description: version information
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class verUtils {
	final static String TAG = "verUtils";

	public static int getSoftwareVersion() {
		Context context = ApplicationManage.getContext();
		int localVersion = 0;
		try {
			PackageInfo packageInfo = context.getApplicationContext().getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			localVersion = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return localVersion;
	}

	public static String getSoftwareVersionName() {
		Context context = ApplicationManage.getContext();
		String localVersion = "";
		try {
			PackageInfo packageInfo = context.getApplicationContext().getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			localVersion = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return localVersion;
	}

	public static String getAppBuildTime() {
		Context context = ApplicationManage.getContext();
		String content = null;
		Resources resources = context.getResources();
		InputStream is = null;
		try {
			is = resources.openRawResource(R.raw.date);
			byte buffer[] = new byte[is.available()];
			is.read(buffer);
			content = new String(buffer);
			sLog.i("read:" + content);
		} catch (IOException e) {
			sLog.e("write file", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					sLog.e("close file", e);
				}
			}
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = format.parse(content.trim());
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
			content = sf.format(date);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			sLog.e(e.getMessage());
		}

		return content;
	}

	public static String getAppBuildString() {
		Context context = ApplicationManage.getContext();
		String content = null;
		Resources resources = context.getResources();
		InputStream is = null;
		try {
			is = resources.openRawResource(R.raw.date);
			byte buffer[] = new byte[is.available()];
			is.read(buffer);
			content = new String(buffer);
			sLog.i("read:" + content);
		} catch (IOException e) {
			sLog.e("write file", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					sLog.e("close file", e);
				}
			}
		}
		return content;
	}

	public static String getSystemBuildTime() {
		Date d = new Date(Build.TIME);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
		return sf.format(d);
	}

	public static String getnewSystemBuildTime() {
		Date d = new Date(Build.TIME);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		String newSystemBuildTime = null;
		if (sf.format(d) != null) {
			newSystemBuildTime = sf.format(d).replace("-", ".");
		}
		// return newSystemBuildTime;
		return "01.00.04";
	}

	public static String getHardwareVersion() {
		String localVersion = "0.0.2";
		return localVersion;
	}

	public static String getFirmwareVersion() {
		String version = Build.VERSION.RELEASE;
		if (version.contains("4.2.2")) {
			version = version.replaceAll("4.2.2", "4.4.2");
		}
		return version;
	}

	static String[] mCpuTypeArray = { "AC8217KBFI", "8227HBFI", "AC8317KNFI", "AC8327HNFI", "AC8327MXFI" };

	public static String getCpuType() {
		int mCpuType = YeconMetazone.GetCpuType();
		sLog.d(" YeconMetazone.GetCpuType()=" + mCpuType);
		if ((mCpuType >= 0xE1) && (mCpuType <= (0xE1 + mCpuTypeArray.length))) {
			return mCpuTypeArray[mCpuType - 0xE1];
		} else {
			Log.d("xuhh", "YeconMetazone.GetCpuType invalid");
			return Build.MODEL;
		}
	}

	public static StringBuilder mcuVersion = new StringBuilder();
	public static StringBuilder mcuID = new StringBuilder();

	public static String getmcuVersion() {
		// return mcuVersion.toString();
		Context context = ApplicationManage.getContext();
		String strVersion = SystemProperties.get(PersistUtils.PERSYS_MCU_VER);
		return !TextUtils.isEmpty(strVersion) ? strVersion
				: context.getResources().getString(R.string.setting_status_unavailable);
	}

	public static String getmcuID() {
		return mcuID.toString();
	}

	public static String getCANVersion() {
		return "0.0.1";
	}

	public static String getSerialNumber() {
		Context context = ApplicationManage.getContext();
		String serial = Build.SERIAL;
		return !TextUtils.isEmpty(serial) ? serial
				: context.getResources().getString(R.string.setting_status_unavailable);
	}

	public static String getBtVersion() {
		return "1.0.0";
	}

	private static boolean bFirstReardEmmc = true;
	private static String myMacAddress = null;

	public static String getEmmcId() {
		if (!bFirstReardEmmc) {
			return myMacAddress;
		}
		Object localOb;
		try {
			localOb = new FileReader("/sys/block/mmcblk0/device/type");
			if (localOb != null) {
				localOb = new BufferedReader((Reader) localOb).readLine().toLowerCase().contentEquals("mmc");
				if (localOb != null) {
					// nand ID
					localOb = new FileReader("/sys/block/mmcblk0/device/cid");
					myMacAddress = new BufferedReader((Reader) localOb).readLine();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (myMacAddress != null) {
			bFirstReardEmmc = false;
		}
		return myMacAddress;
	}

	public static String getUUID() {
		String strContent = getEmmcId();
		// strContent = strContent.substring(strContent.length() - 16,
		// strContent.length());
		return strContent;
	}

	public static String getBtAddr() {
		Context context = ApplicationManage.getContext();
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		String address = bluetooth.isEnabled() ? bluetooth.getAddress() : null;
		return !TextUtils.isEmpty(address) ? address
				: context.getResources().getString(R.string.setting_status_unavailable);
	}

	public static String getWifiMacAddr() {
		Context context = ApplicationManage.getContext();
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
		return !TextUtils.isEmpty(macAddress) ? macAddress
				: context.getResources().getString(R.string.setting_status_unavailable);
	}

	public static String getSystemVersion() {
		String cpuType = getCpuType();
		String version = "";
		if (cpuType.contains("8317")) {
			version = Build.DISPLAY.replaceAll("8317", "8317");
		} else if (cpuType.contains("8327")) {
			version = Build.DISPLAY.replaceAll("8317", "8327");
		} else if (cpuType.contains("8217")) {
			version = Build.DISPLAY.replaceAll("8317", "8217");
		} else if (cpuType.contains("8227")) {
			version = Build.DISPLAY.replaceAll("8317", "8227");
		}
		if (version.contains("4.2.2")) {
			version = version.replaceAll("4.2.2", "4.4.2");
		}
		return version;
	}

	// kernel version
	private static String readLine(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
		try {
			return reader.readLine();
		} finally {
			reader.close();
		}
	}

	public static String formatKernelVersion(String rawKernelVersion) {
		// Example (see tests for more):
		// Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
		// (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
		// Thu Jun 28 11:02:39 PDT 2012

		final String PROC_VERSION_REGEX = "Linux version (\\S+) "
				+ /*
					 * group 1: "3.0.31-g6fb96c9"
					 */
				"\\((\\S+?)\\) " + /* group 2: "x@y.com" (kernel builder) */
				"(?:\\(gcc.+? \\)) " + /* ignore: GCC version information */
				"(#\\d+) " + /* group 3: "#1" */
				"(?:.*?)?"
				+ /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
				"((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /*
														 * group 4:
														 * "Thu Jun 28 11:02:39 PDT 2012"
														 */

		Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
		if (!m.matches()) {
			// Log.e(LOG_TAG, "Regex did not match on /proc/version: " +
			// rawKernelVersion);
			return "Unavailable";
		} else if (m.groupCount() < 4) {
			// Log.e(LOG_TAG, "Regex match on /proc/version only returned " +
			// m.groupCount()
			// + " groups");
			return "Unavailable";
		}
		return m.group(1) + "\n" + // 3.0.31-g6fb96c9
				m.group(2) + " " + m.group(3) + // x@y.com #1
				" " + m.group(4); // Thu Jun 28 11:02:39 PDT 2012
	}

	private static final String FILENAME_PROC_VERSION = "/proc/version";

	public static String getFormattedKernelVersion() {
		try {
			return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

		} catch (IOException e) {
			// L.e("","IO Exception when getting kernel version for Device Info
			// screen",
			// e);

			return "Unavailable";
		}
	}

}
