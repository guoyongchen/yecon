package com.carocean.media.video;

import com.carocean.R;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class VideoRearView extends Presentation {
	private static final String TAG = "VideoRearView";
	private SurfaceView mRearSurfaceView;

	public VideoRearView(Context outerContext, Display display) {
		super(outerContext, display);
	}

	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		Window win = getWindow();
		WindowManager.LayoutParams params = win.getAttributes();
		params.type = WindowManager.LayoutParams.TYPE_DISPLAY_OVERLAY;

		setContentView(R.layout.video_player_rear);

		mRearSurfaceView = (SurfaceView) findViewById(R.id.rearSurface);
		Log.e(TAG, "onCreate:" + mRearSurfaceView);
		if (mRearSurfaceView != null) {
			mRearSurfaceView.setBackgroundResource(android.R.color.transparent);
			mRearSurfaceView.setVisibility(View.VISIBLE);
		}
	}

	public SurfaceView getSurfaceView() {
		return mRearSurfaceView;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		Log.e(TAG, "finalize");
	}

}
