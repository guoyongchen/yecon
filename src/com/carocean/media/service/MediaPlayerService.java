package com.carocean.media.service;

import static android.mcu.McuExternalConstant.*;

import android.annotation.SuppressLint;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.constant.YeconConstants;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;

import com.autochips.storage.EnvironmentATC;
import com.carocean.ApplicationManage;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.media.constants.MediaPlayerContants;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.constants.MediaPlayerContants.RepeatMode;
import com.carocean.media.constants.MediaPlayerContants.ServiceStatus;
import com.carocean.media.constants.MediaScanConstans.YeconMediaAlbumColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaArtistColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaDirColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaFilesColumns;
import com.carocean.media.scan.MediaObject;
import com.carocean.service.BootService;
import com.carocean.utils.Constants;
import com.carocean.utils.DataShared;
import com.carocean.utils.SourceManager;
import com.carocean.utils.Utils;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;
import com.yecon.savedata.SaveData;

@SuppressLint("NewApi")
public class MediaPlayerService extends Binder
		implements IMediaPlayerService, IMediaListQueryHandler, IMultiMediaPlayer, Callback {
	private String TAG = MediaPlayerService.class.getSimpleName();
	public static String VERSION = "unknown";

	private int DEFAULT_APIC_H = 200;
	private int DEFAULT_APIC_W = 200;

	public final String ACTION_MEDIA_RANDOM = "action.media.random";
	public final String ACTION_MEDIA_REPEAT = "action.media.repeat";
	public final String ACTION_QB_POWERON = "autochips.intent.action.QB_POWERON";
	public final String ACTION_QB_POWEROFF = "autochips.intent.action.QB_POWEROFF";
	public final String ACTION_QB_PREPOWEROFF = "autochips.intent.action.QB_PREPOWEROFF";
	public final String ACTION_MEDIA_PLAY_INDEX = "action.media.play.index";
	public final String MEDIA_PLAY_INDEX = "media_play_index";

	public final String ATE_PLAY_INDEX = "playindex";
	public final String ATE_NEXT_PREV = "nextprev";
	public final String ATE_PLAY_MODE = "playmode";
	public final String ATE_PLAY_STATUS = "playstatus";

	private long mlSystemOnOffTime = 0;
	private int IGNORE_TIME = 1000 * 5;

	private Context mContext;

	// 当前服务播放相关的数据库
	private String mDBFile = "";
	// 所有注册的媒体界面的列表
	private List<IMediaPlayerActivity> mListUI = new ArrayList<IMediaPlayerActivity>();
	// 媒体列表异步查询对象
	private MediaListQueryHandler mListQueryHandler = null;
	// 媒体播放状态
	private MediaStatus mStatus = new MediaStatus();
	// 磁盘列表
	MediaStorageState mStorageState;
	private AudioManager mAudioManager;
	private ComponentName mbCN = null;

	// 多媒体播放器
	private MultiMediaPlayer mPlayer = null;
	private int mPlayErrorID = MediaPlayerContants.ID_INVALID;
	private SaveData mSaveData;

	// audio focus 状态
	private boolean mbPauseByAudioFocus = false; // 由于audio focus丢失暂停媒体
	private boolean mbLostbAudioFocusForever = true; // 永久丢失audio focus
	private boolean mbReleaseByEasyCon = false; // 由于亿连申请解码器释放的资源
	private boolean mbOccupyAudioFocus = false; // 当前是否占有audio focus
	private boolean mbPauseByUser = false; // 用户主动暂停

	// 等待MediaProvider扫描时间
	private final int WAIT_LOADING = 120;
	private final int WAIT_FINISH = 0;
	private final int WAIT_MOUNT = 5;
	private int miWaitDatabase = WAIT_FINISH;

	private EnvironmentATC mEnv;
	private AudioManager.OnAudioFocusChangeListener mFocusChangeListener = null;

	private boolean mbPrevAction = false;

	// 单曲循环时,下一曲仍然播放当前曲目
	private boolean mbForceSingle = false;// SystemProperties.getBoolean("persist.sys.force_single",
											// false);
	// 多媒体状态线程
	private RecorverThread mRecoverThread;

	// 用来保存语音点歌路径
	private String mPathBySpeech = "";
	private long mSysTime = 0;

	private boolean mRepeatOff = false;	// 循环模式是否 切换循环关闭
	private final int MSG_RECOVER_PLAY = 1;
	private final int MSG_DOWNLOAD_FILE = 2;
	private final int MSG_RETURN_INDEX = 10002;
	private final long RECOVER_DELAY_TIME = 500;
	protected Handler mMediaHandler;
	protected HandlerThread mMediaHandleThread;

	public MediaPlayerService(Context context) {
		assert (context != null);
		mMediaHandleThread = new HandlerThread("MediaPlayerService");
		mMediaHandleThread.start();
		mMediaHandler = new Handler(mMediaHandleThread.getLooper(), this);
		mbOccupyAudioFocus = false;
		mbPauseByUser = false;
		LogUtil.printError(TAG, LogUtil._FUNC_(), "construct MediaPlayerService");
		mContext = context;
		mEnv = new EnvironmentATC(context);
		mListQueryHandler = new MediaListQueryHandler(context.getContentResolver(), this);
		mSaveData = new SaveData();
		if (SystemProperties.getBoolean(YeconConstants.PROPERTY_QB_POWERON, false)) {
			// Quick Power ON
			mlSystemOnOffTime = SystemClock.uptimeMillis();
		}
		mSysTime = SystemClock.uptimeMillis();
		mPlayer = new MultiMediaPlayer(this, mContext);
		mStorageState = new MediaStorageState();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mbCN = new ComponentName(mContext.getPackageName(), MyMediaButtonReciver.class.getName());
		mFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
			@Override
			public void onAudioFocusChange(int arg0) {
				try {
					mbLostbAudioFocusForever = false;
					switch (arg0) {
					case AudioManager.AUDIOFOCUS_LOSS:
						Log.e(TAG, "onAudioFocusChange:AUDIOFOCUS_LOSS!");
						closeMedia();
						break;
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
						Log.e(TAG, "onAudioFocusChange:duck Audio Focus!");
						mbOccupyAudioFocus = false;
						if (mPlayer != null) {
							if (mPlayer.getPlayState() == PlayStatus.STARTED
									|| mPlayer.getPlayState() == PlayStatus.SEEKING
									|| mPlayer.getPlayState() == PlayStatus.DECODED) {
								mbPauseByAudioFocus = true;
							}
							mPlayer.pause();
						}
						break;
					case AudioManager.AUDIOFOCUS_GAIN:
						Log.e(TAG, "onAudioFocusChange:get Audio Focus! mbPauseByUser:" + mbPauseByUser);
						if (mbPauseByAudioFocus) {
							mbPauseByAudioFocus = false;
						}
						mbOccupyAudioFocus = true;
						if (!mbPauseByUser && mPlayer.isInit()) {
							if (mPlayer != null) {
								mPlayer.play();
							}
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public void Initialize() {
		IntentFilter filter = null;
		filter = new IntentFilter();
		filter.addAction(MediaScanConstans.ACTION_YECON_MEIDA_SCANER_STATUS);
		mContext.registerReceiver(mReceiverScan, filter);

		filter = new IntentFilter();
		filter.addAction(MCU_ACTION_MEDIA_NEXT);
		filter.addAction(MCU_ACTION_MEDIA_PREVIOUS);
		filter.addAction(MCU_ACTION_MEDIA_PLAY_PAUSE);
		filter.addAction(MCU_ACTION_MEDIA_PLAY);
		filter.addAction(MCU_ACTION_MEDIA_PAUSE);
		filter.addAction(MCU_ACTION_MEDIA_STOP);
		filter.addAction(ACTION_MEDIA_RANDOM);
		filter.addAction(ACTION_MEDIA_REPEAT);
		filter.addAction(ACTION_MEDIA_PLAY_INDEX);
		filter.addAction(MCU_ACTION_ACC_OFF);
		filter.addAction(MediaPlayerContants.AUTOMATION_MEDIA_BROADCAST_SEND);
		filter.addAction(YeconConstants.ACTION_BACKCAR_STOP);
		mContext.registerReceiver(mReceiverMCUKey, filter);

		filter = new IntentFilter();
		filter.addAction(BootService.ACTION_IFLY_VOICE_APP);
		filter.addAction(MediaPlayerContants.ACTION_IFLY_VOICE_MUSIC);
		filter.addAction(MediaPlayerContants.ACTION_EASYCONN_ANDROID_RESUME);
		filter.addAction(MediaPlayerContants.ACTION_EASYCONN_IPHONE_IN);
		filter.addAction(MediaPlayerContants.ACTION_EASYCONN_IPHONE_RESUME);
		filter.addAction(MediaPlayerContants.ACTION_CARPLAY_INSERT);

		mContext.registerReceiver(mExternalReceiver, filter);
	}

	public void UnInitialize() {
		if (mPlayer != null) {
			mPlayer.release();
		}
		if (mAudioManager != null) {
			int iStatus = mAudioManager.abandonAudioFocus(mFocusChangeListener);
			Log.i(TAG, "UnInitialize iStatus:" + iStatus);
		}
		mContext.unregisterReceiver(mReceiverMCUKey);
		mContext.unregisterReceiver(mReceiverScan);
		mContext.unregisterReceiver(mExternalReceiver);
		if (mPlayer != null) {
			mPlayer = null;
		}
	}
	
	private void setSource(String strPath) {
		if (strPath != null) {
			if (strPath.equals(MediaScanConstans.UDISK1_PATH)) {
				SourceManager.acquireSource(mContext, Constants.KEY_SRC_MODE_USB1);
			} else if (strPath.equals(MediaScanConstans.UDISK2_PATH)) {
				SourceManager.acquireSource(mContext, Constants.KEY_SRC_MODE_USB2);
			} else if (strPath.equals(MediaScanConstans.EXTERNAL_PATH)) {
				SourceManager.acquireSource(mContext,  Constants.KEY_SRC_MODE_EXTERNAL);
			}
		}
	}
	
	private void closeMedia() {
		mMediaHandler.removeCallbacksAndMessages(null);
		updateServiceStatus(ServiceStatus.LOST_AUDIO_FOCUS, null);
		mbLostbAudioFocusForever = true;
		if (mPlayer != null) {
			mPlayer.release();
		}
		if (mStorageState.mListStorage.size() > 0 && mStorageState.getRecoverStorage() != null) {
			mStatus.writePreference(mContext, mDBFile);
		}
		if (mAudioManager != null) {
			mAudioManager.abandonAudioFocus(mFocusChangeListener);
			mAudioManager.unregisterMediaButtonEventReceiver(mbCN);
		}
		if (SourceManager.getSource() == Constants.KEY_SRC_MODE_USB1 ||
				SourceManager.getSource() == Constants.KEY_SRC_MODE_USB2 || 
				SourceManager.getSource() == Constants.KEY_SRC_MODE_EXTERNAL) {
			SourceManager.unregisterSource(mContext, SourceManager.getSource());
		}
	}

	// 恢复之前的播放现场
	public void recoverStorage(String path, int iRecoverMedia) {
		synchronized (TAG) {
			Log.e(TAG, "++recoverStorage++ : " + path);
			mStatus = new MediaStatus();
			mStatus.readPreference(mContext, mDBFile);
			if (mStatus.strFile != null && !mStatus.strFile.equals("")) {
				// 上一次记忆的播放文件是否存在
				File file = new File(mStatus.strFile);
				if (!file.exists()) {
					mStatus.clearPreference(mContext, mDBFile);
				}
			}
			getListFromDataBase();
			miWaitDatabase = WAIT_FINISH;
			mStorageState.AttachStorage(path);
			mStorageState.RecoverStorage(path);
			updateServiceStatus(ServiceStatus.SCANED, path);
			if ((path.equals(MediaScanConstans.UDISK1_PATH) && SourceManager.getSource() == Constants.KEY_SRC_MODE_USB1) ||
					(path.equals(MediaScanConstans.UDISK2_PATH) && SourceManager.getSource() == Constants.KEY_SRC_MODE_USB2) ||
					(path.equals(MediaScanConstans.EXTERNAL_PATH) && SourceManager.getSource() == Constants.KEY_SRC_MODE_EXTERNAL)) {
				if (mPathBySpeech != null && !mPathBySpeech.equals("")) {
					try {
						requestPlayFile(mPathBySpeech);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					mPathBySpeech = "";
				} else if (iRecoverMedia == MediaType.MEDIA_AUDIO) {
					recoverAudio();
				}
			}
			Log.e(TAG, "--recoverStorage-- : " + path);
		}
	}

	private void getListFromDataBase() {
		queryFileList(false, ListType.ALL_MUSIC_FILE);
		queryFileList(false, ListType.ALL_VIDEO_FILE);
		queryFileList(false, ListType.ALL_IMAGE_FILE);
		queryAlbumList(false);
		queryArtistList(false);
		queryDirList(false);
		try {
			switch (mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iPlayType) {
			case ListType.ARTIST_MIX:
				requestArtistList(false, mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iArtistPos);
				break;
			case ListType.ALBUM_MIX:
				requestAlbumList(false, mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iAlbumPos);
				break;
			default:
				break;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void recoverAudio() {
		synchronized (TAG) {
			if (mStorageState != null && mStorageState.mListStorage != null && mStorageState.mListStorage.size() > 0) {
				mbPauseByUser = false;
				RegisterSource();
				// 存在磁盘列表，即存在磁盘
				if (mStorageState.getRecoverStorage() != null) {
					// 存在已经recover过的磁盘
					setSource(mStorageState.getRecoverStorage().getPath());
					if (!mPlayer.isPlaying()) {
						if (mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile.size() > 0) {
							if (mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).strFile != null
									&& !mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).strFile.equals("")) {
								
								mbPauseByUser = false;
								int iPrevMediaType = mStatus.iMediaType;
								mStatus.iMediaType = MediaType.MEDIA_AUDIO;
								int iListType = mStatus.getMediaStatus(mStatus.iMediaType).iListType;
								setPlayListType(iListType);
								execPlay(mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).strFile, true);
								if (iPrevMediaType != mStatus.iMediaType) {
									for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
										try {
											iMediaPlayerActivity.updateMediaType(mStatus.iMediaType);
										} catch (RemoteException e) {
											e.printStackTrace();
										}
									}
								}
							} else {
								playList(MediaType.MEDIA_AUDIO, 0, mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile.get(0).getFilePath());
							}
						}
					}
					
				} else {
					// 不存在已经recover过的磁盘，单独加载磁盘
					try {
						requestAttachStorage(null, MediaType.MEDIA_AUDIO);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void requestRecover(int iMediaType) {
		synchronized (TAG) {
			if (mbLostbAudioFocusForever && iMediaType == MediaType.MEDIA_AUDIO || iMediaType == MediaType.MEDIA_VIDEO) {
				RegisterSource();
			}
			if (iMediaType == MediaType.MEDIA_AUDIO) {
				mMediaHandler.removeMessages(MSG_RECOVER_PLAY);
				mMediaHandler.sendEmptyMessageDelayed(MSG_RECOVER_PLAY, RECOVER_DELAY_TIME);
			} else if (iMediaType == MediaType.MEDIA_VIDEO) {
				mMediaHandler.removeMessages(MSG_RECOVER_PLAY);
				if (mPlayer != null && !mPlayer.isStop() && mStatus.iMediaType == MediaType.MEDIA_AUDIO) {
					requestStop();
					// 通知主页挂件清除音乐信息
					for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
						try {
							iMediaPlayerActivity.updateMediaType(MediaType.MEDIA_VIDEO);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				if (mStorageState.getRecoverStorage() != null) {
					setSource(mStorageState.getRecoverStorage().getPath());
				}
			}
		}
	}

	private void updateTrackIndex(List<MediaObject> list, String strPath, int iMediaType) {
		if (list != null) {
			int iSize = list.size();
			if (iSize > 0) {
				String strFile = "";
				if (!strPath.isEmpty()) {
					for (int i = 0; i < list.size(); i++) {
						MediaObject cv = list.get(i);
						if (cv.getFilePath().equals(strPath)) {
							strFile = strPath;
							mStatus.setPlayFile(strPath);
							mStatus.strParent = cv.getFileParent();
							mStatus.getMediaStatus(iMediaType).iFilePos = i;

							for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
								try {
									iMediaPlayerActivity.updatePlayIndex(i);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							break;
						}
					}
				}
				if (strFile.isEmpty()) {
					mStatus.setPlayFile("");
				}
			}
		}
	}

	@Override
	public void RegisterSource() {
		LogUtil.printError(TAG, LogUtil._FUNC_(), "++RegisterSource++");
		synchronized (TAG) {
			try {
				if (mStorageState != null && mStorageState.mListStorage != null
						&& mStorageState.mListStorage.size() > 0) {
					// 永久丢失音频焦点的时候重新申请
					if (mbLostbAudioFocusForever) {
						if (mAudioManager != null) {
							int iStatus = mAudioManager.requestAudioFocus(mFocusChangeListener,
									AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
							mAudioManager.registerMediaButtonEventReceiver(mbCN);
							Log.i(TAG, "requestActiveSource iStatus:" + iStatus);
						}
						Log.e(TAG, "requestActiveSource");
						mbOccupyAudioFocus = true;
						mbLostbAudioFocusForever = false;
					}
					if (mbReleaseByEasyCon) {
						mbReleaseByEasyCon = false;
						if (mStatus.iMediaType == MediaType.MEDIA_VIDEO) {
							requestPause();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void UnregisterSource() {
		LogUtil.printError(TAG, LogUtil._FUNC_(), "++UnregisterSource++");
		try {
			if (getPlayingStorage() != null) {
				if (mContext != null && mDBFile != null) {
					mStatus.writePreference(mContext, mDBFile);
				}
			}
			mPlayer.release();
			clearSavedata();
			miWaitDatabase = WAIT_FINISH;
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.printError(TAG, LogUtil._FUNC_(), "--UnregisterSource--");
	}

	@Override
	public boolean requestAttachStorage(String strPath, int iRecoverType) throws RemoteException {
		synchronized (TAG) {
			LogUtil.printError(TAG, LogUtil._FUNC_(), " strPath:" + strPath + ", iRecoverType:" + iRecoverType);
			if (strPath != null && !strPath.equals(MediaPlayerContants.LAST_MEMORY_DEVICE)) {
				boolean bFind = false;
				if (mStorageState.mListStorage.size() > 0) {
					for (MediaStorage mediaStorage : mStorageState.mListStorage) {
						if (strPath.equals(mediaStorage.getPath())) {
							bFind = true;
							break;
						}
					}
				}
				if (!bFind) {
					return false;
				}
			}
			mbPauseByUser = false;
			boolean bLastMemoryDevice = false;
			if (strPath == null || strPath.equals(MediaPlayerContants.LAST_MEMORY_DEVICE)) {
				// 如果磁盘为空 或者是记忆上一次设备
				bLastMemoryDevice = true;
			} else {
				strPath = MediaScanConstans.getStoragePath(strPath);
			}

			try {
				// get last playing storage
				if (bLastMemoryDevice) {
					strPath = MediaScanConstans.getLastDevice(mContext);
					strPath = MediaScanConstans.getStoragePath(strPath);
					// check last memory storage
					if (strPath == null || !MediaScanConstans.checkStorageExist(mEnv, strPath)) {
						// last memory storage not exist , get the storage that
						if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK1_PATH)
								&& MediaScanConstans.isSupport(MediaScanConstans.UDISK1_PATH)) {
							strPath = MediaScanConstans.UDISK1_PATH;
						} else if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.UDISK2_PATH)
								&& MediaScanConstans.isSupport(MediaScanConstans.UDISK2_PATH)) {
							strPath = MediaScanConstans.UDISK2_PATH;
						} else if (MediaScanConstans.checkStorageExist(mEnv, MediaScanConstans.EXTERNAL_PATH)) {
							strPath = MediaScanConstans.EXTERNAL_PATH;
						} else {
							strPath = "";
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (strPath != null && !strPath.equals("")) {
				// check storage
				if (iRecoverType != MediaType.MEDIA_IMAGE) {
					setSource(MediaScanConstans.getStoragePath(strPath));
				}
				String strDBFile = getDBFile(strPath);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "attach storage : " + strPath + ", database:" + strDBFile);
				if (iRecoverType != 0 || !mDBFile.equals(strDBFile)) {
					if (mRecoverThread != null) {
						mRecoverThread.interrupt();
						mRecoverThread = null;
					}
					// save last memory
					try {
						if (mPlayer != null) {
							if (mPlayer.getPlayState() == PlayStatus.STARTED
									|| mPlayer.getPlayState() == PlayStatus.PAUSED) {
								LogUtil.printError(TAG, LogUtil._FUNC_(), "save last memory!!!" + strPath);
								mStatus.writePreference(mContext, mDBFile);
							}
							// 此处必须release, 否则会引起进程异常
							mPlayer.release();
							clearSavedata();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					// play attach storage
					LogUtil.printError(TAG, LogUtil._FUNC_(), "read last memory!!!" + strPath);
					mDBFile = strDBFile;
					mRecoverThread = new RecorverThread(iRecoverType);
					mRecoverThread.setName(strPath);
					mRecoverThread.start();
					MediaScanConstans.saveLastDevice(mContext, strPath);
					return true;
				} else {
					if (mStorageState != null) {
						if (mStorageState.mListStorage.isEmpty()) {
							updateServiceStatus(ServiceStatus.NO_STORAGE, null);
						}
					}
				}
			}
			return false;
		}
	}

	private void clearSavedata() {
		try {
			if (mSaveData != null) {
				mSaveData.setMediaSongName("");
				mSaveData.setMediaTrack(0);
				mSaveData.setMediaTotalNum(0);
				mSaveData.setMediaPlayTime(0);
				mSaveData.setMediaArtWork(null);
				mSaveData.setMediaID3Album("");
				mSaveData.setMediaID3Author("");
				mSaveData.setMediaPlayCurTime(0);
				mSaveData.setMediaPlaySts(0x00);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Title: getDBFile
	 * @Description: 获取当前磁盘的相对应的数据库
	 * @param strPath
	 */
	public String getDBFile(String strPath) {
		String strDatabase = MediaScanConstans.EXTERNAL_VOLUME;
		if (strPath.contains(MediaScanConstans.UDISK1_PATH)) {
			strDatabase = MediaScanConstans.UDISK_VOLUME1;
		} else if (strPath.contains(MediaScanConstans.UDISK2_PATH)) {
			strDatabase = MediaScanConstans.UDISK_VOLUME2;
		} else if (strPath.contains(MediaScanConstans.EXTERNAL_PATH)) {
			strDatabase = MediaScanConstans.EXTERNAL_VOLUME;
		} else {
			LogUtil.printError(TAG, LogUtil._FUNC_(), "unknown path " + strPath);
		}
		return strDatabase;
	}

	@Override
	public void registerUserInterface(IMediaPlayerActivity mpi) throws RemoteException {
		synchronized (TAG) {
			for (IMediaPlayerActivity ui : mListUI) {
				if (ui.equals(mpi)) {
					return;
				}
			}
			mListUI.add(mpi);
		}
	}

	@Override
	public void unregisterUserInterface(IMediaPlayerActivity mpi) throws RemoteException {
		synchronized (TAG) {
			for (IMediaPlayerActivity ui : mListUI) {
				if (ui.equals(mpi)) {
					mListUI.remove(ui);
					return;
				}
			}
		}
	}

	@Override
	public void requestRepeat(int iState) throws RemoteException {
		if (getRepeatStatus() != iState && iState >= (mRepeatOff ? RepeatMode.OFF : RepeatMode.SINGLE) && iState <= RepeatMode.RANDOM) {
			execRepeat(iState);
		}
	}

	private void execRepeat(int iRepeat) throws RemoteException {
		synchronized (TAG) {
			if (getRepeatStatus() != iRepeat) {
				setRepeatStatus(mStatus.iMediaType, iRepeat);
				for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
					iMediaPlayerActivity.updateRepeatStatus(iRepeat);
				}
				mSaveData.setMediaRepeatSts(iRepeat);
			}
		}
	}

	@Override
	public int getMediaType() throws RemoteException {
		synchronized (TAG) {
			return mStatus.iMediaType;
		}
	}

	@Override
	public void requestFileList(int iListType) throws RemoteException {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			try {
				iMediaPlayerActivity.updateListData(iListType, 0x01);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void queryFileList(boolean bAsync, int iListType) {
		Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
		String[] columns = MediaPlayerContants.FILE_COLUMNS;
		int iMediaType = MediaType.MEDIA_AUDIO;
		if (iListType == ListType.ALL_VIDEO_FILE) {
			iMediaType = MediaType.MEDIA_VIDEO;
		} else if (iListType == ListType.ALL_IMAGE_FILE) {
			iMediaType = MediaType.MEDIA_IMAGE;
		}
		if (bAsync) {
			iListType |= 0x100;
		}
		String selection = YeconMediaFilesColumns.MEDIA_TYPE + "=" + iMediaType;
		mListQueryHandler.startQuery(bAsync, iListType, null, uri, columns, selection, null, YeconMediaFilesColumns.LETTER);
	}

	@Override
	public void requestDirList(boolean bAsync, int iDirPos, int mediaType) throws RemoteException {
		int iCurDirPos = mStatus.getMediaStatus(mediaType).iDirPos;
		if (iCurDirPos >= 0 && mStatus.getMediaStatus(mediaType).mListDir.size() > iCurDirPos
				&& iDirPos == mStatus.getMediaStatus(mediaType).mListDir.get(iCurDirPos).getDirId()) {
			mStatus.getMediaStatus(mediaType).mListMix.clear();
			for (MediaObject object : mStatus.getMediaStatus(mediaType).mListDir) {
				mStatus.getMediaStatus(mediaType).mListMix.add(object);
			}
			mStatus.getMediaStatus(mediaType).iDirPos = -1;
			requestFileList(ListType.MUSIC_MIX);
		} else {
			queryDirFileList(true, iDirPos, mediaType);
		}
	}

	private void queryDirList(boolean bAsyc) {
		Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_DIR);
		String[] columns = MediaPlayerContants.DIR_COLUMNS;
		String selection = null;
		String[] selectionArgs = null;
		int iListType = ListType.ALL_DIR;
		if (bAsyc) {
			iListType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsyc, iListType, null, uri, columns, selection, selectionArgs, YeconMediaDirColumns.LETTER);
	}

	private void queryDirFileList(boolean bAsync, int iDirPos, int mediaType) {
		Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
		String[] columns = MediaPlayerContants.FILE_COLUMNS;
		String selection = YeconMediaFilesColumns.MEDIA_TYPE + "=" + mediaType;
		int iType = ListType.MUSIC_MIX;
		if (iDirPos > 0) {
			selection += " and ";
			if (mediaType == MediaType.MEDIA_AUDIO) {
				iType = ListType.MUSIC_MIX;
			} else if (mediaType == MediaType.MEDIA_VIDEO) {
				iType = ListType.VIDEO_MIX;
			} else if (mediaType == MediaType.MEDIA_IMAGE) {
				iType = ListType.IMAGE_MIX;
			}
			selection += YeconMediaFilesColumns.PARENT_ID + "=" + iDirPos;
		}
		String[] selectionArgs = null;
		if (bAsync) {
			iType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsync, iType, null, uri, columns, selection, selectionArgs, YeconMediaFilesColumns.LETTER);
	}

	private void queryAlbumList(boolean bAsync) {
		Uri uri = MediaScanConstans.getContent(mDBFile,
				MediaScanConstans.TABLE_ALBUM);
		String[] columns = MediaPlayerContants.ALBUM_COLUMNS;
		String selection = null;
		String[] selectionArgs = null;
		String orderBy = null;
		int iType = ListType.ALL_ALBUM;
		if (bAsync) {
			iType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsync, iType, null, uri, columns,
				selection, selectionArgs, orderBy);
	}

	private void queryArtistList(boolean bAsync) {
		Uri uri = MediaScanConstans.getContent(mDBFile,
				MediaScanConstans.TABLE_ARTIST);
		String[] columns = MediaPlayerContants.ARTIST_COLUMNS;
		String selection = null;
		String[] selectionArgs = null;
		String orderBy = null;
		int iType = ListType.ALL_ARTIST;
		if (bAsync) {
			iType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsync, iType, null, uri, columns,
				selection, selectionArgs, orderBy);
	}
	
	@Override
	public void requestAlbumList(boolean bAsync, int iAlbumPos) throws RemoteException{
		if (iAlbumPos >= 0 && mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.size() > iAlbumPos) {
			if (iAlbumPos == mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos) {
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix.clear();
				for (MediaObject object : mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum) {
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix.add(object);
				}
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos = -1;
				requestFileList(ListType.ALBUM_MIX);
			} else {
				queryAlbumFileList(bAsync, iAlbumPos);
			}
		}
	}
	
	private void queryAlbumFileList(boolean bAsync, int iAlbumPos) {
		Uri uri = MediaScanConstans.getContent(mDBFile,
				MediaScanConstans.TABLE_FILES);
		String[] columns = MediaPlayerContants.FILE_COLUMNS;
		String selection = YeconMediaFilesColumns.MEDIA_TYPE + "=" + MediaType.MEDIA_AUDIO;
		if (iAlbumPos >= 0 && iAlbumPos < mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.size()) {
			mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos = iAlbumPos;
			selection += " and ";
			int iID = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.get(iAlbumPos).getID();
			selection += YeconMediaFilesColumns.ALBUM_ID + "=" + iID;
		}
		String[] selectionArgs = null;
		String orderBy = null;
		int iType = ListType.ALBUM_MIX;
		if (bAsync) {
			iType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsync, iType, null, uri, columns,
				selection, selectionArgs, YeconMediaFilesColumns.LETTER);
	}
	
	@Override
	public void requestArtistList(boolean bAsync, int iAritstPos) throws RemoteException{
		if (iAritstPos >= 0 && mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.size() > iAritstPos) {
			if (iAritstPos == mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos) {
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix.clear();
				for (MediaObject object : mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist) {
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix.add(object);
				}
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos = -1;
				requestFileList(ListType.ARTIST_MIX);
			} else {
				queryArtistFileList(bAsync, iAritstPos);
			}
		}
	}
	
	private void queryArtistFileList(boolean bAsync, int iArtistPos) {
		Uri uri = MediaScanConstans.getContent(mDBFile,
				MediaScanConstans.TABLE_FILES);
		String[] columns = MediaPlayerContants.FILE_COLUMNS;
		String selection = YeconMediaFilesColumns.MEDIA_TYPE + "=" + MediaType.MEDIA_AUDIO;
		if (iArtistPos >= 0 && iArtistPos < mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.size()) {
			mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos = iArtistPos;
			selection += " and ";
			int iID = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.get(iArtistPos).getID();
			selection += YeconMediaFilesColumns.ARTIST_ID + "=" + iID;
		}
		String[] selectionArgs = null;
		String orderBy = null;
		int iType = ListType.ARTIST_MIX;
		if (bAsync) {
			iType |= 0x100;
		}
		mListQueryHandler.startQuery(bAsync, iType, null, uri, columns,
				selection, selectionArgs, YeconMediaFilesColumns.LETTER);
	}
	
	@Override
	public void requestSaveLastMemory() throws RemoteException {
		synchronized (TAG) {
			mStatus.writePreference(mContext, mDBFile);
		}
	}

	@Override
	public void requestPlayList(int mediaType, int iListType, int iPos, String path) throws RemoteException {
		playList(mediaType, iPos, path);
	}

	private void playList(int mediaType, int iPos, String path) {
		synchronized (TAG) {
			mbPauseByUser = false;
			int iPrevMediaType = mStatus.iMediaType;
			mStatus.iMediaType = mediaType;
			int iListType = mStatus.getMediaStatus(mediaType).iListType;
			setPlayListType(iListType);
			if (iListType == ListType.ALBUM_MIX || iListType == ListType.ARTIST_MIX || iPrevMediaType != mStatus.iMediaType || iListType != mStatus.getMediaStatus(mediaType).iPlayType) {
				// 当前播放的列表类型和选择的列表类型不一致
				execPlay(path, (iPrevMediaType != mStatus.iMediaType || iListType != mStatus.getMediaStatus(mediaType).iPlayType));
			} else {
				execPlay(iPos);
			}
			if (iPrevMediaType != mStatus.iMediaType) {
				for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
					try {
						iMediaPlayerActivity.updateMediaType(mStatus.iMediaType);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Title: setPlayListType
	 * Description:切换列表模式
	 * @param iMode
	 */
	public void setPlayListType(int iListType) {
		Log.e(TAG, "++setPlayListType++:" + iListType);
			
			switch (iListType) {
			case ListType.ALL_MUSIC_FILE:
			case ListType.MUSIC_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iPlayType = ListType.ALL_MUSIC_FILE;
				mStatus.mListPlay = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile;
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iAlbumPos = MediaPlayerContants.ID_INVALID;
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iArtistPos = MediaPlayerContants.ID_INVALID;
				break;
			case ListType.ALL_VIDEO_FILE:
			case ListType.VIDEO_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_VIDEO).iPlayType = ListType.ALL_VIDEO_FILE;
				mStatus.mListPlay = mStatus.getMediaStatus(MediaType.MEDIA_VIDEO).mListFile;
				break;
			case ListType.ALL_IMAGE_FILE:
			case ListType.IMAGE_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).iPlayType = ListType.ALL_IMAGE_FILE;
				mStatus.mListPlay = mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).mListFile;
				break;
			case ListType.ARTIST_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iPlayType = ListType.ARTIST_MIX;
				mStatus.mListPlay = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistFile;
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iArtistPos = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos;
				break;
			case ListType.ALBUM_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iPlayType = ListType.ALBUM_MIX;
				mStatus.mListPlay = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumFile;
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iAlbumPos = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos;
				break;
			default:
				break;
			}
		Log.e(TAG, "--setPlayListType--:" + iListType);
	}

	@Override
	public void requestPause() throws RemoteException {
		synchronized (TAG) {
			Log.e(TAG, "lib version:" + VERSION);
			Log.e(TAG, "++requestPause++");
			int state = mPlayer.getCurrentRealState();
			if (state == PlayStatus.STARTED) {
				mPlayer.pause();
				mbPauseByUser = true;
			} else if (state == PlayStatus.DECODED || state == PlayStatus.PAUSED || state == PlayStatus.DECODING
					|| state == PlayStatus.STOPED) {
				mPlayer.play();
				mbPauseByUser = false;
			} else if (state == PlayStatus.FINISH || state == PlayStatus.IDLE || state == PlayStatus.ERROR) {
				mbPauseByUser = false;
				// if (mStatus.iMediaType == MediaType.MEDIA_VIDEO && state ==
				// PlayStatus.IDLE) {
				if (mStatus.getMediaStatus(mStatus.iMediaType).mListFile.size() > 0) {
					if (state == PlayStatus.IDLE) {
						// 音视频播放时被抢占焦点，再次进入界面时，恢复播放
						if (mStatus.strFile != null && !mStatus.strFile.equals("")) {
							decode(mStatus.strFile);
							if (mStatus.getPlayProgress() > 0) {
								mPlayer.setRecoverProgress(mStatus.getPlayProgress());
							}
						} else {
							recoverAudio();
						}
					} else {
						execNext(false);
					}
				}
			}
			Log.e(TAG, "--requestPause--:" + state);
		}
	}

	@Override
	public void requestStop() {
		synchronized (TAG) {
			if (mPlayer != null) {
				mStatus.writePreference(mContext, mDBFile);
				mPlayer.release();
			}
		}
	}

	@Override
	public void requestSetFrontDisplay(SurfaceHolder holder) throws RemoteException {
		synchronized (TAG) {
			mPlayer.setFrontDisplay(holder, mStatus.getPlayProgress());
		}
	}

	@Override
	public void requestSetRearDisplay(SurfaceHolder holder) throws RemoteException {
		synchronized (TAG) {
			mPlayer.setRearDisplay(holder);
		}
	}

	@Override
	public void requestSeek(int lPos) throws RemoteException {
		synchronized (TAG) {
			if (mPlayer.getDuration() >= lPos) {
				mPlayer.seek(lPos);
			} else {
				LogUtil.printError(TAG, LogUtil._FUNC_(), "illegal parameter, ignore seek!!!");
			}
		}
	}

	@Override
	public void requestNext() throws RemoteException {
		long time = SystemClock.uptimeMillis();
		if (time - mSysTime >= 300) {
			mSysTime = time;
			mbPauseByUser = false;
			execNext(false);
		}
	}

	@Override
	public void requestPrev() throws RemoteException {
		long time = SystemClock.uptimeMillis();
		if (time - mSysTime >= 300) {
			mSysTime = time;
			mbPauseByUser = false;
			execPrev();
		}
	}

	private void execNext(boolean bForcedNext) {
		synchronized (TAG) {
			try {
				int iSize = mStatus.mListPlay.size();
				if (iSize == 0) {
					mPlayer.stop();
					mPlayer.release();
					mStatus.mPlayingTrackInfo = null;
					updateServiceStatus(ServiceStatus.PLAYED, null);
					return;
				}
				int iID = MediaPlayerContants.ID_INVALID;
				if (getRepeatStatus() == RepeatMode.RANDOM) {
					iID = getRandom(0, iSize);
					if (iSize > 1) {
						int icount = 10;
						while (iID == mStatus.getPlayFilePos() && icount-- > 0) {
							iID = getRandom(0, iSize);
						}
					}
				} else {
					if (getRepeatStatus() == RepeatMode.SINGLE && mbForceSingle) {
						// 单曲播放
						iID = mStatus.getPlayFilePos();
					} else if (mRepeatOff && getRepeatStatus() == RepeatMode.OFF) {
						// 顺序播放
						if (mStatus.getPlayFilePos() < iSize - 1) {
							iID = mStatus.getPlayFilePos() + 1;
						} else {
							// 顺序播放到最后一首时，下一曲，定位到列表第一首，返回到列表，同时暂停播放
							mbPauseByUser = true;
							iID = 0;
							mStatus.setPlayFilePos(0);
							for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
								try {
									iMediaPlayerActivity.updatePlayIndex(mStatus.getPlayFilePos());
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							updateServiceStatus(ServiceStatus.PLAYED, null);
						}
					} else {
						// 重复播放
						if (getRepeatStatus() == RepeatMode.SINGLE && mStatus.getPlayFilePos() == (iSize - 1)) {
							// 单曲循环播放最后一首时，下一曲，定位到列表第一首，返回到列表，同时暂停播放
							mbPauseByUser = true;
							iID = 0;
							mStatus.setPlayFilePos(0);
							for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
								try {
									iMediaPlayerActivity.updatePlayIndex(0);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							updateServiceStatus(ServiceStatus.PLAYED, null);
						} else {
							iID = (mStatus.getPlayFilePos() + 1) % iSize;
						}
					}
				}
				if (iID != MediaPlayerContants.ID_INVALID && iID < iSize) {
					mStatus.setPlayFilePos(MediaPlayerContants.ID_INVALID);
					execPlay(iID);
				} else {
					requestStop();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void execPrev() {
		synchronized (TAG) {
			try {
				mbPrevAction = true;
				int iSize = mStatus.mListPlay.size();
				if (iSize == 0) {
					Log.e(TAG, "current list is empty ! ignore play prev");
					return;
				}
				int iID = MediaPlayerContants.ID_INVALID;
				if (getRepeatStatus() == RepeatMode.RANDOM) {
					iID = getRandom(0, iSize);
					if (iSize > 1) {
						int icount = 10;
						while (iID == mStatus.getPlayFilePos() && icount-- > 0) {
							iID = getRandom(0, iSize);
						}
					}
				} else {
					if (getRepeatStatus() == RepeatMode.SINGLE && mbForceSingle) {
						// 单曲播放
						iID = mStatus.getPlayFilePos();
					} else if (mRepeatOff && getRepeatStatus() == RepeatMode.OFF) {
						// 顺序播放
						if (mStatus.getPlayFilePos() > 0) {
							iID = mStatus.getPlayFilePos() - 1;
						} else {
							// 顺序播放到第一首时，上一曲，定位到列表第一首，返回到列表，同时暂停播放
							mbPauseByUser = true;
							iID = 0;
							updateServiceStatus(ServiceStatus.PLAYED, null);
						}
					} else {
						// 重复播放
						if (mStatus.getPlayFilePos() > 0) {
							iID = mStatus.getPlayFilePos() - 1;
						} else {
							if (getRepeatStatus() == RepeatMode.SINGLE) {
								iID = 0;
								// 单曲循环播放第一首时，上一曲，定位到列表第一首，返回到列表，同时暂停播放
								// updateServiceStatus(ServiceStatus.PLAYED,
								// null);
								// mbPauseByUser = true;
							} else {
								iID = iSize - 1;
							}
						}
					}
				}
				if (iID != MediaPlayerContants.ID_INVALID && iID < iSize) {
					mStatus.setPlayFilePos(MediaPlayerContants.ID_INVALID);
					execPlay(iID);
				} else {
					requestStop();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void execPlay(int iPos) {
		// 通过索引播放
		synchronized (TAG) {
			Log.i(TAG, "execPlay iPos:" + iPos + ",size:" + mStatus.mListPlay.size());
			if (iPos != MediaPlayerContants.ID_INVALID && iPos < mStatus.mListPlay.size()) {
				// play
				String path = mStatus.mListPlay.get(iPos).getFilePath();
				if (mStatus.getPlayFilePos() == iPos && mStatus.getPlayFile().equals(path) && mPlayer.getPlayState() != PlayStatus.FINISH) {
					mStatus.setPlayFilePos(iPos);
					if (mPlayer.isInit()) {
						mPlayer.play();
					} else {
						if (decode(mStatus.getPlayFile())) {
							if (mStatus.getPlayProgress() > 0) {
								mPlayer.setRecoverProgress(mStatus.getPlayProgress());
							} else {
								mStatus.setPlayProgress(0);
							}
						}
					}
				} else {
					String strFile = mStatus.mListPlay.get(iPos).getFilePath();
					mStatus.setPlayFilePos(iPos);
					mStatus.setPlayFile(strFile);
					mStatus.setPlayProgress(0);
					decode(strFile);
				}
			}
		}
	}

	private void execPlay(String path, boolean bChange) {
		// 通过路径播放,change媒体类型是否改变
		synchronized (TAG) {
			Log.i(TAG, "execPlay path:" + path + ",size:" + mStatus.mListPlay.size());
			if (mStatus.getPlayFile().equals(path) && !bChange && mPlayer.isInit()) {
				// 如果对应的媒体播放文件相同，并且没有切换媒体类型 又初始化了，直接play
				mPlayer.play();
			} else {
				if (mStatus.getPlayFile().equals(path) && mStatus.getPlayProgress() > 0) {
					mPlayer.setRecoverProgress(mStatus.getPlayProgress());
				} else {
					mStatus.setPlayProgress(0);
				}
				decode(path);
				updateTrackIndex(mStatus.mListPlay, path, mStatus.iMediaType);
			}
		}
	}

	@Override
	public int getPlayStatus() throws RemoteException {
		return mPlayer.getPlayState();
	}

	@Override
	public int getRepeatStatus() {
		return getRepeatStatus(mStatus.iMediaType);
	}

	private int getRepeatStatus(int iMediaType) {
		synchronized (TAG) {
			return mStatus.getMediaStatus(iMediaType).iRepeatMode;
		}
	}

	private void setRepeatStatus(int iMediaType, int iRepeatMode) {
		mStatus.getMediaStatus(iMediaType).iRepeatMode = iRepeatMode;
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		synchronized (TAG) {
			if (cursor != null) {
				cursor.moveToFirst();
				int iType = token & 0xFF;
				switch (iType) {
				case ListType.ALL_MUSIC_FILE:
				case ListType.ALL_VIDEO_FILE:
				case ListType.ALL_IMAGE_FILE:
					List<MediaObject> list = mStatus.getMediaStatus(token).mListFile;
					list.clear();
					for (int i = 0; i < cursor.getCount(); i++) {
						list.add(getMediaObject(cursor, 0, i));
						cursor.moveToNext();
					}
					if (iType == ListType.ALL_IMAGE_FILE) {
						updateImageUrl(list);
					}
					break;
				case ListType.MUSIC_MIX:
				case ListType.VIDEO_MIX:
				case ListType.IMAGE_MIX:
					updateDirFileList(iType, cursor);
					break;
				case ListType.ALL_DIR:
					for (int i = MediaType.MEDIA_AUDIO; i <= MediaType.MEDIA_IMAGE; i++) {
						mStatus.getMediaStatus(i).mListDir.clear();
						mStatus.getMediaStatus(i).mListMix.clear();
					}
					for (int i = 0; i < cursor.getCount(); i++) {
						MediaObject object = getMediaObject(cursor, ListType.ALL_DIR, 0);
						if (cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_AUDIO) > 0) {
							mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListDir.add(object);
							mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListMix.add(object);
						}
						if (cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_VIDEO) > 0) {
							mStatus.getMediaStatus(MediaType.MEDIA_VIDEO).mListDir.add(object);
							mStatus.getMediaStatus(MediaType.MEDIA_VIDEO).mListMix.add(object);
						}
						if (cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_IMAGE) > 0) {
							mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).mListDir.add(object);
							mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).mListMix.add(object);
						}
						cursor.moveToNext();
					}
					break;
				case ListType.ALL_ARTIST:
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.clear();
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix.clear();
					for (int i = 0; i < cursor.getCount(); i++) {
						MediaObject object = getMediaObject(cursor, ListType.ALL_ARTIST, i);
						mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.add(object);
						mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix.add(object);
						cursor.moveToNext();
					}
					break;
				case ListType.ALL_ALBUM:
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.clear();
					mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix.clear();
					for (int i = 0; i < cursor.getCount(); i++) {
						MediaObject object = getMediaObject(cursor, ListType.ALL_ALBUM, i);
						mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.add(object);
						mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix.add(object);
						cursor.moveToNext();
					}
					break;
				case ListType.ARTIST_MIX:
					updateArtistList(cursor);
					break;
				case ListType.ALBUM_MIX:
					updateAlbumList(cursor);
					break;
				default:
					break;
				}
				cursor.close();
				System.gc();
			}
			if ((token & 0x100) > 0) {
				for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
					try {
						iMediaPlayerActivity.updateListData(token & 0xFF, 0x01);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void updateDirFileList(int iListType, Cursor cursor) {
		String parentPath = cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_PARENT);
		if (iListType == ListType.MUSIC_MIX || iListType == ListType.VIDEO_MIX || iListType == ListType.IMAGE_MIX) {
			List<MediaObject> listMix = mStatus.getMediaStatus(iListType - 3).mListMix;
			List<MediaObject> listDir = mStatus.getMediaStatus(iListType - 3).mListDir;
			listMix.clear();
			int iIndex = 0;
			for (int i = 0; i < listDir.size(); i++) {
				listMix.add(listDir.get(i));
				iIndex = i;
				if (parentPath.equals(listDir.get(i).getFilePath())) {
					mStatus.getMediaStatus(iListType - 3).iDirPos = iIndex;
					break;
				}
			}
			for (int i = 0; i < cursor.getCount(); i++) {
				listMix.add(getMediaObject(cursor, 0, 0));
				cursor.moveToNext();
			}
			for (int i = iIndex + 1; i < listDir.size(); i++) {
				listMix.add(listDir.get(i));
			}
			mStatus.getMediaStatus(iListType - 3).iListType = iListType;
		}
	}
	
	private void updateArtistList(Cursor cursor) {
		List<MediaObject> listMix = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix;
		List<MediaObject> listArtist = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist;
		listMix.clear();
		mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistFile.clear();
		int iIndex = 0;
		for (int i = 0; i < mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist.size(); i++) {
			listMix.add(listArtist.get(i));
			iIndex = i;
			String strArtist = cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_ARTIST);
			if (strArtist.equals(listArtist.get(i).getFileName())) {
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos = iIndex;
				break;
			}
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			MediaObject object = getMediaObject(cursor, 0, 0);
			listMix.add(object);
			mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistFile.add(object);
			cursor.moveToNext();
		}
		for (int i = iIndex + 1; i < listArtist.size(); i++) {
			listMix.add(listArtist.get(i));
		}
		mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = ListType.ARTIST_MIX;
	}
	
	private void updateAlbumList(Cursor cursor) {
		List<MediaObject> listMix = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix;
		List<MediaObject> listAlbum = mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum;
		mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumFile.clear();
		listMix.clear();
		int iIndex = 0;
		for (int i = 0; i < mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum.size(); i++) {
			listMix.add(listAlbum.get(i));
			iIndex = i;
			String strAlbum = cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_ALBUM);
			if (strAlbum.equals(listAlbum.get(i).getFileName())) {
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos = iIndex;
				break;
			}
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			MediaObject object = getMediaObject(cursor, 0, 0);
			listMix.add(object);
			mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumFile.add(object);
			cursor.moveToNext();
		}
		for (int i = iIndex + 1; i < listAlbum.size(); i++) {
			listMix.add(listAlbum.get(i));
		}
		mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = ListType.ALBUM_MIX;
	}

	private MediaObject getMediaObject(Cursor cursor, int iMode, int iIndex) {
		MediaObject obj = new MediaObject();

		if (iMode == ListType.ALL_ARTIST) {
			obj.setID(cursor.getInt(MediaPlayerContants.ALBUM_COLUMNS_INDEX_ID));
			obj.setFileName(cursor.getString(MediaPlayerContants.ALBUM_COLUMNS_INDEX_NAME));
			obj.setAudioCount(cursor.getInt(MediaPlayerContants.ALBUM_COLUMNS_INDEX_AMOUNT));
			obj.setMediaType(MediaType.MEDIA_ARTIST);
			obj.setIndex(iIndex);
		} else if (iMode == ListType.ALL_ALBUM) {
			obj.setID(cursor.getInt(MediaPlayerContants.ARTIST_COLUMNS_INDEX_ID));
			obj.setFileName(cursor.getString(MediaPlayerContants.ARTIST_COLUMNS_INDEX_NAME));
			obj.setAudioCount(cursor.getInt(MediaPlayerContants.ARTIST_COLUMNS_INDEX_AMOUNT));
			obj.setMediaType(MediaType.MEDIA_ALBUM);
			obj.setIndex(iIndex);
		} else if (iMode == ListType.ALL_DIR) {
			// obj.setID(cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_ID));
			obj.setDirId(cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_ID));
			obj.setFileName(cursor.getString(MediaPlayerContants.DIR_COLUMNS_INDEX_NAME));
			obj.setFilePath(cursor.getString(MediaPlayerContants.DIR_COLUMNS_INDEX_DATA));
			obj.setAudioCount(cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_AUDIO));
			obj.setVideoCount(cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_VIDEO));
			obj.setImageCount(cursor.getInt(MediaPlayerContants.DIR_COLUMNS_INDEX_AMOUNT_IMAGE));
			obj.setMediaType(MediaType.MEDIA_DIR);
		} else {
			obj.setID(cursor.getInt(MediaPlayerContants.FILE_COLUMNS_INDEX_ID));
			obj.setFileName(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_NAME));
			obj.setFilePath(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_DATA));
			obj.setFileParent(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_PARENT));
			obj.setMediaType(cursor.getInt(MediaPlayerContants.FILE_COLUMNS_INDEX_MEDIA_TYPE));
			obj.setDamage(cursor.getInt(MediaPlayerContants.FILE_COLUMNS_INDEX_DAMAGE));
			obj.setDirId(cursor.getInt(MediaPlayerContants.FILE_COLUMNS_INDEX_PARENT_ID));
			obj.setTitle(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_TITLE));
			obj.setAlbum(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_ALBUM));
			obj.setArtist(cursor.getString(MediaPlayerContants.FILE_COLUMNS_INDEX_ARTIST));
		}
		return obj;
	}

	public int getRandom(int iMin, int iMax) {
		return (int) (Math.random() * (iMax - iMin) + iMin);
	}

	boolean decode(String str) {
		synchronized (TAG) {
			if (mStatus.mPlayingTrackInfo != null) {
				mStatus.mPlayingTrackInfo.recycle();
				mStatus.mPlayingTrackInfo = null;
			}
			return mPlayer.decode(str);
		}
	}

	@Override
	public boolean isAllowPlay() {
		boolean isAllowPlay = (mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC) || mbOccupyAudioFocus) && !mbPauseByUser && !MediaScanConstans.isBtPhone();
		Log.i(TAG, "isAllowPlay isAllowPlay:" + isAllowPlay);
		return isAllowPlay;
	}

	@Override
	public int getMediaPlayerType() {
		return mStatus.iMediaType;
	}

	@Override
	public void updatePlayState() {
		boolean bExcuteNextSong = false;
		if (mPlayer == null) {
			return;
		}
		int iStatus = mPlayer.getPlayState();
		if (mStatus.mPlayingTrackInfo != null) {
			mStatus.mPlayingTrackInfo.setDuration(mPlayer.getDuration());
		}
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			try {
				iMediaPlayerActivity.updatePlayStatus(iStatus);
				if (iStatus == PlayStatus.DECODED) {
					iMediaPlayerActivity.updatePlayIndex(mStatus.getPlayFilePos());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		if (iStatus == PlayStatus.ERROR) {
			// exist next file to play
			if (getRepeatStatus() == RepeatMode.SINGLE && mbForceSingle) {
				LogUtil.printError(TAG, LogUtil._FUNC_(), "single file repeat, can not play next !!!");
			} else if (mPlayErrorID == mStatus.getPlayFilePos()) {
				LogUtil.printError(TAG, LogUtil._FILE_(), "has been tried all files, cat not play next !!!");
			} else {
				bExcuteNextSong = true;
			}
			if (mPlayErrorID == MediaPlayerContants.ID_INVALID) {
				mPlayErrorID = mStatus.getPlayFilePos();
			}
			if (mPlayer != null) {
				mPlayer.reset();
			}
			LogUtil.printError(TAG, LogUtil._FUNC_(), "error, play next song !!!");
		} else if (iStatus == PlayStatus.FINISH) {
			if (getRepeatStatus() == RepeatMode.SINGLE) {
				mStatus.setPlayProgress(0);
				decode(mStatus.strFile);
				bExcuteNextSong = false;
			} else {
				bExcuteNextSong = true;
			}
			LogUtil.printError(TAG, LogUtil._FUNC_(), "finish, play next song !!!");
		} else if (iStatus == PlayStatus.STARTED || iStatus == PlayStatus.DECODED || iStatus == PlayStatus.PAUSED
				|| iStatus == PlayStatus.SEEKING) {
			if (iStatus == PlayStatus.STARTED || iStatus == PlayStatus.DECODED) {
				mPlayErrorID = MediaPlayerContants.ID_INVALID;
				if (!mbOccupyAudioFocus || mbPauseByUser) {
					if (!mbOccupyAudioFocus) {
						mbPauseByAudioFocus = true;
					}
					updatePlayProgress(mStatus.getPlayProgress(), mPlayer.getDuration());
					Log.e(TAG, "can not play! mbOccupyAudioFocus:" + mbOccupyAudioFocus + " mbPauseByUser:"
							+ mbPauseByUser);
				}
			}
			mbPrevAction = false;
			if (iStatus == PlayStatus.STARTED || iStatus == PlayStatus.DECODED || iStatus == PlayStatus.PAUSED) {
				mStatus.writePreference(mContext, mDBFile);
			}
		}

		if (mSaveData != null) {
			mSaveData.setMediaPlaySts(iStatus == PlayStatus.PAUSED || iStatus == PlayStatus.IDLE ? 0x00 : 0x01);
		}
		// need auto play next
		if (bExcuteNextSong) {
			if (mbPrevAction) {
				execPrev();
			} else {
				execNext(false);
			}
		}
	}

	@Override
	public boolean updateDamageState(String strPath, boolean bDamage) {
		Log.i(TAG, "updateDamageState strPath:" + strPath);
		synchronized (TAG) {
			boolean bRet = false;
//			try {
//				if (mContext != null) {
//					Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
//					ContentValues values = new ContentValues();
//					values.put(YeconMediaFilesColumns.DAMAGE, bDamage ? 1 : 0);
//					String where = YeconMediaFilesColumns.DATA + "=?";
//					if (mContext.getContentResolver().update(uri, values, where, new String[] { strPath }) > 0) {
//						bRet = true;
//						if (mStatus.iMediaType == MediaType.MEDIA_AUDIO) {
//							queryFileList(false, ListType.ALL_MUSIC_FILE);
//						} else if (mStatus.iMediaType == MediaType.MEDIA_VIDEO) {
//							queryFileList(false, ListType.ALL_VIDEO_FILE);
//						} else if (mStatus.iMediaType == MediaType.MEDIA_IMAGE) {
//							queryFileList(false, ListType.ALL_IMAGE_FILE);
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			return bRet;
		}
	}

	public void updateServiceStatus(int iState, String strData) {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			try {
				iMediaPlayerActivity.updateServiceStatus(iState, strData);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updatePlayProgress(int iProgress, int iDuration) {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			try {
				iMediaPlayerActivity.updateTimeProcess(iProgress, iDuration);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mStatus.setPlayProgress(iProgress);
		mSaveData.setMediaPlayCurTime(iProgress);
		mSaveData.setMediaPlayTime(iDuration);
	}

	@Override
	public void updateMediaInfo(int iInfo, int iExtra) {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			try {
				iMediaPlayerActivity.updateMediaInfo(iInfo, iExtra);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updatePlayInfo(boolean bError, String strPath) {
		if (bError) {
			updateDamageState(strPath, true);
		}
		updatePlayingFileInfo();
	}

	@Override
	public List<MediaStorage> getStorageList() throws RemoteException {
		synchronized (TAG) {
			return mStorageState.mListStorage;
		}
	}

	@Override
	public List<MediaObject> getPlayList() throws RemoteException {
		synchronized (TAG) {
			return mStatus.mListPlay;
		}
	}

	@Override
	public List<MediaObject> getFileList(int iListType) throws RemoteException {
		synchronized (TAG) {
			switch (iListType) {
			case ListType.ALL_MUSIC_FILE:
			case ListType.ALL_VIDEO_FILE:
			case ListType.ALL_IMAGE_FILE:
				mStatus.getMediaStatus(iListType).iListType = iListType;
				return mStatus.getMediaStatus(iListType).mListFile;
			case ListType.MUSIC_MIX:
			case ListType.VIDEO_MIX:
			case ListType.IMAGE_MIX:
				mStatus.getMediaStatus(iListType - 3).iListType = iListType;
				return mStatus.getMediaStatus(iListType - 3).mListMix;
			case ListType.ARTIST_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = iListType;
				return mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtistMix;
			case ListType.ALBUM_MIX:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = iListType;
				return mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbumMix;
			case ListType.ALL_ARTIST:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = iListType;
				return mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListArtist;
			case ListType.ALL_ALBUM:
				mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iListType = iListType;
				return mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListAlbum;
			default:
				break;
			}
			return null;
		}
	}

	@Override
	public List<MediaObject> getImageList() throws RemoteException {
		if (mStatus != null) {
			return mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).mListFile;
		}
		return null;
	}

	@Override
	public int getListType(int iMediaType) throws RemoteException {
		if (mStatus != null) {
			return mStatus.getMediaStatus(iMediaType).iListType;
		}
		return 0;
	}
	
	@Override
	public int getPlayType(int iMediaType) throws RemoteException {
		if (mStatus != null) {
			return mStatus.getMediaStatus(iMediaType).iPlayType;
		}
		return 0;
	}

	private void updatePlayingFileInfo() {
		Log.i(TAG, " ++ updatePlayingFileInfo() + filepos:" + mStatus.getPlayFilePos() + ",mListPlay.size():" + mStatus.mListPlay.size());
		if (mStatus.mListPlay.size() > mStatus.getPlayFilePos()
				&& mStatus.getPlayFilePos() != MediaPlayerContants.ID_INVALID) {
			try {
				if (mStatus.mPlayingTrackInfo != null) {
					mStatus.mPlayingTrackInfo.recycle();
					mStatus.mPlayingTrackInfo = null;
				}
				MediaTrackInfo trackInfo = new MediaTrackInfo();
				MediaObject object = mStatus.mListPlay.get(mStatus.getPlayFilePos());
				trackInfo.setPath(object.getFilePath());
				trackInfo.setName(object.getFileName());
				trackInfo.setTitle(object.getTitle());
				trackInfo.setAlbum(object.getAlbum());
				trackInfo.setArtist(object.getArtist());
				trackInfo.setFileID(object.getID());
				trackInfo.setDuration(mPlayer.getDuration());
				trackInfo.setTrackID(mStatus.getPlayFilePos());
				trackInfo.setTrackTotal(mStatus.mListPlay.size());
				byte[] buf = mPlayer.getApicData();
				boolean bHasBmp = false;
				if (buf != null && buf.length > 0) {
					Bitmap bmpSrc = BitmapFactory.decodeByteArray(buf, 0, buf.length);
					if (bmpSrc != null) {
						int bmpW = bmpSrc.getWidth();
						int bmpH = bmpSrc.getHeight();
						if (bmpW > 0 && bmpH > 0) {
							Matrix m = new Matrix();
							m.postScale((float) DEFAULT_APIC_W / bmpW, (float) DEFAULT_APIC_H / bmpH);
							trackInfo.setApicBmp(Bitmap.createBitmap(bmpSrc, 0, 0, bmpW, bmpH, m, true));
							bHasBmp = true;
							mSaveData.setMediaArtWork(trackInfo.getApicBmp());
						} else {
							Log.e(TAG, String.format("error size bmpW:%d, bmpH:%d", bmpW, bmpH));
						}
					}
				}
				if (!bHasBmp) {
					mSaveData.setMediaArtWork(null);
				}
				// Savedata
				try {
					mSaveData.setMediaSongName(trackInfo.getName());
					mSaveData.setMediaTrack(trackInfo.getTrackID());
					mSaveData.setMediaTotalNum(trackInfo.getTrackTotal());
					mSaveData.setMediaPlayTime(trackInfo.getDuration());
					mSaveData.setMediaPlayType(mStatus.iMediaType);
					int iStatus = getPlayStatus();
					mSaveData.setMediaPlaySts(iStatus == PlayStatus.PAUSED || iStatus == PlayStatus.IDLE ? 0x00 : 0x01);
					mSaveData.setMediaID3Author(trackInfo.getArtist());
					mSaveData.setMediaID3Album(trackInfo.getAlbum());
				} catch (Exception e) {
					e.printStackTrace();
				}
				mStatus.mPlayingTrackInfo = trackInfo;
				if (mStatus.mPlayingTrackInfo != null) {
					Log.i(TAG, " -- updatePlayingFileInfo() name:" + mStatus.mPlayingTrackInfo.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public MediaTrackInfo getPlayingFileInfo() throws RemoteException {
		synchronized (TAG) {
			if (mStatus.mPlayingTrackInfo != null) {
				mStatus.mPlayingTrackInfo.setCurTime(mStatus.getPlayProgress());
			}
			return mStatus.mPlayingTrackInfo;
		}
	}

	@Override
	public MediaStorage getPlayingStorage() throws RemoteException {
		synchronized (TAG) {
			return mStorageState.getPlayingStorage();
		}
	}

	private String getStorageState(String path) {
		return DataShared.getInstance(mContext).getString(path, MediaScanConstans.ACTION_SCAN_CANCEL);
	}

	public boolean isSystemOnOff() {
		boolean bRet = false;
		if (SystemClock.uptimeMillis() - mlSystemOnOffTime < IGNORE_TIME) {
			Log.e(TAG, "isSystemOnOff:true");
			bRet = true;
		} else {
			Log.e(TAG, "isSystemOnOff:false");
		}
		return bRet;
	}

	@Override
	public Bitmap parseApicData(String path, int iWidth, int iHeight) throws RemoteException {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		byte[] buf = mPlayer.parseApic(path, false);
		Bitmap bitmap = null;
		if (buf != null && buf.length > 0) {
			Bitmap bmpSrc = BitmapFactory.decodeByteArray(buf, 0, buf.length);
			if (bmpSrc != null) {
				int bmpW = bmpSrc.getWidth();
				int bmpH = bmpSrc.getHeight();
				if (bmpW > 0 && bmpH > 0) {
					Matrix m = new Matrix();
					m.postScale((float) iWidth / bmpW, (float) iHeight / bmpH);
					bitmap = Bitmap.createBitmap(bmpSrc, 0, 0, bmpW, bmpH, m, true);
				}
			}
		}
		return bitmap;
	}

	@Override
	public int getFilePos(int iMediaType) throws RemoteException {
		return mStatus.getMediaStatus(iMediaType).iFilePos;
	}
	
	@Override
	public int getSearchPos(int iListType) throws RemoteException {
		int iSearchPos = 0;
		switch (iListType) {
		case ListType.ALL_ARTIST:
		case ListType.ARTIST_MIX:
			iSearchPos =  mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurArtistPos;
			break;
		case ListType.ALL_ALBUM:
		case ListType.ALBUM_MIX:
			iSearchPos =  mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).iCurAlbumPos;
			break;
		case ListType.MUSIC_MIX:
		case ListType.VIDEO_MIX:
		case ListType.IMAGE_MIX:
			iSearchPos = mStatus.getMediaStatus(iListType - 3).iDirPos;
			break;
		case ListType.ALL_IMAGE_FILE:
		case ListType.ALL_MUSIC_FILE:
		case ListType.ALL_VIDEO_FILE:
			iSearchPos = mStatus.getMediaStatus(iListType).iFilePos;
			break;
		default:
			break;
		}
		return iSearchPos;
	}

	@Override
	public String getFileName(int iMediaType) {
		return mStatus.getMediaStatus(iMediaType).strFile;
	}

	@Override
	public void requestPlayImage(int iPos) throws RemoteException {
		if (iPos >= 0) {
			mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).iFilePos = iPos;
			mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).strFile = mStatus.getMediaStatus(MediaType.MEDIA_IMAGE).mListFile
					.get(iPos).getFilePath();
			mStatus.writePreference(mContext, mDBFile);
		}
	}

	@Override
	public void requestAudioFocus() {
		if (!mbLostbAudioFocusForever && !mbOccupyAudioFocus && mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
			// 如果临时焦点丢失并且是静音状态，请求焦点
			if (mAudioManager != null) {
				int iStatus = mAudioManager.requestAudioFocus(mFocusChangeListener, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_GAIN);
				mbOccupyAudioFocus = true;
				Log.i(TAG, "requestAudioFocus iStatus:" + iStatus);
			}
		} else {
			RegisterSource();
		}
	}

	@Override
	public void updateLrcList(ArrayList<LrcContent> list, int iStatus) {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			iMediaPlayerActivity.updateLrcList(list, iStatus);
		}
	}

	@Override
	public void updateLrcIndex(int iStatus, int iIndex) {
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			iMediaPlayerActivity.updateLrcIndex(iStatus, iIndex);
		}
	}

	@Override
	public int getLrcIndex() throws RemoteException {
		if (mPlayer != null) {
			return mPlayer.getLrcIndex();
		}
		return 0;
	}

	@Override
	public ArrayList<LrcContent> getLrcList() throws RemoteException {
		if (mPlayer != null) {
			return mPlayer.getLrcList();
		}
		return null;
	}

	private void parseAudioPath(String strPath) {
		try {
			if (strPath != null && mStorageState.mListStorage.size() > 0) {
				String diskPath = "";
				if (strPath.contains(MediaScanConstans.UDISK1_PATH)) {
					diskPath = MediaScanConstans.UDISK1_PATH;
				} else if (strPath.contains(MediaScanConstans.UDISK2_PATH)) {
					diskPath = MediaScanConstans.UDISK2_PATH;
				} else if (strPath.contains(MediaScanConstans.EXTERNAL_PATH)) {
					diskPath = MediaScanConstans.EXTERNAL_PATH;
				}
				if (!diskPath.equals("")) {
					setSource(diskPath);
					RegisterSource();
					if (mStorageState != null && diskPath.equals(mStorageState.getRecoverStorage().getPath())) {
						requestPlayFile(strPath);
					} else {
						mPathBySpeech = strPath;
						requestAttachStorage(diskPath, MediaType.MEDIA_AUDIO);
					}
				}
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void requestPlayFile(String strPath) throws RemoteException {
		if (strPath != null && !strPath.equals("")) {
			if (mDBFile != null) {
				Cursor cursor = null;
				int iFileID = MediaPlayerContants.ID_INVALID;
				// song, select file
				Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
				String[] columns = MediaPlayerContants.FILE_COLUMNS;
				String selection = YeconMediaFilesColumns.DATA + "=" + "'" + strPath + "'";
				String[] selectionArgs = null;
				cursor = mListQueryHandler.startQuery(uri, columns, selection, selectionArgs, null);
				if (cursor != null) {
					cursor.moveToFirst();
					if (cursor.getCount() > 0) {
						iFileID = cursor.getInt(0);
					}
					cursor.close();
				}

				int iPosition = 0;
				if (iFileID != MediaPlayerContants.ID_INVALID) {
					if (!mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile.isEmpty()) {
						for (MediaObject object : mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile) {
							if (object.getID() == iFileID) {
								break;
							}
							iPosition++;
						}
						iPosition = iPosition % mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile.size();
						if (mStatus.iMediaType != MediaType.MEDIA_AUDIO) {
							updateServiceStatus(ServiceStatus.SWITCH_STORAGE, null);
						}
						// MediaScanConstans.runApp(mContext, null);
						requestPlayList(MediaType.MEDIA_AUDIO, ListType.ALL_MUSIC_FILE, iPosition, strPath);
					}
				}
			}
		}
	}

	@Override
	public void requestVideoPlay() {
		synchronized (TAG) {
			int state = mPlayer.getCurrentRealState();
			Log.e(TAG, "++requestVideoPlay++  state:" + state);
			if (state != PlayStatus.STARTED) {
				decode(mStatus.strFile);
				if (mStatus.getPlayProgress() > 0) {
					mPlayer.setRecoverProgress(mStatus.getPlayProgress());
				}
			}
		}
	}

	public static List<String> mImageUrlList = new ArrayList<String>();

	public void updateImageUrl(List<MediaObject> list) {
		mImageUrlList.clear();
		if (list != null && list.size() > 0) {
			for (MediaObject mediaObject : list) {
				mImageUrlList.add(MediaScanConstans.URL_HEAD + mediaObject.getFilePath());
			}
		}
	}

	private class RecorverThread extends Thread {
		private int miRecoverMedia = 0;

		public RecorverThread(int iRecover) {
			miRecoverMedia = iRecover;
		}

		@Override
		public void run() {
			miWaitDatabase = WAIT_LOADING;
			String path = getName();
			LogUtil.printError(TAG, LogUtil._FUNC_(), "++mRecorverThread:" + path);
			// attach storage
			mStorageState.AttachStorage(path);
			// wait for finish scaning
			while (!isInterrupted()) {
				if (miWaitDatabase == WAIT_FINISH) {
					break;
				} else {
					if (miWaitDatabase-- == WAIT_FINISH + 1) {
						updateServiceStatus(ServiceStatus.SCAN_TIMEOUT, path);
					}
				}
				try {
					String action = getStorageState(path);
					if (action.equals(MediaScanConstans.ACTION_SCAN_ANALYSIS)
							|| action.equals(MediaScanConstans.ACTION_SCAN_FINISH)) {
						LogUtil.printError(TAG, LogUtil._FUNC_(),
								"finish scan!!! path:" + path + "->" + miWaitDatabase);
						break;
					}
					Thread.sleep(1000);
					if (WAIT_LOADING - miWaitDatabase > WAIT_MOUNT
							&& !MediaScanConstans.checkStorageExist(mEnv, path)) {
						// wait for device mount failed
						LogUtil.printError(TAG, LogUtil._FUNC_(), "wait for device Mount failed!!! path:" + path);
						updateServiceStatus(ServiceStatus.LOST_CUR_STORAGE, path);
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			miWaitDatabase = WAIT_FINISH;
			if (mStorageState != null && mStorageState.getPlayingStorage() != null
					&& mStorageState.getPlayingStorage().getPath().equals(path)) {
				// 先接入u1，u1加载时再接入u2，加载完成后，界面在u2音乐列表，但媒体源图标显示为u1
				recoverStorage(path, miRecoverMedia);
			}
			LogUtil.printError(TAG, LogUtil._FUNC_(), "--mRecorverThread:" + path);
		}
	};

	private class MediaStorageState {
		private List<MediaStorage> mListStorage = new ArrayList<MediaStorage>();

		public MediaStorageState() {
			// 初始化 磁盘列表
			mListStorage.clear();
			for (String path : MediaScanConstans.STORAGES) {
				String scan = getStorageState(path);
				if (!scan.equals(MediaScanConstans.ACTION_SCAN_CANCEL)) {
					if (MediaScanConstans.checkStorageExist(mEnv, path)) {
						mListStorage.add(new MediaStorage(path, scan));
					}
					LogUtil.printError(TAG, LogUtil._FUNC_(), path + ":" + scan);
				}
			}
		}

		private void UpdateStorage(String path, String state) {
			synchronized (mListStorage) {
				boolean bFindPath = false;
				for (MediaStorage mediaStorage : mListStorage) {
					if (path.equals(mediaStorage.getPath())) {
						bFindPath = true;
						if (state.equals((MediaScanConstans.ACTION_SCAN_CANCEL))) {
							mListStorage.remove(mediaStorage);
						} else {
							mediaStorage.setState(state);
						}
						break;
					}
				}
				if (!bFindPath) {
					if (!state.equals(MediaScanConstans.ACTION_SCAN_CANCEL)) {
						// 插入U盘后立刻拔出，这里的状态会设置不进去，导致判断磁盘状态异常，统一跟着广播走，不在手动检测
						mListStorage.add(new MediaStorage(path, state));
					}
				}
				LogUtil.printError(TAG, LogUtil._FUNC_(), path + ":" + state);
			}
		}

		public void AttachStorage(String path) {
			synchronized (mListStorage) {
				path = MediaScanConstans.getStoragePath(path);
				for (MediaStorage mediaStorage : mListStorage) {
					mediaStorage.setRecover(false);
					mediaStorage.setPlaying(mediaStorage.getPath().equals(path));
					LogUtil.printError(TAG, LogUtil._FUNC_(), "set playing srotage:" + path);
				}
			}
		}

		public void RecoverStorage(String path) {
			synchronized (mListStorage) {
				path = MediaScanConstans.getStoragePath(path);
				for (MediaStorage mediaStorage : mListStorage) {
					mediaStorage.setRecover(mediaStorage.getPath().equals(path));
					LogUtil.printError(TAG, LogUtil._FUNC_(), "set Recover srotage:" + path);
				}
			}
		}

		public MediaStorage getPlayingStorage() {
			synchronized (mListStorage) {
				for (MediaStorage mediaStorage : mListStorage) {
					if (mediaStorage.getPlaying()) {
						return mediaStorage;
					}
				}
				return null;
			}
		}

		public MediaStorage getRecoverStorage() {
			synchronized (mListStorage) {
				for (MediaStorage mediaStorage : mListStorage) {
					if (mediaStorage.getRecover()) {
						return mediaStorage;
					}
				}
				return null;
			}
		}
	}

	// 媒体播放状态
	private class MediaStatus {
		// 媒体播放类型
		private int iMediaType = MediaType.MEDIA_AUDIO;
		private String strFile = "";
		// 当前播放的文件目录
		private String strParent = "";
		// 当前播放文件信息
		private MediaTrackInfo mPlayingTrackInfo;
		private List<MediaObject> mListPlay = new ArrayList<MediaObject>();
		private List<MediaPlayStatus> mListStatus = new ArrayList<MediaPlayStatus>();

		public MediaStatus() {
			mListStatus.add(new MediaPlayStatus(MediaType.MEDIA_AUDIO));
			mListStatus.add(new MediaPlayStatus(MediaType.MEDIA_VIDEO));
			mListStatus.add(new MediaPlayStatus(MediaType.MEDIA_IMAGE));
		}

		public MediaPlayStatus getMediaStatus(int iMediaType) {
			if (iMediaType == MediaType.MEDIA_VIDEO) {
				return mListStatus.get(1);
			} else if (iMediaType == MediaType.MEDIA_IMAGE) {
				return mListStatus.get(2);
			}
			return mListStatus.get(0);
		}

		public int getPlayFilePos() {
			return getMediaStatus(iMediaType).iFilePos;
		}

		public void setPlayFilePos(int iPos) {
			getMediaStatus(iMediaType).iFilePos = iPos;
		}

		public int getPlayProgress() {
			int iProgress = getMediaStatus(iMediaType).iProgress;
			return iProgress;
		}

		public void setPlayProgress(int iprogress) {
			getMediaStatus(iMediaType).iProgress = iprogress;
		}

		public String getPlayFile() {
			String strFile = getMediaStatus(iMediaType).strFile;
			return strFile;
		}

		public void setPlayFile(String strFile) {
			this.strFile = strFile;
			getMediaStatus(iMediaType).strFile = strFile;
		}

		private void readPreference(Context context, String strDBPath) {
			if (strDBPath != null) {
				Log.e(TAG, "readPreference : " + strDBPath);
				try {
					SharedPreferences sp = context.getSharedPreferences(strDBPath, Context.MODE_PRIVATE);
					iMediaType = sp.getInt("iMediaType", MediaType.MEDIA_AUDIO);
					strFile = sp.getString("strFile", "");
					strParent = sp.getString("strParent", "");
					for (MediaPlayStatus mediaPlayStatus : mListStatus) {
						mediaPlayStatus.readPreference(sp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				printPreference();
			}
		}

		private void clearPreference(Context context, String strDBPath) {
			if (isSystemOnOff()) {
				Log.e(TAG, "machine loading, ignore clearPreference : " + strDBPath);
			} else {
				if (mDBFile != null && mDBFile.equals(strDBPath)) {
					iMediaType = MediaType.MEDIA_AUDIO;
					strFile = "";
					strParent = "";
					for (MediaPlayStatus mediaPlayStatus : mListStatus) {
						mediaPlayStatus.clearPreference();
					}
				}
				SharedPreferences sp = context.getSharedPreferences(strDBPath, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.clear();
				editor.commit();
			}
		}

		private void writePreference(Context context, String strDBPath) {
			if (strDBPath != null && !strDBPath.equals("")) {
				Log.e(TAG, "writePreference : " + strDBPath);
				SharedPreferences sp = context.getSharedPreferences(strDBPath, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("iMediaType", iMediaType);
				editor.putString("strFile", strFile);
				editor.putString("strParent", strParent);
				for (MediaPlayStatus mediaPlayStatus : mListStatus) {
					mediaPlayStatus.writePreference(editor);
				}
				editor.commit();
				printPreference();
			}
		}

		private void printPreference() {
			LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: mStatus.iMediaType :" + mStatus.iMediaType);
			LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: mStatus.strFile : " + mStatus.strFile);
			LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: mStatus.strParent : " + mStatus.strParent);
		}
	}

	// 媒体播放状态
	private class MediaPlayStatus {
		// 媒体播放类型
		private int iMediaType = MediaType.MEDIA_AUDIO;
		private int iRepeatMode = RepeatMode.ALL;
		// 当前曲目在当前列表的索引
		private int iFilePos = MediaPlayerContants.ID_INVALID;
		private int iDirPos = MediaPlayerContants.ID_INVALID;
		// 当前曲目的播放进度
		private int iProgress = 0;
		// 当前播放的文件路径
		private String strFile = "";
		private int iListType = ListType.ALL_MUSIC_FILE;
		private int iPlayType = ListType.ALL_MUSIC_FILE;

		private List<MediaObject> mListFile = new ArrayList<MediaObject>();
		private List<MediaObject> mListDir = new ArrayList<MediaObject>();
		private List<MediaObject> mListMix = new ArrayList<MediaObject>();
		
		private List<MediaObject> mListArtist = new ArrayList<MediaObject>();
		private List<MediaObject> mListAlbum = new ArrayList<MediaObject>();
		private List<MediaObject> mListArtistMix = new ArrayList<MediaObject>();
		private List<MediaObject> mListAlbumMix = new ArrayList<MediaObject>();
		private List<MediaObject> mListArtistFile = new ArrayList<MediaObject>();
		private List<MediaObject> mListAlbumFile = new ArrayList<MediaObject>();
		
		// 当前选中的文件夹列表position
		private int iCurDirPos = MediaPlayerContants.ID_INVALID;
		
		// 当前正在播放专辑列表的列表position
		private int iAlbumPos = MediaPlayerContants.ID_INVALID;
		// 当前选中的专辑列表position
		private int iCurAlbumPos = MediaPlayerContants.ID_INVALID;
		
		// 当前正在播放艺术家列表的列表position
		private int iArtistPos = MediaPlayerContants.ID_INVALID;
		// 当前选中的的艺术家列表position
		private int iCurArtistPos = MediaPlayerContants.ID_INVALID;

		public MediaPlayStatus(int iMediaType) {
			this.iMediaType = iMediaType;
			this.iListType = iMediaType;
			this.iPlayType = iMediaType;
		}

		private void readPreference(SharedPreferences sp) {
			iRepeatMode = sp.getInt("iRepeatMode" + iMediaType, RepeatMode.ALL);
			iFilePos = sp.getInt("iFilePos" + iMediaType, MediaPlayerContants.ID_INVALID);
			iDirPos = sp.getInt("iDirPos" + iMediaType, MediaPlayerContants.ID_INVALID);
			iProgress = sp.getInt("iProgress" + iMediaType, 0);
			strFile = sp.getString("strFile" + iMediaType, "");
//			iListType = sp.getInt("iListType" + iMediaType, iListType);
//			iPlayType = sp.getInt("iPlayType" + iMediaType, iPlayType);
			iCurArtistPos = sp.getInt("iArtistPos" + iMediaType, iArtistPos);
			iCurAlbumPos = sp.getInt("iAlbumPos" + iMediaType, iAlbumPos);
			printPreference();
		}

		private void clearPreference() {
			iRepeatMode = RepeatMode.ALL;
			iFilePos = MediaPlayerContants.ID_INVALID;
			iDirPos = MediaPlayerContants.ID_INVALID;
			iProgress = 0;
			strFile = "";
			iListType = iMediaType;
			iPlayType = iMediaType;
			iAlbumPos = MediaPlayerContants.ID_INVALID;
			iCurAlbumPos = MediaPlayerContants.ID_INVALID;
			iArtistPos = MediaPlayerContants.ID_INVALID;
			iCurArtistPos = MediaPlayerContants.ID_INVALID;
		}

		private void writePreference(SharedPreferences.Editor editor) {
			editor.putInt("iRepeatMode" + iMediaType, iRepeatMode);
			editor.putInt("iFilePos" + iMediaType, iFilePos);
			editor.putInt("iDirPos" + iMediaType, iDirPos);
			editor.putInt("iProgress" + iMediaType, iProgress);
			editor.putString("strFile" + iMediaType, strFile);
//			editor.putInt("iListType" + iMediaType, iListType);
//			editor.putInt("iPlayType" + iMediaType, iPlayType);
			editor.putInt("iAlbumPos" + iMediaType, iAlbumPos);
			editor.putInt("iCurAlbumPos" + iMediaType, iCurAlbumPos);
			editor.putInt("iArtistPos" + iMediaType, iArtistPos);
			editor.putInt("iCurArtistPos" + iMediaType, iCurArtistPos);
			printPreference();
		}

		private void printPreference() {
			if (iMediaType == MediaType.MEDIA_AUDIO) {
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iMediaType :" + iMediaType);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.strFile : " + strFile);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iFilePos : " + iFilePos);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iPlayType : " + iPlayType);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iAlbumPos : " + iAlbumPos);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iCurAlbumPos : " + iCurAlbumPos);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iArtistPos : " + iArtistPos);
				LogUtil.printError(TAG, LogUtil._FUNC_(), "[state]: MediaPlayStatus.iCurArtistPos : " + iCurArtistPos);
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_RECOVER_PLAY:
			recoverAudio();
			break;
		case MSG_DOWNLOAD_FILE:
			copyFile((String) msg.obj);
			break;
		case MSG_RETURN_INDEX:
			int index = mStatus.getMediaStatus(mStatus.iMediaType).iFilePos;
			Intent sendIntent = new Intent(MediaPlayerContants.AUTOMATION_MEDIA_BROADCAST_RECV);
			if (null != sendIntent) {
				sendIntent.putExtra("eventType", msg.arg1);
				sendIntent.putExtra("musicIndex", index + 1);
				mContext.sendBroadcast(sendIntent);
			}
			break;
		default:
			break;
		}
		return false;
	}

	private BroadcastReceiver mReceiverScan = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			synchronized (TAG) {
				try {
					if (intent != null) {
						LogUtil.printError(TAG, LogUtil._FUNC_(), "[mReceiverScan]" + intent);
						final String action = intent.getAction();
						if (action.equals(MediaScanConstans.ACTION_YECON_MEIDA_SCANER_STATUS)) {
							String strAction = intent.getStringExtra(MediaScanConstans.ACTION);
							String strPath = intent.getStringExtra(MediaScanConstans.PATH);
							if (strAction != null && strPath != null) {
								mStorageState.UpdateStorage(strPath, strAction);
								if (strAction.equals(MediaScanConstans.ACTION_SCAN_ANALYSIS)
										|| strAction.equals(MediaScanConstans.ACTION_SCAN_FINISH)) {
									try {
										if (strPath != null && strPath.equals(mStorageState.getRecoverStorage())) {
											getListFromDataBase();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else if (strAction.equals(MediaScanConstans.ACTION_SCAN_START)) {
									updateServiceStatus(ServiceStatus.SCANING, strPath);
								} else if (strAction.equals(MediaScanConstans.ACTION_SCAN_CANCEL)) {
									if (strPath != null && !strPath.equals(MediaScanConstans.getExternalPath())) {
										if (mDBFile.equals(getDBFile(strPath))) {
											mStatus.mPlayingTrackInfo = null;
											mStatus.mListPlay.clear();
											if (mPlayer != null) {
												if (strPath == null || mPlayer.getSourcePath().contains(strPath)) {
													// stop play
													mPlayer.release();
												}
											}
											mStatus.writePreference(mContext, mDBFile);
											miWaitDatabase = WAIT_FINISH;
											if (mRecoverThread != null) {
												mRecoverThread.interrupt();
												mRecoverThread = null;
											}
											mDBFile = "";
											updateServiceStatus(ServiceStatus.LOST_CUR_STORAGE, strPath);
										} else {
											updateServiceStatus(ServiceStatus.LOST_STORAGE, strPath);
										}
									}
								}
								LogUtil.printError(TAG, LogUtil._FUNC_(),
										"[mReceiverScan] Action:" + strAction + " Path:" + strPath);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	};

	private BroadcastReceiver mReceiverMCUKey = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (TAG) {
				String action = intent.getAction();
				try {
					if (action != null) {
						LogUtil.printError(TAG, LogUtil._FUNC_(), action);
						if (mPlayer != null && !mPlayer.isStop()
								&& ((SourceManager.getSource() == Constants.KEY_SRC_MODE_USB1 || 
									SourceManager.getSource() == Constants.KEY_SRC_MODE_USB2 || 
									SourceManager.getSource() == Constants.KEY_SRC_MODE_EXTERNAL))) {
							if (action.equals(MCU_ACTION_MEDIA_PLAY_PAUSE)) {
								requestPause();
							} else if (action.equals(MCU_ACTION_MEDIA_PAUSE) || action.equals(MCU_ACTION_MEDIA_STOP)) {
								if (mPlayer.getCurrentRealState() == PlayStatus.STARTED) {
									mPlayer.pause();
									mbPauseByUser = true;
								} else if (mbPauseByAudioFocus) {
									// 语音抢占了焦点
									mbPauseByUser = true;
								}
							}
							if (action.equals(MCU_ACTION_MEDIA_PLAY)) {
								if (mPlayer.getCurrentRealState() == PlayStatus.PAUSED) {
									mPlayer.play();
									mbPauseByUser = false;
								}
							} else if (action.equals(MCU_ACTION_MEDIA_NEXT)) {
								requestNext();
							} else if (action.equals(MCU_ACTION_MEDIA_PREVIOUS)) {
								requestPrev();
							} else if (action.equals(ACTION_MEDIA_RANDOM)) {
								int mode = intent.getIntExtra(ACTION_MEDIA_RANDOM, -1);
								Log.i(TAG, "ACTION_MEDIA_RANDOM mode:" + mode);
								if ((mode == 1 && getRepeatStatus() != RepeatMode.RANDOM)) {
									requestRepeat(RepeatMode.RANDOM);
								}
							} else if (action.equals(ACTION_MEDIA_REPEAT)) {
								int mode = intent.getIntExtra(ACTION_MEDIA_REPEAT, -1);
								Log.i(TAG, "ACTION_MEDIA_REPEAT mode:" + mode);
								if ((mode == RepeatMode.ALL || mode == RepeatMode.SINGLE
									|| (mRepeatOff && mode == RepeatMode.OFF) || mode == RepeatMode.DIR)) {
									requestRepeat(mode);
								}
							} else if (action.equals(MCU_ACTION_ACC_OFF)) {
								Log.i(TAG, " MCU_ACTION_ACC_OFF");
								mStatus.writePreference(mContext, mDBFile);
							}
						}
						if (action.equals(MediaPlayerContants.AUTOMATION_MEDIA_BROADCAST_SEND)) {
							handleAteCmd(intent);
						} else if (action.equals(YeconConstants.ACTION_BACKCAR_STOP)) {
							if (mStatus.iMediaType == MediaType.MEDIA_VIDEO && !mbLostbAudioFocusForever
									&& mPlayer != null && mPlayer.isInit()) {
								requestVideoPlay();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private BroadcastReceiver mExternalReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.printInfo(TAG, "mBMReceiverListener - action: ", action);
			if (BootService.ACTION_IFLY_VOICE_APP.equals(action)) {
				int operation = intent.getIntExtra(BootService.EXTRA_APP_OPERATION, 0);
				if (operation == 2) {
					int extra_app_id = intent.getIntExtra(BootService.EXTRA_APP_ID, 0);
					if (!mbLostbAudioFocusForever && 
						((extra_app_id == Constants.MUSIC_ID && mStatus.iMediaType == MediaType.MEDIA_AUDIO)
						|| (extra_app_id == Constants.VIDEO_ID && mStatus.iMediaType == MediaType.MEDIA_VIDEO))) {
						closeMedia();
					}
				}
			} else if (MediaPlayerContants.ACTION_IFLY_VOICE_MUSIC.equals(action)) {
			    int music_cmd = intent.getIntExtra(MediaPlayerContants.EXTRA_MUSIC_CMD, 0);
				if (music_cmd == MediaPlayerContants.MUSIC_CMD_PLAY_SONG) {
					String strPath = intent.getStringExtra(MediaPlayerContants.EXTRA_MUSIC_PATH);
					parseAudioPath(strPath);
					if (PageMedia.getView() != ViewType.ViewUsbMusic && PageMedia.getView() != ViewType.ViewUsbMusicFile ) {
						PageMedia.setView(ViewType.ViewUsbMusicFile);
					}
					launcherUtils.startMedia();// 音乐
				}else if (music_cmd == MediaPlayerContants.MUSIC_CMD_PLAY_SONG_BY_KW) {
				    //去酷我搜歌
                    String kw_name = intent.getStringExtra(MediaPlayerContants.EXTRA_KWMUSIC_NAME);
                    String kw_singer = intent.getStringExtra(MediaPlayerContants.EXTRA_KWMUSIC_SINGER);
                    String kw_album = intent.getStringExtra(MediaPlayerContants.EXTRA_KWMUSIC_ALBUM);
                    Log.i("xuhh", "kwmusic name=" + kw_name+", singer="+kw_singer+",album = "+kw_album);
                    Utils.getKwapi().playClientMusics(kw_name, kw_singer, kw_album);
                }
			} else if (MediaPlayerContants.ACTION_EASYCONN_ANDROID_IN.equals(action)
					|| MediaPlayerContants.ACTION_EASYCONN_ANDROID_RESUME.equals(action)
					|| MediaPlayerContants.ACTION_EASYCONN_IPHONE_IN.equals(action)
					|| MediaPlayerContants.ACTION_EASYCONN_IPHONE_RESUME.equals(action)
					|| MediaPlayerContants.ACTION_CARPLAY_INSERT.equals(action)) {
				Log.i(TAG, "EASYCONN action:" + action);
				if ((mStatus.iMediaType == MediaType.MEDIA_VIDEO) && mPlayer != null && mPlayer.isInit()) {
					requestStop();
					mbReleaseByEasyCon = true;
				}
			}
		}
	};

	public void startMedia(String strPath) {
		try {
			if (strPath != null && (strPath.equals(MediaScanConstans.UDISK1_PATH) 
					|| strPath.equals(MediaScanConstans.UDISK2_PATH) || strPath.equals(MediaScanConstans.EXTERNAL_PATH))) {
				if (!strPath.equals(getPlayingStorage())) {
					updateServiceStatus(ServiceStatus.SWITCH_STORAGE, null);
				}
				requestAttachStorage(strPath, MediaType.MEDIA_AUDIO);
			}
		} catch (Exception e) {

		}
	}

	private void handleAteCmd(Intent intent) {
		int type = intent.getIntExtra("eventType", 0);
		switch (type) {
		case 1:
			if(mMediaHandler != null) {
				Message msg = Message.obtain();
				msg.what = MSG_RETURN_INDEX;
				msg.arg1 = type;
				mMediaHandler.sendMessageDelayed(msg, 500);
			}
			break;
		case 2:
			// play mode.
			int playMode = intent.getIntExtra("playType", -1);
			
			switch (playMode) {
			case 1:
			case 2:
			case 4:
				try {
					requestRepeat(playMode);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			break;
		case 3:
			// Play media according to the index.
			int index = intent.getIntExtra("index", -1);
			// TODO play 
			if (index > 0) {
				// 索引从1开始
				if (mStatus.getMediaStatus(MediaType.MEDIA_AUDIO).mListFile.size() >= index) {
					List<MediaObject> list;
					try {
						list = getFileList(ListType.ALL_MUSIC_FILE);
						requestPlayList(MediaType.MEDIA_AUDIO, ListType.ALL_MUSIC_FILE, index - 1, list.get(index-1).getFilePath());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			// TODO return index
			if(mMediaHandler != null) {
				Message msg = Message.obtain();
				msg.what = MSG_RETURN_INDEX;
				msg.arg1 = type;
				mMediaHandler.sendMessageDelayed(msg, 500);
			}
			break;
		case 4:
			// Play prev or next
			int key = intent.getIntExtra("keyId", -1);
			int keyCode = KeyEvent.KEYCODE_UNKNOWN;
			if(key == 0x00) {
				keyCode = KeyEvent.KEYCODE_YECON_PREV;
			} else if(key == 0x01) {
				keyCode = KeyEvent.KEYCODE_YECON_NEXT;
			}
			if (KeyEvent.KEYCODE_UNKNOWN != keyCode) { 
				Utils.TransKey(keyCode);
				if(mMediaHandler != null) {
					Message msg = Message.obtain();
					msg.what = MSG_RETURN_INDEX;
					msg.arg1 = type;
					mMediaHandler.sendMessageDelayed(msg, 500);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void deleteFile(List<String> listPath, int iListType) {
		long time = SystemClock.uptimeMillis();
		synchronized (TAG) {
			try {
				Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
				for (String strPath : listPath) {
					if (strPath != null && !strPath.equals("")) {
						File file = new File(strPath);
						if (file == null || !file.exists() || file.isDirectory()) {
							return;
						}
						// file.delete();
						int iResult = mContext.getContentResolver().delete(uri, YeconMediaFilesColumns.DATA + "=?",
								new String[] { strPath });
						Log.i("deleteFile", " deleteFile string:" + strPath + ",iResult:" + iResult);
					}
				}
				if (iListType == ListType.ALL_MUSIC_FILE || iListType == ListType.ALL_VIDEO_FILE
						|| iListType == ListType.ALL_IMAGE_FILE) {
					queryFileList(false, iListType);
				} else if (iListType == ListType.MUSIC_MIX || iListType == ListType.VIDEO_MIX
						|| iListType == ListType.IMAGE_MIX) {
					queryDirList(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i("deleteFile", " deleteFile time" + (SystemClock.uptimeMillis() - time));
		}
	}

	@Override
	public void deleteFile(int iPos, int iMediaType) {
		long time = SystemClock.uptimeMillis();
		synchronized (TAG) {
			try {
				List<MediaObject> list = new ArrayList<MediaObject>();
				if (iMediaType == MediaType.MEDIA_IMAGE || iMediaType == MediaType.MEDIA_VIDEO) {
					list = mStatus.getMediaStatus(iMediaType).mListFile;
				} else {
					list = mStatus.mListPlay;
				}
				if (iPos >= 0 && iPos < list.size()) {
					String strPath = list.get(iPos).getFilePath();
					String strParent = list.get(iPos).getFileParent();
					String strArtist = list.get(iPos).getArtist();
					String strAlbum = list.get(iPos).getAlbum();
					
					if (strPath != null && !strPath.equals("")) {
						File file = new File(strPath);
						if (file == null || !file.exists() || file.isDirectory()) {
							return;
						}
						list.remove(iPos);
						if (iMediaType == MediaType.MEDIA_IMAGE) {
							mImageUrlList.remove(iPos);
						} else {
							if (iPos > 0) {
								mStatus.setPlayFilePos(iPos - 1);
							} else {
								mStatus.setPlayFilePos(list.size() - 1);
							}
							requestNext();
						}
						file.delete();

						Uri uri = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_FILES);
						int iResult = mContext.getContentResolver().delete(uri, YeconMediaFilesColumns.DATA + "=?",
								new String[] { strPath });
						if (iResult > 0 ) {
							queryFileList(false, iMediaType);
						}
						Log.i("deleteFile", " deleteFile string:" + strPath + ",iResult:" + iResult);
						// dir
						updateDirByDelete(strParent, iMediaType);
						if (iMediaType == MediaType.MEDIA_AUDIO) {
							// artist
							updateArtistByDelete(strArtist);
							// album
							updateAlbumByDelete(strAlbum);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i("deleteFile", " deleteFile time" + (SystemClock.uptimeMillis() - time));
		}
	}
	
	private void updateDirByDelete(String strDir, int iMediaType) {
		// dir
		Uri uriDir = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_DIR);
		String column = YeconMediaDirColumns.AMOUNT_AUDIO;
		if (iMediaType == MediaType.MEDIA_VIDEO) {
			column = YeconMediaDirColumns.AMOUNT_VIDEO;
		} else if (iMediaType == MediaType.MEDIA_IMAGE) {
			column = YeconMediaDirColumns.AMOUNT_IMAGE;
		}
		Cursor c = mContext.getContentResolver().query(uriDir, new String[] { column },
				YeconMediaDirColumns.DATA + "='" + strDir + "'", null, null);
		if (c != null) {
			c.moveToFirst();
			int iCount = c.getInt(0);
			c.close();
			ContentValues values = new ContentValues();
			values.put(column, iCount - 1);
			if (mContext.getContentResolver().update(uriDir, values, YeconMediaDirColumns.DATA + "=?",
					new String[] { strDir }) > 0) {
				queryDirList(false);
			}
		}
	}
	
	private void updateArtistByDelete(String strArtist) {
		if (strArtist != null && !strArtist.equals("")) {
			Uri uriArtist = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_ARTIST);
			Cursor cursorArtist = mContext.getContentResolver().query(uriArtist, new String[] { YeconMediaArtistColumns.AMOUNT },
					YeconMediaArtistColumns.NAME + "='" + strArtist + "'", null, null);
			if (cursorArtist != null) {
				cursorArtist.moveToFirst();
				int iCount = cursorArtist.getInt(0);
				cursorArtist.close();
				if (iCount > 1) {
					// 该艺术家曲目 大于1，则减一
					ContentValues values = new ContentValues();
					values.put(YeconMediaArtistColumns.AMOUNT, iCount - 1);
					mContext.getContentResolver().update(uriArtist, values, YeconMediaArtistColumns.NAME + "=?",
							new String[] { strArtist });
				} else {
					// 该艺术家曲目 小于等于1，则删除该条目
					if (mContext.getContentResolver().delete(uriArtist, YeconMediaArtistColumns.NAME + "=?",
							new String[] { strArtist }) > 0) {
						queryArtistList(false);
					}
				}
			}
		}
	}
	
	private void updateAlbumByDelete(String strAlbum) {
		if (strAlbum != null && !strAlbum.equals("")) {
			Uri uriAlbum = MediaScanConstans.getContent(mDBFile, MediaScanConstans.TABLE_ALBUM);
			Cursor cursorAlbum = mContext.getContentResolver().query(uriAlbum, new String[] { YeconMediaAlbumColumns.AMOUNT },
					YeconMediaAlbumColumns.NAME + "='" + strAlbum + "'", null, null);
			if (cursorAlbum != null) {
				cursorAlbum.moveToFirst();
				int iCount = cursorAlbum.getInt(0);
				cursorAlbum.close();
				if (iCount > 1) {
					// 该专辑曲目 大于1，则减一
					ContentValues values = new ContentValues();
					values.put(YeconMediaAlbumColumns.AMOUNT, iCount - 1);
					mContext.getContentResolver().update(uriAlbum, values, YeconMediaAlbumColumns.NAME + "=?",
							new String[] { strAlbum });
				} else {
					// 该专辑曲目 小于等于1，则删除该条目
					if (mContext.getContentResolver().delete(uriAlbum, YeconMediaAlbumColumns.NAME + "=?",
							new String[] { strAlbum }) > 0) {
						queryAlbumList(false);
					}
				}
			}
		}
	}
	
	@Override
	public void downloadFile(String strPath) {
		if (strPath != null) {
			mMediaHandler.removeMessages(MSG_DOWNLOAD_FILE);
			Message message = mMediaHandler.obtainMessage();
			message.what = MSG_DOWNLOAD_FILE;
			message.obj = strPath;
			mMediaHandler.sendMessage(message);
		}
	}
	
	private void copyFile(String strPath) {
		boolean bRet = MediaScanConstans.copyFile(strPath, MediaScanConstans.KWMUSIC_PATH);
		Uri uri = Uri.parse("file://" + MediaScanConstans.getExternalPath());
		mContext.sendBroadcast(new Intent(MediaScanConstans.ACTION_MEDIA_QUERY, uri));
		for (IMediaPlayerActivity iMediaPlayerActivity : mListUI) {
			iMediaPlayerActivity.updateDowndLoadState(bRet ? 1 : 0);
		}
	}
}