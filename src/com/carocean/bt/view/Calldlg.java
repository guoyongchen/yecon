package com.carocean.bt.view;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;


public class Calldlg extends Dialog{
	public Calldlg(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	public interface DialogOnkeydownListener{  
        void onKeyDown(int keyCode, KeyEvent event);  
    }
	DialogOnkeydownListener mcallback = null;
	public void setcallback(DialogOnkeydownListener callback){
		mcallback = callback;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (mcallback != null && isShowing()) {
			mcallback.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
