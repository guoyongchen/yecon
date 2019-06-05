package com.carocean.floatwindow;

import com.carocean.R;
import com.carocean.floatwindow.fUtils.FLOAT_WINDOW_TYPE;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.settings.utils.tzUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FloatWindow extends RelativeLayout implements OnClickListener {

	private Context mContext;
	private View vPageMedia;
	private MediaView mMediaClass;
	private View vPageBT;
	private View vPageRadio;
	private RadioView mRadioClass;
	
	private View vCurView = null;

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
				initView();
			}
		};
	};

	public FloatWindow(Context context) {
		super(context);
		mContext = context;
		fUtils.setBackground(this);
		LayoutInflater.from(context).inflate(R.layout.floatwindow_layout, this);
	}

	void initView() {
		vPageMedia = findViewById(R.id.view_page_media);
		vPageRadio = findViewById(R.id.view_page_radio);
		vPageBT = findViewById(R.id.view_page_bt);
		
		if (null != vPageMedia && null == mMediaClass) {
			mMediaClass = new MediaView(mContext, vPageMedia);
		}
		if (null != vPageRadio && null == mRadioClass) {
			mRadioClass = new RadioView(mContext, vPageRadio);
		}
		
		((ImageButton)findViewById(R.id.float_bt_btn_showkeyboard)).setOnClickListener(this);
		((TextView) findViewById(R.id.float_media_title)).setOnClickListener(this);
		((TextView) findViewById(R.id.float_radio_title)).setOnClickListener(this);
		
		tzUtils.onSleep(400);
		showWindow(fUtils.floatwindowType);
	}

	public void showWindow(FLOAT_WINDOW_TYPE eType) {

		switch (eType) {
		case FLOAT_MEDIA:
			if (null != mRadioClass) {
				mRadioClass.isShow(View.GONE);
			}
			if (null != vPageBT) {
				vPageBT.setVisibility(View.GONE);
			}
			
			if (null != mMediaClass) {
				mMediaClass.isShow(View.VISIBLE);
				
				if (vCurView != null) {
					vCurView.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_miss));
				}
				vPageMedia.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_show));
				vCurView = vPageMedia;
			}
			break;
		case FLOAT_BT:
			if (null != mRadioClass) {
				mRadioClass.isShow(View.GONE);
			}
			if (null != mMediaClass) {
				mMediaClass.isShow(View.GONE);
			}
			if (null != vPageBT) {
				vPageBT.setVisibility(View.VISIBLE);

				if (vCurView != null) {
					vCurView.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_miss));
				}
				vPageBT.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_show));
				vCurView = vPageBT;
			}
			break;
		case FLOAT_RADIO:
			if (null != mMediaClass) {
				mMediaClass.isShow(View.GONE);
			}
			if (null != vPageBT) {
				vPageBT.setVisibility(View.GONE);
			}
			if (null != mRadioClass) {
				mRadioClass.isShow(View.VISIBLE);

				if (vCurView != null) {
					vCurView.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_miss));
				}
				vPageRadio.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_show));
				vCurView = vPageRadio;
			}
			break;
		}
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		mContext.registerReceiver(mBroadcastReceiver, filter);
		initView();
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mContext.unregisterReceiver(mBroadcastReceiver);
		if (null != vPageBT) {
			vPageBT.setVisibility(View.GONE);
		}
		if (null != mMediaClass) {
			mMediaClass.isShow(View.GONE);
		}
		if (null != mRadioClass) {
			mRadioClass.isShow(View.GONE);
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.float_media_title:
			launcherUtils.startMedia();
			break;
		case R.id.float_bt_btn_showkeyboard:
			launcherUtils.startBT();
			break;
		case R.id.float_radio_title:
			launcherUtils.startRadio();
			break;
		default:
			break;
		}
	}
}
