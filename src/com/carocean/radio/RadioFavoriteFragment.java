package com.carocean.radio;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.radio.constants.RadioConstants;
import com.carocean.radio.constants.RadioFreqInfo;
import com.carocean.radio.constants.RadioMessage;
import com.carocean.radio.db.RadioStation;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RadioFavoriteFragment extends Fragment implements OnClickListener {
	private static final String TAG = "RadioFavoriteStationFragment";

	private Context mContext;
	private View mView;

	private FileListItemAdapter mItemAdapter;
	private ArrayList<RadioFreqInfo> fm_FreqList = null;
	private ArrayList<RadioFreqInfo> am_FreqList = null;
	private ArrayList<RadioFreqInfo> mFreqList = new ArrayList<RadioFreqInfo>();
	private RadioMessage mMsg = RadioMessage.getInstance();

	public void onAttach(Activity activity) {
		Log.i(TAG, "onAttach");
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");

		if (null == mView) {
			mView = inflater.inflate(R.layout.radio_favorite_fragment, container, false);
		}
		initData();
		initUI();
		return mView;
	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		if (null != mFreqList && !mFreqList.isEmpty()) {
			mFreqList.clear();
		}
		if (null != mItemAdapter) {
			mItemAdapter.clear();
			mItemAdapter = null;
		}
		if (null != mMsg && null != mHandler) {
			mMsg.unregisterMsgHandler(mHandler);
		}
	}

	private void initData() {
		if (null != mContext) {
			fm_FreqList = RadioStation.getAllFavoriteFreqInfo(mContext, RadioConstants.BAND_ID_FM);
			am_FreqList = RadioStation.getAllFavoriteFreqInfo(mContext, RadioConstants.BAND_ID_AM);
			mFreqList.addAll(fm_FreqList);
			mFreqList.addAll(am_FreqList);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					Log.e(TAG, "" + d.getFreq() + " - " + d.getBand() + " - " + d.getFavorite() + " " + d.getPreset());
				}
			}
		}
	}

	private void initUI() {

		if (null != mView && null != mContext) {
			if (null != mMsg && null != mHandler) {
				mMsg.registerMsgHandler(mHandler);
			}

			((ImageButton) mView.findViewById(R.id.radio_delete_favorite_btn)).setOnClickListener(this);

			if (null == mItemAdapter) {
				mItemAdapter = new FileListItemAdapter(mContext, R.layout.radio_favorite_list_item, mFreqList);
			}
			ListView listView = (ListView) mView.findViewById(R.id.radio_favorite_list);
			listView.setAdapter(mItemAdapter);
			listView.setOnItemClickListener(new FavoriteListOnItemClickListener());
		}
	}

	private class FavoriteListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			mItemAdapter.setSelectedPosition(position);

			if (!mFreqList.isEmpty() && null != mMsg) {
				Bundle bundle = new Bundle();
				bundle.putInt("freq", mFreqList.get(position).getFreq());
				bundle.putInt("band", mFreqList.get(position).getBand());
				mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ, bundle);
			}
		}
	}

	private class FileListItemAdapter extends ArrayAdapter<RadioFreqInfo> {
		private int layoutId = R.layout.radio_favorite_list_item;
		private View view;
		private ViewHolder viewHolder;

		public FileListItemAdapter(Context context, int layoutId, List<RadioFreqInfo> list) {
			super(context, layoutId, list);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			// 优化--数据重覆不加载
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.radio_band_list_bg = (LinearLayout) view.findViewById(R.id.radio_band_list_bg);
				viewHolder.mImagePlay = (ImageView) view.findViewById(R.id.favorite_freq_play_flag);
				viewHolder.mTextView = (TextView) view.findViewById(R.id.favorite_freq_info);
				viewHolder.m_radio_freq_uint = (TextView) view.findViewById(R.id.radio_freq_uint);
				viewHolder.mImageButton = (ImageButton) view.findViewById(R.id.favorite_freq_delete_btn);
				viewHolder.radio_list_status_play = (ImageView) view.findViewById(R.id.radio_list_status_play);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			if (null != viewHolder && null != viewHolder.mTextView && null != viewHolder.mImageButton
					&& null != viewHolder.radio_list_status_play) {

				viewHolder.mImageButton.setId(position);
				viewHolder.mImageButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						deleteData(v.getId());
					}
				});

				RadioFreqInfo data = getItem(position);
				if (null != data) {

					if (RadioStation.getCurrentFreq(mContext) == data.getFreq()) {
						viewHolder.radio_list_status_play
								.setImageDrawable(mContext.getResources().getDrawable(R.drawable.radio_play_animation));
						AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.radio_list_status_play
								.getDrawable();
						animationDrawable.start();
						viewHolder.radio_band_list_bg.setBackgroundResource(R.drawable.radio_list_item_dn);
						viewHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.bright_yellow));
						viewHolder.m_radio_freq_uint
								.setTextColor(mContext.getResources().getColor(R.color.bright_yellow));

					} else {
						viewHolder.radio_list_status_play.setImageDrawable(null);
						viewHolder.radio_band_list_bg.setBackgroundResource(R.drawable.radio_list_item_n);
						viewHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.white));
						viewHolder.m_radio_freq_uint.setTextColor(mContext.getResources().getColor(R.color.white));
					}

					if (data.getBand() > 2) {
						viewHolder.mTextView.setText(String.format("%d", data.getFreq()));
						viewHolder.m_radio_freq_uint.setText("KHz");
					} else {
						viewHolder.mTextView.setText(String.format("%.1f", data.getFreq() / 100.0f));
						viewHolder.m_radio_freq_uint.setText("MHz");
					}
				}
			}

			// if (position == selectedPosition) {
			// viewHolder.radio_band_list_bg.setBackgroundResource(R.drawable.radio_list_item_dn);
			// viewHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.bright_yellow));
			// viewHolder.m_radio_freq_uint.setTextColor(mContext.getResources().getColor(R.color.bright_yellow));
			// } else {
			// viewHolder.radio_band_list_bg.setBackgroundResource(R.drawable.radio_list_item_n);
			// viewHolder.mTextView.setTextColor(mContext.getResources().getColor(R.color.white));
			// viewHolder.m_radio_freq_uint.setTextColor(mContext.getResources().getColor(R.color.white));
			// }

			return view;
		}

		private class ViewHolder {
			LinearLayout radio_band_list_bg;
			ImageView mImagePlay;
			TextView mTextView;
			TextView m_radio_freq_uint;
			ImageButton mImageButton;
			ImageView radio_list_status_play;
		}

		private int selectedPosition = 0;

		public void setSelectedPosition(int selectedPosition) {
			this.selectedPosition = selectedPosition;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}
	}

	public class FavoriteDialog extends Dialog implements android.view.View.OnClickListener {

		public FavoriteDialog(Context context, int theme) {
			super(context, theme);
		}

		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.radio_favorite_dialog);

			((Button) findViewById(R.id.radio_favorite_dialog_no)).setOnClickListener(this);
			((Button) findViewById(R.id.radio_favorite_dialog_ok)).setOnClickListener(this);
		}

		public void onClick(View v) {
			if (v.getId() == R.id.radio_favorite_dialog_no) {
				dismiss();
			} else if (v.getId() == R.id.radio_favorite_dialog_ok) {
				deleteAllData();
				dismiss();
			}
		}
	}

	private void deleteData(int index) {
		if (null != mContext && !mFreqList.isEmpty() && null != mItemAdapter) {
			RadioStation.removeFavorite(mContext, mFreqList.get(index).getBand(), mFreqList.get(index).getFreq());
			mFreqList.remove(index);
			mItemAdapter.notifyDataSetChanged();
		}
	}

	private void deleteAllData() {
		if (null != mContext && !mFreqList.isEmpty() && null != mItemAdapter) {
			RadioStation.removeBandAllFavorite(mContext, RadioConstants.BAND_ID_FM);
			RadioStation.removeBandAllFavorite(mContext, RadioConstants.BAND_ID_AM);
			mFreqList.clear();
			mItemAdapter.clear();
			mItemAdapter.notifyDataSetChanged();
		}
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == RadioMessage.MSG_UPDATA_FREQ) {
				if (null != mItemAdapter) {
					mItemAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	public void onClick(View v) {
		if (v.getId() == R.id.radio_delete_favorite_btn) {
			if (!mFreqList.isEmpty()) {
				Dialog dialog = new FavoriteDialog(getActivity(), R.style.FavoriteDialog);
				if (null != dialog) {
					dialog.show();
				}
			}
		}
	}
}
