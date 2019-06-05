package com.carocean.media.video;

import com.autochips.settings.AtcSettings;
import com.carocean.R;
import com.carocean.media.bt.MySeekBar;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.constants.MediaPlayerContants.ServiceStatus;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaTrackInfo;
import com.carocean.media.service.MultiMediaPlayer;
import com.carocean.utils.MarqueeTextView;
import com.carocean.utils.PopDialog;
import com.carocean.utils.Utils;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;
import com.yecon.settings.YeconSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VideoActivity extends Activity implements OnClickListener, OnSeekBarChangeListener , android.os.Handler.Callback, OnTouchListener{
	private final String TAG = "VideoActivity";
	private final int MSG_DELAY_SEEK = 251;
	private final int MSG_HIDE_BAR = 254;
	private final int MSG_HIDE_BRIGHTNESS = 253;
	private final int TIME_HIDE_BRIGHTNESS = 1000;
	private final int TIME_AUTO_FULLSREEN = 5 * 1000;

	private boolean mShow = true;
	private FrameLayout mTop;
	private LinearLayout mBottom;

	// layout
	private VideoParkWarningView mLayoutParkWarn = null;

	private TextView mTvDuration;
	private TextView mTvProgress;
	private SeekBar mSeekBar;
	private ImageButton mBtnPrev;
	private ImageButton mBtnPlay;
	private ImageButton mBtnPause;
	private ImageButton mBtnNext;
	private ImageButton mBtnList;
	private MarqueeTextView mTvTitle;

	private SurfaceView mSurfaceFront;
	private boolean mbSurfaceFrontCreated = true;
	private boolean mbStopbyBackGround = false;
	private int miSeekState = 0x00;

	private Toast mToast;
	private boolean mbBackGround = false;
	private PopDialog mDialog = null;
	Handler mHandler = new Handler(this);
	private AudioManager mAudioManager; 
	
	private MySeekBar mBrightnessSeekbar = null;
	private FrameLayout mBrightnessLayout = null;
    private GestureDetector mGestureDetector = null;
	private boolean mFirstScroll  = false;
	private int GESTURE_FLAG = 0;
	private final int FF_FR_DISTANCE = 5;
    private boolean mbScreenDown = false;
    private float mstartX;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		setTheme(android.R.style.Theme_Black_NoTitleBar);
		setContentView(R.layout.activity_video);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initUI();
		if (null != mHandler) {
			MediaActivityProxy.getInstance().RegisterHandler(mHandler);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MediaActivityProxy.getService().RegisterSource();
		PageMedia.setView(ViewType.ViewVideo);
		if (mbBackGround) {
			mbBackGround = false;
			MediaActivityProxy.getService().requestVideoPlay();
		}
		Log.i(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		miSeekState = 0x00;
		mbBackGround = true;
		super.onPause();
		if (mDialog.isAdded()) {
			mDialog.dismiss();
		}
		try {
			if (MediaActivityProxy.getService().getMediaType() == MediaType.MEDIA_VIDEO) {
				MediaActivityProxy.getService().requestStop();
			}
		} catch (Exception e) {
			
		}
		Log.i(TAG, "onPause");
	}

	@Override
	protected void onDestroy() {
		miSeekState = 0x00;
		MediaActivityProxy.getInstance().UnRegisterHandler(mHandler);
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToList();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public void backToList() {
		if (PageMedia.getView() == ViewType.ViewVideo) {
			PageMedia.setView(ViewType.ViewVideoFile);
		}
		finish();
	}

	protected void initUI() {
		mGestureDetector = new GestureDetector(this, new MyOnGestureListener());
		mLayoutParkWarn = (VideoParkWarningView) findViewById(R.id.layout_parking);
		findViewById(R.id.vide_park_warn_ok).setOnClickListener(this);
		findViewById(R.id.video_btn_delete).setOnClickListener(this);

		mBtnPrev = (ImageButton) findViewById(R.id.video_btn_pre);
		mBtnPlay = (ImageButton) findViewById(R.id.video_btn_play);
		mBtnPause = (ImageButton) findViewById(R.id.video_btn_pause);
		mBtnNext = (ImageButton) findViewById(R.id.video_btn_next);
		mBtnList = (ImageButton) findViewById(R.id.video_list);
		mTvTitle = (MarqueeTextView) findViewById(R.id.video_title);
		mSeekBar = (SeekBar) findViewById(R.id.video_seekbar);

		mTvProgress = (TextView) findViewById(R.id.video_progress_time);
		mTvDuration = (TextView) findViewById(R.id.video_total_time);

		mTop = (FrameLayout) findViewById(R.id.video_layout_top);
		mBottom = (LinearLayout) findViewById(R.id.video_layout_bottom);

		findViewById(R.id.video_surfaceView).setOnClickListener(this);
		findViewById(R.id.video_surfaceView).setOnTouchListener(this);
		mBtnPrev.setOnClickListener(this);
		mBtnPlay.setOnClickListener(this);
		mBtnPause.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
		mBtnList.setOnClickListener(this);

		mSeekBar.setOnSeekBarChangeListener(this);
		mLayoutParkWarn.setOnClickListener(this);
		mBrightnessLayout = (FrameLayout) findViewById(R.id.video_brightness_layout);
		mBrightnessSeekbar = (MySeekBar) findViewById(R.id.video_brightness_seekbar);
		mBrightnessSeekbar.setMax(255);
		if (mDialog == null) {
			mDialog = new PopDialog(getResources().getString(R.string.media_delete_video_title));
		}
		CreateFrontDisplay();
	}

	public void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
		YeconSettings.initVideoRgb(YeconSettings.RGBTYPE.USB);
		showVideo();
		ForceUpdateTrack();
		if (mBtnPlay.getVisibility() == View.VISIBLE) {
			start();
		}
		showBar(false);
	}

	public void onClick(View v) {
		try {
			Log.i(TAG, " onClick  v.getid:" + v.getId());
			boolean bPreFullHideBar = true;
			switch (v.getId()) {
			case R.id.video_btn_play:
			case R.id.video_btn_pause:
				MediaActivityProxy.getService().requestPause();
				break;
			case R.id.video_btn_pre:
				MediaActivityProxy.getService().requestPrev();
				break;
			case R.id.video_btn_next:
				MediaActivityProxy.getService().requestNext();
				break;
			case R.id.video_list:
				mHandler.removeCallbacksAndMessages(null);
				backToList();
				break;
			case R.id.video_btn_delete:
				mDialog.show(getFragmentManager(), this.getClass().getName());
				mDialog.setListener(this);
				break;
			case R.id.pop_btn_yes:
				MediaActivityProxy.getService().deleteFile(
						MediaActivityProxy.getService().getFilePos(MediaType.MEDIA_VIDEO),
						MediaType.MEDIA_VIDEO);
				if (mDialog != null) {
					mDialog.dismiss();
				}
				if (MediaActivityProxy.getService().getFileList(ListType.ALL_VIDEO_FILE).size() == 0) {
					backToList();
				}
				break;
			case R.id.pop_btn_no:
				if (mDialog != null) {
					mDialog.dismiss();
				}
				break;
			case R.id.vide_park_warn_ok:
				backToList();
				break;
			case R.id.video_surfaceView:
				bPreFullHideBar = false;
				if (mBottom.getVisibility() == View.VISIBLE) {
					showBar(false);
				} else {
					showBar(true);
				}
				break;
			default:
				bPreFullHideBar = false;
				break;
			}
			if (bPreFullHideBar) {
				showBar(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (miSeekState == 0x01) {
			mTvProgress.setText(MediaActivityProxy.formatData(progress, true));
			mSeekBar.setProgress(progress);
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		miSeekState = 0x01;
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		int iProgress = seekBar.getProgress();
		try {
			if (MediaActivityProxy.isBindService()) {
				MediaActivityProxy.getService().requestSeek(iProgress);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		miSeekState = 0x02;
		mHandler.sendEmptyMessageDelayed(MSG_DELAY_SEEK, 1000);
	}

	private int miTotalDuration = 0;

	// 更新曲目信息
	public void UpdateTrackInfo() {
		try {
			if (MediaActivityProxy.isBindService()) {
				MediaTrackInfo cv = MediaActivityProxy.getService().getPlayingFileInfo();
				if (cv != null && cv.getTrackTotal() != 0) {
					mTvTitle.setText(cv.getName());
					miTotalDuration = cv.getDuration();
					mTvDuration.setText(MediaActivityProxy.formatData(miTotalDuration, true));
					mSeekBar.setMax(miTotalDuration);
					mSeekBar.setEnabled(miTotalDuration != 0);
					if (miTotalDuration != 0) {
						mTvProgress.setText(MediaActivityProxy.formatData(cv.getCurTime(), true));
						mSeekBar.setProgress(cv.getCurTime());
					}
				} else {
					mTvProgress.setText(MediaActivityProxy.formatData(0, true));
					mTvDuration.setText(MediaActivityProxy.formatData(0, true));
					mSeekBar.setMax(0);
					mSeekBar.setEnabled(false);
					mTvTitle.setText("");
					mBtnPlay.setVisibility(View.VISIBLE);
					mBtnPause.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 更新播放状态
	private void UpdatePlayPause(int iState) {
		if (iState == PlayStatus.PAUSED || iState == PlayStatus.IDLE) {
			mBtnPlay.setVisibility(View.VISIBLE);
			mBtnPause.setVisibility(View.GONE);
		} else if (iState == PlayStatus.STARTED) {
			mBtnPlay.setVisibility(View.GONE);
			mBtnPause.setVisibility(View.VISIBLE);
		}
	}

	// 强制刷新曲目信息
	private void ForceUpdateTrack() {
		try {
			if (MediaActivityProxy.isBindService()) {
				UpdateTrackInfo();
				UpdatePlayPause(MediaActivityProxy.getService().getPlayStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}

	public void showToast(String tips) {
		if (mToast == null) {
			mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		}
		if (tips != null) {
			mToast.setText(tips);
			mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.show();
		} else {
			mToast.cancel();
		}
	}

	@SuppressLint("NewApi")
	public void CreateFrontDisplay() {
		mSurfaceFront = (SurfaceView) findViewById(R.id.video_surfaceView);
		mSurfaceFront.setBackgroundResource(android.R.color.transparent);
		mSurfaceFront.getHolder().addCallback(new SurfaceViewCallback());
	}

	public void showVideo() {
		try {
			synchronized (TAG) {
				if (mbSurfaceFrontCreated) {
					MediaActivityProxy.getService().requestSetFrontDisplay(mSurfaceFront.getHolder());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		synchronized (TAG) {
			if (MediaActivityProxy.isBindService() && mbStopbyBackGround) {
				mbStopbyBackGround = false;
				try {
					MediaActivityProxy.getService().requestPause();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void pause() {
		synchronized (TAG) {
			if (MediaActivityProxy.isBindService()) {
				mbStopbyBackGround = true;
				try {
					MediaActivityProxy.getService().requestPause();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class SurfaceViewCallback implements Callback {

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "surfaceCreated");
			if (MediaActivityProxy.isBindService()) {
				synchronized (TAG) {
					try {
						if (holder.equals(mSurfaceFront.getHolder())) {
							mbSurfaceFrontCreated = true;
							MediaActivityProxy.getService().requestSetFrontDisplay(holder);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.i(TAG, "surfaceChanged:format=" + format + ",width=" + width + ",height=" + height);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "surfaceDestroyed");
			if (MediaActivityProxy.isBindService()) {
				synchronized (TAG) {
					try {
						if (holder.equals(mSurfaceFront.getHolder())) {
							mbSurfaceFrontCreated = false;
							MediaActivityProxy.getService().requestSetFrontDisplay(null);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void showBar(boolean bShow) {
		if (null != mTop && null != mBottom) {
			Window window = getWindow();
	        WindowManager.LayoutParams params = window.getAttributes();
			if (bShow) {
				params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
				mHandler.removeMessages(MSG_HIDE_BAR);
				mHandler.sendEmptyMessageDelayed(MSG_HIDE_BAR, TIME_AUTO_FULLSREEN);
				mTop.setVisibility(View.VISIBLE);
				mBottom.setVisibility(View.VISIBLE);
			} else {
				params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				mTop.setVisibility(View.INVISIBLE);
				mBottom.setVisibility(View.INVISIBLE);
			}
			window.setAttributes(params);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MediaPlayerMessage.UPDATE_SERVICE_STATE:
			if (msg.arg1 == ServiceStatus.SCANED) {
				ForceUpdateTrack();
			} else if (msg.arg1 == ServiceStatus.SCANING || msg.arg1 == ServiceStatus.LOST_CUR_STORAGE
					|| msg.arg1 == ServiceStatus.LOST_AUDIO_FOCUS || msg.arg1 == ServiceStatus.SWITCH_STORAGE
					|| msg.arg1 == ServiceStatus.SWITCH_MEDIA_LIST || msg.arg1 == ServiceStatus.RELEASE_ACTIVITY
					|| msg.arg1 == ServiceStatus.PLAYED) {
				if (!isFinishing()) {
					backToList();
				}
			} else if (msg.arg1 == ServiceStatus.PLAYED) {
				showToast(getString(R.string.media_finish_play_list));
				backToList();
			} else if (msg.arg1 == ServiceStatus.SCAN_TIMEOUT) {
				showToast(getString(R.string.media_scan_timeout));
			}
			Log.e(TAG, "UPDATE_SERVICE_STATE:" + msg.arg1);
			break;
		case MediaPlayerMessage.UPDATE_LIST_DATA:
			ForceUpdateTrack();
			break;
		case MediaPlayerMessage.UPDATE_PLAY_STATE:
			// update track info
			if (msg.arg1 == PlayStatus.DECODED || msg.arg1 == PlayStatus.STARTED) {
				if (msg.arg1 == PlayStatus.DECODED) {
					mSeekBar.setProgress(0);
					mTvProgress.setText(MediaActivityProxy.formatData(0, true));
					showVideo();
				}
				ForceUpdateTrack();
			} else if (msg.arg1 == PlayStatus.ERROR) {
				// play error
				ForceUpdateTrack();
				showToast(getString(R.string.media_play_failed));
			}
			mHandler.removeMessages(MSG_HIDE_BAR);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BAR, TIME_AUTO_FULLSREEN);
			UpdatePlayPause(msg.arg1);
			break;
		case MediaPlayerMessage.UPDATE_PLAY_PROGRESS:
			if (miSeekState == 0x00) {
				mTvProgress.setText(MediaActivityProxy.formatData(msg.arg1, true));
				mTvDuration.setText(MediaActivityProxy.formatData(msg.arg2, true));
				mSeekBar.setProgress(msg.arg1);
				mSeekBar.setMax(msg.arg2);
			}
			break;

		case MediaPlayerMessage.UPDATE_MEDIA_INTO:
			@SuppressWarnings("unused")
			String strMediaInfo = null;
			if (msg.arg1 == MultiMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
				showVideo();
			} else if (msg.arg1 == MultiMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
				strMediaInfo = getString(R.string.media_not_support_seek);
			} else if (msg.arg1 == MultiMediaPlayer.MEDIA_INFO_UNSUPPORTED_AUDIO) {
				strMediaInfo = getString(R.string.media_not_support_audio);
				if (msg.arg2 == MultiMediaPlayer.CAP_AUDIO_BITRATE_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_audio_bitrate);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_AUDIO_CODEC_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_audio_codec);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_AUDIO_PROFILE_LEVEL_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_audio_profilelevel);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_AUDIO_SAMPLERATE_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_audio_samplingrate);
				}
			} else if (msg.arg1 == MultiMediaPlayer.MEDIA_INFO_UNSUPPORTED_VIDEO) {
				strMediaInfo = getString(R.string.media_not_support_video);
				if (msg.arg2 == MultiMediaPlayer.CAP_VIDEO_BITRATE_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_video_bitrate);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_VIDEO_CODEC_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_video_codec);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_VIDEO_FRAMERATE_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_video_bitrate);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_VIDEO_PROFILE_LEVEL_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_video_profilelevel);
				} else if (msg.arg2 == MultiMediaPlayer.CAP_VIDEO_RESOLUTION_UNSUPPORT) {
					strMediaInfo = getString(R.string.media_not_support_video_resolution);
				}
			} else if (msg.arg1 == MultiMediaPlayer.MEDIA_INFO_DIVXDRM_ERROR) {
				strMediaInfo = getString(R.string.media_play_failed);
			}
			break;
		case MSG_DELAY_SEEK:
			if (miSeekState == 0x02) {
				miSeekState = 0x00;
			}
			break;
		case MSG_HIDE_BAR:
			if (!mbScreenDown) {
				showBar(false);
			}
			break;
		case MSG_HIDE_BRIGHTNESS:
			if (mBrightnessLayout != null) {
				mBrightnessLayout.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mstartX = event.getX();
			mbScreenDown = true;
			mHandler.removeMessages(MSG_HIDE_BAR);
		} else if (event.getAction() == MotionEvent.ACTION_UP ||
				event.getAction() == MotionEvent.ACTION_CANCEL) {
			mbScreenDown = false;
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BAR, TIME_AUTO_FULLSREEN);
		}
	    if (event.getAction() == MotionEvent.ACTION_UP) {
	        GESTURE_FLAG = 0;
	    }
	    return mGestureDetector.onTouchEvent(event);
	}
	
	class MyOnGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	    if (mFirstScroll) {
    	        if (Math.abs(distanceY) >= Math.abs(distanceX)) {
    	            GESTURE_FLAG = 1;
    	        } 
    	    }
    	    if (GESTURE_FLAG == 1) {
    	        if (Math.abs(distanceY) > Math.abs(distanceX)) {
    	            if (distanceY >= FF_FR_DISTANCE) {
    	    	    	if (mstartX > 640) {
    	    	    		if (mAudioManager != null) {
    	    	    			Utils.TransKey(KeyEvent.KEYCODE_VOLUME_UP);
//								int iVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//								if (iVolume < 31) {
//									mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ++iVolume, AudioManager.FLAG_SHOW_UI);
//								}
							}
    					} else {
    						showBrightness(Utils.getBrightness() + 5);
    					}
    	            	
    	            } else if (distanceY <= -FF_FR_DISTANCE) {
    	    	    	if (mstartX > 640) {
    	    	    		if (mAudioManager != null) {
    	    	    			Utils.TransKey(KeyEvent.KEYCODE_VOLUME_DOWN);
//								int iVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//								if (iVolume > 0) {
//									mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, --iVolume, AudioManager.FLAG_SHOW_UI);
//								}
							}
    					} else {
    						showBrightness(Utils.getBrightness() - 5);
    					}
    	            }
    	        }
    	    }
    	    mFirstScroll = false;
    	    return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mFirstScroll = true;
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mBottom.getVisibility() == View.VISIBLE) {
				showBar(false);
			} else {
				showBar(true);
			}
            return false;
        }
    }
	
	private void showBrightness(int iProgress) {
		if (iProgress > 0 && iProgress <= 255) {
			if (mBrightnessSeekbar != null) {
				mBrightnessSeekbar.setProgress(iProgress);
			}
			mBrightnessLayout.setVisibility(View.VISIBLE);
			mHandler.removeMessages(MSG_HIDE_BRIGHTNESS);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BRIGHTNESS, TIME_HIDE_BRIGHTNESS);
			Utils.setBrightness(this, iProgress);
		}
	}
}
