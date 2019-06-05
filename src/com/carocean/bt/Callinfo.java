package com.carocean.bt;

import java.util.HashMap;

import android.content.Intent;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

public class Callinfo {
    private static final String TAG = "CallInfo";
	public String name = "";
	public String num = "";
	public String area = "";
	
	public Time starttime = new Time();
	public int timecount = 0;
	
	public boolean bActive = false;
	public boolean bFinish = false;
	
	public int status = 0;
	public String type = "";

//    public static final int TYPE_INCOMING_BEFORESPEAKING = 4;
    
    public static final int STATUS_OUTGOING = 1;
    public static final int STATUS_INCOMING = 2;
    public static final int STATUS_SPEAKING = 3;
    public static final int STATUS_TERMINATE = 4;
    

	public Callinfo() {
		starttime.setToNow();
	}
	
	public void oncall(Intent intent){
		int status = intent.getIntExtra(BTService.EXTRA_STATE, -1);
		String callnum = intent.getStringExtra(BTService.EXTRA_NUM);
		if (callnum != null && this.num.isEmpty()) {
			this.num = callnum;
			BTUtils.mBluetooth.getcallname(callnum);
			area = BTUtils.mBluetooth.gettelzone(num);
		}
		this.status = status;
		Log.e(TAG, "oncall status=" + status + " num=" + num);
		switch (status) {
		case STATUS_OUTGOING:
			type = ContactDB.TYPE_CALLOUT;
			bActive = true;
			break;
		case STATUS_INCOMING:
			bActive = true;
			break;
		case STATUS_SPEAKING:
			if (this.status == STATUS_INCOMING) {
				type = ContactDB.TYPE_CALLIN;
			}
			startSpeakTimer();
			bActive = true;
			break;
		case STATUS_TERMINATE:
			if (this.status == STATUS_INCOMING) {
				type = ContactDB.TYPE_MISSED;
			}
			bFinish = true;
			bActive = false;
			stopSpeakTimer();
			break;

		default:
			break;
		}
	}
	
	Handler callhandler = new Handler();
	Runnable callrunnable = new Runnable() {
		@Override
		public void run() {
			++timecount;
			callhandler.postDelayed(callrunnable, 1000);
		}
	};
	public void startSpeakTimer(){
    	if (!callhandler.hasCallbacks(callrunnable)) {
        	callhandler.postDelayed(callrunnable, 1000);
		}
	}
	public void stopSpeakTimer(){
    	if (callhandler.hasCallbacks(callrunnable)) {
			callhandler.removeCallbacks(callrunnable);
		}
	}
	
	public void addonerecord(){
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("num", num);
		item.put("type", type);
		item.put("time", starttime.toString());
	//	item.put("time_f", Bluetooth.formatcallhistorytime(starttime.toString()));
		Log.e(TAG, "addonerecord name=" + name + " num=" + num + " type=" + type + "time=" + starttime.toString());
	//	Bluetooth.recordlist.add(0, item); 
	}
	
}
