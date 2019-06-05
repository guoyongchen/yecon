package com.carocean.vmedia.t19can;

import java.util.ArrayList;
import java.util.Calendar;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.service.BootService;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.settings.utils.timeUtils;
import com.carocean.t19can.CanBusService;
import com.carocean.t19can.T19CanRx;
import com.carocean.t19can.T19CanRx.AirInfo;
import com.carocean.t19can.T19CanRx.CarHelpAirInfo;
import com.carocean.t19can.T19CanRx.CarStatusInfo;
import com.carocean.t19can.T19CanRx.CarbodyInfo;
import com.carocean.t19can.T19CanRx.DashboardConfigInfo1;
import com.carocean.t19can.T19CanRx.DashboardInfo;
import com.carocean.t19can.T19CanRx.EnergyInfo;
import com.carocean.t19can.T19CanRx.LowPowerInfo;
import com.carocean.t19can.T19CanRx.PedestrianInfo;
import com.carocean.t19can.T19CanRx.PowerInfo;
import com.carocean.t19can.T19CanRx.VcuInfo;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;
import com.carocean.t19can.T19CanTx;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vmedia.t19can.view.CustomPickerView;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.mcu.McuManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PageCarHelp
		implements IPage, ServiceConnection, OnClickListener, OnCheckedChangeListener, OnProgressChangedListener {

	private static final String TAG = "PageCarHelp";
	private Context mContext;
	private ViewGroup mRootView;

	public static Boolean isSetTime = false;

	private RadioGroup carhelp_rg;
	private RadioButton carhelp_dashboard_rb, carhelp_top_pedestrian_rb, carhelp_top_carbody_rb, carhelp_top_air_rb,
			carhelp_top_power_rb;
	private LinearLayout layout_dashboard_ll, layout_pedestrian_ll, layout_carbody_ll, layout_carair_ll,
			layout_power_ll;

	// 仪表
	private RelativeLayout carhelp_timeset_rl, carhelp_speed_warning_adjust_rl, carhelp_fatigue_driving_adjust_rl;
	private CheckBox carhelp_speed_warning_ck, carhelp_fatigue_driving_ck;
	private CustomSeekbar carhelp_speed_warning_sb, carhelp_fatigue_driving_sb;
	private TextView carhelp_speed_warning_tv, carhelp_fatigue_driving_tv, carhelp_dashboard_backlight_tv;
	private ImageView carhelp_dashboard_backlight_reduce, carhelp_dashboard_backlight_add;

	// 行人
	private CheckBox carhelp_pedestrian_reminder_ck;
	private TextView carhelp_set_volume_tv, carhelp_set_sound_tv;
	private ImageView carhelp_set_volume_reduce, carhelp_set_volume_add, carhelp_set_sound_reduce,
			carhelp_set_sound_add;
	RelativeLayout carhelp_set_volume_rl, carhelp_set_sound_rl;

	// 车身
	private CheckBox carhelp_carbody_atmosphere_lamp_ck, carhelp_carbody_decorative_lamp_ck,
			carhelp_carbody_rearview_mirror_ck, carhelp_carbody_auto_locking_ck, carhelp_carbody_set_gohome_ck,
			carhelp_carbody_auto_skylight_ck, carhelp_carbody_speed_limit_warning_ck;
	private TextView carhelp_carbody_set_prevention_warn_tv, carhelp_carbody_atmosphere_tv;
	private ImageView carhelp_carbody_set_prevention_warn_reduce, carhelp_carbody_set_prevention_warn_add;
	private CustomSeekbar carhelp_carbody_atmosphere_sb;
	private RelativeLayout carhelp_carbody_atmosphere_lamp_rl, carhelp_carbody_decorative_lamp_rl;

	// 空调
	private CheckBox carhelp_air_auto_cleaning_ck, carhelp_auto_aeration_ck, carhelp_remember_air_ck;
	private TextView carhelp_air_showtime_tv;
	private ImageView carhelp_air_showtime_reduce, carhelp_air_showtime_add;

	// 动力
	private CheckBox carhelp_cESC_ck, carhelp_eHAC_up_ck, carhelp_eHDC_down_ck, carhelp_eAuto_Hold_ck,
			carhelp_speed_limit_ck, carhelp_long_range_endurance_ck, carhelp_backcar_enable_ck;
	private TextView carhelp_eAuto_Hold_setTime_tv;
	private ImageView carhelp_eAuto_Hold_setTime_reduce, carhelp_eAuto_Hold_setTime_add;
	private CustomSeekbar carhelp_speed_limit_sb;
	private TextView carhelp_speed_limit_tv, carhelp_long_range_endurance_time;
	private RelativeLayout carhelp_eAuto_Hold_setTime_rl, carhelp_backcar_enable_rl;

	private CanBusService.CanBusBinder myBinder = null;
	private CarStatusInfo mCarStatusInfo = null;
	private PedestrianInfo mPedestrianInfo = null;
	private DashboardInfo mDashboardInfo = null;
	private CarbodyInfo mCarbodyInfo = null;
	private CarHelpAirInfo mCarHelpAirInfo = null;
	private PowerInfo mPowerInfo = null;
	private DashboardConfigInfo1 mDashboardConfigInfo1 = null;
	public static final int MSG_PEDESTRIANINfO = 103;// 行人
	public static final int MSG_DASHBOARDINfO = 104;// 仪表
	public static final int MSG_CARBODYINfO = 105;// 车身
	public static final int MSG_CARHELPAIRINfO = 106;// 车助空调
	public static final int MSG_POWERINfO = 107;// 动力
	public static final int MSG_DASHBOARDCONFIG1INfO = 108;// 仪表在线配置1

	private LinearLayout common_set_ll, set_time_ll;
	private CustomPickerView year, month, day, hour, mins;
	private int year_value, month_value, day_value, hour_value, mins_value;
	private int maxDay = 30;
	private int yearRange = 35;
	private int monthRange = 12;
	private int hourRange = 24;
	private int minsRange = 60;
	private ArrayList<String> year_dataList = new ArrayList<String>();
	private ArrayList<String> month_dataList = new ArrayList<String>();
	private ArrayList<String> day_dataList = new ArrayList<String>();
	private ArrayList<String> hour_dataList = new ArrayList<String>();
	private ArrayList<String> mins_dataList = new ArrayList<String>();

	private ImageView year_up_iv, year_down_iv, month_up_iv, month_down_iv, day_up_iv, day_down_iv, hour_up_iv,
			hour_down_iv, mins_up_iv, mins_down_iv;
	private Button data_time_gps, settime_sure, settime_cancle;
	private boolean mbAutoTime;
	private static McuManager mMcuManager;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(launcherUtils.START_CARHELP_SETTIME_ACTION)) {
				boolean isShow = intent.getBooleanExtra("isShow", false);
				Log.e(TAG, "action: " + launcherUtils.START_CARHELP_SETTIME_ACTION + "  isShow: " + isShow);
				if (mRootView != null) {
					if (isShow) {
						onClick(mRootView.findViewById(R.id.carhelp_timeset_rl));
					} else {
						isSetTime = false;
						common_set_ll.setVisibility(View.VISIBLE);
						set_time_ll.setVisibility(View.GONE);
					}
				}
				if (carhelp_dashboard_rb != null) {
					carhelp_dashboard_rb.setChecked(true);
				}
				showCarHelpUI(true, false, false, false, false);
			} else if (action.equals(Intent.ACTION_TIME_TICK) || action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
				if (timeUtils.getAutoState(mContext, Settings.Global.AUTO_TIME)) {
					initTime();
				}
			}
		}
	};

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.carinfo_layout_carhelp, null));
			init(context);
			initView(mRootView);

			IntentFilter filter = new IntentFilter();
			filter.addAction(launcherUtils.START_CARHELP_SETTIME_ACTION);
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			filter.addAction(Intent.ACTION_DATE_CHANGED);
			mContext.registerReceiver(mBroadcastReceiver, filter);

		}

		if ("is_setTime".equals(MediaActivity.mActivity.switchType)) {
			onClick(mRootView.findViewById(R.id.carhelp_timeset_rl));
		} else {
			isSetTime = false;
			common_set_ll.setVisibility(View.VISIBLE);
			set_time_ll.setVisibility(View.GONE);
		}
		Intent intent = new Intent(mContext, CanBusService.class);
		mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
		return mRootView;
	}

	void init(Context context) {

	}

	void initView(ViewGroup rootView) {

		carhelp_rg = (RadioGroup) rootView.findViewById(R.id.carhelp_rg);
		carhelp_dashboard_rb = (RadioButton) rootView.findViewById(R.id.carhelp_dashboard_rb);
		carhelp_top_pedestrian_rb = (RadioButton) rootView.findViewById(R.id.carhelp_top_pedestrian_rb);
		carhelp_top_carbody_rb = (RadioButton) rootView.findViewById(R.id.carhelp_top_carbody_rb);
		carhelp_top_air_rb = (RadioButton) rootView.findViewById(R.id.carhelp_top_air_rb);
		carhelp_top_power_rb = (RadioButton) rootView.findViewById(R.id.carhelp_top_power_rb);

		layout_dashboard_ll = (LinearLayout) rootView.findViewById(R.id.layout_dashboard_ll);
		layout_pedestrian_ll = (LinearLayout) rootView.findViewById(R.id.layout_pedestrian_ll);
		layout_carbody_ll = (LinearLayout) rootView.findViewById(R.id.layout_carbody_ll);
		layout_carair_ll = (LinearLayout) rootView.findViewById(R.id.layout_carair_ll);
		layout_power_ll = (LinearLayout) rootView.findViewById(R.id.layout_power_ll);

		if (null != carhelp_rg) {
			carhelp_rg.setOnCheckedChangeListener(this);
		}
		carhelp_dashboard_rb.setChecked(true);
		showCarHelpUI(true, false, false, false, false);

		carhelp_timeset_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_timeset_rl);
		carhelp_speed_warning_adjust_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_speed_warning_adjust_rl);
		carhelp_fatigue_driving_adjust_rl = (RelativeLayout) rootView
				.findViewById(R.id.carhelp_fatigue_driving_adjust_rl);
		carhelp_speed_warning_ck = (CheckBox) rootView.findViewById(R.id.carhelp_speed_warning_ck);
		carhelp_fatigue_driving_ck = (CheckBox) rootView.findViewById(R.id.carhelp_fatigue_driving_ck);
		carhelp_speed_warning_sb = (CustomSeekbar) rootView.findViewById(R.id.carhelp_speed_warning_sb);
		carhelp_fatigue_driving_sb = (CustomSeekbar) rootView.findViewById(R.id.carhelp_fatigue_driving_sb);
		carhelp_speed_warning_tv = (TextView) rootView.findViewById(R.id.carhelp_speed_warning_tv);
		carhelp_fatigue_driving_tv = (TextView) rootView.findViewById(R.id.carhelp_fatigue_driving_tv);
		carhelp_dashboard_backlight_tv = (TextView) rootView.findViewById(R.id.carhelp_dashboard_backlight_tv);
		carhelp_dashboard_backlight_reduce = (ImageView) rootView.findViewById(R.id.carhelp_dashboard_backlight_reduce);
		carhelp_dashboard_backlight_add = (ImageView) rootView.findViewById(R.id.carhelp_dashboard_backlight_add);

		carhelp_speed_warning_ck
				.setChecked(DataShared.getInstance(mContext).getInt(SettingConstants.key_speed_warning_switch,
						SettingConstants.speed_warning_switch) == 1 ? true : false);
		carhelp_fatigue_driving_ck
				.setChecked(DataShared.getInstance(mContext).getInt(SettingConstants.key_fatigue_driving_switch,
						SettingConstants.fatigue_driving_switch) == 1 ? true : false);

		carhelp_speed_warning_tv.setText(T19CanTx.getInstance().changeSpeed(
				DataShared.getInstance(mContext).getInt(SettingConstants.key_speed_warning, 1), 20, 0, 130, 30)
				+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
		set_fatigue_driving(DataShared.getInstance(mContext).getInt(SettingConstants.key_fatigue_driving, 1) - 1);

		// 行人
		carhelp_pedestrian_reminder_ck = (CheckBox) rootView.findViewById(R.id.carhelp_pedestrian_reminder_ck);
		carhelp_set_volume_tv = (TextView) rootView.findViewById(R.id.carhelp_set_volume_tv);
		carhelp_set_sound_tv = (TextView) rootView.findViewById(R.id.carhelp_set_sound_tv);
		carhelp_set_volume_reduce = (ImageView) rootView.findViewById(R.id.carhelp_set_volume_reduce);
		carhelp_set_volume_add = (ImageView) rootView.findViewById(R.id.carhelp_set_volume_add);
		carhelp_set_sound_reduce = (ImageView) rootView.findViewById(R.id.carhelp_set_sound_reduce);
		carhelp_set_sound_add = (ImageView) rootView.findViewById(R.id.carhelp_set_sound_add);
		carhelp_set_volume_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_set_volume_rl);
		carhelp_set_sound_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_set_sound_rl);

		// 车身
		carhelp_carbody_atmosphere_lamp_rl = (RelativeLayout) rootView
				.findViewById(R.id.carhelp_carbody_atmosphere_lamp_rl);
		carhelp_carbody_decorative_lamp_rl = (RelativeLayout) rootView
				.findViewById(R.id.carhelp_carbody_decorative_lamp_rl);
		carhelp_carbody_atmosphere_lamp_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_atmosphere_lamp_ck);
		carhelp_carbody_decorative_lamp_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_decorative_lamp_ck);
		carhelp_carbody_rearview_mirror_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_rearview_mirror_ck);
		carhelp_carbody_auto_locking_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_auto_locking_ck);
		carhelp_carbody_set_gohome_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_set_gohome_ck);
		carhelp_carbody_auto_skylight_ck = (CheckBox) rootView.findViewById(R.id.carhelp_carbody_auto_skylight_ck);
		carhelp_carbody_speed_limit_warning_ck = (CheckBox) rootView
				.findViewById(R.id.carhelp_carbody_speed_limit_warning_ck);
		carhelp_carbody_set_prevention_warn_tv = (TextView) rootView
				.findViewById(R.id.carhelp_carbody_set_prevention_warn_tv);
		carhelp_carbody_set_prevention_warn_reduce = (ImageView) rootView
				.findViewById(R.id.carhelp_carbody_set_prevention_warn_reduce);
		carhelp_carbody_set_prevention_warn_add = (ImageView) rootView
				.findViewById(R.id.carhelp_carbody_set_prevention_warn_add);
		carhelp_carbody_atmosphere_sb = (CustomSeekbar) rootView.findViewById(R.id.carhelp_carbody_atmosphere_sb);
		carhelp_carbody_atmosphere_tv = (TextView) rootView.findViewById(R.id.carhelp_carbody_atmosphere_tv);

		carhelp_carbody_speed_limit_warning_ck.setChecked(DataShared.getInstance(mContext)
				.getBoolean(SettingConstants.key_intelligent_speed_limit_warning, false));

		// 空调
		carhelp_air_auto_cleaning_ck = (CheckBox) rootView.findViewById(R.id.carhelp_air_auto_cleaning_ck);
		carhelp_auto_aeration_ck = (CheckBox) rootView.findViewById(R.id.carhelp_auto_aeration_ck);
		carhelp_remember_air_ck = (CheckBox) rootView.findViewById(R.id.carhelp_remember_air_ck);
		carhelp_air_showtime_tv = (TextView) rootView.findViewById(R.id.carhelp_air_showtime_tv);
		carhelp_air_showtime_reduce = (ImageView) rootView.findViewById(R.id.carhelp_air_showtime_reduce);
		carhelp_air_showtime_add = (ImageView) rootView.findViewById(R.id.carhelp_air_showtime_add);

		if (DataShared.getInstance(mContext).getInt(SettingConstants.key_air_show_time, 15) == 45) {
			carhelp_air_showtime_tv.setText(mContext.getResources().getString(R.string.carhelp_air_show_time));
		} else {
			carhelp_air_showtime_tv
					.setText(DataShared.getInstance(mContext).getInt(SettingConstants.key_air_show_time, 15)
							+ mContext.getResources().getString(R.string.carhelp_time_unit_tv));
		}

		// 动力
		carhelp_cESC_ck = (CheckBox) rootView.findViewById(R.id.carhelp_cESC_ck);
		carhelp_eHAC_up_ck = (CheckBox) rootView.findViewById(R.id.carhelp_eHAC_up_ck);
		carhelp_eHDC_down_ck = (CheckBox) rootView.findViewById(R.id.carhelp_eHDC_down_ck);
		carhelp_eAuto_Hold_ck = (CheckBox) rootView.findViewById(R.id.carhelp_eAuto_Hold_ck);
		carhelp_speed_limit_ck = (CheckBox) rootView.findViewById(R.id.carhelp_speed_limit_ck);
		carhelp_long_range_endurance_ck = (CheckBox) rootView.findViewById(R.id.carhelp_long_range_endurance_ck);
		carhelp_backcar_enable_ck = (CheckBox) rootView.findViewById(R.id.carhelp_backcar_enable_ck);
		carhelp_eAuto_Hold_setTime_tv = (TextView) rootView.findViewById(R.id.carhelp_eAuto_Hold_setTime_tv);
		carhelp_eAuto_Hold_setTime_reduce = (ImageView) rootView.findViewById(R.id.carhelp_eAuto_Hold_setTime_reduce);
		carhelp_eAuto_Hold_setTime_add = (ImageView) rootView.findViewById(R.id.carhelp_eAuto_Hold_setTime_add);
		carhelp_speed_limit_sb = (CustomSeekbar) rootView.findViewById(R.id.carhelp_speed_limit_sb);
		carhelp_speed_limit_tv = (TextView) rootView.findViewById(R.id.carhelp_speed_limit_tv);
		carhelp_long_range_endurance_time = (TextView) rootView.findViewById(R.id.carhelp_long_range_endurance_time);

		carhelp_eAuto_Hold_setTime_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_eAuto_Hold_setTime_rl);
		carhelp_backcar_enable_rl = (RelativeLayout) rootView.findViewById(R.id.carhelp_backcar_enable_rl);

		common_set_ll = (LinearLayout) rootView.findViewById(R.id.common_set_ll);
		set_time_ll = (LinearLayout) rootView.findViewById(R.id.set_time_ll);

		year = (CustomPickerView) rootView.findViewById(R.id.new_year);
		month = (CustomPickerView) rootView.findViewById(R.id.new_month);
		day = (CustomPickerView) rootView.findViewById(R.id.new_day);
		hour = (CustomPickerView) rootView.findViewById(R.id.new_hour);
		mins = (CustomPickerView) rootView.findViewById(R.id.new_mins);

		year_up_iv = (ImageView) rootView.findViewById(R.id.year_up_iv);
		year_down_iv = (ImageView) rootView.findViewById(R.id.year_down_iv);
		month_up_iv = (ImageView) rootView.findViewById(R.id.month_up_iv);
		month_down_iv = (ImageView) rootView.findViewById(R.id.month_down_iv);
		day_up_iv = (ImageView) rootView.findViewById(R.id.day_up_iv);
		day_down_iv = (ImageView) rootView.findViewById(R.id.day_down_iv);
		hour_up_iv = (ImageView) rootView.findViewById(R.id.hour_up_iv);
		hour_down_iv = (ImageView) rootView.findViewById(R.id.hour_down_iv);
		mins_up_iv = (ImageView) rootView.findViewById(R.id.mins_up_iv);
		mins_down_iv = (ImageView) rootView.findViewById(R.id.mins_down_iv);
		data_time_gps = (Button) rootView.findViewById(R.id.data_time_gps);
		settime_sure = (Button) rootView.findViewById(R.id.settime_sure);
		settime_cancle = (Button) rootView.findViewById(R.id.settime_cancle);

		year_up_iv.setOnClickListener(this);
		year_down_iv.setOnClickListener(this);
		month_up_iv.setOnClickListener(this);
		month_down_iv.setOnClickListener(this);
		day_up_iv.setOnClickListener(this);
		day_down_iv.setOnClickListener(this);
		hour_up_iv.setOnClickListener(this);
		hour_down_iv.setOnClickListener(this);
		mins_up_iv.setOnClickListener(this);
		mins_down_iv.setOnClickListener(this);
		data_time_gps.setOnClickListener(this);
		settime_sure.setOnClickListener(this);
		settime_cancle.setOnClickListener(this);

		carhelp_timeset_rl.setOnClickListener(this);
		carhelp_speed_warning_sb.setOnProgressChangedListener(this);
		carhelp_fatigue_driving_sb.setOnProgressChangedListener(this);
		carhelp_dashboard_backlight_reduce.setOnClickListener(this);
		carhelp_dashboard_backlight_add.setOnClickListener(this);
		carhelp_set_volume_reduce.setOnClickListener(this);
		carhelp_set_volume_add.setOnClickListener(this);
		carhelp_set_sound_reduce.setOnClickListener(this);
		carhelp_set_sound_add.setOnClickListener(this);
		carhelp_carbody_set_prevention_warn_reduce.setOnClickListener(this);
		carhelp_carbody_set_prevention_warn_add.setOnClickListener(this);
		carhelp_carbody_atmosphere_sb.setOnProgressChangedListener(this);
		carhelp_air_showtime_reduce.setOnClickListener(this);
		carhelp_air_showtime_add.setOnClickListener(this);
		carhelp_eAuto_Hold_setTime_reduce.setOnClickListener(this);
		carhelp_eAuto_Hold_setTime_add.setOnClickListener(this);
		carhelp_speed_limit_sb.setOnProgressChangedListener(this);

		setCheckedChangeListenerDashboard();
		setCheckedChangeListenerPedestrian();
		setCheckedChangeListenerCarbody();
		setCheckedChangeListenerAir();
		setCheckedChangeListenerPower();

	}

	public void initTime() {
		Calendar c = Calendar.getInstance();
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH) + 1;// 通过Calendar算出的月数要+1
		int curDate = c.get(Calendar.DATE);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMin = c.get(Calendar.MINUTE);

		initYear(curYear);
		initMonth(curMonth);
		maxDay = getDay(curYear, curMonth);
		initDay(curDate);
		initHour(curHour);
		initMins(curMin);

	};

	private void initYear(int cur_year) {
		int cur_index = 0;
		for (int i = 0; i < yearRange; i++) {
			year_dataList.add("" + (i + 2000));
			if (year_dataList.get(i).equals(String.valueOf(cur_year))) {
				cur_index = i;
			}
		}
		year.setDataList(year_dataList);
		year.moveTo(cur_index);
		year.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int curIndex) {
				year_value = Integer.parseInt(year_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----year_value: " + year_value);
			}

			@Override
			public void onScrollFinished(int curIndex) {
				year_value = Integer.parseInt(year_dataList.get(curIndex));
				maxDay = getDay(year_value, Integer.parseInt(month_dataList.get(month.getCurIndex())));
				if (day_value > maxDay) {
					day_value = maxDay;
				}
				if ((day_value - 1) >= 0) {
					initDay(Integer.parseInt(day_dataList.get(day_value - 1)));
				} else {
					initDay(Integer.parseInt(day_dataList.get(0)));
				}
				Log.i(TAG, "onScrollFinished-----year_value: " + year_value + "   maxDay: " + maxDay);
			}
		});
	}

	private void initMonth(int cur_month) {
		int cur_index = 0;
		for (int i = 1; i < monthRange + 1; i++) {
			if (i < 10) {
				month_dataList.add("0" + i);
				if (month_dataList.get(i - 1).equals(("0" + cur_month))) {
					cur_index = i - 1;
				}
			} else {
				month_dataList.add("" + i);
				if (month_dataList.get(i - 1).equals(String.valueOf(cur_month))) {
					cur_index = i - 1;
				}
			}

		}
		month.setDataList(month_dataList);
		month.moveTo(cur_index);
		month.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int curIndex) {
				month_value = Integer.parseInt(month_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----month_value: " + month_value);
			}

			@Override
			public void onScrollFinished(int curIndex) {
				month_value = Integer.parseInt(month_dataList.get(curIndex));
				maxDay = getDay(Integer.parseInt(year_dataList.get(year.getCurIndex())), month_value);
				if (day_value > maxDay) {
					day_value = maxDay;
				}
				if ((day_value - 1) >= 0) {
					initDay(Integer.parseInt(day_dataList.get(day_value - 1)));
				} else {
					initDay(Integer.parseInt(day_dataList.get(0)));
				}

				Log.i(TAG, "onScrollFinished-----month_value: " + month_value + "   maxDay: " + maxDay);
			}
		});
	}

	private void initDay(int cur_day) {
		int cur_index = 0;
		if (day_dataList != null) {
			day_dataList.clear();
		}
		for (int i = 1; i < maxDay + 1; i++) {
			if (i < 10) {
				day_dataList.add("0" + i);
				if (day_dataList.get(i - 1).equals("0" + cur_day)) {
					cur_index = i - 1;
				}
			} else {
				day_dataList.add("" + i);
				if (day_dataList.get(i - 1).equals(String.valueOf(cur_day))) {
					cur_index = i - 1;
				}
			}

		}
		day.setDataList(day_dataList);
		day.moveTo(cur_index);
		day.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int curIndex) {
				day_value = Integer.parseInt(day_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----day_value: " + day_value);
			}

			@Override
			public void onScrollFinished(int curIndex) {
				day_value = Integer.parseInt(day_dataList.get(curIndex));
				Log.i(TAG, "onScrollFinished-----day_value: " + day_value);
			}
		});
	}

	private void initHour(int cur_hour) {
		int cur_index = 0;
		for (int i = 0; i < hourRange; i++) {
			if (i < 10) {
				hour_dataList.add("0" + i);
				if (hour_dataList.get(i).equals("0" + cur_hour)) {
					cur_index = i;
				}
			} else {
				hour_dataList.add("" + i);
				if (hour_dataList.get(i).equals(String.valueOf(cur_hour))) {
					cur_index = i;
				}
			}
		}
		hour.setDataList(hour_dataList);
		hour.moveTo(cur_index);
		hour.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int curIndex) {
				hour_value = Integer.parseInt(hour_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----hour_value: " + hour_value);
			}

			@Override
			public void onScrollFinished(int curIndex) {
				hour_value = Integer.parseInt(hour_dataList.get(curIndex));
				Log.i(TAG, "onScrollFinished-----hour_value: " + hour_value);
			}
		});
	}

	private void initMins(int cur_mins) {
		int cur_index = 0;
		for (int i = 0; i < minsRange; i++) {
			if (i < 10) {
				mins_dataList.add("0" + i);
				if (mins_dataList.get(i).equals("0" + cur_mins)) {
					cur_index = i;
				}
			} else {
				mins_dataList.add("" + i);
				if (mins_dataList.get(i).equals(String.valueOf(cur_mins))) {
					cur_index = i;
				}
			}
		}
		mins.setDataList(mins_dataList);
		mins.moveTo(cur_index);
		mins.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int curIndex) {
				mins_value = Integer.parseInt(mins_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----mins_value: " + mins_value);
			}

			@Override
			public void onScrollFinished(int curIndex) {
				mins_value = Integer.parseInt(mins_dataList.get(curIndex));
				Log.i(TAG, "onScrollChanged-----mins_value: " + mins_value);
			}
		});
	}

	public void setCheckedChangeListenerPedestrian() {

		// 行人提醒
		carhelp_pedestrian_reminder_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub

				carhelp_set_volume_rl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				carhelp_set_sound_rl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				pedestrian_reminder_refreshSysUI(isChecked);

				if (cButton.isPressed()) {
					if (null != mPedestrianInfo) {
						mPedestrianInfo.AVAS_SwSts = isChecked ? 2 : 1;
						mPedestrianInfo.AVAS_VolumeSts = 0;
						mPedestrianInfo.AVAS_AudioSourceSts = 0;
						T19CanTx.getInstance().sendPedestrianData(mPedestrianInfo);
					}
				}
			}
		});

	}

	public void setCheckedChangeListenerCarbody() {

		// 氛围灯
		carhelp_carbody_atmosphere_lamp_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_AmbientLightEnableSts = isChecked ? 2 : 1;
						sendCarbodyData(mCarbodyInfo.BCM_AmbientLightEnableSts, 0, 0, 0, 0, 0, 0, 0);
					}
				}

			}
		});
		// 装饰灯
		carhelp_carbody_decorative_lamp_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_DRL_EnableSts = isChecked ? 2 : 1;
						sendCarbodyData(0, mCarbodyInfo.BCM_DRL_EnableSts, 0, 0, 0, 0, 0, 0);
					}
				}

			}
		});
		// 外后视镜自动折叠
		carhelp_carbody_rearview_mirror_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_MirrorFoldEnableSts = isChecked ? 2 : 1;
						sendCarbodyData(0, 0, mCarbodyInfo.BCM_MirrorFoldEnableSts, 0, 0, 0, 0, 0);
					}
				}
			}
		});
		// 自动落锁
		carhelp_carbody_auto_locking_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_AutoLockEnableSts = isChecked ? 2 : 1;
						sendCarbodyData(0, 0, 0, mCarbodyInfo.BCM_AutoLockEnableSts, 0, 0, 0, 0);
					}
				}

			}
		});
		// 伴我回家
		carhelp_carbody_set_gohome_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_FollowMeSts = isChecked ? 2 : 1;
						sendCarbodyData(0, 0, 0, 0, mCarbodyInfo.BCM_FollowMeSts, 0, 0, 0);
					}
				}

			}
		});
		// 设置自动关闭天窗
		carhelp_carbody_auto_skylight_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarbodyInfo) {
						mCarbodyInfo.BCM_SunroofAutoCloseEnableSts = isChecked ? 2 : 1;
						sendCarbodyData(0, 0, 0, 0, 0, 0, mCarbodyInfo.BCM_SunroofAutoCloseEnableSts, 0);
					}
				}
			}
		});

		//
		carhelp_carbody_speed_limit_warning_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				DataShared.getInstance(mContext).putBoolean(SettingConstants.key_intelligent_speed_limit_warning,
						isChecked);
				DataShared.getInstance(mContext).commit();
			}
		});

	}

	public void setCheckedChangeListenerDashboard() {
		// 车辆报警设置
		carhelp_speed_warning_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub

				int ICM_OverSpdValue = DataShared.getInstance(mContext).getInt(SettingConstants.key_speed_warning, 1);
				if (isChecked) {
					carhelp_speed_warning_adjust_rl.setVisibility(View.VISIBLE);
					carhelp_speed_warning_sb.setProgress(ICM_OverSpdValue - 1);
					carhelp_speed_warning_tv
							.setText(T19CanTx.getInstance().changeSpeed(ICM_OverSpdValue - 1, 20, 0, 130, 30)
									+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
					if (cButton.isPressed()) {
						if (mDashboardInfo != null) {
							mDashboardInfo.ICM_OverSpdValue = ICM_OverSpdValue;
							sendDashboardData(0x1F, 0x3F, mDashboardInfo.ICM_OverSpdValue, 0, 0);
						}
					}
				} else {
					carhelp_speed_warning_adjust_rl.setVisibility(View.GONE);
					if (cButton.isPressed()) {
						if (mDashboardInfo != null) {
							mDashboardInfo.ICM_OverSpdValue = 0x3F;
							sendDashboardData(0x1F, 0x3F, mDashboardInfo.ICM_OverSpdValue, 0, 0);
						}
					}
				}

				DataShared.getInstance(mContext).putInt(SettingConstants.key_speed_warning_switch, isChecked ? 1 : 0);
				DataShared.getInstance(mContext).commit();

			}

		});
		// 疲劳驾驶设置
		carhelp_fatigue_driving_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub

				int ICM_FatigureDrivingTimeSet = DataShared.getInstance(mContext)
						.getInt(SettingConstants.key_fatigue_driving, 1);

				if (isChecked) {
					carhelp_fatigue_driving_adjust_rl.setVisibility(View.VISIBLE);
					carhelp_fatigue_driving_sb.setProgress(ICM_FatigureDrivingTimeSet - 1);
					set_fatigue_driving(ICM_FatigureDrivingTimeSet - 1);

					if (cButton.isPressed()) {
						if (mDashboardInfo != null) {
							mDashboardInfo.ICM_FatigureDrivingTimeSet = ICM_FatigureDrivingTimeSet;
							sendDashboardData(0x1F, 0x3F, 0, mDashboardInfo.ICM_FatigureDrivingTimeSet, 0);
						}
					}

				} else {
					carhelp_fatigue_driving_adjust_rl.setVisibility(View.GONE);

					if (cButton.isPressed()) {
						if (mDashboardInfo != null) {
							mDashboardInfo.ICM_FatigureDrivingTimeSet = 0x0F;
							sendDashboardData(0x1F, 0x3F, 0, mDashboardInfo.ICM_FatigureDrivingTimeSet, 0);
						}
					}
				}

				DataShared.getInstance(mContext).putInt(SettingConstants.key_fatigue_driving_switch, isChecked ? 1 : 0);
				DataShared.getInstance(mContext).commit();

			}

		});
	}

	public void setCheckedChangeListenerAir() {
		// 空调自动清洁
		carhelp_air_auto_cleaning_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarHelpAirInfo) {

						mCarHelpAirInfo.ACCM_AutoCleanEnableSts = isChecked ? 2 : 1;
						sendCarHelpAirData(mCarHelpAirInfo.ACCM_AutoCleanEnableSts, 0, 0, 0, 0, 0);

					}
				}
			}
		});
		// 车内自动通风
		carhelp_auto_aeration_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarHelpAirInfo) {
						mCarHelpAirInfo.ACCM_AutoBlowEnableSts = isChecked ? 2 : 1;
						sendCarHelpAirData(0, mCarHelpAirInfo.ACCM_AutoBlowEnableSts, 0, 0, 0, 0);
					}
				}
			}
		});

		// 空调记忆状态
		carhelp_remember_air_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mCarHelpAirInfo) {
						mCarHelpAirInfo.IHU_ACPMemoryMode = isChecked ? 2 : 1;
						sendCarHelpAirData(0, 0, mCarHelpAirInfo.IHU_ACPMemoryMode, 0, 0, 0);
					}
				}
			}
		});
	}

	public void setCheckedChangeListenerPower() {
		// cESC(驱动防滑)
		carhelp_cESC_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_cESC_EnableSts = isChecked ? 2 : 1;
						sendPowerInfoData(mPowerInfo.VCU_cESC_EnableSts, 0, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0,
								0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
					}
				}

			}
		});
		// eHAC(上坡辅助)
		carhelp_eHAC_up_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_eHAC_EnableSts = isChecked ? 2 : 1;
						sendPowerInfoData(0, mPowerInfo.VCU_eHAC_EnableSts, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0,
								0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
					}
				}

			}
		});
		// eHAC(坡道缓降)
		carhelp_eHDC_down_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_eHDC_EnableSts = isChecked ? 2 : 1;
						sendPowerInfoData(0, 0, mPowerInfo.VCU_eHDC_EnableSts, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0,
								0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
					}
				}
			}
		});
		// eAuto Hold(自动驻车)
		carhelp_eAuto_Hold_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					carhelp_eAuto_Hold_setTime_rl.setVisibility(View.VISIBLE);
					carhelp_backcar_enable_rl.setVisibility(View.VISIBLE);
				} else {
					carhelp_eAuto_Hold_setTime_rl.setVisibility(View.GONE);
					carhelp_backcar_enable_rl.setVisibility(View.GONE);
				}
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_eAVH_EnableSts = isChecked ? 2 : 1;
						sendPowerInfoData(0, 0, 0, mPowerInfo.VCU_eAVH_EnableSts, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0,
								0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
					}
				}
			}
		});
		// 车速限制调节
		carhelp_speed_limit_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_MaxSpdLimitEnableSts = isChecked ? 2 : 1;
						// T19CanTx.getInstance().sendPowerInfoData(mPowerInfo);
					}
				}
			}
		});
		// 长程续航
		carhelp_long_range_endurance_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (cButton.isPressed()) {
					if (null != mPowerInfo) {
						mPowerInfo.VCU_LongRangeModeEnableSts = isChecked ? 2 : 1;
						sendPowerInfoData(0, 0, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0,
								mPowerInfo.VCU_LongRangeModeEnableSts, 0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
					}
				}
			}
		});

		// 倒车禁用
		carhelp_backcar_enable_ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cButton, boolean isChecked) {
				// TODO Auto-generated method stub
				if (null != mPowerInfo) {
					mPowerInfo.VCU_eAVH_ReverseDisableSts = isChecked ? 2 : 1;
					sendPowerInfoData(0, 0, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, mPowerInfo.VCU_eAVH_ReverseDisableSts,
							0, 0, mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
				}
			}
		});

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.carhelp_timeset_rl:
			isSetTime = true;
			common_set_ll.setVisibility(View.GONE);
			set_time_ll.setVisibility(View.VISIBLE);
			initTime();
			mbAutoTime = timeUtils.getAutoState(mContext, Settings.Global.AUTO_TIME);
			setSwitchOnUI(mContext, data_time_gps, mbAutoTime);
			break;
		case R.id.year_up_iv:
			int up_year_index = year.getCurIndex();
			up_year_index--;
			if (up_year_index < 0) {
				up_year_index = yearRange - 1;
			}
			year.moveTo(up_year_index);
			break;
		case R.id.year_down_iv:
			int down_year_index = year.getCurIndex();
			down_year_index++;
			if (down_year_index > yearRange - 1) {
				down_year_index = 0;
			}
			year.moveTo(down_year_index);
			break;
		case R.id.month_up_iv:
			int up_month_index = month.getCurIndex();
			up_month_index--;
			if (up_month_index < 0) {
				up_month_index = monthRange - 1;
			}
			month.moveTo(up_month_index);
			break;
		case R.id.month_down_iv:
			int down_month_index = month.getCurIndex();
			down_month_index++;
			if (down_month_index > monthRange - 1) {
				down_month_index = 0;
			}
			month.moveTo(down_month_index);
			break;
		case R.id.day_up_iv:
			int up_day_index = day.getCurIndex();
			up_day_index--;
			if (up_day_index < 0) {
				up_day_index = maxDay - 1;
			}
			day.moveTo(up_day_index);
			break;
		case R.id.day_down_iv:
			int down_day_index = day.getCurIndex();
			down_day_index++;
			if (down_day_index > maxDay - 1) {
				down_day_index = 0;
			}
			day.moveTo(down_day_index);
			break;
		case R.id.hour_up_iv:
			int up_hour_index = hour.getCurIndex();
			up_hour_index--;
			if (up_hour_index < 0) {
				up_hour_index = hourRange - 1;
			}
			hour.moveTo(up_hour_index);
			break;
		case R.id.hour_down_iv:
			int down_hour_index = hour.getCurIndex();
			down_hour_index++;
			if (down_hour_index > hourRange - 1) {
				down_hour_index = 0;
			}
			hour.moveTo(down_hour_index);
			break;
		case R.id.mins_up_iv:
			int up_mins_index = mins.getCurIndex();
			up_mins_index--;
			if (up_mins_index < 0) {
				up_mins_index = minsRange - 1;
			}
			mins.moveTo(up_mins_index);
			break;
		case R.id.mins_down_iv:
			int down_mins_index = mins.getCurIndex();
			down_mins_index++;
			if (down_mins_index > minsRange - 1) {
				down_mins_index = 0;
			}
			mins.moveTo(down_mins_index);
			break;
		case R.id.data_time_gps:
			mbAutoTime = !mbAutoTime;
			setSwitchOnUI(mContext, data_time_gps, mbAutoTime);
			Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, mbAutoTime ? 1 : 0);
			if (mMcuManager == null) {
				mMcuManager = (McuManager) ApplicationManage.getContext().getSystemService(Context.MCU_SERVICE);
			}
			BootService.sendTimeMcu(mMcuManager);
			if (timeUtils.mLocaltionListener != null)
				timeUtils.mLocaltionListener.LocationListener(mbAutoTime);
			break;
		case R.id.settime_sure:
			int mYear = Integer.parseInt(year_dataList.get(year.getCurIndex()));
			int mMonth = Integer.parseInt(month_dataList.get(month.getCurIndex())) - 1;
			int mDay = Integer.parseInt(day_dataList.get(day.getCurIndex()));
			int mHour = Integer.parseInt(hour_dataList.get(hour.getCurIndex()));
			int mMins = Integer.parseInt(mins_dataList.get(mins.getCurIndex()));
			if (!timeUtils.getAutoState(mContext, Settings.Global.AUTO_TIME)) {
				timeUtils.setDateTime(mContext, mYear, mMonth, mDay, mHour, mMins);
			}
			common_set_ll.setVisibility(View.VISIBLE);
			set_time_ll.setVisibility(View.GONE);
			isSetTime = false;
			break;
		case R.id.settime_cancle:
			common_set_ll.setVisibility(View.VISIBLE);
			set_time_ll.setVisibility(View.GONE);
			isSetTime = false;
			break;
		case R.id.carhelp_dashboard_backlight_reduce:
			String reduce_backlight_str = carhelp_dashboard_backlight_tv.getText().toString();
			if (!TextUtils.isEmpty(reduce_backlight_str)) {
				int reduce_dashboard_backlight = Integer.parseInt(reduce_backlight_str);
				reduce_dashboard_backlight--;
				if (reduce_dashboard_backlight < 1) {
					reduce_dashboard_backlight = 1;
				}
				carhelp_dashboard_backlight_tv.setText(reduce_dashboard_backlight + "");
				if (null != mDashboardInfo) {
					mDashboardInfo.IHU_BrightnessAdjustSet_ICM = reduce_dashboard_backlight;
					sendDashboardData(0x1F, 0x3F, 0, 0, mDashboardInfo.IHU_BrightnessAdjustSet_ICM);
				}

			}
			break;

		case R.id.carhelp_dashboard_backlight_add:
			String add_backlight_str = carhelp_dashboard_backlight_tv.getText().toString();
			if (!TextUtils.isEmpty(add_backlight_str)) {
				int add_dashboard_backlight = Integer.parseInt(add_backlight_str);
				add_dashboard_backlight++;
				if (add_dashboard_backlight > 10) {
					add_dashboard_backlight = 10;
				}
				carhelp_dashboard_backlight_tv.setText(add_dashboard_backlight + "");
				if (null != mDashboardInfo) {
					mDashboardInfo.IHU_BrightnessAdjustSet_ICM = add_dashboard_backlight;
					sendDashboardData(0x1F, 0x3F, 0, 0, mDashboardInfo.IHU_BrightnessAdjustSet_ICM);
				}

			}
			break;

		case R.id.carhelp_set_volume_reduce:
			String reduceStr = carhelp_set_volume_tv.getText().toString();
			if (!TextUtils.isEmpty(reduceStr)) {
				int reduce_volume = Integer.parseInt(reduceStr);
				reduce_volume--;
				if (reduce_volume < 1) {
					reduce_volume = 1;
				}
				carhelp_set_volume_tv.setText(reduce_volume + "");
				if (null != mPedestrianInfo) {
					mPedestrianInfo.AVAS_VolumeSts = reduce_volume;

					mPedestrianInfo.AVAS_SwSts = 0;
					mPedestrianInfo.AVAS_AudioSourceSts = 0;
					T19CanTx.getInstance().sendPedestrianData(mPedestrianInfo);
				}

			}
			break;
		case R.id.carhelp_set_volume_add:
			String addStr = carhelp_set_volume_tv.getText().toString();
			if (!TextUtils.isEmpty(addStr)) {
				int add_volume = Integer.parseInt(addStr);
				add_volume++;
				if (add_volume > 3) {
					add_volume = 3;
				}
				carhelp_set_volume_tv.setText(add_volume + "");
				if (null != mPedestrianInfo) {
					mPedestrianInfo.AVAS_VolumeSts = add_volume;

					mPedestrianInfo.AVAS_SwSts = 0;
					mPedestrianInfo.AVAS_AudioSourceSts = 0;
					T19CanTx.getInstance().sendPedestrianData(mPedestrianInfo);
				}

			}
			break;

		// 设置行人提醒音效
		case R.id.carhelp_set_sound_reduce:
			if (null != mPedestrianInfo) {
				int current_sound_reduce = mPedestrianInfo.AVAS_AudioSourceSts;
				current_sound_reduce--;
				if (current_sound_reduce < 1) {
					current_sound_reduce = 3;
				}
				mPedestrianInfo.AVAS_AudioSourceSts = current_sound_reduce;
				carhelp_set_sound_tv.setText(mContext.getResources().getString(R.string.carhelp_set_sound)
						+ mPedestrianInfo.AVAS_AudioSourceSts);
				mPedestrianInfo.AVAS_SwSts = 0;
				mPedestrianInfo.AVAS_VolumeSts = 0;
				T19CanTx.getInstance().sendPedestrianData(mPedestrianInfo);
			}
			break;

		case R.id.carhelp_set_sound_add:
			if (null != mPedestrianInfo) {
				int current_sound_add = mPedestrianInfo.AVAS_AudioSourceSts;
				current_sound_add++;
				if (current_sound_add > 3) {
					current_sound_add = 1;
				}
				mPedestrianInfo.AVAS_AudioSourceSts = current_sound_add;
				carhelp_set_sound_tv.setText(mContext.getResources().getString(R.string.carhelp_set_sound)
						+ mPedestrianInfo.AVAS_AudioSourceSts);
				mPedestrianInfo.AVAS_SwSts = 0;
				mPedestrianInfo.AVAS_VolumeSts = 0;
				T19CanTx.getInstance().sendPedestrianData(mPedestrianInfo);
			}
			break;

		case R.id.carhelp_carbody_set_prevention_warn_reduce:
			if (null != mCarbodyInfo) {
				int current_prevention_warn_reduce = mCarbodyInfo.BCM_AntiTheftModeSts;
				current_prevention_warn_reduce--;
				if (current_prevention_warn_reduce < 1) {
					current_prevention_warn_reduce = 1;
				}
				mCarbodyInfo.BCM_AntiTheftModeSts = current_prevention_warn_reduce;
				showBCM_AntiTheftModeStsStatus(current_prevention_warn_reduce);
				sendCarbodyData(0, 0, 0, 0, 0, mCarbodyInfo.BCM_AntiTheftModeSts, 0, 0);
			}
			break;
		case R.id.carhelp_carbody_set_prevention_warn_add:
			if (null != mCarbodyInfo) {
				int current_prevention_warn_add = mCarbodyInfo.BCM_AntiTheftModeSts;
				current_prevention_warn_add++;
				if (current_prevention_warn_add > 3) {
					current_prevention_warn_add = 3;
				}
				mCarbodyInfo.BCM_AntiTheftModeSts = current_prevention_warn_add;
				showBCM_AntiTheftModeStsStatus(current_prevention_warn_add);
				sendCarbodyData(0, 0, 0, 0, 0, mCarbodyInfo.BCM_AntiTheftModeSts, 0, 0);
			}
			break;

		case R.id.carhelp_air_showtime_reduce:
			String air_showtime_reduce_Str = carhelp_air_showtime_tv.getText().toString();
			if (!TextUtils.isEmpty(air_showtime_reduce_Str)) {
				if (air_showtime_reduce_Str.equals(mContext.getResources().getString(R.string.carhelp_air_show_time))) {
					air_showtime_reduce_Str = mContext.getResources().getString(R.string.carhelp_45);
				}
				String reduce_result_str = air_showtime_reduce_Str
						.replace(mContext.getResources().getString(R.string.carhelp_time_unit_tv), "");
				int reduce_air_showtime = Integer.parseInt(reduce_result_str);
				reduce_air_showtime -= 15;
				if (reduce_air_showtime < 15) {
					reduce_air_showtime = 15;
				}
				DataShared.getInstance(mContext).putInt(SettingConstants.key_air_show_time, reduce_air_showtime);
				DataShared.getInstance(mContext).commit();
				carhelp_air_showtime_tv.setText(
						reduce_air_showtime + mContext.getResources().getString(R.string.carhelp_time_unit_tv));
			}
			break;
		case R.id.carhelp_air_showtime_add:
			String air_showtime_add_Str = carhelp_air_showtime_tv.getText().toString();
			if (!TextUtils.isEmpty(air_showtime_add_Str)) {
				if (air_showtime_add_Str.equals(mContext.getResources().getString(R.string.carhelp_air_show_time))) {
					air_showtime_add_Str = mContext.getResources().getString(R.string.carhelp_45);
				}
				String add_result_str = air_showtime_add_Str
						.replace(mContext.getResources().getString(R.string.carhelp_time_unit_tv), "");
				int add_air_showtime = Integer.parseInt(add_result_str);
				add_air_showtime += 15;
				if (add_air_showtime > 30) {
					add_air_showtime = 30;
				}
				DataShared.getInstance(mContext).putInt(SettingConstants.key_air_show_time, add_air_showtime);
				DataShared.getInstance(mContext).commit();
				if (add_air_showtime == 45) {
					carhelp_air_showtime_tv.setText(mContext.getResources().getString(R.string.carhelp_air_show_time));
				} else {
					carhelp_air_showtime_tv.setText(
							add_air_showtime + mContext.getResources().getString(R.string.carhelp_time_unit_tv));
				}

			}
			break;

		case R.id.carhelp_eAuto_Hold_setTime_reduce:
			String eAuto_Hold_setTime_reduce_Str = carhelp_eAuto_Hold_setTime_tv.getText().toString();
			if (!TextUtils.isEmpty(eAuto_Hold_setTime_reduce_Str)) {
				int eAuto_Hold_setTime_reduce = Integer.parseInt(eAuto_Hold_setTime_reduce_Str);
				eAuto_Hold_setTime_reduce--;
				if (eAuto_Hold_setTime_reduce < 1) {
					eAuto_Hold_setTime_reduce = 1;
				}
				mPowerInfo.VCU_eAVH_TimeSetSts = eAuto_Hold_setTime_reduce;
				carhelp_eAuto_Hold_setTime_tv.setText(eAuto_Hold_setTime_reduce + "");
				sendPowerInfoData(0, 0, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0, 0,
						mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
			}
			break;
		case R.id.carhelp_eAuto_Hold_setTime_add:
			String eAuto_Hold_setTime_add_Str = carhelp_eAuto_Hold_setTime_tv.getText().toString();
			if (!TextUtils.isEmpty(eAuto_Hold_setTime_add_Str)) {
				int eAuto_Hold_setTime_add = Integer.parseInt(eAuto_Hold_setTime_add_Str);
				eAuto_Hold_setTime_add++;
				if (eAuto_Hold_setTime_add > 5) {
					eAuto_Hold_setTime_add = 5;
				}
				mPowerInfo.VCU_eAVH_TimeSetSts = eAuto_Hold_setTime_add;
				carhelp_eAuto_Hold_setTime_tv.setText(eAuto_Hold_setTime_add + "");
				sendPowerInfoData(0, 0, 0, 0, mPowerInfo.VCU_eAVH_TimeSetSts, 0, 0, 0,
						mPowerInfo.VCU_SpeedLimitValueSetSts, 0, 0);
			}
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
			case R.id.carhelp_speed_warning_sb:
				Log.i(TAG, "carhelp_speed_warning_sb: " + msg.obj);
				if (null != mDashboardInfo && null != msg.obj) {
					int cur_Progress = Integer.parseInt(msg.obj.toString());
					carhelp_speed_warning_tv.setText(T19CanTx.getInstance().changeSpeed(cur_Progress, 20, 0, 130, 30)
							+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
					mDashboardInfo.ICM_OverSpdValue = cur_Progress + 1;
					DataShared.getInstance(mContext).putInt(SettingConstants.key_speed_warning,
							mDashboardInfo.ICM_OverSpdValue);
					DataShared.getInstance(mContext).commit();

					sendDashboardData(0x1F, 0x3F, mDashboardInfo.ICM_OverSpdValue, 0, 0);

				}
				break;
			case R.id.carhelp_fatigue_driving_sb:
				Log.i(TAG, "carhelp_fatigue_driving_sb: " + msg.obj);
				if (null != mDashboardInfo && null != msg.obj) {
					int cur_Progress = Integer.parseInt(msg.obj.toString());
					set_fatigue_driving(cur_Progress);
					mDashboardInfo.ICM_FatigureDrivingTimeSet = cur_Progress + 1;
					DataShared.getInstance(mContext).putInt(SettingConstants.key_fatigue_driving,
							mDashboardInfo.ICM_FatigureDrivingTimeSet);
					DataShared.getInstance(mContext).commit();

					sendDashboardData(0x1F, 0x3F, 0, mDashboardInfo.ICM_FatigureDrivingTimeSet, 0);

				}

				break;
			case R.id.carhelp_carbody_atmosphere_sb:
				Log.i(TAG, "carhelp_carbody_atmosphere_sb: " + msg.obj);
				carhelp_carbody_atmosphere_tv.setText(msg.obj.toString() + "");
				break;
			case R.id.carhelp_speed_limit_sb:
				Log.i(TAG, "carhelp_speed_limit_sb: " + msg.obj);
				if (null != mPowerInfo && null != msg.obj) {
					int cur_Progress = Integer.parseInt(msg.obj.toString());
					if (cur_Progress == 0) {
						carhelp_speed_limit_tv.setText(mContext.getResources().getString(R.string.carhelp_0));
					} else {
						carhelp_speed_limit_tv.setText(T19CanTx.getInstance().changeSpeed(cur_Progress, 19, 1, 120, 30)
								+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
					}
					mPowerInfo.VCU_SpeedLimitValueSetSts = cur_Progress;
					T19CanTx.getInstance().sendPowerInfoData(mPowerInfo);
				}
				break;
			default:
				break;
			}

		}
	};

	public void set_fatigue_driving(int cur_Progress) {
		switch (cur_Progress) {
		case 0:
			carhelp_fatigue_driving_tv.setText("1.0" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 1:
			carhelp_fatigue_driving_tv.setText("1.5" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 2:
			carhelp_fatigue_driving_tv.setText("2.0" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 3:
			carhelp_fatigue_driving_tv.setText("2.5" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 4:
			carhelp_fatigue_driving_tv.setText("3.0" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 5:
			carhelp_fatigue_driving_tv.setText("3.5" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;
		case 6:
			carhelp_fatigue_driving_tv.setText("4.0" + mContext.getResources().getString(R.string.carhelp_time_unit));
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case R.id.carhelp_dashboard_rb:
			showCarHelpUI(true, false, false, false, false);
			break;
		case R.id.carhelp_top_pedestrian_rb:
			showCarHelpUI(false, true, false, false, false);
			break;
		case R.id.carhelp_top_carbody_rb:
			showCarHelpUI(false, false, true, false, false);
			break;
		case R.id.carhelp_top_air_rb:
			showCarHelpUI(false, false, false, true, false);
			break;
		case R.id.carhelp_top_power_rb:
			showCarHelpUI(false, false, false, false, true);
			break;
		default:
			break;
		}

	}

	public void showCarHelpUI(boolean isDashboard, boolean isPedestrian, boolean isCarbody, boolean isAir,
			boolean ispower) {

		layout_dashboard_ll.setVisibility(isDashboard ? View.VISIBLE : View.GONE);
		layout_pedestrian_ll.setVisibility(isPedestrian ? View.VISIBLE : View.GONE);
		layout_carbody_ll.setVisibility(isCarbody ? View.VISIBLE : View.GONE);
		layout_carair_ll.setVisibility(isAir ? View.VISIBLE : View.GONE);
		layout_power_ll.setVisibility(ispower ? View.VISIBLE : View.GONE);

	}

	public void showBCM_AntiTheftModeStsStatus(int BCM_AntiTheftModeSts) {

		switch (BCM_AntiTheftModeSts) {
		case 1:
			carhelp_carbody_set_prevention_warn_tv
					.setText(mContext.getResources().getString(R.string.carhelp_carbody_set_prevention_warn_light) + "+"
							+ mContext.getResources().getString(R.string.carhelp_carbody_set_prevention_warn_horn));
			break;
		case 2:
			carhelp_carbody_set_prevention_warn_tv
					.setText(mContext.getResources().getString(R.string.carhelp_carbody_set_prevention_warn_light));
			break;
		case 3:
			carhelp_carbody_set_prevention_warn_tv
					.setText(mContext.getResources().getString(R.string.carhelp_carbody_set_prevention_warn_horn));
			break;

		default:
			break;
		}

	}

	// 刷新行人UI
	public void updatePedestrianUI(PedestrianInfo mPedestrianInfo) {

		if (null != mPedestrianInfo) {
			if (carhelp_pedestrian_reminder_ck != null) {
				if (mPedestrianInfo.AVAS_SwSts == 1) {
					carhelp_pedestrian_reminder_ck.setChecked(true);
					carhelp_set_volume_rl.setVisibility(View.VISIBLE);
					carhelp_set_sound_rl.setVisibility(View.VISIBLE);
				} else if (mPedestrianInfo.AVAS_SwSts == 0) {
					carhelp_pedestrian_reminder_ck.setChecked(false);
					carhelp_set_volume_rl.setVisibility(View.GONE);
					carhelp_set_sound_rl.setVisibility(View.GONE);
				}

			}
			if (carhelp_set_volume_tv != null) {
				if (mPedestrianInfo.AVAS_VolumeSts != 0) {
					carhelp_set_volume_tv.setText(mPedestrianInfo.AVAS_VolumeSts + "");
				}
			}
			if (carhelp_set_sound_tv != null) {
				if (mPedestrianInfo.AVAS_AudioSourceSts != 0) {
					carhelp_set_sound_tv.setText(mContext.getResources().getString(R.string.carhelp_set_sound)
							+ mPedestrianInfo.AVAS_AudioSourceSts);
				}
			}

		}

	}

	public void pedestrian_reminder_refreshSysUI(boolean isShow) {
		Intent intent = new Intent();
		intent.putExtra("isShow", isShow);
		intent.setAction("com.carocean.refresh.pedestrianReminder");
		mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
	}

	// 刷新仪表UI
	public void updateDashboardUI(DashboardInfo mDashboardInfo) {

		if (null != mDashboardInfo) {

			int speed_warning_switch = DataShared.getInstance(mContext)
					.getInt(SettingConstants.key_speed_warning_switch, SettingConstants.speed_warning_switch);
			int fatigue_driving_switch = DataShared.getInstance(mContext)
					.getInt(SettingConstants.key_fatigue_driving_switch, SettingConstants.fatigue_driving_switch);

			carhelp_speed_warning_ck.setChecked(speed_warning_switch == 1 ? true : false);
			carhelp_fatigue_driving_ck.setChecked(fatigue_driving_switch == 1 ? true : false);

			if (speed_warning_switch == 0) {
				carhelp_speed_warning_adjust_rl.setVisibility(View.GONE);
			} else if (speed_warning_switch == 1) {
				carhelp_speed_warning_adjust_rl.setVisibility(View.VISIBLE);
			}
			// 速度设置
			if (mDashboardInfo.ICM_OverSpdValue != 0) {
				if (mDashboardInfo.ICM_OverSpdValue == 0x3F) {
					carhelp_speed_warning_ck.setChecked(false);
				} else {
					carhelp_speed_warning_ck.setChecked(true);
					carhelp_speed_warning_sb.setProgress(mDashboardInfo.ICM_OverSpdValue - 1);
					carhelp_speed_warning_tv.setText(
							T19CanTx.getInstance().changeSpeed(mDashboardInfo.ICM_OverSpdValue - 1, 20, 0, 130, 30)
									+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
					DataShared.getInstance(mContext).putInt(SettingConstants.key_speed_warning,
							mDashboardInfo.ICM_OverSpdValue);
					DataShared.getInstance(mContext).commit();
				}
			}

			if (fatigue_driving_switch == 0) {
				carhelp_fatigue_driving_adjust_rl.setVisibility(View.GONE);
			} else if (fatigue_driving_switch == 1) {
				carhelp_fatigue_driving_adjust_rl.setVisibility(View.VISIBLE);
			}

			// 疲劳驾驶时间设置
			if (mDashboardInfo.ICM_FatigureDrivingTimeSet != 0) {
				if (mDashboardInfo.ICM_FatigureDrivingTimeSet == 0x0F) {
					carhelp_fatigue_driving_ck.setChecked(false);
				} else {
					carhelp_fatigue_driving_ck.setChecked(true);
					carhelp_fatigue_driving_sb.setProgress(mDashboardInfo.ICM_FatigureDrivingTimeSet - 1);
					set_fatigue_driving(mDashboardInfo.ICM_FatigureDrivingTimeSet - 1);
					DataShared.getInstance(mContext).putInt(SettingConstants.key_fatigue_driving,
							mDashboardInfo.ICM_FatigureDrivingTimeSet);
					DataShared.getInstance(mContext).commit();
				}
			}

			// 设置仪表背光
			if (mDashboardInfo.IHU_BrightnessAdjustSet_ICM != 0) {
				carhelp_dashboard_backlight_tv.setText(mDashboardInfo.IHU_BrightnessAdjustSet_ICM + "");
			}

		}

	}

	// 刷新车身UI
	public void updateCarbodyUI(CarbodyInfo mCarbodyInfo) {

		if (null != mCarbodyInfo) {

			// 氛围灯
			if (carhelp_carbody_atmosphere_lamp_ck != null) {

				if (mCarbodyInfo.BCM_AmbientLightEnableSts != 0) {
					carhelp_carbody_atmosphere_lamp_ck
							.setChecked(mCarbodyInfo.BCM_AmbientLightEnableSts == 2 ? true : false);
				}
			}

			// 装饰灯
			if (carhelp_carbody_decorative_lamp_ck != null) {
				if (mCarbodyInfo.BCM_DRL_EnableSts != 0) {
					carhelp_carbody_decorative_lamp_ck.setChecked(mCarbodyInfo.BCM_DRL_EnableSts == 2 ? true : false);
				}
			}

			// 外后视镜折叠
			if (carhelp_carbody_rearview_mirror_ck != null) {
				if (mCarbodyInfo.BCM_MirrorFoldEnableSts != 0) {
					carhelp_carbody_rearview_mirror_ck
							.setChecked(mCarbodyInfo.BCM_MirrorFoldEnableSts == 2 ? true : false);
				}

			}

			// 自动落锁
			if (carhelp_carbody_auto_locking_ck != null) {
				if (mCarbodyInfo.BCM_AutoLockEnableSts != 0) {
					carhelp_carbody_auto_locking_ck.setChecked(mCarbodyInfo.BCM_AutoLockEnableSts == 2 ? true : false);
				}

			}

			// 伴我回家
			if (carhelp_carbody_set_gohome_ck != null) {
				if (mCarbodyInfo.BCM_FollowMeSts != 0) {
					carhelp_carbody_set_gohome_ck.setChecked(mCarbodyInfo.BCM_FollowMeSts == 2 ? true : false);
				}

			}

			// 设防自动关闭天窗使能状态
			if (carhelp_carbody_auto_skylight_ck != null) {
				if (mCarbodyInfo.BCM_SunroofAutoCloseEnableSts != 0) {
					carhelp_carbody_auto_skylight_ck
							.setChecked(mCarbodyInfo.BCM_SunroofAutoCloseEnableSts == 2 ? true : false);
				}

			}

			// 设置设防提示模式
			showBCM_AntiTheftModeStsStatus(mCarbodyInfo.BCM_AntiTheftModeSts);
		}

	}

	// 刷新车助空调UI
	public void updateCarHelpAirUI(CarHelpAirInfo mCarHelpAirInfo) {

		if (null != mCarHelpAirInfo) {

			if (carhelp_air_auto_cleaning_ck != null) {
				if (mCarHelpAirInfo.ACCM_AutoCleanEnableSts != 0) {
					carhelp_air_auto_cleaning_ck
							.setChecked(mCarHelpAirInfo.ACCM_AutoCleanEnableSts == 2 ? true : false);
				}

			}

			if (carhelp_auto_aeration_ck != null) {
				if (mCarHelpAirInfo.ACCM_AutoBlowEnableSts != 0) {
					carhelp_auto_aeration_ck.setChecked(mCarHelpAirInfo.ACCM_AutoBlowEnableSts == 2 ? true : false);
				}

			}

			if (carhelp_remember_air_ck != null) {
				if (mCarHelpAirInfo.IHU_ACPMemoryMode != 0) {
					carhelp_remember_air_ck.setChecked(mCarHelpAirInfo.IHU_ACPMemoryMode == 2 ? true : false);
				}

			}
		}

	}

	// 刷新动力UI
	public void updatePowerUI(PowerInfo mPowerInfo) {

		if (null != mPowerInfo) {

			if (carhelp_cESC_ck != null) {
				if (mPowerInfo.VCU_cESC_EnableSts != 0) {
					carhelp_cESC_ck.setChecked(mPowerInfo.VCU_cESC_EnableSts == 2 ? true : false);
				}

			}

			if (carhelp_eHAC_up_ck != null) {
				if (mPowerInfo.VCU_eHAC_EnableSts != 0) {
					carhelp_eHAC_up_ck.setChecked(mPowerInfo.VCU_eHAC_EnableSts == 2 ? true : false);
				}

			}

			if (carhelp_eHDC_down_ck != null) {
				if (mPowerInfo.VCU_eHDC_EnableSts != 0) {
					carhelp_eHDC_down_ck.setChecked(mPowerInfo.VCU_eHDC_EnableSts == 2 ? true : false);
				}

			}

			if (carhelp_eAuto_Hold_ck != null) {
				if (mPowerInfo.VCU_eAVH_EnableSts != 0) {
					carhelp_eAuto_Hold_ck.setChecked(mPowerInfo.VCU_eAVH_EnableSts == 2 ? true : false);
				}

			}

			if (carhelp_speed_limit_ck != null) {
				if (mPowerInfo.VCU_MaxSpdLimitEnableSts != 0) {
					carhelp_speed_limit_ck.setChecked(mPowerInfo.VCU_MaxSpdLimitEnableSts == 2 ? true : false);
				}

			}

			if (carhelp_long_range_endurance_ck != null) {
				if (mPowerInfo.VCU_LongRangeModeEnableSts != 0) {
					carhelp_long_range_endurance_ck
							.setChecked(mPowerInfo.VCU_LongRangeModeEnableSts == 2 ? true : false);
				}

			}

			if (carhelp_backcar_enable_ck != null) {
				if (mPowerInfo.VCU_eAVH_ReverseDisableSts != 0) {
					carhelp_backcar_enable_ck.setChecked(mPowerInfo.VCU_eAVH_ReverseDisableSts == 2 ? true : false);
				}

			}

			if (mPowerInfo.VCU_eAVH_TimeSetSts == 0) {
				carhelp_eAuto_Hold_setTime_tv.setText(mPowerInfo.VCU_eAVH_TimeSetSts + 1 + "");
			} else {
				carhelp_eAuto_Hold_setTime_tv.setText(mPowerInfo.VCU_eAVH_TimeSetSts + "");
			}

			carhelp_speed_limit_sb.setProgress(mPowerInfo.VCU_SpeedLimitValueSetSts);
			if (mPowerInfo.VCU_SpeedLimitValueSetSts == 0) {
				carhelp_speed_limit_tv.setText(mContext.getResources().getString(R.string.carhelp_0));
			} else {
				carhelp_speed_limit_tv.setText(
						T19CanTx.getInstance().changeSpeed(mPowerInfo.VCU_SpeedLimitValueSetSts, 19, 1, 120, 30)
								+ mContext.getResources().getString(R.string.naviinfo_speed_unit));
			}

		}

	}

	// 刷新长程续航剩余次数
	public void updateCarStatusUI(CarStatusInfo mCarStatusInfo) {
		if (null != mCarStatusInfo) {
			if (carhelp_long_range_endurance_time != null) {
				carhelp_long_range_endurance_time
						.setText(mContext.getResources().getString(R.string.carhelp_long_range_endurance_time)
								+ mCarStatusInfo.VCU_LongRangeRemainingTime + mContext.getResources()
										.getString(R.string.carhelp_long_range_endurance_time_tv));
			}
		}
	}

	// 刷新仪表在线配置1
	public void updateDashboardConfigInfo1(DashboardConfigInfo1 mDashboardConfigInfo1) {
		if (null != mDashboardConfigInfo1) {

			if (carhelp_carbody_atmosphere_lamp_rl != null) {
				carhelp_carbody_atmosphere_lamp_rl.setVisibility(
						mDashboardConfigInfo1.ICM_AmbientLight_ConfigurationSts == 2 ? View.VISIBLE : View.GONE);
			}
			if (carhelp_carbody_decorative_lamp_rl != null) {
				carhelp_carbody_decorative_lamp_rl
						.setVisibility(mDashboardConfigInfo1.ICM_DRL_ConfigurationSts == 2 ? View.VISIBLE : View.GONE);
			}

		}
	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub
		mContext.unbindService(this);
		MediaActivity.mActivity.switchType = "time_gone";
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub
		Log.e(TAG, "addNotify");

	}

	@Override
	public void onServiceDisconnected(ComponentName paramComponentName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected(ComponentName paramComponentName, IBinder iBinder) {
		// TODO Auto-generated method stub
		myBinder = (CanBusService.CanBusBinder) iBinder;
		myBinder.getCanBusService().setCallback(new CanBusService.Callback() {

			@Override
			public void onDataChange(CarStatusInfo mCarStatusInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = PageCarStatus.MSG_CARSTATUSINfO;
				msg.obj = mCarStatusInfo;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(EnergyInfo mEnergyInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataChange(PedestrianInfo mPedestrianInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_PEDESTRIANINfO;
				msg.obj = mPedestrianInfo;
				mHander.sendMessage(msg);

			}

			@Override
			public void onDataChange(DashboardInfo mDashboardInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_DASHBOARDINfO;
				msg.obj = mDashboardInfo;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(CarbodyInfo mCarbodyInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_CARBODYINfO;
				msg.obj = mCarbodyInfo;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(CarHelpAirInfo mCarHelpAirInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_CARHELPAIRINfO;
				msg.obj = mCarHelpAirInfo;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(PowerInfo mPowerInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_POWERINfO;
				msg.obj = mPowerInfo;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(AirInfo mAirInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataChange(LowPowerInfo mEnergyInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataChange(DashboardConfigInfo1 mDashboardConfigInfo1) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_DASHBOARDCONFIG1INfO;
				msg.obj = mDashboardConfigInfo1;
				mHander.sendMessage(msg);
			}

			@Override
			public void onDataChange(VcuInfo mVcuInfo) {
				// TODO Auto-generated method stub
				
			}

		});
		myBinder.sendData();
	}

	private Handler mHander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case PageCarStatus.MSG_CARSTATUSINfO:
				mCarStatusInfo = (CarStatusInfo) msg.obj;
				updateCarStatusUI(mCarStatusInfo);
				break;

			case MSG_PEDESTRIANINfO:
				mPedestrianInfo = (PedestrianInfo) msg.obj;
				updatePedestrianUI(mPedestrianInfo);
				break;

			case MSG_DASHBOARDINfO:
				mDashboardInfo = (DashboardInfo) msg.obj;
				updateDashboardUI(mDashboardInfo);
				break;

			case MSG_CARBODYINfO:
				mCarbodyInfo = (CarbodyInfo) msg.obj;
				updateCarbodyUI(mCarbodyInfo);
				break;

			case MSG_CARHELPAIRINfO:
				mCarHelpAirInfo = (CarHelpAirInfo) msg.obj;
				updateCarHelpAirUI(mCarHelpAirInfo);
				break;

			case MSG_POWERINfO:
				mPowerInfo = (PowerInfo) msg.obj;
				updatePowerUI(mPowerInfo);
				break;
			case MSG_DASHBOARDCONFIG1INfO:
				mDashboardConfigInfo1 = (DashboardConfigInfo1) msg.obj;
				updateDashboardConfigInfo1(mDashboardConfigInfo1);
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	private int getDay(int year, int month) {
		int day = 30;
		boolean flag = false;
		switch (year % 4) {
		case 0:
			flag = true;
			break;
		default:
			flag = false;
			break;
		}
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = 31;
			break;
		case 2:
			day = flag ? 29 : 28;
			break;
		default:
			day = 30;
			break;
		}
		return day;
	}

	private void setSwitchOnUI(Context context, Button button, boolean bSwitch) {
		Drawable drawableOn = context.getResources().getDrawable(R.drawable.check_carinfo_open);
		drawableOn.setBounds(0, 0, drawableOn.getMinimumWidth(), drawableOn.getMinimumHeight());
		Drawable drawableOff = context.getResources().getDrawable(R.drawable.check_carinfo_close);
		drawableOff.setBounds(0, 0, drawableOff.getMinimumWidth(), drawableOff.getMinimumHeight());
		button.setBackground(bSwitch ? drawableOn : drawableOff);
		setDisableUI(!bSwitch);
	}

	private void setDisableUI(boolean enabled) {
		year.setScroll(enabled);
		month.setScroll(enabled);
		day.setScroll(enabled);
		hour.setScroll(enabled);
		mins.setScroll(enabled);
		year_up_iv.setEnabled(enabled);
		year_down_iv.setEnabled(enabled);
		month_up_iv.setEnabled(enabled);
		month_down_iv.setEnabled(enabled);
		day_up_iv.setEnabled(enabled);
		day_down_iv.setEnabled(enabled);
		hour_up_iv.setEnabled(enabled);
		hour_down_iv.setEnabled(enabled);
		mins_up_iv.setEnabled(enabled);
		mins_down_iv.setEnabled(enabled);
	}

	// 发送仪表数据
	public void sendDashboardData(int ICM_CurrentTime_Hour, int ICM_CurrentTime_Minute, int ICM_OverSpdValue,
			int ICM_FatigureDrivingTimeSet, int IHU_BrightnessAdjustSet_ICM) {

		if (mDashboardInfo != null) {
			mDashboardInfo.ICM_CurrentTime_Hour = ICM_CurrentTime_Hour;// 当前时间-小时
			mDashboardInfo.ICM_CurrentTime_Minute = ICM_CurrentTime_Minute;// 当前时间-分钟
			mDashboardInfo.ICM_OverSpdValue = ICM_OverSpdValue;// 车速报警值
			mDashboardInfo.ICM_FatigureDrivingTimeSet = ICM_FatigureDrivingTimeSet;// 疲劳驾驶时间设置
			mDashboardInfo.IHU_BrightnessAdjustSet_ICM = IHU_BrightnessAdjustSet_ICM;// 仪表亮度

			T19CanTx.getInstance().sendDashboardData(mDashboardInfo);
		}
	}

	// 发送车身数据
	public void sendCarbodyData(int BCM_AmbientLightEnableSts, int BCM_DRL_EnableSts, int BCM_MirrorFoldEnableSts,
			int BCM_AutoLockEnableSts, int BCM_FollowMeSts, int BCM_AntiTheftModeSts, int BCM_SunroofAutoCloseEnableSts,
			int BCM_RearDfstSts) {

		if (mCarbodyInfo != null) {
			mCarbodyInfo.BCM_AmbientLightEnableSts = BCM_AmbientLightEnableSts;// 氛围灯使能状态
			mCarbodyInfo.BCM_DRL_EnableSts = BCM_DRL_EnableSts;// 日间行车灯使能状态(装饰灯)
			mCarbodyInfo.BCM_MirrorFoldEnableSts = BCM_MirrorFoldEnableSts;// 设防后视镜折叠使能状态
			mCarbodyInfo.BCM_AutoLockEnableSts = BCM_AutoLockEnableSts;// 自动落锁使能状态
			mCarbodyInfo.BCM_FollowMeSts = BCM_FollowMeSts;// 伴我回家使能状态
			mCarbodyInfo.BCM_AntiTheftModeSts = BCM_AntiTheftModeSts;// 设防提示模式设置状态
			mCarbodyInfo.BCM_SunroofAutoCloseEnableSts = BCM_SunroofAutoCloseEnableSts;// 设防自动关闭天窗使能状态
			mCarbodyInfo.BCM_RearDfstSts = BCM_RearDfstSts;// 后除霜状态

			T19CanTx.getInstance().sendCarbodyData(mCarbodyInfo);
		}
	}

	// 发送车辆设置空调数据
	public void sendCarHelpAirData(int ACCM_AutoCleanEnableSts, int ACCM_AutoBlowEnableSts, int IHU_ACPMemoryMode,
			int ACCM_AutoCleanActiveSts, int ACCM_AutoBlowActiveSts, int ACCM_AutoAdjustCtrlSource) {

		if (mCarHelpAirInfo != null) {

			mCarHelpAirInfo.ACCM_AutoCleanEnableSts = ACCM_AutoCleanEnableSts;// 空调自动清洁功能使能状态
			mCarHelpAirInfo.ACCM_AutoBlowEnableSts = ACCM_AutoBlowEnableSts;// 空调自通风功能使能状态
			mCarHelpAirInfo.IHU_ACPMemoryMode = IHU_ACPMemoryMode;// 空调状态记忆使能开关
			mCarHelpAirInfo.ACCM_AutoCleanActiveSts = ACCM_AutoCleanActiveSts;// 空调自动清洁激活状态
			mCarHelpAirInfo.ACCM_AutoBlowActiveSts = ACCM_AutoBlowActiveSts;// 空调自通风激活状态
			mCarHelpAirInfo.ACCM_AutoAdjustCtrlSource = ACCM_AutoAdjustCtrlSource;// 空调AUTO调节控制来源

			T19CanTx.getInstance().sendCarHelpAirData(mCarHelpAirInfo);
		}
	}

	// 发送动力数据
	public void sendPowerInfoData(int VCU_cESC_EnableSts, int VCU_eHAC_EnableSts, int VCU_eHDC_EnableSts,
			int VCU_eAVH_EnableSts, int VCU_eAVH_TimeSetSts, int VCU_eAVH_ReverseDisableSts,
			int VCU_LongRangeModeEnableSts, int VCU_MaxSpdLimitEnableSts, int VCU_SpeedLimitValueSetSts,
			int VCU_MaxRegenerationLevelEnableSts, int VCU_RegenerationLevelSts) {

		mPowerInfo.VCU_cESC_EnableSts = VCU_cESC_EnableSts;// 驱动防滑功能使能状态
		mPowerInfo.VCU_eHAC_EnableSts = VCU_eHAC_EnableSts;// 坡道起步功能使能状态
		mPowerInfo.VCU_eHDC_EnableSts = VCU_eHDC_EnableSts;// 陡坡缓降功能使能状态
		mPowerInfo.VCU_eAVH_EnableSts = VCU_eAVH_EnableSts;// AVH使能状态
		mPowerInfo.VCU_eAVH_TimeSetSts = VCU_eAVH_TimeSetSts;// AVH时间设置状态
		mPowerInfo.VCU_eAVH_ReverseDisableSts = VCU_eAVH_ReverseDisableSts;// AVH倒车禁用使能状态
		mPowerInfo.VCU_LongRangeModeEnableSts = VCU_LongRangeModeEnableSts;// 长航模式功能使能状态
		mPowerInfo.VCU_MaxSpdLimitEnableSts = VCU_MaxSpdLimitEnableSts;// 车速限制功能使能状态
		mPowerInfo.VCU_SpeedLimitValueSetSts = VCU_SpeedLimitValueSetSts;// 车速限制值状态
		mPowerInfo.VCU_MaxRegenerationLevelEnableSts = VCU_MaxRegenerationLevelEnableSts;// 最高等级能量回收使能状态
		mPowerInfo.VCU_RegenerationLevelSts = VCU_RegenerationLevelSts;// 能量回收状态

		T19CanTx.getInstance().sendPowerInfoData(mPowerInfo);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}

}
