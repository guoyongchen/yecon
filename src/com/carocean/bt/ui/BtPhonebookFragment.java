package com.carocean.bt.ui;

import java.util.HashMap;
import java.util.List;

import com.carocean.R;
import com.carocean.bt.BTService;
import com.carocean.bt.BTUtils;
import com.carocean.bt.Bluetooth;
import com.carocean.bt.view.MyLetterView;
import com.carocean.bt.view.OkDialog;
import com.carocean.bt.view.TipDialog;
import com.carocean.bt.view.MyLetterView.OnTouchingLetterChangedListener;
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
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BtPhonebookFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener {
	private static final String TAG = "BtPhonebookFragment";

	private View mView;
	private LinearLayout mDownLayout;
	private FrameLayout mPhoneBookLayout;
	private TextView mDownText, tv_c;
	private TextView mTipphonebookisempty;
	private ImageButton mUpdataBtn;
	private ImageButton mDelBtn;
	private MyLetterView mLetter;
	private ListView mPhonebookListView;
	public static boolean isneedtip = true;

	public static boolean bDownloadFail = false;
	private HighlightAdapter mAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		if (null == mView) {
			mView = inflater.inflate(R.layout.bt_phonebook_fragment, container, false);
		}

		init();
		return mView;
	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		BTService.unregisterNotifyHandler(uihandler);

		if (null != mDownLayout && null != mDownText && !BTUtils.mBluetooth.isdownloadcontactidle()) {
			mDownLayout.setVisibility(View.GONE);
			mDownText.setText(getString(R.string.bt_downloading, 0));
		}

		super.onDestroy();
	}

	@Override
	public void onPause() {
		tv_c.setVisibility(View.GONE);
		super.onPause();
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
				OkDialog.getInstance(BTService.getInstance(), R.string.bt_tip_downloadphonebook, new IOKListener() {

					@Override
					public void OnOk(boolean ok) {
						BTUtils.mBluetooth.loadcontact();
					}
				}).show();
			}
		}
		super.onResume();
	}

	Runnable rb_hidec = new Runnable() {

		@Override
		public void run() {
			tv_c.setVisibility(View.GONE);
		}
	};

	private void init() {
		if (null != mView) {
			mDownLayout = (LinearLayout) mView.findViewById(R.id.download_phonebook_layout);
			mPhoneBookLayout = (FrameLayout) mView.findViewById(R.id.phone_book_layout);
			mUpdataBtn = (ImageButton) mView.findViewById(R.id.phone_book_updata_btn);
			mDelBtn = (ImageButton) mView.findViewById(R.id.phone_book_delete_btn);
			mDownText = (TextView) mView.findViewById(R.id.tv_download_phonebook_text);
			tv_c = (TextView) mView.findViewById(R.id.tv_c);
			mLetter = (MyLetterView) mView.findViewById(R.id.phone_book_letter_view);
			mPhonebookListView = (ListView) mView.findViewById(R.id.bt_phone_book_list);
			mTipphonebookisempty = (TextView) mView.findViewById(R.id.tv_tip_phonebookisempty);

			mAdapter = new HighlightAdapter(getActivity(), Bluetooth.contactlist, R.layout.bt_phonebook_fragment_list_item, new String[] { "item_phonebook_name",
					"item_phonebook_number" }, new int[] { R.id.bt_phonebook_item_name, R.id.bt_phonebook_item_num });

			mPhonebookListView.setAdapter(mAdapter);
			mPhonebookListView.setOnItemClickListener(this);
			mPhonebookListView.setOnScrollListener(this);

			mLetter.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
				public void onTouchingLetterChanged(String s) {
					int index = 0;
					for (HashMap<String, String> d : Bluetooth.contactlist) {
						if (d.get("pyheadflag").equals(s.toUpperCase())) {
							mPhonebookListView.setSelection(index);
							tv_c.setText(s);
							tv_c.setVisibility(View.VISIBLE);
							if (uihandler.hasCallbacks(rb_hidec)) {
								uihandler.removeCallbacks(rb_hidec);
							}
							uihandler.postDelayed(rb_hidec, 2000);
							return;
						}
						index++;
					}
				}
			});

			mUpdataBtn.setOnClickListener(this);
			mDelBtn.setOnClickListener(this);

			BTService.registerNotifyHandler(uihandler);

			flushui();
		}
	}

	private Handler uihandler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(BTService.ACTION_CONNECTED) || action.equals(BTService.ACTION_DISCONNECTED) || action.equals(BTService.ACTION_CONTACT)) {
					flushui();
				}else if (action.equals(BTService.ACTION_DOWNLOAD_STATE) && intent.getStringExtra(BTService.EXTRA_PATH).equals("contact")) {
					flushui();
				}
			}
		}
	};

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
				convertView = mInflater.inflate(R.layout.bt_phonebook_fragment_list_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.bt_phonebook_item_name);
				holder.phone = (TextView) convertView.findViewById(R.id.bt_phonebook_item_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String pbname = "";
			String pbnumber = "";
			try {
				pbname = Bluetooth.contactlist.get(position).get("name");
				pbnumber = Bluetooth.contactlist.get(position).get("num");
			} finally {
				;
			}

			holder.name.setText(pbname);
			holder.phone.setText(pbnumber);
			return convertView;
		}

		public final class ViewHolder {
			public TextView name;
			public TextView phone;
		}
	}

	public void flushui() {
		if (!BTUtils.mBluetooth.isHFPconnected()) {
			mPhoneBookLayout.setVisibility(View.GONE);
			mDownLayout.setVisibility(View.GONE);
			mTipphonebookisempty.setVisibility(View.GONE);
		} else if (!BTUtils.mBluetooth.isdownloadcontactidle()) {
			mPhoneBookLayout.setVisibility(View.GONE);
			mDownLayout.setVisibility(View.VISIBLE);
			mTipphonebookisempty.setVisibility(View.GONE);
		} else if (!Bluetooth.contactlist.isEmpty()) {
			mPhoneBookLayout.setVisibility(View.VISIBLE);
			mDownLayout.setVisibility(View.GONE);
			mTipphonebookisempty.setVisibility(View.GONE);
		} else {
			mTipphonebookisempty.setVisibility(View.VISIBLE);
			mPhoneBookLayout.setVisibility(View.GONE);
			mDownLayout.setVisibility(View.GONE);
		}
		mDownText.setText(getString(R.string.bt_downloading, Bluetooth.contactlist.size()));
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		HashMap<String, String> map = Bluetooth.contactlist.get(arg2);
		String pbnumber = map.get("num");
		if (pbnumber != null) {
			BTUtils.mBluetooth.dial(pbnumber);
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) {
		case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
			int position = mPhonebookListView.getFirstVisiblePosition();
			if (Bluetooth.contactlist.size() > position) {
				mLetter.setchoose(Bluetooth.contactlist.get(position).get("pyheadflag"));

			}
			break;

		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.phone_book_updata_btn:
			if (BTUtils.mBluetooth.isdownloadcontactidle() && Bluetooth.contactlist.isEmpty()) {
				OkDialog.getInstance(getActivity(), R.string.bt_tip_downloadphonebook, new IOKListener() {

					@Override
					public void OnOk(boolean ok) {
						BTUtils.mBluetooth.loadcontact();
					}
				}).show();
			}
			break;
		case R.id.phone_book_delete_btn:
			if (BTUtils.mBluetooth.isdownloadcontactidle() && !Bluetooth.contactlist.isEmpty()) {
				OkDialog.getInstance(BTService.getInstance(), R.string.bt_tip_del_phonebook, new IOKListener() {

					@Override
					public void OnOk(boolean ok) {
						if (!BTUtils.mBluetooth.bsortcontactdata) {
							Bluetooth.contactlist.clear();
							mAdapter.notifyDataSetChanged();
							mPhoneBookLayout.setVisibility(View.GONE);
						}
					}
				}).show();
			}
			break;

		default:
			break;
		}
	}
}
