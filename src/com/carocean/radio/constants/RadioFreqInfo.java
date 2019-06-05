package com.carocean.radio.constants;

public class RadioFreqInfo {

	private int mFreq;
	private int mBand;
	private int mFavorite;
	private int mPreset;
	
	public RadioFreqInfo(int freq, int band, int favorite, int preset) {
		mFreq = freq;
		mBand = band;
		mFavorite = favorite;
		mPreset = preset;
	}
	
	public int getFreq() {
		return mFreq;
	}
	
	public int getBand() {
		return mBand;
	}
	
	public int getFavorite() {
		return mFavorite;
	}
	
	public int getPreset() {
		return mPreset;
	}
	
	public void setFreq(int freq) {
		mFreq = freq;
	}
	
	public void setBand(int band) {
		mBand = band;
	}
	
	public void setFavorite(int favorite) {
		mFavorite = favorite;
	}
	
	public void setPreset(int preset) {
		mPreset = preset;
	}
}
