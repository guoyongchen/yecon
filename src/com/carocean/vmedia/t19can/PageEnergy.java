package com.carocean.vmedia.t19can;

import java.util.ArrayList;

import com.carocean.R;
import com.carocean.page.IPage;
import com.carocean.t19can.CanBusService;
import com.carocean.t19can.T19CanRx;
import com.carocean.t19can.T19CanTx;
import com.carocean.vmedia.t19can.view.CustomPickerView;
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
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PageEnergy implements IPage, OnClickListener, ServiceConnection {

	private String TAG = "PageEnergy";
	private Context mContext;
	private ViewGroup mRootView;
	private ImageView energy_charging_iv, energy_battery_iv, energy_status_middle_iv, energy_status_up_iv,
			energy_status_down_iv, energy_up_iv, energy_down_iv, energy_battery_iv_1, energy_battery_iv_2,
			energy_battery_iv_3, energy_battery_iv_4, energy_battery_iv_5;
	private CustomPickerView energy_cycle_wl;
	private ArrayList<String> energy_cycle_dataList = new ArrayList<String>();

	private AnimationDrawable battery_animationDrawable = null;
	private AnimationDrawable middle_animationDrawable = null;
	private AnimationDrawable up_animationDrawable = null;
	private AnimationDrawable down_animationDrawable = null;

	private CanBusService.CanBusBinder myBinder = null;
	private EnergyInfo mEnergyInfo;
	private VcuInfo mVcuInfo;
	public static final int MSG_ENERGYINfO = 102;// 能量回收和释放
	public static final int MSG_VcuInfo = 103;// 电量显示

	public boolean isChange = false;
	public boolean isReceive = false;

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.carinfo_layout_energy, null));
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

		energy_charging_iv = (ImageView) rootView.findViewById(R.id.energy_charging_iv);
		energy_battery_iv = (ImageView) rootView.findViewById(R.id.energy_battery_iv);
		energy_status_middle_iv = (ImageView) rootView.findViewById(R.id.energy_status_middle_iv);
		energy_status_up_iv = (ImageView) rootView.findViewById(R.id.energy_status_up_iv);
		energy_status_down_iv = (ImageView) rootView.findViewById(R.id.energy_status_down_iv);
		energy_up_iv = (ImageView) rootView.findViewById(R.id.energy_up_iv);
		energy_down_iv = (ImageView) rootView.findViewById(R.id.energy_down_iv);

		energy_battery_iv_1 = (ImageView) rootView.findViewById(R.id.energy_battery_iv_1);
		energy_battery_iv_2 = (ImageView) rootView.findViewById(R.id.energy_battery_iv_2);
		energy_battery_iv_3 = (ImageView) rootView.findViewById(R.id.energy_battery_iv_3);
		energy_battery_iv_4 = (ImageView) rootView.findViewById(R.id.energy_battery_iv_4);
		energy_battery_iv_5 = (ImageView) rootView.findViewById(R.id.energy_battery_iv_5);

		energy_cycle_wl = (CustomPickerView) rootView.findViewById(R.id.energy_cycle_wl);

		if (null != mEnergyInfo) {
			initEnergyCycleWheelView(mEnergyInfo.VCU_RegenerationLevelSts);
		} else {
			initEnergyCycleWheelView(0);
		}

		energy_up_iv.setOnClickListener(this);
		energy_down_iv.setOnClickListener(this);

		battery_animationDrawable = (AnimationDrawable) energy_battery_iv.getDrawable();

	}

	private void initEnergyCycleWheelView(int cur_index) {

		for (int i = 0; i < 3; i++) {
			switch (i) {
			case 0:
				energy_cycle_dataList.add(mContext.getResources().getString(R.string.energe_realse_low));
				break;
			case 1:
				energy_cycle_dataList.add(mContext.getResources().getString(R.string.energe_realse_middle));
				break;
			case 2:
				energy_cycle_dataList.add(mContext.getResources().getString(R.string.energe_realse_high));
				break;

			default:
				break;
			}
		}

		energy_cycle_wl.setDataList(energy_cycle_dataList);
		energy_cycle_wl.moveTo(cur_index);
		energy_cycle_wl.setScroll(false);

		energy_cycle_wl.setOnScrollChangedListener(new CustomPickerView.OnScrollChangedListener() {

			@Override
			public void onScrollChanged(int curIndex) {
				isChange = true;
				isReceive = false;
				Log.i(TAG, "onScrollChanged-----curIndex: " + curIndex + " isChange: " + isChange);
			}

			@Override
			public void onScrollFinished(int curIndex) {

				Log.i(TAG, "onScrollFinished-----curIndex: " + curIndex + " isChange: " + isChange);

				// if (isChange) {
				// if (null != mEnergyInfo) {
				// if (!isReceive) {
				// if (mEnergyInfo.VCU_RegenerationLevelSts != (curIndex + 1)) {
				// mEnergyInfo.VCU_RegenerationLevelSts = curIndex + 1;
				// Log.i(TAG, "mEnergyInfo.VCU_RegenerationLevelSts: "
				// + mEnergyInfo.VCU_RegenerationLevelSts);
				// T19CanTx.getInstance().sendEnergyReg(mEnergyInfo);
				// }
				// }
				//
				// }
				// isChange = false;
				// } else {
				// if (null != mEnergyInfo) {
				// if (!isReceive) {
				// if (mEnergyInfo.VCU_RegenerationLevelSts != curIndex + 1) {
				// mEnergyInfo.VCU_RegenerationLevelSts = curIndex + 1;
				// Log.i(TAG, "mEnergyInfo.VCU_RegenerationLevelSts: "
				// + mEnergyInfo.VCU_RegenerationLevelSts);
				// T19CanTx.getInstance().sendEnergyReg(mEnergyInfo);
				// }
				// }
				//
				// }
				// }

			}
		});
	}

	public void updateEnergyUI(EnergyInfo mEnergyInfo) {

		Log.i(TAG, "updateEnergyUI");
		if (null != mEnergyInfo) {

			if (mEnergyInfo.BMS_ChgWireConnectStsDisp == 1 || mEnergyInfo.BMS_ChgWireConnectStsDisp == 2) {

				if (energy_charging_iv != null) {
					energy_charging_iv.setVisibility(View.VISIBLE);
				}

				if (battery_animationDrawable != null) {
					battery_animationDrawable.start();
				}

				// 充电时，箭头隐藏
				if (energy_status_middle_iv != null) {
					energy_status_middle_iv.setVisibility(View.INVISIBLE);
				}
				if (energy_status_down_iv != null) {
					energy_status_down_iv.setVisibility(View.INVISIBLE);
				}
				if (energy_status_up_iv != null) {
					energy_status_up_iv.setVisibility(View.INVISIBLE);
				}
			} else {

				if (energy_charging_iv != null) {
					energy_charging_iv.setVisibility(View.INVISIBLE);
				}

				if (battery_animationDrawable != null) {
					battery_animationDrawable.stop();
				}
				if (mEnergyInfo.BMS_BattCurrDisp <= 31940) {
					// 中
					if (energy_status_middle_iv != null) {
						energy_status_middle_iv.setVisibility(View.VISIBLE);
						energy_status_middle_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_release_middle_animation));
					}
					middle_animationDrawable = (AnimationDrawable) energy_status_middle_iv.getDrawable();
					if (middle_animationDrawable != null) {
						middle_animationDrawable.start();
					}

					// 上
					if (energy_status_up_iv != null) {
						energy_status_up_iv.setVisibility(View.VISIBLE);
						energy_status_up_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_release_down_animation));
					}
					up_animationDrawable = (AnimationDrawable) energy_status_up_iv.getDrawable();
					if (up_animationDrawable != null) {
						up_animationDrawable.start();
					}

					// 下
					if (energy_status_down_iv != null) {
						energy_status_down_iv.setVisibility(View.VISIBLE);
						energy_status_down_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_release_up_animation));
					}
					down_animationDrawable = (AnimationDrawable) energy_status_down_iv.getDrawable();
					if (down_animationDrawable != null) {
						down_animationDrawable.start();
					}

				} else if (mEnergyInfo.BMS_BattCurrDisp <= 32000 && mEnergyInfo.BMS_BattCurrDisp > 31940) {
					// 中
					if (energy_status_middle_iv != null) {
						energy_status_middle_iv.setVisibility(View.VISIBLE);
						energy_status_middle_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_release_middle_animation));
					}
					middle_animationDrawable = (AnimationDrawable) energy_status_middle_iv.getDrawable();
					if (middle_animationDrawable != null) {
						middle_animationDrawable.start();
					}
					energy_status_up_iv.setVisibility(View.INVISIBLE);
					energy_status_down_iv.setVisibility(View.INVISIBLE);

				} else if (mEnergyInfo.BMS_BattCurrDisp > 32000) {
					// 中
					if (energy_status_middle_iv != null) {
						energy_status_middle_iv.setVisibility(View.VISIBLE);
						energy_status_middle_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_recovery_middle_animation));
					}
					middle_animationDrawable = (AnimationDrawable) energy_status_middle_iv.getDrawable();
					if (middle_animationDrawable != null) {
						middle_animationDrawable.start();
					}

					// 上
					if (energy_status_up_iv != null) {
						energy_status_up_iv.setVisibility(View.VISIBLE);
						energy_status_up_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_recovery_up_animation));
					}
					up_animationDrawable = (AnimationDrawable) energy_status_up_iv.getDrawable();
					if (up_animationDrawable != null) {
						up_animationDrawable.start();
					}

					// 下
					if (energy_status_down_iv != null) {
						energy_status_down_iv.setVisibility(View.VISIBLE);
						energy_status_down_iv.setImageDrawable(
								mContext.getResources().getDrawable(R.drawable.energy_recovery_down_animation));
					}
					down_animationDrawable = (AnimationDrawable) energy_status_down_iv.getDrawable();
					if (down_animationDrawable != null) {
						down_animationDrawable.start();
					}
				}
			}

			if (energy_cycle_wl != null) {
				if (mEnergyInfo.VCU_RegenerationLevelSts != 0) {
					isReceive = true;
					energy_cycle_wl.moveTo(mEnergyInfo.VCU_RegenerationLevelSts - 1);
				}
			}

		}

	}

	public void updateVcuInfoUI(VcuInfo mVcuInfo) {

		if (mVcuInfo != null) {
			if (mVcuInfo.VCU_SOC_Disp >= 0 && mVcuInfo.VCU_SOC_Disp <= 20) {

				setVCU_SOC_Disp(true, false, false, false, false);

			} else if (mVcuInfo.VCU_SOC_Disp > 20 && mVcuInfo.VCU_SOC_Disp <= 40) {

				setVCU_SOC_Disp(false, true, false, false, false);

			} else if (mVcuInfo.VCU_SOC_Disp > 40 && mVcuInfo.VCU_SOC_Disp <= 60) {

				setVCU_SOC_Disp(false, false, true, false, false);

			} else if (mVcuInfo.VCU_SOC_Disp > 60 && mVcuInfo.VCU_SOC_Disp <= 80) {

				setVCU_SOC_Disp(false, false, false, true, false);

			} else if (mVcuInfo.VCU_SOC_Disp > 80 && mVcuInfo.VCU_SOC_Disp <= 100) {

				setVCU_SOC_Disp(false, false, false, false, true);
			}
		}

	}

	public void setVCU_SOC_Disp(boolean iv_1, boolean iv_2, boolean iv_3, boolean iv_4, boolean iv_5) {

		if (energy_battery_iv_1 != null) {
			energy_battery_iv_1.setVisibility(iv_1 ? View.VISIBLE : View.INVISIBLE);
		}

		if (energy_battery_iv_2 != null) {
			energy_battery_iv_2.setVisibility(iv_2 ? View.VISIBLE : View.INVISIBLE);
		}

		if (energy_battery_iv_3 != null) {
			energy_battery_iv_3.setVisibility(iv_3 ? View.VISIBLE : View.INVISIBLE);
		}

		if (energy_battery_iv_4 != null) {
			energy_battery_iv_4.setVisibility(iv_4 ? View.VISIBLE : View.INVISIBLE);
		}

		if (energy_battery_iv_5 != null) {
			energy_battery_iv_5.setVisibility(iv_5 ? View.VISIBLE : View.INVISIBLE);
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
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.energy_up_iv:
			int currentItem_up = energy_cycle_wl.getCurIndex();
			currentItem_up--;
			if (currentItem_up < 0) {
				currentItem_up = 0;
			}
			isChange = false;
			isReceive = false;
			energy_cycle_wl.moveTo(currentItem_up);
			if (mEnergyInfo.VCU_RegenerationLevelSts != (currentItem_up + 1)) {

				mEnergyInfo.VCU_RegenerationLevelSts = currentItem_up + 1;
				T19CanTx.getInstance().sendEnergyReg(mEnergyInfo);

			}
			break;
		case R.id.energy_down_iv:
			int currentItem_down = energy_cycle_wl.getCurIndex();
			currentItem_down++;
			if (currentItem_down > 2) {
				currentItem_down = 2;
			}
			isChange = false;
			isReceive = false;
			energy_cycle_wl.moveTo(currentItem_down);
			if (mEnergyInfo.VCU_RegenerationLevelSts != (currentItem_down + 1)) {
				mEnergyInfo.VCU_RegenerationLevelSts = currentItem_down + 1;
				T19CanTx.getInstance().sendEnergyReg(mEnergyInfo);
			}

			break;
		default:
			break;
		}

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
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
			}

			@Override
			public void onDataChange(EnergyInfo mEnergyInfo) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_ENERGYINfO;
				msg.obj = mEnergyInfo;
				mHander.sendMessage(msg);

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
				Message msg = new Message();
				msg.what = MSG_VcuInfo;
				msg.obj = mVcuInfo;
				mHander.sendMessage(msg);
			}

		});
		myBinder.sendData();

	}

	private Handler mHander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case MSG_ENERGYINfO:
				mEnergyInfo = (EnergyInfo) msg.obj;
				updateEnergyUI(mEnergyInfo);
				break;

			case MSG_VcuInfo:
				mVcuInfo = (VcuInfo) msg.obj;
				updateVcuInfoUI(mVcuInfo);
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
