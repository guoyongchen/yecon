package com.carocean.media.constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.autochips.storage.EnvironmentATC;
import com.carocean.utils.DataShared;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

public class MediaScanConstans {
	private static final String TAG = "MediaScanConstans";
	public static final String PROPERTY_SYS_TIME_SYSTEMON = "persist.sys.timesystemon";
	public static final String ACTION_MEDIA_QUERY = "yecon.intent.action.MEDIA_QUREY";
	
	public static final String URL_HEAD = "file://";
	public static final long MEDIA_HOTPLUG_IGNORE_TIME = 15000;
	public static final long LOCAL_FILE_MAX_SIZE = 512 * 1024 * 1024; // 536870912
	// AUTHORITIES
	private static final String AUTHORITIES = "com.carocean.media.scan.MediaProvider";

	/*
	 * Uri construct content:// + AUTHORITIES + / + path (Database name + /
	 * Table name + / ... )
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/");

	/**
	 * @Title: getContent
	 * @Description: 获取需要查询的数据库中的表的URI
	 * @param @param
	 *            strDataBase 需要查询的数据库
	 * @param @param
	 *            strTable 需要查询的表
	 */
	public static Uri getContent(String strDataBase, String strTable) {
		return Uri.parse(CONTENT_URI + strDataBase + "/" + strTable);
	}

	public static final String YECON_MEDIA_STORE_SP_NAME = "YeconMediaStoreSharePreference";

	/*
	 * file type code
	 */
	public static final class FileType {
		// MUSIC
		public static final int FILE_TYPE_MP3 = 1;
		public static final int FILE_TYPE_M4A = 2;
		public static final int FILE_TYPE_WAV = 3;
		public static final int FILE_TYPE_AMR = 4;
		public static final int FILE_TYPE_AWB = 5;
		public static final int FILE_TYPE_WMA = 6;
		public static final int FILE_TYPE_OGG = 7;
		public static final int FILE_TYPE_AAC = 8;
		public static final int FILE_TYPE_MKA = 9;
		public static final int FILE_TYPE_FLAC = 10;
		// MIDI
		public static final int FILE_TYPE_MID = 11;
		public static final int FILE_TYPE_SMF = 12;
		public static final int FILE_TYPE_IMY = 13;
		public static final int FILE_TYPE_RA = 14;
		public static final int FILE_TYPE_AIFF = 15;
		public static final int FILE_TYPE_AC3 = 16;
		public static final int FILE_TYPE_APE = 17;
		// VIDEO
		public static final int FILE_TYPE_MP4 = 21;
		public static final int FILE_TYPE_M4V = 22;
		public static final int FILE_TYPE_3GPP = 23;
		public static final int FILE_TYPE_3GPP2 = 24;
		public static final int FILE_TYPE_WMV = 25;
		public static final int FILE_TYPE_ASF = 26;
		public static final int FILE_TYPE_MKV = 27;
		public static final int FILE_TYPE_MP2TS = 28;
		public static final int FILE_TYPE_AVI = 29;
		public static final int FILE_TYPE_WEBM = 30;
		public static final int FILE_TYPE_MP2PS = 200;
		public static final int FILE_TYPE_RM = 201;
		// IMAGE
		public static final int FILE_TYPE_JPEG = 31;
		public static final int FILE_TYPE_GIF = 32;
		public static final int FILE_TYPE_PNG = 33;
		public static final int FILE_TYPE_BMP = 34;
		public static final int FILE_TYPE_WBMP = 35;
		public static final int FILE_TYPE_WEBP = 36;
		// other
		public static final int FILE_TYPE_M3U = 41;
		public static final int FILE_TYPE_PLS = 42;
		public static final int FILE_TYPE_WPL = 43;
		public static final int FILE_TYPE_HTTPLIVE = 44;
		public static final int FILE_TYPE_FL = 51;
		// Other popular file types
		public static final int FILE_TYPE_TEXT = 100;
		public static final int FILE_TYPE_HTML = 101;
		public static final int FILE_TYPE_PDF = 102;
		public static final int FILE_TYPE_XML = 103;
		public static final int FILE_TYPE_MS_WORD = 104;
		public static final int FILE_TYPE_MS_EXCEL = 105;
		public static final int FILE_TYPE_MS_POWERPOINT = 106;
		public static final int FILE_TYPE_ZIP = 107;
	}

	/*
	 * database name
	 */
	public static final String INTERNAL_VOLUME = "internal";
	public static final String EXTERNAL_VOLUME = "external";
	public static final String SDCARD_VOLUME1 = "sdcard1";
	public static final String SDCARD_VOLUME2 = "sdcard2";
	public static final String UDISK_VOLUME1 = "udisk1";
	public static final String UDISK_VOLUME2 = "udisk2";
	public static final String UDISK_VOLUME3 = "udisk3";
	public static final String UDISK_VOLUME4 = "udisk4";
	public static final String UDISK_VOLUME5 = "udisk5";

	public static final String DATABASES[] = new String[] { EXTERNAL_VOLUME, SDCARD_VOLUME1, SDCARD_VOLUME2,
			UDISK_VOLUME1, UDISK_VOLUME2, UDISK_VOLUME3, UDISK_VOLUME4, UDISK_VOLUME5 };

	/*
	 * storage absolute path
	 */
	public static final String EXTERNAL_PATH = "/mnt/sdcard";
	public static final String EXT_SDCARD1_PATH = "/mnt/ext_sdcard1";
	public static final String EXT_SDCARD2_PATH = "/mnt/ext_sdcard2";
	public static final String UDISK1_PATH = "/mnt/udisk1";
	public static final String UDISK2_PATH = "/mnt/udisk2";
	public static final String KWMUSIC_PATH = getExternalPath() + "/kwmusiccar/song";

	public static final String STORAGES[] = new String[] {
			EXTERNAL_PATH,
//			EXT_SDCARD1_PATH,
			// EXT_SDCARD2_PATH,
			UDISK1_PATH, 
			UDISK2_PATH,
			// UDISK3_PATH,
			// UDISK4_PATH,
			// UDISK5_PATH
	};

	public static final String PERSIST_YECON_SCAN = "persist.sys.yecon_scan";

	public static final class Support {
		public static final int EXTERNAL = 1 << 0;
		public static final int SDCARD1 = 1 << 1;
		public static final int SDCARD2 = 1 << 2;
		public static final int UDISK1 = 1 << 3;
		public static final int UDISK2 = 1 << 4;
		public static final int UDISK3 = 1 << 5;
		public static final int UDISK4 = 1 << 6;
		public static final int UDISK5 = 1 << 7;
		public static final int ALL = EXTERNAL | SDCARD1 | SDCARD2 | UDISK1 | UDISK2 | UDISK3 | UDISK4 | UDISK5;
	}

	public static String getExternalPath() {
		return EXTERNAL_PATH;
	}

	public static void setScanFlag(int iFlag) {
		SystemProperties.set(PERSIST_YECON_SCAN, String.valueOf(iFlag));
	}

	public static boolean isSupport(String storage) {
		int iFlag = SystemProperties.getInt(PERSIST_YECON_SCAN, Support.ALL);
		int iSupport = 0;
		if (storage == null) {
			return false;
		} else if (storage.contains(UDISK1_PATH)) {
			iSupport = iFlag & Support.UDISK1;
		} else if (storage.contains(UDISK2_PATH)) {
			iSupport = iFlag & Support.UDISK2;
		} else if (storage.contains(EXTERNAL_PATH)) {
			iSupport = iFlag & Support.EXTERNAL;
		}
		return (iSupport != 0);
	}

	public static boolean checkStorageExist(EnvironmentATC env, String storage) {
		if (env != null && storage != null) {
			try {
				if (storage.equals(EXTERNAL_PATH)) {
					return isExist(EXTERNAL_PATH);
				} else {
					String[] devices = env.getStorageMountedPaths();
					for (int i = 0; i < devices.length; i++) {
						if (devices[i].contains(storage)) {
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private static boolean isExist(String path) {
		boolean isExist = false;
		if (path != null) {
			File file = new File(path);
			if (file.exists() && file.canRead() && file.canWrite()) {
				isExist = true;
			}
		}
		return isExist;
	}

	/*
	 * intent
	 */
	public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "yecon.intent.action.MEDIA_SCANNER_SCAN_DIR";
	public static final String ACTION_YECON_MEIDA_SCANER_STATUS = "yecon.intent.action.MEDIA_SCANER_STATUS";
	public static final String YECON_MEDIA_MOUNTED = "yecon.intent.action.MEDIA_MOUNTED";

	// action
	public static final String ACTION = "action";
	public static final String ACTION_SCAN_START = "scan_start";
	public static final String ACTION_SCAN_ANALYSIS = "scan_analysis";
	public static final String ACTION_SCAN_FINISH = "scan_finish";
	public static final String ACTION_SCAN_CANCEL = "scan_cancel";
	public static final String ACTION_SCAN_FILE = "scan_file";

	public static final String PATH = "path";
	public static final String START_UI = "start_ui";
	/*
	 * 文件表, 包含音乐 、视频 、图片 三种文件信息
	 */
	public static final String TABLE_FILES = "files";
	/*
	 * 文件夹表，存放所有的文件夹信息
	 */
	public static final String TABLE_DIR = "dir";
	/*
	 * 专辑表，存放所有的专辑信息
	 */
	public static final String TABLE_ALBUM = "album";
	/*
	 * 艺术家表，存放所有的艺术家信息
	 */
	public static final String TABLE_ARTIST = "artist";
	/**
	 * @ClassName: YeconMediaFilesColumns
	 * @Description: Files的表结构
	 */
	public static final class YeconMediaFilesColumns {

		/*
		 * The unique ID for a row. Type: INTEGER (long) PRIMARY KEY
		 */
		public static final String _ID = "_id";
		public int mID;

		/*
		 * file absolute path type: char
		 */
		public static final String DATA = "data";
		public String mData;

		/*
		 * display file name type: CHAR
		 */
		public static final String NAME = "file_name";
		public String mName;

		/*
		 * parent dir type: CHAR
		 */
		public static final String PARENT = "parent";
		public String mParent;

		/*
		 * artist type: INTEGER
		 */
		public static final String PARENT_ID = "parent_id";
		public int mParentID;

		/*
		 * media type type: CHAR
		 */
		public static final String MEDIA_TYPE = "media_type";
		public int mMediaType;

		/*
		 * mime type type: INTEGER
		 */
		public static final String MIME_TYPE = "mime_type";
		public int mMimeType;

		/*
		 * damage type int
		 */
		public static final String DAMAGE = "damage";
		public int mDamage;
		
		/*
		 * title type: CHAR
		 */
		public static final String TITLE = "title";
		public String mTitle;

		/*
		 * artist type: CHAR
		 */
		public static final String ARTIST = "artist";
		public String mArtist;

		/*
		 * artist_id type: INTEGER
		 */
		public static final String ARTIST_ID = "artist_id";
		public int mArtistID;
		
		/*
		 * album type: CHAR
		 */
		public static final String ALBUM = "album";
		public String mAlbum;

		/*
		 * artist type: INTEGER
		 */
		public static final String ALBUM_ID = "album_id";
		public int mAlbumID;
		
		/*
		 * letter type String
		 */
		public static final String LETTER = "letter";
		public String mLetter;
		/*
		 * get sql for create this table
		 */
		public static String getCreateTableSQL(String table) {
			String strSQL = "CREATE TABLE IF NOT EXISTS ";
			if (table == null || table.length() == 0) {
				table = "files";
			}
			strSQL += table;
			strSQL += "(";
			strSQL += _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
			strSQL += DATA + " TEXT NOT NULL, ";
			strSQL += NAME + " TEXT, ";
			strSQL += PARENT + " TEXT, ";
			strSQL += PARENT_ID + " INTEGER, ";
			strSQL += MEDIA_TYPE + " INTEGER, ";
			strSQL += MIME_TYPE + " INTEGER, ";
			strSQL += DAMAGE + " INTEGER,";
			strSQL += TITLE + " TEXT, ";
			strSQL += ARTIST + " TEXT, ";
			strSQL += ARTIST_ID + " INTEGER, ";
			strSQL += ALBUM + " TEXT, ";
			strSQL += ALBUM_ID + " INTEGER, ";
			strSQL += LETTER + " TEXT";
			strSQL += ")";
			return strSQL;
		}
	}
	
	public static final class YeconMediaDirColumns {

		/*
		 * The unique ID for a row. Type: INTEGER (long) PRIMARY KEY
		 */
		public static final String _ID = "_id";
		public int mID;

		/*
		 * dir absolute path type: char
		 */
		public static final String DATA = "data";
		public String mData;

		/*
		 * display dir name type: CHAR
		 */
		public static final String NAME = "dir_name";
		public String mName;

		/*
		 * parent dir type: CHAR
		 */
		public static final String PARENT = "parent";
		public String mParent;

		/*
		 * the numbers of audio type: int
		 */
		public static final String AMOUNT_AUDIO = "amount_audio";
		public int mAudio;

		/*
		 * the numbers of video type: int
		 */
		public static final String AMOUNT_VIDEO = "amount_video";
		public int mVideo;

		/*
		 * the numbers of images type: int
		 */
		public static final String AMOUNT_IMAGE = "amount_image";
		public int mImage;

		/*
		 * letter type String
		 */
		public static final String LETTER = "letter";
		public String mLetter;
		/*
		 * get sql for create this table
		 */
		public static String getCreateTableSQL(String table) {
			String strSQL = "CREATE TABLE IF NOT EXISTS ";
			if (table == null || table.length() == 0) {
				table = "dir";
			}
			strSQL += table;
			strSQL += "(";
			strSQL += _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
			strSQL += DATA + " TEXT, ";
			strSQL += NAME + " TEXT, ";
			strSQL += PARENT + " TEXT, ";
			strSQL += AMOUNT_AUDIO + " INTEGER, ";
			strSQL += AMOUNT_VIDEO + " INTEGER, ";
			strSQL += AMOUNT_IMAGE + " INTEGER, ";
			strSQL += LETTER + " TEXT";
			strSQL += ")";
			return strSQL;
		}
	}
	
	
	public static final class YeconMediaAlbumColumns {

		/*
		 * The unique ID for a row. Type: INTEGER (long)
		 */
		public static final String _ID = "_id";

		/*
		 * display album name type: CHAR
		 */
		public static final String NAME = "album_name";

		/*
		 * the numbers of album type: int
		 */
		public static final String AMOUNT = "amount";

		/*
		 * get sql for create this table
		 */
		public static String getCreateTableSQL(String table) {
			String strSQL = "CREATE TABLE IF NOT EXISTS ";
			if (table == null || table.length() == 0) {
				table = "album";
			}
			strSQL += table;
			strSQL += "(";
			strSQL += _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
			strSQL += NAME + " TEXT, ";
			strSQL += AMOUNT + " INTEGER";
			strSQL += ")";
			return strSQL;
		}
	}

	public static final class YeconMediaArtistColumns {

		/*
		 * The unique ID for a row. Type: INTEGER (long)
		 */
		public static final String _ID = "_id";

		/*
		 * display artist name type: CHAR
		 */
		public static final String NAME = "artist_name";

		/*
		 * the numbers of album type: int
		 */
		public static final String AMOUNT = "amount";

		/*
		 * get sql for create this table
		 */
		public static String getCreateTableSQL(String table) {
			String strSQL = "CREATE TABLE IF NOT EXISTS ";
			if (table == null || table.length() == 0) {
				table = "artist";
			}
			strSQL += table;
			strSQL += "(";
			strSQL += _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
			strSQL += NAME + " TEXT, ";
			strSQL += AMOUNT + " INTEGER";
			strSQL += ")";
			return strSQL;
		}
	}

	public static String Conver2Database(String strStorage) {
		String strDatabase = null;
		if (strStorage == null) {
			return null;
		} else if (strStorage.contains(UDISK1_PATH)) {
			strDatabase = UDISK_VOLUME1;
		} else if (strStorage.contains(UDISK2_PATH)) {
			strDatabase = UDISK_VOLUME2;
		} else if (strStorage.contains(EXTERNAL_PATH)) {
			strDatabase = EXTERNAL_VOLUME;
		}
		return strDatabase;
	}

	public static String Convert2Storage(String dbName) {
		String strStorage = null;
		if (dbName != null) {
			if (UDISK1_PATH.contains(dbName)) {
				strStorage = UDISK1_PATH;
			} else if (UDISK2_PATH.contains(dbName)) {
				strStorage = UDISK2_PATH;
			} else if (EXTERNAL_PATH.contains(dbName)) {
				strStorage = EXTERNAL_PATH;
			}
		}
		return strStorage;
	}

	public static String getStoragePath(String strFile) {
		String strDisk = null;
		// file path
		if (strFile == null) {
			return null;
		} else if (strFile.contains(UDISK1_PATH)) {
			strDisk = UDISK1_PATH;
		} else if (strFile.contains(UDISK2_PATH)) {
			strDisk = UDISK2_PATH;
		} else if (strFile.contains(EXTERNAL_PATH)) {
			strDisk = EXTERNAL_PATH;
		}
		return strDisk;
	}
	
	public static String getLastDevice(Context context) {
		return DataShared.getInstance(context).getString(MediaPlayerContants.LAST_MEMORY_DEVICE, null);
	}

	public static void saveLastDevice(Context context, String strSource) {
		Log.i(TAG, "saveLastDevice strSource:"+ strSource);
		DataShared.getInstance(context).putString(MediaPlayerContants.LAST_MEMORY_DEVICE, strSource);
		DataShared.getInstance(context).commit();
	}
	
	public static boolean isBtPhone() {
		return false;//YeconSaveDataUtils.isPhoneStartup();
	}
	
	public static boolean isNaviForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> tasksAll = am.getRecentTasks(1, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		if (tasksAll != null) {
			for (RecentTaskInfo recentTaskInfo : tasksAll) {
				if (recentTaskInfo.id > 0 && recentTaskInfo.baseIntent != null) {
					String baseintent = recentTaskInfo.baseIntent.toString();
					if (baseintent != null && baseintent.contains("com.autonavi.amapauto")) {
						Log.i(TAG, "isNaviForeground true");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isPowerOff() {
		return false;//YeconSaveDataUtils.getSceneStatus(SaveDataConstant.COMMON_SAVE_DATA_KEY_POWER_STATUS) == 0 ? true : false;
	}
	
	public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
            	 return true;
            }
        }
        return false;
    }
	
	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

    /**
     * 获取指定文件夹的大小
     *
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File f) {
        long size = 0;
        File flist[] = f.listFiles();//文件夹目录下的所有文件
        if (flist == null) {//4.2的模拟器空指针。
            return 0;
        }
        if (flist != null) {
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {//判断是否父目录下还有子目录
                    size = size + getFileSizes(flist[i]);
                } else {
                    size = size + getFileSize(flist[i]);
                }
            }
        }
        return size;
    }
    
    /**
     * 获取指定文件的大小
     *
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) {

        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);//使用FileInputStream读入file的数据流
                size = fis.available();//文件的大小
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
        }
        return size;
    }
    
	/**
	 * @param src
	 * @param dest
	 * @return new file path if successful, or return null
	 */
	public static boolean copyFile(String src, String dest) {
		boolean bRet = false;
		File file = new File(src);
		if (!file.exists() || file.isDirectory()) {
			Log.i(TAG, "copyFile: file not exist or is directory, " + src);
			return bRet;
		}
		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			fi = new FileInputStream(file);
			File destPlace = new File(dest);
			if (!destPlace.exists()) {
				if (!destPlace.mkdirs())
					return bRet;
			}

			String destPath = makePath(dest, file.getName());
			File destFile = new File(destPath);
			if (destFile.exists()) {
				destFile.delete();
			}
//			int i = 1;
//			while (destFile.exists()) {
//				String destName = FileUtil.getNameFromFilename(file.getName()) + " " + i++ + "."
//						+ FileUtil.getExtFromFilename(file.getName());
//				destPath = FileUtil.makePath(dest, destName);
//				destFile = new File(destPath);
//			}

			if (!destFile.createNewFile())
				return bRet;
			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}
			bRet = true;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "copyFile: file not found, " + src);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "copyFile: " + e.toString());
		} finally {
			try {
				if (fo != null)
					fo.flush();
				if (fo != null)
					fo.getFD().sync();
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bRet;
	}
}
