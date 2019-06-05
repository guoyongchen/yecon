package com.carocean.media.music;

import java.io.File;

import com.carocean.R;
import com.carocean.media.CircleImageView;
import com.carocean.media.MediaFragment;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.constants.MediaPlayerContants.RepeatMode;
import com.carocean.media.constants.MediaPlayerContants.ServiceStatus;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaTrackInfo;
import com.carocean.utils.Constants;
import com.carocean.utils.MarqueeTextView;
import com.carocean.utils.PopDialog;
import com.carocean.utils.PopDialog.PopType;
import com.carocean.utils.SourceManager;
import com.carocean.utils.Utils;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vmedia.media.PageMedia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlayerFragment extends MediaFragment implements OnClickListener, OnSeekBarChangeListener {
	private static final String TAG = MusicPlayerFragment.class.getSimpleName();
	private View mView = null;

	private final int MSG_DELAY_SEEK = 254;
	private final int UPDATA_MUSIC_INFO = 253;

	private MarqueeTextView mTvTitle, mTvArtist, mTvAlbum;
	private TextView mTvDuration, mTvProgress;
	private SeekBar mSeekBar;

	private ImageButton mlists, mPrev, mPlay, mPause, mNext;
	// 顺序/循环/单曲/随机
	private ImageButton mOrder, mCycle, mSingle, mRandom;
	private CircleImageView mIvApic;
	private ImageView mIvApicDef, mIvApicFg;
	private ImageView mIvDownload;
	
	private Animation mAnimDisc;
	private View mLayoutDisc;
	
	int miSeekState = 0x00;
	private PopDialog mDialog = null;

	public static int mPlayStatus = Constants.PLAY_STATUS_STOP;

	private MediaActivity mActivity = null;

	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = (MediaActivity) activity;
		
		if (null != mHandler) {
			mHandler.sendEmptyMessage(UPDATA_MUSIC_INFO);
		}
	}

	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mView == null) {
			mView = inflater.inflate(R.layout.activity_music_usb, null);
			initUI();
		}
		return mView;
	}

	private void initUI() {
		mAnimDisc = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mAnimDisc.setInterpolator(new LinearInterpolator()); 
		mAnimDisc.setDuration(5000);
		mAnimDisc.setRepeatCount(-1);
		mAnimDisc.setFillAfter(true);	
		mLayoutDisc = mView.findViewById(R.id.music_layout_disc);
		mlists = (ImageButton) mView.findViewById(R.id.usb_music_lists_btn);

		mTvTitle = (MarqueeTextView) mView.findViewById(R.id.music_tv_title);
		mTvAlbum = (MarqueeTextView) mView.findViewById(R.id.music_tv_ablum);
		mTvArtist = (MarqueeTextView) mView.findViewById(R.id.music_tv_artist);
		mSeekBar = (SeekBar) mView.findViewById(R.id.music_sb_process);
		mTvDuration = (TextView) mView.findViewById(R.id.music_tv_total_time);
		mTvProgress = (TextView) mView.findViewById(R.id.music_tv_progress_time);

		mPrev = (ImageButton) mView.findViewById(R.id.music_btn_pre);
		mPlay = (ImageButton) mView.findViewById(R.id.music_btn_play);
		mPause = (ImageButton) mView.findViewById(R.id.music_btn_pause);
		mNext = (ImageButton) mView.findViewById(R.id.music_btn_next);

		mOrder = (ImageButton) mView.findViewById(R.id.music_btn_order);
		mCycle = (ImageButton) mView.findViewById(R.id.music_btn_cycle);
		mSingle = (ImageButton) mView.findViewById(R.id.music_btn_single);
		mRandom = (ImageButton) mView.findViewById(R.id.music_btn_random);
		mIvApic = (CircleImageView) mView.findViewById(R.id.music_iv_apic);
		mIvApicDef = (ImageView) mView.findViewById(R.id.music_iv_apic_def);
		mIvApicFg = (ImageView) mView.findViewById(R.id.music_iv_apic_fg);
		mView.findViewById(R.id.music_btn_delete).setOnClickListener(this);
		mIvDownload = (ImageView) mView.findViewById(R.id.music_btn_download);
		mIvDownload.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(this);
		mlists.setOnClickListener(this);
		mPrev.setOnClickListener(this);
		mPlay.setOnClickListener(this);
		mPause.setOnClickListener(this);
		mNext.setOnClickListener(this);
		mOrder.setOnClickListener(this);
		mCycle.setOnClickListener(this);
		mSingle.setOnClickListener(this);
		mRandom.setOnClickListener(this);
		if (mDialog == null) {
			mDialog = new PopDialog(getString(R.string.media_delete_music_title));
			mDialog.setListener(this);
		}
		updateDownLoadStatus();
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (mActivity.isMediaPage()) {
			MediaActivityProxy.getService().RegisterSource();
			initMusic();
			updateDownLoadStatus();
			boolean bScaning = MediaActivityProxy.getInstance().isScaningAttachedDevice();
			if (!bScaning) {
				ForceUpdateTrack();
			} else {
				Log.d(TAG, "onResume:scaning -> loading");
			}
		}
	}

	public void onPause() {
		super.onPause();
		if (mDialog.isAdded()) {
			mDialog.dismiss();
		}
		miSeekState = 0x00;
		Log.d(TAG, "onPause");
	}

	public void onDestroy() {
		miSeekState = 0x00;
		mPlayStatus = Constants.PLAY_STATUS_STOP;
		if (null != mHandler) {
			mHandler.removeCallbacksAndMessages(null);
			MediaActivityProxy.getInstance().UnRegisterHandler(mHandler);
		}
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.music_btn_order:
				MediaActivityProxy.getService().requestRepeat(RepeatMode.ALL);
				break;
			case R.id.music_btn_cycle:
				MediaActivityProxy.getService().requestRepeat(RepeatMode.SINGLE);
				break;
			case R.id.music_btn_single:
				MediaActivityProxy.getService().requestRepeat(RepeatMode.RANDOM);
				break;
			case R.id.music_btn_random:
				MediaActivityProxy.getService().requestRepeat(RepeatMode.ALL);
				break;
			case R.id.music_btn_next:
				MediaActivityProxy.getService().requestNext();
				break;
			case R.id.music_btn_play:
			case R.id.music_btn_pause:
				MediaActivityProxy.getService().requestPause();
				break;
			case R.id.music_btn_pre:
				MediaActivityProxy.getService().requestPrev();
				break;
			case R.id.usb_music_lists_btn:
				PageMedia.getInstance().showView(PageMedia.ViewType.ViewUsbMusicFile);
				break;
			case R.id.music_btn_download:
				long time = SystemClock.uptimeMillis();
				long iCurSize = MediaScanConstans.getFileSizes(new File(MediaScanConstans.KWMUSIC_PATH));
				long iSize = iCurSize + MediaScanConstans.getFileSize(new File(MediaActivityProxy.getService().getFileName(MediaType.MEDIA_AUDIO)));
				if (iSize <= MediaScanConstans.LOCAL_FILE_MAX_SIZE) {
					mDialog.setPopType(PopType.DOWNLOAD, getString(R.string.media_download_music_title));
					mDialog.show(getFragmentManager(), this.getClass().getName());
				} else {
					Utils.showToast(getString(R.string.media_disk_drive_full));
				}
				break;
			case R.id.music_btn_delete:
				mDialog.setPopType(PopType.DELETE, getString(R.string.media_delete_music_title));
				mDialog.show(getFragmentManager(), this.getClass().getName());
				break;
			case R.id.pop_btn_yes:
				if (mDialog != null) {
					if (mDialog.getPopType() == PopType.DELETE) {
						MediaActivityProxy.getService().deleteFile(MediaActivityProxy.getService().getFilePos(MediaType.MEDIA_AUDIO), MediaType.MEDIA_AUDIO);
						if (mDialog != null) {
							mDialog.dismiss();
						}
					} else {
						MediaTrackInfo cv = MediaActivityProxy.getService().getPlayingFileInfo(); 
						if (cv != null && cv.getPath() != null) {
							MediaActivityProxy.getService().downloadFile(cv.getPath());
						}
						if (mDialog != null) {
							mDialog.dismiss();
						}
					}
				}
				break;
			case R.id.pop_btn_no:
				if (mDialog != null) {
					mDialog.dismiss();
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mView == null)
				return;
			try {
				switch (msg.what) {
				case MediaPlayerMessage.UPDATE_SERVICE_STATE:
					Log.i(TAG, "UPDATE_SERVICE_STATE msg.arg1:" + msg.arg1);
					if (msg.arg1 == ServiceStatus.SCANED || msg.arg1 == ServiceStatus.EMPTY_STORAGE) {
						ForceUpdateTrack();
					} else if (msg.arg1 == ServiceStatus.LOST_CUR_STORAGE
							|| msg.arg1 == ServiceStatus.LOST_AUDIO_FOCUS || msg.arg1 == ServiceStatus.SWITCH_STORAGE
							|| msg.arg1 == ServiceStatus.SWITCH_MEDIA_LIST || msg.arg1 == ServiceStatus.PLAYED
							|| msg.arg1 == ServiceStatus.RELEASE_ACTIVITY) {
						if (SourceManager.isMediaSource() && mActivity.isMediaPage() && isResumed()) {
							PageMedia.getInstance().showView(PageMedia.ViewType.ViewUsbMusicFile);
						} else {
							PageMedia.setView(PageMedia.ViewType.ViewUsbMusicFile);
						}
					}
					updateDownLoadStatus();
					break;
				case MediaPlayerMessage.UPDATE_PLAY_STATE:
					if (msg.arg1 == PlayStatus.DECODED || msg.arg1 == PlayStatus.STARTED) {
						if (msg.arg1 == PlayStatus.DECODED) {
							mSeekBar.setProgress(0);
							mTvProgress.setText(MediaActivityProxy.formatData(0, false));
						}
						ForceUpdateTrack();
					} else if (msg.arg1 == PlayStatus.ERROR) {
						ForceUpdateTrack();
						int iRepeat = RepeatMode.ALL;
						if (MediaActivityProxy.isBindService()) {
							iRepeat = MediaActivityProxy.getService().getRepeatStatus();
						}
						if (iRepeat == RepeatMode.SINGLE) {
							Utils.showToast(getString(R.string.media_play_repeat_one_failed));
						} else {
							Utils.showToast(getString(R.string.media_play_failed));
						}
					}
					UpdatePlayPause(msg.arg1);
					break;
				case MediaPlayerMessage.UPDATE_PLAY_PROGRESS:
					if (miSeekState == 0x00 && null != MediaActivityProxy.getInstance()) {
						String progress = MediaActivityProxy.formatData(msg.arg1, false);
						String total = MediaActivityProxy.formatData(msg.arg2, false);
						mTvProgress.setText(progress);
						mTvDuration.setText(total);
						mSeekBar.setProgress(msg.arg1);
						mSeekBar.setMax(msg.arg2);
					}
					break;
				case MediaPlayerMessage.UPDATE_RANDOM_STATE:
					UpdateRepeat(MediaActivityProxy.getService().getRepeatStatus());
					break;
				case MediaPlayerMessage.UPDATE_REPEAT_STATE:
					UpdateRepeat(msg.arg1);
					break;
				case MediaPlayerMessage.UPDATE_DWONLOAD_STATE:
					Utils.showToast(msg.arg1 == 1 ?  getString(R.string.media_download_success) : getString(R.string.media_download_failed));
					break;
				case MSG_DELAY_SEEK:
					if (miSeekState == 0x02) {
						miSeekState = 0x00;
					}
					break;
				case UPDATA_MUSIC_INFO: {
					if (null != mHandler) {
						initMusic();
					}
				}
				default:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (miSeekState == 0x01) {
			mTvProgress.setText(MediaActivityProxy.formatData(progress, false));
			mSeekBar.setProgress(progress);
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d(TAG, "[onStartTrackingTouch] start get seek to progress");
		miSeekState = 0x01;
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		int iProgress = seekBar.getProgress();
		Log.d(TAG, "[onStopTrackingTouch] progress:" + iProgress);
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

	private void UpdateTrackInfo() {
		try {
			if (MediaActivityProxy.isBindService()) {
				MediaTrackInfo cv = MediaActivityProxy.getService().getPlayingFileInfo();
				if (cv != null && cv.getTrackTotal() != 0) {
					mTvTitle.setText(cv.getTitle());
					mTvAlbum.setText(cv.getAlbum());
					mTvArtist.setText(cv.getArtist());
					int iDuration = cv.getDuration();
					mTvDuration.setText(MediaActivityProxy.formatData(iDuration, false));
					mSeekBar.setMax(iDuration);
					mSeekBar.setEnabled(iDuration != 0);
					if (iDuration != 0) {
						mTvProgress.setText(MediaActivityProxy.formatData(cv.getCurTime(), false));
						mSeekBar.setProgress(cv.getCurTime());
					}
					Bitmap bmpApic = cv.getApicBmp();
					if (bmpApic != null && !bmpApic.isRecycled()) {
						mIvApic.setImageBitmap(bmpApic);
						mIvApicDef.setVisibility(View.INVISIBLE);
						mIvApic.setVisibility(View.VISIBLE);
						mIvApicFg.setVisibility(View.VISIBLE);
					} else {
						mIvApicDef.setVisibility(View.VISIBLE);
						mIvApic.setVisibility(View.INVISIBLE);
						mIvApicFg.setVisibility(View.INVISIBLE);
					}
				} else {
					mTvProgress.setText(MediaActivityProxy.formatData(0, false));
					mTvDuration.setText(MediaActivityProxy.formatData(0, false));
					mSeekBar.setMax(0);
					mSeekBar.setEnabled(false);
					mIvApicDef.setVisibility(View.VISIBLE);
					mIvApic.setVisibility(View.INVISIBLE);
					mIvApicFg.setVisibility(View.INVISIBLE);
					showPlayStatus(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void UpdateRepeat(int iReapeat) {

		mOrder.setVisibility(View.GONE);
		mSingle.setVisibility(View.GONE);
		mCycle.setVisibility(View.GONE);
		mRandom.setVisibility(View.GONE);

		switch (iReapeat) {
		case RepeatMode.OFF:
			mOrder.setVisibility(View.VISIBLE);
			break;
		case RepeatMode.SINGLE:
			mSingle.setVisibility(View.VISIBLE);
			break;
		case RepeatMode.ALL:
			mCycle.setVisibility(View.VISIBLE);
			break;
		case RepeatMode.RANDOM:
			mRandom.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void UpdatePlayPause(int iState) {
		if (iState == PlayStatus.PAUSED || iState == PlayStatus.IDLE) {
			mPlayStatus = Constants.PLAY_STATUS_PAUSE;
		} else if (iState == PlayStatus.STARTED) {
			mPlayStatus = Constants.PLAY_STATUS_PLAYING;
		}
		showPlayStatus(mPlayStatus == Constants.PLAY_STATUS_PLAYING ? true : false);
	}

	private void ForceUpdateTrack() {
		try {
			if (MediaActivityProxy.isBindService()) {
				UpdateTrackInfo();
				UpdateRepeat(MediaActivityProxy.getService().getRepeatStatus());
				UpdatePlayPause(MediaActivityProxy.getService().getPlayStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void showToast(String tips) {
//		if (mToast == null) {
//			mToast = Toast.makeText(mActivity.getApplicationContext(), "", Toast.LENGTH_SHORT);
//		}
//		if (tips != null) {
//			mToast.setText(tips);
//			mToast.setDuration(Toast.LENGTH_SHORT);
//			mToast.show();
//		} else {
//			mToast.cancel();
//		}
//	}

	private void showPlayStatus(boolean isShow) {
		if (null != mPlay && null != mPause) {
			mPlay.setVisibility(isShow ? View.GONE : View.VISIBLE);
			mPause.setVisibility(isShow ? View.VISIBLE : View.GONE);
		}
		if (isShow) {
			if(mLayoutDisc.getAnimation()==null){
				mLayoutDisc.startAnimation(mAnimDisc);
			}
		} else {
			mLayoutDisc.clearAnimation();
		}
	}

	private void initMusic() {
		if (null != mHandler && null != mActivity) {
			MediaActivityProxy.getInstance().RegisterHandler(mHandler);
		}
	}

	@Override
	public void addNotify() {
		Log.i(TAG, "addNotify");
		MediaActivityProxy.getService().requestRecover(MediaType.MEDIA_AUDIO);
	}
	
	public void updateDownLoadStatus() {
		try {
			boolean isUsbDisk = false;
			if (MediaActivityProxy.getService().getPlayingStorage() != null) {
				String path = MediaActivityProxy.getService().getPlayingStorage().getPath();
				if (path != null) {
					if (path.equals(MediaScanConstans.UDISK1_PATH) || path.equals(MediaScanConstans.UDISK2_PATH)) {
						isUsbDisk = true;
						mIvDownload.setVisibility(View.VISIBLE);
					}
				}
			}
			if (!isUsbDisk) {
				mIvDownload.setVisibility(View.GONE);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
