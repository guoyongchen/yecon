package com.carocean.vmedia.radio;

import static android.constant.YeconConstants.ACTION_QB_POWEROFF;
import static android.constant.YeconConstants.ACTION_QB_POWERON;
import static android.constant.YeconConstants.ACTION_QB_PREPOWEROFF;
import static android.constant.YeconConstants.SRC_VOLUME_RADIO;
import static android.mcu.McuExternalConstant.*;

import static com.carocean.radio.constants.RadioConstants.BAND_ID_AM;
import static com.carocean.radio.constants.RadioConstants.BAND_ID_FM;
import static com.carocean.radio.constants.RadioConstants.DEFAULT_AM_STATION_MIN;
import static com.carocean.radio.constants.RadioConstants.DEFAULT_FM_STATION_MIN;
import static com.carocean.radio.constants.RadioConstants.PROPERTY_KEY_AVIN_TYPE;

import com.carocean.utils.Constants;
import com.carocean.utils.SoundSourceInfoUtils;
import com.carocean.utils.SourceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.autochips.inputsource.AVIN;
import com.autochips.inputsource.InputSource;
import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.floatwindow.FloatWindowMessage;
import com.carocean.page.IPage;
import com.carocean.radio.RadioAMFragment;
import com.carocean.radio.RadioFMFragment;
import com.carocean.radio.RadioFavoriteFragment;
import com.carocean.radio.RadioMediaButtonReciver;
import com.carocean.radio.RadioSearchFragment;
import com.carocean.radio.constants.RadioConstants;
import com.carocean.radio.constants.RadioMcuManager;
import com.carocean.radio.constants.RadioMessage;
import com.carocean.radio.db.RadioStation;
import com.carocean.service.BootService;
import com.carocean.utils.sLog;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mcu.McuBaseInfo;
import android.mcu.McuExternalConstant;
import android.mcu.McuListener;
import android.mcu.McuManager;
import android.mcu.McuRadioExtBandInfo;
import android.mcu.McuRadioFreqInfo;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * @ClassName: PageRadio
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageRadio implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {
	private static final String TAG = "PageRadio";
	Context mContext;
	private ViewGroup mRootView;

	private final String ACTION_AVIN_REQUEST_NOTIFY = "yecon.intent.action.AVIN.REQUEST";
	private final String ACTION_UPDATA_LANGUAGE = "UPDATA_LANGUAGE";

	private static final Object mLock = new Object();
	private boolean mAppIsFinished = false;

	private McuManager mMcuManager;
	private RadioMcuManager mRadioMcuManager;
	private AudioManager mAudioManager;
	private AVIN mAvin;
	private boolean mPauseAudio = false;
	public static Boolean isSRadio = false;
	public static boolean isSlide = false;

	private RadioGroup mRadioGroup;
	private Fragment mCurFragment = null;
	private RadioSearchFragment mRadioSearchFragment = new RadioSearchFragment();
	private RadioFMFragment mRadioFMFragment = new RadioFMFragment();
	private RadioAMFragment mRadioAMFragment = new RadioAMFragment();
	private RadioFavoriteFragment mRadioFavoriteFragment = new RadioFavoriteFragment();

	private RadioMessage mRadioMsg = RadioMessage.getInstance();
	private ComponentName mbCN = null;

	private boolean mbInit = false;

	private enum PageType {
		RADIO_SEARCH_PAGE, RADIO_FM_PAGE, RADIO_AM_PAGE, RADIO_SELECT_PAGE, RADIO_CLOSE_PAGE
	}

	private boolean mbLostbAudioFocusForever = true; // 永久丢失audio focus
	private boolean mbOccupyAudioFocus = false; // 当前是否占有audio focus

	private McuListener mMcuListener = new McuListener() {

		public void onMcuInfoChanged(McuBaseInfo mcuBaseInfo, int infoType) {
			if (mcuBaseInfo == null) {
				Log.i(TAG, "onMcuInfoChanged - mcuBaseInfo is null");
				return;
			}

			if (infoType == McuExternalConstant.MCU_RADIO_BAND_FREQ_INFO_TYPE) {
				onRadioBandFreqInfoChanged(mcuBaseInfo);
			} else if (infoType == McuExternalConstant.MCU_RADIO_EXT_BAND_INFO_TYPE) {
				onRadioExtBandInfoChanged(mcuBaseInfo);
			}
		}

		private void onRadioExtBandInfoChanged(McuBaseInfo mcuInfo) {
			synchronized (mLock) {
				McuRadioExtBandInfo info = mcuInfo.getRadioExtBandInfo();
				if (null == info)
					return;
				if (null != mRadioMsg) {
					mRadioMsg.sendMsg(RadioMessage.MSG_MCU_TO_RADIO_SCAN_STATUS, info.getAutoMemoryScanStatus());
				}
			}
		}

		private int mBand = -1;

		private void onRadioBandFreqInfoChanged(McuBaseInfo mcuInfo) {

			synchronized (mLock) {
				McuRadioFreqInfo info = mcuInfo.getRadioFreqInfo();
				if (info == null)
					return;
				// add by yangcuiyuan 当前是收音机且屏保在，只解除屏保
				if (SystemProperties.getBoolean("persist.sys.srceenkey", false)) {
					Intent exit_intent = new Intent("com.yecon.action.SCREENSAVER");
					exit_intent.putExtra("cmd", "exit");
					mContext.sendBroadcastAsUser(exit_intent, UserHandle.ALL);
				}

				final int freq = info.getCurrBandFreq();
				final int band = info.getCurrBand();
				final boolean valid = info.isValidFreq();

				RadioStation.setRadioBand(mContext, band);
				RadioStation.setCurrentFreq(mContext, freq);

				if (valid) {
					RadioStation.addPresetFreq(mContext, freq);
				}

				setRadioVolume(band);

				if (null != mRadioMsg) {
					mRadioMsg.sendMsg(RadioMessage.MSG_UPDATA_FREQ, freq);
				}

				if (null != FloatWindowMessage.getInstance()) {
					FloatWindowMessage.getInstance().sendMsg(FloatWindowMessage.FLOAT_RADIO_MSG_FREQ, freq);
				}

				if (mBand != band) {
					mBand = band;
					mRadioMsg.sendMsg(RadioMessage.MSG_MCU_TO_RADIO_SET_BAND, null);

					if (null != FloatWindowMessage.getInstance()) {
						FloatWindowMessage.getInstance().sendMsg(FloatWindowMessage.FLOAT_RADIO_MSG_BAND, band);
					}
				}
			}
		}

		private void setRadioVolume(int band) {
			if (band == BAND_ID_FM) {
				SystemProperties.set(PROPERTY_KEY_AVIN_TYPE, "fm_type");
			} else {
				SystemProperties.set(PROPERTY_KEY_AVIN_TYPE, "am_type");
			}

			int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		}
	};

	private BroadcastReceiver mRadioReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i(TAG, "mRadioReceiver - action: " + action);
			if (null != action && action.equals(MCU_ACTION_MEDIA_PLAY_PAUSE)) {
				pauseAvin();
			} else if (action.equals(MCU_ACTION_MEDIA_PAUSE)) {
				RadioSearchFragment.setButtonStatus(RadioSearchFragment.radio_status_pause);
				pauseAvin();
			} else if (action.equals(MCU_ACTION_MEDIA_PLAY)) {
				RadioSearchFragment.setButtonStatus(RadioSearchFragment.radio_status_play);
				resumeAvin();
			} else if (action.equals(MCU_ACTION_MEDIA_NEXT)) {
				RadioSearchFragment.nextPreset();
			} else if (action.equals(MCU_ACTION_MEDIA_PREVIOUS)) {
				RadioSearchFragment.prevPreset();
			} else if (ACTION_QB_POWERON.equals(action) || MCU_ACTION_ACC_ON.equals(action)) {
				new Thread() {
					public void run() {
						synchronized (mLock) {
							SystemClock.sleep(200);
							Log.i(TAG, "mQuickBootListener - mAppIsFinished: " + mAppIsFinished);
							if (mAppIsFinished)
								return;
							deinitAvin();
							initAvin();
						}
					}
				}.start();
			} else if (MCU_ACTION_ACC_OFF.equals(action)) {
				deinitAvin();
			} else if (ACTION_QB_POWEROFF.equals(action) || ACTION_QB_PREPOWEROFF.equals(action)) {

			} else if (ACTION_AVIN_REQUEST_NOTIFY.equals(intent.getAction())) {
				deinitAvin();

			} else if (ACTION_UPDATA_LANGUAGE.equals(intent.getAction())) {

			} else if (BootService.ACTION_IFLY_VOICE_APP.equals(intent.getAction())) {
				int operation = intent.getIntExtra(BootService.EXTRA_APP_OPERATION, 0);
				int extra_app_id = intent.getIntExtra(BootService.EXTRA_APP_ID, 0);
				if (operation == 2 && extra_app_id == 1) {
					closeRadio(true);
				}
			}
		}
	};

	private void pauseAvin() {
		if (!mPauseAudio) {
			mPauseAudio = true;
			deinitAvin();
		}
	}

	private void resumeAvin() {
		if (mPauseAudio || !mbOccupyAudioFocus) {
			if (!mHandler.hasCallbacks(rblResumeAvin)) {
				mHandler.postDelayed(rblResumeAvin, 500);
			}
		}
	}

	void init() {
		if (null != mHandler && null != mRadioMsg) {
			mRadioMsg.registerMsgHandler(mHandler);
		}
		initSource();
		initReceiver();
		initMcu();
		mbInit = true;
	}

	void initView(ViewGroup root) {
		initBtn(root);
		showView(PageType.RADIO_SEARCH_PAGE);
	}

	@Override
	public void addNotify() {
		mHandler.removeMessages(RadioMessage.MSG_INIT_RADIO);
		mHandler.sendEmptyMessageDelayed(RadioMessage.MSG_INIT_RADIO, 100);
		Log.i(TAG, "addNotify");
	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_radio, null));
			mContext = context;
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			mbCN = new ComponentName(ApplicationManage.getContext().getPackageName(),
					RadioMediaButtonReciver.class.getName());
			init();
			initView(mRootView);
		}
		return mRootView;
	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub
		isSRadio = false;
		Log.i(TAG, "removeNotify");
	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		default:
			break;
		}
	}

	void changeProgress(CustomSeekbar seekbar, int offset) {
		int pos = seekbar.getProgress() + offset;
		seekbar.setProgress(pos);
		onChanged(seekbar);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			mHandler.sendEmptyMessageDelayed(view.getId(), 100);
			break;
		case MotionEvent.ACTION_UP:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			break;
		default:
			break;
		}
		return false;
	}

	private void showFragment(Fragment frament) {
		if (frament != null && frament != mCurFragment) {

			FragmentManager ObjFragmentManager = ((Activity) mContext).getFragmentManager();
			FragmentTransaction ObjTransaction = ObjFragmentManager.beginTransaction();
			if (null != ObjFragmentManager && null != ObjTransaction) {
				ObjTransaction.replace(R.id.radio_fragment, frament);
				ObjTransaction.commit();

				if (null != mCurFragment) {
					ObjTransaction.remove(mCurFragment);
				}
				mCurFragment = frament;
			}
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case RadioMessage.MSG_RADIO_TO_MCU_FM_SCAN_START:
				if (null != mRadioMcuManager) {
					RadioStation.setRadioBand(mContext, BAND_ID_FM);
					RadioStation.setCurrentFreq(mContext, DEFAULT_FM_STATION_MIN);
					byte[] data = { 0x00, 0x22, (byte) 0x2E };
					// mRadioMcuManager.setBand(data);
				}
				startFreqScan();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_AM_SCAN_START:
				if (null != mRadioMcuManager) {
					RadioStation.setRadioBand(mContext, BAND_ID_AM);
					RadioStation.setCurrentFreq(mContext, DEFAULT_AM_STATION_MIN);
					byte[] data = { 0x03, 0x02, 0x13 };
					// mRadioMcuManager.setBand(data);
				}
				startFreqScan();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_SCAN_STOP:
				stopFreqScan();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_PERV_FREQ:
				onPresetPre();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_NEXT_FREQ:
				onPresetNext();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ:
				Bundle bundle = (Bundle) msg.obj;
				if (null != bundle) {
					final int band = bundle.getInt("band");
					final int freq = bundle.getInt("freq");
					sendFreqToMcu(band, freq);
				}
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_SEND_BAND: {
				int band = RadioStation.getRadioBand(mContext);
				int freq = RadioStation.getCurrentFreq(mContext);

				byte[] data = { 0x00, 0x00, 0x00 };
				if (band == BAND_ID_AM) {
					data[0] = 0x03;
					data[1] = (byte) ((freq >> 8) & 0xFF);
					data[2] = (byte) (freq & 0xFF);
				} else {
					data[1] = (byte) ((freq >> 8) & 0xFF);
					data[2] = (byte) (freq & 0xFF);
				}

				RadioStation.setRadioBand(mContext, band);
				RadioStation.setCurrentFreq(mContext, freq);
				mRadioMcuManager.setBand(data);
			}
				break;
			case RadioMessage.MSG_RESUME_AUIN:
				resumeAvin();
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_SCAN_PERV:
				if (null != mRadioMcuManager) {
					mRadioMcuManager.onSeekDown();
				}
				break;
			case RadioMessage.MSG_RADIO_TO_MCU_SCAN_NEXT:
				if (null != mRadioMcuManager) {
					mRadioMcuManager.onSeekUp();
				}
				break;
			case RadioMessage.MSG_SET_VOLUME:
				boolean isPowerKey = SystemProperties.getBoolean(PROPERTY_KEY_POWERKEY, false);
				if (!isPowerKey) {
					setVolumeMute(false);
				}
				break;
			case RadioMessage.MSG_INIT_RADIO:
				isSRadio = true;
				if (mbInit) {
					initSource();
				} else {
					init();
				}
				break;
			default:
				break;
			}
		}
	};

	private void initSource() {
		Log.i(TAG, "initSource");
		if (mbLostbAudioFocusForever) {
			SourceManager.acquireSource(mContext, RadioStation.getRadioBand(mContext) == RadioConstants.BAND_ID_FM
					? Constants.KEY_SRC_MODE_FM : Constants.KEY_SRC_MODE_AM);
			mAudioManager.requestAudioFocus(mAudioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			mAudioManager.registerMediaButtonEventReceiver(mbCN);
			mbLostbAudioFocusForever = false;
			mbOccupyAudioFocus = true;
			try {
				if (mMcuManager == null) {
					mMcuManager = (McuManager) mContext.getSystemService(Context.MCU_SERVICE);
				}
				mMcuManager.RPC_SetSource(MCU_SOURCE_RADIO, 0x00);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initAvin();
			initMcu();
		}
	}

	private OnAudioFocusChangeListener mAudioFocus = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int arg0) {
			Log.i(TAG, "onAudioFocusChange : " + arg0);
			mbLostbAudioFocusForever = false;
			switch (arg0) {
			case AudioManager.AUDIOFOCUS_LOSS:
				mPauseAudio = false;
				mbOccupyAudioFocus = false;
				closeRadio(false);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				mbOccupyAudioFocus = false;
				deinitAvin();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				mbOccupyAudioFocus = true;
				if (!mPauseAudio) {
					initAvin();
				}
				break;
			}
		}
	};

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(MCU_ACTION_MEDIA_PLAY_PAUSE);
		filter.addAction(MCU_ACTION_MEDIA_PLAY);
		filter.addAction(MCU_ACTION_MEDIA_PAUSE);
		filter.addAction(MCU_ACTION_MEDIA_PREVIOUS);
		filter.addAction(MCU_ACTION_MEDIA_NEXT);
		filter.addAction(ACTION_QB_POWERON);
		filter.addAction(MCU_ACTION_ACC_ON);
		filter.addAction(MCU_ACTION_ACC_OFF);
		filter.addAction(ACTION_QB_POWEROFF);
		filter.addAction(ACTION_QB_PREPOWEROFF);
		filter.addAction(ACTION_AVIN_REQUEST_NOTIFY);
		filter.addAction(ACTION_UPDATA_LANGUAGE);
		filter.addAction(BootService.ACTION_IFLY_VOICE_APP);
		if (null != mRadioReceiver && null != filter) {
			mContext.registerReceiver(mRadioReceiver, filter);
		}
	}

	private void initMcu() {

		int band = RadioStation.getRadioBand(mContext);
		int freq = RadioStation.getCurrentFreq(mContext);

		mMcuManager = (McuManager) mContext.getSystemService(Context.MCU_SERVICE);
		if (null != mMcuManager) {
			mRadioMcuManager = new RadioMcuManager(mMcuManager);
			if (null != mRadioMcuManager) {
				mRadioMcuManager.onRequestListener(mMcuListener);
			} else {
				Log.e(TAG, "Failed to get radio mcu management handle");
			}
		} else {
			Log.e(TAG, "Failed to get mcu management handle");
		}

		sLog.i(TAG, "initMcu - mCurrBand: " + band + " - mCurrBandFreq: " + freq);
		if (null != mRadioMcuManager && freq != 0) {
			mRadioMcuManager.sendBandFreqInfo(band, freq);
		}

		if (null != mRadioMcuManager) {
			mRadioMcuManager.onOpenRadioSource();
		}
	}

	private void initAvin() {

		if (mAvin == null) {
			mPauseAudio = false;
			if (!mbOccupyAudioFocus) {
				// 临时丢失焦点，也抢焦点
				mAudioManager.requestAudioFocus(mAudioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			}
			mHandler.removeCallbacks(rblResumeAvin);
			mAvin = new AVIN();

			mAvin.setDestination(InputSource.DEST_TYPE_FRONT);

			int retValueAudio = mAvin.setSource(InputSource.SOURCE_TYPE_AVIN, AVIN.PORT_NONE, AVIN.PORT3,
					AVIN.PRIORITY_IN_CBM_LEVEL_DEFAULT);
			if (InputSource.ERR_FAILED == retValueAudio) {
				deinitAvin();
			} else {
				setVolumeMute(true);
				retValueAudio = mAvin.play();
				SoundSourceInfoUtils.updateRadioPlayStatus(true);
				if (InputSource.ERR_CBM_NOT_ALLOWED == retValueAudio) {
					deinitAvin();
					setVolumeMute(true);
				} else {
					unMuteLater();
				}
			}
		} else {
			resumeAvin();
		}
	}

	private void deinitAvin() {
		if (null != mHandler && null != rblResumeAvin) {
			mHandler.removeCallbacks(rblResumeAvin);
		}

		if (mAvin != null) {
			mAvin.stop();
			mAvin.release();
			mAvin = null;
			SoundSourceInfoUtils.updateRadioPlayStatus(false);
		}
		if (mHandler.hasMessages(RadioMessage.MSG_SET_VOLUME)) {
			setVolumeMute(false);
		}
	}

	private void deinitMcu() {
		if (null != mRadioMcuManager && null != mMcuListener) {
			mRadioMcuManager.onCloseRadioSource();
			mRadioMcuManager.onReleaseListener(mMcuListener);
		}
	}

	private void unMuteLater() {
		mHandler.removeMessages(RadioMessage.MSG_SET_VOLUME);
		Message msg = new Message();
		msg.what = RadioMessage.MSG_SET_VOLUME;
		mHandler.sendMessageDelayed(msg, RadioMessage.SET_VOLUME_DELAY);
	}

	private void setVolumeMute(boolean mute) {
		Log.i(TAG, "setVolumeMute : " + mute);
		mHandler.removeMessages(RadioMessage.MSG_SET_VOLUME);
		if (mAudioManager != null) {
			mAudioManager.setYeconVolumeMute(AudioManager.STREAM_MUSIC, mute, 0, SRC_VOLUME_RADIO);
		}
	}

	private Runnable rblResumeAvin = new Runnable() {
		public void run() {
			if (mPauseAudio || !mbOccupyAudioFocus) {
				mPauseAudio = false;
				initAvin();
			}
		}
	};

	private void initBtn(ViewGroup root) {
		mRadioGroup = (RadioGroup) root.findViewById(R.id.radio_page_type_radiogroup);
		if (null != mRadioGroup) {
			mRadioGroup.setOnCheckedChangeListener(new RadioGroupCheck());
		}
	}

	private class RadioGroupCheck implements OnCheckedChangeListener {

		public void onCheckedChanged(RadioGroup arg0, int id) {
			switch (id) {
			case R.id.radio_search_btn:
				showFragment(mRadioSearchFragment);
				break;
			case R.id.radio_fm_band_btn:
				showFragment(mRadioFMFragment);
				break;
			case R.id.radio_am_band_btn:
				showFragment(mRadioAMFragment);
				break;
			case R.id.radio_select_btn:
				showFragment(mRadioFavoriteFragment);
				break;
			case R.id.radio_close_btn:
				closeRadio(true);
				break;
			default:
				break;
			}
		}
	}

	// 关闭收音机
	public void closeRadio(boolean closeActivity) {
		mbInit = false;
		deinitAvin();
		deinitMcu();
		if (null != mRadioMsg && null != mHandler) {
			mRadioMsg.unregisterMsgHandler(mHandler);
		}
		if (null != mRadioReceiver) {
			mContext.unregisterReceiver(mRadioReceiver);
		}
		mbLostbAudioFocusForever = true;
		mAudioManager.abandonAudioFocus(mAudioFocus);
		mAudioManager.unregisterMediaButtonEventReceiver(mbCN);
		SourceManager.unregisterSource(mContext, RadioStation.getRadioBand(mContext) == RadioConstants.BAND_ID_FM
				? Constants.KEY_SRC_MODE_FM : Constants.KEY_SRC_MODE_AM);
		try {
			if (mMcuManager == null) {
				mMcuManager = (McuManager) mContext.getSystemService(Context.MCU_SERVICE);
			}
			mMcuManager.RPC_SetSource(MCU_SOURCE_OFF, 0x00);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (closeActivity) {
			if (MediaActivity.mActivity != null && MediaActivity.mActivity.isRadioPage()) {
				MediaActivity.mActivity.finish();
			}
		}
	}

	private void showView(PageType pageType) {

		switch (pageType) {
		case RADIO_SEARCH_PAGE:
			((RadioButton) mRadioGroup.findViewById(R.id.radio_search_btn)).setChecked(true);
			break;
		case RADIO_FM_PAGE:
			((RadioButton) mRadioGroup.findViewById(R.id.radio_fm_band_btn)).setChecked(true);
			break;
		case RADIO_AM_PAGE:
			((RadioButton) mRadioGroup.findViewById(R.id.radio_am_band_btn)).setChecked(true);
			break;
		case RADIO_SELECT_PAGE:
			((RadioButton) mRadioGroup.findViewById(R.id.radio_select_btn)).setChecked(true);
			break;
		default:
			break;
		}
	}

	private void onPresetPre() {
		if (null != mContext) {
			final int band = RadioStation.getRadioBand(mContext);
			final int freq = RadioStation.getCurrentFreq(mContext);
			ArrayList<Integer> freqList = RadioStation.getAllBandFreqInfo(mContext, band);
			if (null != freqList && !freqList.isEmpty()) {

				Log.i(TAG, "onPresetPre : " + Arrays.toString(freqList.toArray()));
				int index = freqList.indexOf(freq);
				if (-1 == index) {
					freqList.add(freq);
				}

				Collections.sort(freqList);
				int size = freqList.size() - 1;
				index = freqList.indexOf(freq);

				if (index != -1) {

					index = --index < 0 ? size : index;
					sendFreqToMcu(band, freqList.get(index));
				}
			}
		}
	}

	private void onPresetNext() {
		if (null != mContext) {
			final int band = RadioStation.getRadioBand(mContext);
			final int freq = RadioStation.getCurrentFreq(mContext);

			ArrayList<Integer> freqList = RadioStation.getAllBandFreqInfo(mContext, band);
			if (null != freqList && !freqList.isEmpty()) {

				Log.i(TAG, "onPresetNext : " + Arrays.toString(freqList.toArray()));
				int index = freqList.indexOf(freq);
				if (-1 == index) {
					freqList.add(freq);
				}

				Collections.sort(freqList);
				int size = freqList.size() - 1;
				index = freqList.indexOf(freq);

				if (index != -1) {
					index = ++index > size ? 0 : index;
					sendFreqToMcu(band, freqList.get(index));
				}
			}
		}
	}

	private void startFreqScan() {
		Log.e(TAG, "startFreqScan");
		showView(PageType.RADIO_SEARCH_PAGE);
		if (null != mRadioMcuManager) {
			mRadioMcuManager.onAS();
		}
	}

	private void stopFreqScan() {
		Log.e(TAG, "stopFreqScan");
		if (null != mRadioMcuManager) {
			mRadioMcuManager.onAS();
		}
	}

	private void sendFreqToMcu(int band, int freq) {
		if (null != mContext) {
			RadioStation.setRadioBand(mContext, band);
			RadioStation.setCurrentFreq(mContext, freq);
			Log.i(TAG, "send freq to mcu info : " + band + " - " + freq);
			mRadioMcuManager.sendBandFreqInfo(band, freq);
		}
	}

	@Override
	public void onResume() {
		if (MediaActivity.mActivity.isRadioPage()) {
			mHandler.removeMessages(RadioMessage.MSG_INIT_RADIO);
			mHandler.sendEmptyMessageDelayed(RadioMessage.MSG_INIT_RADIO, 100);
		}
	}
}
