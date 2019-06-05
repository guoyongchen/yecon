package com.carocean.media.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.SystemClock;
import android.util.Log;

import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.service.MediaFile;


public class MediaFileParse {
	private final static String TAG = "YeconMediaProvider";
	// 忽略的文件夹不遍历
	private final static String RECYCLE = "recycle";
	private final static String DVR_NORMAL = "dvr/normal";
	private final static String DVR_URGENT = "dvr/urgent";
	private final static String TXZ_VOICE = MediaScanConstans.getExternalPath() + "/txz";
	private final static String KW_WELCOME = "kuwomusic/welcome";
	private final static String NAVI_ONE = "NaviOne";
	private final static String FUN_DRIVE = "FunDrive";
	private final static String AMAPAUTO8 = "amapauto8";
	private final static String SVN = ".svn";
	
	private final static String IGNORE_DIR[] = new String[] {
//		RECYCLE,
//		DVR_NORMAL,
//		DVR_URGENT,
//		TXZ_VOICE,
//		KW_WELCOME,
		NAVI_ONE,
		FUN_DRIVE,
		AMAPAUTO8,
		SVN
	};
	
	// no media tag
	private final static String NOMEDIA = ".nomedia";
	
	// 单个文件夹广度遍历时间
	private final int MAX_TRAVEL_TIME = 5 * 1000;
	
	/*
	 * @Description: 广度优先遍历文件
	 * @param1: String path : 扫描的文件夹
	 * @param2:	List<String> lsFile : 用于存放扫描到文件的列表
	 * @param3:	boolean bContainsFolder : 扫描的文件列表是否包含文件夹
	 */
	public void traverseFolder(String path, List<MediaObject> lsFile, boolean bContainsFolder, int iReScanCount) {
		List<MediaObject> lsDir = new ArrayList<MediaObject>();
		try {
			TravelThread thread = new TravelThread(path, bContainsFolder);
			thread.start();
			try {
				thread.join(MAX_TRAVEL_TIME);
				if (thread.isAlive()) {
					thread.interrupt();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			lsFile.addAll(thread.lsFile);
			lsDir.addAll(thread.lsDir);
			thread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < lsDir.size(); i++) {
			traverseFolder(lsDir.get(i).getFilePath(), lsFile, bContainsFolder, 1);
		}
	}
	
	public class TravelThread extends Thread {

		private boolean bContainsFolder;
		private String path;
		private List<MediaObject> lsDir = new ArrayList<MediaObject>();
		private List<MediaObject> lsFile = new ArrayList<MediaObject>();
		
		TravelThread(String path, boolean bContainsFolder) {
			this.bContainsFolder = bContainsFolder;
			if (path != null && path.equals(MediaScanConstans.EXTERNAL_PATH)) {
				this.path = MediaScanConstans.KWMUSIC_PATH;
			} else {
				this.path = path;
			}
		}

		@Override
		public void run() {
			super.run();
			long lStart = SystemClock.uptimeMillis();
			Log.i(TAG, "++traversing folder++ " + path);
			try {
				File dir = new File(path);
				if (dir.exists()) {
					File[] files = dir.listFiles();
					if (files != null && files.length != 0) {
						for (File file : files) {
							if (!isInterrupted()) {
								String filepath = file.getAbsolutePath();
								filepath.trim();
								Log.i(TAG, "traversing folder：" + path + ",filepath:" + filepath);
								if (filepath.isEmpty()) {
									continue;
								}
								if (file.isDirectory()) {
									if (!isIgnore(filepath)) {
										lsDir.add(new MediaObject(filepath, getFileNameEx(filepath), path, 4, 0));
										if (bContainsFolder) {
											lsFile.add(new MediaObject(filepath, getFileNameEx(filepath), path, 4, 0));
										}
									}
								} else {
									if (filepath.indexOf(".") != -1) {
										String mimeType = MediaFile.getMimeTypeForFile(filepath);
										if (MediaFile.isMimeTypeMedia(mimeType)) {
											int iMemeType = MediaFile.getFileTypeForMimeType(mimeType);
											lsFile.add(new MediaObject(filepath, getFileNameEx(filepath), path,
													getMediaType(iMemeType), iMemeType));
										}
									}
								}
							} else {
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i(TAG, "--traversing folder-- " + path + " file:" + lsFile.size() + " folder:" + lsDir.size() + " cost:" + (SystemClock.uptimeMillis() - lStart));
		}
	}
	
	/**
	 * @Title: isIgnore
	 * @Description: 当前文件是否在被忽略的文件夹目录 或者 是否存在 .nomedia 文件
	 */
	public static boolean isIgnore(String file) {
		boolean bIgnore = false;
		try {
			if (file != null) {
				// check ignore files
				for (String ignoreDir : IGNORE_DIR) {
					if (file.toLowerCase().contains(ignoreDir.toLowerCase())) {
						bIgnore = true;
						break;
					};
				}
				
				// check no media tag
				String strNoMediaTag = file + File.separator + NOMEDIA;
				File fileNoMediaTag = new File(strNoMediaTag);
				if (fileNoMediaTag.exists()) {
					bIgnore = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIgnore;
	}
	
	/**
	 * @Title: getFileName
	 * @Description: 获取文件文件名不带后缀
	 * @param @param strFile
	 */
	public static String getFileName(String strFile) {
		String strFileName = null;

		try {
			if (strFile != null && strFile.length() > 0) {
				int iStart = strFile.lastIndexOf('/');
				if (iStart != 0 && iStart < strFile.length()) {
					iStart++;
				}
				int iEnd = strFile.lastIndexOf('.');
				if (iEnd > strFile.length() || iEnd <= iStart) {
					iEnd = strFile.length();
				}
				strFileName = strFile.substring(iStart, iEnd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strFileName;
	}
	
	/**
	 * @Title: getFileName
	 * @Description: 获取文件文件名带后缀
	 * @param @param strFile
	 */
	public static String getFileNameEx(String strFile) {
		String strFileName = null;

		try {
			if (strFile != null && strFile.length() > 0) {
				int iStart = strFile.lastIndexOf('/');
				if (iStart != 0 && iStart < strFile.length()) {
					iStart++;
				}
				strFileName = strFile.substring(iStart, strFile.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strFileName;
	}
	
	/**
	 * @Title: getParentDir
	 * @Description: 获取当前文件所在文件夹目录
	 * @param @param strPath
	 */
	public static String getParentDir(String strFile) {
		return strFile.substring(0, strFile.lastIndexOf('/'));
	}
	
	/**
	 * @Title: getFileType
	 * @Description: 获取当前文件的类型
	 * @param @param strFile
	 */
	public static int getMimeType(String strFile) {
		return MediaFile.getFileTypeForMimeType(MediaFile.getMimeTypeForFile(strFile));
	}
	
	public static int getMediaType(int iMimeType) {
		if (MediaFile.isAudioFileType(iMimeType)) {
			return 1;
		} else if (MediaFile.isVideoFileType(iMimeType)) {
			return 2;
		} else if (MediaFile.isImageFileType(iMimeType)) {
			return 3;
		}
		return 0;
	}
	
	public static int getMediaType(String strFile) {
		return getMediaType(getMimeType(strFile));
	}
}
