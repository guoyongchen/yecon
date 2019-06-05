package com.carocean.screenlink;


import com.carocean.R;
import com.carocean.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ScreenLinkActivity extends Activity implements OnClickListener {

	Integer ID_View[] = { R.id.link_carplay, R.id.link_carlife };
	View[] mView = new View[ID_View.length];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenlink);

		for (int i = 0; i < ID_View.length; i++) {
			mView[i] = findViewById(ID_View[i]);
			mView[i].setOnClickListener(this);
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
		case R.id.link_carplay:
			Utils.RunApp("com.zjinnova.zlink", "com.zjinnova.zlink.main.view.SplashActivity");
			break;
		case R.id.link_carlife:
			try {
				Utils.RunApp("net.easyconn", "net.easyconn.WelcomeActivity");
			} catch (Exception e) {
				Log.v("", e.toString());
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
