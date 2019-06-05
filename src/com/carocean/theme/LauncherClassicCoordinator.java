package com.carocean.theme;

import static com.carocean.radio.constants.RadioConstants.BAND_ID_FM;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Callinfo;
import com.carocean.bt.data.FormatTelNumber;
import com.carocean.bt.BTService;
import com.carocean.coordinator.Coordinator;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.utils.Constants;
import com.carocean.utils.SoundSourceInfoUtils;
import com.carocean.utils.SourceInfoInterface;
import com.carocean.utils.SourceManager;

import android.content.Context;
import android.content.Intent;
import android.mcu.McuExternalConstant;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LauncherClassicCoordinator extends Coordinator implements SourceInfoInterface {
	private final String TAG = getClass().getSimpleName() + "_";
	private final LauncherClassic mLancherClassic;
	private Context mContext;
	private View mView;

	private FrameLayout phonelink_fl, allapp_fl, media_radio_fl, media_bt_fl, bt_media_fl, bt_radio_fl;
	private FrameLayout radio_media_fl, radio_bt_fl;
	LinearLayout media_layout_ll, radio_layout_ll, bt_layout_ll;
	private static ImageView radio_band_iv;
	private final int MSG_UPDATE_SRC = 0x01;

	private TextView bt_equipment_name, bt_contacts_name, bt_phone_type;
	private ImageView answer_iv, hang_up_iv;

	public LauncherClassicCoordinator(LauncherClassic arg) {
		this.mLancherClassic = arg;
		mContext = ApplicationManage.getInstance().getApplicationContext();
		BTService.registerNotifyHandler(uiHandler);
		SoundSourceInfoUtils.RegisterSourceInfo(this);
	}

	void initView(View view) {

		media_layout_ll = (LinearLayout) view.findViewById(R.id.media_layout_ll);
		radio_layout_ll = (LinearLayout) view.findViewById(R.id.radio_layout_ll);
		bt_layout_ll = (LinearLayout) view.findViewById(R.id.bt_layout_ll);

		phonelink_fl = (FrameLayout) view.findViewById(R.id.phonelink_fl);
		allapp_fl = (FrameLayout) view.findViewById(R.id.allapp_fl);
		media_radio_fl = (FrameLayout) view.findViewById(R.id.media_radio_fl);
		media_bt_fl = (FrameLayout) view.findViewById(R.id.media_bt_fl);

		radio_media_fl = (FrameLayout) view.findViewById(R.id.radio_media_fl);
		radio_bt_fl = (FrameLayout) view.findViewById(R.id.radio_bt_fl);

		bt_media_fl = (FrameLayout) view.findViewById(R.id.bt_media_fl);
		bt_radio_fl = (FrameLayout) view.findViewById(R.id.bt_radio_fl);

		radio_band_iv = (ImageView) view.findViewById(R.id.radio_band_iv);
		setFM_AM(BAND_ID_FM);

		bt_equipment_name = (TextView) view.findViewById(R.id.bt_equipment_name);
		bt_contacts_name = (TextView) view.findViewById(R.id.bt_contacts_name);
		bt_phone_type = (TextView) view.findViewById(R.id.bt_phone_type);
		answer_iv = (ImageView) view.findViewById(R.id.answer_iv);
		hang_up_iv = (ImageView) view.findViewById(R.id.hang_up_iv);

		phonelink_fl.setOnClickListener(onClickListener);
		allapp_fl.setOnClickListener(onClickListener);
		media_radio_fl.setOnClickListener(onClickListener);
		media_bt_fl.setOnClickListener(onClickListener);
		radio_media_fl.setOnClickListener(onClickListener);
		radio_bt_fl.setOnClickListener(onClickListener);
		bt_media_fl.setOnClickListener(onClickListener);
		bt_radio_fl.setOnClickListener(onClickListener);

		answer_iv.setOnClickListener(onClickListener);
		hang_up_iv.setOnClickListener(onClickListener);

		updateSourceInfo(SourceManager.getSource(), SourceManager.getUnRegisterSource());

	}

	@Override
	public void attach(View _view) {
		super.attach(_view);
		mView = _view;
		initView(mView);
	}

	@Override
	public void detach(View view) {
		// TODO Auto-generated method stub
		super.detach(view);

	}

	View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.phonelink_fl:
				if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
					launcherUtils.startScreenLink();
				}
				break;
			case R.id.allapp_fl:
				if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
					launcherUtils.startAllapp();
				}
				break;
			case R.id.media_radio_fl:
				launcherUtils.startRadio();
				break;
			case R.id.radio_media_fl:
				launcherUtils.startMedia();
				break;
			case R.id.media_bt_fl:
			case R.id.radio_bt_fl:
				launcherUtils.startBT();
				break;
			case R.id.bt_media_fl:
				break;
			case R.id.bt_radio_fl:
				break;
			case R.id.answer_iv:
				BTUtils.mBluetooth.answer();// 接听
				break;
			case R.id.hang_up_iv:
				BTUtils.mBluetooth.hangup();// 挂断
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void updateSourceInfo(int source, int iUnRegisterSource) {
		// TODO Auto-generated method stub
		Log.i(TAG, "source: " + source + "  iUnRegisterSource: " + iUnRegisterSource);
		mHandler.removeMessages(MSG_UPDATE_SRC);
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_SRC;
		message.arg1 = source;
		message.arg2 = iUnRegisterSource;
		mHandler.sendMessage(message);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SRC:
				switch (msg.arg2) {
				case Constants.KEY_SRC_MODE_FM:
				case Constants.KEY_SRC_MODE_AM:
					setRadioUI();
					break;
				case Constants.KEY_SRC_MODE_USB1:
				case Constants.KEY_SRC_MODE_USB2:
				case Constants.KEY_SRC_MODE_BT_MUSIC:
				case Constants.KEY_SRC_MODE_EXTERNAL:
					setMedioUI();
					break;
				default:
					break;
				}
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
				String action = intent.getAction();
				if (action.equals(BTService.ACTION_DISCONNECTED)) {
					String path = intent.getStringExtra(BTService.EXTRA_PATH);
					if (path == null || !path.equals("a2dp")) {
						Message message = mHandler.obtainMessage();
						message.what = MSG_UPDATE_SRC;
						message.arg2 = SourceManager.getUnRegisterSource();
						mHandler.sendMessage(message);
					}

				} else if (action.equals(BTService.ACTION_CALL_STATE)) {
					int status = intent.getIntExtra(BTService.EXTRA_STATE, 0);
					if (status == Callinfo.STATUS_TERMINATE) {
						Message message = mHandler.obtainMessage();
						message.what = MSG_UPDATE_SRC;
						message.arg2 = SourceManager.getUnRegisterSource();
						mHandler.sendMessage(message);
					}else if (status == Callinfo.STATUS_INCOMING) {
						setBtUI();
						setBtDetailUI(Callinfo.STATUS_INCOMING);
					}else if (status == Callinfo.STATUS_OUTGOING) {
						setBtUI();
						setBtDetailUI(Callinfo.STATUS_OUTGOING);
					}
				}
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.carocean.utils.SourceInfoInterface#updateRadioPlayStatus(boolean)
	 */
	@Override
	public void updateRadioPlayStatus(boolean bPlay) {
		// TODO Auto-generated method stub

	}

	public static void setFM_AM(int band) {
		radio_band_iv.setSelected(band == BAND_ID_FM ? true : false);
	}

	public void setRadioUI() {
		media_layout_ll.setVisibility(View.INVISIBLE);
		radio_layout_ll.setVisibility(View.VISIBLE);
		bt_layout_ll.setVisibility(View.INVISIBLE);
	}

	public void setMedioUI() {
		media_layout_ll.setVisibility(View.VISIBLE);
		radio_layout_ll.setVisibility(View.INVISIBLE);
		bt_layout_ll.setVisibility(View.INVISIBLE);
	}

	public void setBtUI() {
		media_layout_ll.setVisibility(View.INVISIBLE);
		radio_layout_ll.setVisibility(View.INVISIBLE);
		bt_layout_ll.setVisibility(View.VISIBLE);
	}

	public void setBtDetailUI(int status) {
		if (BTUtils.mBluetooth.isHFPconnected()) {
			if (bt_equipment_name != null) {
				bt_equipment_name.setText(BTUtils.mBluetooth.name_connected);
			}
		}
		if (bt_contacts_name != null) {
			if (BTUtils.mBluetooth.getcallname().isEmpty()) {
				bt_contacts_name.setText(FormatTelNumber.ui_format_tel_number(BTUtils.mBluetooth.getcallnum()));
			} else {
				bt_contacts_name.setText(BTUtils.mBluetooth.getcallname());
			}

		}
		if (bt_phone_type != null) {
			if (status == Callinfo.STATUS_INCOMING) {
				bt_phone_type.setText(
						ApplicationManage.getContext().getResources().getString(R.string.bt_income_call_status));
			} else if (status == Callinfo.STATUS_OUTGOING) {
				bt_phone_type
						.setText(ApplicationManage.getContext().getResources().getString(R.string.bt_out_call_status));
			}
		}

	}

}
