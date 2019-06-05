package com.carocean.utils;


import com.carocean.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CmnUtil {
	private  static Toast mToast;
	public static void showToast(Context context, int resid) {
        if (mToast != null) {
        	mToast.cancel();
        }
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.toast, null);
        mToast=new Toast(context);
        mToast.setView(toastRoot);
		TextView tv=(TextView)toastRoot.findViewById(R.id.tvContent);
		tv.setText(resid);
		mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }
	public static void cancelToast(){
		 if (mToast != null) {
			mToast.cancel();
			mToast=null;
		}
	}
}
