/**
 * 
 */
package com.carocean;

import com.carocean.coordinator.Coordinator;
import com.carocean.coordinator.CoordinatorProvider;
import com.carocean.coordinator.Coordinators;
import com.carocean.launcher.popupwindow.SettingPopupWindow;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;
import com.carocean.utils.Utils.OnThemeChangeListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mcu.McuExternalConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/* @ClassName MainActivity.java
 * @Description TODO
 * @author LIUZHIYUAN
 * @Date 2019年5月17日下午1:48:15 
 */
public class MainActivity extends Activity implements OnClickListener, OnTouchListener {

	private final String TAG = getClass().getSimpleName() + "_";
	Context mContext;
	public static boolean mHasFocus = false;
	private static final int MSG_ID_INIT = 0x1000;

	private LinearLayout layout_time;
	private TextView tv_week, tv_date, tv_time;
	private ImageView car_info;
	private RelativeLayout bottom_rl;
	private SettingPopupWindow mSettingPopupWindow;
	private float mY = 0;

	private static final ObjectGraph OBJECT_GRAPH = new ObjectGraph();
	private static int mCurTheme = R.layout.launcher_carrouse_layout;
	private ViewGroup container;

	void initData() {
		mContext = this;
		registerReceiver();
		// startHandleThread();
		// mThreadHandler.sendEmptyMessageDelayed(MSG_ID_INIT, 500);
	}

	void initView() {
		layout_time = (LinearLayout) findViewById(R.id.layout_time);
		tv_week = (TextView) findViewById(R.id.id_week);
		tv_date = (TextView) findViewById(R.id.id_date);
		tv_time = (TextView) findViewById(R.id.id_time);
		car_info = (ImageView) findViewById(R.id.car_info);
		bottom_rl = (RelativeLayout) findViewById(R.id.bottom_rl);
		bottom_rl.setOnTouchListener(this);
		layout_time.setOnClickListener(this);
		car_info.setOnClickListener(this);
		mSettingPopupWindow = new SettingPopupWindow(this);
		updateDateTime(mContext);

		container = (ViewGroup) findViewById(R.id.fl_container);
		Coordinators.installBinder(container, new CoordinatorProvider() {
			@Override
			public Coordinator provideCoordinator(View view) {
				String coordinatorName = (String) view.getTag().toString();
				return OBJECT_GRAPH.get(coordinatorName);
			}
		});
		launcherUtils.mTheme = DataShared.getInstance(mContext).getInt(SettingConstants.key_ui_theme,
				launcherUtils.mTheme);
		setLauncherTheme(launcherUtils.mTheme);

		Utils.setOnThemeChangeListener(new OnThemeChangeListener() {

			@Override
			public void onItemClick(int theme) {
				// TODO Auto-generated method stub
				setLauncherTheme(launcherUtils.mTheme);
			}
		});
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Log.i(TAG, "....................onCreate---");

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Log.e(TAG, "width=" + dm.widthPixels + " height=" + dm.heightPixels);
		
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			final boolean alreadyOnHome = mHasFocus && ((intent.getFlags()
					& Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			if (alreadyOnHome) {
				setLauncherTheme(launcherUtils.mTheme);
			}
		}
		super.onNewIntent(intent);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		mHasFocus = hasFocus;
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		SystemProperties.set("persist.sys.home.show", "true");
		showBackOrHome(true);
		if (mSettingPopupWindow != null) {
			mSettingPopupWindow.dismiss();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		SystemProperties.set("persist.sys.home.show", "false");
		showBackOrHome(false);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver();
		super.onDestroy();
	}

	protected Handler mThreadHandler;
	protected HandlerThread mCustomHandleThread;

	void startHandleThread() {
		mCustomHandleThread = new HandlerThread("LauncherHandleThread");
		mCustomHandleThread.start();
		mThreadHandler = new Handler(mCustomHandleThread.getLooper(), mCallback);
	}

	Callback mCallback = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int what = msg.what;
			switch (what) {
			case MSG_ID_INIT:
				Log.i(TAG, "....................MSG_ID_INIT---");
				break;
			default:
				break;
			}
			return false;
		}
	};

	private void setLauncherTheme(int id) {
		container.removeAllViews();
		switch (id) {
		case 0:
		default:
			mCurTheme = R.layout.launcher_carrouse_layout;
			break;
		case 1:
			mCurTheme = R.layout.launcher_default_theme_layout;
			break;
		case 2:
			mCurTheme = R.layout.launcher_custom_layout;
			break;
		}
		LayoutInflater.from(container.getContext()).inflate(mCurTheme, container);
		showTime();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mY = event.getY();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float y_move = event.getY();
			Log.e(TAG, "mY: " + mY + " y_move: " + y_move);
			if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
				if (mY - y_move > 30) {
					if (mSettingPopupWindow != null) {
						mSettingPopupWindow.setBacklight();
						mSettingPopupWindow.showAtLocation(bottom_rl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.layout_time:
			launcherUtils.startCarInfo("is_setTime");
			launcherUtils.startCarHelpSetTime(true);
			break;

		case R.id.car_info:
			if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
				launcherUtils.startCarInfo("is_carInfo");
			}
			break;

		default:
			break;
		}

	}

	void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	void unregisterReceiver() {
		if (mBroadcastReceiver != null && mContext != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
		}
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// final String action = intent.getAction();
			updateDateTime(context);
		}
	};

	public void showTime() {
		if (launcherUtils.mTheme == 2) {
			layout_time.setVisibility(View.VISIBLE);
		} else {
			layout_time.setVisibility(View.INVISIBLE);
		}
	}

	private void updateDateTime(Context context) {
		String time = Utils.getHourMinute(Utils.is24HourFormat(context));

		String date = Utils.getDate();
		String week = Utils.getCurrentWeek(mContext);

		String date_str = null;
		if (!TextUtils.isEmpty(date)) {
			date_str = date.replace("-", ".");
		}

		if (tv_date != null)
			tv_date.setText(date_str);
		if (tv_week != null)
			tv_week.setText(week);
		if (tv_time != null)
			tv_time.setText(time);
		Log.i(TAG, "\ntime:" + time + " date:" + date + " date:" + week);
	}

	public void showBackOrHome(boolean isFront) {
		Intent intent = new Intent(launcherUtils.HOME_OR_BACK_SHOW_ACTION);
		intent.putExtra("isFront", isFront);
		mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
	}
}
