package com.carocean.can;

import com.carocean.R;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class AlertDialogEx implements OnCheckedChangeListener, OnClickListener{
	private AlertDialog.Builder mDialogBuilder;
	private AlertDialog mDialog;
	private int         mItemIds[] = {R.id.can_selected_dialog_item, R.id.can_selected_dialog_item_1, R.id.can_selected_dialog_item_2};
	private RadioButton mItemArrays[] = new RadioButton[mItemIds.length];
	private Drawable mDrawableLeft;
	private Drawable mDrawableLeftFocus;
	private View     mNormalView;
	private View     mPickerView;
	private int      mButtonIds[] = {R.id.can_selected_dialog_ok_btn, R.id.can_selected_dialog_cancle_btn,
			                         R.id.can_close_btn};
	private int      mItemString[] = null;
	private Context  mContext;
	private CanNumberPickerView mNumberPickerView;
	private String mPickerDatas[] = {"30","35","40","45","50","55","60", "65", "70", "75", "80", "85","90", "95", 
            "100", "105", "110", "115", "120", "125", "130", 
            "135", "140", "145", "150", "155", "160", "165","170","175", "180", "185", "190","195", "200",
            "205", "210", "215", "220"};
	private OnDialogItemSelectChangeListener mOnDialogItemSelectChangeListener;
	AlertDialogEx(Context context, int layout, int itemText[], String title){
		mContext = context;
		mDialogBuilder = new AlertDialog.Builder(context, R.style.Can_Dialog_Theme_Transparent);
		View view = LayoutInflater.from(context).inflate(layout, null);
		
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.can_checkbox_icon_n);
		mDrawableLeft = context.getResources().getDrawable(R.drawable.can_checkbox_icon_n);
		mDrawableLeftFocus = context.getResources().getDrawable(R.drawable.can_checkbox_icon_f);
		mDrawableLeftFocus.setBounds(0,  0, bmp.getWidth() , bmp.getHeight());
		mDrawableLeft.setBounds(0,  0, bmp.getWidth() , bmp.getHeight());
		RadioGroup radioGroup = (RadioGroup)view.findViewById(R.id.can_selected_dialog_radiogroup);
		
		if(null != radioGroup) {
			radioGroup.setOnCheckedChangeListener(this);
		}
			
		initViews(view, itemText, title);
		processCheckStatus();
		mDialog = mDialogBuilder.create();
		
		mDialog.setView(view, 0, 0, 0, 0);
	}
	public void setStyle(int style, int index) {
		if(0 == style) {
			if(null != mPickerView) {
				mPickerView.setVisibility(View.GONE);
			}
			
			if(index < mItemArrays.length) {
				mItemArrays[index].setChecked(true);
			}
		}else {
			if(null != mNormalView) {
				mNormalView.setVisibility(View.GONE);
			}
			
			if(null != mNumberPickerView) {
				mNumberPickerView.refreshByNewDisplayedValues(mPickerDatas);
				mNumberPickerView.setValue(index);
			}
		}
	}
	public void setCurrentItem(int item) {
		if(item < mItemArrays.length) {
			mItemArrays[item].setChecked(true);
		}
	}
	private void initViews(View v, int itemText[], String title) {
		if(null == v) {
			return ;
		}
		for(int i = 0; i < mItemIds.length; i ++) {
			mItemArrays[i] = (RadioButton)v.findViewById(mItemIds[i]);
			
			if(null != itemText) {
				if(i < itemText.length) {
					mItemArrays[i].setText(itemText[i]);
				}
			}
		}
		
		for(int i = 0; i < mButtonIds.length; i ++) {
			View child = v.findViewById(mButtonIds[i]);
			
			if(null != child) {
				child.setOnClickListener(this);
			}
		}

		if(null != title) {
			TextView tv = (TextView)v.findViewById(R.id.can_selected_dialog_title);
			
			if(null != tv) {
				tv.setText(title);
			}
		}
		mNormalView = v.findViewById(R.id.can_selected_dialog_list);
		mPickerView = v.findViewById(R.id.can_selected_dialog_number_picker);
		mNumberPickerView = (CanNumberPickerView)v.findViewById(R.id.can_number_picker);
		mItemString = itemText;
	}
	private void processCheckStatus() {
		for(int i = 0; i < mItemArrays.length; i ++) {
			RadioButton btn = mItemArrays[i];
			if(null != btn) {
				if(btn.isChecked()) {
					btn.setCompoundDrawables(null, null, mDrawableLeftFocus, null);
				}else {
					btn.setCompoundDrawables(null, null, mDrawableLeft, null);
				}
			}
		}
	}

	public void show() {
		Window dialogWindow = mDialog.getWindow();
		DisplayMetrics dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = -width/2; 
		lp.y = -height/2; 
	
		dialogWindow.setAttributes(lp);
		mDialog.show();
	}
	public void setOnDialogItemSelectChangeListener(OnDialogItemSelectChangeListener listener) {
		mOnDialogItemSelectChangeListener = listener;
	}
	public void onCheckedChanged(RadioGroup parent, int id) {
		processCheckStatus();
		int index = getCheckedIndex(id);
		String data = null;
		
		if(null != mItemString && index < mItemString.length) {
			id = mItemString[index];
			
			data = mContext.getResources().getString(id);
		}
		OnDialogItemSelectChange(index, data);
	}
	private void OnDialogItemSelectChange(int index, String data) {
		if(null != mOnDialogItemSelectChangeListener) {
			mOnDialogItemSelectChangeListener.OnDialogItemSelectChange(index, data);
		}
	}
	private int getCheckedIndex(int id) {
		int index = 0;
		for(int i = 0; i < mItemIds.length; i ++) {
			if(id == mItemIds[i]) {
				index = i;
				break;
			}
		}
		return index;
	}
	public interface OnDialogItemSelectChangeListener{
		void OnDialogItemSelectChange(int index, String data);
	}
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.can_close_btn:
		case R.id.can_selected_dialog_cancle_btn:
			break;
		case R.id.can_selected_dialog_ok_btn:
			String[] content = mNumberPickerView.getDisplayedValues();
			String current = content[mNumberPickerView.getValue() - mNumberPickerView.getMinValue()];
			int value = Integer.valueOf(current);
			OnDialogItemSelectChange(value, current);
			break;
		}
		mDialog.dismiss();
	}
}
