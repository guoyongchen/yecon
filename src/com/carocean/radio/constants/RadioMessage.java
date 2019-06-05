package com.carocean.radio.constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;

public class RadioMessage {
	private static final Object obj_handler = new Object();
	public static final int SET_VOLUME_DELAY = 600;
	// radio message type id
	public static final int MSG_MCU_TO_RADIO_SET_BAND = 1001;
	public static final int MSG_MCU_TO_RADIO_SET_FREQ = 1002;
	public static final int MSG_MCU_TO_RADIO_SCAN_STATUS = 1004;
	public static final int MSG_MCU_TO_RADIO_SCAN_FREQ = 1005;

	public static final int MSG_UPDATA_FREQ = 2001;
	public static final int MSG_RESUME_AUIN = 2002;
	public static final int MSG_UPDATA_PROGRESS = 2003;
	public static final int MSG_SET_VOLUME = 2004;
	public static final int MSG_INIT_RADIO = 2005;

	public static final int MSG_RADIO_TO_MCU_FM_SCAN_START = 3001;
	public static final int MSG_RADIO_TO_MCU_AM_SCAN_START = 3002;
	public static final int MSG_RADIO_TO_MCU_SCAN_STOP = 3003;
	public static final int MSG_RADIO_TO_MCU_PERV_FREQ = 3004;
	public static final int MSG_RADIO_TO_MCU_NEXT_FREQ = 3005;
	public static final int MSG_RADIO_TO_MCU_SEND_FREQ = 3006;
	public static final int MSG_RADIO_TO_MCU_SEND_BAND = 3007;
	public static final int MSG_RADIO_TO_MCU_SCAN_PERV = 3008;
	public static final int MSG_RADIO_TO_MCU_SCAN_NEXT = 3009;

	private List<Handler> notifyHandlerList = new ArrayList<Handler>();

	private static RadioMessage mMsg = null;

	private RadioMessage() {
	}

	public static RadioMessage getInstance() {
		if (mMsg == null) {
			mMsg = new RadioMessage();
		}
		return mMsg;
	}

	public void registerMsgHandler(Handler handler) {
		synchronized (obj_handler) {
			for (Handler h : notifyHandlerList) {
				if (h.equals(handler)) {
					return;
				}
			}
			notifyHandlerList.add(handler);
		}
	}

	public void unregisterMsgHandler(Handler handler) {
		synchronized (obj_handler) {
			notifyHandlerList.remove(handler);
		}
	}

	public List<Handler> getListHandler() {
		return notifyHandlerList;
	}

	public void sendMsg(int msgType, Object obj) {
		synchronized (obj_handler) {
			Iterator<Handler> nf = notifyHandlerList.iterator();
			Handler h;
			while (nf.hasNext()) {
				h = nf.next();
				h.sendMessage(h.obtainMessage(msgType, obj));
			}
		}
	}

	public void sendMsgDelayed(int msgType, int tiemLen) {
		synchronized (obj_handler) {
			Iterator<Handler> nf = notifyHandlerList.iterator();
			Handler h;
			while (nf.hasNext()) {
				h = nf.next();
				h.sendEmptyMessageDelayed(msgType, tiemLen);
			}
		}
	}
}
