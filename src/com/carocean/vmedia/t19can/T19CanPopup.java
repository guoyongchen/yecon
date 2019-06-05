package com.carocean.vmedia.t19can;

import com.carocean.R;
import com.carocean.t19can.T19CanRx.TipsInfo;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.TextView;

public class T19CanPopup implements OnTouchListener, OnClickListener {

	private String TAG = "T19CanPopup";
	private Context mContext = null;
	private Handler mHandler = null;
	private PopWind mPopWind = null;
	private long mlshowAirTime = 0;
	private int showAirTime = 5;
	private boolean mbAirAutoCloseFlag = false;

	protected FrameLayout mT19CanPopupView = null;
	private TextView msg_tv;

	public T19CanPopup(LayoutInflater inflater, Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mPopWind = new PopWind((int) context.getResources().getDimension(R.dimen.can_popup_width),
				(int) context.getResources().getDimension(R.dimen.can_popup_height));
		mT19CanPopupView = (FrameLayout) inflater.inflate(R.layout.t19_can_popup, null);
		msg_tv = (TextView) mT19CanPopupView.findViewById(R.id.msg_tv);
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

	public void show(TipsInfo mTipsInfo, boolean bshow) {

		if (mTipsInfo != null) {

			switch (mTipsInfo.tipContent) {
			case 0:
				msg_tv.setText("");
				break;

			case 1:
				msg_tv.setText(mContext.getResources().getString(R.string.can_tip_content_1));
				break;

			case 2:
				msg_tv.setText(mContext.getResources().getString(R.string.can_tip_content_2));
				break;

			case 3:
				msg_tv.setText(mContext.getResources().getString(R.string.can_tip_content_3));
				break;

			case 4:
				msg_tv.setText(mContext.getResources().getString(R.string.can_tip_content_4));
				break;

			default:
				break;
			}
			this.showAirTime = mTipsInfo.tipShowTime;
		}

		if (!IsShow() && bshow) {
			if (mPopWind != null) {
				mlshowAirTime = mPopWind.show(mContext, mT19CanPopupView);
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
			mPopWind.hide(mT19CanPopupView);
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

		default:
			break;
		}

	}

}
