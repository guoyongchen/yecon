package com.carocean.vsettings.wifi;

import com.carocean.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * @ClassName: PopupWindow_connect
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.05.08
 **/
public class PopupWindow_connect extends PopupWindow implements OnClickListener {

	private OnConnectActionListener mActionListener;
	int ID_ITEM[] = { R.id.wifi_connected_ssid, R.id.wifi_connected_enctype, R.id.dlg_btn_connect,
			R.id.dlg_btn_forget };
	private TextView mTextView[] = new TextView[ID_ITEM.length];
	private String mStrSSID, mStrENCType;

	public interface OnConnectActionListener {
		void connectAction(String mAction);
	}

	public void setOnConnectListener(String ssid, String enctype, OnConnectActionListener actionListener) {
		mActionListener = actionListener;
		mStrSSID = ssid;
		mStrENCType = enctype;

		if (mTextView[0] != null)
			mTextView[0].setText(mStrSSID);
		if (mTextView[1] != null)
			mTextView[1].setText(mStrENCType);
	}

	public PopupWindow_connect(Context context, View contentView) {
		// TODO Auto-generated constructor stub
		setContentView(contentView);
		init(context);
		initView(contentView);
	}

	private void init(Context context) {
		int width = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_w3);
		int height = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_h3);
		setWidth(width);
		setHeight(height);
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setAnimationStyle(R.style.popAnimationFade);
		setBackgroundDrawable(context.getResources().getDrawable(R.drawable.setting_dlg_shape_bg));
	}

	private void initView(View rootView) {

		for (int i = 0; i < mTextView.length; i++) {
			mTextView[i] = (TextView) rootView.findViewById(ID_ITEM[i]);
			mTextView[i].setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.dlg_btn_connect:
			if (mActionListener != null)
				mActionListener.connectAction("connect");
			dismiss();
			break;

		case R.id.dlg_btn_forget:
			if (mActionListener != null)
				mActionListener.connectAction("forget");
			dismiss();
			break;

		default:
			break;
		}
	}

}
