package com.carocean.launcher.customView;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaTrackInfo;
import com.carocean.utils.Constants;
import com.carocean.utils.MarqueeTextView;
import com.carocean.utils.SoundSourceInfoUtils;
import com.carocean.utils.SourceInfoInterface;
import com.carocean.utils.SourceManager;
import com.carocean.utils.Utils;
import com.yecon.savedata.SaveData;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.kuwo.autosdk.api.OnPlayerStatusListener;
import cn.kuwo.autosdk.api.PlayerStatus;
import cn.kuwo.base.bean.Music;

public class MediaWidget extends LinearLayout implements OnClickListener, SourceInfoInterface {
	private final String TAG = "MediaWidget";
	public static final String ACTION_NOTIFICATION_PRE = "com.yecon.action.MEDIA_PREVIOUS";
	public static final String ACTION_NOTIFICATION_PLAYE_PAUSE = "com.yecon.action.MEDIA_PLAY_PAUSE";
	public static final String ACTION_NOTIFICATION_PLAYE_ONLYPLAY = "com.yecon.action.MEDIA_PLAY";
	public static final String ACTION_NOTIFICATION_NEXT = "com.yecon.action.MEDIA_NEXT";
	ImageView mPrevBtn = null;
	ImageView mPlayBtn = null;
	ImageView mPauseBtn = null;
	ImageView mNextBtn = null;
	LinearLayout mCtrlLayout = null;
	MarqueeTextView mMediaArtist = null, mMediaTitle = null;
	TextView media_text_time;
	int mTitleColor = Color.WHITE;
	float mTitleSize = 24;
	public static SaveData mSaveData;
	ActivityManager activityManager;
	private static Context mContext;
	private final int MSG_UPDATE_TIME = 255;
	private final int MSG_UPDATE_SRC = 254;

	void initView(Context context) {

		mContext = ApplicationManage.getContext();

		View view_ex = LayoutInflater.from(context).inflate(R.layout.launcher_widget_media_layout, this);
		View view = view_ex.findViewById(R.id.widget_layout);
		if (null != view) {
			view.setOnClickListener(this);
		}
		mPrevBtn = (ImageView) view_ex.findViewById(R.id.music_prev_btn);
		mPlayBtn = (ImageView) view_ex.findViewById(R.id.music_play_btn);
		mPauseBtn = (ImageView) view_ex.findViewById(R.id.music_pause_btn);
		mNextBtn = (ImageView) view_ex.findViewById(R.id.music_next_btn);
		mMediaArtist = (MarqueeTextView) view_ex.findViewById(R.id.media_text_artist);
		mMediaTitle = (MarqueeTextView) view_ex.findViewById(R.id.media_title);
		media_text_time = (TextView) view_ex.findViewById(R.id.media_text_time);
		mCtrlLayout = (LinearLayout) view_ex.findViewById(R.id.music_layout_ctrl);

		if (mPrevBtn != null && mPlayBtn != null && mPauseBtn != null && mNextBtn != null) {
			mPrevBtn.setOnClickListener(this);
			mPlayBtn.setOnClickListener(this);
			mPauseBtn.setOnClickListener(this);
			mNextBtn.setOnClickListener(this);
		}

		MediaActivityProxy.getInstance().RegisterHandler(mHandler);
		Log.i("lihaibin", "initView RegisterSourceInfo this:" + this);
		SoundSourceInfoUtils.RegisterSourceInfo(this);
		BTService.registerNotifyHandler(uiHandler);
//		ApplicationManage.getKwapi().registerPlayerStatusListener(new OnPlayerStatusListener() {
//			
//			@Override
//			public void onPlayerStatus(PlayerStatus playerStatus, Music music) {
//				// TODO Auto-generated method stub
//				String string = "";
//				if (playerStatus == PlayerStatus.PAUSE) {
//					string += "PAUSE";
//					mHandler.removeMessages(MSG_UPDATE_TIME);
//				} else if (playerStatus == PlayerStatus.PLAYING) {
//					string += "PLAYING";
//					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
//				} else if (playerStatus == PlayerStatus.STOP) {
//					mHandler.removeMessages(MSG_UPDATE_TIME);
//					string += "STOP";
//				}
//				if (music != null) {
//					string += music.name;
//					string += music.artist;
//					string += music.album;
//					if (music.name != null) {
//						mMediaTitle.setText(music.name);
//					}
//					if (music.artist != null) {
//						mMediaArtist.setText(music.artist);
//					}
//				}
//				Log.i(TAG, "onPlayerStatus string:" + string);
//			}
//		});
	}

	public MediaWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaWidget);
		mTitleColor = ta.getColor(R.styleable.MediaWidget_titleColor, Color.YELLOW);
		mTitleSize = ta.getDimension(R.styleable.MediaWidget_titleSize, 24);
		ta.recycle();

		initView(context);
	}

	public MediaWidget(Context context) {
		super(context);

		initView(context);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == R.id.widget_layout) {
			launcherUtils.startMedia();
		} else if (arg0.getId() == R.id.music_prev_btn) {

			ApplicationManage.getContext().sendBroadcast(new Intent(ACTION_NOTIFICATION_PRE));

		} else if (arg0.getId() == R.id.music_play_btn) {

			ApplicationManage.getContext().sendBroadcast(new Intent(ACTION_NOTIFICATION_PLAYE_PAUSE));

		} else if (arg0.getId() == R.id.music_pause_btn) {

			ApplicationManage.getContext().sendBroadcast(new Intent(ACTION_NOTIFICATION_PLAYE_PAUSE));

		} else if (arg0.getId() == R.id.music_next_btn) {

			ApplicationManage.getContext().sendBroadcast(new Intent(ACTION_NOTIFICATION_NEXT));

		}

	}

	public static void sendKeyCode(final int keyCode) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(keyCode);
				} catch (Exception e) {
					// TODO: handle exception
				}
			};
		}.start();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.e(TAG, "handleMessage msg.arg:" + msg.arg1);
			switch (msg.what) {
			case MediaPlayerMessage.UPDATE_MEDIA_TYPE:
				if (SourceManager.getSource() != Constants.KEY_SRC_MODE_BT_MUSIC && 
				msg.arg1 == MediaType.MEDIA_VIDEO) {
					setNoMusicPlay();
				}
				break;
			case MediaPlayerMessage.UPDATE_PLAY_PROGRESS:
				try {
					if (SourceManager.getSource() != Constants.KEY_SRC_MODE_BT_MUSIC) {
						if (MediaActivityProxy.getService().getMediaType() == MediaType.MEDIA_AUDIO) {
							String progress = MediaActivityProxy.formatData(msg.arg1, false);
							String total = MediaActivityProxy.formatData(msg.arg2, false);
							if (media_text_time != null) {
								media_text_time.setText(progress + "-" + total);
							}
						}
					}
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				break;
			case MediaPlayerMessage.UPDATE_PLAY_STATE:
				try {
					if (SourceManager.getSource() != Constants.KEY_SRC_MODE_BT_MUSIC && 
							MediaActivityProxy.getService().getMediaType() == MediaType.MEDIA_AUDIO) {
						if (msg.arg1 == PlayStatus.DECODED || msg.arg1 == PlayStatus.STARTED
								|| msg.arg1 == PlayStatus.ERROR) {
							mCtrlLayout.setVisibility(View.VISIBLE);
							MediaTrackInfo cv = MediaActivityProxy.getService().getPlayingFileInfo();
							if (cv != null && cv.getTrackTotal() != 0) {
								if (mMediaTitle != null) {
									mMediaTitle.setText(cv.getTitle());
								}

								if (mMediaArtist != null) {
									mMediaArtist.setText(cv.getArtist());
								}

								String progress_end = MediaActivityProxy.formatData(cv.getCurTime(), false);
								String total_end = MediaActivityProxy.formatData(cv.getDuration(), false);
								if (media_text_time != null) {
									media_text_time.setText(progress_end + "-" + total_end);
								}
							}
						}
						if (msg.arg1 == PlayStatus.PAUSED || msg.arg1 == PlayStatus.IDLE) {
							if (msg.arg1 == PlayStatus.IDLE) {
								MediaTrackInfo cv = MediaActivityProxy.getService().getPlayingFileInfo();
								if (cv == null) {
									setNoMusicPlay();
								}
							}
							// pasue
							mPlayBtn.setVisibility(View.VISIBLE);
							mPauseBtn.setVisibility(View.GONE);

						} else {
							// play
							mPlayBtn.setVisibility(View.GONE);
							mPauseBtn.setVisibility(View.VISIBLE);
						}
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
//			case MSG_UPDATE_TIME:
//				if (ApplicationManage.getKwapi().getPlayerStatus() == PlayerStatus.PLAYING) {
//					if (media_text_time != null) {
//						Log.i(TAG, "MSG_UPDATE_TIME timetext:" + updateTimeText(ApplicationManage.getKwapi().getCurrentMusicDuration(), ApplicationManage.getKwapi().getCurrentPos()));
//						media_text_time.setText(updateTimeText(ApplicationManage.getKwapi().getCurrentMusicDuration(), ApplicationManage.getKwapi().getCurrentPos()));
//						invalidate();
//						Log.i(TAG, "MSG_UPDATE_TIME media_text_time:" + media_text_time.getText());
//					}
//					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
//				}
//				break;
			case MSG_UPDATE_SRC:
				setNoMusicPlay();
				break;
			default:
				break;
			}
		};
	};

	private Handler uiHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String recievedAction = intent.getAction();
				if (recievedAction.equals(BTService.ACTION_DISCONNECTED)) {
					if (!BTUtils.mBluetooth.isA2DPconnected() && 
							SourceManager.getSource() == Constants.KEY_SRC_MODE_BT_MUSIC) {
						setNoMusicPlay();
					}

				} else if (recievedAction.equals(BTService.ACTION_MUSIC_ID3)) {

					String mediaTitleInfo = BTUtils.mBluetooth.title;
					String mediaArtistInfo = BTUtils.mBluetooth.singer;

					Log.e(TAG, "mediaTitleInfo: " + mediaTitleInfo + "  mediaArtistInfo: " + mediaArtistInfo);

					if (SourceManager.getSource() == Constants.KEY_SRC_MODE_BT_MUSIC) {
						mCtrlLayout.setVisibility(View.VISIBLE);
						if (mMediaTitle != null) {
							if (TextUtils.isEmpty(mediaTitleInfo)) {
								mMediaTitle.setText("unknown");
							} else {
								mMediaTitle.setText(mediaTitleInfo);
							}

						}
						if (mMediaArtist != null) {
							if (TextUtils.isEmpty(mediaArtistInfo)) {
								mMediaArtist.setText("unknown");
							} else {
								mMediaArtist.setText(mediaArtistInfo);
							}

						}

						if (TextUtils.isEmpty(mediaTitleInfo) && TextUtils.isEmpty(mediaArtistInfo)) {
							setNoMusicPlay();
						}
					}

				} else if (recievedAction.equals(BTService.ACTION_MUSIC_PLAYING)) {
					boolean bplaying = intent.getBooleanExtra(BTService.EXTRA_STATE, false);
					if (bplaying) {
						mPlayBtn.setVisibility(View.GONE);
						mPauseBtn.setVisibility(View.VISIBLE);
					}else{
						mPlayBtn.setVisibility(View.VISIBLE);
						mPauseBtn.setVisibility(View.GONE);
					}
				}else if (recievedAction.equals(BTService.ACTION_MUSIC_PLAY_POS)) {
					if (SourceManager.getSource() == Constants.KEY_SRC_MODE_BT_MUSIC) {
						int mediaLength = BTUtils.mBluetooth.music_time_max;
						int mediaPosition = BTUtils.mBluetooth.music_time_cur;
						if (media_text_time != null) {
							media_text_time.setText(updateTimeText(mediaLength, mediaPosition));
						}
						Log.e(TAG, "mediaLength: " + mediaLength + "  mediaPosition: " + mediaPosition);
					}
				}
			}
		}
	};

	public void setNoMusicPlay() {
		if (mPlayBtn != null) {
			mPlayBtn.setVisibility(View.VISIBLE);
		}
		if (mPauseBtn != null) {
			mPauseBtn.setVisibility(View.GONE);
		}
		if (mMediaTitle != null) {
			mMediaTitle.setText(mContext.getResources().getString(R.string.general_music));
		}
		if (mMediaArtist != null) {
			mMediaArtist.setText("");
		}
		if (media_text_time != null) {
			media_text_time.setText("");
		}
		if (mCtrlLayout != null) {
			mCtrlLayout.setVisibility(View.GONE);
		}
	}

	private String updateTimeText(int song_length, int song_position) {
		String result = null;
		String length = "00:00";
		String pos = "00:00";

		if (song_length != 0xFFFFFFFF) {
			length = millSeconds2readableTime(song_length);
		}
		if (song_position != 0xFFFFFFFF) {
			pos = millSeconds2readableTime(song_position);
		}
		result = pos + "-" + length;
		return result;
	}

	private String millSeconds2readableTime(int millseconds) {
		int totalSeconds = millseconds / 1000;
		int hour = (totalSeconds / 60) / 60;
		int minute = (totalSeconds / 60) % 60;
		int second = totalSeconds % 60;
		return String.format("%02d:%02d", minute, second);
	}

	@Override
	public void updateSourceInfo(int source, int iUnRegisterSource) {
		// TODO Auto-generated method stub
		Log.e(TAG, "source: " + source + "  iUnRegisterSource: " + iUnRegisterSource + ", running:" + Utils.getKwapi().isKuwoRunning());
		//if (source == 0 && ApplicationManage.getKwapi().getPlayerStatus() != PlayerStatus.PLAYING) {
		mHandler.removeMessages(MSG_UPDATE_SRC);
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_SRC;
		message.arg1 = source;
		message.arg2 = iUnRegisterSource;
		mHandler.sendMessage(message);
	}

	@Override
	public void updateRadioPlayStatus(boolean bPlay) {
		// TODO Auto-generated method stub
		Log.e(TAG, "MediaWidget----bPlay: " + bPlay);
	}

}
