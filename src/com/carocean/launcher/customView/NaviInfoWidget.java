package com.carocean.launcher.customView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.yecon.common.SourceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.mcu.McuExternalConstant;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NaviInfoWidget extends LinearLayout implements OnClickListener {
	private final String TAG = "NaviInfoWidget_Launcher3";
	RelativeLayout mNaviInfoLayout;
	LinearLayout mNaviInfoDispLayout;
	String provinceName;
	String cityName;
	String versionName;
	String channelName;
	int navi_type;
	String cur_load_name = "";
	String next_load_name = "";
	int icon_id = 1;
	int route_remain_dis = 0;
	int route_remain_dis_tmp = -1;
	int route_remain_time = 0;
	int route_remain_time_tmp = 0;
	int seg_remain_dis = 0;
	int seg_remain_time = 0;
	int round_about_num = 0;
	int route_all_dis = 0;
	int route_all_time = 0;
	int cur_speed = 0;
	int cur_speed_tmp = -1;
	boolean arrive_status = false;
	int naviStatus = 9;
	String cur_load_name_tmp = "";
	int seg_remain_dis_tmp = -1;
	String next_load_name_tmp = "";
	int icon_id_tmp = -1;
	static long mReceTime = 0;
	Context mContext = null;
	LinearLayout navi_run_ll;
	TextView mNaviinfoTitle;
	TextView mRouteRmainDis;
	TextView mNextRoad;
	TextView mCurSpeed;
	TextView mDistance;
	ImageView mIcon;
	TextView mNaviinfoArriveTime;

	Runnable mDogRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub\r
			long curtime = System.currentTimeMillis();

			// Log.i(TAG, "navimap---curtime="+curtime+",
			// mReceTime="+mReceTime);

			if ((mReceTime != 0) && (curtime - mReceTime) > 2000) {
				stopNavi(mContext);
			}
			mDogHandler.postDelayed(mDogRunnable, 1000);
		}
	};
	Handler mDogHandler = new Handler();

	final int allIcon[] = { R.drawable.cheji_icon_1x1, R.drawable.cheji_icon_1x2, R.drawable.cheji_icon_1x3,
			R.drawable.cheji_icon_1x4, R.drawable.cheji_icon_1x5, R.drawable.cheji_icon_1x6, R.drawable.cheji_icon_1x7,
			R.drawable.cheji_icon_1x8, R.drawable.cheji_icon_1x9, R.drawable.cheji_icon_1x10,
			R.drawable.cheji_icon_1x11, R.drawable.cheji_icon_1x12, R.drawable.cheji_icon_1x13,
			R.drawable.cheji_icon_1x14, R.drawable.cheji_icon_1x15, R.drawable.cheji_icon_1x16,
			R.drawable.cheji_icon_2x17, R.drawable.cheji_icon_2x18, R.drawable.cheji_icon_2x19,
			R.drawable.cheji_icon_2x20, };

	void resetParams() {
		cur_load_name_tmp = "";
		seg_remain_dis_tmp = -1;
		next_load_name_tmp = "";
		icon_id_tmp = -1;
		route_remain_dis_tmp = -1;
		cur_speed_tmp = -1;
		route_remain_time_tmp = -1;

	}

	void hideNaviInfoViews() {
		mNaviinfoTitle.setVisibility(View.VISIBLE);
		mNaviInfoLayout.setBackgroundResource(R.drawable.icon_selector_navigation);

		navi_run_ll.setVisibility(View.GONE);
	}

	void showNaviInfoViews() {
		mNaviinfoTitle.setVisibility(View.INVISIBLE);
		mNaviInfoLayout.setBackgroundResource(R.drawable.icon_selector_naviinfo_run);

		navi_run_ll.setVisibility(View.VISIBLE);
	}

	void stopNavi(Context context) {
		Log.i(TAG, "navimap---stopNavi");
		resetParams();
		mReceTime = 0;

		if (context == null) {
			return;
		}

		hideNaviInfoViews();
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent == null) {
				return;
			}

			String action = intent.getAction();
			int keytype = intent.getIntExtra("KEY_TYPE", 0);
			// Log.i(TAG, "navimap---onReceive,
			// action="+action+",keytype="+keytype);
			if (null == action) {
				return;
			}

			mContext = context;

			if (action.equals("AUTONAVI_STANDARD_BROADCAST_SEND")) {
				mReceTime = System.currentTimeMillis();
				Log.i(TAG, "mReceTime: " + mReceTime);

				if (keytype == 10030) {
					provinceName = intent.getStringExtra("PRVINCE_NAME");
					cityName = intent.getStringExtra("CITY_NAME");
				} else if (keytype == 10041) {
					versionName = intent.getStringExtra("VERSION_NUM");
					channelName = intent.getStringExtra("CHANNEL_NUM");
				} else if (keytype == 10001) {
					navi_type = intent.getIntExtra("TYPE", 0);
					cur_load_name = intent.getStringExtra("CUR_ROAD_NAME");
					next_load_name = intent.getStringExtra("NEXT_ROAD_NAME");// 下一道路名称
					icon_id = intent.getIntExtra("ICON", 1);// 导航最新的转向图标，对应的值为
															// int 类型
					route_remain_dis = intent.getIntExtra("ROUTE_REMAIN_DIS", 0);// 路径剩余距离，对应的值为
																					// int
																					// 类型，单位：米
					route_remain_time = intent.getIntExtra("ROUTE_REMAIN_TIME", 0);
					seg_remain_dis = intent.getIntExtra("SEG_REMAIN_DIS", 0);// 当前导航段剩余距离，对应的值为
																				// int
																				// 类型，单位：米
					seg_remain_time = intent.getIntExtra("SEG_REMAIN_TIME", 0);// 当前导航段剩余时间，对应的值为
																				// int
																				// 类型，单位：秒
					round_about_num = intent.getIntExtra("ROUNG_ABOUT_NUM", 0);
					route_all_dis = intent.getIntExtra("ROUTE_ALL_DIS", 0);
					route_all_time = intent.getIntExtra("ROUTE_ALL_TIME", 0);
					cur_speed = intent.getIntExtra("CUR_SPEED", 0);// 当前车速
					arrive_status = intent.getBooleanExtra("ARRIVE_STATUS", false);
					updateNaviInfo(context, true);
				} else if (keytype == 10016) {
					// Intent light_intent = new Intent();
					// light_intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
					// light_intent.putExtra("KEY_TYPE", 10017);
					// light_intent.putExtra("EXTRA_HEADLIGHT_STATE", 0);
					// context.sendBroadcast(light_intent);
				} else if (keytype == 10019) {
					naviStatus = intent.getIntExtra("EXTRA_STATE", 0);
					updateNaviInfo(context, false);
				}
			}
		}
	};

	public int getSource() {
		return SourceManager.lastSource();
	}

	void initView(Context context) {
		mContext = context;

		LayoutInflater.from(context).inflate(R.layout.layout_naviinfo_widget, this);
		mNaviInfoLayout = (RelativeLayout) findViewById(R.id.layout_naviInfo);
		mNaviInfoLayout.setOnClickListener(this);

		// mNaviInfoDispLayout = (LinearLayout)
		// findViewById(R.id.naviinfo_disp_layout);
		// mNaviInfoDispLayout.setVisibility(View.GONE);

		navi_run_ll = (LinearLayout) findViewById(R.id.navi_run_ll);
		mNaviinfoTitle = (TextView) findViewById(R.id.naviinfo_title);
		mRouteRmainDis = (TextView) findViewById(R.id.naviinfo_route_remain_dis);
		mNextRoad = (TextView) findViewById(R.id.naviinfo_next_road);
		mCurSpeed = (TextView) findViewById(R.id.naviinfo_speed);
		mDistance = (TextView) findViewById(R.id.naviinfo_dis);
		mIcon = (ImageView) findViewById(R.id.naviinfo_icon);
		mNaviinfoArriveTime = (TextView) findViewById(R.id.naviinfo_arrive_time);

		IntentFilter filter = new IntentFilter();
		filter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
		getContext().registerReceiver(mBroadcastReceiver, filter);

		mReceTime = 0;
		resetParams();
		if (mDogHandler != null && mDogRunnable != null) {
			mDogHandler.removeCallbacks(mDogRunnable);
			mDogHandler.postDelayed(mDogRunnable, 1000);
		}

		hideNaviInfoViews();
	}

	void updateNaviInfo(Context context, boolean bForceFlag) {
		// Log.i(TAG, "navimap---start updateNaviInfo, status ="+naviStatus);
		if (context == null) {
			return;
		}

		boolean changeFlag = false;

		if (naviStatus == 8 || naviStatus == 10 || bForceFlag) {
			// mNaviInfoDispLayout.setVisibility(View.VISIBLE);
			showNaviInfoViews();

			if (cur_load_name != null && !cur_load_name.equals(cur_load_name_tmp)) {
				cur_load_name_tmp = cur_load_name;
				// mCurRoad.setText(cur_load_name);
				changeFlag = true;
			}

			if (seg_remain_dis != seg_remain_dis_tmp) {
				seg_remain_dis_tmp = seg_remain_dis;
				if (seg_remain_dis < 1000) {
					mRouteRmainDis.setText(seg_remain_dis + " " + context.getResources().getString(R.string.dis_metre));
				} else {
					float cur_km_f = (float) (seg_remain_dis / 1000.0f);
					DecimalFormat decimalFormat = new DecimalFormat(".0");
					String cur_km = decimalFormat.format(cur_km_f);
					mRouteRmainDis.setText(cur_km + " " + context.getResources().getString(R.string.dis_km));
				}

				changeFlag = true;
			}

			if (route_remain_dis != route_remain_dis_tmp) {
				route_remain_dis_tmp = route_remain_dis;

				float cur_km_f = (float) (route_remain_dis / 1000.0f);
				DecimalFormat decimalFormat = new DecimalFormat(".0");
				String cur_km = decimalFormat.format(cur_km_f);
				mDistance.setText(cur_km + " ");

				changeFlag = true;
			}

			if (next_load_name != null && !next_load_name.equals(next_load_name_tmp)) {
				next_load_name_tmp = next_load_name;
				mNextRoad.setText(next_load_name);
				changeFlag = true;
			}

			if (icon_id != icon_id_tmp) {
				icon_id_tmp = icon_id;
				if (!(icon_id == 12 || icon_id == 18)) {
					if (icon_id != 0 && (icon_id - 1) < allIcon.length) {
						mIcon.setImageResource(allIcon[icon_id - 1]);
					}
				}

				if (icon_id == 11 || icon_id == 12 || icon_id == 17 || icon_id == 18) {
					if (!(icon_id == 12 || icon_id == 18)) {
					}

				} else {
				}
				changeFlag = true;
			}

			if (cur_speed != cur_speed_tmp) {
				cur_speed_tmp = cur_speed;
				mCurSpeed.setText(cur_speed + "");
				changeFlag = true;
			}

			if (route_remain_time != route_remain_time_tmp) {
				route_remain_time_tmp = route_remain_time;
				mNaviinfoArriveTime.setText(context.getResources().getString(R.string.naviinfo_estimate)
						+ getArriveTime(mReceTime, route_remain_time)
						+ context.getResources().getString(R.string.naviinfo_arrive));
			}

			postInvalidate();
			return;
		} else if (naviStatus == 9 || naviStatus == 12 || naviStatus == 1) {
			stopNavi(context);
			postInvalidate();
			return;
		}
	}

	public NaviInfoWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RadioWidget);

		ta.recycle();

		initView(context);
	}

	public NaviInfoWidget(Context context) {
		super(context);

		initView(context);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int curSource = getSource();

		Log.i(TAG, "widget---when click, cursource=" + curSource);

		if (arg0.getId() == R.id.layout_naviInfo) {
			if (mContext != null) {
				if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
					launcherUtils.startNavi();
				}
			}
			return;
		}

	}

	// 计算到达时间
	public String getArriveTime(long curTime, long time) {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		long lcc = curTime / 1000 + time;
		String times = sdf.format(new Date(lcc * 1000));
		return times;

	}

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBroadcastReceiver != null && mContext != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }
}
