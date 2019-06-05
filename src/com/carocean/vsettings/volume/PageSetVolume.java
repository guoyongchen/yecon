package com.carocean.vsettings.volume;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.DataShared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

public class PageSetVolume implements IPage, OnClickListener, OnProgressChangedListener {
	private Context mContext;
	private ViewGroup mContentView;
	private CustomSeekbar media_volume_sb, bt_volume_sb, navi_volume_sb, voice_volume_sb;
	private TextView media_volume_value, bt_volume_value, navi_volume_value, voice_volume_value, speed_level_tv;
	private AudioManager mAudioManager;
	private ImageView set_volume_prev_iv, set_volume_next_iv;
	private boolean flag = false;// 音量进度条滑动时，不需要广播再设置标志

	void init(Context context) {
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	void initView(ViewGroup rootView) {

		media_volume_sb = (CustomSeekbar) rootView.findViewById(R.id.media_volume_sb);
		bt_volume_sb = (CustomSeekbar) rootView.findViewById(R.id.bt_volume_sb);
		navi_volume_sb = (CustomSeekbar) rootView.findViewById(R.id.navi_volume_sb);
		voice_volume_sb = (CustomSeekbar) rootView.findViewById(R.id.voice_volume_sb);

		media_volume_value = (TextView) rootView.findViewById(R.id.media_volume_value);
		bt_volume_value = (TextView) rootView.findViewById(R.id.bt_volume_value);
		navi_volume_value = (TextView) rootView.findViewById(R.id.navi_volume_value);
		voice_volume_value = (TextView) rootView.findViewById(R.id.voice_volume_value);
		speed_level_tv = (TextView) rootView.findViewById(R.id.speed_level_tv);

		set_volume_prev_iv = (ImageView) rootView.findViewById(R.id.set_volume_prev_iv);
		set_volume_next_iv = (ImageView) rootView.findViewById(R.id.set_volume_next_iv);

		media_volume_sb.setOnProgressChangedListener(this);
		bt_volume_sb.setOnProgressChangedListener(this);
		navi_volume_sb.setOnProgressChangedListener(this);
		voice_volume_sb.setOnProgressChangedListener(this);

		set_volume_prev_iv.setOnClickListener(this);
		set_volume_next_iv.setOnClickListener(this);

		set_speed_compensation(SettingConstants.speed_compensation_flag);

		if (mAudioManager != null) {
			int media_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			int bt_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO);
			int navi_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_GIS);
			int voice_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

			media_volume_sb.setProgress(media_volume);
			bt_volume_sb.setProgress(bt_volume);
			navi_volume_sb.setProgress(navi_volume);
			voice_volume_sb.setProgress(voice_volume);

			media_volume_value.setText(media_volume + "");
			bt_volume_value.setText(bt_volume + "");
			navi_volume_value.setText(navi_volume + "");
			voice_volume_value.setText(voice_volume + "");
		}

	}

	public void sendMessage(int what, int index) {
		Message message = Message.obtain();
		message.what = what;
		message.obj = index;
		mHandler.sendMessage(message);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			flag = true;
			switch (msg.what) {
			case R.id.media_volume_sb:
				int media_volume = (Integer) msg.obj;
				media_volume_value.setText(media_volume + "");
				if (mAudioManager != null) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, media_volume, AudioManager.ADJUST_SAME);
				}
				break;

			case R.id.bt_volume_sb:
				int bt_volume = (Integer) msg.obj;
				bt_volume_value.setText(bt_volume + "");
				if (mAudioManager != null) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, bt_volume,
							AudioManager.ADJUST_SAME);
				}
				break;

			case R.id.navi_volume_sb:
				int navi_volume = (Integer) msg.obj;
				navi_volume_value.setText(navi_volume + "");
				if (mAudioManager != null) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_GIS, navi_volume, AudioManager.ADJUST_SAME);
				}
				break;

			case R.id.voice_volume_sb:
				int voice_volume = (Integer) msg.obj;
				voice_volume_value.setText(voice_volume + "");
				if (mAudioManager != null) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, voice_volume, AudioManager.ADJUST_SAME);
				}
				break;

			default:
				break;
			}

		}
	};

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		mContext = context;
		mContentView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_page_set_volume,
				null));
		init(context);
		initView(mContentView);

		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
		context.registerReceiver(mBroadcastReceiver, filter);

		return mContentView;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {

				int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, 0);
				int index = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0);
				int oldIndex = intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, 0);
				Log.e("PageSetVolume", "streamType: " + streamType + "  index: " + index + "  oldIndex: " + oldIndex);

				if (flag) {
					flag = false;
				} else {
					switch (streamType) {

					case AudioManager.STREAM_MUSIC:
						if (media_volume_sb != null) {
							if (media_volume_sb.getProgress() != index) {
								media_volume_sb.setProgress(index);
							}
						}
						if (media_volume_value != null) {
							if (!media_volume_value.getText().toString().trim().equals(index + "")) {
								media_volume_value.setText(index + "");
							}
						}
						break;

					case AudioManager.STREAM_BLUETOOTH_SCO:

						if (bt_volume_sb != null) {
							if (bt_volume_sb.getProgress() != index) {
								bt_volume_sb.setProgress(index);
							}

						}
						if (bt_volume_value != null) {
							if (!bt_volume_value.getText().toString().trim().equals(index + "")) {
								bt_volume_value.setText(index + "");
							}
						}

						break;

					case AudioManager.STREAM_GIS:
						if (navi_volume_sb != null) {
							if (navi_volume_sb.getProgress() != index) {
								navi_volume_sb.setProgress(index);
							}

						}
						if (navi_volume_value != null) {
							if (!navi_volume_value.getText().toString().trim().equals(index + "")) {
								navi_volume_value.setText(index + "");
							}
						}
						break;

					case AudioManager.STREAM_ALARM:
						if (voice_volume_sb != null) {
							if (voice_volume_sb.getProgress() != index) {
								voice_volume_sb.setProgress(index);
							}

						}
						if (voice_volume_value != null) {
							if (!voice_volume_value.getText().toString().trim().equals(index + "")) {
								voice_volume_value.setText(index + "");
							}
						}

						break;

					default:
						break;
					}
				}

			}
		}
	};

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(com.carocean.vsettings.view.CustomSeekbar seekBar) {
		// TODO Auto-generated method stub
		sendMessage(seekBar.getId(), seekBar.getProgress());
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.set_volume_prev_iv:
			int prev_speed_compensation_flag = DataShared.getInstance(mContext)
					.getInt(SettingConstants.key_speed_compensation_flag, 0);
			prev_speed_compensation_flag--;
			if (prev_speed_compensation_flag < 0) {
				prev_speed_compensation_flag = 3;
			}
			set_speed_compensation(prev_speed_compensation_flag);
			SettingConstants.speed_compensation_flag = prev_speed_compensation_flag;
			DataShared.getInstance(mContext).putInt(SettingConstants.key_speed_compensation_flag,
					prev_speed_compensation_flag);
			DataShared.getInstance(mContext).commit();
			break;

		case R.id.set_volume_next_iv:
			int next_speed_compensation_flag = DataShared.getInstance(mContext)
					.getInt(SettingConstants.key_speed_compensation_flag, 0);
			next_speed_compensation_flag++;
			if (next_speed_compensation_flag > 3) {
				next_speed_compensation_flag = 0;
			}
			set_speed_compensation(next_speed_compensation_flag);
			SettingConstants.speed_compensation_flag = next_speed_compensation_flag;
			DataShared.getInstance(mContext).putInt(SettingConstants.key_speed_compensation_flag,
					next_speed_compensation_flag);
			DataShared.getInstance(mContext).commit();
			break;

		default:
			break;
		}
	}

	public void set_speed_compensation(int speed_compensation_flag) {

		switch (speed_compensation_flag) {
		case 0:
			if (speed_level_tv != null) {
				speed_level_tv.setText(mContext.getResources().getString(R.string.setting_sound_velocity_off));
			}
			break;
		case 1:
			if (speed_level_tv != null) {
				speed_level_tv.setText(mContext.getResources().getString(R.string.setting_sound_velocity_low));
			}
			break;
		case 2:
			if (speed_level_tv != null) {
				speed_level_tv.setText(mContext.getResources().getString(R.string.setting_sound_velocity_middle));
			}
			break;
		case 3:
			if (speed_level_tv != null) {
				speed_level_tv.setText(mContext.getResources().getString(R.string.setting_sound_velocity_high));
			}
			break;

		default:
			break;
		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
