package com.carocean.bt.ui;

import static com.carocean.bt.Bluetooth.mContext;

import java.util.ArrayList;
import java.util.HashMap;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.bt.Profile;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.carocean.vmedia.bt.*;

public class BtSettingFragment extends Fragment implements PageBT.IKeyBoardVisibleListener {
	private static final String TAG = "BtSettingFragment";

	private View mView;
	private CheckBox mSwitch, mAnswer, bt_pairmod;
	private EditText mDeviceName;
	private LinearLayout bt_reset_btn;
	private LinearLayout mSearchDevice;
	private LinearLayout mLinearLayout;
	private LinearLayout mLayout_bt_switchbt;
	private TextView mTip_switchingbt, bt_timefound;
	private final int TYPE_PAIRED = 1;
	private final int TYPE_SEARCH = 2;
	private ImageView bt_searching;

	private FileListItemAdapter mAdapter_search, mAdapter_paired;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView");

		if (null == mView) {
			mView = inflater.inflate(R.layout.bt_settings_fragment, container, false);
		}

		init();
		return mView;
	}

	private void showpairedlist() {
		mAdapter_paired.notifyDataSetChanged();
	}

	public void onResume() {
		Log.e(TAG, "onResume");
		if (mView == null) {
			return;
		}
		super.onResume();
		BTUtils.mBluetooth.stopdiscovery();
		bt_pairmod.setChecked(BTUtils.mBluetooth.pair_mod);
		bt_timefound.setText("");
		clean_search();
		showpairedlist();

		inputHidden();
		flushui();
		mDeviceName.setCursorVisible(false);
		mDeviceName.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
					mDeviceName.setCursorVisible(true);
				}
				return false;
			}
		});

	}

	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		BTService.unregisterNotifyHandler(uihandler);
		BTUtils.mBluetooth.outpairmod();
		if (uihandler.hasCallbacks(rb_found)) {
			uihandler.removeCallbacks(rb_found);
		}
	}

	public void startSearchAnim() {
		bt_searching.setVisibility(View.VISIBLE);
		Animation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(500);
		anim.setRepeatCount(-1);
		bt_searching.startAnimation(anim);
	}

	public void stopSearchAnim() {
		bt_searching.setVisibility(View.GONE);
		bt_searching.clearAnimation();
	}

	private void init() {

		if (null != mView) {
			bt_searching = (ImageView) mView.findViewById(R.id.bt_searching);
			mTip_switchingbt = (TextView) mView.findViewById(R.id.bt_tip_switching);
			bt_timefound = (TextView) mView.findViewById(R.id.bt_timefound);
			mLayout_bt_switchbt = (LinearLayout) mView.findViewById(R.id.bt_tip_switchbt);
			mLayout_bt_switchbt.setVisibility(View.GONE);
			mSwitch = (CheckBox) mView.findViewById(R.id.bt_switch_btn);
			mAnswer = (CheckBox) mView.findViewById(R.id.bt_auto_btn);
			bt_pairmod = (CheckBox) mView.findViewById(R.id.bt_pairmod);
			mDeviceName = (EditText) mView.findViewById(R.id.bt_device_name);
			mSearchDevice = (LinearLayout) mView.findViewById(R.id.bt_search_device);
			bt_reset_btn = (LinearLayout) mView.findViewById(R.id.bt_reset_btn);
			mLinearLayout = (LinearLayout) mView.findViewById(R.id.bt_set_layout);
			mLinearLayout.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
						inputHidden();
					}
					return false;
				}
			});

			MyOnEditorActionListener editClick = new MyOnEditorActionListener();
			// mDeviceName.setOnEditorActionListener(editClick);
			// mDevicePin.setOnEditorActionListener(editClick);

			MyOnClickListener click = new MyOnClickListener();
			mSwitch.setOnClickListener(click);
			mAnswer.setOnClickListener(click);
			mSearchDevice.setOnClickListener(click);
			bt_reset_btn.setOnClickListener(click);
			bt_pairmod.setOnClickListener(click);

			mSearchDevice.setOnTouchListener(new OnTouchListener() {
				long pre = 0;
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					switch (arg1.getAction()) {
					case MotionEvent.ACTION_DOWN:
						pre = SystemClock.elapsedRealtime();
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (SystemClock.elapsedRealtime() - pre > 5000) {
							Toast.makeText(mContext, "bt version is " + Profile.version, Toast.LENGTH_LONG).show();
						}
						pre = 0;
						break;

					default:
						break;
					}
					return false;
				}
			});

			mAdapter_search = new FileListItemAdapter(mView.getContext(), R.layout.bt_settings_search_list_item, Bluetooth.searchlist, TYPE_SEARCH);
			ListView listView = (ListView) mView.findViewById(R.id.bt_searchlist);
			listView.setAdapter(mAdapter_search);
			listView.setOnItemClickListener(new BtSearchListOnItemClickListener());

			mAdapter_paired = new FileListItemAdapter(mView.getContext(), R.layout.bt_settings_paired_list_item, Bluetooth.pairedlist, TYPE_PAIRED);
			listView = (ListView) mView.findViewById(R.id.bt_pairedlist);
			listView.setAdapter(mAdapter_paired);
			listView.setOnItemClickListener(new BtPairedListOnItemClickListener());

		}

		BTService.registerNotifyHandler(uihandler);
	}

	private class MyOnEditorActionListener implements OnEditorActionListener {

		public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

			boolean ret = false;
			final int id = arg0.getId();

			if (arg1 == EditorInfo.IME_ACTION_DONE) {

				if (id == R.id.bt_device_name) {
					BTUtils.mBluetooth.setlocaldevicename(arg0.getText().toString());
					ret = true;
				} else if (id == R.id.bt_device_pin) {
					// BTUtils.mBluetooth.setDevicePin(arg0.getText().toString());
					ret = true;
				}

				if (ret) {
					inputHidden();
				}
			}

			return ret;
		}
	}

	private void inputHidden() {
		InputMethodManager in = (InputMethodManager) ApplicationManage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != in && null != mDeviceName) {
			in.hideSoftInputFromWindow(mDeviceName.getApplicationWindowToken(), 0);
		}
	}

	public void onPause() {
		BTUtils.mBluetooth.stopdiscovery();
		stopSearchAnim();
		super.onPause();
	};

	Runnable rb_found = new Runnable() {

		@Override
		public void run() {
			if (time_found == -1) {
				bt_pairmod.performClick();
				bt_timefound.setText("");
				return;
			}
			bt_timefound.setText("" + time_found + "s");
			time_found--;
			uihandler.postDelayed(rb_found, 1000);
		}
	};

	int time_found = 180;

	private class MyOnClickListener implements OnClickListener {

		public void onClick(View arg0) {

			final int id = arg0.getId();

			switch (id) {
			case R.id.bt_search_device:
				if (BTUtils.mBluetooth.isbtopened() && !BTUtils.mBluetooth.isconnected()) {
					BTUtils.mBluetooth.discovery();
				}
				break;
			case R.id.bt_switch_btn:
				BTUtils.mBluetooth.switchbt();
				flushui();
				break;
			case R.id.bt_auto_btn:
				BTUtils.mBluetooth.setautoanswer(!BTUtils.mBluetooth.isautoanswer());
				flushui();
				break;
			case R.id.bt_reset_btn:
				BTUtils.mBluetooth.resetbt();
				clean_search();
				clean_paired();
				flushui();
				break;
			case R.id.bt_pairmod:
				BTUtils.mBluetooth.switchpairmod();
				bt_pairmod.setChecked(BTUtils.mBluetooth.pair_mod);
				if (BTUtils.mBluetooth.pair_mod) {
					time_found = 180;
					uihandler.postDelayed(rb_found, 0);
				} else {
					bt_timefound.setText("");
					if (uihandler.hasCallbacks(rb_found)) {
						uihandler.removeCallbacks(rb_found);
					}
				}
				flushui();
				break;
			default:
				break;
			}
		}
	}

	private void flushui() {
		if (BTUtils.mBluetooth.isopeningbt()) {
			mLayout_bt_switchbt.setVisibility(View.VISIBLE);
			mTip_switchingbt.setText(R.string.bt_tip_openingbt);
		}else if (BTUtils.mBluetooth.iscloseingbt()) {
			mLayout_bt_switchbt.setVisibility(View.VISIBLE);
			mTip_switchingbt.setText(R.string.bt_tip_closingbt);
		}else{
			mLayout_bt_switchbt.setVisibility(View.GONE);
		}
		
		if (BTUtils.mBluetooth.isbtopened()) {
			mAnswer.setEnabled(true);
			bt_pairmod.setEnabled(true);
			bt_reset_btn.setEnabled(true);
			mDeviceName.setEnabled(true);
			mSearchDevice.setEnabled(true);
		} else {
			mAnswer.setEnabled(false);
			bt_pairmod.setEnabled(false);
			bt_reset_btn.setEnabled(false);
			mDeviceName.setEnabled(false);
			mSearchDevice.setEnabled(false);
		}

		mAnswer.setChecked(BTUtils.mBluetooth.isautoanswer());

		mDeviceName.setText(BTUtils.mBluetooth.devicename);

		if (!BTUtils.mBluetooth.isbtopened() || !BTUtils.mBluetooth.pair_mod) {
			if (uihandler.hasCallbacks(rb_found)) {
				uihandler.removeCallbacks(rb_found);
			}
			bt_timefound.setText("");
			bt_pairmod.setChecked(false);
		} else if (BTUtils.mBluetooth.pair_mod && !uihandler.hasCallbacks(rb_found)) {
			time_found = 180;
			uihandler.postDelayed(rb_found, 0);
			bt_pairmod.setChecked(true);
		} else if (BTUtils.mBluetooth.pair_mod) {
			bt_pairmod.setChecked(true);
		} else {
			bt_pairmod.setChecked(false);
		}
		
		mAdapter_paired.notifyDataSetChanged();
		mAdapter_search.notifyDataSetChanged();
	}


	public synchronized void handleDeviceFound(Intent intent) {
		BluetoothDevice deviceData = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		addSearchData(deviceData);
	}

	private Handler uihandler = new Handler() {

		public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(BTService.ACTION_DEVICE_NAME) || action.equals(BTService.ACTION_DEVICE_FOUND)
						|| action.equals(BTService.ACTION_DISCOVERY_START) || action.equals(BTService.ACTION_DISCOVERY_END)
						|| action.equals(BTService.ACTION_PAIREDLIST) || action.equals(BTService.ACTION_DISCONNECTED) || action.equals(BTService.ACTION_BTSTATE)) {
					flushui();
				}
			}
		}
	};

	private class BtSearchListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (!Bluetooth.searchlist.isEmpty()) {

				if (BTUtils.mBluetooth.isconnected()) {
					return;
				} else {
					if (BTUtils.mBluetooth.isbtopened()) {
						final String address = Bluetooth.searchlist.get(position).get("mac");
						BTUtils.mBluetooth.connect(address);
					}
				}
			}
		}
	}

	private class BtPairedListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (position < Bluetooth.pairedlist.size()) {
				HashMap<String, String> item = Bluetooth.pairedlist.get(position);
				String mac = item.get("mac");
				Log.e(TAG, "click mac=" + mac);
				if (BTUtils.mBluetooth.isconnected()) {
					if (BTUtils.mBluetooth.isaddrconnected(mac)) {
						BTUtils.mBluetooth.disconnect();
						return;
					} else {
						return;
					}
				} else {
					BTUtils.mBluetooth.connect(mac);
				}
			}
		}
	}

	private class FileListItemAdapter extends ArrayAdapter<HashMap<String, String>> {
		private int layoutId = R.layout.bt_settings_search_list_item;
		private View view;
		private ViewHolder viewHolder;
		private int type = 0;

		public FileListItemAdapter(Context context, int layoutId, ArrayList<HashMap<String, String>> list, int type) {
			super(context, layoutId, list);
			this.layoutId = layoutId;
			this.type = type;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			// 优化--数据重覆不加载
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.mImageView = (ImageView) view.findViewById(R.id.bt_icon);
				viewHolder.mTextView = (TextView) view.findViewById(R.id.bt_name);
				if (type == TYPE_PAIRED) {
					viewHolder.del = (Button) view.findViewById(R.id.bt_del);
				}
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			if (null != viewHolder && null != viewHolder.mImageView && null != viewHolder.mTextView) {

				HashMap<String, String> data = getItem(position);
				int color = R.color.white;
				if (null != data) {
					if (type == TYPE_PAIRED) {
						int id = R.drawable.bt_link_up;
						if (BTUtils.mBluetooth.isaddrconnected(data.get("mac"))) {
							id = R.drawable.bt_link_dn;
							color = R.color.bt_connected_color;
						} else {
							color = R.color.white;
							viewHolder.del.setTag(data.get("mac"));
							viewHolder.del.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									String mac = (String) arg0.getTag();
									if (BTUtils.mBluetooth.isaddrconnected(mac)) {
										return;
									}
									BTUtils.mBluetooth.delpair(mac);
									del_pairlist(mac);
								}
							});
						}
						viewHolder.mImageView.setBackgroundResource(id);
						viewHolder.mTextView.setTextColor(BTService.getInstance().getResources().getColor(color));
					}

					final String str = data.get("name");
					if (null != str) {
						viewHolder.mTextView.setText(str);
					}
				}
			}

			return view;
		}

		private class ViewHolder {
			ImageView mImageView;
			TextView mTextView;
			Button del;
		}
	}

	private void addSearchData(BluetoothDevice deviceData) {
		if (null != deviceData) {
			String address = deviceData.getAddress();
			String name = deviceData.getName();

			for (HashMap<String, String> item : Bluetooth.pairedlist) {
				if (item.get("mac").equals(address)) {
					return;
				}
			}

			for (int i = 0; i < Bluetooth.searchlist.size(); i++) {
				if (Bluetooth.searchlist.get(i).get("mac").equals(address)) {
					Bluetooth.searchlist.get(i).put("name", name);
					mAdapter_search.notifyDataSetChanged();
					return;
				}
			}
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("name", name);
			data.put("mac", address);
			add_searchlist(data);
		}
	}

	private void clean_search() {
		Bluetooth.searchlist.clear();
		mAdapter_search.notifyDataSetChanged();
	}

	private void del_pairlist(String mac) {
		for (HashMap<String, String> item : Bluetooth.pairedlist) {
			if (mac.equals(item.get("mac"))) {
				Bluetooth.pairedlist.remove(item);
				break;
			}
		}
		mAdapter_paired.notifyDataSetChanged();
	}

	private void clean_paired() {
		if (BTUtils.mBluetooth.isbtopened()) {
			for (HashMap<String, String> item : Bluetooth.pairedlist) {
				String mac = item.get("mac");
				BTUtils.mBluetooth.delpair(mac);
			}
		}
		Bluetooth.pairedlist.clear();
		mAdapter_paired.notifyDataSetChanged();
	}

	private void add_searchlist(HashMap<String, String> data) {
		Bluetooth.searchlist.add(data);
		mAdapter_search.notifyDataSetChanged();
	}

	public void setdeviceinfo() {
		String curname = mDeviceName.getText().toString();
		if (curname.isEmpty()) {
			mDeviceName.setText(BTUtils.mBluetooth.devicename);
		} else {
			BTUtils.mBluetooth.setlocaldevicename(curname);
		}

		Log.e(TAG, "setdeviceinfo curname=" + curname);
	}

	@Override
	public void onSoftKeyBoardVisible(boolean visible, int windowBottom) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onSoftKeyBoardVisible visible=" + visible);
		if (mDeviceName == null) {
			return;
		}
		if (visible) {
			mDeviceName.setCursorVisible(true);
			mDeviceName.setSelection(mDeviceName.getText().length());
			return;
		}
		mDeviceName.setCursorVisible(false);
		setdeviceinfo();
	}
}
