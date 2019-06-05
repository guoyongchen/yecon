package com.carocean.settings.fragment;

import android.app.DialogFragment;
import android.view.ViewGroup;
import android.view.Window;

public class DialogFragmentBase extends DialogFragment {

	@Override
	public void onStart() {
		// TODO Auto-generated method stub

		Window window = getDialog().getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		// Window window = getDialog().getWindow();
		// WindowManager.LayoutParams lp = window.getAttributes();
		// lp.gravity = Gravity.TOP;
		// lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		// lp.height =
		// getResources().getDimensionPixelSize(R.dimen.parent_height);
		// window.setAttributes(lp);

		super.onStart();
	}
}
