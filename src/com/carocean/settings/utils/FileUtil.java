/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.carocean.settings.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.autochips.storage.EnvironmentATC;
import com.carocean.ApplicationManage;
import com.carocean.utils.sLog;
import com.yecon.common.YeconEnv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class FileUtil {
	private static final String TAG = "FileUtil";

	public static final String MOUNTED_ROOT_PATH = "/mnt/";
	public final static String INT_SDCARD_PATH = YeconEnv.INT_SDCARD_PATH;
	public final static String EXT_SDCARD1_PATH = YeconEnv.EXT_SDCARD1_PATH;
	public final static String EXT_SDCARD2_PATH = YeconEnv.EXT_SDCARD2_PATH;
	public final static String UDISK1_PATH = YeconEnv.UDISK1_PATH;
	public final static String UDISK2_PATH = YeconEnv.UDISK2_PATH;
	public final static String UDISK3_PATH = YeconEnv.UDISK3_PATH;
	public final static String UDISK4_PATH = YeconEnv.UDISK4_PATH;
	public final static String UDISK5_PATH = YeconEnv.UDISK5_PATH;
	public final static String USR1_PATH = "/mnt/usr1";
	public final static String USR2_PATH = "/mnt/usr2";

	public final static String UPGRADE_SINGAL_FILE = "k1copy.img";
	public final static String UPGRADE_SYSTEM_SINGAL_FILE = "yc8317.img";
	public final static String MEDIA_SUFFIX[] = new String[] { ".mp3", ".mp4", ".aac", ".wma", ".wmv", ".avi", ".flash",
			".txt", ".mid", ".mov", ".wav", ".jpeg", ".png", ".gif", ".bmp", ".jpg" };

	public static final String ROOT_PATH = "/";

	public static final String SDCARD_PATH = ROOT_PATH + "sdcard";

	public static final String[] images = { "image/*" };

	public static final String imageMime = "image/";

	public static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

	private static final String LOG_TAG = "FileUtil";

	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	// if path1 contains path2
	public static boolean containsPath(String path1, String path2) {
		String path = path2;
		while (path != null) {
			if (path.equalsIgnoreCase(path1))
				return true;

			if (path.equals(FileUtil.ROOT_PATH))
				break;
			path = new File(path).getParent();
		}

		return false;
	}

	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}

	public static String getExtFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1 && dotPosition != 0) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	public static String getNameFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1 && dotPosition != 0) {
			return filename.substring(0, dotPosition);
		}
		return filename;
	}

	public static String getPathFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	public static String getNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	/**
	 * @param src
	 * @param dest
	 * @return new file path if successful, or return null
	 */
	public static String copyFile(String src, String dest) {
		File file = new File(src);
		if (!file.exists() || file.isDirectory()) {
			sLog.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
			return null;
		}
		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			fi = new FileInputStream(file);
			File destPlace = new File(dest);
			if (!destPlace.exists()) {
				if (!destPlace.mkdirs())
					return null;
			}

			String destPath = FileUtil.makePath(dest, file.getName());
			File destFile = new File(destPath);
			int i = 1;
			while (destFile.exists()) {
				String destName = FileUtil.getNameFromFilename(file.getName()) + " " + i++ + "."
						+ FileUtil.getExtFromFilename(file.getName());
				destPath = FileUtil.makePath(dest, destName);
				destFile = new File(destPath);
			}

			if (!destFile.createNewFile())
				return null;

			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}

			// TODO: set access privilege
			return destPath;
		} catch (FileNotFoundException e) {
			sLog.e(LOG_TAG, "copyFile: file not found, " + src);
			e.printStackTrace();
		} catch (IOException e) {
			sLog.e(LOG_TAG, "copyFile: " + e.toString());
		} finally {
			try {
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean setText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	public static boolean setText(View view, int id, int text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	// comma separated number
	@SuppressLint("DefaultLocale")
	public static String convertNumber(long number) {
		return String.format("%,d", number);
	}

	// storage, G M K B
	@SuppressLint("DefaultLocale")
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

	public static class CardInfo {
		public long total;

		public long free;
	}

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
			sLog.e(LOG_TAG, e.toString());
		}

		return null;
	}

	public static String formatDateString(Context context, long time) {
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
		Date date = new Date(time);
		return dateFormat.format(date) + " " + timeFormat.format(date);
	}

	public static void writeData(String fullFileName, String value) {
		try {
			FileOutputStream output = new FileOutputStream(fullFileName);
			String strXML = value;
			output.write(strXML.getBytes());
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isFileExist(String path) {
		boolean bisExist = false;
		File file = new File(path);
		if (file.exists()) {
			bisExist = true;
		}
		return bisExist;
	}

	public static boolean isSignalFileExist(String strFile) {
		boolean bRet = false;
		String filePath = getSignalFilePath(strFile);
		if (!TextUtils.isEmpty(filePath)) {
			bRet = true;
		}
		return bRet;
	}

	public static File[] getUSBMountedPoints() {
		File file = new File(MOUNTED_ROOT_PATH);
		File[] result = null;

		if (file.exists() && file.canRead() && file.isDirectory()) {
			result = file.listFiles();
		} else {
			sLog.e(TAG, "--------------------fail to access /storage/usb-otg/------------------");
		}
		return result;
	}

	public static String getSignalFilePath(String strFile) {
		boolean hasFile = false;
		File file = null;
		StringBuilder filePath = new StringBuilder();
		List<String> usbPathList = new ArrayList<String>();
		File[] paths = getUSBMountedPoints();
		if (paths != null) {
			int size = paths.length;
			for (int i = 0; i < size; ++i) {
				String absPath = paths[i].getAbsolutePath();
				if (absPath.equalsIgnoreCase(INT_SDCARD_PATH))
					continue;
				usbPathList.add(absPath);
				sLog.d(TAG, "init usb path is " + absPath);
			}
		}

		if (!hasFile) {
			for (int i = 0; i < usbPathList.size(); i++) {
				filePath.setLength(0);
				filePath.append(usbPathList.get(i));
				filePath.append("/");
				file = new File(filePath + strFile);
				if (!file.exists()) {
					hasFile = false;
				} else {
					hasFile = true;
					break;
				}
			}
		}

		if (hasFile) {
			return filePath.toString();
		} else {
			return "";
		}
	}

	static public String getDeviceMountedPath() {
		Context context = ApplicationManage.getContext();
		EnvironmentATC mEnv = new EnvironmentATC(context);
		StringBuilder xmlfileDir = new StringBuilder();

		String strPath[] = { UDISK1_PATH, UDISK2_PATH, UDISK3_PATH, UDISK4_PATH, UDISK5_PATH, EXT_SDCARD1_PATH,
				EXT_SDCARD2_PATH };

		for (int i = 0; i < strPath.length; i++) {
			if (mEnv.getStorageState(strPath[i]).equals(Environment.MEDIA_MOUNTED)) {
				xmlfileDir.append(strPath[i]);
				break;
			}
		}

		return xmlfileDir.toString();
	}

}
