package com.carocean.media.service;

public class MediaStorage {
	// 磁盘路径
	private String mstrPath;
	// 磁盘状态 
	private String mstrState;
	// 播放状态
	private int mbAttach = 0;
	// recover状态
	private boolean mbRecover = false;
	
	public MediaStorage() {
		
	}
	
	public MediaStorage(String path, String state) {
		mstrPath = path;
		mstrState = state;
	}
	
	public boolean getPlaying() {
		return (mbAttach != 0);
	}
	
	public void setPlaying(boolean bplaying) {
		mbAttach = bplaying ? 1 : 0;
	}
	
	public boolean getRecover() {
		return mbRecover;
	}
	
	public void setRecover(boolean bRecover) {
		mbRecover = bRecover;
	}
	
	public String getPath() {
		return mstrPath;
	}

	public void setPath(String strPath) {
		this.mstrPath = strPath;
	}

	public String getState() {
		return mstrState;
	}

	public void setState(String strState) {
		this.mstrState = strState;
	}
}
