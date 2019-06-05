package com.carocean.vmedia.t19can;

import com.carocean.R;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.t19can.T19CanRx.LowPowerInfo;
import com.carocean.t19can.T19CanRx.TipsInfo;
import com.carocean.utils.DataShared;
import com.carocean.t19can.T19CanTx;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class T19NaviPopup implements OnTouchListener, OnClickListener {

	private String TAG = "T19NaviPopup";
	private Context mContext = null;
	private Handler mHandler = null;
	private PopWind mPopWind = null;
	private long mlshowAirTime = 0;
	private int showAirTime = 86400;
	private boolean mbAirAutoCloseFlag = false;

	protected LinearLayout mT19NaviPopup = null;
	private Button intelligent_charging_pile_navi_ok, intelligent_charging_pile_navi_no;

	public T19NaviPopup(LayoutInflater inflater, Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mPopWind = new PopWind((int) context.getResources().getDimension(R.dimen.can_popup_width),
				(int) context.getResources().getDimension(R.dimen.can_popup_height));
		mT19NaviPopup = (LinearLayout) inflater.inflate(R.layout.low_power_intelligent_charging_pile_dialog, null);
		intelligent_charging_pile_navi_ok = (Button) mT19NaviPopup.findViewById(R.id.intelligent_charging_pile_navi_ok);
		intelligent_charging_pile_navi_no = (Button) mT19NaviPopup.findViewById(R.id.intelligent_charging_pile_navi_no);
		intelligent_charging_pile_navi_ok.setOnClickListener(this);
		intelligent_charging_pile_navi_no.setOnClickListener(this);
	}

	Runnable AirAutoClose = new Runnable() {

		@Override
		public void run() {
			long lcurtime = System.currentTimeMillis();

			if ((lcurtime - mlshowAirTime) >= showAirTime * 1000) {

				Hide();
				mbAirAutoCloseFlag = false;
			}

			if (mbAirAutoCloseFlag) {
				mHandler.postDelayed(AirAutoClose, 500);
			}
		}
	};

	public void show(boolean bshow) {

		if (!IsShow() && bshow) {
			if (mPopWind != null) {
				mlshowAirTime = mPopWind.show(mContext, mT19NaviPopup);
				mHandler.post(AirAutoClose);
				mbAirAutoCloseFlag = true;
			}
		} else {
			if (IsShow()) {
				if (mHandler.hasCallbacks(AirAutoClose)) {
					mlshowAirTime = System.currentTimeMillis();
					mHandler.removeCallbacks(AirAutoClose);
					mHandler.post(AirAutoClose);
				}
			}
		}

	}

	public boolean IsShow() {
		return mPopWind.IsVisiable();
	}

	public void Hide() {
		if (mPopWind != null) {
			mPopWind.hide(mT19NaviPopup);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (IsShow()) {
			Hide();
			mbAirAutoCloseFlag = false;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.intelligent_charging_pile_navi_ok:
			gointo_navi_charging_pile(
					DataShared.getInstance(mContext).getString(SettingConstants.key_gps_longitude, null),
					DataShared.getInstance(mContext).getString(SettingConstants.key_gps_altitude, null));
			DataShared.getInstance(mContext).putBoolean(SettingConstants.key_show_navi_flag, true);
			if (IsShow()) {
				Hide();
				mbAirAutoCloseFlag = false;
			}
			break;

		case R.id.intelligent_charging_pile_navi_no:
			DataShared.getInstance(mContext).putBoolean(SettingConstants.key_show_navi_flag, true);
			if (IsShow()) {
				Hide();
				mbAirAutoCloseFlag = false;
			}
			break;

		default:
			break;
		}

	}

	public void gointo_navi_charging_pile(String lon, String lat) {
		double Longitude = 0;
		double Altitude = 0;
		if (!TextUtils.isEmpty(lon)) {
			Longitude = Double.parseDouble(lon);
		}
		if (!TextUtils.isEmpty(lat)) {
			Altitude = Double.parseDouble(lat);
		}

		Intent intent = new Intent();
		intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10037);
		intent.putExtra("KEYWORDS", mContext.getResources().getString(R.string.charging_pile_str));
		intent.putExtra("LAT", Altitude);
		intent.putExtra("LON", Longitude);
		intent.putExtra("DEV", 0);
		intent.putExtra("SOURCE_APP", "Third App");
		mContext.sendBroadcast(intent);
	}

}
