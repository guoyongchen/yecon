package com.carocean.bt.ui;

import java.util.HashMap;
import java.util.List;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.bt.ContactDB;
import com.carocean.bt.view.OkDialog;
import com.carocean.bt.view.TipDialog;
import com.carocean.bt.view.OkDialog.IOKListener;
import com.carocean.vmedia.MediaActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class BtCallHistoryFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener {
	private static final String TAG = "BtCallHistoryFragment";

	private View mView;
	private LinearLayout mDownLayout, mCallHistoyLayout;
	private LinearLayout mDelLayout;
	private TextView mDownText;
	private TextView mTiprecordisempty;
	private ImageButton mUpdataBtn;
	private ImageButton mDelBtn;
	private Button mOKBtn;
	private Button mCancelBtn;

	private HighlightAdapter mAdapter;
	private ListView mCallHistoryListView;
	public static boolean isneedtip = true;

	public static boolean bDownloadFail = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");

		if (null == mView) {
			mView = inflater.inflate(R.layout.bt_callhistory_fragment, container, false);
		}

		init();

		return mView;
	}

	@Override
	public void onResume() {
		Log.e(TAG, "onResume");

		if (mView == null) {
			return;
		}
		if (MediaActivity.mActivity.isdestpagebt()) {
			if (!BTUtils.mBluetooth.isHFPconnected()) {
				TipDialog.getInstance(BTService.getInstance(), R.string.bt_connectbt).show();
			} else if (isneedtip) {
				isneedtip = false;
				OkDialog.getInstance(BTService.getInstance(), R.string.bt_tip_downloadrecord, new IOKListener() {

					@Override
					public void OnOk(boolean ok) {
						// BTUtils.mBluetooth.needdownloadphonebook = true;
						BTUtils.mBluetooth.loadrecord();
					}
				}).show();
			}
		}
		super.onResume();

	}

	private void init() {
		if (null != mView) {

			mDownLayout = (LinearLayout) mView.findViewById(R.id.download_callhistory_layout);
			mDelLayout = (LinearLayout) mView.findViewById(R.id.bt_del_callhistory_layout);
			mCallHistoyLayout = (LinearLayout) mView.findViewById(R.id.callhistory_layout);
			mDelBtn = (ImageButton) mView.findViewById(R.id.call_history_delete_btn);
			mOKBtn = (Button) mView.findViewById(R.id.bt_OK);
			mCancelBtn = (Button) mView.findViewById(R.id.bt_Cancel);
			mDownText = (TextView) mView.findViewById(R.id.tv_download_callhistory_text);
			mUpdataBtn = (ImageButton) mView.findViewById(R.id.call_history_updata_btn);
			mCallHistoryListView = (ListView) mView.findViewById(R.id.bt_call_history_list);
			mTiprecordisempty = (TextView) mView.findViewById(R.id.tv_tip_recordisempty);

			mAdapter = new HighlightAdapter(getActivity(), Bluetooth.recordlist, R.layout.bt_callhistory_fragment_list_item, new String[] {
					"item_history_name", "item_history_formattime" }, new int[] { R.id.claa_name, R.id.claa_date });

			mCallHistoryListView.setAdapter(mAdapter);
			mCallHistoryListView.setOnItemClickListener(this);

			mUpdataBtn.setOnClickListener(this);
			mDelBtn.setOnClickListener(this);
			mOKBtn.setOnClickListener(this);
			mCancelBtn.setOnClickListener(this);

			BTService.registerNotifyHandler(uihandler);
			flushui();
		}
	}

	public void flushui() {
		if (!BTUtils.mBluetooth.isHFPconnected()) {
			mTiprecordisempty.setVisibility(View.GONE);
			mDownLayout.setVisibility(View.GONE);
			mCallHistoyLayout.setVisibility(View.GONE);
		} else if (!BTUtils.mBluetooth.isdownloadrecordidle()) {
			mDownLayout.setVisibility(View.VISIBLE);
			mCallHistoyLayout.setVisibility(View.GONE);
			mTiprecordisempty.setVisibility(View.GONE);
		} else if (Bluetooth.recordlist.isEmpty()) {
			mTiprecordisempty.setVisibility(View.VISIBLE);
			mDownLayout.setVisibility(View.GONE);
			mCallHistoyLayout.setVisibility(View.GONE);
		} else {
			mTiprecordisempty.setVisibility(View.GONE);
			mDownLayout.setVisibility(View.GONE);
			mCallHistoyLayout.setVisibility(View.VISIBLE);
		}
		mDownText.setText(getString(R.string.bt_downloading, Bluetooth.recordlist.size()));
		mAdapter.notifyDataSetChanged();
	}

	class HighlightAdapter extends SimpleAdapter {
		private LayoutInflater mInflater;
		private int mSelectIdx;

		public HighlightAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.mInflater = LayoutInflater.from(context);
			mSelectIdx = 0;
		}

		public void setSelect(int index) {
			mSelectIdx = index;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.bt_callhistory_fragment_list_item, null);
				holder.type = (ImageView) convertView.findViewById(R.id.claa_type);
				holder.name = (TextView) convertView.findViewById(R.id.claa_name);
				holder.phone = (TextView) convertView.findViewById(R.id.claa_date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String pbname = "";
			String pbnumber = "";
			String pbtime = "";
			pbname = Bluetooth.recordlist.get(position).get("name");
			pbtime = Bluetooth.recordlist.get(position).get("time_f");
			pbnumber = Bluetooth.recordlist.get(position).get("num");

			String type = Bluetooth.recordlist.get(position).get("type");
			int id = R.drawable.bt_calltype_callout;
			if (type.equals(ContactDB.TYPE_CALLIN)) {
				id = R.drawable.bt_calltype_callin;
			}else if (type.equals(ContactDB.TYPE_MISSED)) {
				id = R.drawable.bt_calltype_callmiss;
			}
			holder.type.setImageDrawable(Bluetooth.mContext.getResources().getDrawable(id));
			if (pbname == null || pbname.isEmpty()) {
				pbname = pbnumber;
			}
			holder.name.setText(pbname);
			holder.phone.setText(pbtime);
			return convertView;
		}

		public final class ViewHolder {
			public ImageView type;
			public TextView name;
			public TextView phone;
		}
	}

	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		BTService.unregisterNotifyHandler(uihandler);
	}

	private Handler uihandler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();

				if (action.equals(BTService.ACTION_CONNECTED) || action.equals(BTService.ACTION_DISCONNECTED) || action.equals(BTService.ACTION_RECORD)) {
					flushui();
				} else if (action.equals(BTService.ACTION_DOWNLOAD_STATE) && intent.getStringExtra(BTService.EXTRA_PATH).equals("record")) {
					flushui();
				}
			}
		}
	};

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.call_history_updata_btn:
			if (BTUtils.mBluetooth.isdownloadrecordidle() && Bluetooth.recordlist.isEmpty()) {
				OkDialog.getInstance(getActivity(), R.string.bt_tip_downloadrecord, new IOKListener() {

					@Override
					public void OnOk(boolean ok) {
						BTUtils.mBluetooth.loadrecord();
					}
				}).show();
			}
			break;
		case R.id.call_history_delete_btn:
			if (BTUtils.mBluetooth.isdownloadrecordidle() && !Bluetooth.recordlist.isEmpty()) {
				mDelLayout.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.bt_OK:
			if (!Bluetooth.bsortrecorddata) {
				Bluetooth.recordlist.clear();
				flushui();
			}
			mDelLayout.setVisibility(View.GONE);
			break;
		case R.id.bt_Cancel:
			mDelLayout.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		HashMap<String, String> map = Bluetooth.recordlist.get(arg2);
		String pbnumber = map.get("num");
		if (pbnumber != null) {
			BTUtils.mBluetooth.dial(pbnumber);
		}
	}
}
