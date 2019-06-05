package com.carocean.can;

import com.carocean.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class CanPageSimpleItem extends LinearLayout implements OnClickListener,OnCheckedChangeListener {
	private int mTextViewIds[] = { R.id.can_item_sub_title, R.id.can_item_content, R.id.can_item_textview,
			R.id.can_item_textview, };

	private int mCurrentIndex = -1;
	private View mParentView;
	private Switch mSwitch;
	private OnStatusChangeListener mOnStatusChangeListener = null;

	public CanPageSimpleItem(Context context) {
		super(context);
	}

	public CanPageSimpleItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void onFinishInflate() {
		View v = findViewById(R.id.can_page_item_content_rect);

		if (null != v) {
			v.setOnClickListener(this);
		}
		mParentView = v;
		
		mSwitch = (Switch)findViewById(R.id.can_item_switch);
		
		if(null != mSwitch) {
			mSwitch.setOnCheckedChangeListener(this);
		}
	}

	public void setProperty(int arrays[]) {
		mCurrentIndex = mOnStatusChangeListener.allocateIndex();
		for (int i = 0; i < arrays.length - 1; i++) {
			TextView titleView = (TextView) findViewById(mTextViewIds[i]);

			if (null != titleView) {
				if (0 != arrays[i]) {
					titleView.setText(arrays[i]);

					if (R.id.can_item_textview == mTextViewIds[i]) {
						View v = findViewById(R.id.can_item_switch);

						if (null != v) {
							v.setVisibility(View.GONE);
						}
					} else if (R.id.can_item_textview == mTextViewIds[i]) {
						View v = findViewById(R.id.can_item_switch);

						if (null != v) {
							v.setVisibility(View.GONE);
						}
					}
				} else {
					titleView.setVisibility(View.INVISIBLE);
				}
			}
		}

		int drawableId = arrays[arrays.length - 1];

		if (0 != drawableId && null != mParentView) {
			mParentView.setBackgroundResource(drawableId);
		}
	}

	public void setOnStatusChangeListener(OnStatusChangeListener listener) {
		mOnStatusChangeListener = listener;
	}

	public interface OnStatusChangeListener {
		void onClick(int index);
		void onCheck(boolean checked, int index);
		int allocateIndex();
	}

	public void onClick(View v) {

		if (null != mOnStatusChangeListener) {
			mOnStatusChangeListener.onClick(mCurrentIndex);
		}
	}

	public void setItemData(String data) {
		TextView tv = (TextView) findViewById(R.id.can_item_textview);

		if (null != tv) {
			tv.setText(data);
		}
	}
	
	public void onCheckedChanged(CompoundButton btn, boolean check) {
		if (null != mOnStatusChangeListener) {
			mOnStatusChangeListener.onCheck(check, mCurrentIndex);
		}
	}
	public void setItemChecked(boolean checked) {
		if(null != mSwitch) {
			mSwitch.setChecked(checked);
		}
	}
}
