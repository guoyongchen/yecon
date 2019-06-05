package com.carocean.vsettings.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.settings.sound.Mtksetting;
import com.carocean.settings.sound.SoundArray;
import com.carocean.settings.sound.SoundMethodManager;
import com.carocean.settings.sound.SoundUtils;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.sLog;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

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
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * @ClassName: PageSound
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageSound implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {

	private ViewGroup mRootView;
	int ID_TEXTVIEW[] = { R.id.tv_number_point0, R.id.tv_number_point1, R.id.tv_number_point2, };
	TextView mTextView[] = new TextView[ID_TEXTVIEW.length];
	int ID_BUTTON[] = { R.id.btn_add_point0, R.id.btn_cut_point0, R.id.btn_add_point1, R.id.btn_cut_point1,
			R.id.btn_add_point2, R.id.btn_cut_point2, };
	Button mButton[] = new Button[ID_BUTTON.length];

	Integer ID_RADIOBUTTON[] = { R.id.eq_mode_standard, R.id.eq_mode_user, R.id.eq_mode_pop, R.id.eq_mode_classic,
			R.id.eq_mode_jazz, R.id.eq_mode_dance, R.id.eq_mode_folk, R.id.eq_mode_voice, R.id.eq_mode_rock,
			R.id.eq_mode_bass_boost, R.id.eq_mode_treble_boost };
	RadioButton mRadioEQ[] = new RadioButton[ID_RADIOBUTTON.length];

	int ID_SEEKBAR[] = { R.id.seekbar_point0, R.id.seekbar_point1, R.id.seekbar_point2 };
	CustomSeekbar mSeekBar[] = new CustomSeekbar[ID_SEEKBAR.length];
	private List<CustomSeekbar> vSeekBarList = new ArrayList<CustomSeekbar>();
	private List<RadioButton> radioButtonList = new ArrayList<RadioButton>();
	int[] userEQGain = new int[ID_SEEKBAR.length];
	private final static int handler_set_effect = 300;
	private final int MSG_SET_USER_EQ = 255;
	public static int nTreble = 26;
	public static int nAlto = 17;
	public static int nBass = 8;

	SoundMethodManager mAudMethodManager;
	String[] arrayEQMode;

	void init(Context context) {
		mAudMethodManager = SoundMethodManager.getInstance(context);
		arrayEQMode = context.getResources().getStringArray(R.array.eq_mode_values);
	}

	void initView(ViewGroup root) {
		for (int i = 0; i < ID_TEXTVIEW.length; i++) {
			mTextView[i] = (TextView) root.findViewById(ID_TEXTVIEW[i]);
		}
		for (int i = 0; i < ID_BUTTON.length; i++) {
			mButton[i] = (Button) root.findViewById(ID_BUTTON[i]);
			mButton[i].setOnClickListener(this);
			mButton[i].setOnTouchListener(this);
		}
		radioButtonList.clear();
		for (int i = 0; i < ID_RADIOBUTTON.length; i++) {
			mRadioEQ[i] = (RadioButton) root.findViewById(ID_RADIOBUTTON[i]);
			mRadioEQ[i].setOnClickListener(this);
			radioButtonList.add(mRadioEQ[i]);
		}
		vSeekBarList.clear();
		for (int i = 0; i < ID_SEEKBAR.length; i++) {
			mSeekBar[i] = (CustomSeekbar) root.findViewById(ID_SEEKBAR[i]);
			mSeekBar[i].setOnProgressChangedListener(this);
			vSeekBarList.add(mSeekBar[i]);
		}
		SetSeekBarProgress(SoundUtils.curPEQPresetType);
		SetPEQPresetTypeChecked(SoundUtils.curPEQPresetType);
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub
	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_sound, null));
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
		// TODO Auto-generated method stub
		sLog.d("PageSound removeNotify()");
		if(isRegister && mContext != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
			isRegister = false;
		}
	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub
		for (int i = 0; i < userEQGain.length; i++) {
			if (seekBar.getId() == ID_SEEKBAR[i]) {
				int progress = seekBar.getProgress();
				userEQGain[i] = progress - 10;
				mTextView[i].setText(progress - 10 + "");
				break;
			}
		}

		SoundUtils.curPEQPresetType = Mtksetting.PEQPresetType.AUD_EQ_USER;
		Mtksetting.PEQPresetType eqType = SoundUtils.curPEQPresetType;
		if (!radioButtonList.get(SoundUtils.curPEQPresetType.ordinal()).isChecked()) {
			SetPEQPresetTypeChecked(eqType);
		}

		for (int i = 0; i < userEQGain.length; i++) {
			int index = (i + 1) * 9 - 1;
			SoundArray.gEQTypePos31[eqType.ordinal()][index] = userEQGain[i];
		}
		mHandler.removeMessages(MSG_SET_USER_EQ);
		mHandler.sendEmptyMessageDelayed(MSG_SET_USER_EQ, 200);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		mHandler.removeMessages(MSG_SET_USER_EQ);
		switch (arg0.getId()) {
		case R.id.eq_mode_standard:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_TYPE_OFF);
			break;
		case R.id.eq_mode_user:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_USER);
			break;
		case R.id.eq_mode_pop:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_POP);
			break;
		case R.id.eq_mode_classic:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_CLASSICAL);
			break;
		case R.id.eq_mode_jazz:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_JAZZ);
			break;
		case R.id.eq_mode_dance:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_DANCE);
			break;
		case R.id.eq_mode_folk:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_FOLK);
			break;
		case R.id.eq_mode_rock:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_ROCK);
			break;
		case R.id.eq_mode_bass_boost:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_BASS_BOOST);
			break;
		case R.id.eq_mode_treble_boost:
			SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_TREBLE_BOOST);
			break;
		case R.id.btn_add_point0:
			changeProgress(mSeekBar[0], 1);
			break;
		case R.id.btn_cut_point0:
			changeProgress(mSeekBar[0], -1);
			break;
		case R.id.btn_add_point1:
			changeProgress(mSeekBar[1], 1);
			break;
		case R.id.btn_cut_point1:
			changeProgress(mSeekBar[1], -1);
			break;
		case R.id.btn_add_point2:
			changeProgress(mSeekBar[2], 1);
			break;
		case R.id.btn_cut_point2:
			changeProgress(mSeekBar[2], -1);
			break;
		default:
			break;
		}
	}

	void changeProgress(CustomSeekbar seekbar, int offset) {
		int pos = seekbar.getProgress() + offset;
		seekbar.setProgress(pos);
		onChanged(seekbar);
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

	void SetCurPEQPresetType(Mtksetting.PEQPresetType eType) {
		SoundUtils.curPEQPresetType = eType;
		SetPEQPresetType(eType);
	}

	int SetPEQPresetType(Mtksetting.PEQPresetType eType) {
		SetPEQPresetTypeChecked(eType);
		SetSeekBarProgress(eType);
		int result = mAudMethodManager.SetPEQPresetType(eType);
		return result;
	}

	void SetPEQPresetTypeChecked(Mtksetting.PEQPresetType eType) {
		for (RadioButton radioButton : radioButtonList) {
			radioButton.setChecked(false);
		}
		radioButtonList.get(eType.ordinal()).setChecked(true);
	}

	public void SetSeekBarProgress(Mtksetting.PEQPresetType eType) {
		Message message = Message.obtain();
		message.what = handler_set_effect;
		message.obj = eType.ordinal();
		mHandler.sendMessage(message);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case handler_set_effect:
				int index = (Integer) msg.obj;
				for (int idx = 0; idx < vSeekBarList.size(); idx++) {
					int mPre = SoundArray.gEQTypePos31[index][(idx + 1) * 9 - 1];
					userEQGain[idx] = mPre;
					vSeekBarList.get(idx).setProgress(mPre + 10);
					mTextView[idx].setText(mPre + "");
				}
				break;
			case R.id.btn_add_point0:
			case R.id.btn_add_point1:
			case R.id.btn_add_point2:
			case R.id.btn_cut_point0:
			case R.id.btn_cut_point1:
			case R.id.btn_cut_point2:
				if (mHandler.hasMessages(msg.what))
					mHandler.removeMessages(msg.what);
				mHandler.sendEmptyMessageDelayed(msg.what, 100);
				onClick((Button) mRootView.findViewById(msg.what));
				break;
			case MSG_SET_USER_EQ:
				int result = mAudMethodManager.SetPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_USER);
				sLog.d("result:" + result + "......SoundArray.gEQTypePos31: " + Arrays.deepToString(SoundArray.gEQTypePos31));
				break;
			default:
				break;
			}

		}
	};
	
	
	private boolean isRegister = false;
	
	private Context mContext;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(SettingConstants.AUTOMATION_EQ_BROADCAST_SEND.equals(action)) {
				int eventType = intent.getIntExtra("eventType", 0);
				if(SettingConstants.ATE_CMD_EQ_TREB_ALTO_BASS_SET == eventType) {
					int soundType = intent.getIntExtra("soundType", 0xFF);
					int value = intent.getIntExtra("value", 0);
					// set the sound value
					SoundArray.gEQTypePos31[Mtksetting.PEQPresetType.AUD_EQ_USER.ordinal()][(3 - soundType) * 9 - 1] = transformValue(value);
					SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_USER);
				} else if(SettingConstants.ATE_CMD_EQ_RESTORE == eventType) {
					SetCurPEQPresetType(Mtksetting.PEQPresetType.AUD_EQ_TYPE_OFF);
				}
			}
		}
	};

	private int  transformValue(int value) {
		int soundValue = 0;
		switch (value) {
		case 0x00:
			soundValue = -10;
			break;
		case 0x0E:
			soundValue = 0;
			break;
		case 0x1D:
			soundValue = 10;
			break;
		default:
			break;
		}
		return soundValue;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}
}
