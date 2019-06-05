package com.carocean.vsettings.balance;

import com.carocean.R;
import com.carocean.page.IPage;
import com.carocean.settings.sound.Mtksetting;
import com.carocean.settings.sound.SoundArray;
import com.carocean.settings.sound.SoundUtils;
import com.carocean.settings.sound.SoundUtils.onBalanceListener;
import com.carocean.settings.sound.view.BalanceView;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.sLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @ClassName: PageBalance
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageBalance implements IPage, OnClickListener, OnTouchListener, onBalanceListener {
	private Context mContext;
	private ViewGroup mRootView;
	int ID_BUTTON[] = { R.id.btn_left, R.id.btn_up, R.id.btn_right, R.id.btn_down, R.id.btn_default, };
	Button mButton[] = new Button[ID_BUTTON.length];
	int ID_TEXTVIEW[] = { R.id.bal_front_rear_value, R.id.bal_left_right_value, };
	TextView mTextView[] = new TextView[ID_TEXTVIEW.length];
	BalanceView mBalanceView;
	private PolicyHandler mHandler;

	void init(Context context) {
		mHandler = new PolicyHandler();
	}

	void initView(ViewGroup rootView) {
		for (int i = 0; i < ID_TEXTVIEW.length; i++) {
			mTextView[i] = (TextView) rootView.findViewById(ID_TEXTVIEW[i]);
		}
		for (int i = 0; i < ID_BUTTON.length; i++) {
			mButton[i] = (Button) rootView.findViewById(ID_BUTTON[i]);
			mButton[i].setOnClickListener(this);
			mButton[i].setOnTouchListener(this);
		}
		mBalanceView = (BalanceView) rootView.findViewById(R.id.custom_balance_view);
		SoundUtils.setBalanceListener(this);
		int value[] = mBalanceView.getValue();
		freshBalanceValue(value[0], value[1]);
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_balance, null));
			init(context);
			initView(mRootView);
		}
		if(!isRegister) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(SettingConstants.AUTOMATION_EQ_BROADCAST_SEND);
			context.registerReceiver(mBroadcastReceiver, filter);
			isRegister = true;
			mContext = context;
		}
		return mRootView;
	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		sLog.d("PageBalance removeNotify()");
		if(isRegister && mContext != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
			isRegister = false;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			mHandler.sendEmptyMessageDelayed(view.getId(), 100);
			break;
		case MotionEvent.ACTION_UP:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_left:
			mBalanceView.cutPointX();
			break;
		case R.id.btn_right:
			mBalanceView.addPointX();
			break;
		case R.id.btn_up:
			mBalanceView.cutPointY();
			break;
		case R.id.btn_down:
			mBalanceView.addPointY();
			break;
		case R.id.btn_default:
			mBalanceView.onChangeBalanceMode(0);
			break;
		default:
			break;
		}
	}

	private class PolicyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.btn_up:
			case R.id.btn_left:
			case R.id.btn_down:
			case R.id.btn_right:
				if (mHandler.hasMessages(msg.what))
					mHandler.removeMessages(msg.what);
				mHandler.sendEmptyMessageDelayed(msg.what, 100);
				onClick((Button) mRootView.findViewById(msg.what));
				break;
			}
		}
	}

	@Override
	public void onChangeBalance(int lr, int fr) {
		// TODO Auto-generated method stub
		freshBalanceValue(lr, fr);
	}

	private void getStringBuffer(int value, StringBuffer strbuffer) {
		strbuffer.setLength(0);
		if (value >= 0) {
			strbuffer.append("+  ").append(value);
		} else {
			strbuffer.append("-  ").append(Math.abs(value));
		}
	}

	private void freshBalanceValue(int fr, int lr) {
		StringBuffer strBuf = new StringBuffer();
		getStringBuffer(lr, strBuf);
		String strF_R = mContext.getResources().getString(R.string.setting_sound_balance_fr, strBuf);
		getStringBuffer(fr, strBuf);
		String strL_R = mContext.getResources().getString(R.string.setting_sound_balance_lr, strBuf);
		mTextView[0].setText(strF_R);
		mTextView[1].setText(strL_R);
	}
	
    private boolean isRegister = false;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(SettingConstants.AUTOMATION_EQ_BROADCAST_SEND.equals(action)) {
				int eventType = intent.getIntExtra("eventType", 0);
				if(SettingConstants.ATE_CMD_EQ_FADE_BALANCE_SET == eventType) {
					int soundType = intent.getIntExtra("balanceType", 0xFF);
					int value = intent.getIntExtra("value", 0);
					if(mBalanceView != null) {
						int values[] = mBalanceView.getValue();
						int valueX = 0;
						int valueY = 0;
						switch (soundType) {
						// fornt back balance
						case 0x00:
							valueX = values[0];
							valueY = value -20;
							break;
						// left right balance
						case 0x01:
							valueX = value -20;
							valueY = values[1];
							break;
						default:
							break;
						}
						mBalanceView.onChangeBalance(valueX, valueY);
					}
				} else if(SettingConstants.ATE_CMD_EQ_RESTORE == eventType) {
					if(mBalanceView != null) { 
						mBalanceView.onChangeBalanceMode(0);
					}
				}
			}
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
