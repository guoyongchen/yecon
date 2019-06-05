package com.carocean.media.service;

public class LrcContent implements Comparable<LrcContent>{
	private String lrcStr; // 歌词内容
	private long lrcTime; // 歌词当前时间

	public String getLrcStr() {
		return lrcStr;
	}

	public void setLrcStr(String lrcStr) {
		this.lrcStr = lrcStr;
	}

	public long getLrcTime() {
		return lrcTime;
	}

	public void setLrcTime(long lrcTime) {
		this.lrcTime = lrcTime;
	}

	@Override
	public int compareTo(LrcContent another) {
//        int i = lrcStr.compareTo(another.lrcStr); //比较名字字符串  
//        if (i == 0) { //如果名字一样，则继续比较年龄  
           return (int) (lrcTime - another.lrcTime);  
//        } else { //首先比较名字，名字不一样，则返回比较结果  
//            return i;  
//        } 
	}
}
