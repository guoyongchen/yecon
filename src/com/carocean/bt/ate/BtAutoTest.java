package com.carocean.bt.ate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 
 * @author CB070
 *
 */
public class BtAutoTest {
	
	public final static String TAG = "BtAutoTest";
	/** 收到ATE发送的广播信息*/
	public final static String AUTOMATION_BT_BROADCAST_SEND = "AUTOMATION_BT_BROADCAST_SEND";
	/** 向ATE发送的广播信息*/
	public final static String AUTOMATION_BT_BROADCAST_RECV = "AUTOMATION_BT_BROADCAST_RECV";

	private volatile static BtAutoTest mInstance;
	
	private BroadcastReceiver mBtBroadcastReceiver = null;
	
	private Context mContext = null;
	
	private BtInterface mBtInterface = null;

	public interface BtInterface {
		public void setOnoff(int onoff); // 0 关闭 1 开启

		public void getOnoff(int event); // 得到关闭开启的状态

		public void handonoff(int cmd); // 0 挂断 1 接听

		public void getConnectStatus(int event); // 得到连接状态

		public void getCallStatus(int event); // 得到蓝牙通话状态

		public void enterFunction(int id); // 进入指定的界面
		
		public void bookStatus(); // 获取蓝牙电话本下载状态
	}

	public BtAutoTest(Context context) {
		mContext = context;

		mBtBroadcastReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.i(TAG, "autotest---receive msg=" + action);

				if (null == action || mBtInterface == null || mContext == null) {
					return;
				}

				if (action.equals(AUTOMATION_BT_BROADCAST_SEND)) {

					final int eventType = intent.getIntExtra("eventType", 0);
					Log.i(TAG, "eventType = " + eventType);

					switch (eventType) {
					case 0x01:
						int cmd = intent.getIntExtra("openType", -1);
						mBtInterface.setOnoff(cmd);
						mBtInterface.getOnoff(eventType);
						break;
					case 0x02:
						int viewType = intent.getIntExtra("viewType", -1);
						mBtInterface.enterFunction(viewType);
						break;
					case 0x03:
						int callType = intent.getIntExtra("callType", -1);
						mBtInterface.getCallStatus(eventType);
						mBtInterface.handonoff(callType);
						break;
					case 0x04:
						mBtInterface.getConnectStatus(eventType);
						break;
					case 0x0B:
						mBtInterface.bookStatus();
						break;
					default:
						break;
					}
				}
			}
		};
	}

	public static BtAutoTest getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new BtAutoTest(context);
		}
		return mInstance;
	}

	public void init(BtInterface radioInterface) {
		mBtInterface = radioInterface;
		IntentFilter filter = new IntentFilter();
		filter.addAction(AUTOMATION_BT_BROADCAST_SEND);
		if (mContext != null) {
			mContext.registerReceiver(mBtBroadcastReceiver, filter);
		}
	}

	public void exit() {
		if (mContext != null && mBtBroadcastReceiver != null) {
			mContext.unregisterReceiver(mBtBroadcastReceiver);
		}
	}
}
