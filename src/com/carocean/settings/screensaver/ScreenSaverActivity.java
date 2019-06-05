package com.carocean.settings.screensaver;

import com.carocean.R;
import com.carocean.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class ScreenSaverActivity extends Activity implements OnClickListener {

	Integer ID_View[] = { R.id.dlg_back, };
	View[] mView = new View[ID_View.length];

	void initParams() {
		// Utils.showSystemUI(this, false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
		getWindow().getDecorView().setBackgroundResource(scUtils.imageIds[scUtils.imageSelected]);
		Utils.setStatusBarVisible(this, false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initParams();
		setContentView(R.layout.activity_screensaver);

		for (int i = 0; i < ID_View.length; i++) {
			mView[i] = findViewById(ID_View[i]);
			mView[i].setOnClickListener(this);
		}
		if (null != ScreenSaverService.getInstance()) {
			ScreenSaverService.getInstance().mScreensaverImpBind.attachActivity(this);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		default:
			break;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (null != ScreenSaverService.getInstance()) {
			ScreenSaverService.getInstance().mScreensaverImpBind.resetTime();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}

}
