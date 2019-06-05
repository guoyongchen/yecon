package com.carocean.media.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.autochips.media.AtcMediaPlayer;
import com.autochips.media.AtcMediaPlayer.OnSetRateCompleteListener;
import com.carocean.media.constants.MediaPlayerContants;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaScanner;
import android.media.TimedText;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

@SuppressLint("NewApi")
public class MultiMediaPlayer implements OnCompletionListener, OnErrorListener, OnInfoListener, OnPreparedListener,
		OnSeekCompleteListener, OnVideoSizeChangedListener, OnSetRateCompleteListener, OnTimedTextListener, Callback {

	public final String[] PLAY_STATE = new String[] { "IDLE", "DECODING", "DECODED", "SEEKING", "STATED", "PAUSED",
			"STOPED", "ERROR", "FINISH", "REWIND" };

	// 外部直接创建的对象用此标志
	private boolean mbExternalFlag = false;

	public void setExternalFlag(boolean bFlag) {
		mbExternalFlag = bFlag;
	}

	public boolean getExternalFlag() {
		return mbExternalFlag;
	}

	public final String PROPERTY_KEY_CBMSOURCE = "persist.sys.cbmsource";
	public final int MSG_MEDIA_UPDATE_STATE = 0;
	public final int MSG_MEDIA_DECODE = 1;
	public final int MSG_MEDIA_PLAY = 2;
	public final int MSG_MEDIA_PAUSE = 3;
	public final int MSG_MEDIA_SEEK = 4;
	public final int MSG_MEDIA_STOP = 5;
	public final int MSG_MEDIA_FRONT = 6;
	public final int MSG_MEDIA_REAR = 7;
	public final int MSG_MEDIA_PROGRESS = 8;
	public final int MSG_MEDIA_INFO = 9;
	public final int MSG_MEDIA_UPDATE_LRC_INDEX = 10;
	public final int MSG_MEDIA_PARSE_LRC = 11;
	public final int MSG_MEDIA_START = 12;
	public final int MSG_MEDIA_UPDATE_LRC_LIST = 15;
	public final int MSG_MEDIA_UPDATE_INFO = 16;

	public static final int MEDIA_INFO_VIDEO_RENDERING_START = 1;
	public static final int MEDIA_INFO_NOT_SEEKABLE = 2;
	public static final int MEDIA_INFO_UNSUPPORTED_VIDEO = 3;
	public static final int MEDIA_INFO_UNSUPPORTED_AUDIO = 4;
	public static final int MEDIA_INFO_DIVXDRM_ERROR = 5;

	public static final int CAP_FILE_SEEK_UNSUPPORT = 0x00000001;
	public static final int CAP_FILE_FF_UNSUPPORT = CAP_FILE_SEEK_UNSUPPORT << 1;
	public static final int CAP_FILE_RW_UNSUPPORT = CAP_FILE_FF_UNSUPPORT << 1;
	public static final int CAP_VIDEO_RESOLUTION_UNSUPPORT = CAP_FILE_RW_UNSUPPORT << 1;
	public static final int CAP_VIDEO_BITRATE_UNSUPPORT = CAP_VIDEO_RESOLUTION_UNSUPPORT << 1;
	public static final int CAP_VIDEO_FRAMERATE_UNSUPPORT = CAP_VIDEO_BITRATE_UNSUPPORT << 1;
	public static final int CAP_VIDEO_CODEC_UNSUPPORT = CAP_VIDEO_FRAMERATE_UNSUPPORT << 1;
	public static final int CAP_VIDEO_PROFILE_LEVEL_UNSUPPORT = CAP_VIDEO_CODEC_UNSUPPORT << 1;
	public static final int CAP_AUDIO_BITRATE_UNSUPPORT = CAP_VIDEO_PROFILE_LEVEL_UNSUPPORT << 1;
	public static final int CAP_AUDIO_SAMPLERATE_UNSUPPORT = CAP_AUDIO_BITRATE_UNSUPPORT << 1;
	public static final int CAP_AUDIO_CODEC_UNSUPPORT = CAP_AUDIO_SAMPLERATE_UNSUPPORT << 1;
	public static final int CAP_AUDIO_PROFILE_LEVEL_UNSUPPORT = CAP_AUDIO_CODEC_UNSUPPORT << 1;

	public final int[] HANDLE_ACTION = new int[] { MSG_MEDIA_DECODE, MSG_MEDIA_PLAY, MSG_MEDIA_PAUSE, MSG_MEDIA_SEEK,
			MSG_MEDIA_STOP, MSG_MEDIA_FRONT, MSG_MEDIA_REAR, MSG_MEDIA_PROGRESS };

	public int DELAY_EXEC_TIME = 200; // ms
	public int DELAY_DECADE_TIME = 100; // ms

	/** @hide */
	public boolean DEBUG = true;

	private String TAG = getClass().getName();

	private MediaPlayer mSysMediaPlayer;
	private AtcMediaPlayer mAtcMediaPlayer;
	private boolean mbInitialize = false;
	private boolean mbCanSeek = false;
	private int mDuration = 0;
	private IMultiMediaPlayer mPlayerClient;
	private int miPlayState = PlayStatus.IDLE;
	private Context mContext = null;
	private Handler mHandler;
	private String mSourceFile = "";
	private WeakReference<SurfaceHolder> mSurfaceFront;
	protected int miProgress = 0;
	private LrcProcess mLrcProcess;
	private int miLrcStatus = 0;
	protected HandlerThread mMediaHandleThread;

	public MultiMediaPlayer(IMultiMediaPlayer playerClient, Context context) {
		mPlayerClient = playerClient;
		mContext = context;
		mHandler = new Handler(this);
		mLrcProcess = new LrcProcess();
		mMediaHandleThread = new HandlerThread("MediaPlayerService");
		mMediaHandleThread.start();
		mHandler = new Handler(mMediaHandleThread.getLooper(), this);
//		mMp3id3Parser = new Mp3ID3Parser(context);
		init();
	}

	public boolean isInit() {
		return mbInitialize;
	}

	private void init() {
		synchronized (TAG) {
			Log.e(TAG, "++init++:" + this);
			if (mSysMediaPlayer == null) {
				mSysMediaPlayer = new MediaPlayer();
				mSysMediaPlayer.setOnCompletionListener(this);
				mSysMediaPlayer.setOnErrorListener(this);
				mSysMediaPlayer.setOnInfoListener(this);
				mSysMediaPlayer.setOnPreparedListener(this);
				mSysMediaPlayer.setOnSeekCompleteListener(this);
				mSysMediaPlayer.setOnVideoSizeChangedListener(this);
			}

			if (mAtcMediaPlayer == null) {
				mAtcMediaPlayer = new AtcMediaPlayer(mSysMediaPlayer);
				mAtcMediaPlayer.openAudioOutput(AtcMediaPlayer.MEDIA_DEST_FRONT_REAR);
				mAtcMediaPlayer.setOnSetRateCompleteListener(this);
			}
			Log.e(TAG, "--init--");
//			initMCUManager();
		}
	}

	private void deinit() {
		synchronized (TAG) {
			Log.e(TAG, "++deinit++");
			mbCanSeek = false;
			mbInitialize = false;
			mHandler.removeCallbacksAndMessages(null);
			clearMessage();
			if (mAtcMediaPlayer != null) {
				if (mAtcMediaPlayer != null) {
					mAtcMediaPlayer.closeAudioOutput(AtcMediaPlayer.MEDIA_DEST_FRONT_REAR);
					mAtcMediaPlayer.clearDivxService();
				}
				mAtcMediaPlayer.setOnSetRateCompleteListener(null);
				mAtcMediaPlayer = null;
			}
			if (mSysMediaPlayer != null) {
				mSysMediaPlayer.setOnCompletionListener(null);
				mSysMediaPlayer.setOnErrorListener(null);
				mSysMediaPlayer.setOnInfoListener(null);
				mSysMediaPlayer.setOnPreparedListener(null);
				mSysMediaPlayer.setOnSeekCompleteListener(null);
				mSysMediaPlayer.reset();
				mSysMediaPlayer.release();
				mSysMediaPlayer = null;
			}
			Log.e(TAG, "--deinit--");
		}
	}

	private void clearMessage() {
		if (mHandler != null) {
			for (int iMsgWhat : HANDLE_ACTION) {
				mHandler.removeMessages(iMsgWhat);
			}
		}
	}

	public String getSourcePath() {
		return mSourceFile;
	}

	/**
	 * @Title: decode @Description: 播放文件 @throws
	 */
	public boolean decode(String strFile) {
		clearMessage();
		Message msg = Message.obtain();
		msg.what = MSG_MEDIA_DECODE;
		Bundle data = new Bundle();
		data.putString("file", strFile);
		msg.setData(data);
		return mHandler.sendMessageDelayed(msg, DELAY_DECADE_TIME);
	}

	private boolean execDecode(String strFile) {
		synchronized (TAG) {
			long time = SystemClock.uptimeMillis();
			Log.e(TAG, "++execDecode++ ->" + strFile + ", time:" + time);
			try {
				mHandler.removeCallbacks(mRunnable);
				mHandler.removeMessages(MSG_MEDIA_UPDATE_LRC_INDEX);
				mbCanSeek = false;
				miPlayState = PlayStatus.IDLE;
				mHandler.removeMessages(MSG_MEDIA_INFO);
				mHandler.sendEmptyMessage(MSG_MEDIA_INFO);
				File file = new File(strFile);
				mSourceFile = strFile;
				if (!file.exists()) {
					Log.e(TAG, "file is not exist!!!");
					miPlayState = PlayStatus.ERROR;
					updatePlayInfo(0);
					updatePlayStatus();
					return false;
				}
				if (mSysMediaPlayer != null) {
					mSysMediaPlayer.reset();
				} else {
					init();
				}
				setDuration(0);
				FileInputStream fileInputStream = new FileInputStream(file);
				mSysMediaPlayer.setDataSource(fileInputStream.getFD());
				fileInputStream.close();
				miPlayState = PlayStatus.DECODING;

//				mMp3id3Parser.AnalysisID3(strFile);
				if (mPlayerClient != null && mPlayerClient.getMediaPlayerType() == 
						MediaPlayerContants.MediaType.MEDIA_AUDIO) {
					if (!mCurPath.equals(strFile)) {
						mApicBuf = null;
						mApicBuf = parseApic(strFile, true);
					}
				}
				mbInitialize = prepare();
				if (mbInitialize) {
					Log.e(TAG, "decode : " + strFile + " success!");
				} else {
					miPlayState = PlayStatus.ERROR;
					Log.e(TAG, "decode : " + strFile + " failed!");
				}
			} catch (IllegalArgumentException e) {
				miPlayState = PlayStatus.ERROR;
				e.printStackTrace();
			} catch (SecurityException e) {
				miPlayState = PlayStatus.ERROR;
				e.printStackTrace();
			} catch (IllegalStateException e) {
				miPlayState = PlayStatus.ERROR;
				e.printStackTrace();
			} catch (IOException e) {
				mApicBuf = null;
				mCurPath = strFile;
				miPlayState = PlayStatus.ERROR;
				e.printStackTrace();
				Log.e(TAG, "file is IOException!!!");
			}
			updatePlayInfo(miPlayState == PlayStatus.ERROR ? 1 : 0);
			updatePlayStatus();
			Log.e(TAG, "--execDecode--" + mbInitialize + ", time:" + (SystemClock.uptimeMillis() - time));
			return mbInitialize;
		}
	}

	/**
	 * 设置前排视频显示位置
	 */
	public void setFrontDisplay(SurfaceHolder holder, int iProgress) {
		synchronized (TAG) {
			if (holder == null) {
				mSurfaceFront = null;
			} else {
				mSurfaceFront = new WeakReference<SurfaceHolder>(holder);
			}
			execFrontDisplay(iProgress);
		}
	}

	private void execFrontDisplay(int iProgress) {
		synchronized (TAG) {
			Log.e(TAG, "++execFrontDisplay miPlayState:" + miPlayState + ", mbInitialize:" + mbInitialize);
			SurfaceHolder holder = null;
			if (mbInitialize) {
				if (mSurfaceFront != null) {
					holder = mSurfaceFront.get();
				}
				if (holder != null) {
					Log.e(TAG, "actual set surface");
					mSysMediaPlayer.setDisplay(holder);
					if (miPlayState == PlayStatus.PAUSED && !mPlayerClient.isAllowPlay()) {
						try {
							// mSysMediaPlayer.seekTo(mSysMediaPlayer.getCurrentPosition());
							int iSeekTime = iProgress * 1000;
							if (iSeekTime >= 0 && iSeekTime <= mSysMediaPlayer.getDuration()) {
								mSysMediaPlayer.seekTo(iSeekTime);
							} else {
								mSysMediaPlayer.seekTo(0);
								mSysMediaPlayer.seekTo(iProgress);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					mSysMediaPlayer.setDisplay(null);
				}
			} else {
				if (mSysMediaPlayer != null) {
					mSysMediaPlayer.setDisplay(null);
				}
			}
			Log.e(TAG, "--execFrontDisplay:" + holder);
		}
	}

	/**
	 * 设置后排视频显示位置
	 * 
	 * @param holder
	 */
	public void setRearDisplay(SurfaceHolder holder) {
		
	}

	/**
	 * @Title: play
	 * @Description: 开始播放
	 */
	public void play() {
		mHandler.removeMessages(MSG_MEDIA_PLAY);
		mHandler.removeMessages(MSG_MEDIA_PAUSE);
		mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PLAY, DELAY_EXEC_TIME);
	}

	private void execPlay() {
		// 请求焦点必须放在外面 不然会造成死锁
		mPlayerClient.requestAudioFocus();
		synchronized (TAG) {
			Log.e(TAG, "execPlay:" + mbInitialize);
			try {
				mSysMediaPlayer.start();
				miPlayState = PlayStatus.STARTED;
				updatePlayStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Title: pause
	 * @Description: 暂停播放
	 */
	public void pause() {
		mHandler.removeMessages(MSG_MEDIA_PLAY);
		mHandler.removeMessages(MSG_MEDIA_PAUSE);
		mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PAUSE, DELAY_EXEC_TIME);
	}

	private void execPause() {
		synchronized (TAG) {
			Log.e(TAG, "execPause");
			try {
				if (mbInitialize && (miPlayState == PlayStatus.STARTED || miPlayState == PlayStatus.SEEKING)) {
					mSysMediaPlayer.pause();
					miPlayState = PlayStatus.PAUSED;
					updatePlayStatus();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Title: stop
	 * @Description: 停止播放
	 */
	public void stop() {
		synchronized (TAG) {
			Log.e(TAG, "stop");
			try {
				mHandler.removeMessages(MSG_MEDIA_PROGRESS);
				if (mbInitialize) {
					mSysMediaPlayer.stop();
					miPlayState = PlayStatus.STOPED;
					updatePlayStatus();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Title: seek
	 * @Description: 跳转到msec时间点
	 */
	public void seek(int msec) {
		Log.e(TAG, "seek");
		Message msg = Message.obtain();
		msg.what = MSG_MEDIA_SEEK;
		msg.arg1 = msec;
		mHandler.removeMessages(MSG_MEDIA_SEEK);
		mHandler.sendMessageDelayed(msg, DELAY_EXEC_TIME / 2);
	}

	public void setRecoverProgress(int iProgress) {
		miProgress = iProgress;
	}

	private void execSeek(int msec) {
		synchronized (TAG) {
			msec *= 1000;
			Log.e(TAG, "execSeek to " + msec + " mbInitialize:" + mbInitialize + ", mbCanSeek:" + mbCanSeek);
			if (mbCanSeek) {
				try {
					mSysMediaPlayer.seekTo(msec);
					miPlayState = PlayStatus.SEEKING;
					updatePlayStatus();
					mPlayerClient.updatePlayProgress(msec / 1000, getDuration());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void reset() {
		Log.e(TAG, "reset");
		synchronized (TAG) {
			clearMessage();
			if (mSysMediaPlayer != null) {
				mSysMediaPlayer.reset();
			}
		}
		miPlayState = PlayStatus.IDLE;
		updatePlayStatus();
	}

	public void release() {
		Log.e(TAG, "release");
		deinit();
		miPlayState = PlayStatus.IDLE;
		updatePlayStatus();
		mbCanSeek = false;
		mbInitialize = false;
	}

	private boolean prepare() {
		synchronized (TAG) {
			Log.e(TAG, "prepare");
			boolean bRet = false;
			try {
				mSysMediaPlayer.prepare();
				bRet = true;
			} catch (IllegalStateException e) {
				e.printStackTrace();
				Log.e(TAG, "illegal state!!!");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "file prepare IOException!!!");
			}
			return bRet;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.e(TAG, "onCompletion!!!");
		if (mbInitialize) {
			mbCanSeek = false;
			miPlayState = PlayStatus.FINISH;
			if (mPlayerClient != null && mPlayerClient.getMediaPlayerType() == MediaType.MEDIA_AUDIO) {
				mHandler.removeCallbacks(mRunnable);
			}
			updatePlayStatus();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "onError what:" + what + " extra:" + extra + "mbInitialize:" + mbInitialize);
		if (mbInitialize) {
			mbCanSeek = false;
			mbInitialize = false;
			miPlayState = PlayStatus.ERROR;
			updatePlayInfo(1);
			updatePlayStatus();
		}
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
		Log.e(TAG, "onInfo what:" + whatInfo + " extra:" + extra);
		Message msg = Message.obtain();
		msg.what = MSG_MEDIA_INFO;
		switch (whatInfo) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			break;

		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			break;

		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			break;

		case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			Log.i(TAG, "onInfo - MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START");
			mHandler.removeMessages(MSG_MEDIA_INFO);
			msg.arg1 = MEDIA_INFO_VIDEO_RENDERING_START;
			break;

		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			// not seekable
			msg.arg1 = MEDIA_INFO_NOT_SEEKABLE;
			break;

		case AtcMediaPlayer.MEDIA_INFO_DIVX: // add by mtk94107
			Log.i(TAG, " MEDIA_INFO_DIVX Divx menu file");
			break;

		case AtcMediaPlayer.MEDIA_INFO_UNSUPPORTED_VIDEO: {
			msg.arg1 = MEDIA_INFO_UNSUPPORTED_VIDEO;
			if ((extra & CAP_VIDEO_RESOLUTION_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO -- Video Resolution not supported!");
				msg.arg2 = CAP_VIDEO_RESOLUTION_UNSUPPORT;
			} else if ((extra & CAP_VIDEO_BITRATE_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO -- Video Bitrate not supported!");
				msg.arg2 = CAP_VIDEO_BITRATE_UNSUPPORT;
			} else if ((extra & CAP_VIDEO_FRAMERATE_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO -- Video Frame Rate not supported!");
				msg.arg2 = CAP_VIDEO_FRAMERATE_UNSUPPORT;
			} else if ((extra & CAP_VIDEO_CODEC_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO -- Video Format not supported!");
				msg.arg2 = CAP_VIDEO_CODEC_UNSUPPORT;
			} else if ((extra & CAP_VIDEO_PROFILE_LEVEL_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO -- Video Profile Level not supported!");
				msg.arg2 = CAP_VIDEO_PROFILE_LEVEL_UNSUPPORT;
			} else {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_VIDEO, extra = " + extra);
			}
		}
			break;

		case AtcMediaPlayer.MEDIA_INFO_UNSUPPORTED_AUDIO: {
			msg.arg1 = MEDIA_INFO_UNSUPPORTED_AUDIO;
			if ((extra & CAP_AUDIO_CODEC_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_AUDIO -- Audio Format not supported!");
				msg.arg2 = CAP_AUDIO_CODEC_UNSUPPORT;
			} else if ((extra & CAP_AUDIO_BITRATE_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_AUDIO -- Audio Bitrate not supported!");
				msg.arg2 = CAP_AUDIO_BITRATE_UNSUPPORT;
			} else if ((extra & CAP_AUDIO_SAMPLERATE_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_AUDIO -- Audio Sampling Rate not supported!");
				msg.arg2 = CAP_AUDIO_SAMPLERATE_UNSUPPORT;
			} else if ((extra & CAP_AUDIO_PROFILE_LEVEL_UNSUPPORT) != 0) {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_AUDIO -- Audio Profile Level not supported!");
				msg.arg2 = CAP_AUDIO_PROFILE_LEVEL_UNSUPPORT;
			} else {
				Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_AUDIO, extra = " + extra);
			}
		}
			break;

		case AtcMediaPlayer.MEDIA_INFO_UNSUPPORTED_MENU:
			Log.i(TAG, "onInfo:MEDIA_INFO_UNSUPPORTED_MENU");
			break;

		case AtcMediaPlayer.MEDIA_INFO_DIVXDRM:
			Log.i(TAG, "onInfo  :MEDIA_INFO_DIVXDRM");
			// Log.i(TAG, "onInfo :mDivxDrmInfo.maxplayCount.:" +
			// mDivxDrmInfo.maxplayCount);
			// Log.i(TAG, "onInfo :mDivxDrmInfo.remainplayCount:" +
			// mDivxDrmInfo.remainPlayCount);
			// Log.i(TAG, "onInfo :mDivxDrmInfo.cgmsa]:" + mDivxDrmInfo.cgmsa);
			// Log.i(TAG, "onInfo :mDivxDrmInfo.acptb" + mDivxDrmInfo.acptb);
			// Log.i(TAG, "onInfo :mDivxDrmInfo.digitalpretection" +
			// mDivxDrmInfo.digitalProtection);
			// Log.i(TAG, "onInfo :mDivxDrmInfo.lict" + mDivxDrmInfo.lict);
			break;

		case AtcMediaPlayer.MEDIA_INFO_DIVXDRM_ERROR:
			if ((extra == AtcMediaPlayer.MEDIA_ERROR_DIVXDRM_NOT_AUTHORIZED)
					|| (extra == AtcMediaPlayer.MEDIA_ERROR_DIVXDRM_NOT_REGISTERED)
					|| (extra == AtcMediaPlayer.MEDIA_ERROR_DIVXDRM_NEVER_REGISTERED)) {
				Log.i(TAG, "onInfo :MEDIA_ERROR_DRM_NO_LICENSE");
			} else if (extra == AtcMediaPlayer.MEDIA_ERROR_DIVXDRM_RENTAL_EXPIRED) {
				Log.i(TAG, "onInfo :MEDIA_ERROR_DRM_RENTAL_EXPIRED");
			} else {
				Log.i(TAG, "onInfo, whatInfo=" + whatInfo + ", extra=" + extra);
			}
			msg.arg1 = MEDIA_INFO_DIVXDRM_ERROR;
			break;
		case AtcMediaPlayer.MEDIA_INFO_CBM_STOP:
			//此处状态不更新，如果更新的话，通话时，会被CBM暂停，然后结束通话会自动播放（通话时静音，挂断后自动恢复播放了，实际还是静音状态）
//			miPlayState = getCurrentRealState();
		case AtcMediaPlayer.MEDIA_INFO_CBM_PAUSE:
		case AtcMediaPlayer.MEDIA_INFO_CBM_FORBID:
		case AtcMediaPlayer.MEDIA_INFO_CBM_RESUME:
			updatePlayStatus();
			break;
		default:
			Log.i(TAG, "other onInfo:" + whatInfo);
		}

		if (msg.arg1 != 0) {
			mHandler.sendMessageDelayed(msg, DELAY_EXEC_TIME);
		}

		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.e(TAG, "onSeekComplete!!!");
		if (mbInitialize) {
			miPlayState = getCurrentRealState();
			if (miPlayState == PlayStatus.PAUSED && mPlayerClient != null) {
				int iProgress = mSysMediaPlayer.getCurrentPosition() / 1000;
				setDuration(mSysMediaPlayer.getDuration());
				mPlayerClient.updatePlayProgress(iProgress, getDuration());
			}
			updatePlayStatus();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.e(TAG, "onPrepared!!!");
		if (mbInitialize) {
			mbCanSeek = true;
			miPlayState = PlayStatus.DECODED;
			setDuration(mSysMediaPlayer.getDuration());
			updatePlayStatus();
			mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PROGRESS, 1000);
			mHandler.removeMessages(MSG_MEDIA_SEEK);
			mHandler.removeMessages(MSG_MEDIA_PLAY);
			mHandler.removeMessages(MSG_MEDIA_PAUSE);
			if (!mbExternalFlag) {
				if (miProgress > 0) {
					execSeek(miProgress);
					miProgress = 0;
				}
			}
			mHandler.sendEmptyMessageDelayed(MSG_MEDIA_START, DELAY_EXEC_TIME);
		}
	}

	private void execStart() {
		// 请求焦点必须放在外面 不然会造成死锁
		mPlayerClient.requestAudioFocus();
		synchronized (TAG) {
			if (mbInitialize && !MediaScanConstans.isBtPhone()) {
				
				mSysMediaPlayer.start();
				miPlayState = PlayStatus.STARTED;
				if (!mbExternalFlag && !mPlayerClient.isAllowPlay()) {
					pause();
				}
				if (mPlayerClient != null && mPlayerClient.getMediaPlayerType() == MediaType.MEDIA_AUDIO) {
					mHandler.sendEmptyMessage(MSG_MEDIA_PARSE_LRC);
				}
				updatePlayStatus();
			}
		}
	}

	private void execParseLrc() {
		miLrcStatus = mLrcProcess.readLRC(mSourceFile);
		mLrcProcess.getLrcIndexChanged(0, mSysMediaPlayer.getDuration());
		if (miLrcStatus == 1) {
			mHandler.post(mRunnable);
		}
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_MEDIA_UPDATE_LRC_LIST;
		msg.arg1 = miLrcStatus;
		msg.obj = mLrcProcess.getLrcList();
		mHandler.sendMessage(msg);
	}

	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			if (mLrcProcess != null && mLrcProcess.getLrcIndexChanged(mSysMediaPlayer.getCurrentPosition(),
					mSysMediaPlayer.getDuration())) {
				mHandler.removeMessages(MSG_MEDIA_UPDATE_LRC_INDEX);
				Message message = mHandler.obtainMessage();
				message.what = MSG_MEDIA_UPDATE_LRC_INDEX;
				message.arg1 = miLrcStatus;
				message.arg2 = mLrcProcess.getCurIndex();
				mHandler.sendMessage(message);
			}
			mHandler.postDelayed(mRunnable, 200);
		}
	};

	public int getLrcIndex() {
		if (mLrcProcess != null) {
			return mLrcProcess.getCurIndex();
		}
		return 0;
	}

	public ArrayList<LrcContent> getLrcList() {
		if (mLrcProcess != null) {
			return mLrcProcess.getLrcList();
		}
		return null;
	}

	@Override
	public void onSetRateComplete(MediaPlayer arg0) {

	}

	@Override
	public void onTimedText(MediaPlayer mp, TimedText text) {
		Log.e(TAG, "onTimedText");
		updatePlayStatus();
	}

	public int getPlayState() {
//		synchronized (TAG) {
			if (mbInitialize) {
				Log.e(TAG, "state:" + PLAY_STATE[miPlayState]);
			}
			return miPlayState;
//		}
	}

	public int getCurrentRealState() {
		synchronized (TAG) {
			if (mAtcMediaPlayer != null && mbInitialize) {
				int realState = mAtcMediaPlayer.getRealCurrentState();
				getPlayState();
				switch (realState) {
				case AtcMediaPlayer.MEDIA_PLAYER_INITIALIZED:
				case AtcMediaPlayer.MEDIA_PLAYER_PREPARING:
					return PlayStatus.DECODING;
				case AtcMediaPlayer.MEDIA_PLAYER_PREPARED:
					return PlayStatus.DECODED;
				case AtcMediaPlayer.MEDIA_PLAYER_STARTED:
					return PlayStatus.STARTED;
				case AtcMediaPlayer.MEDIA_PLAYER_PAUSED:
					return PlayStatus.PAUSED;
				case AtcMediaPlayer.MEDIA_PLAYER_PLAYBACK_COMPLETE:
					return PlayStatus.FINISH;
				case AtcMediaPlayer.MEDIA_PLAYER_IDLE:
					return PlayStatus.IDLE;
				case AtcMediaPlayer.MEDIA_PLAYER_STATE_ERROR:
					return PlayStatus.ERROR;
				case AtcMediaPlayer.MEDIA_PLAYER_STOPPED:
					return PlayStatus.STOPED;
				default:
					break;
				}
			}
			return miPlayState;
		}
	}

	@SuppressLint("DefaultLocale")
	public int getDuration() {
		return mDuration / 1000;
	}

	public int getProgress() {
		synchronized (TAG) {
			if (mbInitialize) {
				return mSysMediaPlayer.getCurrentPosition() / 1000;
			}
		}
		return 0;
	}

	private void setDuration(int mDuration) {
		this.mDuration = mDuration;
	}

	@Override
	public boolean handleMessage(Message msg) {
		try {
			switch (msg.what) {
			case MSG_MEDIA_DECODE:
				execDecode(msg.getData().getString("file"));
				updatePlayStatus();
				break;
			case MSG_MEDIA_PLAY:
				if (msg.arg1 <= 3 && mPlayerClient.isAllowPlay()) {
					execPlay();
					if (getCurrentRealState() != PlayStatus.STARTED) {
						Log.e(TAG, "exec play again!");
						Message remsg = Message.obtain();
						remsg.what = MSG_MEDIA_PLAY;
						remsg.arg1 = msg.arg1 + 1;
						mHandler.sendMessageDelayed(remsg, DELAY_EXEC_TIME);
					}
				} else {
					Log.e(TAG, "exec play failed!");
				}
				break;

			case MSG_MEDIA_PAUSE:
				if (msg.arg1 <= 3) {
					execPause();
					if (getCurrentRealState() != PlayStatus.PAUSED) {
						Log.e(TAG, "exec pause again!");
						Message remsg = Message.obtain();
						remsg.what = MSG_MEDIA_PAUSE;
						remsg.arg1 = msg.arg1 + 1;
						mHandler.sendMessageDelayed(remsg, DELAY_EXEC_TIME);
					}
				} else {
					Log.e(TAG, "exec pause failed!");
				}
				break;

			case MSG_MEDIA_SEEK:
				execSeek(msg.arg1);
				break;

			case MSG_MEDIA_STOP:
				break;

			case MSG_MEDIA_PROGRESS:
				if (mbInitialize) {
					if (mSysMediaPlayer.isPlaying()) {
						int iProgress = mSysMediaPlayer.getCurrentPosition() / 1000;
						setDuration(mSysMediaPlayer.getDuration());
						mPlayerClient.updatePlayProgress(iProgress, getDuration());
						if (miPlayState != PlayStatus.STARTED) {
							miPlayState = getCurrentRealState();
						} else if ((iProgress == getDuration() && iProgress != 0)) {
							miPlayState = PlayStatus.FINISH;
						}
					}
				}
				if (mHandler != null) {
					mHandler.removeMessages(MSG_MEDIA_PROGRESS);
					mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PROGRESS, 500);
				}
				break;

			case MSG_MEDIA_UPDATE_STATE:
				if (mPlayerClient != null) {
					mPlayerClient.updatePlayState();
				}
				break;

			case MSG_MEDIA_INFO:
				if (mPlayerClient != null) {
					mPlayerClient.updateMediaInfo(msg.arg1, msg.arg2);
				}
				break;
			case MSG_MEDIA_UPDATE_LRC_INDEX:
				if (mPlayerClient != null) {
					mPlayerClient.updateLrcIndex(msg.arg1, msg.arg2);
				}
				break;
			case MSG_MEDIA_UPDATE_LRC_LIST:
				if (mPlayerClient != null) {
					mPlayerClient.updateLrcList((ArrayList<LrcContent>) msg.obj, msg.arg1);
				}
				break;
			case MSG_MEDIA_PARSE_LRC:
				execParseLrc();
				break;
			case MSG_MEDIA_START:
				execStart();
				break;
			case MSG_MEDIA_UPDATE_INFO:
				if (mPlayerClient != null) {
					mPlayerClient.updatePlayInfo(msg.arg1 == 1 ? true : false, (String)msg.obj);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Log.e(TAG, "finalize");
	}

	private void updatePlayStatus() {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(MSG_MEDIA_UPDATE_STATE);
		}
	}
	
	private void updatePlayInfo(int iStatus) {
		if (mHandler != null) {
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_MEDIA_UPDATE_INFO;
			msg.obj = mSourceFile;
			Log.i(TAG, "update mSourceFile:" + mSourceFile);
			msg.arg1 = iStatus;
			mHandler.sendMessage(msg);
		}
	}

	public boolean isPlaying() {
		boolean bPlaying = false;
		synchronized (TAG) {
			if (mbInitialize && mSysMediaPlayer != null) {
				if (miPlayState == PlayStatus.STARTED || miPlayState == PlayStatus.PAUSED) {
					bPlaying = true;
				}
			}
		}
		return bPlaying;
	}

	public boolean isStop() {
		boolean bStop = true;
		synchronized (TAG) {
			if (mbInitialize && mSysMediaPlayer != null) {
				if (miPlayState != PlayStatus.IDLE) {
					bStop = false;
				}
			}
		}
		return bStop;
	}

	private String mCurPath = "";
	private byte[] mApicBuf = null;
//	private Mp3ID3Parser mMp3id3Parser = null;

	// 获取文件的专辑封面信息
	public byte[] parseApic(String path, boolean bPlaying) {
		byte[] apicBuf = null;
		if (mCurPath.equals(path)) {
			return mApicBuf;
		}
		try {
			File f = new File(path);
			ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
			MediaScanner scanner = new MediaScanner(mContext);
			apicBuf = scanner.extractAlbumArt(pfd.getFileDescriptor());
			pfd.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (bPlaying) {
			mCurPath = path;
		}
		return apicBuf;
	}

	public byte[] getApicData() {
		return mApicBuf;
	}

//	public String getId3Title() {
//		return mMp3id3Parser.getTitle();
//	}
//
//	public String getId3Ablum() {
//		return mMp3id3Parser.getAlbum();
//	}
//
//	public String getId3Artist() {
//		return mMp3id3Parser.getArtist();
//	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

	}
}
