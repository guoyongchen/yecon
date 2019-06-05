package com.carocean.bt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.carocean.bt.ate.BtAutoTest;
import com.carocean.bt.ate.BtAutoTest.BtInterface;
import com.carocean.bt.BTService;
import com.carocean.bt.ui.BtPhoneCallFragment;
import com.carocean.bt.view.BtNaviDialog;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.vmedia.bt.PageBT;
import com.carocean.vmedia.bt.PageBT.PageType;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;

public class BTService extends Service {
	public static String TAG = "Service";
	private static BTService mInstance = null;
	static public Bluetooth mBluetooth = null;
	private static List<Handler> notifyHandlerList=new ArrayList<Handler>();
	static final Object obj_handler = new Object();
	public final static int MSG_BT_STATUS_NOTIFY  = 1008;

	static public String ACTION_GO_PBCARD = "action.bt.go_pbcard";
	static public String ACTION_OUT_PBCARD = "action.bt.out_pbcard";
	
	static public final int DOWNLOAD_STATE_FAIL = 0;
	static public final int DOWNLOAD_STATE_SUCCESS = 1;
	static public final int DOWNLOAD_STATE_END = 2;

	static public final int CMD_PAUSE = 1;
	static public final int CMD_PLAY = 2;
	static public final int CMD_PREV = 3;
	static public final int CMD_NEXT = 4;
	static public final int CMD_STOP = 5;
	
	

	static public String ACTION_DEVICE_FOUND = "action.bt.device_found";
	static public String EXTRA_NAME = "name";
	static public String EXTRA_MAC = "mac";
	static public String EXTRA_INDEX = "index";
	static public String EXTRA_NUM = "num";
	static public String EXTRA_STATE = "state";
	static public String EXTRA_TYPE = "type";
	static public String EXTRA_TIME = "time";
	static public String EXTRA_PATH = "path";
	static public String EXTRA_SINGER = "singer";
	
	static public String ACTION_DISCOVERY_START = "action.bt.discovery_start";
	static public String ACTION_DISCOVERY_END = "action.bt.discovery_end";

	static public String ACTION_DEVICE_NAME = "action.bt.device_name";
	static public String ACTION_CONNECTED = "action.bt.connected";
	static public String ACTION_DISCONNECTED = "action.bt.disconnected";
	static public String ACTION_CONNECTING = "action.bt.connecting";
	static public String ACTION_PAIREDLIST = "action.bt.pairedlist";
	static public String ACTION_CONTACT = "action.bt.contact";
	static public String ACTION_RECORD = "action.bt.record";
	static public String ACTION_DOWNLOAD_STATE = "action.bt.download_state";
	static public String ACTION_CALL_STATE = "action.bt.call_state";
	static public String ACTION_CALLNAME = "action.bt.callname";
	static public String ACTION_AUDIO = "action.bt.audio";
	static public String ACTION_MIC = "action.bt.mic";
	static public String ACTION_MUSIC_PLAYING = "action.bt.music_playing";
	static public String ACTION_MUSIC_ID3 = "action.bt.music_id3";
	static public String ACTION_MUSIC_PLAY_POS = "action.bt.music.play.pos";
	static public String ACTION_PHONE_POWER_STATUS_CHANGE = "action.bt.powerstatus_change";
	static public String ACTION_GOTORADIO = "action.bt.gotoradio";
	static public String ACTION_BTSTATE = "action.bt.btstate";
	@Override
	public void onCreate() {
        Log.e(TAG, "btservice oncreate");
        mInstance = this;
        IntentFilter filter = new IntentFilter();
        for (String item : actions) {
        	filter.addAction(item); 
		}
        registerReceiver(mBTReceiver, filter);
        if (mBluetooth == null) {
			mBluetooth = BTUtils.mBluetooth;
		}
        mBluetooth.handlebootcompleted();
		super.onCreate();
	}

	private void initAteTool() {
		BtAutoTest.getInstance(this).init(new BtInterface() {

			public void setOnoff(int onoff) {
				if (onoff == 0x01) {
					if (mBluetooth != null) {
						mBluetooth.openbt();
					}
				} else if (onoff == 0x02) {
					if (mBluetooth != null) {
						mBluetooth.closebt();
					}
				}
			}

			public void enterFunction(int id) {
				if (id == 0x00) {
					PageBT.setPageType(PageType.TAB_INDEX_BT_CONTACT);
					launcherUtils.startBT();
				} else if (id == 0x01) {
					PageBT.setPageType(PageType.TAB_INDEX_BT_SETTINGS);
					launcherUtils.startBT();
				} else if (id == 0x02) {
					PageMedia.setView(ViewType.ViewBtMusic);
					launcherUtils.startMedia();
				} else if (id == 0x03) {
					PageBT.setPageType(PageType.TAB_INDEX_BT_CALL_BOOK);
					launcherUtils.startBT();
				} else if (id == 0x04) {
					PageBT.setPageType(PageType.TAB_INDEX_BT_RECORD);
					launcherUtils.startBT();
				} else if (id == 0x05) {
					PageBT.setPageType(PageType.TAB_INDEX_BT_SETTINGS);
					launcherUtils.startBT();
				}
			}

			public void handonoff(int cmd) {
				if (cmd == 0x01) {
					if (mBluetooth != null) {
						mBluetooth.answer();
					}
					// Intent intent = new
					// Intent(YeconConstants.ACTION_YECON_KEY_UP);
					// intent.putExtra("key_code",
					// KeyEvent.KEYCODE_YECON_PHONE_ON);
					// sendBroadcast(intent);
				} else if (cmd == 0x02) {
					if (mBluetooth != null) {
						mBluetooth.hangup();
					}
					// Intent intent = new
					// Intent(YeconConstants.ACTION_YECON_KEY_UP);
					// intent.putExtra("key_code",
					// KeyEvent.KEYCODE_YECON_PHONE_OFF);
					// sendBroadcast(intent);
				}
			}

			public void getOnoff(int event) {
				Intent intent = new Intent(BtAutoTest.AUTOMATION_BT_BROADCAST_RECV);
				intent.putExtra("eventType", event);
				intent.putExtra("openVal", mBluetooth.isbtopened() ? 1 : 2);
				mBluetooth.sendBroadcastATE(intent);
			}

			public void getCallStatus(int event) {
				int callType = mBluetooth.getcallstatus();
				Intent intent = new Intent(BtAutoTest.AUTOMATION_BT_BROADCAST_RECV);
				intent.putExtra("eventType", event);
				intent.putExtra("callVal", callType);
				mBluetooth.sendBroadcastATE(intent);
			}

			public void getConnectStatus(int event) {
				Intent intent = new Intent(BtAutoTest.AUTOMATION_BT_BROADCAST_RECV);
				intent.putExtra("eventType", event);
				intent.putExtra("connectedVal", mBluetooth.isconnected());
				mBluetooth.sendBroadcastATE(intent);
			}

			public void bookStatus() {
				Intent intent = new Intent(BtAutoTest.AUTOMATION_BT_BROADCAST_RECV);
				intent.putExtra("eventType", 0x0B);
				if (!mBluetooth.isconnected()) {
					mPhoneBook = 0;
				}
				intent.putExtra("bookStatus", mPhoneBook);
				mBluetooth.sendBroadcastATE(intent);
			}
		});
	}
	public static BTService getInstance(){
		return mInstance;
	}
	public static int mPhoneBook = 0;
	public static boolean bback = false;
    private BroadcastReceiver mBTReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	Log.e(TAG, "action=" + action);
        	if (action.equals(ACTION_DEVICE_NAME)) {
				mBluetooth.onlocaldevicenamechanged(intent);
			}else if (action.equals(ACTION_DEVICE_FOUND)) {
				mBluetooth.ondevicefound(intent);
			}else if (action.equals(ACTION_DISCOVERY_START)) {
				mBluetooth.ondiscoverystart();
			}else if (action.equals(ACTION_DISCOVERY_END)) {
				mBluetooth.ondiscoveryend();
			}else if (action.equals(ACTION_CONNECTED)) {
				mBluetooth.onconnected(intent);
			}else if (action.equals(ACTION_DISCONNECTED)) {
				mBluetooth.ondisconnected(intent);
			}else if (action.equals(ACTION_PAIREDLIST)) {
				mBluetooth.onpairedlist(intent);
			}else if (action.equals(ACTION_CONTACT)) {
				mBluetooth.oncontactload(intent);
			}else if (action.equals(ACTION_DOWNLOAD_STATE)) {
				if (Bluetooth.mask_loadrecord != 0) {
					intent.putExtra(EXTRA_PATH, "record");
				}else if (Bluetooth.loading_contact) {
					intent.putExtra(EXTRA_PATH, "contact");
				}else{
					intent.putExtra(EXTRA_PATH, "");
				}
				mBluetooth.ondownloadstatechange(intent);
			}else if (action.equals(ACTION_RECORD)) {
				mBluetooth.onrecordload(intent);
			}else if (action.equals(ACTION_CALL_STATE)) {
				int status = intent.getIntExtra(BTService.EXTRA_STATE, 0);
				if (status == Callinfo.STATUS_TERMINATE) {
					mBluetooth.releasehfpfocus();
				}else {
					mBluetooth.requesthfpfocus();
				}
				
				mBluetooth.oncall(intent);
				oncall(intent);
			}else if (action.equals(ACTION_AUDIO)) {
				mBluetooth.onaudiochange(intent);
			}else if (action.equals(ACTION_MIC)) {
				mBluetooth.onaudiochange(intent);
			}else if (action.equals(ACTION_MUSIC_PLAYING)) {
				mBluetooth.onplayingstatechanged(intent);
			}else if (action.equals(ACTION_MUSIC_ID3)) {
				mBluetooth.onid3info(intent);
			}else if (action.equals(ACTION_MUSIC_PLAY_POS)) {
				mBluetooth.onmusicpos(intent);
			}else if (action.equals(ACTION_BTSTATE)) {
				mBluetooth.onbtstate(intent);
			}
        	notifyUiBtStatus(intent);
        }
    };
    
    private void oncall(Intent intent) {
		int status = intent.getIntExtra(BTService.EXTRA_STATE, 0);
		if (mBluetooth.iscurnavimod()) {
			BtNaviDialog.getInstance(this).show();
		} else if (!BtPhoneCallFragment.bforeground && !BtNaviDialog.getInstance(this).isshowing()) {
			launcherUtils.startBT();
		}
	}
    
    public static void showbtactivity(Class<?> cls){
		Intent intentPhoneCall = new Intent();
		intentPhoneCall.setClass(mInstance, cls);
		intentPhoneCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mInstance.startActivity(intentPhoneCall);
    }
    
    

	public static void registerNotifyHandler(Handler handler){
		synchronized (obj_handler) {
			for (Handler h : notifyHandlerList) {
				if (h.equals(handler)) {
					return;
				}
			}
			notifyHandlerList.add(handler);
		}
	}
	public static void unregisterNotifyHandler(Handler handler){
		synchronized (obj_handler) {
			notifyHandlerList.remove(handler);
		}
	}
	public static void notifyUiBtStatus(Intent intent){
		synchronized (obj_handler) {
			Iterator<Handler> nf = notifyHandlerList.iterator();
			Handler h;
			while(nf.hasNext()){
				h = nf.next();
				h.sendMessage(h.obtainMessage(MSG_BT_STATUS_NOTIFY, intent));
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	String[] actions = {ACTION_DEVICE_FOUND, ACTION_DEVICE_NAME, ACTION_DISCOVERY_START, ACTION_DISCOVERY_END, ACTION_CONNECTED, ACTION_DISCONNECTED, 
			ACTION_CONNECTING, ACTION_PAIREDLIST, ACTION_CONTACT, ACTION_DOWNLOAD_STATE, ACTION_RECORD, ACTION_CALL_STATE, ACTION_CALLNAME, ACTION_AUDIO,
			ACTION_MIC, ACTION_MUSIC_PLAYING, ACTION_MUSIC_ID3, ACTION_MUSIC_PLAY_POS, ACTION_BTSTATE
	};
}