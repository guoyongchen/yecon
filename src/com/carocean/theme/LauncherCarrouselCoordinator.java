package com.carocean.theme;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.coordinator.Coordinator;
import com.carocean.launcher.carrousel.CarrouselLayout;
import com.carocean.launcher.carrousel.OnCarrouselItemClickListener;

import android.content.Context;
import android.view.View;

public class LauncherCarrouselCoordinator extends Coordinator {
	private final String TAG = getClass().getSimpleName() + "_";
	private final LauncherCarrousel mLancherCarrousel;
	private Context mContext;
	private View mView;

	public LauncherCarrouselCoordinator(LauncherCarrousel arg) {
		this.mLancherCarrousel = arg;
		mContext = ApplicationManage.getInstance().getApplicationContext();
	}

	void initView(View view) {

		final CarrouselLayout carrousel = (CarrouselLayout) view.findViewById(R.id.carrousel);
		carrousel.setR(420)// 设置R的大小
				.setAutoRotation(false)// 是否自动切换
				.setAutoRotationTime(1500);// 自动切换的时间 单位毫秒
		carrousel.setOnCarrouselItemClickListener(new OnCarrouselItemClickListener() {

			@Override
			public void onItemClick(View view, int position) {
				// TODO 自动生成的方法存根
				carrousel.setSelectItem(position);
			}
		});
	}

	@Override
	public void attach(View _view) {
		super.attach(_view);
		mView = _view;
		initView(mView);
	}

	@Override
	public void detach(View view) {
		// TODO Auto-generated method stub
		super.detach(view);
	}

}
