package com.carocean.floatwindow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;

public class FloatWindowMessage {
	private static final Object obj_handler = new Object();

	// medai message id
	public final static int FLOAT_MEDIA_MSG_PLAY_PROGRESS = 1001;
	public final static int FLOAT_MEDIA_MSG_PLAY_STATUS = 1002;
	
	// radio message id
	public final static int FLOAT_RADIO_MSG_FREQ = 3001;
	public final static int FLOAT_RADIO_MSG_BAND = 3002;
	
	private List<Handler> notifyHandlerList = new ArrayList<Handler>();
	
	private static FloatWindowMessage mMsg = new FloatWindowMessage();
	private FloatWindowMessage() {
	}
	
	public static FloatWindowMessage getInstance() {
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
