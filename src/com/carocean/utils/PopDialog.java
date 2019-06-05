package com.carocean.utils;

import com.carocean.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class PopDialog extends DialogFragment {

	private String mStrTitle = "";
	private View mView = null;
	private TextView mTextView = null;

	private OnClickListener mClickListener = null;
	private PopType mPopType = PopType.DELETE;
	
	public void setPopType(PopType popType, String string) {
		mPopType = popType;
		mStrTitle = string;
		if (mTextView != null) {
			mTextView.setText(mStrTitle);
		}
	}
	
	public PopType getPopType() {
		return mPopType;
	}
	public PopDialog(String string) {
		mStrTitle = string;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.popdialogtheme);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.pop_dialog, container, false);
		mTextView = (TextView) mView.findViewById(R.id.pop_text_title);
		mTextView.setText(mStrTitle);
		initUI();
		return mView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	public void setListener(OnClickListener listener) {
		mClickListener = listener;
		initUI();
	}

	private void initUI() {
		if (mView != null && mClickListener != null) {
			mView.findViewById(R.id.pop_btn_yes).setOnClickListener(mClickListener);
			mView.findViewById(R.id.pop_btn_no).setOnClickListener(mClickListener);
		}
	}
	
	public static enum PopType {
		DOWNLOAD, DELETE;
	}
}
