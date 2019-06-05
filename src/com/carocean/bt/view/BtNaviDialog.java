package com.carocean.bt.view;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.bt.Callinfo;

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

public class BtNaviDialog implements OnClickListener, OnDismissListener, OnTouchListener {

	private static String TAG="OkDialog";
	private static BtNaviDialog dialog= null;
	Context mContext = null;
	private Calldlg d = null;
	private LinearLayout mll = null;
	public final int mIType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
	TextView bt_name, bt_area, bt_status;
	Button bt_answer, bt_hangup;
	private BtNaviDialog(Context context){
		mContext = context;
	}

	public static BtNaviDialog getInstance(Context context){
		if (dialog == null) {
			dialog = new BtNaviDialog(context);
			dialog.dismiss();
		}
		return dialog;
	}
	
	public void init(){
		if (d == null) {
			LayoutInflater lif = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mll = (LinearLayout) lif.inflate(R.layout.bt_navidialog, null);
			
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
			bt_status = (TextView)findViewById(R.id.bt_status);
			bt_name = (TextView)findViewById(R.id.bt_name);
			bt_area = (TextView)findViewById(R.id.bt_area);
			bt_answer = (Button)findViewById(R.id.bt_answer);
			bt_hangup = (Button)findViewById(R.id.bt_hangup);
			bt_answer.setOnClickListener(this);
			bt_hangup.setOnClickListener(this);
			d.setOnDismissListener(this);
			d.getWindow().getAttributes().windowAnimations = -1;
	//		mll.setOnTouchListener(this);
		}
	}
	
	public void flushui(){
		if (BTUtils.mBluetooth.iscallidle()) {
			return;
		}
		if (BTUtils.mBluetooth.isincoming()) {
			bt_answer.setVisibility(View.VISIBLE);
			bt_status.setText(R.string.bt_income_call_status);
		}else if (BTUtils.mBluetooth.isoutgoing()) {
			bt_answer.setVisibility(View.GONE);
			bt_status.setText(R.string.bt_out_call_status);
		}else if (BTUtils.mBluetooth.isspeaking()) {
			bt_answer.setVisibility(View.GONE);
//			bt_status.setText(R.string.bt_call_active);
		}
		String show = BTUtils.mBluetooth.getcallname();
		if (show == null || show.isEmpty()) {
			show = BTUtils.mBluetooth.getcallnum();
		}
		bt_name.setText(show);
		bt_area.setText(BTUtils.mBluetooth.getcallarea());
	}
	public void show(){
		init();
		if (d == null) {
		}else{
			BTService.registerNotifyHandler(uiHandler);
			flushui();
			d.show();
			Log.e(TAG, "show");
		}
	}
	
	public boolean isshowing(){
		return d != null && d.isShowing();
	}
	
	public void dismiss(){
		if (d != null && d.isShowing()) {
			BTService.unregisterNotifyHandler(uiHandler);
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
	
	Runnable rb_time = new Runnable() {
		
		@Override
		public void run() {
			if (BTUtils.mBluetooth.iscallidle()) {
				return;
			}
			String status = mContext.getResources().getString(R.string.bt_call_active);
			String time = Bluetooth.millSeconds2readableTime(BTUtils.mBluetooth.getcalltimecount());
			String show = status + "\n" + time;
			bt_status.setText(show);
			uiHandler.postDelayed(rb_time, 1000);
		}
	};
	
	Handler uiHandler = new Handler(){
        public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(BTService.ACTION_CALL_STATE)) {
					int status = intent.getIntExtra(BTService.EXTRA_STATE, 0);
					
					if (status == Callinfo.STATUS_SPEAKING) {
						uiHandler.postDelayed(rb_time, 0);
					}else if (status == Callinfo.STATUS_TERMINATE) {
						if (uiHandler.hasCallbacks(rb_time)) {
							uiHandler.removeCallbacks(rb_time);
						}
						dismiss();
					}
					flushui();
				}else if (action.equals(BTService.ACTION_DISCONNECTED)) {
					String path = intent.getStringExtra(BTService.EXTRA_PATH);
					if (path == null || path.equals("hfp")) {
						dismiss();
					}
				}
			}
        }
	};
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
        switch (arg0.getId()) {
        case R.id.bt_answer:
        	BTUtils.mBluetooth.answer();
        	break;
        case R.id.bt_hangup:
        	BTUtils.mBluetooth.hangup();
        	dismiss();
        	break;
        }
	}

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
