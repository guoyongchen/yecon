package com.carocean.bt.view;

import com.carocean.R;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TipDialog implements OnDismissListener, OnTouchListener {

	private static String TAG="OkDialog";
	private static TipDialog mtipdialog= null;
	Context mContext = null;
	private Calldlg d = null;
	private LinearLayout mll = null;
	public String m_tip;
	public final int mIType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
	
	private TipDialog(Context context){
		mContext = context;
	}

    public interface IOKListener{  
        void OnOk(boolean ok);
    }

	public static TipDialog getInstance(Context context, String tip){
		if (mtipdialog == null) {
			mtipdialog = new TipDialog(context);
		}
		mtipdialog.m_tip = tip;
		return mtipdialog;
	}
	public static TipDialog getInstance(Context context, int id){
		if (mtipdialog == null) {
			mtipdialog = new TipDialog(context);
		}
		mtipdialog.m_tip = context.getResources().getString(id);
		return mtipdialog;
	}
	public void init(){
		if (d == null) {
			LayoutInflater lif = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mll = (LinearLayout) lif.inflate(R.layout.bt_tipdialog, null);
			
			d = new Calldlg(mContext, R.style.NobackDialog);
			d.setCanceledOnTouchOutside(false);
			d.getWindow().setType(mIType);
			d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);  
			d.getWindow().setContentView(mll);
			
			DisplayMetrics dMetrics = new DisplayMetrics();
			WindowManager objWManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			objWManager.getDefaultDisplay().getMetrics(dMetrics);
			WindowManager.LayoutParams lparams = d.getWindow().getAttributes();
			lparams.width = dMetrics.widthPixels;
			lparams.height = dMetrics.heightPixels;
			lparams.dimAmount =0f;
//			lparams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
			lparams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
			d.getWindow().setAttributes(lparams);
			d.setOnDismissListener(this);
			d.getWindow().getAttributes().windowAnimations = -1;
	//		mll.setOnTouchListener(this);
		}

		((TextView)findViewById(R.id.bt_tip)).setText(m_tip);
	}
	
	public void show(){
		init();
		if (d == null) {
		}else{
			d.show();
			myHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					dismiss();
				}
			}, 2000);
			Log.e(TAG, "show");
		}
	}
	public void dismiss(){
		if (d != null && d.isShowing()) {
			d.dismiss();
			Log.e(TAG, "dismiss");
		}else{
			return;
		}
	}
	private View findViewById(int id){
		if (mll == null) {
			return null;
		}else{
			return mll.findViewById(id);
		}
	}
	Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
        	
        }
	};

	@Override
	public void onDismiss(DialogInterface arg0) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDismiss");
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub

		View v_bg = mll.findViewById(R.id.bt_tipbox_bg);
		View v_fg = mll.findViewById(R.id.bt_tipbox_fg);
		if (event.getX() > v_fg.getX() && event.getX() < (v_fg.getX() + v_fg.getWidth())
				&& event.getY() > v_fg.getY() && event.getY() < (v_fg.getY() + v_fg.getHeight())) {
		}else{
		}
		return true;
	}
}
