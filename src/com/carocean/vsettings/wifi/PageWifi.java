package com.carocean.vsettings.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.carocean.R;
import com.carocean.settings.utils.tzUtils;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.view.MyListView;
import com.carocean.vsettings.view.vHeaderLayout;
import com.carocean.vsettings.view.vHeaderLayout.onOneCheckBoxListener;
import com.carocean.vsettings.wifi.Fragment_Wifi_password.Fragment_OnCustomDialogListener;
import com.carocean.vsettings.wifi.PopupWindow_connect.OnConnectActionListener;
import com.carocean.vsettings.wifi.PopupWindow_password.OnCustomDialogListener;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

/**
 * @ClassName: PageWifi
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.05.08
 **/
public class PageWifi extends DialogFragment {
	public View mRootView;
	public static PopupWindow_password mPopupWindow_password;
	public static PopupWindow_connect mPopupWindow_connect;
	public static Fragment_Wifi_password fragment;
	public Context mContext;
	private WifiManager mWifiManager;
	private ProgressBar wifiRefreshing;
	private vHeaderLayout layout_wifi_switch;

	private WifiUtils mWifiUtils;
	private List<ScanResult> mScanResultList;
	private List<WifiItem> mListWifi = new ArrayList<WifiItem>();
	private MyListView listView;

	private WifiItem mCurrentWifi = null;
	private WifiListAdapter wifiAdapter;
	private List<WifiConfiguration> mConfigList;
	private String wifiPassword = null;

	private DetailedState mLastState;

	FragmentManager mFragmentManager;

	public PageWifi(Context context, ViewGroup root) {
		// TODO Auto-generated constructor stub
		mContext = context;
		init(context);
		initView(root);
	}

	void setTimer_WifiStartScan() {
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_ENABLED)
					WifiConfigInfo();
				if (mWifiUtils.getScanResults().size() > 0)
					timer.cancel();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	void init(Context context) {
		mRootView = MediaActivity.mActivity.getWindow().getDecorView();
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mFragmentManager = MediaActivity.mActivity.getFragmentManager();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		filter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
		filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	void initView(View rootView) {
		wifiRefreshing = (ProgressBar) rootView.findViewById(R.id.wifi_refreshing_prog);
		listView = (MyListView) rootView.findViewById(R.id.wifi_listview);
		wifiRefreshing.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
		wifiAdapter = new WifiListAdapter(mContext, R.layout.setting_wifi_list_item_layout, mListWifi);
		listView.setAdapter(wifiAdapter);
		ListOnItemClickListener wifiListListener = new ListOnItemClickListener();
		listView.setOnItemClickListener(wifiListListener);

		mWifiUtils = new WifiUtils(mContext);

		mWifiUtils.WifiOpen();
		setTimer_WifiStartScan();

		layout_wifi_switch = (vHeaderLayout) rootView.findViewById(R.id.view_wifi_switch);
		layout_wifi_switch.setOneCheckBoxListener(new onOneCheckBoxListener() {

			@Override
			public void onCheckout(View view, boolean value) {
				// TODO Auto-generated method stub
				if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING
						&& mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLING)
					setWifiEnable();
			}
		});
	}

	void setWifiEnable() {
		if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING
				|| mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
			return;
		} else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			mWifiManager.setWifiEnabled(false);
		} else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED
				|| mWifiManager.getWifiState() == WifiManager.WIFI_STATE_UNKNOWN) {
			if (mWifiManager.isWifiApEnabled())
				mWifiManager.setWifiApEnabled(null, false);
			mWifiManager.setWifiEnabled(true);
			setTimer_WifiStartScan();
		}
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
					|| WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)
					|| WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
				refreshWifiList(mLastState);
			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				if (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_DISABLED) {
					wifiAdapter.clear();
					wifiAdapter.notifyDataSetChanged();
					layout_wifi_switch.setChecked(false);
					wifiRefreshing.setVisibility(View.GONE);
					listView.setVisibility(View.GONE);
				} else if (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_ENABLED) {
					layout_wifi_switch.setChecked(true);
					wifiRefreshing.setVisibility(View.VISIBLE);
				} else if (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_ENABLING) {
				} else if (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_DISABLING) {
				} else {
					tzUtils.showToast(R.string.setting_wifi_WLAN_UNKNOWN);
				}

			} else if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				DetailedState state = ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
						.getDetailedState();
				mLastState = state;
				refreshWifiList(state);
			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
				// SupplicantState supState =
				// intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				// DetailedState state = WifiInfo.getDetailedStateOf(supState);
				// mLastState = state;
				// refreshWifiList(state);
			} else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
			} else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
			}
		}
	};

	public List<ScanResult> noSameName(List<ScanResult> oldSr) {
		List<ScanResult> newSr = new ArrayList<ScanResult>();
		for (ScanResult result : oldSr) {
			if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID))
				newSr.add(result);
		}
		return newSr;
	}

	public boolean containName(List<ScanResult> sr, String name) {
		for (ScanResult result : sr) {
			if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name))
				return true;
		}
		return false;
	}

	private void getScanResultOrderedList() {
		mListWifi.clear();
		mScanResultList = noSameName(mWifiUtils.getScanResults());

		onShowListView();

		if (!isWifiConnected(mContext) && (mWifiUtils.WifiCheckState() == WifiManager.WIFI_STATE_ENABLED)) {
			Log.w("getScanResultOrderedList", "scan result , change left display");
		}

		for (int i = 0; i < mScanResultList.size(); i++) {
			if (null == mScanResultList.get(i).SSID || mScanResultList.get(i).SSID == "") {
				continue;
			}

			WifiItem wifiItem = new WifiItem();
			wifiItem.setWifiName(mScanResultList.get(i).SSID);
			wifiItem.setWifiLock(true);
			wifiItem.setWifiBssid(mScanResultList.get(i).BSSID);

			String desc = "";
			String mDesc = mScanResultList.get(i).capabilities;
			if (mDesc.toUpperCase().contains("WPA-PSK")) {
				desc = "WPA";
			}
			if (mDesc.toUpperCase().contains("WPA2-PSK")) {
				desc = "WPA2";
			}
			if (mDesc.toUpperCase().contains("WPA-PSK") && mDesc.toUpperCase().contains("WPA2-PSK")) {
				desc = "WPA/WPA2";
			}
			if (mDesc.toUpperCase().contains("WPS")) {
				desc = desc + mContext.getResources().getString(R.string.setting_wifi_WPA_Usable);
			}

			int level = mScanResultList.get(i).level;

			if (desc.isEmpty()) {
				wifiItem.setWifiLock(false);
				desc = mContext.getResources().getString(R.string.setting_wifi_WLAN_Unprotected);
				if (level <= 0 && level > -60) {
					wifiItem.setSignalCount(4);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_4);
				} else if (level <= -60 && level > -72) {
					wifiItem.setSignalCount(3);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_3);
				} else if (level <= -72 && level >= -83) {
					wifiItem.setSignalCount(2);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_2);
				} else if (level < -83 && level > -95) {
					wifiItem.setSignalCount(1);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_1);
				}
			} else {
				wifiItem.setWifiLock(true);
				if (level <= 0 && level > -60) {
					wifiItem.setSignalCount(4);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_lock4);
				} else if (level <= -60 && level > -72) {
					wifiItem.setSignalCount(3);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_lock3);
				} else if (level <= -72 && level >= -83) {
					wifiItem.setSignalCount(2);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_lock2);
				} else if (level < -83 && level > -95) {
					wifiItem.setSignalCount(1);
					wifiItem.setImageLevelID(R.drawable.wifi_signal_lock1);
				}
			}
			wifiItem.setDescribes(desc);
			wifiItem.setConnected(desc);
			wifiItem.setSignalLevel(level);
			mListWifi.add(wifiItem);
		}

		Comparator<WifiItem> comp = new Comparator<WifiItem>() {
			@Override
			public int compare(WifiItem lhs, WifiItem rhs) {
				int level1 = lhs.getSignalLevel();
				int level2 = rhs.getSignalLevel();
				if (level1 < level2)
					return 1;
				if (level1 > level2)
					return -1;
				return 0;
			}
		};
		Collections.sort(mListWifi, comp);
	}

	private boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void connectedFace() {
		String strState = mContext.getResources().getString(R.string.setting_wifi_state_CONNECTED);
		mListWifi.get(0).setConnected(strState);
		Log.i("connectedFace", "connected Face");
	}

	/**
	 * 鍒锋柊wifi鍒楄〃
	 */
	private void refreshWifiList(DetailedState dState) {
		// if wifi device don't enable
		if (mWifiUtils.WifiCheckState() != WifiManager.WIFI_STATE_ENABLED) {
			return;
		}

		String connectingWifissid;
		WifiInfo mWifiInfo = mWifiUtils.getConnectedInfo();
		connectingWifissid = mWifiInfo.getSSID();

		getScanResultOrderedList();
		if (null != connectingWifissid && connectingWifissid != "" && dState != null) {
			mCurrentWifi = null;
			for (int i = 0; i < mListWifi.size(); i++) {
				int length = connectingWifissid.length() - 1;
				String ssid = connectingWifissid.substring(1, length);
				if (mListWifi.get(i).getWifiName().equals(ssid)) {
					mCurrentWifi = mListWifi.get(i);
					mListWifi.remove(i);
					break; // out of the loop (for)
				}
			}

			if (mCurrentWifi != null) {
				String strState;
				Log.d("DetailedState", "SUPPLICANT-----DetailedState_  " + dState);
				switch (dState) {
				case SCANNING:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_SCANNING);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				case CONNECTING:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_CONNECTING);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				case AUTHENTICATING:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_AUTHENTICATING);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				case OBTAINING_IPADDR:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_OBTAINING_IPADDR);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				case CONNECTED:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_CONNECTED);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				case DISCONNECTED:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_DISCONNECTED);
					mCurrentWifi.setConnected(strState); // 宸叉柇寮�
					break;
				case DISCONNECTING:
					strState = mContext.getResources().getString(R.string.setting_wifi_state_DISCONNECTING);
					mCurrentWifi.setConnected(strState);
					mListWifi.add(0, mCurrentWifi);
					break;
				default:
					break;
				}

			}
			if (mCurrentWifi != null && mCurrentWifi.getSignalCount() > 0) {
				// connectedFace();
				switch (mCurrentWifi.getSignalCount()) {
				case 4:
					mCurrentWifi.setImageLevelID(R.drawable.wifi_signal_connect_4);
					break;
				case 3:
					mCurrentWifi.setImageLevelID(R.drawable.wifi_signal_connect_3);
					break;
				case 2:
					mCurrentWifi.setImageLevelID(R.drawable.wifi_signal_connect_2);
					break;
				case 1:
					mCurrentWifi.setImageLevelID(R.drawable.wifi_signal_connect_1);
					break;
				default:
					break;
				}
			}
			wifiAdapter.notifyDataSetChanged();
		}
	}

	private void onShowListView() {
		if (!mScanResultList.isEmpty()) {
			if (wifiRefreshing.getVisibility() == View.VISIBLE)
				wifiRefreshing.setVisibility(View.GONE);
			if (wifiRefreshing.getVisibility() != View.VISIBLE)
				listView.setVisibility(View.VISIBLE);
		}
	}

	class ListOnItemClickListener implements OnItemClickListener {
		String wifiItemSSID = null;
		private String wifiItemENC;
		private int wifiItemId;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			wifiItemENC = wifiAdapter.getItem(position).getDescribes();
			wifiItemSSID = wifiAdapter.getItem(position).getWifiName();
			wifiItemId = mWifiUtils.IsConfiguration("\"" + wifiItemSSID + "\"");
			if (wifiItemId != -1) {
				if (wifiItemId == mWifiUtils.getConnectedID()) {

					showPopWindow_connect();
					mPopupWindow_connect.setOnConnectListener(wifiItemSSID, wifiItemENC, new OnConnectActionListener() {

						@Override
						public void connectAction(String mAction) {
							// TODO Auto-generated method stub
							if (mAction.equalsIgnoreCase("forget")) {
								mWifiUtils.removeNetwork(wifiItemId);
								mWifiUtils.getConfiguration();
							} else if (mAction.equalsIgnoreCase("connect")) {
								// mWifiUtils.ConnectWifi(mContext, wifiItemId);
							}
						}
					});
					return;
				}

				showPopWindow_connect();
				mPopupWindow_connect.setOnConnectListener(wifiItemSSID, wifiItemENC, new OnConnectActionListener() {

					@Override
					public void connectAction(String mAction) {
						// TODO Auto-generated method stub
						if (mAction.equalsIgnoreCase("forget")) {
							mWifiUtils.removeNetwork(wifiItemId);
							mWifiUtils.getConfiguration();
						} else if (mAction.equalsIgnoreCase("connect")) {
							mWifiUtils.ConnectWifi(mContext, wifiItemId);
						}
					}
				});

			} else if (!(wifiAdapter.getItem(position).isWifiLock())) {
				connectNewWifi(wifiItemSSID, false);
			} else {
				// showPopWindow_password();
				// 如果没有，提示输入密码
				fragment = new Fragment_Wifi_password(wifiItemSSID, new Fragment_OnCustomDialogListener() {

					@Override
					public void back(String str) {
						// TODO Auto-generated method stub
						wifiPassword = str;
						if (wifiPassword != null) {
							connectNewWifi(wifiItemSSID, true);
						}
					}
				});
				if (mFragmentManager == null) {
					mFragmentManager = MediaActivity.mActivity.getFragmentManager();
				}
				fragment.show(mFragmentManager, "set_wifi_password");
			}
		}

		private void showPopWindow_password() {
			if (mPopupWindow_password == null) {
				View localView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.setting_wifi_password_layout, null);
				mPopupWindow_password = new PopupWindow_password(mContext, localView);
				mPopupWindow_password.setSoftInputMode(
						PopupWindow.INPUT_METHOD_NEEDED | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

			}
			mPopupWindow_password.setOnListener(wifiItemSSID, new OnCustomDialogListener() {
				@Override
				public void back(String str) {
					// TODO Auto-generated method stub
					wifiPassword = str;
					if (wifiPassword != null) {
						connectNewWifi(wifiItemSSID, true);
					}
				}
			});
			mPopupWindow_password.showAtLocation(mRootView, Gravity.CENTER, 0, 50);

		}

		private void showPopWindow_connect() {
			if (mPopupWindow_connect == null) {
				View localView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.setting_wifi_connect_layout, null);
				mPopupWindow_connect = new PopupWindow_connect(mContext, localView);
				mPopupWindow_connect.setSoftInputMode(
						PopupWindow.INPUT_METHOD_NEEDED | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			}
			mPopupWindow_connect.showAtLocation(mRootView, Gravity.CENTER, 70, 0);

		}
	}

	protected void connectNewWifi(String newWifiItemSSID, boolean ispwd) {
		int netId = mWifiUtils.AddWifiConfig(mScanResultList, newWifiItemSSID, wifiPassword, ispwd);
		Log.d("WifiPswDialog", String.valueOf(netId)); // add wifi
		// success,display NetId
		if (netId != -1) {
			mWifiUtils.getConfiguration();
			mWifiUtils.ConnectWifi(mContext, netId);
		} else {
			tzUtils.showToast(R.string.setting_wifi_UNABLECONNECT);
		}
	}

	private void WifiConfigInfo() {
		mWifiUtils.WifiStartScan();
		mConfigList = mWifiUtils.getConfiguration();
	}

	private String getWifiConnectedInfo() {
		WifiInfo mWifiInfo = mWifiUtils.getConnectedInfo();
		SupplicantState connectSupplicantState = mWifiInfo.getSupplicantState();
		int mNetworkId = mWifiInfo.getNetworkId();
		String cntSSID;
		if (null != mWifiInfo.getSSID() && mWifiInfo.getSSID() != "") {
			cntSSID = mWifiInfo.getSSID().substring(1, (mWifiInfo.getSSID().length() - 1));
		} else
			cntSSID = mWifiInfo.getSSID();
		if (mNetworkId != -1) {

		}
		return cntSSID;
	}


}
