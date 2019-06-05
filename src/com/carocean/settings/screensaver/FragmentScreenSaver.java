package com.carocean.settings.screensaver;

import com.carocean.R;
import com.carocean.settings.fragment.DialogFragmentBase;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("DrawAllocation")
@SuppressWarnings("deprecation")
public class FragmentScreenSaver extends DialogFragmentBase implements OnItemClickListener, View.OnClickListener {
	// private final static String TAG = "FragmentScreenSaver";
	private Context mContext;
	int ID_TextView[] = { R.id.dlg_back, R.id.dlg_confirm, };
	TextView[] mTextView = new TextView[ID_TextView.length];

	private TextView mTextViewName;
	private GalleryAdapter mGalleryAdapter;
	FragmentManager mFragmentManager;

	public FragmentScreenSaver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setStyle(DialogFragment.STYLE_NO_FRAME, 0);
		initData();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.setting_general_screensaver, container);
		initView(rootView);

		return rootView;
	}

	void initData() {
		mContext = getActivity();
	}

	void initView(View rootView) {
		for (int i = 0; i < ID_TextView.length; i++) {
			mTextView[i] = (TextView) rootView.findViewById(ID_TextView[i]);
			mTextView[i].setOnClickListener(this);
		}
		mTextViewName = (TextView) rootView.findViewById(R.id.name_pic);
		Gallery mGallery = (Gallery) rootView.findViewById(R.id.gallery1);
		mGalleryAdapter = new GalleryAdapter(getActivity());
		mGalleryAdapter.setSelectItem(scUtils.imageSelected);
		mGallery.setAdapter(mGalleryAdapter);
		mGallery.setOnItemClickListener(this);
		mGallery.setSelection(scUtils.imageSelected);
		mGallery.setUnselectedAlpha(0.8f);
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		// TODO Auto-generated method stub
		super.show(manager, tag);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.dlg_back:
			dismiss();
			break;
		case R.id.dlg_confirm:
			if (mOnItemSelectListener != null)
				mOnItemSelectListener.onSelectItem(scUtils.imageSelected);
			dismiss();
			break;
		default:
			break;
		}
	}

	class GalleryAdapter extends BaseAdapter {
		private Context mContext;
		private int selectItem;

		public GalleryAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return scUtils.imageIds.length;
		}

		@Override
		public Object getItem(int position) {
			return scUtils.imageIds[position];
		}

		@Override
		public long getItemId(int position) {
			mTextViewName.setText(scUtils.styles[position]);
			// this.selectItem = position;
			// notifyDataSetChanged();
			return position;
		}

		public void setSelectItem(int selectItem) {
			this.selectItem = selectItem;
			notifyDataSetChanged();
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView imageView = new ImageView(mContext);
			imageView.setImageResource(scUtils.imageIds[position]);

			// float h, w;
			// h = imageView.getDrawable().getIntrinsicHeight();
			// w = imageView.getDrawable().getIntrinsicWidth();
			// imageView.setLayoutParams(new Gallery.LayoutParams((int) (190.0 /
			// (h / w)), 190));

			if (selectItem == position) {
				imageView = new customImageView(mContext);
				imageView.setImageResource(scUtils.imageIds[position]);
			}

			imageView.setLayoutParams(new Gallery.LayoutParams(618, 300));
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			return imageView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		scUtils.imageSelected = arg2;
		mGalleryAdapter.setSelectItem(scUtils.imageSelected);
		// mGalleryAdapter.notifyDataSetInvalidated();
	}

	@SuppressLint("DrawAllocation")
	public class customImageView extends ImageView {

		public customImageView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public customImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public customImageView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			Rect rect = canvas.getClipBounds();
			Paint paint = new Paint();
			paint.setColor(Color.parseColor("#D10C18"));
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
			canvas.drawRect(rect, paint);
		}
	}

	private OnItemSelectListener mOnItemSelectListener;

	public void setOnItemSelectListener(OnItemSelectListener mListener) {
		mOnItemSelectListener = mListener;
	}

	// 定义dialog的回调事件
	public interface OnItemSelectListener {
		void onSelectItem(int index);
	}
}
