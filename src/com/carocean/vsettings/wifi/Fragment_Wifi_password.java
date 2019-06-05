package com.carocean.vsettings.wifi;

import com.carocean.R;
import com.carocean.vmedia.MediaActivity;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Fragment_Wifi_password extends DialogFragment implements OnClickListener {

	public Context mContext;
	FragmentManager mFragmentManager;
	private Fragment_OnCustomDialogListener customDialogListener;
	private TextView textView[] = new TextView[3];
	private EditText pswEdit;
	private CheckBox mpswDisplay;
	private String mStrSSID;

	public Fragment_Wifi_password() {

	}

	public Fragment_Wifi_password(String ssid, Fragment_OnCustomDialogListener customListener) {
		customDialogListener = customListener;
		mStrSSID = ssid;
	}

	// 定义dialog的回调事件
	public interface Fragment_OnCustomDialogListener {
		void back(String str);
	}

	private void initData() {
		mContext = getActivity();
		mFragmentManager = MediaActivity.mActivity.getFragmentManager();
	}

	private void initView(View rootView) {

		textView[0] = (TextView) rootView.findViewById(R.id.dlg_btn_wifi_ssid);
		textView[1] = (TextView) rootView.findViewById(R.id.dlg_btn_connect);
		textView[2] = (TextView) rootView.findViewById(R.id.dlg_btn_dismiss);

		textView[0].setText(mStrSSID);
		pswEdit = (EditText) rootView.findViewById(R.id.dlg_edit_password);
		// pswEdit.setFocusable(false);
		// pswEdit.setOnClickListener(this);
		mpswDisplay = (CheckBox) rootView.findViewById(R.id.password_check);
		mpswDisplay.setOnClickListener(this);
		for (int i = 0; i < textView.length; i++) {
			textView[i].setOnClickListener(this);
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Bitmap bk = BitmapFactory.decodeResource(getResources(), R.drawable.setting_dlg_shape_bg);
		Window window = getDialog().getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.x = 70;
		params.y = -20;
		window.setAttributes(params);
		int width = mContext.getResources().getDimensionPixelSize(R.dimen.setting_dlg_w3);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.setting_dlg_h3);
		window.setLayout(width, height);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomizeActivityTheme);
		initData();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mRootView = inflater.inflate(R.layout.setting_wifi_password_layout, container, false);
		initView(mRootView);
		return mRootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.dlg_edit_password:
			// Fragment_Keyboard_all dialog =
			// Function.onSet_keyboard_all(mFragmentManager);
			// dialog.setOnKeyboardAllListener(new OnKeyboardAllListener() {
			// @Override
			// public void back(int id, String str) {
			// // TODO Auto-generated method stub
			// pswEdit.setText(str);
			// setEditTextCursorLocation(pswEdit);
			// }
			// });
			break;

		case R.id.password_check:
			int selection = pswEdit.getSelectionStart(); // 保存光标位置
			if (mpswDisplay.isChecked()) {
				// 如果选中，显示密码
				pswEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				// 否则隐藏密码
				pswEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			pswEdit.setSelection(selection); // 重置光标位置
			break;
		case R.id.dlg_btn_connect:
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

	public void setEditTextCursorLocation(EditText editText) {
		CharSequence text = editText.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
	}

}
