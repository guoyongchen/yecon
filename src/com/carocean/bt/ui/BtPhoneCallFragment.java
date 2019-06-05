package com.carocean.bt.ui;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.data.FormatTelNumber;
import com.carocean.utils.CmnUtil;
import com.carocean.vmedia.MediaActivity;

import android.app.Fragment;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class BtPhoneCallFragment extends Fragment implements OnClickListener {
	private static final String TAG = "BtPhoneCallFragment";

	private AutoAnswerTimeoutThread mAutoAnswerTimeoutThread = null;

	private View mView = null;
	
	public static boolean bforeground = false;

	private StringBuffer mPhoneStr = new StringBuffer();
	private boolean isneedshowtip = false;
	
	RadioButton rb_speaking_switch, rb_incomming_switch, rb_outgoing_switch, rb_speaking_switch_inkeypad;
	TextView tv_name, tv_num, tv_time;
	LinearLayout ly_speaking_btns, ly_incomming_btns, ly_outgoing_btns, ly_keypad;
	boolean bkeypadshow = false;

	private void checkAutoAnswerOption() {
		if (BTUtils.mBluetooth.isincoming()) {
			if (BTUtils.mBluetooth.isautoanswer()) {
				mAutoAnswerTimeoutThread = new AutoAnswerTimeoutThread();
				if (mAutoAnswerTimeoutThread != null) {
					mAutoAnswerTimeoutThread.start();
				}
			}
		}
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView");
		if (null == mView) {
			mView = inflater.inflate(R.layout.bt_phone_call_fragment, container, false);
		}
		init();
		return mView;
	}

	@SuppressWarnings("static-access")
	private void init() {
		if (null != mView) {
			((ImageButton) mView.findViewById(R.id.phone_call_key_1_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_2_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_3_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_4_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_5_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_6_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_7_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_8_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_9_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_star_hide)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_0_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_sharp_btn)).setOnClickListener(this);

			((RadioButton) mView.findViewById(R.id.bt_call_speaking_switch)).setOnClickListener(this);
			((RadioButton) mView.findViewById(R.id.bt_call_incomming_switch)).setOnClickListener(this);
			((RadioButton) mView.findViewById(R.id.bt_call_outgoing_switch)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_speaking_hangup)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_incomming_hangup)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_outgoing_hangup)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_incomming_answer)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_speaking_min_inkeypad)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_speaking_max)).setOnClickListener(this);
			((RadioButton) mView.findViewById(R.id.bt_call_speaking_switch_inkeypad)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.bt_call_speaking_hangup_inkeypad)).setOnClickListener(this);
			
			rb_speaking_switch = (RadioButton) mView.findViewById(R.id.bt_call_speaking_switch);
			rb_incomming_switch = (RadioButton) mView.findViewById(R.id.bt_call_incomming_switch);
			rb_outgoing_switch = (RadioButton) mView.findViewById(R.id.bt_call_outgoing_switch);
			rb_speaking_switch_inkeypad = (RadioButton) mView.findViewById(R.id.bt_call_speaking_switch_inkeypad);

			ly_speaking_btns = (LinearLayout) mView.findViewById(R.id.bt_call_speaking);
			ly_incomming_btns = (LinearLayout) mView.findViewById(R.id.bt_call_incomming);
			ly_outgoing_btns = (LinearLayout) mView.findViewById(R.id.bt_call_outgoing);
			ly_keypad = (LinearLayout) mView.findViewById(R.id.bt_call_pad);

			tv_name = (TextView) mView.findViewById(R.id.bt_phone_call_name);
			tv_num = (TextView) mView.findViewById(R.id.bt_phone_call_number);
			tv_time = (TextView) mView.findViewById(R.id.bt_phone_call_time);
			

			getActivity().setVolumeControlStream(AudioManager.STREAM_BLUETOOTH_SCO);
			checkAutoAnswerOption();
			BTService.registerNotifyHandler(uiHandler);
			flushui(false);
		}
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		bforeground = false;
	}
	@Override
	public void onResume() {
		Log.e(TAG, "onResume");
		if (mView == null) {
			return;
		}
		CmnUtil.cancelToast();
		super.onResume();
		bforeground = true;
	}

	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		if (null != uiHandler) {
			BTService.unregisterNotifyHandler(uiHandler);
		}

		if (null != mTimeHandler && null != mTiemRunnable) {
			mTimeHandler.removeCallbacks(mTiemRunnable);
		}

		stopAutoAnswerTimeoutThread();
		
		if (null != mPhoneStr) {
			mPhoneStr.delete(0, mPhoneStr.length());
		}

		super.onDestroy();
	}

	private class AutoAnswerTimeoutThread extends Thread {
		private boolean stoped = false;

		public void run() {
			autoAnswertimeout();
		}

		public void shutdown() {
			stoped = true;
		}

		private void autoAnswertimeout() {
			boolean timeout = false;
			int cnt = 0;
			Log.e(TAG, "autoAnswer timeout Thread is running");
			while (!stoped) {
				// wait for 2 seconds
				if (cnt >= 2000) {
					timeout = true;
					break;
				}

				try {
					Thread.sleep(100);
				} catch (Exception e) {
					Log.e(TAG, "Waiting for action was interrupted.");
				}
				cnt += 100;
			}

			if (timeout) {
				Log.e(TAG, "auto answer time out,answer the call");
				BTUtils.mBluetooth.answer();
			}
		}
	}

	private void stopAutoAnswerTimeoutThread() {
		if (mAutoAnswerTimeoutThread != null) {
			try {
				Log.e(TAG, "mConnectTimeout close.");
				mAutoAnswerTimeoutThread.shutdown();
				mAutoAnswerTimeoutThread.join();
				mAutoAnswerTimeoutThread = null;
			} catch (InterruptedException e) {
			}
		}
	}

	private Runnable mTiemRunnable = new Runnable() {
		public void run() {
			Message message = Message.obtain();
			message.what = 300;
			message.obj = 0;
			mTimeHandler.sendMessage(message);
			mTimeHandler.postDelayed(mTiemRunnable, 1000);
		}
	};

	private Handler mTimeHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 300) {
				if (BTUtils.mBluetooth.isspeaking()) {
					String time = millSeconds2readableTime(BTUtils.mBluetooth.getcalltimecount());
					tv_time.setText(time);
				}
			}
		}
	};

	private void startTimer() {
		if (null != mTimeHandler && null != mTiemRunnable) {
			if (mTimeHandler.hasCallbacks(mTiemRunnable)) {
				return;
			}
			mTimeHandler.postDelayed(mTiemRunnable, 0);
		}
	}

	void stopTimer() {
		if (null != mTimeHandler && null != mTiemRunnable) {
			if (mTimeHandler.hasCallbacks(mTiemRunnable)) {
				mTimeHandler.removeCallbacks(mTiemRunnable);
			}
		}
	}

	private Handler uiHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {

				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				
				if (action.equals(BTService.ACTION_CALL_STATE)) {
					flushui(bkeypadshow);
				}else if (action.equals(BTService.ACTION_AUDIO)) {
					if (isneedshowtip) {
						isneedshowtip = false;
						if (BTUtils.mBluetooth.audiopath.equals("car")) {
							CmnUtil.showToast(MediaActivity.mActivity, R.string.bt_audio_incar);
						}else{
							CmnUtil.showToast(MediaActivity.mActivity, R.string.bt_audio_inphone);
						}
					}
				}
			}
		}
	};

	public void flushui(boolean keypadshow){
		bkeypadshow = keypadshow;
 //       int isMute = AtcSettings.Audio.GetMicMute(0);
        rb_incomming_switch.setChecked(!BTUtils.mBluetooth.isaudioincar());
        rb_speaking_switch.setChecked(!BTUtils.mBluetooth.isaudioincar());
        rb_outgoing_switch.setChecked(!BTUtils.mBluetooth.isaudioincar());
        rb_speaking_switch_inkeypad.setChecked(!BTUtils.mBluetooth.isaudioincar());
        ly_keypad.setVisibility(bkeypadshow ? View.VISIBLE : View.GONE);
        if (bkeypadshow) {
			ly_speaking_btns.setVisibility(View.GONE);
			ly_incomming_btns.setVisibility(View.GONE);
			ly_outgoing_btns.setVisibility(View.GONE);
		}else{
			rb_speaking_switch.setEnabled(false);
			rb_incomming_switch.setEnabled(false);
			rb_outgoing_switch.setEnabled(false);
			rb_speaking_switch_inkeypad.setEnabled(false);
			if (BTUtils.mBluetooth.isspeaking()) {
				ly_speaking_btns.setVisibility(View.VISIBLE);
				ly_incomming_btns.setVisibility(View.GONE);
				ly_outgoing_btns.setVisibility(View.GONE);

				rb_speaking_switch.setEnabled(true);
				rb_incomming_switch.setEnabled(true);
				rb_outgoing_switch.setEnabled(true);
				rb_speaking_switch_inkeypad.setEnabled(true);
				
			}else if (BTUtils.mBluetooth.isincoming()) {
				ly_speaking_btns.setVisibility(View.GONE);
				ly_incomming_btns.setVisibility(View.VISIBLE);
				ly_outgoing_btns.setVisibility(View.GONE);
				tv_time.setText(R.string.bt_income_call_status);
			}else if (BTUtils.mBluetooth.isoutgoing()) {
				ly_speaking_btns.setVisibility(View.GONE);
				ly_incomming_btns.setVisibility(View.GONE);
				ly_outgoing_btns.setVisibility(View.VISIBLE);
				tv_time.setText(R.string.bt_out_call_status);
			}else if (BTUtils.mBluetooth.isonhold()) {
				ly_speaking_btns.setVisibility(View.GONE);
				ly_incomming_btns.setVisibility(View.VISIBLE);
				ly_outgoing_btns.setVisibility(View.GONE);
			}else{
				ly_speaking_btns.setVisibility(View.GONE);
				ly_incomming_btns.setVisibility(View.VISIBLE);
				ly_outgoing_btns.setVisibility(View.GONE);
			}
		}
        if (BTUtils.mBluetooth.isspeaking()) {
			startTimer();
		}else{
			stopTimer();
		}
        refreshnamenum();
	}

	private void refreshnamenum(){
		String strPhoneNumber = BTUtils.mBluetooth.getcallnum();
        if(strPhoneNumber == null)
            return ;
        if(strPhoneNumber.isEmpty()){
            return ;
        }
        String callnum_afterformat 	= FormatTelNumber.ui_format_tel_number(strPhoneNumber);
        String name = BTUtils.mBluetooth.getcallname();
        if (name == null || name.isEmpty()) {
        	tv_name.setText(R.string.bt_unknownname);
		}else{
        	tv_name.setText(name);
		}
        tv_num.setText(callnum_afterformat);
        
        if (mPhoneStr.length() != 0) {
        	tv_name.setText("");
        	tv_num.setText(mPhoneStr.toString());
		}
	}

	private String millSeconds2readableTime(int millseconds) {

		int hour = (millseconds / 60) / 60;
		int minute = (millseconds / 60) % 60;
		int second = millseconds % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);

	}

	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.phone_call_key_1_btn:
			mPhoneStr.append("1");
			BTUtils.mBluetooth.sendDTMFCode("1");
			break;
		case R.id.phone_call_key_2_btn:
			mPhoneStr.append("2");
			BTUtils.mBluetooth.sendDTMFCode("2");
			break;
		case R.id.phone_call_key_3_btn:
			mPhoneStr.append("3");
			BTUtils.mBluetooth.sendDTMFCode("3");
			break;
		case R.id.phone_call_key_4_btn:
			mPhoneStr.append("4");
			BTUtils.mBluetooth.sendDTMFCode("4");
			break;
		case R.id.phone_call_key_5_btn:
			mPhoneStr.append("5");
			BTUtils.mBluetooth.sendDTMFCode("5");
			break;
		case R.id.phone_call_key_6_btn:
			mPhoneStr.append("6");
			BTUtils.mBluetooth.sendDTMFCode("6");
			break;
		case R.id.phone_call_key_7_btn:
			mPhoneStr.append("7");
			BTUtils.mBluetooth.sendDTMFCode("7");
			break;
		case R.id.phone_call_key_8_btn:
			mPhoneStr.append("8");
			BTUtils.mBluetooth.sendDTMFCode("8");
			break;
		case R.id.phone_call_key_9_btn:
			mPhoneStr.append("9");
			BTUtils.mBluetooth.sendDTMFCode("9");
			break;
		case R.id.phone_call_key_star_hide:
			mPhoneStr.append("*");
			BTUtils.mBluetooth.sendDTMFCode("*");
			break;
		case R.id.phone_call_key_0_btn:
			mPhoneStr.append("0");
			BTUtils.mBluetooth.sendDTMFCode("0");
			break;
		case R.id.phone_call_key_sharp_btn:
			mPhoneStr.append("#");
			BTUtils.mBluetooth.sendDTMFCode("#");
			break;
		case R.id.bt_call_speaking_switch:
		case R.id.bt_call_incomming_switch:
		case R.id.bt_call_outgoing_switch:
		case R.id.bt_call_speaking_switch_inkeypad:
			isneedshowtip = true;
			BTUtils.mBluetooth.switchaudio();
			break;
		case R.id.bt_call_speaking_hangup:
		case R.id.bt_call_incomming_hangup:
		case R.id.bt_call_outgoing_hangup:
		case R.id.bt_call_speaking_hangup_inkeypad:
			BTUtils.mBluetooth.hangup();
			break;
		case R.id.bt_call_incomming_answer:
			BTUtils.mBluetooth.answer();
			break;
		case R.id.bt_call_speaking_min_inkeypad:
			flushui(false);
			break;
		case R.id.bt_call_speaking_max:
			flushui(true);
			break;
		default:
			break;
		}
		refreshnamenum();
	}
}
