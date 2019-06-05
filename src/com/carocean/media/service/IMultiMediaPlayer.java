package com.carocean.media.service;

import java.util.ArrayList;

public interface IMultiMediaPlayer {
	void updatePlayState();
	void updatePlayProgress(int iProgress, int iDuration);
	void updateMediaInfo(int iInfo, int iExtra);
	void requestAudioFocus();
	void updateLrcList(ArrayList<LrcContent> list, int iStatus);
	void updateLrcIndex(int iStatus, int iIndex);
	void updatePlayInfo(boolean bError, String strPath);
	boolean isAllowPlay();
	int getMediaPlayerType();
}