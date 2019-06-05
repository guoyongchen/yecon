package com.carocean.floatwindow;

import static com.carocean.radio.constants.RadioConstants.BAND_ID_FM;

import com.carocean.R;
import com.carocean.radio.constants.RadioMessage;
import com.carocean.radio.db.RadioStation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class RadioView implements OnClickListener {

	private TextView mFreq, mBand;
	private Context mContext;
	private View mView;

	public RadioView(Context context, View view) {
		mContext = context;
		mView = view;
		
		init();
	}
	
	private void init() {
		if (null != mView) {
			mFreq = (TextView) mView.findViewById(R.id.float_radio_freq);
			mBand = (TextView) mView.findViewById(R.id.float_radio_band);
			
			((ImageButton) mView.findViewById(R.id.float_radio_btn_pre)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.float_radio_btn_next)).setOnClickListener(this);
			
			setRadioBand();
			setRadioFreq(RadioStation.getCurrentFreq(mContext));
		}
	}

	public void onClick(View v) {

		final int id = v.getId();
		if (id == R.id.float_radio_btn_pre) {
			sendMessage(RadioMessage.MSG_RADIO_TO_MCU_PERV_FREQ);
		} else if (id == R.id.float_radio_btn_next) {
			sendMessage(RadioMessage.MSG_RADIO_TO_MCU_NEXT_FREQ);
		}
	}
	
	private void sendMessage(int msgId) {
		RadioMessage msg = RadioMessage.getInstance();
		if (null != msg) {
			msg.sendMsg(msgId, null);
		}
	}

	private void initHandler() {
		FloatWindowMessage msg = FloatWindowMessage.getInstance();
		if (null != msg) {
			msg.registerMsgHandler(mRadioHandler);
		}
	}

	private void uintHandler() {
		FloatWindowMessage msg = FloatWindowMessage.getInstance();
		if (null != msg) {
			msg.unregisterMsgHandler(mRadioHandler);
		}
	}

	public void isShow(final int type) {
		
		if (null != mView) {
			mView.setVisibility(type);
			
			if (mView.isShown()) {
				initHandler();
			} else {
				uintHandler();
			}
		}
	}

	private Handler mRadioHandler = new Handler() {

		public void handleMessage(Message msg) {

			if (FloatWindowMessage.FLOAT_RADIO_MSG_FREQ == msg.what) {
				setRadioFreq((Integer) msg.obj);
			} else if (FloatWindowMessage.FLOAT_RADIO_MSG_BAND == msg.what) {
				setRadioBand();
			}

			super.handleMessage(msg);
		}
	};

	private void setRadioBand() {
		final int band = RadioStation.getRadioBand(mContext);
		if (null != mBand) {
			mBand.setText(band == BAND_ID_FM ? "MHz" : "KHz");
		}
	}

	private void setRadioFreq(int freq) {
		final int band = RadioStation.getRadioBand(mContext);
		String freqStr = band == BAND_ID_FM ? String.format("%.02f", freq / 100.0f) : String.format("%d", freq);
		if (null != mFreq) {
			mFreq.setText(freqStr);
		}
	}
}
