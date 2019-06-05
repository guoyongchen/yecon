package com.carocean.media.service;

import java.util.ArrayList;
import java.util.List;

import com.carocean.media.scan.MediaObject;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

public interface IMediaPlayerService {
	
	// 注册源
	public void RegisterSource();
	
	// 注销源
	public void UnregisterSource();
	
	// 注册界面
	public void registerUserInterface(IMediaPlayerActivity mpi) throws android.os.RemoteException;
	
	// 注销界面
	public void unregisterUserInterface(IMediaPlayerActivity mpi) throws android.os.RemoteException;
	
	// 绑定播放磁盘及播放媒体类型
	public boolean requestAttachStorage(String strPath, int iRecoverType) throws android.os.RemoteException;
	
	// 切换重复状态带参数
	public void requestRepeat(int iState) throws android.os.RemoteException;
	
	// 请求获取磁盘所有的文件列表
	public void requestFileList(int iListType) throws android.os.RemoteException;
	
	// 请求获取指定目录下的文件
	public void requestDirList(boolean bAsync, int iDirPos, int mediaType) throws android.os.RemoteException;
	
	public void requestArtistList(boolean bAsync, int iAritstPos) throws android.os.RemoteException;
	
	public void requestAlbumList(boolean bAsync, int iAlbumPos) throws android.os.RemoteException;
	
	// 请求保存lastmemory
	public void requestSaveLastMemory() throws android.os.RemoteException;
	
	// 请求播放
	public void requestPlayList(int mediaType, int iListType, int iPos, String path) throws android.os.RemoteException;
	
	// 请求暂停
	public void requestPause() throws android.os.RemoteException;
	
	// 请求停止
	public void requestStop();
	
	// 请求显示隐藏前排视频
	public void requestSetFrontDisplay(SurfaceHolder holder) throws android.os.RemoteException;
	
	// 请求显示隐藏后排视频
	public void requestSetRearDisplay(SurfaceHolder holder) throws android.os.RemoteException;
	
	// 请求seek
	public void requestSeek(int lPos) throws android.os.RemoteException;
	
	// 请求下一曲
	public void requestNext() throws android.os.RemoteException;
	
	// 请求上一曲
	public void requestPrev() throws android.os.RemoteException;
	
	// 获取播放状态
	public int getPlayStatus() throws android.os.RemoteException;
	
	// 获取重复状态
	public int getRepeatStatus() throws android.os.RemoteException;
	
	// 获取多媒体类型
	public int getMediaType() throws android.os.RemoteException;
	
	// 获取磁盘列表
	public java.util.List<MediaStorage> getStorageList() throws android.os.RemoteException;
	
	// 获取当前播放磁盘信息
	public MediaStorage getPlayingStorage() throws android.os.RemoteException;
	
	// 获取当前播放列表
	public List<MediaObject> getPlayList() throws android.os.RemoteException;
	
	// 获取磁盘列表
	public List<MediaObject> getFileList(int iListType) throws android.os.RemoteException;
	
	// 获取对应媒体的列表类型
	public int getListType(int iMediaType) throws android.os.RemoteException;
	
	// 获取对应媒体的播放列表类型
	public int getPlayType(int iMediaType) throws android.os.RemoteException;
	
	public List<MediaObject> getImageList() throws android.os.RemoteException;
	
	// 获取当前正在播放文件信息
	public MediaTrackInfo getPlayingFileInfo() throws android.os.RemoteException;
	
	// 解析歌曲的专辑图片
	public Bitmap parseApicData(String path, int iWidth, int iHeight) throws android.os.RemoteException;
	
	// 获取文件索引
	public int getFilePos(int iMediaType) throws android.os.RemoteException;
	
	// 获取检索类型的索引：文件夹、艺术家、专辑
	public int getSearchPos(int iListType) throws android.os.RemoteException;
	
	// 获取文件名字
	public String getFileName(int iMediaType); 
	
	// 请求切换到图片播放
	public void requestPlayImage(int iPos) throws android.os.RemoteException;
	
	// 获取当前歌词索引
	public int getLrcIndex() throws android.os.RemoteException;
	
	// 获取当前歌词列表
	public ArrayList<LrcContent> getLrcList() throws android.os.RemoteException;
	
	// 播放指定歌曲
	public void requestPlayFile(String strPath) throws android.os.RemoteException;
	
	// 视频进入前台或者倒车结束时调用  ，语音时倒车/通话时倒车/倒车时通话等等  结束后强制恢复视频的播放。
	public void requestVideoPlay();
	
	// 更新不支持文件状态
	public boolean updateDamageState(String strPath, boolean bDamage);
	
	public void deleteFile(List<String> strPath, int iListType);
	
	public void deleteFile(int iPos, int iMediaType);
	
	public void requestRecover(int iMediaType);
	
	public void downloadFile(String strPath);
}