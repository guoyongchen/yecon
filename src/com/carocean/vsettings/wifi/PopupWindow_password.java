package com.carocean.vsettings.wifi;

import com.carocean.R;

import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * @ClassName: PopupWindow_password
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.05.08
 **/
public class PopupWindow_password extends PopupWindow implements OnClickListener {

	private OnCustomDialogListener customDialogListener;
	int ID_ITEM[] = { R.id.dlg_btn_wifi_ssid, R.id.dlg_btn_connect, R.id.dlg_btn_dismiss, };
	private TextView mTextView[] = new TextView[ID_ITEM.length];
	private EditText pswEdit;
	private CheckBox mpswDisplay;
	private String mStrSSID;

	public void setOnListener(String ssid, OnCustomDialogListener customListener) {
		customDialogListener = customListener;
		mStrSSID = ssid;
		if (mTextView[0] != null)
			mTextView[0].setText(mStrSSID);
		if (pswEdit != null)
			pswEdit.setText("");
	}

	public interface OnCustomDialogListener {
		void back(String str);
	}

	public PopupWindow_password(Context context, View contentView) {
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

		pswEdit = (EditText) rootView.findViewById(R.id.dlg_edit_password);
		mpswDisplay = (CheckBox) rootView.findViewById(R.id.password_check);
		mpswDisplay.setOnClickListener(this);
		for (int i = 0; i < mTextView.length; i++) {
			mTextView[i] = (TextView) rootView.findViewById(ID_ITEM[i]);
			mTextView[i].setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.password_check:
			int selection = pswEdit.getSelectionStart();
			if (mpswDisplay.isChecked()) {
				pswEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				pswEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			pswEdit.setSelection(selection);
			break;
		case R.id.dlg_btn_connect:
			if (customDialogListener != null)
				customDialogListener.back(pswEdit.getText().toString());
			dismiss();
			break;
		case R.id.dlg_btn_dismiss:
			dismiss();
			break;
		default:
			break;
		}
	}

}
