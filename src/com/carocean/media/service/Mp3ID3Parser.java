package com.carocean.media.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import com.carocean.media.scan.MediaFileParse;

import android.content.Context;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class Mp3ID3Parser {
	private final String TAG = getClass().getName();
	private String mAlbum;
	private String mArtist;
	private String mTitle;

	// 7位ASCII字符，也叫作ISO646-US、Unicode字符集的基本拉丁块
	public static final String US_ASCII = "US-ASCII";

	// ISO 拉丁字母表 No.1，也叫作 ISO-LATIN-1
	public static final String ISO_8859_1 = "ISO-8859-1";

	// 8 位 UCS 转换格式 */
	public static final String UTF_8 = "UTF-8";

	// 16 位 UCS 转换格式，Big Endian（最低地址存放高位字节）字节顺序
	public static final String UTF_16BE = "UTF-16BE";

	// 16 位 UCS 转换格式，Little-endian（最高地址存放高位字节）字节顺序
	public static final String UTF_16LE = "UTF-16LE";

	// 16 位 UCS 转换格式，字节顺序由可选的字节顺序标记来标识
	public static final String UTF_16 = "UTF-16";

	// 中文超大字符集
	public static final String GB2312 = "GB2312";

	public static final String GBK = "GBK";

	public static final String[] CHARSET = { GB2312, US_ASCII, ISO_8859_1, UTF_8, UTF_16BE, UTF_16LE, UTF_16 };

	public final String UNKNOWN = "unknown";
	private Context mContext = null;

	public Mp3ID3Parser(Context context) {
		mContext = context;
	}
	
	public boolean parseID3(String path) {
		boolean bRet = false;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			bRet = parseID3(raf);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bRet;
	}

	protected static final int MP3_TAG_V1_LENGTH = 128;
	protected static final int TAG_FILE_HEADER_SIZE = 10;
	protected static final int TAG_FRAME_HEADER_SIZE = 10;
	protected static final int MP3_TAG_V2_DEFAULT_TOTAL_SIZE = 1024 * 2;

	protected byte[] mMP3TagV2Header = new byte[TAG_FILE_HEADER_SIZE];
	protected byte[] mMP3TagV1Buffer = new byte[MP3_TAG_V1_LENGTH];
	protected byte[] mMP3TagV2Buffer = new byte[MP3_TAG_V2_DEFAULT_TOTAL_SIZE];

	protected int mMP3TagV2BufferTotalSize = MP3_TAG_V2_DEFAULT_TOTAL_SIZE;

	protected boolean getMP3Tag(RandomAccessFile raf) {
		boolean ret = false;

		if (getMP3TagV2(raf)) {
			ret = true;
		} else if (getMP3TagV1(raf)) {
			ret = true;
		}
		return ret;
	}

	private boolean getMP3TagV2(RandomAccessFile raf) {
		boolean ret = false;
		try {
			raf.read(mMP3TagV2Header);
			if (mMP3TagV2Header[0] == 73 && mMP3TagV2Header[1] == 68 && mMP3TagV2Header[2] == 51) { // "ID3"
				// Log.i(TAG, "getMP3TagV2 is ID3V2");
				ret = true;
			} else {
				return ret;
			}

			long length = raf.length();

			int totalSize = (mMP3TagV2Header[6] & 0x7F) * 0x200000 + (mMP3TagV2Header[7] & 0x7F) * 0x4000
					+ (mMP3TagV2Header[8] & 0x7F) * 0x80 + (mMP3TagV2Header[9] & 0x7F);
			if (totalSize < 0 || totalSize > length) {
				if (raf != null) {
					raf.close();
				}
				return ret;
			}

			raf.seek(TAG_FILE_HEADER_SIZE);
			raf.read(mMP3TagV2Buffer, 0, MP3_TAG_V2_DEFAULT_TOTAL_SIZE);

			int offset = 0;
			int frameSize = 0;
			int stopFlag = 0;
			String language = mContext.getResources().getConfiguration().locale.getLanguage();
			while (offset + TAG_FRAME_HEADER_SIZE < MP3_TAG_V2_DEFAULT_TOTAL_SIZE) {
				if ((stopFlag & 0x07) == 0x07) {
					break;
				}
				frameSize = mMP3TagV2Buffer[offset + 4] * 0x1000000 + mMP3TagV2Buffer[offset + 5] * 0x10000
						+ mMP3TagV2Buffer[offset + 6] * 0x100 + mMP3TagV2Buffer[offset + 7];
				if (frameSize <= 0 || frameSize > MP3_TAG_V2_DEFAULT_TOTAL_SIZE - offset - TAG_FRAME_HEADER_SIZE) {
					break;
				}

				String frame = "";
				int tempOffset = offset + TAG_FRAME_HEADER_SIZE;
				if (mMP3TagV2Buffer[tempOffset] == 0) { // ISO-8859-1
					String strCode = GB2312;
					if (language.endsWith("es") || language.endsWith("fr") || language.endsWith("pt")
							|| language.endsWith("it") || language.endsWith("ru") || language.endsWith("ar")) {
						strCode = ISO_8859_1;
					} else if (language.endsWith("ja")) {
						strCode = GBK;
					}
					frame = new String(mMP3TagV2Buffer, tempOffset + 1, frameSize - 1, strCode);
				} else if (mMP3TagV2Buffer[tempOffset] == 1) { // UTF-16
					if (mMP3TagV2Buffer[tempOffset + 1] == (byte)0xff && mMP3TagV2Buffer[tempOffset + 2] == (byte)0xfe) {
						frame = new String(mMP3TagV2Buffer, tempOffset + 3, frameSize - 3, UTF_16LE);
					} else if (mMP3TagV2Buffer[tempOffset + 1] == (byte)0xfe && mMP3TagV2Buffer[tempOffset + 2] == (byte)0xff) {
						frame = new String(mMP3TagV2Buffer, tempOffset + 3, frameSize - 3, UTF_16BE);
					} else {
						frame = new String(mMP3TagV2Buffer, tempOffset + 1, frameSize - 1, UTF_16LE);
					}
				} else if (mMP3TagV2Buffer[tempOffset] == 2) {
					frame = new String(mMP3TagV2Buffer, tempOffset + 1, frameSize - 1, UTF_16);
				} else if (mMP3TagV2Buffer[tempOffset] == 3) { // UTF-8
					frame = new String(mMP3TagV2Buffer, tempOffset + 1, frameSize - 1, UTF_8);
				}

				if (mMP3TagV2Buffer[offset] == 84 && mMP3TagV2Buffer[offset + 1] == 73
						&& mMP3TagV2Buffer[offset + 2] == 84 && mMP3TagV2Buffer[offset + 3] == 50) { // "TIT2"
																										// -
																										// title
					stopFlag |= 0x01;
					handleStringTag(MediaStore.MediaColumns.TITLE, frame);
				} else if (mMP3TagV2Buffer[offset] == 84 && mMP3TagV2Buffer[offset + 1] == 80
						&& mMP3TagV2Buffer[offset + 2] == 69 && mMP3TagV2Buffer[offset + 3] == 49) { // "TPE1"
																										// -
																										// artist
					stopFlag |= 0x02;
					handleStringTag(Audio.Media.ARTIST, frame);
				} else if (mMP3TagV2Buffer[offset] == 84 && mMP3TagV2Buffer[offset + 1] == 65
						&& mMP3TagV2Buffer[offset + 2] == 76 && mMP3TagV2Buffer[offset + 3] == 66) { // "TALB"
																										// -
																										// album
					stopFlag |= 0x04;
					handleStringTag(Audio.Media.ALBUM, frame);
				}

				offset += frameSize + TAG_FRAME_HEADER_SIZE;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private boolean getMP3TagV1(RandomAccessFile raf) {
		try {
			if (raf.length() < MP3_TAG_V1_LENGTH) {
				return false;
			}
			raf.seek(raf.length() - MP3_TAG_V1_LENGTH);
			raf.read(mMP3TagV1Buffer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mMP3TagV1Buffer.length != MP3_TAG_V1_LENGTH) {
			return false;
		} else {
			try {
				if (!(mMP3TagV1Buffer[0] == 84 && mMP3TagV1Buffer[1] == 65 && mMP3TagV1Buffer[2] == 71)) { // "TAG"
					return false;
				}
				String language = mContext.getResources().getConfiguration().locale.getLanguage();
				String strCode = GB2312;
				if (language.endsWith("es") || language.endsWith("fr") || language.endsWith("pt")
						|| language.endsWith("it") || language.endsWith("ru") || language.endsWith("ar")) {
					strCode = ISO_8859_1;
				} else if (language.endsWith("ja")) {
					strCode = GBK;
				}
				String title = new String(mMP3TagV1Buffer, 3, 30, strCode).trim();
				String artist = new String(mMP3TagV1Buffer, 33, 30, strCode).trim();
				String album = new String(mMP3TagV1Buffer, 63, 30, strCode).trim();

				handleStringTag(MediaStore.MediaColumns.TITLE, title);
				handleStringTag(Audio.Media.ARTIST, artist);
				handleStringTag(Audio.Media.ALBUM, album);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	protected void handleStringTag(String name, String value) {
		if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
			mTitle = value.trim();
		} else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
			mArtist = value.trim();
		} else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
			mAlbum = value.trim();
		}
	}

	private boolean parseID3(RandomAccessFile raf) {
		return getMP3Tag(raf);
	}

	/**
	 * @Title: getFileSuffix
	 * @Description: 获取文件后缀名
	 * @param @param
	 *            strFile
	 */
	public String getFileSuffix(String strFile) {
		String strSuffix = null;
		try {
			if (strFile != null && strFile.length() > 0 && strFile.contains(".")) {
				strSuffix = strFile.substring(strFile.lastIndexOf(".") + 1, strFile.length());
				strSuffix = strSuffix.toLowerCase();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return strSuffix;
	}

	/**
	 * @Title: AnalysisID3
	 * @Description: 解析ID3
	 */
	public boolean AnalysisID3(String strFile) {
		boolean bRet = false;
		mAlbum = "";
		mArtist = "";
		mTitle = "";
		try {
			String strSuffix = getFileSuffix(strFile);
			if (strSuffix != null) {
				if (strSuffix.toLowerCase().equals("mp3")) {
					bRet = parseID3(strFile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mAlbum == null || mAlbum.equals("")) {
			mAlbum = UNKNOWN;
		}
		if (mArtist == null || mArtist.equals("")) {
			mArtist = UNKNOWN;
		}
		if (mTitle == null || mTitle.equals("")) {
			mTitle = MediaFileParse.getFileNameEx(strFile);
		}
		Log.i(TAG, "mAlbum:" + mAlbum + ", mArtist:" + mArtist + ", mTitle:" + mTitle);
		return bRet;
	}

	public String getAlbum() {
		return (mAlbum == null || mAlbum.equals("")) ? UNKNOWN : mAlbum;
	}

	public String getArtist() {
		return (mArtist == null || mAlbum.equals("")) ? UNKNOWN : mArtist;
	}

	public String getTitle() {
		return mTitle;
	}
}
