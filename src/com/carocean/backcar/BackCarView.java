
package com.carocean.backcar;

import com.autochips.backcar.BackCar;
import com.carocean.utils.sLog;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BackCarView extends SurfaceView {

	private String TAG = "BackCarView";

	public static SurfaceHolder mSurfaceHolder = null;

	@SuppressWarnings("deprecation")
	private void init() {
		sLog.i(TAG, "BackCarView - BackCarView object init");

		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public BackCarView(Context context) {
		super(context);

		init();
	}

	public BackCarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public BackCarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			sLog.i(TAG, "mSHCallback - surfaceChanged - w: " + w + " - h: " + h);

			Surface surface = holder.getSurface();
			if (null != surface) {
				BackCar.setVideoSurface(surface);
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			sLog.i(TAG, "mSHCallback - surfaceCreated");

			BackCar.setVideoSurface(null);
			Surface surface = holder.getSurface();
			mSurfaceHolder = holder;
			if (null != surface) {
				BackCar.setVideoSurface(surface);
				BackCarSetMirror();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.w(TAG, "mSHCallback - surfaceDestroyed");
			BackCar.setVideoSurface(null);
			mSurfaceHolder = null;
		}
	};

	public void BackCarSetMirror() {
		int mirrorIndex = 0;
		mirrorIndex = Integer.parseInt(SystemProperties.get(BackCarConstants.PERSYS_BACKCAR_MIRROR, mirrorIndex + ""));
		BackCar.SetMirror(mirrorIndex);
	}
}
