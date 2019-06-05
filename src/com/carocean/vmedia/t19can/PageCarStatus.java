package com.carocean.vmedia.t19can;

import com.carocean.R;
import com.carocean.page.IPage;
import com.carocean.t19can.CanBusService;
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PageCarStatus implements IPage, ServiceConnection {

	private String TAG = "PageCarStatus";
	private Context mContext;
	private CanBusService.CanBusBinder myBinder = null;
	private ViewGroup mRootView;

	public static final int MSG_CARSTATUSINfO = 101;// 车辆状况

	// 正常显示门及把手
	private ImageView rearview_mirror_left_iv, rearview_mirror_right_iv, handle_left_front_iv, handle_left_back_iv,
			handle_right_front_iv, handle_right_back_iv;
	// 不正常显示门及把手
	private ImageView carstatus_front_cover_not_close_iv, carstatus_left_front_not_close_iv,
			carstatus_left_back_not_close_iv, carstatus_right_front_not_close_iv, carstatus_right_back_not_close_iv,
			carstatus_trunk_not_close_iv;

	private TextView car_endurance_mileage_tv;// 续航里程
	private TextView instantaneous_power_consumption_tv, instantaneous_power_consumption_unit_tv;// 瞬时电耗

	private CarStatusInfo mCarStatusInfo;

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.carinfo_layout_carstatus, null));
			init(context);
			initView(mRootView);
		}
		Intent intent = new Intent(mContext, CanBusService.class);
		mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
		return mRootView;
	}

	void init(Context context) {
		this.mContext = context;
	}

	void initView(ViewGroup rootView) {

		rearview_mirror_left_iv = (ImageView) rootView.findViewById(R.id.rearview_mirror_left_iv);
		rearview_mirror_right_iv = (ImageView) rootView.findViewById(R.id.rearview_mirror_right_iv);
		handle_left_front_iv = (ImageView) rootView.findViewById(R.id.handle_left_front_iv);
		handle_left_back_iv = (ImageView) rootView.findViewById(R.id.handle_left_back_iv);
		handle_right_front_iv = (ImageView) rootView.findViewById(R.id.handle_right_front_iv);
		handle_right_back_iv = (ImageView) rootView.findViewById(R.id.handle_right_back_iv);

		carstatus_front_cover_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_front_cover_not_close_iv);
		carstatus_left_front_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_left_front_not_close_iv);
		carstatus_left_back_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_left_back_not_close_iv);
		carstatus_right_front_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_right_front_not_close_iv);
		carstatus_right_back_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_right_back_not_close_iv);
		carstatus_trunk_not_close_iv = (ImageView) rootView.findViewById(R.id.carstatus_trunk_not_close_iv);

		car_endurance_mileage_tv = (TextView) rootView.findViewById(R.id.car_endurance_mileage_tv);
		instantaneous_power_consumption_tv = (TextView) rootView.findViewById(R.id.instantaneous_power_consumption_tv);
		instantaneous_power_consumption_unit_tv = (TextView) rootView
				.findViewById(R.id.instantaneous_power_consumption_unit_tv);

	}

	public void updataCarStatusUI(CarStatusInfo mCarStatusInfo) {

		Log.i(TAG, "updataCarStatusUI");
		if (null != mCarStatusInfo) {

			if (car_endurance_mileage_tv != null) {
				car_endurance_mileage_tv.setText(mCarStatusInfo.VCU_ResidualOdometer
						+ mContext.getResources().getString(R.string.car_endurance_mileage_default));
			}
			if (instantaneous_power_consumption_tv != null) {
				instantaneous_power_consumption_tv.setText(String.format("%.1f", mCarStatusInfo.VCU_InstPowerConsum));
			}

			if (mCarStatusInfo.VCU_VehSpd < 3) {
				if (instantaneous_power_consumption_unit_tv != null) {
					instantaneous_power_consumption_tv
							.setText(mContext.getString(R.string.instantaneous_power_consumption_3_default));
				}
			} else {
				if (instantaneous_power_consumption_unit_tv != null) {
					instantaneous_power_consumption_tv
							.setText(mContext.getString(R.string.instantaneous_power_consumption_default));
				}
			}

			// 前舱盖状态
			carstatus_front_cover_not_close_iv
					.setVisibility(mCarStatusInfo.BCM_Reserved == 1 ? View.VISIBLE : View.INVISIBLE);
			// 后备箱状态
			carstatus_trunk_not_close_iv
					.setVisibility(mCarStatusInfo.BCM_TrunkAjarSts == 1 ? View.VISIBLE : View.INVISIBLE);
			// 门的开启和关闭状态
			if (mCarStatusInfo.BCM_DoorAjarSts_Driver == 1) {// 主驾驶门
				carstatus_left_front_not_close_iv.setVisibility(View.VISIBLE);
				rearview_mirror_left_iv.setVisibility(View.INVISIBLE);
				handle_left_front_iv.setVisibility(View.INVISIBLE);
			} else {
				carstatus_left_front_not_close_iv.setVisibility(View.INVISIBLE);
				rearview_mirror_left_iv.setVisibility(View.VISIBLE);
				handle_left_front_iv.setVisibility(View.VISIBLE);
			}

			if (mCarStatusInfo.BCM_DoorAjarSts_Psngr == 1) {// 副驾驶门
				carstatus_right_front_not_close_iv.setVisibility(View.VISIBLE);
				rearview_mirror_right_iv.setVisibility(View.INVISIBLE);
				handle_right_front_iv.setVisibility(View.INVISIBLE);
			} else {
				carstatus_right_front_not_close_iv.setVisibility(View.INVISIBLE);
				rearview_mirror_right_iv.setVisibility(View.VISIBLE);
				handle_right_front_iv.setVisibility(View.VISIBLE);
			}

			if (mCarStatusInfo.BCM_DoorAjarSts_RL == 1) {// 左后门
				carstatus_left_back_not_close_iv.setVisibility(View.VISIBLE);
				handle_left_back_iv.setVisibility(View.INVISIBLE);
			} else {
				carstatus_left_back_not_close_iv.setVisibility(View.INVISIBLE);
				handle_left_back_iv.setVisibility(View.VISIBLE);
			}

			if (mCarStatusInfo.BCM_DoorAjarSts_RR == 1) {// 右后门
				carstatus_right_back_not_close_iv.setVisibility(View.VISIBLE);
				handle_right_back_iv.setVisibility(View.INVISIBLE);
			} else {
				carstatus_right_back_not_close_iv.setVisibility(View.INVISIBLE);
				handle_right_back_iv.setVisibility(View.VISIBLE);
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
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder iBinder) {
		// TODO Auto-generated method stub
		myBinder = (CanBusService.CanBusBinder) iBinder;
		myBinder.getCanBusService().setCallback(new CanBusService.Callback() {

			@Override
			public void onDataChange(CarStatusInfo mCarStatusInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_CARSTATUSINfO;
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

			}

			@Override
			public void onDataChange(DashboardInfo mDashboardInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataChange(CarbodyInfo mCarbodyInfo) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDataChange(CarHelpAirInfo mCarHelpAirInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataChange(PowerInfo mPowerInfo) {
				// TODO Auto-generated method stub

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

			}

			@Override
			public void onDataChange(VcuInfo mVcuInfo) {
				// TODO Auto-generated method stub
				
			}

		});
		myBinder.sendData();

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub

	}

	private Handler mHander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case MSG_CARSTATUSINfO:
				mCarStatusInfo = (CarStatusInfo) msg.obj;
				updataCarStatusUI(mCarStatusInfo);
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}

}
