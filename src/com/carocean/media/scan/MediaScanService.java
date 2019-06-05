package com.carocean.media.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.autochips.storage.EnvironmentATC;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaScanConstans.YeconMediaDirColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaFilesColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaAlbumColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaArtistColumns;
import com.carocean.media.scan.MediaProvider.DatabaseHelper;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaPlayerServiceProxy;
import com.carocean.media.service.Mp3ID3Parser;
import com.carocean.utils.DataShared;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

public class MediaScanService extends Service {
	private final String TAG = getClass().getSimpleName();
	private final int MSG_SCAN = 1;
	private final int MSG_NOTIFY = 2;
	
	private final int NOTICE_DELAY_TIME = 1000 * 10;
	private EnvironmentATC mEnv = null;
	// binder
	private MediaScanServiceBinder mBinder = new MediaScanServiceBinder();
	// handler
	private MediaServiceHandler mHandler = new MediaServiceHandler();
	// scan list
	private HashMap<String, MediaScanThread> mThreadList = new HashMap<String, MediaScanThread>();
	// 系统启动时间
	private long mSystemOnTime = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addAction(MediaScanConstans.ACTION_MEDIA_QUERY);
		intentFilter.addDataScheme("file");
		registerReceiver(mMediaReceiver, intentFilter);
		mEnv = new EnvironmentATC(getApplicationContext());
		startService(new Intent(this, MediaPlayerServiceProxy.class));
		MediaActivityProxy.getInstance().bindMediaService(getApplicationContext());
		if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK1_PATH)) {
			sendStartScanMsg(MediaScanConstans.UDISK1_PATH, true, false);
		} 
		if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK2_PATH)) {
			sendStartScanMsg(MediaScanConstans.UDISK2_PATH, true, false);
		}
		Log.i("lihaibin", " onCreate checkStorageExist EXTERNAL_PATH:" + MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.EXTERNAL_PATH));
		if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.EXTERNAL_PATH)) {
			sendStartScanMsg(MediaScanConstans.EXTERNAL_PATH, true, false);
		} 
		Log.e(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			long lDelay = 0;
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_SCAN;
			msg.arg1 = startId;
			msg.obj = intent.getExtras();
			mHandler.sendMessageDelayed(msg, lDelay);
		}
		Log.e(TAG, "onStartCommand");
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mMediaReceiver);
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
		Log.e(TAG, "onDestroy");
	}

	public class MediaScanServiceBinder extends Binder {
		public MediaScanService getService() {
			return MediaScanService.this;
		}
	}

	/*
	 * 使用handler,保证不管是使用bind的方式还是intent的方式都能在同一个入口进行处理
	 */
	public class MediaServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_SCAN) {
				Bundle bundle = (Bundle) msg.obj;
				String strFile = bundle.getString(MediaScanConstans.PATH);
				String strPath = MediaScanConstans.getStoragePath(strFile);
				String strAction = bundle.getString(MediaScanConstans.ACTION);
				if (strPath != null && strAction != null) {
					if (MediaScanConstans.isSupport(strPath)) {
						String strDatabase = MediaScanConstans.Conver2Database(strPath);
						if (strDatabase != null) {
							if (strAction.equals(MediaScanConstans.ACTION_SCAN_START)) {
								Log.e(TAG, "+++++++lihaibin+++++" + strPath + ":" + strAction + "+++++++++++++");
								if (bundle.getBoolean("rescan", false)) {
									restartScan(strPath, strDatabase);
								} else {
									startScan(strPath, strDatabase, bundle.getBoolean("noticesystem", false));
								}
							} else if (strAction.equals(MediaScanConstans.ACTION_SCAN_FINISH)) {
								finishScan(strPath, strDatabase);
								Log.e(TAG, "-------lihaibin-----" + strPath + ":" + strAction + "------------");
							} 
						}
					} else {
						Log.e(TAG, "not support scan " + strPath + ", notice media scanner to scan it !!!");
						writePreference(strPath, MediaScanConstans.ACTION_SCAN_FINISH, true);
						NoticeMediaScaner(strPath);
					}
				}
			} else if (msg.what == MSG_NOTIFY) {
				Bundle bundle = (Bundle) msg.obj;
				String path = bundle.getString(MediaScanConstans.PATH);
				NoticeMediaScaner(path);
			}
		}
	}
	
	public void NoticeMediaScaner(String path) {
		Log.e(TAG, "++NoticeMediaScaner:" + path);
		if (MediaScanConstans.checkStorageExist(mEnv, path)) {
			if (path.equals(MediaScanConstans.UDISK1_PATH)
					|| path.equals(MediaScanConstans.UDISK2_PATH)
					|| path.equals(MediaScanConstans.getExternalPath())
					|| path.equals(MediaScanConstans.EXT_SDCARD1_PATH)
					|| path.equals(MediaScanConstans.EXT_SDCARD2_PATH)) {
				Uri uri = Uri.parse(MediaScanConstans.URL_HEAD + path);
				sendBroadcast(new Intent(MediaScanConstans.YECON_MEDIA_MOUNTED, uri));
				Log.e(TAG, "[lihaibin]NoticeMediaScaner:" + uri);
			}
		}
		Log.e(TAG, "--NoticeMediaScaner:" + path);
	}

	/*
	 * 创建线程,进行搜索目标磁盘并存储到相应的database操作
	 */
	private void startScan(String strPath, String strDatabase, boolean bNoticeSystem) {
		synchronized (TAG) {
			Log.e(TAG, "++prescan++ :" + strPath);
			if (strPath != null) {
				MediaScanThread t = null;
				if (mThreadList.containsKey(strDatabase)) {
					t = mThreadList.get(strDatabase);
				} 
				if (t == null || t.isInterrupted()) {
					t = new MediaScanThread(strPath, strDatabase, bNoticeSystem);
					mThreadList.put(strDatabase, t);
					Log.e(TAG, "  prescan   excute scan:" + strPath);
					t.start();
					try {
						t.join(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					Log.e(TAG, "  prescan   ignore -> this path is scanning:" + strPath);
					if (bNoticeSystem && !t.mbNoticeSystem) {
						t.mbNoticeSystem = true;
						if (readPreference(strPath) == MediaScanConstans.ACTION_SCAN_FINISH) {
							NoticeMediaScaner(strPath);
						}
					}
				}
			}
			Log.e(TAG, "--prescan-- :" + strPath);
		}
	}
	
	private void restartScan(String strPath, String strDatabase) {
		synchronized (TAG) {
			Log.e(TAG, "++rescan++ :" + strPath);
			if (strPath != null) {
				MediaScanThread t = null;
				finishScan(strPath, strDatabase);
				if (mThreadList.containsKey(strDatabase)) {
					t = (MediaScanThread) mThreadList.get(strDatabase);
					t.interrupt();
					mThreadList.remove(strDatabase);
				}
				t = new MediaScanThread(strPath, strDatabase, false);
				mThreadList.put(strDatabase, t);
				if (t != null) {
					Log.e(TAG, "  rescan   excute scan:" + strPath);
					t.start();
				}
			}
			Log.e(TAG, "--rescan-- :" + strPath);
		}
	}

	/*
	 * 停止线程相应的database操作
	 */
	private void finishScan(String strPath, String strDatabase) {
		synchronized (TAG) {
			if (mThreadList.containsKey(strDatabase)) {
				MediaScanThread t = (MediaScanThread) mThreadList
						.get(strDatabase);
				mThreadList.remove(strDatabase);
				if (!t.isInterrupted()) {
					try {
						t.join(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				t.exit();
			}
			if (strPath != null) {
				writePreference(strPath, MediaScanConstans.ACTION_SCAN_CANCEL, true);
			}
		}
	}

	private void writePreference(String path, String action, boolean bNoticeMcu) {
		if (action != null && !action.equals(readPreference(path))) {
			DataShared.getInstance(getApplicationContext()).putString(path, action);
			DataShared.getInstance(getApplicationContext()).commit();
			// notify user
			Log.e(TAG, "UpdateUI:" + path + " -> " + action);
			UpdateUI(path, action, bNoticeMcu);
		}
		Log.i(TAG, "writePreference path:" + path +", action:" + action);
	}
	
	private String readPreference(String path) {
		return DataShared.getInstance(getApplicationContext()).getString(path, MediaScanConstans.ACTION_SCAN_CANCEL);
	}

	public void UpdateUI(String strPath, String strAction, boolean  bNoticeMcu) {
		if (strPath != null && strAction != null) {
			Intent intent = new Intent();
			intent.setAction(MediaScanConstans.ACTION_YECON_MEIDA_SCANER_STATUS);
			intent.putExtra(MediaScanConstans.PATH, strPath);
			intent.putExtra(MediaScanConstans.ACTION, strAction);
			intent.putExtra(MediaScanConstans.START_UI, bNoticeMcu);
			sendBroadcast(intent);
		}
	}

	public class MediaScanThread extends Thread {

		private static final String TAG = "YeconMediaScanRunable";
		/*
		 * 当前Runnable扫描的文件目录名
		 */
		private String mstrScanPath;

		/*
		 * 该目录对应的数据库
		 */
		private String mstrDataBase;
		private DatabaseHelper mdbHelper;
		
		/*
		 * 扫描完成后是否需要通知系统扫描
		 */
		private boolean mbNoticeSystem = false;

		/*
		 * 媒体文件列表
		 */
		private Map<String, MediaObject> mMapOldMediaFile;
		private List<MediaObject> mListAllFile;
		/*
		 * 文件夹信息统计
		 */
		private HashMap<String, YeconMediaDirColumns> mMapDir;
		/*
		 * 专辑信息统计
		 */
		private HashMap<String, Integer> mMapAlbum;
		/*
		 * 艺术家信息统计
		 */
		private HashMap<String, Integer> mMapArtist;
		private Mp3ID3Parser mMp3id3Parser = null;
		/*
		 * 将KEY加入到MAP中,重复插入则计数器+1
		 */
		public void insertMap(HashMap<String, Integer> map, String key) {
			if (map.containsKey(key)) {
				map.put(key, map.get(key).intValue() + 1);
			} else {
				map.put(key, 1);
			}
		}

		public MediaScanThread(String strPath, String strDatabase, boolean bNoticeSystem) {
			this.mstrScanPath = strPath;
			this.mstrDataBase = strDatabase;
			this.mbNoticeSystem = bNoticeSystem;
			this.mdbHelper = DatabaseHelper.getInstance(getApplicationContext(), this.mstrDataBase);
			this.mMapOldMediaFile = new HashMap<String, MediaObject>();
			this.mListAllFile = new ArrayList<MediaObject>();
			this.mMapDir = new HashMap<String, YeconMediaDirColumns>();
			this.mMapAlbum = new HashMap<String, Integer>();
			this.mMapArtist = new HashMap<String, Integer>();
			this.mMp3id3Parser = new Mp3ID3Parser(getApplicationContext());
		}

		// 退出扫描线程
		public void exit() {
			synchronized (TAG) {
				Log.e(TAG, "++exit++");
				interrupt();
				if (mstrScanPath != null) {
					writePreference(mstrScanPath, MediaScanConstans.ACTION_SCAN_CANCEL, true);
				}
				Log.e(TAG, "--exit--");
			}
		}

		// 扫描流程
		@Override
		public void run() {
			mSystemOnTime = SystemProperties.getLong(MediaScanConstans.PROPERTY_SYS_TIME_SYSTEMON, 0);
			long lStart = System.currentTimeMillis();
			long lInsert = 0;
			Log.e(TAG, "++run++ " + mstrScanPath);
			try {
				// clear list
				mMapAlbum.clear();
				mMapArtist.clear();
				mListAllFile.clear();
				mMapDir.clear();
				synchronized (TAG) {
					if (!isInterrupted()) {
						boolean bNoticeMcu = false;
						Log.i(TAG, " run mSystemOnTime:" + mSystemOnTime + ", curtime:" + SystemClock.uptimeMillis());
						int iDeviceState = Settings.Secure.getInt(MediaScanService.this.getContentResolver(), "device_provisioned");
						if (iDeviceState == 1) {
							if(mSystemOnTime != 0 && (SystemClock.uptimeMillis() - mSystemOnTime) > MediaScanConstans.MEDIA_HOTPLUG_IGNORE_TIME){
								if (!MediaScanConstans.isBtPhone()) {
									bNoticeMcu = true;
								}
							}
						}
						writePreference(mstrScanPath, MediaScanConstans.ACTION_SCAN_START, bNoticeMcu);
					}
				}
				queryFileList();
				// get all files
				mListAllFile = getFileList(mstrScanPath);
				// analysis files info
				List<ContentValues> lsFileValues = AnalysisFileInfo(mListAllFile);
				// insert into databases
				lInsert = insertToDB(lsFileValues);
				
				mMapOldMediaFile.clear();
				mMapAlbum.clear();
				mMapArtist.clear();
				mMapDir.clear();
				
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (TAG) {
					if (!isInterrupted() && MediaScanConstans.checkStorageExist(mEnv, mstrScanPath)) {
						writePreference(mstrScanPath, MediaScanConstans.ACTION_SCAN_FINISH, true);
						if (mbNoticeSystem) {
							Message msg = mHandler.obtainMessage();
							msg.what = MSG_NOTIFY;
							Bundle bundle = new Bundle();
							bundle.putString(MediaScanConstans.PATH, mstrScanPath);
							msg.obj = bundle;
							mHandler.sendMessageDelayed(msg, NOTICE_DELAY_TIME);
						} else {
							Log.e(TAG, "don’t notice system scanner!!!");
						}
					}
				}
			}
			if (!isInterrupted()) {
				interrupt();
			}
			Log.e(TAG, "--run-- " + mstrScanPath + " cost time:"
							+ (System.currentTimeMillis() - lStart)
							+ " files amount:" + lInsert);
		}
		
		private long insertToDB(List<ContentValues> lsFileValues) {
			Log.e(TAG, "++insertToDB++");
			long lInsert = 0;
			if (!isInterrupted()) {
				List<ContentValues> lsDirValues = new ArrayList<ContentValues>();
				for (Entry<String, YeconMediaDirColumns> entry : mMapDir.entrySet()) {
					ContentValues cv = new ContentValues();
					cv.put(YeconMediaDirColumns.DATA, entry.getValue().mData);
					cv.put(YeconMediaDirColumns.NAME, entry.getValue().mName);
					cv.put(YeconMediaDirColumns.PARENT, entry.getValue().mParent);
					cv.put(YeconMediaDirColumns.AMOUNT_AUDIO,
							entry.getValue().mAudio);
					cv.put(YeconMediaDirColumns.AMOUNT_VIDEO,
							entry.getValue().mVideo);
					cv.put(YeconMediaDirColumns.AMOUNT_IMAGE,
							entry.getValue().mImage);
					cv.put(YeconMediaDirColumns.LETTER, HanziToPinyin.getInstance().getFirstLetter(entry.getValue().mName));
					lsDirValues.add(cv);
				}
				
				mdbHelper.ClearTable();
				lInsert = mdbHelper.insert(MediaScanConstans.TABLE_FILES, lsFileValues);
				// insert into table dir
				mdbHelper.insert(MediaScanConstans.TABLE_DIR, lsDirValues);
				
				List<ContentValues> lsAlbumValues = new ArrayList<ContentValues>();
				for (Entry<String, Integer> entry : mMapAlbum.entrySet()) {
					ContentValues cv = new ContentValues();
					cv.put(YeconMediaAlbumColumns.NAME, entry.getKey());
					cv.put(YeconMediaAlbumColumns.AMOUNT, entry.getValue());
					lsAlbumValues.add(cv);
				}
				mdbHelper.insert(MediaScanConstans.TABLE_ALBUM, lsAlbumValues);

				// insert into table artist
				List<ContentValues> lsArtistValues = new ArrayList<ContentValues>();
				for (Entry<String, Integer> entry : mMapArtist.entrySet()) {
					ContentValues cv = new ContentValues();
					cv.put(YeconMediaArtistColumns.NAME, entry.getKey());
					cv.put(YeconMediaArtistColumns.AMOUNT, entry.getValue());
					lsArtistValues.add(cv);
				}
				mdbHelper.insert(MediaScanConstans.TABLE_ARTIST, lsArtistValues);
			}
			Log.e(TAG, "--insertToDB--");
			return lInsert;
		}

		/*
		 * 获取浏览目录的所有文件
		 */
		private List<MediaObject> getFileList(String path) {
			long lStart = SystemClock.uptimeMillis();
			Log.e(TAG, "++getFileList++");
			List<MediaObject> lsFile = new ArrayList<MediaObject>();
			try {
				MediaFileParse travel = new MediaFileParse();
				if (MediaScanConstans.checkStorageExist(mEnv, path)) {
					travel.traverseFolder(path, lsFile, false, 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.e(TAG, "--getFileList-- file:" + lsFile.size() + " cost:" + (SystemClock.uptimeMillis() - lStart));
			return lsFile;
		}

		/*
		 * 解析文件, 并且构建相关的ID3数据列表
		 */
		private List<ContentValues> AnalysisFileInfo(List<MediaObject> lsFile) {
			long lStart = SystemClock.uptimeMillis();
			Log.e(TAG, "++AnalysisFileInfo++");
			List<ContentValues> lsFileValues = new ArrayList<ContentValues>();
			if (lsFile != null) {
				for (MediaObject mediaFileInfo : lsFile) {
					if (isInterrupted()) {
						break;
					}
					try {
						if (mediaFileInfo != null && mediaFileInfo.getFileName() != null) {
							// add to media file list
							ContentValues values = new ContentValues();
							values.put(YeconMediaFilesColumns.DATA, mediaFileInfo.getFilePath());
							values.put(YeconMediaFilesColumns.NAME, mediaFileInfo.getFileName());
							values.put(YeconMediaFilesColumns.PARENT, mediaFileInfo.getFileParent());
							values.put(YeconMediaFilesColumns.MEDIA_TYPE, mediaFileInfo.getMediaType());
							values.put(YeconMediaFilesColumns.MIME_TYPE, mediaFileInfo.getMimeType());
							if (mediaFileInfo.getMediaType() == 1) {
								MediaObject mediaObject = null;
								if (mMapOldMediaFile != null) {
									if (mMapOldMediaFile.containsKey(mediaFileInfo.getFilePath())) {
										mediaObject = mMapOldMediaFile.get(mediaFileInfo.getFilePath());
										mMapOldMediaFile.remove(mediaFileInfo.getFilePath());
									}
								} 
								if (mediaObject != null) {
									// 重复文件直接使用上一次解析的ID3
									values.put(YeconMediaFilesColumns.TITLE, mediaObject.getTitle());
									values.put(YeconMediaFilesColumns.ALBUM, mediaObject.getAlbum());
									values.put(YeconMediaFilesColumns.ARTIST, mediaObject.getArtist());
									// insert into map
									insertMap(mMapAlbum, mediaObject.getAlbum());
									insertMap(mMapArtist, mediaObject.getArtist());
								} else {
									mMp3id3Parser.AnalysisID3(mediaFileInfo.getFilePath());
									values.put(YeconMediaFilesColumns.TITLE, mMp3id3Parser.getTitle());
									values.put(YeconMediaFilesColumns.ALBUM, mMp3id3Parser.getAlbum());
									values.put(YeconMediaFilesColumns.ARTIST, mMp3id3Parser.getArtist());
									insertMap(mMapAlbum, mMp3id3Parser.getAlbum());
									insertMap(mMapArtist, mMp3id3Parser.getArtist());
								}
							}
							values.put(YeconMediaFilesColumns.LETTER, HanziToPinyin.getInstance().getFirstLetter(mediaFileInfo.getFileName()));
							lsFileValues.add(values);
						}
						// dir
						YeconMediaDirColumns dir = null;
						if (mMapDir.containsKey(mediaFileInfo.getFileParent())) {
							dir = mMapDir.get(mediaFileInfo.getFileParent());
						} else {
							dir = new YeconMediaDirColumns();
							dir.mData = mediaFileInfo.getFileParent();
							dir.mParent = MediaFileParse.getParentDir(mediaFileInfo.getFileParent());
							dir.mName = MediaFileParse.getFileName(mediaFileInfo.getFileParent());
						}
						if (mediaFileInfo.getMediaType() == 1) {
							dir.mAudio++;
						} else if (mediaFileInfo.getMediaType() == 2) {
							dir.mVideo++;
						} else if (mediaFileInfo.getMediaType() == 3) {
							dir.mImage++;
						}
						mMapDir.put(mediaFileInfo.getFileParent(), dir);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			Log.e(TAG, "--AnalysisFileInfo-- file:" + lsFileValues.size() + " cost:" + (SystemClock.uptimeMillis() - lStart));
			return lsFileValues;
		}
		
		/*
		 * 查询数据库中已经解析过的数据
		 */
		private void queryFileList() {
			Log.e(TAG, "++queryFileList++");
			long startTime = System.currentTimeMillis();
			String columns[] = new String[] {
					YeconMediaFilesColumns.DATA, 
					YeconMediaFilesColumns.TITLE,
					YeconMediaFilesColumns.ARTIST,
					YeconMediaFilesColumns.ALBUM};
			String selection =  YeconMediaFilesColumns.MEDIA_TYPE + "=1 ";
			Cursor cursor = null;
			try {
				cursor = mdbHelper.query(MediaScanConstans.TABLE_FILES, columns, selection, null);
				if (cursor != null) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						try {
							MediaObject file = new MediaObject();
							file.setFilePath(cursor.getString(0));
							file.setTitle(cursor.getString(1));
							file.setArtist(cursor.getString(2));
							file.setAlbum(cursor.getString(3));
							mMapOldMediaFile.put(file.getFilePath(), file);
						} catch (Exception e) {
							e.printStackTrace();
						}
						cursor.moveToNext();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			Log.e(TAG, "--queryFileList--, cost:" + (System.currentTimeMillis() - startTime));
		}
	}
	
	private BroadcastReceiver mMediaReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (intent != null && action != null) {
				Log.i(TAG, "++onReceive mMediaReceiver :" + action);
				final Uri uri = intent.getData();
				if (uri != null && uri.getScheme() != null && uri.getScheme().equals("file")) {
					String path = uri.getPath();
					Log.i(TAG, "--onReceive mMediaReceiver path:" + path);
					if (path != null) {
						if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
							sendStartScanMsg(path, true, false);
						} else if (action.equals(MediaScanConstans.ACTION_MEDIA_QUERY)) {
							sendStartScanMsg(path, false, true);
						} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
								|| action.equals(Intent.ACTION_MEDIA_EJECT)
								|| action.equals(Intent.ACTION_MEDIA_REMOVED) 
								|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
							sendFinishScanMsg(path);
						} 
					}
				}
			}
		}
	};
	
	private void sendStartScanMsg(String strPath, boolean bNoticeSystem, boolean bReScan) {
		if (strPath != null && (strPath.equals(MediaScanConstans.UDISK1_PATH) || 
				strPath.equals(MediaScanConstans.UDISK2_PATH) || strPath.equals(MediaScanConstans.EXTERNAL_PATH))) {
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_SCAN;
			Bundle bundle = new Bundle();
			bundle.putBoolean("noticesystem", bNoticeSystem);
			bundle.putString(MediaScanConstans.PATH, strPath);
			bundle.putString(MediaScanConstans.ACTION,
					MediaScanConstans.ACTION_SCAN_START);
			bundle.putBoolean("rescan", bReScan);
			msg.obj = bundle;
			mHandler.sendMessage(msg);
		}
	}
	
	private void sendFinishScanMsg(String strPath) {
		if (strPath != null && (strPath.equals(MediaScanConstans.UDISK1_PATH) || 
				strPath.equals(MediaScanConstans.UDISK2_PATH) || strPath.equals(MediaScanConstans.EXTERNAL_PATH))) {
			if (!MediaScanConstans.ACTION_SCAN_CANCEL.equals(readPreference(strPath))) {
				Message msg = mHandler.obtainMessage();
				msg.what = MSG_SCAN;
				Bundle bundle = new Bundle();
				bundle.putString(MediaScanConstans.PATH, strPath);
				bundle.putString(MediaScanConstans.ACTION,
						MediaScanConstans.ACTION_SCAN_FINISH);
				msg.obj = bundle;
				mHandler.sendMessage(msg);
			}
		}
	}
	
}
