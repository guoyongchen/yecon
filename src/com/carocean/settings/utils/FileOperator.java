package com.carocean.settings.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.carocean.utils.sLog;

/**
 * @ClassName: FileOperator
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class FileOperator {
	private final static String Tag = "FileOperator";

	public static void searchFile(Map<String, List<String>> result, File dir, String name) {
		File file = dir;
		if (!file.exists() || name == null || name.isEmpty()) {
			return;
		}
		String otherName = file.getName();
		if (otherName.contains(name)) {
			String path = file.getPath();
			if (!result.containsKey(otherName)) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(path);
				result.put(otherName, list);
			} else {
				result.get(otherName).add(path);
			}
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				int size = files.length;
				for (int i = 0; i < size; ++i) {
					searchFile(result, files[i], name);
				}
			}
		}
	}

	public static boolean deleteFile(File file, IProgressListener listener) {
		if (file.exists()) {
			if (listener != null) {
				if (listener.onProgressUpdate(null, 0)) {
					return false;
				}
			}
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					sLog.d(Tag, "in file =" + f.toString());
					if (!deleteFile(f, listener)) {
						return false;
					}
					sLog.d(Tag, "delete file =" + f.toString());
				}
			}
			sLog.d(Tag, "final delete file =" + file.toString());
			boolean isFile = file.isFile();
			boolean result = file.delete();
			if (result && listener != null && isFile) {
				listener.onAddNewFile(null, file.getPath());
			}
			return result;
		}
		sLog.d(Tag, "delete file: no file exists");
		return true;
	}

	public static long getFileCount(String path) {
		File file = new File(path);
		long result = 0;
		if (file != null && file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					if (file.isDirectory()) {
						result += getFileCount(f.getPath());
					} else {
						result++;
					}
				}
			} else {
				result++;
			}
		}
		return result;
	}

	public static long getFilesTotalSpace(File[] paths, boolean isIncludeDir) {
		if (paths == null || paths.length == 0) {
			sLog.d("file space", "0");
			return 0;
		}
		long i = 0;
		for (File file : paths) {
			if (file != null && file.exists()) {
				if (file.isDirectory()) {
					if (isIncludeDir) {
						i += file.length();
					}
					File[] localfiles = file.listFiles();
					i += getFilesTotalSpace(localfiles, isIncludeDir);
				} else {
					i += file.length();
				}
			} else {
				// TODO:
				return 0;
			}
		}
		sLog.d(Tag, "i " + i);
		return i;
	}

	public interface IProgressListener {
		boolean onProgressUpdate(File file, long length);

		void onAddNewFile(String old, String path);
	}

	// TODO:file permission
	// TODO: on file paste operation failure
	// TODO: how to show failure dialog
	public static String pasteFile(String src, String dst, IProgressListener listener) {
		File to = new File(dst);
		File from = new File(src);
		boolean result = false;
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		FileChannel fromChannel = null;
		FileChannel toChannel = null;

		if (!to.isDirectory() || !to.exists() || !from.exists() || !to.canWrite()) {
			return null;
		}
		String fileName = from.getName();
		sLog.d(Tag, "pastefile filename=" + fileName);
		File file = new File(to, fileName);
		String name = FileUtil.getNameFromFilename(fileName);
		sLog.d(Tag, "pastefile name=" + name);
		String ext = FileUtil.getExtFromFilename(fileName);
		if (!ext.equals("")) {
			ext = "." + ext;
		}
		sLog.d(Tag, "pastefile ext=" + ext);
		int i = 1;

		if (file.exists()) {
			if ((file.getAbsolutePath()).compareToIgnoreCase(from.getAbsolutePath()) == 0) {
				fileName = name + "_" + String.valueOf((i++)) + ext;
				file = new File(to, fileName);
			}
		}

		sLog.d(Tag, "pastefile new filename=" + file.getAbsolutePath());
		try {
			if (from.isDirectory()) {
				if (!file.exists())
					file.mkdirs();
				sLog.d(Tag, "make a dir");
				result = true;
				String path = file.getPath();
				sLog.d(Tag, "file path is=" + path);
				File[] children = null;
				if (from.canRead() && from.canExecute()) {
					children = from.listFiles();
				} else {
					result = false;
				}
				for (File child : children) {
					sLog.d(Tag, "child file is" + child);
					if (pasteFile(child.getPath(), path, listener) == null) {
						result = false;
						break;
					}
				}
				sLog.d(Tag, "is a dir");
			} else {
				sLog.d(Tag, "copy file");
				inputStream = new FileInputStream(from);
				outputStream = new FileOutputStream(file);

				if (listener == null) {
					fromChannel = inputStream.getChannel();
					toChannel = outputStream.getChannel();
					if (from.length() == fromChannel.transferTo(0, fromChannel.size(), toChannel)) {
						sLog.d(Tag, "normal file transfer done");
						result = true;
					}
				} else {
					int count = 102400;
					byte[] buffer = new byte[count];
					int counter = 0;
					int read = 0;
					while ((read = inputStream.read(buffer, 0, count)) != -1) {
						counter += read;
						listener.onProgressUpdate(from, counter);
						outputStream.write(buffer, 0, read);
					}
					result = true;
					listener.onAddNewFile(from.getPath(), file.getPath());
				}
			}
		} catch (IOException e) {
			sLog.d(Tag, e.toString());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					// outputStream.flush();
					// outputStream.getFD().sync();
					outputStream.close();
				}
				if (fromChannel != null) {
					fromChannel.close();
				}
				if (toChannel != null) {
					toChannel.close();
				}
			} catch (IOException e) {
				sLog.d(Tag, e.toString());
			}
		}

		sLog.d(Tag, "paste result is" + (result ? "yes" : "no"));
		return result ? file.getPath() : null;
	}
}
