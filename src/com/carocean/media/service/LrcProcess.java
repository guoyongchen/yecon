package com.carocean.media.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LrcProcess {
	private final String TAG = "LrcProcess";
	private final int BYTE_SIZE = 8;  
	private ArrayList<LrcContent> lrcList; // List集合存放歌词内容对象
	private LrcContent mLrcContent; // 声明一个歌词内容对象
	private int miCurIndex = 0; // 歌词检索值
	// 时间
	private long currentTime;
	// 对应时间的内容
	private String currentContent;

	/**
	 * 无参构造函数用来实例化对象
	 */
	public LrcProcess() {
		lrcList = new ArrayList<LrcContent>();
	}

	/**
	 * 读取歌词
	 * 
	 * @param path
	 * @return
	 */
	public int readLRC(String path) {
		// 定义一个StringBuilder对象，用来存放歌词内容
		int iIndex = 0;
		String strPath = "";
		lrcList.clear();
		if (path != null) {
			int iLastIndex = path.lastIndexOf(".");
			if (iLastIndex > 0) {
				strPath = path.substring(0, iLastIndex + 1) + "lrc";
				File f = new File(strPath);
				if (f.exists()) {
					FileInputStream fis = null;
					BufferedInputStream bis = null;
					BufferedReader reader = null;
					try {
						fis = new FileInputStream(f);
						bis = new BufferedInputStream(fis);
						bis.mark(4);
						byte[] first3bytes = new byte[3];
						// 找到文档的前三个字节并自动判断文档类型。
						bis.read(first3bytes);
						Log.i(TAG, "readLRC path:" + path);
						bis.reset();
						String charSet = "GBK";
						if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
								&& first3bytes[2] == (byte) 0xBF) {// utf-8
							charSet = "utf-8";
						} else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
							charSet = "utf-16le";
						} else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
							charSet = "utf-16be";
						} else {
							try {
								if (isUTF8(bis)) {
									charSet = "utf-8";
								} 
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Log.i(TAG, "readLRC charSet:" + charSet);
						bis.reset();
						reader = new BufferedReader(new InputStreamReader(bis, charSet));
						String str = null;
						// 逐行解析
						while ((str = reader.readLine()) != null) {
							if (!str.equals("")) {
								decodeLine(str);
							}
						}
						// 全部解析完后，设置lrcLists
						fis.close();
						bis.close();
						reader.close();
						iIndex = 1;
					} catch (IOException e) {
						e.printStackTrace();
						iIndex = 2;
					} catch (NumberFormatException e) {
						e.printStackTrace();
						iIndex = 3;
					}
				}
			}
		}
		return iIndex;
	}

	
	/** 
     * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK 
     * 
     * @param bis 
     * @return 
     */  
    private boolean isUTF8(BufferedInputStream bis) throws Exception {  
        bis.reset();  
  
        //读取第一个字节  
        int code = bis.read();  
        do {  
            BitSet bitSet = convert2BitSet(code);  
            //判断是否为单字节  
            if (bitSet.get(0)) {//多字节时，再读取N个字节  
                if (!checkMultiByte(bis, bitSet)) {//未检测通过,直接返回  
                    return false;  
                }  
            } else {  
                //单字节时什么都不用做，再次读取字节  
            }  
            code = bis.read();  
        } while (code != -1);  
        return true;  
    }  
  
    /** 
     * 将整形转为BitSet 
     * 
     * @param code 
     * @return 
     */  
    private BitSet convert2BitSet(int code) {  
        BitSet bitSet = new BitSet(BYTE_SIZE);  
  
        for (int i = 0; i < BYTE_SIZE; i++) {  
            int tmp3 = code >> (BYTE_SIZE - i - 1);  
            int tmp2 = 0x1 & tmp3;  
            if (tmp2 == 1) {  
                bitSet.set(i);  
            }  
        }  
        return bitSet;  
    } 
    
    /** 
     * 检测多字节，判断是否为utf8，已经读取了一个字节 
     * 
     * @param bis 
     * @param bitSet 
     * @return 
     */  
    private  boolean checkMultiByte(BufferedInputStream bis, BitSet bitSet) throws Exception {  
        int count = getCountOfSequential(bitSet);  
        byte[] bytes = new byte[count - 1];//已经读取了一个字节，不能再读取  
        bis.read(bytes);  
        for (byte b : bytes) {  
            if (!checkUtf8Byte(b)) {  
                return false;  
            }  
        }  
        return true;  
    }  
  
    /** 
     * 检测单字节，判断是否为utf8 
     * 
     * @param b 
     * @return 
     */  
    private boolean checkUtf8Byte(byte b) throws Exception {  
        BitSet bitSet = convert2BitSet(b);  
        return bitSet.get(0) && !bitSet.get(1);  
    }  
  
    /** 
     * 检测bitSet中从开始有多少个连续的1 
     * 
     * @param bitSet 
     * @return 
     */  
    private int getCountOfSequential(BitSet bitSet) {  
        int count = 0;  
        for (int i = 0; i < BYTE_SIZE; i++) {  
            if (bitSet.get(i)) {  
                count++;  
            } else {  
                break;  
            }  
        }  
        return count;  
    }
    
	/**
	 * 单行解析
	 */
	private void decodeLine(String str) {
		if (str.startsWith("[ti:")) {
			// 歌曲名
			// lrcInfo.setTitle(str.substring(4, str.lastIndexOf("]")));
		} else if (str.startsWith("[ar:")) {// 艺术家
			// lrcInfo.setArtist(str.substring(4, str.lastIndexOf("]")));
		} else if (str.startsWith("[al:")) {// 专辑
			// lrcInfo.setAlbum(str.substring(4, str.lastIndexOf("]")));
		} else if (str.startsWith("[by:")) {// 作词
			// lrcInfo.setBySomeBody(str.substring(4, str.lastIndexOf("]")));

		} else if (str.startsWith("[la:")) {// 语言
			// lrcInfo.setLanguage(str.substring(4, str.lastIndexOf("]")));
		} else {

			// 设置正则表达式，可能出现一些特殊的情况
			String timeflag = "\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,2})\\]|\\[(\\d{1,2}:\\d{1,2})\\]";

			Pattern pattern = Pattern.compile(timeflag);
			Matcher matcher = pattern.matcher(str);
			// 如果存在匹配项则执行如下操作
			while (matcher.find()) {
				// // 得到匹配的内容
				// String msg = matcher.group();
				// // 得到这个匹配项开始的索引
				// int start = matcher.start();
				// // 得到这个匹配项结束的索引
				// int end = matcher.end();
				// 得到这个匹配项中的数组
				int groupCount = matcher.groupCount();
				for (int index = 0; index < groupCount; index++) {
					String timeStr = matcher.group(index);
					if (index == 0) {
						// 将第二组中的内容设置为当前的一个时间点
						currentTime = str2Long(timeStr.substring(1, timeStr.length() - 1));
					}
				}

				// 得到时间点后的内容
				String[] content = pattern.split(str);

				// 将内容设置为当前内容，需要判断只出现时间的情况，没有内容的情况
				if (content.length == 0) {
					currentContent = "";
				} else {
					currentContent = content[content.length - 1];
				}
				// 设置时间点和内容的映射
				mLrcContent = new LrcContent();
				mLrcContent.setLrcTime(currentTime);
				mLrcContent.setLrcStr(currentContent);
				lrcList.add(mLrcContent);
				Collections.sort(lrcList);
			}
		}
	}

	/**
	 * 解析歌词时间 歌词内容格式如下： [00:02.32]陈奕迅 [00:03.43]好久不见 [00:05.22]歌词制作 王涛
	 * 
	 * @param timeStr
	 * @return
	 */
	private long str2Long(String timeStr) {
		// 将时间格式为xx:xx.xx，返回的long要求以毫秒为单位
		String[] s = timeStr.split("\\:");
		int min = Integer.parseInt(s[0]);
		int sec = 0;
		int mill = 0;
		if (s[1].contains(".")) {
			String[] ss = s[1].split("\\.");
			sec = Integer.parseInt(ss[0]);
			mill = Integer.parseInt(ss[1]);
		} else {
			sec = Integer.parseInt(s[1]);
		}
		// 时间的组成
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

	public ArrayList<LrcContent> getLrcList() {
		return lrcList;
	}

	public boolean getLrcIndexChanged(int iCurTime, int iTotalTime) {
		int iIndex = 0;
		if (iCurTime <= iTotalTime) {
			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (iCurTime < lrcList.get(i).getLrcTime() && i == 0) {
						iIndex = i;
					}
					if (iCurTime > lrcList.get(i).getLrcTime() && iCurTime < lrcList.get(i + 1).getLrcTime()) {
						iIndex = i;
					}
				}
				if (i == lrcList.size() - 1 && iCurTime > lrcList.get(i).getLrcTime()) {
					iIndex = i;
				}
			}
		}
		if (iIndex != miCurIndex) {
			miCurIndex = iIndex;
			return true;
		} else {
			return false;
		}
	}

	public int getCurIndex() {
		return miCurIndex;
	}

	public String getLrcStr() {
		if (lrcList != null && lrcList.size() > miCurIndex) {
			return lrcList.get(miCurIndex).getLrcStr();
		} else {
			return "";
		}
	}
}
