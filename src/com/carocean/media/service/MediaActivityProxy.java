package com.carocean.media.service;


import java.util.ArrayList;
import java.util.List;

import com.carocean.ApplicationManage;
import com.carocean.media.constants.MediaPlayerContants;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.utils.Utils;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class MediaActivityProxy implements IMediaPlayerActivity {

	private final String TAG = getClass().getName();

	// 该代理实际代理的界面
	private List<Handler> mListUIHandler;
	private int miStatus = -1;
	// 媒体播放远程服务端
	private MediaPlayerService mPlayerService;

	// 当前进程相关的context
	private Context mContext;
	// 当前进程所播放的磁盘
	private String mStorage;

	private static class SingletonHolder {
		public static MediaActivityProxy instance = new MediaActivityProxy();
	}

	public static MediaActivityProxy getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * 创建一个新的实例 MediaPlayerActivityProxy.
	 */
	private MediaActivityProxy() {
		Log.i(TAG, "MediaActivityProxy()");
		mListUIHandler = new ArrayList<Handler>();
	}

	private ServiceConnection mMediaPlayerService = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mPlayerService = null;
			Log.e(TAG, "MediaPlayerService onServiceDisconnected");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service != null) {
				mPlayerService = (MediaPlayerService) service;
				try {
					mPlayerService.registerUserInterface(MediaActivityProxy.this);
					if (mListUIHandler != null && mListUIHandler.size() > 0) {
						// 如果拉起服务时，已经有UI监听注册了，说明服务是由UI拉起来的（应用重启或者单独调试才会走此流程）
						Log.i(TAG, "onServiceConnected  mListUIHandler.size() > 0");
						notifiConnect();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private void init(Context context) {
		mContext = context;
	}

	// 绑定到服务
	public void bindMediaService(Context context) {
		init(context);
		
		if (mContext != null) {
			// 连接到远程媒体播放服务
			if (mPlayerService == null) {
				Intent intentService = new Intent();
				intentService.setClass(mContext, MediaPlayerServiceProxy.class);
				if (mContext.bindService(intentService, mMediaPlayerService, Context.BIND_AUTO_CREATE)) {
					Log.e(TAG, "Bind MediaPlayService Success!");
				} else {
					Log.e(TAG, "Bind MediaPlayService Failed!");
				}
			} else {
				Log.e(TAG, "Application Has been Binded MediaPlayService!!!");
			}
		}
	}

	// 释放服务
	public void unbindMediaService() {
		if (mContext != null) {
			if (isBindPlayerService()) {
				try {
					mPlayerService.unregisterUserInterface(this);
					mPlayerService.UnregisterSource();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mContext.unbindService(mMediaPlayerService);
			mPlayerService = null;
		}
	}

	// 当界面创建的时候,注册handler到代理
	public void RegisterHandler(Handler handler) {
		if (!mListUIHandler.contains(handler)) {
			mListUIHandler.add(handler);
		}
	}

	// 当界面注销的时候,从代理中注销handler
	public void UnRegisterHandler(Handler handler) {
		if (mListUIHandler.contains(handler)) {
			mListUIHandler.remove(handler);
		}
	}

	@Override
	public void updatePlayStatus(int iStatus) throws RemoteException {
//		if (miStatus != iStatus) {
			miStatus = iStatus;
			for (Handler handler : mListUIHandler) {
				Message msg = Message.obtain();
				msg.what = MediaPlayerMessage.UPDATE_PLAY_STATE;
				msg.arg1 = iStatus;
				handler.sendMessage(msg);
			}
//		}
	}
	
	@Override
	public void updateRepeatStatus(int iStatus) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_REPEAT_STATE;
			msg.arg1 = iStatus;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateRandomStatus(int iStatus) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_RANDOM_STATE;
			msg.arg1 = iStatus;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateServiceStatus(int iStatus, String strData) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_SERVICE_STATE;
			msg.arg1 = iStatus;
			if (strData != null && !strData.isEmpty()) {
				Bundle data = new Bundle();
				data.putString(MediaPlayerContants.PATH, strData);
				msg.setData(data);
			}
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateListData(int iList, int iFreshAll) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_LIST_DATA;
			msg.arg1 = iList;
			msg.arg2 = iFreshAll;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateTimeProcess(int iProgress, int iDuration) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_PLAY_PROGRESS;
			msg.arg1 = iProgress;
			msg.arg2 = iDuration;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateMediaInfo(int iInfo, int iExtra) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_MEDIA_INTO;
			msg.arg1 = iInfo;
			msg.arg2 = iExtra;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void updateMediaType(int iStatus) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_MEDIA_TYPE;
			msg.arg1 = iStatus;
			handler.sendMessage(msg);
		}
	}
	
	@Override
	public void updatePlayIndex(int iPos) throws RemoteException {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_PLAY_INDEX;
			msg.arg1 = iPos;
			handler.sendMessage(msg);
		}
	}

	// 绑定磁盘
	public boolean AttachStorage(String storage, int imediaType) {
		mStorage = storage;
		if (isBindPlayerService()) {
			try {
				return mPlayerService.requestAttachStorage(mStorage, imediaType);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static IMediaPlayerService getService() {
		return getInstance().getMediaPlayerService();
	}
	
	public static boolean isBindService() {
		return getInstance().isBindPlayerService();
	}
	
	// 获取远程媒体播放服务
	private IMediaPlayerService getMediaPlayerService() {
		if (mPlayerService == null) {
			Utils.initMedia();
		}
		return mPlayerService;
	}

	private boolean isBindPlayerService() {
		if (mPlayerService == null) {
			Utils.initMedia();
		}
		return (mPlayerService != null);
	}

	// 退出当前APP
	public void exitApp() {
//			mContext.unregisterReceiver(mReceiver);
//			mAudioManager.unregisterMediaButtonEventReceiver(mbCN);
//			if (isBindPlayerService()) {
//				try {
//					mService.requestSaveLastMemory();
//					mService.UnregisterSource();
//					unbindMediaService();
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//			mListUIHandler.clear();
	}

	public static String formatData(int iDuration, boolean bVideo) {
		if (bVideo || iDuration >= 3600) {
			return String.format("%02d:%02d:%02d", iDuration / 60 / 60, iDuration / 60 % 60, iDuration % 60);
		} else {
			return String.format("%02d:%02d", iDuration / 60 % 60, iDuration % 60);
		}
	}

	public boolean isScaningAttachedDevice() {
		boolean bRet = false;
		MediaStorage storage;
		try {
			if (isBindPlayerService()) {
				storage = getMediaPlayerService().getPlayingStorage();
				if (storage != null) {
					if (storage.getState().equals(MediaScanConstans.ACTION_SCAN_START)) {
						bRet = true;
					}
				} else {
					bRet = true;
				}
			} else {
				bRet = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bRet;
	}

	@Override
	public void updateLrcList(ArrayList<LrcContent> list, int iStatus) {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_LRC_LIST;
			msg.arg1 = iStatus;
			msg.obj = list;
			handler.sendMessage(msg);
		}
	}
	
	@Override
	public void updateLrcIndex(int iStatus, int iIndex) {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_LRC_INDEX;
			msg.arg1 = iStatus;
			msg.arg2 = iIndex;
			handler.sendMessage(msg);
		}
	}
	
	@Override
	public void updateDowndLoadState(int iState) {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_DWONLOAD_STATE;
			msg.arg1 = iState;
			handler.sendMessage(msg);
		}
	}
	
	private void notifiConnect() {
		for (Handler handler : mListUIHandler) {
			Message msg = Message.obtain();
			msg.what = MediaPlayerMessage.UPDATE_BIND_SUCCESS;
			handler.sendMessage(msg);
		}
	}
}
