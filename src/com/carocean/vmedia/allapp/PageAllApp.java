package com.carocean.vmedia.allapp;

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
import com.carocean.utils.sLog;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * @ClassName: PageSound
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageAllApp implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {

	private ViewGroup mRootView;
	private LinearLayout carsetting_app, setting_app, kwmusic_app, klauto_app, filemanager_app, calendar_app, ie_app,
			iflytek_app;
	public static Boolean isSettings = false;

	void init(Context context) {

	}

	void initView(ViewGroup root) {

		carsetting_app = (LinearLayout) root.findViewById(R.id.carsetting_app);
		setting_app = (LinearLayout) root.findViewById(R.id.setting_app);
		kwmusic_app = (LinearLayout) root.findViewById(R.id.kwmusic_app);
		klauto_app = (LinearLayout) root.findViewById(R.id.klauto_app);
		filemanager_app = (LinearLayout) root.findViewById(R.id.filemanager_app);
		calendar_app = (LinearLayout) root.findViewById(R.id.calendar_app);
		ie_app = (LinearLayout) root.findViewById(R.id.ie_app);
		iflytek_app = (LinearLayout) root.findViewById(R.id.iflytek_app);

		carsetting_app.setOnClickListener(this);
		setting_app.setOnClickListener(this);
		kwmusic_app.setOnClickListener(this);
		klauto_app.setOnClickListener(this);
		filemanager_app.setOnClickListener(this);
		calendar_app.setOnClickListener(this);
		ie_app.setOnClickListener(this);
		iflytek_app.setOnClickListener(this);

	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.allapp_layout, null));
			init(context);
			initView(mRootView);
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

	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

		switch (view.getId()) {

		case R.id.carsetting_app:
			// launcherUtils.startCarSetting();
			launcherUtils.startCarInfo("is_carInfo");
			break;

		case R.id.setting_app:
			isSettings = true;
			launcherUtils.startSettings();
			break;

		case R.id.kwmusic_app:
			launcherUtils.startkwmusic();
			break;

		case R.id.klauto_app:
			launcherUtils.startklauto();
			break;

		case R.id.filemanager_app:
			launcherUtils.startFileManager();
			break;

		case R.id.calendar_app:
			launcherUtils.startCalendar();
			break;

		case R.id.ie_app:
			launcherUtils.startIe();
			break;

		case R.id.iflytek_app:
			launcherUtils.startiflytek();
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

			break;
		case MotionEvent.ACTION_UP:

			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
