package com.carocean.launcher.customView;

import static com.carocean.radio.constants.RadioConstants.BAND_ID_FM;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.floatwindow.FloatWindowMessage;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.radio.RadioSearchFragment;
import com.carocean.radio.db.RadioStation;
import com.carocean.theme.LauncherClassicCoordinator;
import com.carocean.utils.Constants;
import com.carocean.utils.SoundSourceInfoUtils;
import com.carocean.utils.SourceInfoInterface;
import com.carocean.utils.SourceManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.mcu.McuExternalConstant;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RadioWidget extends LinearLayout implements OnClickListener, OnLongClickListener, SourceInfoInterface{

	final static String TAG = "RadioWidget";

	private TextView mFreq, mBand;
	private ImageView radio_prev_btn, radio_play_btn, radio_pause_btn, radio_next_btn;
	private LinearLayout mLayoutCtrl = null;
	public final static int MSG_UPGRADE_UI = 1000;
	public final static int MSG_UPDATE_SOURCE = 1001;

	void initView(Context context) {
		mContext = context;

		LayoutInflater.from(context).inflate(R.layout.launcher_widget_radio_layout, this);
		View view = findViewById(R.id.radio_widget_layout);
		mBand = (TextView) view.findViewById(R.id.radio_title);
		mFreq = (TextView) view.findViewById(R.id.radio_text_artist);
		mLayoutCtrl = (LinearLayout) view.findViewById(R.id.radio_layout_ctrl);

		radio_prev_btn = (ImageView) view.findViewById(R.id.radio_prev_btn);
		radio_play_btn = (ImageView) view.findViewById(R.id.radio_play_btn);
		radio_pause_btn = (ImageView) view.findViewById(R.id.radio_pause_btn);
		radio_next_btn = (ImageView) view.findViewById(R.id.radio_next_btn);

		radio_prev_btn.setOnClickListener(this);
//		radio_prev_btn.setOnLongClickListener(this);
		radio_play_btn.setOnClickListener(this);
		radio_pause_btn.setOnClickListener(this);
		radio_next_btn.setOnClickListener(this);
//		radio_next_btn.setOnLongClickListener(this);

		if (null != view) {
			view.setOnClickListener(this);
		}

		SoundSourceInfoUtils.RegisterSourceInfo(this);
		initHandler();

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

	public RadioWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaWidget);
		ta.recycle();

		initView(context);
	}

	public RadioWidget(Context context) {
		super(context);

		initView(context);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() != R.id.radio_widget_layout && SourceManager.getSource() != Constants.KEY_SRC_MODE_FM && 
				SourceManager.getSource() != Constants.KEY_SRC_MODE_AM) {
			return;
		}
		switch (view.getId()) {
		case R.id.radio_widget_layout:
			launcherUtils.startRadio();
			break;

		case R.id.radio_prev_btn:
			RadioSearchFragment.prevScan();
			break;

		case R.id.radio_play_btn:
			ApplicationManage.getContext().sendBroadcast(new Intent(McuExternalConstant.MCU_ACTION_MEDIA_PLAY));
			break;

		case R.id.radio_pause_btn:
			ApplicationManage.getContext().sendBroadcast(new Intent(McuExternalConstant.MCU_ACTION_MEDIA_PAUSE));
			break;

		case R.id.radio_next_btn:
			RadioSearchFragment.nextScan();
			break;
		default:
			break;
		}
	}

	private Handler mRadioHandler = new Handler() {

		public void handleMessage(Message msg) {

			if (FloatWindowMessage.FLOAT_RADIO_MSG_FREQ == msg.what) {
				setRadioFreq((Integer) msg.obj);
			} else if (FloatWindowMessage.FLOAT_RADIO_MSG_BAND == msg.what) {
				setRadioBand();
			} else if (MSG_UPGRADE_UI == msg.what) {
				int bPlay = msg.arg1;
				if (bPlay == 1) {
					radio_play_btn.setVisibility(View.GONE);
					radio_pause_btn.setVisibility(View.VISIBLE);
				} else {
					radio_play_btn.setVisibility(View.VISIBLE);
					radio_pause_btn.setVisibility(View.GONE);
				}
			} else if (MSG_UPDATE_SOURCE == msg.what) {
				if (msg.arg1 == 0) {
					mLayoutCtrl.setVisibility(View.INVISIBLE);
					if (null != mBand) {
						mBand.setText(mContext.getResources().getString(R.string.general_radio));
					}
					if (null != mFreq) {
						mFreq.setText("");
					}
				} else if (msg.arg1 == Constants.KEY_SRC_MODE_FM || msg.arg1 == Constants.KEY_SRC_MODE_AM) {
					mLayoutCtrl.setVisibility(View.VISIBLE);
					setRadioBand();
				} else {
					mLayoutCtrl.setVisibility(View.INVISIBLE);
				}
			}

			super.handleMessage(msg);
		}
	};

	private void setRadioBand() {
		final int band = RadioStation.getRadioBand(mContext);
		if (null != mBand) {
			mBand.setText(band == BAND_ID_FM ? "FM" : "AM");
			LauncherClassicCoordinator.setFM_AM(band);
		}
	}

	private void setRadioFreq(int freq) {
		final int band = RadioStation.getRadioBand(mContext);
		String freqStr = band == BAND_ID_FM ? String.format("%.1f", freq / 100.0f) + "MHz"
				: String.format("%d", freq) + "KHz";
		if (null != mFreq) {
			mFreq.setText(freqStr);
		}
	}

	@Override
	public void updateSourceInfo(int source, int iUnRegisterSource) {
		// TODO Auto-generated method stub
		Log.i(TAG, "source: " + source + "  iUnRegisterSource: " + iUnRegisterSource);
		mRadioHandler.removeMessages(MSG_UPDATE_SOURCE);
		Message msg = mRadioHandler.obtainMessage();
		msg.what = MSG_UPDATE_SOURCE;
		msg.arg1 = source;
		mRadioHandler.sendMessage(msg);
	}

	@Override
	public void updateRadioPlayStatus(boolean bPlay) {
		// TODO Auto-generated method stub
		Message msg = mRadioHandler.obtainMessage();
		msg.what = MSG_UPGRADE_UI;
		if (bPlay) {
			msg.arg1 = 1;
		} else {
			msg.arg1 = 0;
		}
		mRadioHandler.sendMessage(msg);
	}

	@Override
	public boolean onLongClick(View v) {
		if (SourceManager.getSource() == Constants.KEY_SRC_MODE_FM && 
				SourceManager.getSource()== Constants.KEY_SRC_MODE_AM) {
			switch (v.getId()) {
			case R.id.radio_prev_btn:
				RadioSearchFragment.prevPreset();
				return true;
				
			case R.id.radio_next_btn:
				RadioSearchFragment.nextPreset();
				return true;
				
			default:
				break;
			}
		}
		return false;
	}
}
