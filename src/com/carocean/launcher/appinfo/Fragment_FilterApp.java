package com.carocean.launcher.appinfo;

import com.carocean.R;
import com.carocean.settings.fragment.DialogFragmentBase;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class Fragment_FilterApp extends DialogFragmentBase implements OnClickListener {

	private Button btallapp; // 所有应用程序
	private Button btsystemapp;// 系统程序
	private Button btthirdapp; // 第三方应用程序
	private Button btsdcardapp; // 安装在SDCard的应用程序
	private Button btinstallapk; // 返回

	private int filter = appinfoUtil.FILTER_ALL_APP;

	View mRootView;
	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.launcher_appinfo_layout, container, false);
		initView(mRootView);
		return mRootView;
	}

	private void initView(View rootView) {
		btinstallapk = (Button) rootView.findViewById(R.id.btninstallapk);
		btallapp = (Button) rootView.findViewById(R.id.btallapp);
		btsystemapp = (Button) rootView.findViewById(R.id.btsystemapp);
		btthirdapp = (Button) rootView.findViewById(R.id.btthirdapp);
		btsdcardapp = (Button) rootView.findViewById(R.id.btsdcardapp);

		btinstallapk.setOnClickListener(this);
		btallapp.setOnClickListener(this);
		btsystemapp.setOnClickListener(this);
		btthirdapp.setOnClickListener(this);
		btsdcardapp.setOnClickListener(this);
	}

	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NO_FRAME, 0);
		mContext = getActivity();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		System.out.println("" + view.getId());
		switch (view.getId()) {
		case R.id.btninstallapk:
			// appinfoUtil.getInstance().installAPK();
			dismiss();
			return;
		case R.id.btallapp:
			filter = appinfoUtil.FILTER_ALL_APP;
			break;
		case R.id.btsystemapp:
			filter = appinfoUtil.FILTER_SYSTEM_APP;
			break;
		case R.id.btthirdapp:
			filter = appinfoUtil.FILTER_THIRD_APP;
			break;
		case R.id.btsdcardapp:
			filter = appinfoUtil.FILTER_SDCARD_APP;
			break;
		}

		FragmentManager mFragmentManager = getFragmentManager();
		Fragment_appinfo fragment = new Fragment_appinfo(filter);
		String strTag = "set_appinfo";
		if (mFragmentManager.findFragmentByTag(strTag) == null)
			fragment.show(mFragmentManager, strTag);
	}

}
