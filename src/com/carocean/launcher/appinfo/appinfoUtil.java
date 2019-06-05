package com.carocean.launcher.appinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.carocean.ApplicationManage;
import com.carocean.utils.sLog;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Looper;

public class appinfoUtil {
	private static final String TAG = "appinfo";

	public static final int FILTER_ALL_APP = 0; // 所有应用程序
	public static final int FILTER_SYSTEM_APP = 1; // 系统程序
	public static final int FILTER_THIRD_APP = 2; // 第三方应用程序
	public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

	PackageInstallObserver observer = new PackageInstallObserver();
	public static appinfoUtil mInstance;
	private boolean isInstalling = false;
	private List<File> fileList = new ArrayList<File>();

	public static appinfoUtil getInstance() {
		if (null == mInstance) {
			mInstance = new appinfoUtil();
		}
		return mInstance;
	}

	public boolean autoInstallAPK(File file) {
		String fileName = file.getAbsolutePath();
		ApplicationInfo appInfo = null;
		String packageName = "xx.xx";
		PackageManager pm = ApplicationManage.getContext().getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(fileName, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			appInfo = info.applicationInfo;
			packageName = appInfo.packageName; // 得到安装包名称
		}
		sLog.v(TAG, "----filename = " + fileName + ",----packageName = " + packageName);
		if (appInfo == null) {
			doInstallOneApkError();
			return false;
		}
		return instatllBatch(appInfo, file, packageName);
	}

	public boolean instatllBatch(ApplicationInfo mAppInfo, File file, String packageName) {
		sLog.i(TAG, "path = " + file.getAbsolutePath());
		int installFlags = 0;
		PackageManager pm = ApplicationManage.getContext().getPackageManager();
		try {
			if (mAppInfo.packageName == null) {
				doInstallOneApkError();
				return false;
			}
			PackageInfo pi = pm.getPackageInfo(mAppInfo.packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
		} catch (NameNotFoundException e) {

		}
		if ((installFlags & PackageManager.INSTALL_REPLACE_EXISTING) != 0) {
			sLog.w(TAG, "Replacing package:" + mAppInfo.packageName);
		}

		Uri mPackageURI = Uri.fromFile(file);

		String installerPackageName = "";// getIntent().getStringExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME);

		pm.installPackage(mPackageURI, observer, installFlags, installerPackageName);
		return true;
	}

	private class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			sLog.i(TAG, "----INSTALL_COMPLETE----,returnCode = " + returnCode + ",----packageName =" + packageName);
			isInstalling = false;

		}
	}

	private void doInstallOneApkError() {
		isInstalling = false;
	}

	public void installAPK() {
		fileList.clear();
		File file1 = new File("/system/data/MiuDrive_for_car_signed.apk");
		fileList.add(file1);
		File file2 = new File("/system/data/MiuDriveServer_for_car_signed.apk");
		fileList.add(file2);

		// fileList.remove(file1);

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				long time = System.currentTimeMillis();
				for (int i = 0; i < fileList.size(); i++) {
					isInstalling = true;
					appinfoUtil.getInstance().autoInstallAPK(fileList.get(i));

					while (isInstalling) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {

						}
					}
				}
				long countTime = System.currentTimeMillis() - time;
				sLog.i(TAG, "..................countTime:" + countTime);
				Looper.loop();
			}
		}.start();
	}

}
