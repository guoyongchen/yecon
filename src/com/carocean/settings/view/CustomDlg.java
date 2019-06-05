package com.carocean.settings.view;

import com.carocean.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class CustomDlg {

	public static void setDialogAttributes(Dialog dlg, View view, int w, int h) {
		Window window = dlg.getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		dlg.setContentView(view);
		WindowManager.LayoutParams lp = window.getAttributes();
		window.setGravity(Gravity.CENTER);
		lp.width = w;
		lp.height = h;
		lp.alpha = 1.0f;
		window.setAttributes(lp);
	}

	public static Dialog buildRadioGroupDlg(Context context, Dialog dialog, final int id, final int index, String title, String[] strArray) {

		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
			return dialog;
		}

		int total = strArray.length;
		int width = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_w2);
		int height = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_h2);
		int layoutid = R.layout.setting_dlg_radiogroup_layout;

		final int[] ID_ITEM = { R.id.rb_0, R.id.rb_1, R.id.rb_2, R.id.rb_3, R.id.rb_4, R.id.rb_5, R.id.rb_6, R.id.rb_7, R.id.rb_8, R.id.rb_9,
				R.id.rb_10, };

		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(layoutid, null);

		dialog = new Dialog(context, R.style.base_dialog);
		setDialogAttributes(dialog, layout, width, height);
		TextView textView = (TextView) layout.findViewById(R.id.dlg_title);
		textView.setText(title);
		RadioGroup mRadioGroud = (RadioGroup) layout.findViewById(R.id.radiogroup);

		final RadioButton[] radioButtons = new RadioButton[ID_ITEM.length];

		for (int i = 0; i < ID_ITEM.length; i++) {
			radioButtons[i] = (RadioButton) layout.findViewById(ID_ITEM[i]);
			if (i < total)
				radioButtons[i].setText(strArray[i]);
			else
				radioButtons[i].setVisibility(View.GONE);
		}
		if (total == 11)
			radioButtons[0].setVisibility(View.GONE);
		if (index < ID_ITEM.length)
			mRadioGroud.check(ID_ITEM[index]);
		final ScrollView scrollView = (ScrollView) layout.findViewById(R.id.ScrollView1);

		scrollView.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				scrollView.scrollTo(0, (radioButtons[1].getHeight() + 25) * (index - 1));
			}
		});

		final Dialog finalDlg = dialog;
		dialog.show();
		mRadioGroud.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.playSoundEffect(android.view.SoundEffectConstants.CLICK);
				for (int i = 0; i < ID_ITEM.length; i++) {
					if (arg1 == ID_ITEM[i]) {
						if (mCheckedChangedListener != null)
							mCheckedChangedListener.onRadioGroupListener(id, i);
						finalDlg.dismiss();
						break;
					}
				}
			}
		});

		((TextView) layout.findViewById(R.id.dlg_back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finalDlg.dismiss();
			}
		});
		return dialog;
	}

	public static onRadioGroupChangedListener mCheckedChangedListener;

	public interface onRadioGroupChangedListener {
		public void onRadioGroupListener(int id, int index);
	}

	public static void setRadioGroupChangedListener(onRadioGroupChangedListener lisenter) {
		mCheckedChangedListener = lisenter;
	}

	public static Dialog buildDlg(Context context, Dialog dialog, int type, final int id, String title, String content) {

		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
			return dialog;
		}

		int width = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_w);
		int height = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_h);
		int layoutid = R.layout.setting_dlg_prompt_layout;
		if (type == 2) {
			layoutid = R.layout.setting_dlg_prompt_layout2;
			width = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_w2);
			height = context.getResources().getDimensionPixelSize(R.dimen.setting_dlg_h2);
		}

		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(layoutid, null);

		dialog = new Dialog(context, R.style.base_dialog);
		setDialogAttributes(dialog, layout, width, height);
		TextView textView1 = (TextView) layout.findViewById(R.id.dlg_title);
		textView1.setText(title);
		TextView textView2 = (TextView) layout.findViewById(R.id.dlg_content);
		textView2.setText(content);

		final Dialog finalDlg = dialog;
		dialog.show();

		((TextView) layout.findViewById(R.id.dlg_back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finalDlg.dismiss();
			}
		});

		((TextView) layout.findViewById(R.id.dlg_ok)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mOnDlgClickListener != null)
					mOnDlgClickListener.onDlgConfirm(id);
				finalDlg.dismiss();
			}
		});

		((TextView) layout.findViewById(R.id.dlg_cancle)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mOnDlgClickListener != null)
					mOnDlgClickListener.onDlgCancel(id);
				finalDlg.dismiss();
			}
		});
		return dialog;
	}

	private static OnDlgClickListener mOnDlgClickListener;

	public static void setOnDlgClickListener(OnDlgClickListener mListener) {
		mOnDlgClickListener = mListener;
	}

	// 定义dialog的回调事件
	public interface OnDlgClickListener {
		void onDlgConfirm(int id);

		void onDlgCancel(int id);
	}

}
