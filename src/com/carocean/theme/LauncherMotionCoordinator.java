package com.carocean.theme;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.coordinator.Coordinator;
import com.carocean.launcher.utils.launcherUtils;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class LauncherMotionCoordinator extends Coordinator {
	private final LauncherMotion mLancherMotion;
	private Context mContext;
	private View mView;

	private LinearLayout ll_navi;
	private LinearLayout ll_radio;
	private LinearLayout ll_bt;
	private LinearLayout ll_phone_link;
	private LinearLayout ll_media;
	private LinearLayout ll_settings;

	public LauncherMotionCoordinator(LauncherMotion arg) {
		this.mLancherMotion = arg;
		mContext = ApplicationManage.getInstance().getApplicationContext();
	}

	void initView(View _view) {
		mView = _view;
		ll_navi = (LinearLayout) mView.findViewById(R.id.ll_navi);
		ll_radio = (LinearLayout) mView.findViewById(R.id.ll_radio);
		ll_bt = (LinearLayout) mView.findViewById(R.id.ll_bt);
		ll_phone_link = (LinearLayout) mView.findViewById(R.id.ll_phone_link);
		ll_media = (LinearLayout) mView.findViewById(R.id.ll_media);
		ll_settings = (LinearLayout) mView.findViewById(R.id.ll_settings);

		ll_navi.setOnClickListener(onClickListener);
		ll_radio.setOnClickListener(onClickListener);
		ll_bt.setOnClickListener(onClickListener);
		ll_phone_link.setOnClickListener(onClickListener);
		ll_media.setOnClickListener(onClickListener);
		ll_settings.setOnClickListener(onClickListener);
	}

	@Override
	public void attach(View _view) {
		super.attach(_view);
		initView(_view);
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
			case R.id.ll_navi:
				launcherUtils.startNavi();
				break;
			case R.id.ll_phone_link:
				launcherUtils.startScreenLink();
				break;
			case R.id.ll_radio:
				launcherUtils.startRadio();
				break;
			case R.id.ll_media:
				launcherUtils.startMedia();
				break;
			case R.id.ll_bt:
				launcherUtils.startBT();
				break;
			case R.id.ll_settings:
				launcherUtils.startAllapp();
				break;
			default:
				break;
			}
		}
	};

}
