package com.carocean.bt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class BtMediaButtonReceiver extends BroadcastReceiver {
	static long pretime = 0;
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
    	if (SystemClock.elapsedRealtime() - pretime < 200) {
			return;
		}
    	pretime = SystemClock.elapsedRealtime();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event == null)
				return;
			int keycode = event.getKeyCode();
			Log.e("", "mediabutton onreceive action=" + intentAction + " keycode=" + keycode);
			switch (keycode) {
			case KeyEvent.KEYCODE_MEDIA_STOP:
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_STOP);
				break;
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				if (BTUtils.mBluetooth.isA2DPPlaying()) {
					BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PAUSE);
				}else{
					BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PLAY);
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_NEXT);
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PREV);
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PAUSE);
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				BTUtils.mBluetooth.sendAvrcpCommand(BTService.CMD_PLAY);
				break;
			}
		}
	}
}