package com.carocean.t19can;

import com.carocean.t19can.T19CanRx.AirInfo;
import com.carocean.t19can.T19CanRx.CarHelpAirInfo;
import com.carocean.t19can.T19CanRx.CarbodyInfo;
import com.carocean.t19can.T19CanRx.DashboardInfo;
import com.carocean.t19can.T19CanRx.EnergyInfo;
import com.carocean.t19can.T19CanRx.PedestrianInfo;
import com.carocean.t19can.T19CanRx.PowerInfo;

import android.content.Context;
import android.mcu.McuManager;
import android.util.Log;

//单例模式
//通过MCU发送数据给CAN

public class T19CanTx {
	private final String TAG = "CanTx_T19";

	private static T19CanTx mInstance = null;
	private McuManager mMcuManager;
	private CanBusService mCanBusService;

	private T19CanTx() {

	}

	public static T19CanTx getInstance() {
		if (null == mInstance) {
			mInstance = new T19CanTx();
		}
		return mInstance;
	}

	public void init(CanBusService instance) {
		Log.i(TAG, "init");
		mCanBusService = instance;
		mMcuManager = (McuManager) mCanBusService.getSystemService(Context.MCU_SERVICE);

	}

	// 能量释放及回收
	public void sendEnergyReg(EnergyInfo mEnergyInfo) {

		byte[] data = new byte[5];
		data[0] = (byte) 0x10;
		data[1] = (byte) mEnergyInfo.VCU_MaxRegenerationLevelEnableSts;
		data[2] = (byte) mEnergyInfo.VCU_RegenerationLevelSts;
		data[3] = (byte) 0;
		data[3] = (byte) 0;

		sendData(data, data.length);
	}

	// 仪表
	public void sendDashboardData(DashboardInfo mDashboardInfo) {

		byte[] data = new byte[7];
		data[0] = (byte) 0x12;
		data[1] = (byte) mDashboardInfo.ICM_CurrentTime_Hour;
		data[2] = (byte) mDashboardInfo.ICM_CurrentTime_Minute;
		data[3] = (byte) mDashboardInfo.ICM_OverSpdValue;
		data[4] = (byte) mDashboardInfo.ICM_FatigureDrivingTimeSet;
		data[5] = (byte) mDashboardInfo.IHU_BrightnessAdjustSet_ICM;
		data[6] = (byte) 0;
		sendData(data, data.length);
	}

	// 行人
	public void sendPedestrianData(PedestrianInfo mPedestrianInfo) {

		byte[] data = new byte[4];
		data[0] = (byte) 0x11;
		data[1] = (byte) mPedestrianInfo.AVAS_SwSts;
		data[2] = (byte) mPedestrianInfo.AVAS_VolumeSts;
		data[3] = (byte) mPedestrianInfo.AVAS_AudioSourceSts;
		sendData(data, data.length);
	}

	// 车身
	public void sendCarbodyData(CarbodyInfo mCarbodyInfo) {

		byte[] data = new byte[5];
		data[0] = (byte) 0x13;
		data[1] = (byte) ((mCarbodyInfo.BCM_AmbientLightEnableSts & 0x03)
				| ((mCarbodyInfo.BCM_DRL_EnableSts & 0x03) << 2) | ((mCarbodyInfo.BCM_MirrorFoldEnableSts & 0x03) << 4)
				| ((mCarbodyInfo.BCM_AutoLockEnableSts & 0x03) << 6));
		data[2] = (byte) ((mCarbodyInfo.BCM_FollowMeSts & 0x03) | ((mCarbodyInfo.BCM_AntiTheftModeSts & 0x03) << 2)
				| ((mCarbodyInfo.BCM_SunroofAutoCloseEnableSts & 0x03) << 4) | (0 << 6));
		data[3] = (byte) mCarbodyInfo.BCM_AmbientLightBrightnessSts;
		data[4] = (byte) mCarbodyInfo.BCM_RearDfstSts;
		sendData(data, data.length);
	}

	// 车况空调
	public void sendCarHelpAirData(CarHelpAirInfo mCarHelpAirInfo) {

		byte[] data = new byte[7];
		data[0] = (byte) 0x14;
		data[1] = (byte) mCarHelpAirInfo.ACCM_AutoCleanEnableSts;
		data[2] = (byte) ((mCarHelpAirInfo.ACCM_AutoBlowEnableSts & 0x03)
				| ((mCarHelpAirInfo.IHU_ACPMemoryMode & 0x03) << 2));
		data[3] = (byte) mCarHelpAirInfo.ACCM_AutoCleanActiveSts;
		data[4] = (byte) mCarHelpAirInfo.ACCM_AutoBlowActiveSts;
		data[5] = (byte) mCarHelpAirInfo.ACCM_AutoAdjustCtrlSource;
		data[6] = (byte) mCarHelpAirInfo.ACCM_InternalTemp;

		sendData(data, data.length);
	}

	// 动力
	public void sendPowerInfoData(PowerInfo mPowerInfo) {

		byte[] data = new byte[12];
		data[0] = (byte) 0x15;
		data[1] = (byte) mPowerInfo.VCU_cESC_EnableSts;
		data[2] = (byte) mPowerInfo.VCU_eHAC_EnableSts;
		data[3] = (byte) mPowerInfo.VCU_eHDC_EnableSts;
		data[4] = (byte) mPowerInfo.VCU_eAVH_EnableSts;
		data[5] = (byte) mPowerInfo.VCU_eAVH_TimeSetSts;
		data[6] = (byte) mPowerInfo.VCU_eAVH_ReverseDisableSts;
		data[7] = (byte) mPowerInfo.VCU_LongRangeModeEnableSts;
		data[8] = (byte) mPowerInfo.VCU_MaxSpdLimitEnableSts;
		data[9] = (byte) mPowerInfo.VCU_SpeedLimitValueSetSts;
		data[10] = (byte) mPowerInfo.VCU_MaxRegenerationLevelEnableSts;
		data[11] = (byte) mPowerInfo.VCU_RegenerationLevelSts;

		sendData(data, data.length);
	}

	// 空调
	public void sendAirInfoData(AirInfo mAirInfo) {

		byte[] data = new byte[7];
		data[0] = (byte) 0x16;
		data[1] = (byte) ((mAirInfo.ACP_AUTO_SwSts & 0x01) | ((mAirInfo.ACP_AC_SwSts & 0x01) << 1)
				| ((mAirInfo.ACP_PTC_SwSts & 0x01) << 2) | ((mAirInfo.ACP_OFF_SwSts & 0x01) << 3)
				| ((mAirInfo.ACP_RearDfstSwSts & 0x01) << 4) | ((mAirInfo.ACP_CirculationModeSwSts & 0x01) << 5)
				| ((mAirInfo.ACP_AQS_SwSts & 0x01) << 6));
		data[2] = (byte) mAirInfo.ACP_BlowModeSet;
		data[3] = (byte) mAirInfo.ACP_TempSet;
		data[4] = (byte) mAirInfo.ACP_BlowerLeverSet;
		data[5] = (byte) mAirInfo.ACP_IHU_Volume;
		data[6] = (byte) mAirInfo.ACP_IHU_TrackListSelect;

		sendData(data, data.length);
	}

	// 天窗及窗口车灯控制
	public void sendVoiceCtrlCarData(int IHU_VoiceCtrl_SunroofReq, int IHU_VoiceCtrl_LowbeamReq,
			int IHU_VoiceCtrl_WindowReq_FL, int IHU_VoiceCtrl_WindowReq_FR, int IHU_VoiceCtrl_WindowReq_RL,
			int IHU_VoiceCtrl_WindowReq_RR) {

		byte[] data = new byte[6];
		data[0] = (byte) 0x06;
		data[1] = (byte) ((IHU_VoiceCtrl_SunroofReq & 0x0F) | ((IHU_VoiceCtrl_LowbeamReq & 0x0F) << 4));
		data[2] = (byte) IHU_VoiceCtrl_WindowReq_FL;
		data[3] = (byte) IHU_VoiceCtrl_WindowReq_FR;
		data[4] = (byte) IHU_VoiceCtrl_WindowReq_RL;
		data[5] = (byte) IHU_VoiceCtrl_WindowReq_RR;

		sendData(data, data.length);
	}

	// GPS时间
	public void sendGpsTimeData(int IHU_GPS_Time_year, int IHU_GPS_Time_Month, int IHU_GPS_Time_Day,
			int IHU_GPS_Time_Hour, int IHU_GPS_Time_Minute, int IHU_GPS_Time_Second, int IHU_DVR_QuickCameraSw) {

		byte[] data = new byte[8];
		data[0] = (byte) 0x05;
		data[1] = (byte) IHU_GPS_Time_year;
		data[2] = (byte) IHU_GPS_Time_Month;
		data[3] = (byte) IHU_GPS_Time_Day;
		data[4] = (byte) IHU_GPS_Time_Hour;
		data[5] = (byte) IHU_GPS_Time_Minute;
		data[6] = (byte) IHU_GPS_Time_Second;
		data[7] = (byte) IHU_DVR_QuickCameraSw;

		sendData(data, data.length);
	}

	public void sendData(byte[] data, int len) {
		if (null != mMcuManager) {
			try {
				mMcuManager.RPC_SendCANInfo(data, len);
				Log.i(TAG, "Send::len = " + len + " data = " + byteToString(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int getChecksum(byte[] data, int len) {
		int num = 0;
		for (int i = 0; i < data.length; i++) {
			num ^= data[i];
		}
		return num;
	}

	public static String byteToString(byte[] data) {
		StringBuffer str = new StringBuffer();
		for (byte d : data) {
			str.append(String.format("%02X ", d));
		}
		return str.toString();
	}

	// 速度转换
	public int changeSpeed(int cur_Progress, int m_MaxProgress, int m_MinProgress, int m_MaxSpeed, int m_MinSpeed) {
		int fRange_progress = m_MaxProgress - m_MinProgress;
		int fRange_speed = m_MaxSpeed - m_MinSpeed;
		int s = (int) ((cur_Progress - m_MinProgress) * fRange_speed / fRange_progress + m_MinSpeed);
		return s;
	}

	// 时间转换
	public float changeTime(int cur_Progress, int m_MaxProgress, int m_MinProgress, float m_MaxTime, float m_MinTime) {
		int fRange_progress = m_MaxProgress - m_MinProgress;
		float fRange_time = m_MaxTime - m_MinTime;
		float s = (float) ((cur_Progress - m_MinProgress) * fRange_time / (fRange_progress * 10 / 10.0) + m_MinTime);
		return s;
	}

	// 温度转换
	public float changeTem(int cur_Progress, int m_MaxProgress, int m_MinProgress, float m_MaxTem, float m_MinTem) {
		int fRange_progress = m_MaxProgress - m_MinProgress;
		float fRange_tem = m_MaxTem - m_MinTem;
		float s = (float) ((cur_Progress - m_MinProgress) * fRange_tem / (fRange_progress * 10 / 10.0) + m_MinTem);
		return s;
	}

	// 温度转换成发给mcu的数值
	public int changeTem_to_send(int cur_Progress, int fRange_temp, int fRange_progress) {
		int s = (((cur_Progress * 10 - 180) * fRange_progress) / (fRange_temp * 10));
		return s;
	}

}
