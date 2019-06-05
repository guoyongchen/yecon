package com.carocean.launcher.popupwindow;

import com.carocean.R;
import com.carocean.bt.BTUtils;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.mcu.McuMethodManager;
import com.carocean.utils.Utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingPopupWindow extends PopupWindow implements OnClickListener, OnLongClickListener {

	private final static String TAG = "SettingPopupWindow";
	private View mPopView;
	private Context mContext;
	private SeekBar setting_pop_backlight_sb;
	private FrameLayout pop_close_screen_fr, pop_setting_fr, pop_wifi_fr, pop_bt_fr, pop_backhome_fr, pop_restart_fr,
			pop_close_fr;
	private ImageView pop_wifi_iv, pop_bt_iv;
	private TextView pop_wifi_tv, pop_bt_tv;

	private WifiManager mWifiManager;

	public SettingPopupWindow(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
		setPopupWindow();

		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		set_status_bt_wifi();
		setBacklight();
		setting_pop_backlight_sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar mSeekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar mSeekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar mSeekBar, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				int brightness = changeBackLight(arg1, 100, 0, 201, 1);
				Utils.setBrightness(mContext, brightness);

			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, filter);

		pop_close_screen_fr.setOnClickListener(this);
		pop_setting_fr.setOnClickListener(this);
		pop_wifi_fr.setOnClickListener(this);
		pop_bt_fr.setOnClickListener(this);
		pop_backhome_fr.setOnClickListener(this);
		pop_restart_fr.setOnClickListener(this);
		pop_close_fr.setOnClickListener(this);

		pop_wifi_fr.setOnLongClickListener(this);
		pop_bt_fr.setOnLongClickListener(this);

	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context);
		// 绑定布局
		mPopView = inflater.inflate(R.layout.popwindow_setting, null);

		setting_pop_backlight_sb = (SeekBar) mPopView.findViewById(R.id.setting_pop_backlight_sb);
		pop_close_screen_fr = (FrameLayout) mPopView.findViewById(R.id.pop_close_screen_fr);
		pop_setting_fr = (FrameLayout) mPopView.findViewById(R.id.pop_setting_fr);
		pop_wifi_fr = (FrameLayout) mPopView.findViewById(R.id.pop_wifi_fr);
		pop_bt_fr = (FrameLayout) mPopView.findViewById(R.id.pop_bt_fr);
		pop_backhome_fr = (FrameLayout) mPopView.findViewById(R.id.pop_backhome_fr);
		pop_restart_fr = (FrameLayout) mPopView.findViewById(R.id.pop_restart_fr);
		pop_close_fr = (FrameLayout) mPopView.findViewById(R.id.pop_close_fr);

		pop_wifi_iv = (ImageView) mPopView.findViewById(R.id.pop_wifi_iv);
		pop_bt_iv = (ImageView) mPopView.findViewById(R.id.pop_bt_iv);

		pop_wifi_tv = (TextView) mPopView.findViewById(R.id.pop_wifi_tv);
		pop_bt_tv = (TextView) mPopView.findViewById(R.id.pop_bt_tv);

	}

	public void setBacklight() {
		if (setting_pop_backlight_sb != null) {
			
			int cur_brightness = Utils.getBrightness();
			if (cur_brightness > 201) {
				cur_brightness = 201;
			}
			setting_pop_backlight_sb.setProgress(changeBackLight(cur_brightness, 201, 1, 100, 0));
		}
	};

	/**
	 * 初始化时设置蓝牙和wifi的状态
	 */
	public void set_status_bt_wifi() {

		if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			pop_wifi_iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.setting_pop_wifi_selector));
			pop_wifi_tv.setSelected(false);
		} else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			pop_wifi_iv.setImageDrawable(
					mContext.getResources().getDrawable(R.drawable.setting_pop_wifi_disable_selector));
			pop_wifi_tv.setSelected(true);
		}

        if (!BTUtils.mBluetooth.isbtopened()) {
			pop_bt_iv.setTag(false);
			pop_bt_iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.setting_pop_bt_selector));
			pop_bt_tv.setSelected(false);
		} else {
			pop_bt_iv.setTag(true);
			pop_bt_iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.setting_pop_bt_disable_selector));
			pop_bt_tv.setSelected(true);
		}
	}

	/**
	 * 设置窗口的相关属性
	 */
	private void setPopupWindow() {
		this.setContentView(mPopView);// 设置View
		this.setWidth(LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
		this.setHeight(LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
		this.setFocusable(true);// 设置弹出窗口可
		this.setAnimationStyle(R.style.setting_popwindow_anim_style);// 设置动画
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
		mPopView.setOnTouchListener(new OnTouchListener() {// 如果触摸位置在窗口外面则销毁

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int height = mPopView.findViewById(R.id.id_pop_layout).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
	}

	/**
	 * 背光亮度相互转换0-100转0到255
	 */
	public static int changeBackLight(int cur_Progress, int m_MaxProgress, int m_MinProgress, int m_MaxLight,
			int m_MinLight) {
		int fRange_progress = m_MaxProgress - m_MinProgress;
		int fRange_light = m_MaxLight - m_MinLight;
		int s = (int) ((cur_Progress - m_MinProgress) * fRange_light / fRange_progress + m_MinLight);
		return s;

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.pop_close_screen_fr:
			Utils.TransKey(KeyEvent.KEYCODE_YECON_BLACKOUT);
			dismiss();
			break;

		case R.id.pop_setting_fr:
			launcherUtils.startSettings();
			dismiss();
			break;

		case R.id.pop_wifi_fr:

			if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLING) {
				return;
			} else if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED) {
				mWifiManager.setWifiEnabled(false);
			} else if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_DISABLED
					|| mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_UNKNOWN) {
				mWifiManager.setWifiApEnabled(null, false);
				mWifiManager.setWifiEnabled(true);
			}

			break;

		case R.id.pop_bt_fr:

			if ((Boolean) pop_bt_iv.getTag()) {
				pop_bt_iv.setTag(false);
				pop_bt_iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.setting_pop_bt_selector));
				pop_bt_tv.setSelected(false);
			} else {
				pop_bt_iv.setTag(true);
				pop_bt_iv.setImageDrawable(
						mContext.getResources().getDrawable(R.drawable.setting_pop_bt_disable_selector));
				pop_bt_tv.setSelected(true);
			}

			BTUtils.mBluetooth.switchbt();

			break;

		case R.id.pop_backhome_fr:
			Intent intent = new Intent(launcherUtils.SHOW_BACKSTAGE_ACTION);
			mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
			break;

		case R.id.pop_restart_fr:
			IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
			try {
				power.reboot(false, "", false);
				McuMethodManager.getInstance(mContext).sendSysRestartKeyCMD();
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException when RebootSystem: ");
				return;
			}
			break;

		case R.id.pop_close_fr:
			Utils.TransKey(KeyEvent.KEYCODE_YECON_POWER);
			dismiss();
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onLongClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.pop_wifi_fr:
			launcherUtils.startWifi();
			break;

		case R.id.pop_bt_fr:
			launcherUtils.startBT();
			break;

		default:
			break;
		}
		return false;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				Log.e(TAG, "wifiState: " + wifiState);
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					pop_wifi_iv.setImageDrawable(
							mContext.getResources().getDrawable(R.drawable.setting_pop_wifi_selector));
					pop_wifi_tv.setSelected(false);
				} else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
					pop_wifi_iv.setImageDrawable(
							mContext.getResources().getDrawable(R.drawable.setting_pop_wifi_disable_selector));
					pop_wifi_tv.setSelected(true);
				}
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				Log.e(TAG, "btState: " + btState);
				if (btState == BluetoothAdapter.STATE_ON) {
					pop_bt_iv.setTag(false);
					pop_bt_iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.setting_pop_bt_selector));
					pop_bt_tv.setSelected(false);
				} else if (btState == BluetoothAdapter.STATE_OFF) {
					pop_bt_iv.setTag(true);
					pop_bt_iv.setImageDrawable(
							mContext.getResources().getDrawable(R.drawable.setting_pop_bt_disable_selector));
					pop_bt_tv.setSelected(true);
				}
			}
		}
	};

}
