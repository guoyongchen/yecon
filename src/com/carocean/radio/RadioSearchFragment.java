package com.carocean.radio;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.carocean.R;
import com.carocean.floatwindow.FloatWindowMessage;
import com.carocean.launcher.customView.RadioWidget;
import com.carocean.radio.constants.RadioConstants;
import com.carocean.radio.constants.RadioFreqInfo;
import com.carocean.radio.constants.RadioMessage;
import com.carocean.radio.db.RadioStation;
import com.carocean.radio.view.RadioView;
import com.carocean.radio.view.RadioViewCallBack;
import com.carocean.utils.SoundSourceInfoUtils;
import com.carocean.utils.SourceInfoInterface;
import com.carocean.utils.Utils;
import com.carocean.vmedia.radio.PageRadio;

import java.util.ArrayList;

import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_PAUSE;
import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_PLAY;
import static com.carocean.radio.constants.RadioConstants.BAND_ID_AM;
import static com.carocean.radio.constants.RadioConstants.BAND_ID_FM;
import static com.carocean.radio.constants.RadioConstants.DEFAULT_AM_STATION_MIN;
import static com.carocean.radio.constants.RadioConstants.DEFAULT_FM_STATION_MIN;

public class RadioSearchFragment extends Fragment implements SourceInfoInterface {
	private static final String TAG = "RadioSearchFragment";

	private View mView;
	private TextView mTVFreq, mTVBandUint, radio_tv_band_current, radio_tv_freq_current;
	private ImageButton mFavorite, radio_search;

	private static ImageButton radio_start;
	private static ImageButton radio_pause;
	private Button mTVBand, radio_tv_band_fm, radio_tv_band_am;

	private Context mContext;

	private BtnClick mBtnClick = new BtnClick();
	private BtnLongClick mBtnLongClick = new BtnLongClick();

	private static RadioMessage mMsg = RadioMessage.getInstance();

	private ArrayList<Integer> mFavoriteList = new ArrayList<Integer>();

	private boolean mScanStatus = false;

	private RadioView mRadioView;

	public static final int radio_status_play = 1;
	public static final int radio_status_pause = 2;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
		mContext = activity.getApplicationContext();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SoundSourceInfoUtils.RegisterSourceInfo(this);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		Log.i(TAG, "msg Instance = " + mMsg);
		if (null == mView) {
			mView = inflater.inflate(R.layout.radio_search_fragment, container, false);
		}
		initData();
		initUI();
		regVoiceMsg(getActivity().getApplicationContext());// reg msg for ifly
															// voice
		return mView;
	}

	private void initData() {
		if (null != mContext) {
			initFavoriteData(RadioStation.getRadioBand(mContext));
		}
	}

	private void initUI() {
		if (null != mView) {

			if (null != mMsg) {
				mMsg.registerMsgHandler(mHandler);
			}

			mTVBand = (Button) mView.findViewById(R.id.radio_tv_band);
			radio_tv_band_fm = (Button) mView.findViewById(R.id.radio_tv_band_fm);
			radio_tv_band_am = (Button) mView.findViewById(R.id.radio_tv_band_am);
			mTVFreq = (TextView) mView.findViewById(R.id.radio_tv_freq);
			mTVBandUint = (TextView) mView.findViewById(R.id.radio_tv_freq_uint);
			radio_tv_band_current = (TextView) mView.findViewById(R.id.radio_tv_band_current);
			radio_tv_freq_current = (TextView) mView.findViewById(R.id.radio_tv_freq_current);

			mFavorite = (ImageButton) mView.findViewById(R.id.radio_btn_favorite);
			radio_search = (ImageButton) mView.findViewById(R.id.radio_search);
			radio_start = (ImageButton) mView.findViewById(R.id.radio_start);
			radio_pause = (ImageButton) mView.findViewById(R.id.radio_pause);
			mRadioView = (RadioView) mView.findViewById(R.id.radio_view);
			((ImageButton) mView.findViewById(R.id.radio_btn_prev)).setOnClickListener(mBtnClick);
			((ImageButton) mView.findViewById(R.id.radio_btn_next)).setOnClickListener(mBtnClick);
			((ImageButton) mView.findViewById(R.id.radio_btn_prev)).setOnLongClickListener(mBtnLongClick);
			((ImageButton) mView.findViewById(R.id.radio_btn_next)).setOnLongClickListener(mBtnLongClick);

			final int freq = RadioStation.getCurrentFreq(mContext);
			final int band = RadioStation.getRadioBand(mContext);
			if (null != mRadioView) {
				if (band == BAND_ID_FM) {
					mRadioView.initFmData();
				} else {
					mRadioView.initAmData();
				}
			}

			mRadioView.setCallBack(new RadioViewCallBack() {

				public void resetProgress() {
				}

				public void prevProgress(int freq) {
					if (!mScanStatus) {

						PageRadio.isSlide = true;
						sendRadioToMcu(freq);
						setFreq(freq);
						if (null != FloatWindowMessage.getInstance()) {
							FloatWindowMessage.getInstance().sendMsg(FloatWindowMessage.FLOAT_RADIO_MSG_FREQ, freq);
						}
					}
				}

				public void nextProgress(int freq) {
					if (!mScanStatus) {
						PageRadio.isSlide = true;
						sendRadioToMcu(freq);
						setFreq(freq);
						if (null != FloatWindowMessage.getInstance()) {
							FloatWindowMessage.getInstance().sendMsg(FloatWindowMessage.FLOAT_RADIO_MSG_FREQ, freq);
						}
					}
				}
			});

			mTVBand.setOnClickListener(mBtnClick);
			radio_tv_band_fm.setOnClickListener(mBtnClick);
			radio_tv_band_am.setOnClickListener(mBtnClick);
			mFavorite.setOnClickListener(mBtnClick);
			radio_search.setOnClickListener(mBtnClick);
			radio_start.setOnClickListener(mBtnClick);
			radio_pause.setOnClickListener(mBtnClick);

			setCheck(false);
			setBand();
			setFreq(freq);
		}
	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		SoundSourceInfoUtils.UnRegisterSourceInfo(this);
		unRegVoiceMsg(getActivity().getApplicationContext());

		if (null != mMsg) {
			if (mScanStatus) {
				mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SCAN_STOP, null);
				mScanStatus = false;
			}
			mRadioView.resetProgress();
			mMsg.unregisterMsgHandler(mHandler);
		}
		if (!mFavoriteList.isEmpty()) {
			mFavoriteList.clear();
		}
	}

	public static void prevPreset() {
		if (null != mMsg) {
			mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_PERV_FREQ, null);
		}
	}

	public static void nextPreset() {
		if (null != mMsg) {
			mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_NEXT_FREQ, null);
		}
	}
	
	public static void prevScan() {
		if (null != mMsg) {
			mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SCAN_PERV, null);
		}
	}

	public static void nextScan() {
		if (null != mMsg) {
			mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SCAN_NEXT, null);
		}
	}

	void favoriteCurFreq() {
		if (null != mMsg) {
			mMsg.sendMsg(RadioMessage.MSG_MCU_TO_RADIO_SET_FREQ, !mFavorite.isSelected());
		}
	}

	void startSearch() {
		int current_band_search = RadioStation.getRadioBand(mContext);
		if (current_band_search == BAND_ID_FM) {
			RadioStation.removeAllPresetFreq(mContext, BAND_ID_FM);
			RadioMessage.getInstance().sendMsg(RadioMessage.MSG_RADIO_TO_MCU_FM_SCAN_START, null);
		} else if (current_band_search == BAND_ID_AM) {
			RadioStation.removeAllPresetFreq(mContext, BAND_ID_AM);
			RadioMessage.getInstance().sendMsg(RadioMessage.MSG_RADIO_TO_MCU_AM_SCAN_START, null);
		}
	}

	public static void switchBand(Context context, int iBand) {
		if (RadioStation.getRadioBand(context) != iBand) {
			RadioStation.setRadioBand(context, iBand);
			mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_BAND, null);
		}
	}

	public static void sendFreq(Context context, int iFreq, int iBand) {
		if (RadioStation.getRadioBand(context) != iBand) {
			RadioStation.setRadioBand(context, iBand);
		}
		if (RadioStation.getCurrentFreq(context) != iFreq) {
			RadioStation.setCurrentFreq(context, iFreq);
		}
		Bundle bundle = new Bundle();
		bundle.putInt("freq", iFreq);
		bundle.putInt("band", iBand);
		mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ, bundle);
	}

	private class BtnClick implements OnClickListener {

		public void onClick(View v) {
			// 搜台时不允许点击其他按钮
			if (null != v && !mScanStatus) {
				final int id = v.getId();

				switch (id) {
				case R.id.radio_btn_prev:
					prevScan();
					break;
				case R.id.radio_btn_next:
					nextScan();
					break;
				case R.id.radio_start:
					setButtonStatus(radio_status_play);
					Intent radio_start_intent = new Intent(MCU_ACTION_MEDIA_PLAY);
					mContext.sendBroadcast(radio_start_intent);
					break;
				case R.id.radio_pause:
					setButtonStatus(radio_status_pause);
					Intent radio_pause_intent = new Intent(MCU_ACTION_MEDIA_PAUSE);
					mContext.sendBroadcast(radio_pause_intent);
					break;
				case R.id.radio_btn_favorite:
					favoriteCurFreq();
					break;
				case R.id.radio_search:
					startSearch();
					break;
				case R.id.radio_tv_band_fm:
					switchBand(mContext, RadioConstants.BAND_ID_FM);
					break;
				case R.id.radio_tv_band_am:
					switchBand(mContext, RadioConstants.BAND_ID_AM);
					break;
				default:
					break;
				}
			} else {
				if (mScanStatus && v.getId() == R.id.radio_btn_favorite) {
					setCheck(false);
				}
			}
		}
	}

	public static void setButtonStatus(int play_or_pause) {
		if (play_or_pause == radio_status_play) {
			radio_start.setVisibility(View.GONE);
			radio_pause.setVisibility(View.VISIBLE);
		} else if (play_or_pause == radio_status_pause) {
			radio_start.setVisibility(View.VISIBLE);
			radio_pause.setVisibility(View.GONE);
		}
	}

	private void initFavoriteData(int band) {

		if (!mFavoriteList.isEmpty()) {
			mFavoriteList.clear();
		}

		ArrayList<RadioFreqInfo> favoriteList = RadioStation.getAllFavoriteFreqInfo(mContext, band);
		if (null != favoriteList && !favoriteList.isEmpty()) {
			for (RadioFreqInfo d : favoriteList) {
				mFavoriteList.add(d.getFreq());
			}
		}

		if (!mFavoriteList.isEmpty()) {
			int index = mFavoriteList.indexOf(band == BAND_ID_FM ? DEFAULT_FM_STATION_MIN : DEFAULT_AM_STATION_MIN);
			setCheck(index != -1 ? true : false);
		} else {
			setCheck(false);
		}
	}

	private class BtnLongClick implements OnLongClickListener {
		public boolean onLongClick(View v) {

			boolean flag = true;
			if (null != v && !mScanStatus) {
				switch (v.getId()) {
				case R.id.radio_btn_prev:
					// if (null != mMsg) {
					// mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SCAN_PERV,
					// null);
					// }
					prevPreset();
					break;
				case R.id.radio_btn_next:
					// if (null != mMsg) {
					// mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SCAN_NEXT,
					// null);
					// }
					nextPreset();
					break;
				default:
					flag = false;
					break;
				}
			}
			return flag;
		}

	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case RadioMessage.MSG_MCU_TO_RADIO_SET_FREQ:
				if (!mScanStatus) {
					setCheck((Boolean) msg.obj);
					saveFavorite();
				}
				break;
			case RadioMessage.MSG_MCU_TO_RADIO_SET_BAND:
				setBand();
				break;
			case RadioMessage.MSG_UPDATA_FREQ:
				setFreq((Integer) msg.obj);
				break;
			case RadioMessage.MSG_MCU_TO_RADIO_SCAN_STATUS:
				mScanStatus = ((Integer) msg.obj) > 0 ? true : false;
				if (mScanStatus) {
					mRadioView.resetProgress();
				}
				break;
			case RadioMessage.MSG_MCU_TO_RADIO_SCAN_FREQ:
				mRadioView.next();
				break;
			case RadioMessage.MSG_UPDATA_PROGRESS:
				final int freq = ((Integer) msg.obj);
				mRadioView.setProgress(freq);
				break;
			case RadioWidget.MSG_UPGRADE_UI:
				int bPlay = msg.arg1;
				if (bPlay == 1) {
					setButtonStatus(radio_status_play);
				} else {
					setButtonStatus(radio_status_pause);
				}
				break;
			default:
				break;
			}
		}
	};

	private void setBand() {
		final int band = RadioStation.getRadioBand(mContext);
		// if (null != mTVBand) {
		// mTVBand.setText(band == BAND_ID_FM ?
		// getString(R.string.radio_band_fm) :
		// getString(R.string.radio_band_am));
		// }
		if (band == BAND_ID_FM) {
			setFMUI();
			initFavoriteData(BAND_ID_FM);
			mRadioView.initFmData();
		} else if (band == BAND_ID_AM) {
			setAMUI();
			initFavoriteData(BAND_ID_AM);
			mRadioView.initAmData();
		}
	}

	public void setFMUI() {
		if (null != radio_tv_band_fm) {
			radio_tv_band_fm.setBackgroundResource(R.drawable.fm_select);
		}
		if (null != radio_tv_band_am) {
			radio_tv_band_am.setBackgroundResource(0);
		}
	}

	public void setAMUI() {
		if (null != radio_tv_band_fm) {
			radio_tv_band_fm.setBackgroundResource(0);
		}
		if (null != radio_tv_band_am) {
			radio_tv_band_am.setBackgroundResource(R.drawable.am_select);
		}
	}

	private void setFreq(int freq) {
		final int band = RadioStation.getRadioBand(mContext);
		String freqStr = band == BAND_ID_FM ? String.format("%.1f", freq / 100.0f) : String.format("%d", freq);
		if (null != mTVFreq) {
			mTVFreq.setText(freqStr);
		}
		if (null != radio_tv_freq_current) {
			radio_tv_freq_current.setText(freqStr);
		}
		if (null != radio_tv_band_current) {
			radio_tv_band_current.setText(
					band == BAND_ID_FM ? getString(R.string.radio_band_fm) : getString(R.string.radio_band_am));
		}
		if (null != mTVBandUint) {
			mTVBandUint.setText(band == BAND_ID_FM ? "MHz" : "KHz");
		}

		if (null != mRadioView) {
			mMsg.sendMsg(RadioMessage.MSG_UPDATA_PROGRESS, freq);
		}

		if (null != mMsg && !mFavoriteList.isEmpty()) {
			if (-1 != mFavoriteList.indexOf(freq)) {
				setCheck(true);
			} else {
				setCheck(false);
			}
		}
	}

	private void setCheck(boolean isShow) {
		if (null != mFavorite) {
			mFavorite.setSelected(isShow);
		}
	}

	private void saveFavorite() {
		if (null != mContext) {

			final int freq = RadioStation.getCurrentFreq(mContext);
			final int band = RadioStation.getRadioBand(mContext);

			if (mFavorite.isSelected()) {
				ArrayList<RadioFreqInfo> current_FavoriteList = RadioStation.getAllFavoriteFreqInfoList(mContext);
				if (null != current_FavoriteList) {
					if (current_FavoriteList.size() < 20) {
						RadioStation.addFavorite(mContext, freq);
					} else {
						Utils.showToast(mContext.getResources().getString(R.string.radio_favorite_toast));
						mFavorite.setSelected(false);
					}
				}

			} else {
				if (null != mContext) {
					RadioStation.removeFavorite(mContext, band, freq);
					if (!mFavoriteList.isEmpty()) {
						final int index = mFavoriteList.indexOf(freq);
						if (index != -1) {
							mFavoriteList.remove(index);
						}
					}
				}
			}

			if (!mFavoriteList.isEmpty()) {
				mFavoriteList.clear();
			}

			ArrayList<RadioFreqInfo> favoriteList = RadioStation.getAllFavoriteFreqInfo(mContext, band);
			if (null != favoriteList && !favoriteList.isEmpty()) {
				for (RadioFreqInfo d : favoriteList) {
					mFavoriteList.add(d.getFreq());
				}
			}
		}
	}

	private void sendRadioToMcu(int freq) {
		Bundle bundle = new Bundle();
		bundle.putInt("freq", freq);
		bundle.putInt("band", RadioStation.getRadioBand(mContext));
		mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ, bundle);
	}

	private void sendRadioToMcu(int freq, int band) {
		Bundle bundle = new Bundle();
		bundle.putInt("freq", freq);
		bundle.putInt("band", band);
		mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ, bundle);
	}

	// for voice interface
	private BroadcastReceiver mVoiceReceiver;

	private void regVoiceMsg(Context context) {
		mVoiceReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (null == action) {
					return;
				}

				if (action.equals(RadioConstants.ACTION_IFLY_VOICE_RADIO)) {
					int cmd = intent.getIntExtra(RadioConstants.EXTRA_RADIO_CMD, 0);
					Log.i(TAG, "radio---receive ifly voice msg, cmd=" + cmd);

					switch (cmd) {
					case RadioConstants.RADIO_CMD_PREV_PRESET:
						prevPreset();
						break;
					case RadioConstants.RADIO_CMD_NEXT_PRESET:
						nextPreset();
						break;
					case RadioConstants.RADIO_CMD_PREV_STEP:
						break;
					case RadioConstants.RADIO_CMD_NEXT_STEP:
						break;
					case RadioConstants.RADIO_CMD_SEARCH:
						startSearch();
						break;
					case RadioConstants.RADIO_CMD_PREV_SEARCH:
						break;
					case RadioConstants.RADIO_CMD_NEXT_SEARCH:
						break;
					case RadioConstants.RADIO_CMD_FAVOR_CURFREQ:
						favoriteCurFreq();
						break;
					default:
						break;
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(RadioConstants.ACTION_IFLY_VOICE_RADIO);
		context.registerReceiver(mVoiceReceiver, filter);
	}

	private void unRegVoiceMsg(Context context) {
		if (mVoiceReceiver != null) {
			context.unregisterReceiver(mVoiceReceiver);
		}
	}
	// for voice interface end

	@Override
	public void updateSourceInfo(int source, int iUnRegisterSource) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateRadioPlayStatus(boolean bPlay) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.what = RadioWidget.MSG_UPGRADE_UI;
		if (bPlay) {
			msg.arg1 = 1;
		} else {
			msg.arg1 = 0;
		}
		mHandler.sendMessage(msg);

	}
}
