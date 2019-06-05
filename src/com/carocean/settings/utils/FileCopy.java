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
import java.util.ArrayList;
import java.util.List;

import com.carocean.ApplicationManage;
import com.carocean.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

/**
 * @ClassName: FileOperator
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class FileCopy {

	private final String TAG = getClass().toString();
	public static FileCopy mInstance;
	private Handler mHandler;
	HandlerThread thread = null;
	private boolean bBackgroundRunnable = false;
	private ProgressDialog mProgressDlg = null;
	ProgressInfo pinfo = ProgressInfo.getInstance();
	private final static int MSG_REFRESH_PROGRESS = 100;
	private final static int MSG_NOT_WRITABLE = 101;
	private final static int MSG_CONTAINED = 102;
	private final static int MSG_ANDROID_SECURE = 103;
	private final static int TYPE_ERROR_PASTE_CONTAINED = 0;
	private final static int TYPE_ERROR_PASTE_ANDROIDSECURE = 1;
	private final static int TYPE_ERROR_PASTE_NOTWRITEABLE = 2;
	private final static String CRASH_FOLDER = "crash_launcher";

	public FileCopy() {
		// TODO Auto-generated constructor stub
		thread = new HandlerThread("CopyHandlerThread");
		thread.start();
		mHandler = new Handler(thread.getLooper());
	}

	public void OnDestroy() {
		mHandler.removeCallbacks(mBackgroundRunnable);
		if (thread != null) {
			thread.getLooper().quit();
			thread = null;
		}
	}

	public static FileCopy getInstance() {
		if (null == mInstance) {
			mInstance = new FileCopy();
		}
		return mInstance;
	}

	private void sendMSG(int what) {
		Message message = Message.obtain();
		message.what = what;
		myHandler.sendMessage(message);
	}

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_PROGRESS:
				mProgressDlg.setMessage(pinfo.name);
				mProgressDlg.setProgress(pinfo.current);
				break;
			case MSG_NOT_WRITABLE:
				showIllegalDialog(TYPE_ERROR_PASTE_NOTWRITEABLE);
				break;
			case MSG_CONTAINED:
				showIllegalDialog(TYPE_ERROR_PASTE_CONTAINED);
				break;
			case MSG_ANDROID_SECURE:
				showIllegalDialog(TYPE_ERROR_PASTE_CONTAINED);
				break;
			}
		}
	};

	private void showIllegalDialog(int type) {

		Context context = ApplicationManage.getContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_error_title).setCancelable(false)
				.setNeutralButton(R.string.activity_input_confirm, null);
		switch (type) {
		case TYPE_ERROR_PASTE_CONTAINED:
			builder.setMessage(R.string.dialog_paste_error_contained);
			break;
		case TYPE_ERROR_PASTE_ANDROIDSECURE:

			builder.setMessage(context.getString(R.string.dialog_paste_error_androidsecure));
			break;
		case TYPE_ERROR_PASTE_NOTWRITEABLE:
			builder.setMessage(context.getString(R.string.dialog_paste_error_notwritable));
			break;
		default:

		}
		builder.create().show();
	}

	private static final class ProgressInfo {
		String name;
		int current;
		static int max;

		private static ProgressInfo info;

		static ProgressInfo getInstance() {
			if (info == null) {
				info = new ProgressInfo();
			}
			return info;
		}
	}

	private ProgressDialog onProgressDialogBuild(Activity activity, int msgId) {
		Context context = ApplicationManage.getContext();
		Activity parent = activity;
		ProgressDialog pd;
		pd = new ProgressDialog(parent, ProgressDialog.STYLE_HORIZONTAL);
		pd.setTitle(parent.getString(msgId));
		pd.setIndeterminate(false);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setProgressNumberFormat(null);
		pd.setMessage(context.getResources().getString(R.string.dialog_caculating));
		return pd;
	}

	List<String> listDst = new ArrayList<String>();
	List<String> mCopyedInfos = new ArrayList<String>();
	long total = 0;
	long space = 0;

	public void copyCrashData(Activity activity) {

		total = 0;
		space = 0;

		if (listDst.size() > 0) {
			listDst.clear();
		}

		if (mCopyedInfos.size() > 0) {
			mCopyedInfos.clear();
		}

		String sFilePath = FileUtil.INT_SDCARD_PATH + File.separator + CRASH_FOLDER;
		String tFilePath = FileUtil.getDeviceMountedPath();

		if (FileUtil.isFileExist(sFilePath)) {
			mCopyedInfos.add(sFilePath);
			listDst.add(tFilePath);
		} else {
			tzUtils.showToast(R.string.no_found_error_log_file);
			return;
		}

		if (TextUtils.isEmpty(tFilePath)) {
			tzUtils.showToast(R.string.no_found_device_mounted);
			return;
		}

		if (mCopyedInfos.size() > 0) {
			FilePaster(activity);
		}
	}

	void FilePaster(Activity activity) {
		bBackgroundRunnable = true;
		if (mProgressDlg == null) {
			mProgressDlg = onProgressDialogBuild(activity, R.string.dialog_progress_paste);
			ProgressInfo.max = 10000;
			mProgressDlg.setMax(ProgressInfo.max);
		}
		if (!mProgressDlg.isShowing()) {
			mProgressDlg.show();
		}
		mHandler.post(mBackgroundRunnable);
	}

	Runnable mBackgroundRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (bBackgroundRunnable) {

				for (String str : listDst) {
					if (!isWritableDest(str)) {
						sendMSG(MSG_NOT_WRITABLE);
						return;
					}
					if (!isPasteLegal(str, mCopyedInfos)) {
						sendMSG(MSG_CONTAINED);
						return;
					}
				}

				if (isAndroidSecureIn(mCopyedInfos)) {
					sendMSG(MSG_ANDROID_SECURE);
					return;
				}

				int msize = mCopyedInfos.size();
				Log.d(TAG, "msize :" + msize);

				int i = 0;
				File[] files = new File[msize];
				for (i = 0; i < msize; i++) {
					files[i] = new File(mCopyedInfos.get(i));
				}
				space = FileOperator.getFilesTotalSpace(files, true);
				Log.d(TAG, "space " + FileUtil.convertStorage(space));

				int j = 0;
				for (String info : mCopyedInfos) {
					String path = FileOperator.pasteFile(info, listDst.get(j), new FileOperator.IProgressListener() {
						@Override
						public boolean onProgressUpdate(File file, long length) {
							long mlength = file.length();
							pinfo.current = (int) ((((double) (total + length)) / space) * (ProgressInfo.max));
							if (length < mlength) {
							} else {
								total += mlength;
							}
							pinfo.name = file.getName();

							Log.d(TAG,
									"pinfo.current :................................................" + pinfo.current);
							Log.d(TAG, "pinfo.name :................................................" + pinfo.name);
							sendMSG(MSG_REFRESH_PROGRESS);
							return true;
						}

						@Override
						public void onAddNewFile(String old, String newPath) {
							for (String str : FileUtil.MEDIA_SUFFIX) {
								if (newPath.toLowerCase().endsWith(str)) {
									MediaScannerConnection.scanFile(ApplicationManage.getContext(),
											new String[] { newPath.toString() }, null, null);
									return;
								}
							}
						}
					});

					if (path == null) {
						bBackgroundRunnable = false;
						return;
					}
					j++;
				}

				if (mProgressDlg.getProgress() != ProgressInfo.max) {
					ProgressInfo in = ProgressInfo.getInstance();
					pinfo.current = ProgressInfo.max;
					sendMSG(MSG_REFRESH_PROGRESS);
				}
				if (mProgressDlg != null) {
					SystemClock.sleep(1000);
					mProgressDlg.dismiss();
					mProgressDlg = null;
				}
				bBackgroundRunnable = false;
			}

		}
	};

	private boolean isPasteLegal(String dest, List<String> src) {
		int size = src.size();
		for (int i = 0; i < size; ++i) {
			String path = src.get(i);
			if (FileUtil.containsPath(path, dest)) {
				return false;
			}
		}
		return true;
	}

	private boolean isWritableDest(String dest) {
		File file = new File(dest);
		if (!file.exists() || !file.canWrite()) {
			return false;
		}
		return true;
	}

	private boolean isAndroidSecureIn(List<String> src) {
		for (String path : src) {
			if (path.equals(FileUtil.ANDROID_SECURE)) {
				return true;
			}
		}
		return false;
	}

}
