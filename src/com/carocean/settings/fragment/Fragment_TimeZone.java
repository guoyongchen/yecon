package com.carocean.settings.fragment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.carocean.R;
import com.carocean.settings.utils.TimeZoneSet;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Fragment_TimeZone extends DialogFragmentBase implements OnClickListener, OnItemClickListener {
	private Context mContext;
	private ListView mTimeZoneListView;

	private TimeZoneSet mTimeZoneSet;
	private SimpleAdapter mTimezoneSortedAdapter;
	private SimpleAdapter mAlphabeticalAdapter;
	private SimpleAdapter mAdapter;
	private int mCurrentIndex;

	int ID_TextView[] = { R.id.dlg_back, R.id.dlg_ok, R.id.dlg_cancle };
	TextView[] mTextView = new TextView[ID_TextView.length];

	public Fragment_TimeZone(int timezoneIndex) {
		mCurrentIndex = timezoneIndex;
	}

	public Fragment_TimeZone(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	private void initData() {
		mContext = getActivity();
		mTimeZoneSet = TimeZoneSet.getInstance(mContext);
		mTimezoneSortedAdapter = constructTimezoneAdapter(mContext, false);
		mAlphabeticalAdapter = constructTimezoneAdapter(mContext, true);
	}

	private void initView(View mView, Context context) {
		for (int i = 0; i < ID_TextView.length; i++) {
			mTextView[i] = (TextView) mView.findViewById(ID_TextView[i]);
			mTextView[i].setOnClickListener(this);
		}

		mTimeZoneListView = (ListView) mView.findViewById(R.id.id_listView);
		mTimeZoneListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		setSorting(true);
		mTimeZoneListView.setOnItemClickListener(this);
	}

	private void setWindowPara() {
		int width = getResources().getDimensionPixelSize(R.dimen.setting_dlg_w2);
		int height = ViewGroup.LayoutParams.MATCH_PARENT;

		Window window = getDialog().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.gravity = Gravity.CENTER;
		lp.width = width;
		lp.height = height;
		lp.dimAmount = 0.8f;
		window.setAttributes(lp);
		getDialog().setCanceledOnTouchOutside(true);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setWindowPara();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.base_dialog);
		initData();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.setting_timezone_layout, container);
		initView(rootView, mContext);
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		// Update the system timezone value
		setSelectItem(position);
		mAdapter.notifyDataSetInvalidated();
	}

	public void setSelectItem(int select) {
		mCurrentIndex = select;
	}

	private void setSorting(boolean sortByTimezone) {
		/* final SimpleAdapter */
		mAdapter = sortByTimezone ? mTimezoneSortedAdapter : mAlphabeticalAdapter;
		mTimeZoneListView.setAdapter(mAdapter);
		// defaultIndex = getTimeZoneIndex(sortedList, TimeZone.getDefault());
		if (mCurrentIndex >= 0) {
			mTimeZoneListView.setSelection(mCurrentIndex);
			// mTimeZoneListView.setSelectionFromTop(defaultIndex, 130);
		}
	}

	public SimpleAdapter constructTimezoneAdapter(Context context, boolean sortedByName) {
		return constructTimezoneAdapter(context, sortedByName, R.layout.setting_timezone_setup_custom_list_item_2);
	}

	/**
	 * Constructs an adapter with TimeZone list. Sorted by TimeZone in default.
	 * 
	 * @param sortedByName
	 *            use Name for sorting the list.
	 */

	@SuppressLint("ViewHolder")
	public SimpleAdapter constructTimezoneAdapter(Context context, boolean sortedByName, final int layoutId) {
		final String[] from = new String[] { TimeZoneSet.KEY_DISPLAYNAME, TimeZoneSet.KEY_GMT };
		final int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		final String sortKey = (sortedByName ? TimeZoneSet.KEY_DISPLAYNAME : TimeZoneSet.KEY_OFFSET);

		final TimeZoneSet.MyComparator comparator = new TimeZoneSet.MyComparator(sortKey);
		final List<HashMap<String, Object>> sortedList = mTimeZoneSet.getZones(context);
		Collections.sort(sortedList, comparator);

		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final SimpleAdapter adapter = new SimpleAdapter(context, sortedList, layoutId, from, to) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = inflater.inflate(layoutId, parent, false);
				TextView text1 = (TextView) view.findViewById(to[0]);
				TextView text2 = (TextView) view.findViewById(to[1]);

				final Map<?, ?> map = (Map<?, ?>) sortedList.get(position);
				final String displayname = (String) map.get(TimeZoneSet.KEY_DISPLAYNAME);
				final String gmt = (String) map.get(TimeZoneSet.KEY_GMT);
				text1.setText(displayname);
				text2.setText(gmt);

				int color;
				if (mCurrentIndex == position) {
					// view.setBackgroundResource(R.drawable.listbk_down);
					color = mContext.getResources().getColor(R.color.white);
					text1.setTextColor(color);
					text2.setTextColor(color);
				} else {
					// view.setBackgroundResource(R.drawable.selector_listview_state);
					// ColorStateList csl = (ColorStateList)
					// mContext.getResources().getColorStateList(R.color.general_color_selector_text);
					color = mContext.getResources().getColor(R.color.darker_gray);
					text1.setTextColor(color);
					text2.setTextColor(color);
				}
				return view;
			};
		};
		return adapter;
	}

	private OnItemClickListener mOnItemClickListener;

	public void setOnItemClickListener(OnItemClickListener mListener) {
		mOnItemClickListener = mListener;
	}

	// 定义dialog的回调事件
	public interface OnItemClickListener {
		void onClickItem(String str);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.dlg_back:
			dismiss();
			break;
		case R.id.dlg_ok:
			final Map<?, ?> map = (Map<?, ?>) mTimeZoneListView.getItemAtPosition(mCurrentIndex);
			final String tzId = (String) map.get(TimeZoneSet.KEY_ID);
			if (mOnItemClickListener != null)
				mOnItemClickListener.onClickItem(tzId);
			dismiss();
			break;
		case R.id.dlg_cancle:
			dismiss();
			break;
		}
	}

}
