package com.carocean.vsettings.display;

import com.autochips.settings.AtcSettings;
import com.carocean.R;
import com.carocean.page.IPage;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.Utils;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @ClassName: PageDisplay
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageDisplay implements IPage, OnClickListener, OnProgressChangedListener {
    private final static String TAG="PageDisplay";
	private Context mContext;
	private ViewGroup mRootView;
	int ID_SEEKBAR[] = { R.id.seekbar_brightness, R.id.seekbar_contrast, R.id.seekbar_chroma, R.id.seekbar_saturation,
			R.id.seekbar_backlight };
	CustomSeekbar mSeekBar[] = new CustomSeekbar[ID_SEEKBAR.length];
	int ID_TEXTVIEW[] = { R.id.brightness_value, R.id.contrast_value, R.id.chroma_value, R.id.saturation_value,
			R.id.backlight_value, };
	TextView mTextView[] = new TextView[ID_TEXTVIEW.length];
	int ID_BUTTON[] = { R.id.display_confirm, R.id.display_cancel, R.id.display_default, };
	Button mButton[] = new Button[ID_BUTTON.length];
	int mSaveVideoPara[] = new int[SettingConstants.videoPara.length];
	int mSaveBacklight;

	void init(Context context) {
		// mVideoPara[0] = AtcSettings.Display.GetBrightnessLevel();
		// mVideoPara[1] = AtcSettings.Display.GetContrastLevel();
		// mVideoPara[2] = AtcSettings.Display.GetHueLevel();
		// mVideoPara[3] = AtcSettings.Display.GetSaturationLevel();

		for (int i = 0; i < SettingConstants.videoPara.length; i++) {
			mSaveVideoPara[i] = SettingConstants.videoPara[i];
		}
		mSaveBacklight = SettingConstants.backlight;
	}

	void initView(ViewGroup rootView) {
		for (int i = 0; i < ID_TEXTVIEW.length; i++) {
			mTextView[i] = (TextView) rootView.findViewById(ID_TEXTVIEW[i]);
			if (i < 4)
				mTextView[i].setText(String.valueOf(SettingConstants.videoPara[i]));
			else if (i == 4) {
				int cur_brightness = Utils.getBrightness();
				if (cur_brightness > 201) {
					cur_brightness = 201;
				}
				int show_brightness = changeBackLight(cur_brightness, 201, 1, 100, 0);
				mTextView[i].setText(String.valueOf(show_brightness));
                Log.i(TAG, "backlight---when init view, cur_brightness = " + cur_brightness+",show_brightness="+show_brightness);
			}
		}

		for (int i = 0; i < ID_BUTTON.length; i++) {
			mButton[i] = (Button) rootView.findViewById(ID_BUTTON[i]);
			mButton[i].setOnClickListener(this);
		}

		for (int i = 0; i < ID_SEEKBAR.length; i++) {
			mSeekBar[i] = (CustomSeekbar) rootView.findViewById(ID_SEEKBAR[i]);
			mSeekBar[i].setOnProgressChangedListener(this);
			if (i < 4)
				mSeekBar[i].setProgress(SettingConstants.videoPara[i]);
			else if (i == 4) {
				int cur_brightness = Utils.getBrightness();
				if (cur_brightness > 201) {
					cur_brightness = 201;
				}
				mSeekBar[i].setProgress(changeBackLight(cur_brightness, 201, 1, 100, 0));
			}
		}
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
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_display, null));
			init(context);
		}
		initView(mRootView);
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

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.display_confirm:
			for (int i = 0; i < SettingConstants.videoPara.length; i++) {
				mSaveVideoPara[i] = SettingConstants.videoPara[i];
			}
			mSaveBacklight = SettingConstants.backlight;
			break;
		case R.id.display_cancel:
			for (int i = 0; i < SettingConstants.videoPara.length; i++) {
				SettingConstants.videoPara[i] = mSaveVideoPara[i];
				setDisplay(i);
			}
			SettingConstants.backlight = mSaveBacklight;
			setBacklight();
			break;
		case R.id.display_default:
			resetVideoValue();
			break;
		default:
			break;
		}
	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub
		sendMessage(seekBar.getId(), seekBar.getProgress());
	}

	public void sendMessage(int what, int index) {
		Message message = Message.obtain();
		message.what = what;
		message.obj = index;
		myHandler.sendMessage(message);
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.seekbar_brightness:
			case R.id.seekbar_contrast:
			case R.id.seekbar_chroma:
			case R.id.seekbar_saturation:
				int i = 0;
				for (int nValue : ID_SEEKBAR) {
					if (nValue == msg.what) {
						SettingConstants.videoPara[i] = (Integer) msg.obj;
						Log.i(TAG, "backlight---onseekbar change, videoPara["+i+"]="+SettingConstants.videoPara[i]);
						mTextView[i].setText(String.valueOf(SettingConstants.videoPara[i]));
						setDisplayValue(i, SettingConstants.videoPara[i]);
						return;
					}
					i++;
				}
				break;
			case R.id.seekbar_backlight:
				int seekbar_backlight = (Integer) msg.obj;
                Log.i(TAG, "backlight---onseekbar change, seekbar backlight="+seekbar_backlight);

				mTextView[4].setText(String.valueOf(seekbar_backlight));
				int brightnesss = changeBackLight(seekbar_backlight, 100, 0, 201, 1);

                Log.i(TAG, "backlight---onseekbar change, changed backlight="+brightnesss);

				Utils.setBrightness(mContext, brightnesss);
				break;
			default:
				break;
			}

		}
	};

	/**
	 * 背光亮度相互转换0-100转0到255
	 */
	public static int changeBackLight(int cur_Progress, int m_MaxProgress, int m_MinProgress, int m_MaxLight,
			int m_MinLight) {
		int fRange_progress = m_MaxProgress - m_MinProgress;
		int fRange_light = m_MaxLight - m_MinLight;
		int s = (int) ((cur_Progress - m_MinProgress) * fRange_light / fRange_progress + m_MinLight);
		return s;

	}

	public void setDisplayValue(int type, int value) {
		if (value >= 100)
			value = 100;
		if (value <= 0)
			value = 0;
		switch (type) {
		case 0:
			AtcSettings.Display.SetBrightnessLevel(value);
            Log.i(TAG, "backlight---SetBrightnessLevel=" + value);
			break;
		case 1:
			AtcSettings.Display.SetContrastLevel(value);
            Log.i(TAG, "backlight---SetContrastLevel=" + value);
			break;
		case 2:
			AtcSettings.Display.SetHueLevel(value);
            Log.i(TAG, "backlight---SetHueLevel=" + value);
			break;
		case 3:
			AtcSettings.Display.SetSaturationLevel(value);
            Log.i(TAG, "backlight---SetSaturationLevel=" + value);
			break;
		}
	}

	private void resetVideoValue() {
		for (int i = 0; i < SettingConstants.videoPara.length; i++) {
			mSaveVideoPara[i] = SettingConstants.videoPara[i] = SettingConstants.DEFAULT_RGB_VALUES[i];
			setDisplay(i);
		}
		mSaveBacklight = SettingConstants.backlight = SettingConstants.DEFAULT_BACKLIGHT_VALUE;
		setBacklight();
	}

	private void setDisplay(int i) {
		mTextView[i].setText(String.valueOf(SettingConstants.videoPara[i]));
		mSeekBar[i].setProgress(SettingConstants.videoPara[i]);
		SettingConstants.videoPara[i] = SettingConstants.videoPara[i];
		setDisplayValue(i, SettingConstants.videoPara[i]);
	}

	private void setBacklight() {
		int cur_backlight = SettingConstants.backlight;
		if (cur_backlight > 201) {
			cur_backlight = 201;
		}
		int backlight = changeBackLight(cur_backlight, 201, 1, 100, 0);
		mTextView[4].setText(backlight + "");

		mSeekBar[4].setProgress(backlight);
		Utils.setBrightness(mContext, SettingConstants.backlight);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}

}
