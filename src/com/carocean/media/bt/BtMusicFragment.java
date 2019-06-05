package com.carocean.media.bt;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.media.MediaFragment;
import com.carocean.utils.MarqueeTextView;
import com.carocean.utils.SourceManager;

import android.app.Activity;
import android.constant.YeconConstants;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class BtMusicFragment extends MediaFragment implements OnClickListener {
	private static final String TAG = "BtMusicFragment";

	private MarqueeTextView mTitle;
	private TextView mTvProgress;
	private TextView mTvDuration;
	private SeekBar mSeekBar;
	private ImageButton mPrev, mPlay, mPause, mNext;
	private View mView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.activity_music_bt, container, false);
		if (null != view) {
			mView = view;
			mTitle = (MarqueeTextView) view.findViewById(R.id.bt_music_title);
			mTvProgress = (TextView) view.findViewById(R.id.bt_music_progress_time);
			mTvDuration = (TextView) view.findViewById(R.id.bt_music_total_time);

			mSeekBar = (SeekBar) view.findViewById(R.id.bt_music_seekbar);
			mSeekBar.setEnabled(false);

			mPrev = (ImageButton) view.findViewById(R.id.bt_music_btn_pre);
			mPlay = (ImageButton) view.findViewById(R.id.bt_music_btn_play);
			mPause = (ImageButton) view.findViewById(R.id.bt_music_btn_pause);
			mNext = (ImageButton) view.findViewById(R.id.bt_music_btn_next);

			mPrev.setOnClickListener(this);
			mPlay.setOnClickListener(this);
			mPause.setOnClickListener(this);
			mNext.setOnClickListener(this);

			BTService.registerNotifyHandler(uiHandler);
		}

		return view;
	}
	
	private void flushui(){
		if (BTUtils.mBluetooth.isA2DPconnected()) {
			mTitle.setText(BTUtils.mBluetooth.title);
			updateProgress(BTUtils.mBluetooth.music_time_max, BTUtils.mBluetooth.music_time_cur);
			updateTimeText(BTUtils.mBluetooth.music_time_max, BTUtils.mBluetooth.music_time_cur);
			playStatus(BTUtils.mBluetooth.isA2DPPlaying());
			
		}else{
			mTitle.setText("");
			updateProgress(0, 0);
			updateTimeText(0, 0);
			playStatus(false);
			BTService.notifyUiBtStatus(new Intent(BTService.ACTION_GOTORADIO));
		}
	}

	public void onResume() {
		Log.e(TAG, "onResume");
		SourceManager.acquireSource(getActivity(), YeconConstants.SRC_VOLUME_BT_MUSIC);
		BTUtils.mBluetooth.requesta2dpfocus();
		flushui();
		super.onResume();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_music_btn_pause:
		case R.id.bt_music_btn_play:
			if (BTUtils.mBluetooth.isA2DPPlaying()) {
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PAUSE);
			} else {
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PLAY);
			}
			break;
		case R.id.bt_music_btn_pre:
			BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PREV);
			break;
		case R.id.bt_music_btn_next:
			BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_NEXT);
			break;
		default:
			break;
		}
	}

	public void onPause() {
		super.onPause();
	}

	public void onDestroy() {
		BTService.unregisterNotifyHandler(uiHandler);
		super.onDestroy();
		Log.e(TAG, "onDestroy");
	}

	private Handler uiHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(BTService.ACTION_MUSIC_PLAYING) || action.equals(BTService.ACTION_MUSIC_ID3) || action.equals(BTService.ACTION_MUSIC_PLAY_POS)
						|| action.equals(BTService.ACTION_CONNECTED) || action.equals(BTService.ACTION_DISCONNECTED)) {
					flushui();
				}
			}
		}
	};

	private void updateTimeText(int song_length, int song_position) {
		String length = "00:00:00";
		String pos = "00:00:00";

		if (song_length != 0xFFFFFFFF) {
			length = Bluetooth.millSeconds2readableTime(song_length);
		}
		if (song_position != 0xFFFFFFFF) {
			pos = Bluetooth.millSeconds2readableTime(song_position);
		}

		mTvDuration.setText(length);
		mTvProgress.setText(pos);
	}

	private void updateProgress(int total_long, int playing_time) {
		mSeekBar.setMax(total_long == 0xFFFFFFFF ? 0 : total_long);
		mSeekBar.setProgress(playing_time == 0xFFFFFFFF ? 0 : playing_time);
	}

	private void playStatus(boolean bplay) {
		if (null != mPause && null != mPlay) {
			mPause.setVisibility(bplay ? View.VISIBLE : View.GONE);
			mPlay.setVisibility(bplay ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void addNotify() {
		Log.e(TAG, "addNotify");
	}

}
