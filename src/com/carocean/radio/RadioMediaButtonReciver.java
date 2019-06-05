package com.carocean.radio;

import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_NEXT;
import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_PLAY;
import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_PAUSE;
import static android.mcu.McuExternalConstant.MCU_ACTION_MEDIA_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;


public class RadioMediaButtonReciver extends BroadcastReceiver {
    @Override  
    public void onReceive(Context context, Intent intent) {  
        String intentAction = intent.getAction();  
        KeyEvent keyevent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);  
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {  
            int keyCode = keyevent.getKeyCode();  
            int keyAction = keyevent.getAction(); 
            if (keyAction == KeyEvent.ACTION_UP) {
    			switch (keyCode) {
    			case KeyEvent.KEYCODE_MEDIA_PLAY:
    				context.sendBroadcast(new Intent(MCU_ACTION_MEDIA_PLAY));
    				break;
    			case KeyEvent.KEYCODE_MEDIA_PAUSE:
    				context.sendBroadcast(new Intent(MCU_ACTION_MEDIA_PAUSE));
    				break;
    			case KeyEvent.KEYCODE_MEDIA_NEXT:
    				context.sendBroadcast(new Intent(MCU_ACTION_MEDIA_NEXT));
    				break;
    			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
    				context.sendBroadcast(new Intent(MCU_ACTION_MEDIA_PREVIOUS));
    				break;
    			}
			}
        }  
    }  
} 
