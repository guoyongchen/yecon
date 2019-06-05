package com.carocean.radio.view;

public interface RadioViewCallBack {

	public void nextProgress(int freq);
	
	public void prevProgress(int freq);
	
	public void resetProgress();
}
