package com.carocean.floatwindow;

import com.carocean.R;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.service.IMediaPlayerService;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.vmedia.media.PageMedia;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaView implements OnClickListener {

	private TextView mProgressTime, mTotalTime, mTitle;
	private SeekBar mSeekBar;
	private ImageButton mPlay, mPause;
	private View mView;

	public MediaView(Context context, View view) {
		mView = view;
		init();
	}

	private void init() {
		if (null != mView) {

			mTitle = (TextView) mView.findViewById(R.id.float_media_title);
			mProgressTime = (TextView) mView.findViewById(R.id.float_music_progress_time);
			mTotalTime = (TextView) mView.findViewById(R.id.float_music_total_time);
			mSeekBar = (SeekBar) mView.findViewById(R.id.float_music_seekbar);
			mSeekBar.setEnabled(false);
			mSeekBar.setProgress(0);

			mPlay = (ImageButton) mView.findViewById(R.id.float_music_btn_play);
			mPause = (ImageButton) mView.findViewById(R.id.float_music_btn_pause);

			mPlay.setOnClickListener(this);
			mPause.setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.float_music_btn_pre)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.float_music_btn_next)).setOnClickListener(this);
		}
	}

	private void initHandler() {
		FloatWindowMessage msg = FloatWindowMessage.getInstance();
		if (null != msg) {
			msg.registerMsgHandler(mMediaHandler);
		}
	}

	private void uintHandler() {
		FloatWindowMessage msg = FloatWindowMessage.getInstance();
		if (null != msg) {
			msg.unregisterMsgHandler(mMediaHandler);
		}
	}

	public void isShow(final int type) {

		if (null != mView) {
			mView.setVisibility(type);

			if (mView.isShown()) {
				initHandler();
				showPlayStatus();
			} else {
				uintHandler();
			}
		}
	}

	public void onClick(View view) {
		IMediaPlayerService service = MediaActivityProxy.getService();
		if (null != service) {
			try {
				switch (view.getId()) {
				case R.id.float_music_btn_pre:
					service.requestPrev();
					break;
				case R.id.float_music_btn_pause:
				case R.id.float_music_btn_play:
					service.requestPause();
					break;
				case R.id.float_music_btn_next:
					service.requestNext();
					break;
				default:
					break;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private Handler mMediaHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == FloatWindowMessage.FLOAT_MEDIA_MSG_PLAY_PROGRESS) {
				palyProgress((Bundle) msg.obj);
			} else if (msg.what == FloatWindowMessage.FLOAT_MEDIA_MSG_PLAY_STATUS) {
				showPlayStatus();
			}
			super.handleMessage(msg);
		}
	};

	private void palyProgress(Bundle bundle) {

		if (null != bundle) {

			final int seek_max = bundle.getInt("seekbar_max", 0);
			final int seek_prs = bundle.getInt("seekbar_progress", 0);
			final String total = bundle.getString("total", "--:--");
			final String progress = bundle.getString("progress", "--:--");
			final String title = bundle.getString("title", mView.getResources().getString(R.string.media_music_usb));

			if (null != mProgressTime) {
				mProgressTime.setText(progress);
			}
			if (null != mTotalTime) {
				mTotalTime.setText(total);
			}
			if (null != mSeekBar) {
				mSeekBar.setProgress(seek_prs);
				mSeekBar.setMax(seek_max);
			}
			if (null != mTitle) {
				mTitle.setText(title);
			}

			showPlayStatus();
		}
	}

	private void showPlayStatus() {
		IMediaPlayerService service = MediaActivityProxy.getService();
		if (null != service) {
			try {
				int iState = service.getPlayStatus();
				boolean playStatus = false;
				if (iState == PlayStatus.PAUSED || iState == PlayStatus.IDLE) {
					playStatus = false;
				} else if (iState == PlayStatus.STARTED) {
					playStatus = true;
				}
				
				if (null != mPlay && null != mPause) {
					mPlay.setVisibility(playStatus ? View.GONE : View.VISIBLE);
					mPause.setVisibility(playStatus ? View.VISIBLE : View.GONE);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
