package com.carocean.t19can;

import java.util.Arrays;

import com.nostra13.universalimageloader.utils.L;

import android.content.Context;
import android.mcu.McuBaseInfo;
import android.mcu.McuExternalConstant;
import android.mcu.McuListener;
import android.mcu.McuManager;
import android.os.RemoteException;
import android.util.Log;

public class T19CanRx {
	private String TAG = "T19CanRxTx";
	// 定义接收数据缓冲最大长度
	private final int miMaxLength = 4096 * 10;
	// 数据缓冲区
	private byte[] mBuffer = new byte[miMaxLength];
	// 每次收到实际长度
	private int miAvailable = 0;
	// 当前已经收到包的总长度
	private int mpRemainingLength[] = new int[1];
	// 最小包长度
	private int mpMinPacketLength[] = new int[1];
	// 分析buf有效数据起点
	private int mpCursor[] = new int[1];
	final byte mDataLenCursor = (byte) 4;
	final byte mDataCursor = (byte) 13;

	public static final int MSG_CAN_DATA = 333;
	public static final int carstatusInfoType = 0x001;
	public static final int energyInfoType = 0x002;
	public static final int pedestrianInfoType = 0x003;
	public static final int dashboardInfoType = 0x004;
	public static final int carbodyInfoType = 0x005;
	public static final int carHelpAirInfoType = 0x006;
	public static final int powerInfoType = 0x007;
	public static final int lowPowerInfoType = 0x008;
	public static final int vcuInfoType = 0x009;
	public static final int dashboardConfigInfo1Type = 0x010;
	public static final int airInfoType = 0x100;
	public static final int tipsInfoType = 0x101;

	McuManager mMcuMananger = null;
	private static T19CanRx mT19CanRx = null;
	private CanBusService mCanBusService;

	public CarStatusInfo mCarStatusInfo = null;
	public EnergyInfo mEnergyInfo = null;
	public LowPowerInfo mLowPowerInfo = null;
	public PedestrianInfo mPedestrianInfo = null;
	public DashboardInfo mDashboardInfo = null;
	public CarbodyInfo mCarbodyInfo = null;
	public CarHelpAirInfo mCarHelpAirInfo = null;
	public PowerInfo mPowerInfo = null;
	public AirInfo mAirInfo = null;
	public TipsInfo mTipsInfo = null;
	public VcuInfo mVcuInfo = null;
	public DashboardConfigInfo1 mDashboardConfigInfo1 = null;

	// 空调标志，开机起来第一次不弹出
	public boolean flag_show_air = false;
	public static byte[] byte_original_airData = new byte[7];

	private T19CanRx() {

	}

	public static T19CanRx getInstance() {
		if (null == mT19CanRx) {
			mT19CanRx = new T19CanRx();
		}
		return mT19CanRx;
	}

	public void init(CanBusService instance) {
		L.i(TAG, "init");
		mCanBusService = CanBusService.getInstance();
		mMcuMananger = (McuManager) mCanBusService.getSystemService(Context.MCU_SERVICE);

		mCarStatusInfo = new CarStatusInfo();
		mEnergyInfo = new EnergyInfo();
		mLowPowerInfo = new LowPowerInfo();
		mPedestrianInfo = new PedestrianInfo();
		mDashboardInfo = new DashboardInfo();
		mCarbodyInfo = new CarbodyInfo();
		mCarHelpAirInfo = new CarHelpAirInfo();
		mPowerInfo = new PowerInfo();
		mAirInfo = new AirInfo();
		mTipsInfo = new TipsInfo();
		mVcuInfo = new VcuInfo();
		mDashboardConfigInfo1 = new DashboardConfigInfo1();

		try {
			mMcuMananger.RPC_RequestMcuInfoChangedListener(mMcuListener);
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
		}
	}

	public void onDestroy() {
		L.i(TAG, "onDestroy");
		try {
			mMcuMananger.RPC_RemoveMcuInfoChangedListener(mMcuListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private McuListener mMcuListener = new McuListener() {
		@Override
		public void onMcuInfoChanged(McuBaseInfo mcuBaseInfo, int infoType) {
			if (infoType == McuExternalConstant.MCU_CANBUS_INFO_TYPE) {
				OnCanRxDataT19(infoType, mcuBaseInfo.getOriginalInfo().getMcuData(),
						mcuBaseInfo.getOriginalInfo().getMcuData().length);
			}
		}
	};

	/**
	 * @ClassName: CarStatusInfo
	 * @Functions: 车辆状况
	 */
	public class CarStatusInfo {

		public int BCM_KeySts;// 电源档位
		public int BCM_PosLightSts;// 位置灯状态
		public int BCM_DoorAjarSts_Driver;// 主驾驶门状态
		public int BCM_DoorAjarSts_Psngr;// 副驾驶门状态
		public int BCM_DoorAjarSts_RR;// 右后门状态
		public int BCM_DoorAjarSts_RL;// 左后门状态
		public int BCM_Reserved;// 预留-前舱盖状态
		public int BCM_TrunkAjarSts;// 后备箱门状态
		public int BCM_AntiTheftMode;// 设防模式
		public int BCM_TirePositionWarn_FL;// 左前胎压报警
		public int BCM_TirePositionWarn_FR;// 右前胎压报警
		public int BCM_TirePosition;// 轮胎位置
		public int BCM_TirePositionWarn_RL;// 左后胎压报警
		public int BCM_TirePositionWarn_RR;// 右后胎压报警
		public int BCM_TirePressureWarnLampDisp;// 胎压报警灯状态
		public int BCM_TirePressureSystemFault;// 胎压系统故障状态
		public int BCM_TireTemp;// 轮胎温度
		public float BCM_TirePressure_FL;// 左前轮胎胎压
		public float BCM_TirePressure_FR;// 右前轮胎胎压
		public float BCM_TirePressure_RL;// 左后轮胎胎压
		public float BCM_TirePressure_RR;// 右后轮胎胎压
		public int VCU_LongRangeRemainingTime;// 长航模式剩余次数
		public float VCU_InstPowerConsum;// 百公里电耗显示
		public int VCU_ResidualOdometer;// 续航里程显示
		public int VCU_VehSpd;// VCU车速

	}

	/**
	 * @ClassName: LowPowerInfo
	 * @Functions: 低电量指示
	 */
	public class LowPowerInfo {

		public int ICM_LowSOC_LampSts;// 0:OFF,1:ON(IHU接收，在低电量指示灯亮起时推送附近充电桩位置信)

	}

	/**
	 * @ClassName: EnergyInfo
	 * @Functions: 能量回收
	 */
	public class EnergyInfo {

		public int VCU_MaxRegenerationLevelEnableSts;// 最高等级能量回收使能状态，0x0:非活动/初始值，0x1:禁用，0x2:使能，0x3:预留
		public int VCU_RegenerationLevelSts;// 能量回收状态，0x0:非活动/初始值，0x1:低，0x2:中，0x3:高，0x4:大，0x5-0x7:保留
		public int BMS_BattCurrDisp;// 动力电池电流显示
		public int BMS_ChgWireConnectStsDisp;// 充电连接显示

	}

	/**
	 * @ClassName: PedestrianInfo
	 * @Functions: 行人
	 */
	public class PedestrianInfo {

		// 行人提醒
		public int AVAS_SwSts;// 开关状态
		public int AVAS_VolumeSts;// 音量档位状态
		public int AVAS_AudioSourceSts;// 音源状态

	}

	/**
	 * @ClassName: DashboardInfo
	 * @Functions: 仪表
	 */
	public class DashboardInfo {

		public int ICM_CurrentTime_Hour;// 当前时间-小时
		public int ICM_CurrentTime_Minute;// 当前时间-分钟
		public int ICM_OverSpdValue;// 车速报警值
		public int ICM_FatigureDrivingTimeSet;// 疲劳驾驶时间设置
		public int IHU_BrightnessAdjustSet_ICM;// 仪表亮度
		public int ICM_OverSpdSwitch;// 车速报警开关
		public int ICM_FatigureDrivingTimeSwitch;// 疲劳驾驶时间开关
	}

	/**
	 * @ClassName: CarbodyInfo
	 * @Functions: 车身
	 */
	public class CarbodyInfo {

		public int BCM_AmbientLightEnableSts;// 氛围灯使能状态
		public int BCM_DRL_EnableSts;// 日间行车灯使能状态 (装饰灯)
		public int BCM_MirrorFoldEnableSts;// 设防后视镜折叠使能状态
		public int BCM_AutoLockEnableSts;// 自动落锁使能状态
		public int BCM_FollowMeSts;// 伴我回家使能状态
		public int BCM_AntiTheftModeSts;// 设防提示模式设置状态
		public int BCM_SunroofAutoCloseEnableSts;// 设防自动关闭天窗使能状态
		public int BCM_AmbientLightBrightnessSts;// 氛围灯亮度状态
		public int BCM_RearDfstSts;// 后除霜状态
	}

	/**
	 * @ClassName: CarHelpAirInfo
	 * @Functions: 空调
	 */
	public class CarHelpAirInfo {

		// 空调设置
		public int ACCM_AutoCleanEnableSts;// 空调自动清洁功能使能状态
		public int ACCM_AutoBlowEnableSts;// 空调自通风功能使能状态
		public int IHU_ACPMemoryMode;// 空调状态记忆使能开关
		public int ACCM_AutoCleanActiveSts;// 空调自动清洁激活状态
		public int ACCM_AutoBlowActiveSts;// 空调自通风激活状态
		public int ACCM_AutoAdjustCtrlSource;// 空调AUTO调节控制来源
		public int ACCM_InternalTemp;// 室内温度
	}

	/**
	 * @ClassName: PowerInfo
	 * @Functions: 动力
	 */
	public class PowerInfo {

		public int VCU_cESC_EnableSts;// 驱动防滑功能使能状态
		public int VCU_eHAC_EnableSts;// 坡道起步功能使能状态
		public int VCU_eHDC_EnableSts;// 陡坡缓降功能使能状态
		public int VCU_eAVH_EnableSts;// AVH使能状态
		public int VCU_eAVH_TimeSetSts;// AVH时间设置状态
		public int VCU_eAVH_ReverseDisableSts;// AVH倒车禁用使能状态
		public int VCU_LongRangeModeEnableSts;// 长航模式功能使能状态
		public int VCU_MaxSpdLimitEnableSts;// 车速限制功能使能状态
		public int VCU_SpeedLimitValueSetSts;// 车速限制值状态
		public int VCU_MaxRegenerationLevelEnableSts;// 最高等级能量回收使能状态
		public int VCU_RegenerationLevelSts;// 能量回收状态
	}

	/**
	 * @ClassName: AirInfo
	 * @Functions: 空调信息
	 */
	public static class AirInfo {

		public int ACP_AUTO_SwSts;// AUTO开关状态
		public int ACP_AC_SwSts;// AC开关状态
		public int ACP_PTC_SwSts;// PTC开关状态
		public int ACP_OFF_SwSts;// OFF开关状态
		public int ACP_RearDfstSwSts;// 后除霜开关状态
		public int ACP_CirculationModeSwSts;// 内外循环开关状态
		public int ACP_AQS_SwSts;// AQS开关状态
		public int ACP_BlowModeSet;// 吹风模式Mode设置
		public int ACP_TempSet;// 温度设置
		public int ACP_BlowerLeverSet;// 风机档位(默认值为记忆的上次档位值)
		public int ACP_IHU_Volume;// DVD音量调节
		public int ACP_IHU_TrackListSelect;// DVD上下曲目选择

	}

	/**
	 * @ClassName: TipsInfo
	 * @Functions: 提示框
	 */
	public static class TipsInfo {

		public int tipContent;// 弹框提示信息
		public int tipShowTime;// 弹框显示时间
	}

	/**
	 * @ClassName: VcuInfo
	 * @Functions: Vcu状态显示
	 */
	public static class VcuInfo {

		public int VCU_GearboxPositionDisp;// 档位显示
		public int VCU_DriveMode;// 车辆驾驶模式
		public float VCU_ExternalTempDisp;// 室外温度显示
		public int VCU_SOC_Disp;// 电量显示
	}

	/**
	 * @ClassName: DashboardConfigInfo1
	 * @Functions: 仪表在线配置1
	 */
	public static class DashboardConfigInfo1 {

		public int ICM_DRL_ConfigurationSts;// 装饰灯状态
		public int ICM_AmbientLight_ConfigurationSts;// 氛围灯状态
		public int ICM_MirrorAutoFoldConfigurationSts;// 设防后视镜折叠配置状态

	}

	public void OnCanRxDataT19(int infoType, byte[] data, int length) {

		CanInfo.RxEx(data, length);
		if (length > 0) {
			parseCanData(data);
		}

	}

	public void parseCanData(byte[] packet) {

		byte bData0 = packet[0];
		int sid = bData0 & 0xFF;

		switch (sid) {
		case 0x01:
			// 车辆信息
			if (packet.length == 22) {
				parseCarStatusInfo(packet, mCarStatusInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, carstatusInfoType, 0, mCarStatusInfo, 0);
			}
			break;
		case 0x02:
			// 提示框
			if (packet.length == 3) {
				parseTipsInfo(packet, mTipsInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, tipsInfoType, 0, mTipsInfo, 0);
			}
			break;
		case 0x04:
			if (packet.length == 3) {
				parseLowPowerInfo(packet, mLowPowerInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, lowPowerInfoType, 0, mLowPowerInfo, 0);
			}
			break;
		case 0x10:
			// 能量回收及释放
			if (packet.length == 6) {
				parseEnergyInfo(packet, mEnergyInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, energyInfoType, 0, mEnergyInfo, 0);
			}
			break;

		case 0x12:
			// 仪表
			if (packet.length == 7) {
				parseDashboardInfo(packet, mDashboardInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, dashboardInfoType, 0, mDashboardInfo, 0);
			}
			break;
		case 0x11:
			// 行人
			if (packet.length == 4) {
				parsePedestrianInfo(packet, mPedestrianInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, pedestrianInfoType, 0, mPedestrianInfo, 0);
			}
			break;
		case 0x13:
			// 车身
			if (packet.length == 5) {
				parseCarbodyInfo(packet, mCarbodyInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, carbodyInfoType, 0, mCarbodyInfo, 0);
			}
			break;
		case 0x14:
			// 空调
			if (packet.length == 7) {
				parseCarHelpAirInfo(packet, mCarHelpAirInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, carHelpAirInfoType, 0, mCarHelpAirInfo, 0);
			}
			break;
		case 0x15:
			// 动力
			if (packet.length == 12) {
				parsePowerInfo(packet, mPowerInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, powerInfoType, 0, mPowerInfo, 0);
			}
			break;
		case 0x16:
			// 显示空调
			if (packet.length == 7) {
				if (flag_show_air) {
					if (!Arrays.equals(packet, byte_original_airData)) {
						System.arraycopy(packet, 0, byte_original_airData, 0, 7);
						Log.e(TAG, "byte_original_airData[]： " + Arrays.toString(byte_original_airData));
						parseAirInfo(byte_original_airData, mAirInfo);
						mCanBusService.sendToHandler(MSG_CAN_DATA, airInfoType, 1, mAirInfo, 0);
					}
				} else {
					System.arraycopy(packet, 0, byte_original_airData, 0, 7);
					parseAirInfo(byte_original_airData, mAirInfo);
					mCanBusService.sendToHandler(MSG_CAN_DATA, airInfoType, 0, mAirInfo, 0);
				}
				flag_show_air = true;
			}
			break;
		case 0x44:
			// VCU状态显示
			if (packet.length == 5) {
				parseVcuInfo(packet, mVcuInfo);
				mCanBusService.sendToHandler(MSG_CAN_DATA, vcuInfoType, 0, mVcuInfo, 0);
			}
			break;
		case 0x41:
			if (packet.length == 8) {
				parseDashboardConfigInfo1(packet, mDashboardConfigInfo1);
				mCanBusService.sendToHandler(MSG_CAN_DATA, dashboardConfigInfo1Type, 0, mDashboardConfigInfo1, 0);
			}
			break;

		default:
			break;
		}

	}

	// 解析车辆信息数据
	public CarStatusInfo parseCarStatusInfo(byte[] packet, CarStatusInfo mCarStatusInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];
		byte bData6 = packet[6];
		byte bData7 = packet[7];
		byte bData8 = packet[8];
		byte bData9 = packet[9];
		byte bData10 = packet[10];
		byte bData11 = packet[11];
		byte bData12 = packet[12];
		byte bData13 = packet[13];
		byte bData14 = packet[14];
		byte bData15 = packet[15];
		byte bData16 = packet[16];
		byte bData17 = packet[17];
		byte bData18 = packet[18];
		byte bData19 = packet[19];
		byte bData20 = packet[20];
		byte bData21 = packet[21];

		mCarStatusInfo.BCM_KeySts = bData1 & 0xFF;
		mCarStatusInfo.BCM_PosLightSts = bData2 & 0x01;// 位置灯状态
		mCarStatusInfo.BCM_DoorAjarSts_Driver = (bData2 >> 1) & 0x01;// 主驾驶门状态
		mCarStatusInfo.BCM_DoorAjarSts_Psngr = (bData2 >> 2) & 0x01;// 副驾驶门状态
		mCarStatusInfo.BCM_DoorAjarSts_RR = (bData2 >> 3) & 0x01;// 右后门状态
		mCarStatusInfo.BCM_DoorAjarSts_RL = (bData2 >> 4) & 0x01;// 左后门状态
		mCarStatusInfo.BCM_Reserved = (bData2 >> 5) & 0x01;// 预留-前舱盖状态
		mCarStatusInfo.BCM_TrunkAjarSts = (bData2 >> 6) & 0x01;// 后备箱门状态
		mCarStatusInfo.BCM_AntiTheftMode = bData3 & 0xFF;// 设防模式
		mCarStatusInfo.BCM_TirePositionWarn_FL = bData4 & 0xFF;// 左前胎压报警
		mCarStatusInfo.BCM_TirePositionWarn_FR = bData5 & 0xFF;// 右前胎压报警
		mCarStatusInfo.BCM_TirePosition = bData6 & 0xFF;// 轮胎位置
		mCarStatusInfo.BCM_TirePositionWarn_RL = bData7 & 0xFF;// 左后胎压报警
		mCarStatusInfo.BCM_TirePositionWarn_RR = bData8 & 0xFF;// 右后胎压报警
		mCarStatusInfo.BCM_TirePressureWarnLampDisp = bData9 & 0xFF;// 胎压报警灯状态
		mCarStatusInfo.BCM_TirePressureSystemFault = bData10 & 0xFF;// 胎压系统故障状态
		mCarStatusInfo.BCM_TireTemp = -40 + (bData11 & 0xFF);// 轮胎温度
		mCarStatusInfo.BCM_TirePressure_FL = (float) ((bData12 & 0xFF) * 0.018);// 左前轮胎胎压
		mCarStatusInfo.BCM_TirePressure_FR = (float) ((bData13 & 0xFF) * 0.018);// 右前轮胎胎压
		mCarStatusInfo.BCM_TirePressure_RL = (float) ((bData14 & 0xFF) * 0.018);// 左后轮胎胎压
		mCarStatusInfo.BCM_TirePressure_RR = (float) ((bData15 & 0xFF) * 0.018);// 右后轮胎胎压
		mCarStatusInfo.VCU_LongRangeRemainingTime = bData16 & 0xFF;// 长航模式剩余次数
		mCarStatusInfo.VCU_InstPowerConsum = (float) ((bData17 & 0xFF) * 0.196);// 百公里电耗显示
		mCarStatusInfo.VCU_ResidualOdometer = (int) (((bData18 & 0xFF) * 256 + (bData19 & 0xFF)) * 0.1);// 续航里程显示
		mCarStatusInfo.VCU_VehSpd = (int) (((bData20 & 0xFF) * 256 + (bData21 & 0xFF)) * 0.0625);// VCU车速

		return mCarStatusInfo;

	}

	// 解析能量回收及释放数据
	public EnergyInfo parseEnergyInfo(byte[] packet, EnergyInfo mEnergyInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];

		mEnergyInfo.VCU_MaxRegenerationLevelEnableSts = bData1 & 0xFF;
		mEnergyInfo.VCU_RegenerationLevelSts = bData2 & 0xFF;
		mEnergyInfo.BMS_BattCurrDisp = (int) ((bData3 & 0xFF) * 256 + (bData4 & 0xFF));
		mEnergyInfo.BMS_ChgWireConnectStsDisp = bData5 & 0xFF;

		return mEnergyInfo;

	}

	// 解析低电量数据
	public LowPowerInfo parseLowPowerInfo(byte[] packet, LowPowerInfo mLowPowerInfo) {

		byte bData1 = packet[1];

		mLowPowerInfo.ICM_LowSOC_LampSts = bData1 & 0xFF;

		return mLowPowerInfo;

	}

	// 解析仪表数据
	public DashboardInfo parseDashboardInfo(byte[] packet, DashboardInfo mDashboardInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];
		byte bData6 = packet[6];

		mDashboardInfo.ICM_CurrentTime_Hour = bData1 & 0xFF;
		mDashboardInfo.ICM_CurrentTime_Minute = bData2 & 0xFF;
		mDashboardInfo.ICM_OverSpdValue = bData3 & 0xFF;
		mDashboardInfo.ICM_FatigureDrivingTimeSet = bData4 & 0xFF;
		mDashboardInfo.IHU_BrightnessAdjustSet_ICM = bData5 & 0xFF;
		mDashboardInfo.ICM_OverSpdSwitch = bData6 & 0xFF;

		return mDashboardInfo;
	}

	// 解析行人数据
	public PedestrianInfo parsePedestrianInfo(byte[] packet, PedestrianInfo mPedestrianInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];

		mPedestrianInfo.AVAS_SwSts = bData1 & 0xFF;
		mPedestrianInfo.AVAS_VolumeSts = bData2 & 0xFF;
		mPedestrianInfo.AVAS_AudioSourceSts = bData3 & 0xFF;

		return mPedestrianInfo;
	}

	// 解析车身数据
	public CarbodyInfo parseCarbodyInfo(byte[] packet, CarbodyInfo mCarbodyInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];

		mCarbodyInfo.BCM_AmbientLightEnableSts = bData1 & 0x03;
		mCarbodyInfo.BCM_DRL_EnableSts = (bData1 >> 2) & 0x03;
		mCarbodyInfo.BCM_MirrorFoldEnableSts = (bData1 >> 4) & 0x03;
		mCarbodyInfo.BCM_AutoLockEnableSts = (bData1 >> 6) & 0x03;

		mCarbodyInfo.BCM_FollowMeSts = bData2 & 0x03;
		mCarbodyInfo.BCM_AntiTheftModeSts = (bData2 >> 2) & 0x03;
		mCarbodyInfo.BCM_SunroofAutoCloseEnableSts = (bData2 >> 4) & 0x03;

		mCarbodyInfo.BCM_AmbientLightBrightnessSts = (bData3 & 0xFF) * 5;
		mCarbodyInfo.BCM_RearDfstSts = bData4 & 0xFF;

		return mCarbodyInfo;
	}

	// 解析空调数据
	public CarHelpAirInfo parseCarHelpAirInfo(byte[] packet, CarHelpAirInfo mCarHelpAirInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];
		byte bData6 = packet[6];

		mCarHelpAirInfo.ACCM_AutoCleanEnableSts = bData1 & 0xFF;// 空调自动清洁功能使能状态
		mCarHelpAirInfo.ACCM_AutoBlowEnableSts = bData2 & 0x03;// 空调自通风功能使能状态
		mCarHelpAirInfo.IHU_ACPMemoryMode = (bData2 >> 2) & 0x03;// 空调自通风功能使能状态
		mCarHelpAirInfo.ACCM_AutoCleanActiveSts = bData3 & 0xFF;// 空调自动清洁激活状态
		mCarHelpAirInfo.ACCM_AutoBlowActiveSts = bData4 & 0xFF;// 空调自通风激活状态
		mCarHelpAirInfo.ACCM_AutoAdjustCtrlSource = bData5 & 0xFF;// 空调AUTO调节控制来源
		mCarHelpAirInfo.ACCM_InternalTemp = (int) (-40 + (bData6 & 0xFF) * 0.5);// 室内温度

		Log.i(TAG,
				"mCarHelpAirInfo.ACCM_AutoCleanEnableSts: " + mCarHelpAirInfo.ACCM_AutoCleanEnableSts
						+ "mCarHelpAirInfo.ACCM_AutoBlowEnableSts: " + mCarHelpAirInfo.ACCM_AutoBlowEnableSts
						+ "mCarHelpAirInfo.IHU_ACPMemoryMode: " + mCarHelpAirInfo.IHU_ACPMemoryMode
						+ "mCarHelpAirInfo.ACCM_AutoCleanActiveSts: " + mCarHelpAirInfo.ACCM_AutoCleanActiveSts
						+ "mCarHelpAirInfo.ACCM_AutoBlowActiveSts: " + mCarHelpAirInfo.ACCM_AutoBlowActiveSts
						+ "mCarHelpAirInfo.ACCM_AutoAdjustCtrlSource: " + mCarHelpAirInfo.ACCM_AutoAdjustCtrlSource
						+ "mCarHelpAirInfo.ACCM_InternalTemp: " + mCarHelpAirInfo.ACCM_InternalTemp);

		return mCarHelpAirInfo;
	}

	// 解析动力数据
	public PowerInfo parsePowerInfo(byte[] packet, PowerInfo mPowerInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];
		byte bData6 = packet[6];
		byte bData7 = packet[7];
		byte bData8 = packet[8];
		byte bData9 = packet[9];
		byte bData10 = packet[10];
		byte bData11 = packet[11];

		mPowerInfo.VCU_cESC_EnableSts = bData1 & 0xFF;// 驱动防滑功能使能状态
		mPowerInfo.VCU_eHAC_EnableSts = bData2 & 0xFF;// 坡道起步功能使能状态
		mPowerInfo.VCU_eHDC_EnableSts = bData3 & 0xFF;// 陡坡缓降功能使能状态
		mPowerInfo.VCU_eAVH_EnableSts = bData4 & 0xFF;// AVH使能状态
		mPowerInfo.VCU_eAVH_TimeSetSts = bData5 & 0xFF;// AVH时间设置状态
		mPowerInfo.VCU_eAVH_ReverseDisableSts = bData6 & 0xFF;// AVH倒车禁用使能状态
		mPowerInfo.VCU_LongRangeModeEnableSts = bData7 & 0xFF;// 长航模式功能使能状态
		mPowerInfo.VCU_MaxSpdLimitEnableSts = bData8 & 0xFF;// 车速限制功能使能状态
		mPowerInfo.VCU_SpeedLimitValueSetSts = bData9 & 0xFF;// 车速限制值状态
		mPowerInfo.VCU_MaxRegenerationLevelEnableSts = bData10 & 0xFF;// 最高等级能量回收使能状态
		mPowerInfo.VCU_RegenerationLevelSts = bData11 & 0xFF;// 能量回收状态

		return mPowerInfo;
	}

	// 解析空调数据
	public AirInfo parseAirInfo(byte[] packet, AirInfo mAirInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];
		byte bData5 = packet[5];
		byte bData6 = packet[6];

		mAirInfo.ACP_AUTO_SwSts = bData1 & 0x01;// AUTO开关状态
		mAirInfo.ACP_AC_SwSts = (bData1 >> 1) & 0x01;// AC开关状态
		mAirInfo.ACP_PTC_SwSts = (bData1 >> 2) & 0x01;// PTC开关状态
		mAirInfo.ACP_OFF_SwSts = (bData1 >> 3) & 0x01;// OFF开关状态
		mAirInfo.ACP_RearDfstSwSts = (bData1 >> 4) & 0x01;// 后除霜开关状态
		mAirInfo.ACP_CirculationModeSwSts = (bData1 >> 5) & 0x01;// 内外循环开关状态
		// mAirInfo.ACP_AQS_SwSts = (bData1 >> 6) & 0x01;// AQS开关状态
		mAirInfo.ACP_BlowModeSet = bData2 & 0xFF;// 吹风模式Mode设置
		mAirInfo.ACP_TempSet = bData3 & 0xFF;// 温度设置
		mAirInfo.ACP_BlowerLeverSet = bData4 & 0xFF;// 风机档位(默认值为记忆的上次档位值)
		mAirInfo.ACP_IHU_Volume = bData5 & 0xFF;// DVD音量调节
		mAirInfo.ACP_IHU_TrackListSelect = bData6 & 0xFF;// DVD上下曲目选择

		return mAirInfo;

	}

	// 解析提示框数据
	public TipsInfo parseTipsInfo(byte[] packet, TipsInfo mTipsInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];

		mTipsInfo.tipContent = bData1 & 0xFF;
		mTipsInfo.tipShowTime = bData2 & 0xFF;

		return mTipsInfo;

	}

	// 解析VCU状态显示
	public VcuInfo parseVcuInfo(byte[] packet, VcuInfo mVcuInfo) {

		byte bData1 = packet[1];
		byte bData2 = packet[2];
		byte bData3 = packet[3];
		byte bData4 = packet[4];

		mVcuInfo.VCU_GearboxPositionDisp = bData1 & 0x0F;
		mVcuInfo.VCU_DriveMode = (bData1 >> 4) & 0x07;
		mVcuInfo.VCU_ExternalTempDisp = (float) (-40 + (bData2 & 0xFF) * 0.5);
		mVcuInfo.VCU_SOC_Disp = (int) (((bData3 & 0xFF) * 256 + (bData4 & 0xFF)) * 0.01);

		return mVcuInfo;
	}

	// 解析仪表在线配置1
	public DashboardConfigInfo1 parseDashboardConfigInfo1(byte[] packet, DashboardConfigInfo1 mDashboardConfigInfo1) {

		byte bData1 = packet[1];

		mDashboardConfigInfo1.ICM_DRL_ConfigurationSts = bData1 & 0x03;// 装饰灯状态
		mDashboardConfigInfo1.ICM_AmbientLight_ConfigurationSts = (bData1 >> 2) & 0x03;// 氛围灯状态
		mDashboardConfigInfo1.ICM_MirrorAutoFoldConfigurationSts = (bData1 >> 4) & 0x03;// 设防后视镜折叠配置状态

		return mDashboardConfigInfo1;
	}

	public static String byteToString(byte[] data) {
		StringBuffer str = new StringBuffer();
		for (byte d : data) {
			str.append(String.format("%02X ", d));
		}
		return str.toString();
	}

}
