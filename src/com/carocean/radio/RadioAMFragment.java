package com.carocean.radio;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.radio.constants.RadioConstants;
import com.carocean.radio.constants.RadioFreqInfo;
import com.carocean.radio.constants.RadioMessage;
import com.carocean.radio.db.RadioStation;
import com.carocean.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import static com.carocean.radio.constants.RadioConstants.BAND_ID_AM;

public class RadioAMFragment extends Fragment implements OnClickListener {
	private static final String TAG = "RadioAMFragment";

	private View mView;
	private ArrayList<RadioFreqInfo> mFreqList = null;
	private FileListItemAdapter mItemAdapter;

	private Context mContext;
	private RadioMessage mMsg = RadioMessage.getInstance();

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
		mContext = activity.getApplicationContext();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");

		if (null == mView) {
			mView = inflater.inflate(R.layout.radio_band_fragment, container, false);
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
			mFreqList = RadioStation.getAllPresetFreqInfo(mContext, BAND_ID_AM);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					Log.i(TAG, "" + d.getFreq() + " - " + d.getBand() + " - " + d.getFavorite());
				}
			}
		}
	}

	private void initUI() {
		if (null != mView && null != mContext) {

			if (null != mMsg && null != mHandler) {
				mMsg.registerMsgHandler(mHandler);
			}

			((ImageButton) mView.findViewById(R.id.radio_band_updata_btn)).setOnClickListener(this);

			if (null == mItemAdapter) {
				mItemAdapter = new FileListItemAdapter(mContext, R.layout.radio_band_list_item, mFreqList);
			}
			ListView listView = (ListView) mView.findViewById(R.id.radio_band_list);
			listView.setAdapter(mItemAdapter);
			listView.setOnItemClickListener(new AmBandListOnItemClickListener());
		}
	}

	private class AmBandListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			mItemAdapter.setSelectedPosition(position);

			if (!mFreqList.isEmpty() && null != mMsg) {
				Bundle bundle = new Bundle();
				bundle.putInt("freq", mFreqList.get(position).getFreq());
				bundle.putInt("band", RadioConstants.BAND_ID_AM);
				mMsg.sendMsg(RadioMessage.MSG_RADIO_TO_MCU_SEND_FREQ, bundle);
			}
		}
	}

	private class FileListItemAdapter extends ArrayAdapter<RadioFreqInfo> {
		private int layoutId = R.layout.radio_band_list_item;
		private View view;
		private ViewHolder viewHolder;

		public FileListItemAdapter(Context context, int layoutId, List<RadioFreqInfo> list) {
			super(context, layoutId, list);
		}

		public void refreshData(int position) {
			mFreqList = RadioStation.getAllPresetFreqInfo(mContext, BAND_ID_AM);
			if (null != mItemAdapter) {
				mItemAdapter.clear();
				mItemAdapter.addAll(mFreqList);
				mItemAdapter.notifyDataSetChanged();
			}

		}

		@SuppressLint("ResourceAsColor")
		public View getView(final int position, View convertView, ViewGroup parent) {

			// 优化--数据重覆不加载
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.radio_band_list_bg = (LinearLayout) view.findViewById(R.id.radio_band_list_bg);
				viewHolder.mImagePlay = (ImageView) view.findViewById(R.id.radio_list_play_flag);
				viewHolder.mTextView = (TextView) view.findViewById(R.id.radio_list_info);
				viewHolder.m_radio_freq_uint = (TextView) view.findViewById(R.id.radio_freq_uint);
				viewHolder.mImageCollection = (ImageView) view.findViewById(R.id.radio_list_collection_flag);
				viewHolder.radio_list_status_play = (ImageView) view.findViewById(R.id.radio_list_status_play);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			if (null != viewHolder && null != viewHolder.mImagePlay && null != viewHolder.mTextView
					&& null != viewHolder.mImageCollection && null != viewHolder.radio_list_status_play) {

				RadioFreqInfo data = getItem(position);
				if (null != data) {

					int collectionId = R.drawable.radio_favorite_n;
					if (data.getFavorite() == 1) {
						collectionId = R.drawable.radio_favorite_p;
					}

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

					viewHolder.mTextView.setText(Integer.toString(data.getFreq()));
					viewHolder.m_radio_freq_uint.setText("KHz");
					viewHolder.mImageCollection.setImageResource(collectionId);
				}
			}

			viewHolder.mImageCollection.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					if (getItem(position).getFavorite() == 1) {
						RadioStation.removeFavorite(mContext, getItem(position).getBand(), getItem(position).getFreq());
					} else if (getItem(position).getFavorite() == 0) {
						ArrayList<RadioFreqInfo> mFavoriteList = RadioStation.getAllFavoriteFreqInfoList(mContext);
						if (null != mFavoriteList) {
							if (mFavoriteList.size() < 20) {
								RadioStation.addFavorite(mContext, getItem(position).getFreq());
							} else {
								Utils.showToast(mContext.getResources().getString(R.string.radio_favorite_toast));
							}
						}
					}

					refreshData(position);
				}
			});

			return view;
		}

		private class ViewHolder {
			LinearLayout radio_band_list_bg;
			ImageView mImagePlay;
			TextView mTextView;
			TextView m_radio_freq_uint;
			ImageView mImageCollection;
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

	public void onClick(View v) {
		if (v.getId() == R.id.radio_band_updata_btn) {
			RadioStation.removeAllPresetFreq(mContext, BAND_ID_AM);
			RadioMessage.getInstance().sendMsg(RadioMessage.MSG_RADIO_TO_MCU_AM_SCAN_START, null);
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
}
