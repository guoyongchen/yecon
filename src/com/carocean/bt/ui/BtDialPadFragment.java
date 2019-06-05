package com.carocean.bt.ui;


import static android.constant.YeconConstants.ACTION_YECON_KEY_UP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.bt.view.TipDialog;
import com.carocean.utils.MarqueeTextView;
import com.carocean.vmedia.MediaActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BtDialPadFragment extends Fragment implements OnClickListener, OnItemClickListener, OnLongClickListener{
	private static final String TAG = "BtDialPadFragment";
	private View mView;

	StringBuffer mHistoryStr = new StringBuffer();
	MarqueeTextView mCallnum;
	ImageButton btn_max;
	LinearLayout Ly_call_pad;
	ListView lv_searchlist;
    HighlightAdapter mAdapter;

	public static ArrayList<HashMap<String, String>> lSearch  = new ArrayList<HashMap<String, String>>();
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView");

		if (null == mView) {
			mView = inflater.inflate(R.layout.bt_phonebook_call_fragment, container, false);
		}

		init();
		BTService.registerNotifyHandler(uihandler);
		return mView;
	}


	private Handler uihandler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(ACTION_YECON_KEY_UP)) {
		        	int keyCode = intent.getIntExtra("key_code", 0);
		            if (keyCode == KeyEvent.KEYCODE_YECON_PHONE_ON && BTUtils.mBluetooth.isHFPconnected()) {
						CharSequence number = mCallnum.getText();
						if (number != null) {
							BTUtils.mBluetooth.dial(number.toString());
						}
		            }
				}
			}
		}
	};
	
	public void onResume() {
		Log.e(TAG, "onResume");
		if (mView == null) {
			return;
		}
		if (!BTUtils.mBluetooth.isHFPconnected() && MediaActivity.mActivity.isdestpagebt()) {
			TipDialog.getInstance(BTService.getInstance(), R.string.bt_connectbt).show();
		}
		super.onResume();
	};
	
	private void init() {

		if (null != mView) {
			mCallnum = (MarqueeTextView) mView.findViewById(R.id.bt_callnum);
			btn_max = (ImageButton) mView.findViewById(R.id.phone_call_key_max_btn);
			lv_searchlist = (ListView) mView.findViewById(R.id.bt_call_searchlist);
			Ly_call_pad = (LinearLayout) mView.findViewById(R.id.bt_call_pad);

			((ImageButton) mView.findViewById(R.id.phone_call_key_1_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_2_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_3_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_4_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_5_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_6_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_7_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_8_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_9_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_star_hide)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_0_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_sharp_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_call_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_delete_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_max_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_min_btn)).setOnClickListener(this);
			((ImageButton) mView.findViewById(R.id.phone_call_key_backspace_btn)).setOnClickListener(this);

			((ImageButton) mView.findViewById(R.id.phone_call_key_0_btn)).setOnLongClickListener(this);

	        mAdapter = new HighlightAdapter(getActivity(), lSearch,
	                R.layout.bt_phonebook_fragment_list_item, new String[] {
	                        "item_phonebook_name", "item_phonebook_number" },
	                new int[] { R.id.bt_phonebook_item_name,
	                        R.id.bt_phonebook_item_num });

	        lv_searchlist.setAdapter(mAdapter);
	        lv_searchlist.setOnItemClickListener(this);
		}
	}
    class HighlightAdapter extends SimpleAdapter {
        private LayoutInflater mInflater;
		private int mSelectIdx;
        
        public HighlightAdapter(Context context,
                List<HashMap<String, String>> data, int resource,
                String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.mInflater = LayoutInflater.from(context);
			mSelectIdx = 0;
        }

		public void setSelect(int index){
			mSelectIdx = index;
		}
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.bt_phonebook_fragment_list_item, null);
                holder.name = (TextView)convertView.findViewById(R.id.bt_phonebook_item_name);
                holder.phone = (TextView)convertView.findViewById(R.id.bt_phonebook_item_num);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            
            String pbname = "";
            String pbnumber = "";
            try{
                pbname = lSearch.get(position).get("name");
                pbnumber = lSearch.get(position).get("num");
            }finally{
                ;
            }
            
            holder.name.setText(Integer.toString(position+1)+". "+pbname);
            holder.phone.setText(pbnumber);
            return convertView;
        }
        
        public final class ViewHolder {
            public TextView name;
            public TextView phone;
        }
    }
	public void onDestroy() {
		Log.i(TAG, "onDestroy");

		BTService.unregisterNotifyHandler(uihandler);
		super.onDestroy();
		
	}

    void searchphonebookforkey(String inputnum){
    	if (inputnum == null || inputnum.isEmpty() || Bluetooth.contactlist.isEmpty()) {
    		lSearch.clear();
    		mAdapter.notifyDataSetChanged();
			return;
		}
    	ArrayList<HashMap<String, String>> lSearch_source_phonebook_record  = new ArrayList<HashMap<String, String>>();
 //   	lSearch_source_phonebook_record.addAll(BtCallHistoryFragment.m_CallHistoryList);
    	lSearch_source_phonebook_record.addAll(Bluetooth.contactlist);
    	
		Log.e(TAG,
				"searchphonebookforkey lSearch_source_phonebook_record.size()="
						+ lSearch_source_phonebook_record.size()
						+ " lSearch.size()="
						+ lSearch.size()
						+ " BTUtils.mBluetooth.isPhonebookDownloadIdle()="
						+ BTUtils.mBluetooth.isdownloadcontactidle()
						+ " inputnum=" + inputnum);
		
    	if (lSearch_source_phonebook_record.isEmpty()) {
			return;
		}
    	if (!BTUtils.mBluetooth.isdownloadcontactidle()) {
			return;
		}
    	
    	lv_searchlist.setSelection(0);
    	
    	lSearch.clear();
    	String repeat_str = "";
    	for (HashMap<String, String> item : lSearch_source_phonebook_record) {
			String num = item.get("num");
			String pyheadtodialnum = item.get("pyheadtodialnum");
			String pytodialnum = item.get("pytodialnum");
			if (num == null) {
				num = "";
			}
			if (pyheadtodialnum == null) {
				pyheadtodialnum = "";
			}
			if (pytodialnum == null) {
				pytodialnum = "";
			}
			if (num.contains(inputnum) || pyheadtodialnum.contains(inputnum)) {
				lSearch.add(new HashMap<String, String>(item));
			}else{
				String formatpytodialnum = pytodialnum.replace(" ", "");
				if (formatpytodialnum.contains(inputnum)) {
					String c = inputnum.substring(0, 1);
					while(true){
						int index = pytodialnum.indexOf(" " + c);
						if (index != -1) {
							pytodialnum = pytodialnum.substring(index + 1);
							if (pytodialnum.replace(" ", "").contains(inputnum)) {
								lSearch.add(new HashMap<String, String>(item));
							}
						}else{
							break;
						}
					}
					
				}
			}
		}
    	mAdapter.notifyDataSetChanged();
    }
    
	@Override
	public void onClick(View view) {
		final int id = view.getId();

		switch (id) {
		case R.id.phone_call_key_1_btn:
			mHistoryStr.append("1");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_2_btn:
			mHistoryStr.append("2");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_3_btn:
			mHistoryStr.append("3");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_4_btn:
			mHistoryStr.append("4");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_5_btn:
			mHistoryStr.append("5");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_6_btn:
			mHistoryStr.append("6");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_7_btn:
			mHistoryStr.append("7");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_8_btn:
			mHistoryStr.append("8");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_9_btn:
			mHistoryStr.append("9");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_0_btn:
			mHistoryStr.append("0");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_star_hide:
			mHistoryStr.append("*");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_key_sharp_btn:
			mHistoryStr.append("#");
			searchphonebookforkey(mHistoryStr.toString());
			break;
		case R.id.phone_call_delete_btn:
			mHistoryStr.setLength(0);
			searchphonebookforkey(null);
			break;
		case R.id.phone_call_key_backspace_btn:
			final int len = mHistoryStr.length();
			if (len > 0) {
				mHistoryStr.deleteCharAt(len - 1);
				searchphonebookforkey(mHistoryStr.toString());
			}
			break;
		case R.id.phone_call_key_call_btn:
			if (null != mCallnum) {
				CharSequence number = mCallnum.getText();
				if (number != null) {
					BTUtils.mBluetooth.dial(number.toString());

					if (null != mHistoryStr && null != mCallnum) {
						mHistoryStr.delete(0, mHistoryStr.length());
					}
				}else if(!BTUtils.mBluetooth.lastcallnum.isEmpty()){
					mHistoryStr.append(BTUtils.mBluetooth.lastcallnum);
				}
			}
			break;
		case R.id.phone_call_key_max_btn:
			showCallpad(true);
			break;
		case R.id.phone_call_key_min_btn:
			showCallpad(false);
			break;
		default:
			break;
		}
		mCallnum.setText(mHistoryStr);

	}

	public void showCallpad(boolean show){
		if (show) {
			Ly_call_pad.setVisibility(View.VISIBLE);
			btn_max.setVisibility(View.GONE);
			 ViewGroup.LayoutParams params = lv_searchlist.getLayoutParams();
	         params.height = 128;
	         lv_searchlist.setLayoutParams(params);
		}else{
			Ly_call_pad.setVisibility(View.GONE);
			btn_max.setVisibility(View.VISIBLE);
			 ViewGroup.LayoutParams params = lv_searchlist.getLayoutParams();
	         params.height = 350;
	         lv_searchlist.setLayoutParams(params);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		HashMap<String, String> map = lSearch.get(arg2);
		String pbnumber =  map.get("num");
        if (pbnumber != null) {
			BTUtils.mBluetooth.dial(pbnumber);
		}
	}

	@Override
	public boolean onLongClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.phone_call_key_0_btn:
			mHistoryStr.append("+");
			searchphonebookforkey(mHistoryStr.toString());
			mCallnum.setText(mHistoryStr);
			return true;

		default:
			break;
		}
		return false;
	}
}
