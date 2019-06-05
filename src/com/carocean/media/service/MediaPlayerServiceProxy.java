package com.carocean.media.service;


import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MediaPlayerServiceProxy extends Service {

	private final String TAG = getClass().getName();
	// 多媒体服务
	private MediaPlayerService mMediaPlayerService;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "++onCreate++ ");
		try {
			if (mMediaPlayerService == null) {
				mMediaPlayerService = new MediaPlayerService(getApplicationContext());
				mMediaPlayerService.Initialize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e(TAG, "--onCreate--");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMediaPlayerService;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        Notification notification = mBuilder.build();
		startForeground(1, notification);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "++onDestroy++");
		if (mMediaPlayerService != null) {
			mMediaPlayerService.UnInitialize();
			mMediaPlayerService = null;
		}
		Log.e(TAG, "--onDestroy--");
		stopForeground(true);
		super.onDestroy();
	}
}
