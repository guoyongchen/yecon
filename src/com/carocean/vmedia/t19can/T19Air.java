package com.carocean.vmedia.t19can;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.t19can.T19CanRx.AirInfo;
import com.carocean.utils.DataShared;
import com.carocean.t19can.T19CanTx;
import com.carocean.vmedia.t19can.view.AirTempNumberPicker;
import com.carocean.vmedia.t19can.view.AirWindSpeedNumberPicker;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 
 * @author Dsream
 *
 */
public class T19Air implements OnTouchListener, OnClickListener {

	private String TAG = "T19Air";
	private Context mContext = null;
	private Handler mHandler = null;
	private PopWind mPopWind = null;
	private long mlshowAirTime = 0;
	private int showAirTime = 15;
	private int showAirTime_permanent = 86400;
	private boolean mbAirAutoCloseFlag = false;

	protected FrameLayout mAirView = null;
	private AirTempNumberPicker air_temp_numberpicker;
	private AirWindSpeedNumberPicker wind_speed_numberpicker;

	private ImageView energy_up_iv, energy_down_iv;
	private ImageView air_front_defogger_iv, air_mode_iv, air_back_defogger_iv;
	private ImageView energy_up_iv_wind_speed, energy_down_iv_wind_speed;
	private ImageView air_ac_iv, air_auto_iv, air_cycle_out_iv, air_cycle_in_iv, air_close_iv, air_ptc_iv;
	private RelativeLayout air_cycle_out_rl, air_cycle_in_rl;
	private ImageView close_air;
	private LinearLayout close_air_ll;

	private int cur_ACP_BlowModeSet;

	private AirInfo airInfo = new AirInfo();
	private float mY = 0;

	public T19Air(LayoutInflater inflater, Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mPopWind = new PopWind((int) context.getResources().getDimension(R.dimen.air_width),
				(int) context.getResources().getDimension(R.dimen.air_height));
		mAirView = (FrameLayout) inflater.inflate(R.layout.t19_air, null);
		// mAirView.setOnTouchListener(this);
		initView(mAirView);
	}

	public void initView(View view) {

		energy_up_iv = (ImageView) view.findViewById(R.id.energy_up_iv);
		energy_down_iv = (ImageView) view.findViewById(R.id.energy_down_iv);

		air_front_defogger_iv = (ImageView) view.findViewById(R.id.air_front_defogger_iv);
		air_mode_iv = (ImageView) view.findViewById(R.id.air_mode_iv);
		air_back_defogger_iv = (ImageView) view.findViewById(R.id.air_back_defogger_iv);
		energy_up_iv_wind_speed = (ImageView) view.findViewById(R.id.energy_up_iv_wind_speed);
		energy_down_iv_wind_speed = (ImageView) view.findViewById(R.id.energy_down_iv_wind_speed);

		air_ac_iv = (ImageView) view.findViewById(R.id.air_ac_iv);
		air_auto_iv = (ImageView) view.findViewById(R.id.air_auto_iv);
		air_cycle_out_iv = (ImageView) view.findViewById(R.id.air_cycle_out_iv);
		air_cycle_in_iv = (ImageView) view.findViewById(R.id.air_cycle_in_iv);
		air_close_iv = (ImageView) view.findViewById(R.id.air_close_iv);
		air_ptc_iv = (ImageView) view.findViewById(R.id.air_ptc_iv);

		air_cycle_out_rl = (RelativeLayout) view.findViewById(R.id.air_cycle_out_rl);
		air_cycle_in_rl = (RelativeLayout) view.findViewById(R.id.air_cycle_in_rl);

		close_air = (ImageView) view.findViewById(R.id.close_air);
		close_air_ll = (LinearLayout) view.findViewById(R.id.close_air_ll);

		energy_up_iv.setOnClickListener(this);
		energy_down_iv.setOnClickListener(this);
		air_front_defogger_iv.setOnClickListener(this);
		air_mode_iv.setOnClickListener(this);
		air_back_defogger_iv.setOnClickListener(this);
		energy_up_iv_wind_speed.setOnClickListener(this);
		energy_down_iv_wind_speed.setOnClickListener(this);

		air_ac_iv.setOnClickListener(this);
		air_auto_iv.setOnClickListener(this);
		air_cycle_out_iv.setOnClickListener(this);
		air_cycle_in_iv.setOnClickListener(this);
		air_close_iv.setOnClickListener(this);
		air_ptc_iv.setOnClickListener(this);
		close_air.setOnClickListener(this);
		close_air_ll.setOnTouchListener(this);

		air_temp_numberpicker = (AirTempNumberPicker) view.findViewById(R.id.air_temp_numberpicker);
		wind_speed_numberpicker = (AirWindSpeedNumberPicker) view.findViewById(R.id.wind_speed_numberpicker);

	}

	Runnable AirAutoClose = new Runnable() {

		@Override
		public void run() {
			long lcurtime = System.currentTimeMillis();
			Log.e(TAG, "lcurtime: " + lcurtime + "  mlshowAirTime: " + mlshowAirTime);
			showAirTime = DataShared.getInstance(ApplicationManage.getContext())
					.getInt(SettingConstants.key_air_show_time, 5);
			if (showAirTime == 45) {
				showAirTime = showAirTime_permanent;
			}
			if ((lcurtime - mlshowAirTime) >= showAirTime * 1000) {

				Hide();
				mbAirAutoCloseFlag = false;
			}

			if (mbAirAutoCloseFlag) {
				mHandler.postDelayed(AirAutoClose, 500);
			}
		}
	};

	public boolean IsShow() {
		return mPopWind.IsVisiable();
	}

	public void show(AirInfo mAirInfo, boolean bshow) {

		if (mAirInfo != null) {

			mlshowAirTime = System.currentTimeMillis();

			airInfo = mAirInfo;
			if (mAirInfo.ACP_TempSet >= 2) {
				air_temp_numberpicker.setSpeedIndex(mAirInfo.ACP_TempSet - 2);
			} else {
				air_temp_numberpicker.setSpeedIndex(0);
			}

			if (mAirInfo.ACP_BlowerLeverSet != 0) {
				wind_speed_numberpicker.setSpeedIndex(mAirInfo.ACP_BlowerLeverSet - 1);
			}

			// 开关
			air_close_iv.setSelected(mAirInfo.ACP_OFF_SwSts == 1 ? true : false);
			air_close_iv.setTag(mAirInfo.ACP_OFF_SwSts == 1 ? true : false);

			// 前除霜
			if (mAirInfo.ACP_BlowModeSet == 5) {
				mAirInfo.ACP_AQS_SwSts = 1;
			} else {
				if (mAirInfo.ACP_BlowModeSet != 0) {
					mAirInfo.ACP_AQS_SwSts = 0;
				}
			}
			air_front_defogger_iv.setSelected(mAirInfo.ACP_AQS_SwSts == 1 ? true : false);
			air_front_defogger_iv.setTag(mAirInfo.ACP_AQS_SwSts == 1 ? true : false);

			// 后除霜
			air_back_defogger_iv.setSelected(mAirInfo.ACP_RearDfstSwSts == 1 ? true : false);
			air_back_defogger_iv.setTag(mAirInfo.ACP_RearDfstSwSts == 1 ? true : false);

			// AC
			air_ac_iv.setSelected(mAirInfo.ACP_AC_SwSts == 1 ? true : false);
			air_ac_iv.setTag(mAirInfo.ACP_AC_SwSts == 1 ? true : false);

			// AUTO
			air_auto_iv.setSelected(mAirInfo.ACP_AUTO_SwSts == 1 ? true : false);
			air_auto_iv.setTag(mAirInfo.ACP_AUTO_SwSts == 1 ? true : false);

			// PTC
			air_ptc_iv.setSelected(mAirInfo.ACP_PTC_SwSts == 1 ? true : false);
			air_ptc_iv.setTag(mAirInfo.ACP_PTC_SwSts == 1 ? true : false);

			// 内外循环
			if (mAirInfo.ACP_CirculationModeSwSts == 0) {
				// 外循环
				air_cycle_out_rl.setVisibility(View.VISIBLE);
				air_cycle_in_rl.setVisibility(View.GONE);
				air_cycle_out_iv.setSelected(true);
				air_cycle_out_iv.setTag(true);
				air_cycle_in_iv.setSelected(false);
				air_cycle_in_iv.setTag(false);
			} else if (mAirInfo.ACP_CirculationModeSwSts == 1) {
				// 内循环
				air_cycle_out_rl.setVisibility(View.GONE);
				air_cycle_in_rl.setVisibility(View.VISIBLE);
				air_cycle_out_iv.setSelected(false);
				air_cycle_out_iv.setTag(false);
				air_cycle_in_iv.setSelected(true);
				air_cycle_in_iv.setTag(true);
			}

			// 吹风模式 0吹脸，1吹脸吹脚，2吹脚，3吹脚除霜，4前偏移
			cur_ACP_BlowModeSet = mAirInfo.ACP_BlowModeSet;
			setBlowMode(cur_ACP_BlowModeSet);

		}

		if (!IsShow() && bshow) {
			if (mPopWind != null) {
				mlshowAirTime = mPopWind.show(mContext, mAirView);
				mHandler.post(AirAutoClose);
				mbAirAutoCloseFlag = true;
			}
		} else {
			// air off
			if (IsShow()) {
				if (mHandler.hasCallbacks(AirAutoClose)) {
					mlshowAirTime = System.currentTimeMillis();
					mHandler.removeCallbacks(AirAutoClose);
					mHandler.post(AirAutoClose);
				}
				// Hide();
				// mbAirAutoCloseFlag = false;
			}
		}
	}

	public void setBlowMode(int mode) {
		switch (mode) {
		case 1:
			air_mode_iv.setBackground(mContext.getResources().getDrawable(R.drawable.air_face));
			break;

		case 2:
			air_mode_iv.setBackground(mContext.getResources().getDrawable(R.drawable.air_face_foot));
			break;

		case 3:
			air_mode_iv.setBackground(mContext.getResources().getDrawable(R.drawable.air_foot));
			break;

		case 4:
			air_mode_iv.setBackground(mContext.getResources().getDrawable(R.drawable.air__foot_defogger));
			break;

		case 5:
			air_mode_iv.setBackground(mContext.getResources().getDrawable(R.drawable.air_defogger));
			break;

		default:
			break;
		}
	}

	public void Hide() {
		if (mPopWind != null) {
			mPopWind.hide(mAirView);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mY = event.getY();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float y_move = event.getY();

			if (mY - y_move > 30) {
				if (IsShow()) {
					Hide();
					mbAirAutoCloseFlag = false;
				}
			}

		}
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.air_mode_iv:
			cur_ACP_BlowModeSet++;
			if (cur_ACP_BlowModeSet > 4) {
				cur_ACP_BlowModeSet = 1;
			}
			setBlowMode(cur_ACP_BlowModeSet);
			airInfo.ACP_BlowModeSet = cur_ACP_BlowModeSet;

			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.energy_up_iv:
			int currentItem_up = air_temp_numberpicker.getSpeedIndex();
			currentItem_up--;
			if (currentItem_up < 0) {
				currentItem_up = 0;
			}
			air_temp_numberpicker.setSpeedIndex(currentItem_up);
			sendTempSet(currentItem_up + 2);
			break;

		case R.id.energy_down_iv:
			int currentItem_down = air_temp_numberpicker.getSpeedIndex();
			currentItem_down++;
			if (currentItem_down > 28) {
				currentItem_down = 28;
			}
			air_temp_numberpicker.setSpeedIndex(currentItem_down);
			sendTempSet(currentItem_down + 2);
			break;

		case R.id.air_front_defogger_iv:
			// if ((Boolean) air_front_defogger_iv.getTag()) {
			// air_front_defogger_iv.setTag(false);
			// air_front_defogger_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_AQS_SwSts = 1;
			// }
			// } else {
			// air_front_defogger_iv.setTag(true);
			// air_front_defogger_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_AQS_SwSts = 1;
			// }
			// }

			airInfo.ACP_AQS_SwSts = 1;
			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择
			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.air_back_defogger_iv:
			// if ((Boolean) air_back_defogger_iv.getTag()) {
			// air_back_defogger_iv.setTag(false);
			// air_back_defogger_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_RearDfstSwSts = 1;
			// }
			// } else {
			// air_back_defogger_iv.setTag(true);
			// air_back_defogger_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_RearDfstSwSts = 1;
			// }
			// }

			airInfo.ACP_RearDfstSwSts = 1;
			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.energy_up_iv_wind_speed:
			int wind_speed_currentItem_up = wind_speed_numberpicker.getSpeedIndex();
			wind_speed_currentItem_up--;
			if (wind_speed_currentItem_up < 0) {
				wind_speed_currentItem_up = 0;
			}
			wind_speed_numberpicker.setSpeedIndex(wind_speed_currentItem_up);

			sendWindSpeed(wind_speed_currentItem_up + 1);

			break;

		case R.id.energy_down_iv_wind_speed:
			int wind_speed_currentItem_down = wind_speed_numberpicker.getSpeedIndex();
			wind_speed_currentItem_down++;
			if (wind_speed_currentItem_down > 7) {
				wind_speed_currentItem_down = 7;
			}
			wind_speed_numberpicker.setSpeedIndex(wind_speed_currentItem_down);
			sendWindSpeed(wind_speed_currentItem_down + 1);
			break;

		case R.id.air_ac_iv:
			// if ((Boolean) air_ac_iv.getTag()) {
			// air_ac_iv.setTag(false);
			// air_ac_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_AC_SwSts = 1;
			// }
			// } else {
			// air_ac_iv.setTag(true);
			// air_ac_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_AC_SwSts = 1;
			// }
			// }

			airInfo.ACP_AC_SwSts = 1;
			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.air_auto_iv:
			// if ((Boolean) air_auto_iv.getTag()) {
			// air_auto_iv.setTag(false);
			// air_auto_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_AUTO_SwSts = 1;
			// }
			// } else {
			// air_auto_iv.setTag(true);
			// air_auto_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_AUTO_SwSts = 1;
			// }
			// }

			airInfo.ACP_AUTO_SwSts = 1;
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.air_cycle_out_iv:
			// 外循环
			// air_cycle_out_rl.setVisibility(View.GONE);
			// air_cycle_in_rl.setVisibility(View.VISIBLE);
			// air_cycle_in_iv.setSelected(true);
			// air_cycle_in_iv.setTag(true);
			// air_cycle_out_iv.setSelected(false);
			// air_cycle_out_iv.setTag(false);

			if (airInfo != null) {
				airInfo.ACP_CirculationModeSwSts = 1;

				airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
				airInfo.ACP_AC_SwSts = 0;// AC开关状态
				airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
				airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
				airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
				airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
				airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
				airInfo.ACP_TempSet = 0;// 温度设置
				airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
				airInfo.ACP_IHU_Volume = 0;// DVD音量调节
				airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

				T19CanTx.getInstance().sendAirInfoData(airInfo);
			}

			break;

		case R.id.air_cycle_in_iv:
			// 内循环
			// air_cycle_out_rl.setVisibility(View.VISIBLE);
			// air_cycle_in_rl.setVisibility(View.GONE);
			// air_cycle_out_iv.setSelected(true);
			// air_cycle_out_iv.setTag(true);
			// air_cycle_in_iv.setSelected(false);
			// air_cycle_in_iv.setTag(false);

			if (airInfo != null) {
				airInfo.ACP_CirculationModeSwSts = 1;

				airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
				airInfo.ACP_AC_SwSts = 0;// AC开关状态
				airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
				airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
				airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
				airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
				airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
				airInfo.ACP_TempSet = 0;// 温度设置
				airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
				airInfo.ACP_IHU_Volume = 0;// DVD音量调节
				airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

				T19CanTx.getInstance().sendAirInfoData(airInfo);
			}
			break;

		case R.id.air_close_iv:
			// if ((Boolean) air_close_iv.getTag()) {
			// air_close_iv.setTag(false);
			// air_close_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_OFF_SwSts = 1;
			// }
			// } else {
			// air_close_iv.setTag(true);
			// air_close_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_OFF_SwSts = 1;
			// }
			// }

			airInfo.ACP_OFF_SwSts = 1;
			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.air_ptc_iv:
			// if ((Boolean) air_ptc_iv.getTag()) {
			// air_ptc_iv.setTag(false);
			// air_ptc_iv.setSelected(false);
			// if (airInfo != null) {
			// airInfo.ACP_PTC_SwSts = 1;
			// }
			// } else {
			// air_ptc_iv.setTag(true);
			// air_ptc_iv.setSelected(true);
			// if (airInfo != null) {
			// airInfo.ACP_PTC_SwSts = 1;
			// }
			// }

			airInfo.ACP_PTC_SwSts = 1;
			airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
			airInfo.ACP_AC_SwSts = 0;// AC开关状态
			airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
			airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
			airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
			airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
			airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
			airInfo.ACP_TempSet = 0;// 温度设置
			airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
			airInfo.ACP_IHU_Volume = 0;// DVD音量调节
			airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

			T19CanTx.getInstance().sendAirInfoData(airInfo);
			break;

		case R.id.close_air:
			if (IsShow()) {
				Hide();
				mbAirAutoCloseFlag = false;
			}
			break;

		default:
			break;
		}

	}

	// 风机档位(默认值为记忆的上次档位值)
	public static void sendWindSpeed(int cur_wind_speed) {

		AirInfo airInfo = new AirInfo();
		airInfo.ACP_BlowerLeverSet = cur_wind_speed;
		airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
		airInfo.ACP_AC_SwSts = 0;// AC开关状态
		airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
		airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
		airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
		airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
		airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
		airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
		airInfo.ACP_TempSet = 0;// 温度设置
		airInfo.ACP_IHU_Volume = 0;// DVD音量调节
		airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

		T19CanTx.getInstance().sendAirInfoData(airInfo);
	}

	// 温度设置
	public static void sendTempSet(int cur_temp) {

		AirInfo airInfo = new AirInfo();

		airInfo.ACP_TempSet = cur_temp;// 温度设置

		airInfo.ACP_AUTO_SwSts = 0;// AUTO开关状态
		airInfo.ACP_AC_SwSts = 0;// AC开关状态
		airInfo.ACP_PTC_SwSts = 0;// PTC开关状态
		airInfo.ACP_OFF_SwSts = 0;// OFF开关状态
		airInfo.ACP_RearDfstSwSts = 0;// 后除霜开关状态
		airInfo.ACP_CirculationModeSwSts = 0;// 内外循环开关状态
		airInfo.ACP_AQS_SwSts = 0;// AQS开关状态
		airInfo.ACP_BlowerLeverSet = 0;// 风机档位(默认值为记忆的上次档位值)
		airInfo.ACP_BlowModeSet = 0;// 吹风模式Mode设置
		airInfo.ACP_IHU_Volume = 0;// DVD音量调节
		airInfo.ACP_IHU_TrackListSelect = 0;// DVD上下曲目选择

		T19CanTx.getInstance().sendAirInfoData(airInfo);
	}

}
