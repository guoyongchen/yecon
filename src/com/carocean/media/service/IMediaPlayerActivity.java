package com.carocean.media.service;

import java.util.ArrayList;

public interface IMediaPlayerActivity {
	
	// 通知UI更新列表
	public void updateListData(int iList, int iFreshAll) throws android.os.RemoteException;

	// 通知UI服务状态
	public void updateServiceStatus(int iStatus, java.lang.String strData) throws android.os.RemoteException;
	
	// 刷新播放状态
	public void updatePlayStatus(int iStatus) throws android.os.RemoteException;
	
	// 更新播放时间
	public void updateTimeProcess(int iProgress, int iDuration) throws android.os.RemoteException;
	
	// 更新媒体信息
	public void updateMediaInfo(int iInfo, int iExtra) throws android.os.RemoteException;

	// 刷新重复状态
	public void updateRepeatStatus(int iStatus) throws android.os.RemoteException;
	
	// 刷新随机状态
	public void updateRandomStatus(int iStatus) throws android.os.RemoteException;
	
	// 刷新播放类型
	public void updateMediaType(int iStatus) throws android.os.RemoteException;
	
	// 刷新播放索引
	public void updatePlayIndex(int iPos) throws android.os.RemoteException;
	
	// 更新音乐歌词列表
	public void updateLrcList(ArrayList<LrcContent> list, int iStatus);
	
	// 更新音乐歌词索引
	public void updateLrcIndex(int iStatus, int iIndex);
	// 更新拷贝状态
	public void updateDowndLoadState(int iState);
}
