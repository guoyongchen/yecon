package com.carocean.backcar;

import com.carocean.service.BootService;
import com.carocean.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Slog;
import android.view.View;

public class BackCarActivity extends Activity {

	private static final String TAG = "BackCarActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Slog.i(TAG, "onCreate - start");

		View view = new View(this);

		setContentView(view);
	}

	@Override
	protected void onResume() {
		Slog.i(TAG, "onResume - start");

		if (BootService.getInstance() != null) {
			BootService.getInstance().setBackCarActivity(this);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Slog.i(TAG, "onDestroy - start");
	}

}
