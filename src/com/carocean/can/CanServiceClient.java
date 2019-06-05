package com.carocean.can;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.mcu.McuBaseInfo;
import android.mcu.McuExternalConstant;
import android.mcu.McuListener;
import android.mcu.McuManager;
import android.util.Log;

public class CanServiceClient{
	private String TAG = "CanServiceClient";
	McuManager mMcuMananger = null;
	private byte mBuffer[] = new byte[1024];
	private byte mSendBuffer[] = new byte[13];
	private final int GW_ATC_512h = 0x5102;
	private final int GW_ATC_514h = 0x5104;
	private final int GW_ATC_510h = 0x5100;
	private final int GW_ATC_3F1h = 0x3F01;
	private final int GW_IPK_6F1h = 0x6F01;
	private final int GW_ADAS_293h = 0x2903;
	private final int GW_PDC_28Ah = 0x280A;
	private final int GW_FICM_560h = 0x5600;
	private final int GW_FICM_562h = 0x5602;
	private final int GW_BCM_581h = 0x5801;
	private final int GW_BCM_379h = 0x3709;
	private final int GW_BCM_TPMS_5E0h = 0x5E00;
	private static CanServiceClient mCanServiceClient = null;
	private byte Test_5102[] = {0x00,0x00,0x51,0x02,0x08,0x00,0x00,0x00,0x00,0x55,0x00,0x00,0x00};
	//00001 11 1 01100100 01100100 01 1 1 1 110 01000000 00100000 00011111 00011110 
    //    1  3 1 100      100      1  1 1 1   6  1         1          31         30
	private byte Test_5104[] = {0x00,0x00,0x51,0x04,0x08,0x0F,0x64,0x64,0x7E,0x40,0x20,0x1F,0x1E};
	// 00000000 01110000 01111111 01111101 000000000 000000001 00011110 0100 1000
    //                                                             30    4   8
	private byte Test_510[] = {0x00,0x00,0x51,0x00,0x08,0x00,0x70,-127,0x7D,0x00,0x01,0x1E,0x38};
	private byte Test_3f1[] = {0x00,0x00,0x3f,0x01,0x08,0x32,0x09,0x00,0x00,0x00,0x00,0x00,0x00};
	private byte Test_6f1[] = {0x00,0x00,0x6f,0x01,0x08,0x00,0x00,0x00,0x00,0x6E,-127,0x00,0x00};
	private byte Test_293[] = {0x00,0x00,0x29,0x03,0x08,0x00,0x00,0x00,0x04,0x24,-127,0x00,0x00};
	private byte Test_PDC28A[] ={0x00,0x00,0x28,0x0A,0x08,0x00,0x10,0x00,0x00,0x00,0x00,0x00,0x00};
	private byte Test_560[] ={0x00,0x00,0x56,0x00,0x08,0x00,0x10,0x00,0x00,0x00,0x00,0x00,0x30};
	private byte Test_562[] ={0x00,0x00,0x56,0x02,0x08,0x00,0x10,0x00,0x00,0x00,0x06,0x00,0x00};
	private byte Test_581[] ={0x00,0x00,0x58,0x01,0x08,0x40,0x10,0x00,0x00,0x00,0x08,0x50,0x00};
	private int mFLSeatHeatLvl = 0;//24bit
	private int mFRSeatHeatLvl = 0;//26bit
	
	private int mRLSeatHeatLvl = 0;//28bit 3: High,2: Mid,1: Low,0: Off
	private int mRRSeatHeatLvl = 0;//30bit
	private int mAirClnrOnOffDspCmd = 0;//1: On,0: Off
	private int mInsdAirPrtclMtrCDC = 0;//FF: error,FE: reserved,FD: Invalid,FC: reminder on,FB: reminder off
	private int mOtsdAirTemCrVal = 0;//1: Invalid,0: Valid
	private int mInsdAirTemCrVal = 0;
	private int mHVACPowerStatus = 0;//1: ON,0: OFF
	private int mHVACAutoIndicationStatus = 0;//1: ON,0: OFF
	private int mHVACOnRequestIndication = 0;//1: Indicator On,0: Indicator Off
	private int mHVACCycleMode = 0;//3: 3£ºInvalid,2: Auto Recirculation,1: ExteriorCirculation,0: InteriorCirculation
	private int mHVACMode = 0;//F: Invalid,E: Reserved,D: Reserved,C: Reserved,B: Reserved,A: Reserved,9: Reserved
	                          //8: Reserved,7: Reserved,6: auto air distribute mode,5: Defrost,4: Foot&Defrost,3: Foot
	                          //2: Face&Foot,1: Face,0: Default
	private int mHVACAirVolume = 0;//9: auto blower level,8: 8,7: 7,6: 6,5: 5,4: 4,3: 3,2: 2,1: 1,0: 0,F: Invalid,A-E: Reserved
	private int mHVACSetTemperature = 0;
	
	private OnAirTemChangeListener mOnAirTemChangeListener;
	private OnSeatHeatLvlChangeListener mOnSeatHeatLvlChangeListener;
	private OnAirClnrStatusChangeListener mOnAirClnrStatusChangeListener;
	private OnConditionerStatusChangeListener mOnConditionerStatusChangeListener;
	
	public static CanServiceClient getInstance(Context context) {
		if(null == mCanServiceClient) {
			if(null != context) {
				mCanServiceClient = new CanServiceClient(context); 
			}
		}
		
		return mCanServiceClient;
	}
	private CanServiceClient(Context context) {
		mCanServiceClient = this;
		McuManager mMcuMananger = (McuManager) context.getSystemService(Context.MCU_SERVICE);
		try {
			mMcuMananger.RPC_RemoveMcuInfoChangedListener(mMcuListener);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		
		OnCanRxData(Test_5102, Test_5102.length);
		OnCanRxData(Test_5104, Test_5104.length);
		OnCanRxData(Test_510, Test_510.length);
		OnCanRxData(Test_3f1, Test_3f1.length);
		OnCanRxData(Test_6f1, Test_6f1.length);
		OnCanRxData(Test_293, Test_293.length);
		OnCanRxData(Test_PDC28A, Test_PDC28A.length);
		OnCanRxData(Test_560, Test_560.length);
		OnCanRxData(Test_562, Test_562.length);
		OnCanRxData(Test_581, Test_581.length);
	}
	private McuListener mMcuListener = new McuListener() {
		@Override
		public void onMcuInfoChanged(McuBaseInfo arg0, int arg1) {
			if (arg0 != null) {
				if (arg1 == McuExternalConstant.MCU_CANBUS_INFO_TYPE) {
					byte[] datas = arg0.getOriginalInfo().getMcuData();
					int iLength = arg0.getOriginalInfo().getMcuData().length;
					OnCanRxData(datas, iLength);
				}
			}
		}
	};
	public void OnCanRxData(byte[] pack, int arg2) {
		Log.v(TAG, "OnCanRxData.pack:"+new String(pack));
		byte2Binary(pack, pack.length);
		int flag = pack[2] & 0xff;
		flag = (flag << 8) | pack[3] & 0xff;
		switch(flag) {
		case GW_ATC_512h:
			parse512H(pack);
			break;
		case GW_ATC_514h:
			parse514H(pack);
			break;
		case GW_ATC_510h:
			parse510H(pack);
			break;
		case GW_ATC_3F1h:
			parse3F01H(pack);
			break;
		case GW_IPK_6F1h:
			parse6F01H(pack);
			break;
		case GW_ADAS_293h:
			parseADAS293H(pack);
			break;
		case GW_PDC_28Ah:
			parsePDC28Ah(pack);
			break;
		case GW_FICM_560h:
			parseFICM560h(pack);
			break;
		case GW_FICM_562h:
			parseFICM562h(pack);
			break;
		case GW_BCM_581h:
			parseBCM581h(pack);
			break;
		case GW_BCM_TPMS_5E0h:
			parseBCMTPMS5E0h(pack);
			break;
		}
	}

	private void parse512H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[4];
		mFLSeatHeatLvl = data & 0x03;//24bit
		mFRSeatHeatLvl = (data & 0x0C) >> 2;//26bit
		
		mRLSeatHeatLvl = (data & 0x30) >> 4;//28bit
		mRRSeatHeatLvl = (data & 0xC0) >> 6;//30bit
		
		int FrontLftSeatVentStatus = mBuffer[5] & 0x00000011;
		int FrontRgtSeatVentStatus = mBuffer[5] & 0x00001100;
		int RearLftSeatVentStatus = mBuffer[5] &  0x00110000;
		int RearRgtSeatVentStatus = mBuffer[5] &  0x11000000;
		
		if(null != mOnSeatHeatLvlChangeListener) {
			mOnSeatHeatLvlChangeListener.OnSeatHeatLvlChange(mFLSeatHeatLvl, mFRSeatHeatLvl, mRLSeatHeatLvl, mRRSeatHeatLvl);
		}

	}
	private int mAirClnrUserCustAutoWDC = 0;//3: Function disable,2: Reserved,1: on,0: off
	private void parse514H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 1];
		int HVACInCarTermperature = data & 0xff;//0-7bit
		data = mBuffer[len - 2];
		int OtsdAirPrtclMtrCDC = (data & 0xff);//8-15bit
		data = mBuffer[len - 3];
		int HVACDisplayedInformationMask  = (data & 0x20) >> 5;//21bit
		data = mBuffer[len - 4];
		int HVACAutoDspCmd = (data & 0x40) >> 6;//30bit
		data = mBuffer[len - 5];
		int AirClnrBlwrLvlDspCmd = (data & 0x7);//32-34bit
		int AirClnrBlwrSpdSwA = (data & 0x8) >> 3;//35bit
		mAirClnrOnOffDspCmd = (data & 0x10) >> 4;//36bit
		int AirClnrAutoDspCmd = (data & 0x20) >> 5;//37bit
		mAirClnrUserCustAutoWDC  = (data & 0xC0) >> 6;//38-39bit
		
		data = mBuffer[len - 6];
		int AirClnrFltrLife = data & 0xff;//40-47
		mInsdAirPrtclMtrCDC = mBuffer[len - 7] & 0xff;//48-55bit
		
		int AirClnrFltrLifeValidity = (mBuffer[len - 8] & 0x1);//56bit
		int AirClnrFltrCsumLvlDspC = (mBuffer[len - 8] & 0x3) >> 1;//57-58bit
		int AirClnrIonizerDspCmd = (mBuffer[len - 8] & 0x8) >> 3;//59bit
		processAirClnrStatusChange();
	}

	private void processAirClnrStatusChange() {
		for (WeakReference<OnAirClnrStatusChangeListener> callback : mOnAirClnrStatusChangeListenerList) {
			final OnAirClnrStatusChangeListener oldCallbacks = callback.get();
			if (null != oldCallbacks) {
				oldCallbacks.OnAirClnrStatusChange(mAirClnrOnOffDspCmd, mInsdAirPrtclMtrCDC, mAirClnrUserCustAutoWDC);
			}
		}
	}
	private void parse510H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 1];
		
		mHVACAirVolume = data & 0x0f;//0-3bit
		mHVACMode      = (data & 0xf0) >> 4;//4-7bit
		
		mHVACSetTemperature = mBuffer[len - 2];//8-15bit
		
		int RearWindowHeatingIndicator = mBuffer[len - 3] & 0x01;//16bit
		
		int EvaporatorTemperature = mBuffer[len - 4];//24-31bit	
		
		mHVACCycleMode = (mBuffer[len - 5] & 0x03);//32-33bit
		int HVACOnRequest = (mBuffer[len - 5] & 0x04) >> 2;//34bit
		int HVACOnRequestValidity = (mBuffer[len - 5] & 0x08) >> 3;//35bit
		int HVACFrontDefrostStatus = (mBuffer[len - 5] & 0x10) >> 4;//36bit
		int HVACFrontDefrostStatusValidity = (mBuffer[len - 5] & 0x20) >> 5;//37bit
		int HVACRearDefrostStatus = (mBuffer[len - 5] & 0x40) >> 5;//38bit
		int HVACRearDefrostStatusValidity = (mBuffer[len - 5] & 0x80) >> 6;//39bit
		
		mHVACAutoIndicationStatus = (mBuffer[len - 6] & 0x01);//40bit
		int HVACAutoIndicationStatusValidity = (mBuffer[len - 6] & 0x02) >> 1;//41bit
		int HVACAirClearingIndicationStatus = (mBuffer[len - 6] & 0x04) >> 2;//42bit
		int HVACAirClearingIndicationStatusValidity = (mBuffer[len - 6] & 0x08) >> 3;//43bit
		int HVACEnvirmentTemperatureMask = (mBuffer[len - 6] & 0x10) >> 4;//44bit
		mHVACPowerStatus = (mBuffer[len - 5] & 0x20) >> 5;//45bit
		int HVACPressureValidity = (mBuffer[len - 6] & 0x40) >> 6;//46bit
		mHVACOnRequestIndication = (mBuffer[len - 6] & 0x80) >> 7;//47bit
		int i = mBuffer[len - 6] & 0xff;
		int f  = (i & 0x80) >> 7;
		int HVACAirVolumeValidity = (mBuffer[len - 7] & 0x10) >> 4;//52bit
		int HVACModeValidity = (mBuffer[len - 7] & 0x20) >> 5;//53bit
		int HVACSetTemperatureValidity = (mBuffer[len - 7] & 0x40) >> 6;//54bit
		int HVACCycleModeValidity = (mBuffer[len - 7] & 0x80) >> 7;//55bit
		
		if(null != mOnConditionerStatusChangeListener) {
			mOnConditionerStatusChangeListener.OnACStatusChange(mHVACPowerStatus, mHVACAutoIndicationStatus,
					mHVACOnRequestIndication, mHVACCycleMode, mHVACMode, mHVACAirVolume, mHVACSetTemperature);
		}
	}

	private void parse3F01H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 7];
		
		int OtsdAirTemCrValV = data & 0x01;//48bit
		int OtsdAirTemCrValMsk = (data & 0x08) >> 3;//51bit
		data = mBuffer[len - 8];
		mOtsdAirTemCrVal = (data & 0xff);//56-63bit
		
		if(null != mOnAirTemChangeListener) {
			mOnAirTemChangeListener.OnAirTemChange(mInsdAirTemCrVal, mOtsdAirTemCrVal);
		}
	}
	private int mDASTSRMainSwitchFeedback = 0;//1: ON,0: OFF
	private int mDASFCMMainSwitchFeedback = 0;//3: FCM+AEB,2: FCM,1: off,0: No Request
	private int mDASFCMMainSensitivityFeedback = 0;//3: High,2: Standard,1: Low,0: No Request
	private int mDASLKAMainSwitchFeedback = 0;//1: UnAvailable,0: Available
	private int mDASLKAMainSensitivityFeedback = 0;
	private int mDASLKASwitchAvailableFeedback = 0;//32bit
	private void parseADAS293H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 3];
		
		mDASTSRMainSwitchFeedback = (data & 0x80) >> 7;//23bit Speed limit identification mark
		
		int DASFCMSwitchAvailableFeedback = (mBuffer[len - 4] & 0x01);//24bit
		mDASFCMMainSwitchFeedback = (mBuffer[len - 4] & 0x0E) >> 1;//25-27bit Forward Collision Warning(FCWS)
		mDASFCMMainSensitivityFeedback  = (mBuffer[len - 4] & 0x70) >> 4;//28-30bit FCWS Sensitivity
		
		mDASLKASwitchAvailableFeedback = (mBuffer[len - 5] & 0x01);//32bit
		mDASLKAMainSwitchFeedback = (mBuffer[len - 5] & 0x0E) >> 1;//33 - 35bit Lane-Keeping Assist
		
		int SpdAssistSystemFeedback = (mBuffer[len - 6] & 0x07);//40 - 42bit
		
		if(null != mOnAdasStatusChangeListener) {
			mOnAdasStatusChangeListener.onAdasStatusChange(mDASTSRMainSwitchFeedback, mDASFCMMainSwitchFeedback,
					                                       mDASFCMMainSensitivityFeedback, mDASLKASwitchAvailableFeedback,mDASLKAMainSwitchFeedback);
		}
	}
	private int mFrontPDCEnableStatus = 0;//0: OFF,1: ON
	private void parseFICM560h(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 1];
		mFICMDDDFunctionStatus = (data & 0x60) >> 5;//5-6bit
		OnFICMStatusChange(mFICMDDDFunctionStatus,mBSDSwitchStatusFeedback);
	}
	private int mAutoFollowMeHomeOption = 0;//0:Disabled,1:Enabled,2:Reserved,3:not Available
	private int mFollowMeHomeDuration = 0;//0:0s,1:30s,2:60,3:90
	private int mMirrorAutoFoldOption = 0;//0:Disabled,1:Enabled,2:Reserved,3:all mirror auto unfold related feature not Available
	private int mFindMyCarFeedbackOptions = 0;//0:Lights Only,1:Horn And Lights On
	private int mAutoUnlockingOption = 0;//0:disabled,1:enabled,2:Reserved,3:not Available
	private int mAutomaticLockOption = 0;//0:disabled,1:enabled,2:Reserved,3:not Available
	private void parseBCM581h(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 2];
		mAutoFollowMeHomeOption = (data & 0x30) >> 4;//12-13bit
		mFollowMeHomeDuration = (data & 0xC0) >> 6;//14-15bit
		
		int RemoteLockingFeedbackOptionsMenuOption = (mBuffer[len - 3] & 0x01);//16bit
		int WelcomeLightOptionsMenuOption = (mBuffer[len - 3] & 0x02) >> 1;//17bit
		mFindMyCarFeedbackOptions = (mBuffer[len - 3] & 0x08) >> 3;//19bit
		int FindMyCarDuration = (mBuffer[len - 3] & 0x30) >> 4;//20-21bit
		int WelcomeLightDuration = (mBuffer[len - 3] & 0xC0) >> 6;//22-23bit
		
		mAutoUnlockingOption = (mBuffer[len - 7] & 0x03);//48-49bit
		mAutomaticLockOption = (mBuffer[len - 7] & 0x30) >> 4;//52-53bit
		
		mMirrorAutoFoldOption = (mBuffer[len - 8] & 0xC0) >> 6;;//62-63bit
		processBCMStatusChange();
	}
	private void processBCMStatusChange() {
		if(null != mOnBCMStatusChangeListener) {
			mOnBCMStatusChangeListener.onBCMFCarStatusChange(mFindMyCarFeedbackOptions);
			mOnBCMStatusChangeListener.onBCMFMHStatusChange(mAutoFollowMeHomeOption, mFollowMeHomeDuration);
			mOnBCMStatusChangeListener.onBCMMirrorAutoFoldOptionChange(mMirrorAutoFoldOption);
		}
	}
	private OnBCMStatusChangeListener mOnBCMStatusChangeListener;
	public void setOnBCMStatusChangeListener(OnBCMStatusChangeListener l) {
		mOnBCMStatusChangeListener = l;
		processBCMStatusChange();
	}
	public interface OnBCMStatusChangeListener{
		void onBCMFMHStatusChange(int home, int homeTime);
		void onBCMFCarStatusChange(int fdCar);
		void onBCMMirrorAutoFoldOptionChange(int option);
	}
	private int mBSDSwitchStatusFeedback = 0;//0 not support,1 off 2 --on
	private void parseFICM562h(byte[] pack) {
		/*int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 3];
		
		mBSDSwitchStatus = (data & 0x06) >> 1;//17-18bit
		OnFICMStatusChange(mFICMDDDFunctionStatus,mBSDSwitchStatus);*/
	}
	private int mLFPressure = 0;//0-7bit
	private int mRFPressure = 0;//8-15bit
	private int mRRPressure = 0;//16-23
	private int mLRPressure = 0;//24-31bit
	private int mTireTemperature = 0;//32-39bit
	private int mLFPressureWarning = 0;//40-42bit,7: Low pressure warning&Quik leakage,6: Sensor failure
	                                   //5: Sensor battery low,4: Lost Sensor,3: Quik leakage,2: Low pressure warning
	                                   //1: High pressure warning,0: No warning
	private int mRFPressureWarning = 0;//43-45bit
	private int mRRPressureWarning = 0;
	private int mLRPressureWarning = 0;
	private int mTemperatureWarning = 0;
	private void parseBCMTPMS5E0h(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		mLFPressure = mBuffer[len - 1];//0-7bit
		mRFPressure = mBuffer[len - 2];//8-15bit
		mRRPressure = mBuffer[len - 3];//16-23bit
		mLRPressure = mBuffer[len - 4];//24-31bit
		mTireTemperature = mBuffer[len - 5];//32-39bit
		
		mLFPressureWarning = mBuffer[len - 6] & 0x07;//40-42bit
		mRFPressureWarning = (mBuffer[len - 6] & 0x38) >> 3;//43-45bit
		int TPMSSystemStatus = (mBuffer[len - 6] & 0xC0) >> 6;//46-47bit
		
		mRRPressureWarning = mBuffer[len - 7] & 0x07;//48-50bit
		mLRPressureWarning = (mBuffer[len - 7] & 0x38) >> 3;//51-53bit
		mTemperatureWarning = (mBuffer[len - 7] & 0x40) >> 6;//54bit
		int SignalStatus = (mBuffer[len - 7] & 0x80) >> 7;//55bit
		
		int TirePosition = mBuffer[len - 8] & 0x07;//56-58bit,7-5: reseved,4: Left Rear tire,3: Right Rear tire,2: Right Front tire,1: Left Front tire,0: No any sensor
		processTPMSStatusChange();
	}
	private void processTPMSStatusChange() {
		if(null != mOnTPMSStatusChangeListener) {
			mOnTPMSStatusChangeListener.OnTPMSPressureChange(mLFPressure, mRFPressure, mLRPressure, mRRPressure);
			mOnTPMSStatusChangeListener.OnTPMSPressureWarning(mLFPressureWarning, mRFPressureWarning, mLRPressureWarning, mRRPressureWarning);
		}
	}
	private OnTPMSStatusChangeListener mOnTPMSStatusChangeListener;
	public void setOnTPMSStatusChangeListener(OnTPMSStatusChangeListener l) {
		mOnTPMSStatusChangeListener = l;
		processTPMSStatusChange();
	}
	public interface OnTPMSStatusChangeListener{
		void OnTPMSPressureChange(int lf, int rf, int lr, int rr);
		void OnTPMSTemperatureChange(int lf, int rf, int lr, int rr);
		void OnTPMSPressureWarning(int lf, int rf, int lr, int rr);
		void OnTPMSTemperatureWarning(int lf, int rf, int lr, int rr);
	}
	private OnFICMStatusChangeListener mOnFICMStatusChangeListener;
	public void setOnFICMStatusChangeListener(OnFICMStatusChangeListener l) {
		mOnFICMStatusChangeListener = l;
		OnFICMStatusChange(mFICMDDDFunctionStatus,mBSDSwitchStatusFeedback);
	}
	private void OnFICMStatusChange(int dddStatus, int bsdStatus) {
		if(null != mOnFICMStatusChangeListener) {
			mOnFICMStatusChangeListener.OnFICMStatusChange(dddStatus, bsdStatus);
		}
	}
	public interface OnFICMStatusChangeListener{
		void OnFICMStatusChange(int dddStatus, int bsdStatus);
	}
	private int mFICMDDDFunctionStatus = 0;//0 not support,1 off 2 --on
	private void parsePDC28Ah(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 7];
		
		mFrontPDCEnableStatus = (data & 0x10) >> 4;//52bit
		processPDCStatusChange(mFrontPDCEnableStatus);
	}
	private OnPDCStatusChangeListener mOnPDCStatusChangeListener;
	public void setOnPDCStatusChangeListener(OnPDCStatusChangeListener l) {
		mOnPDCStatusChangeListener = l;
		
		processPDCStatusChange(mFrontPDCEnableStatus);
	}
	private void processPDCStatusChange(int status) {
		if(null != mOnPDCStatusChangeListener) {
			mOnPDCStatusChangeListener.onPDCStatusChange(status);
		}
	}
	public interface OnPDCStatusChangeListener{
		void onPDCStatusChange(int status);
	}
	private OnAdasStatusChangeListener mOnAdasStatusChangeListener;
	public void setOnAdasStatusChangeListener(OnAdasStatusChangeListener l) {
		mOnAdasStatusChangeListener = l;
		
		if(null != mOnAdasStatusChangeListener) {
			mOnAdasStatusChangeListener.onAdasStatusChange(mDASTSRMainSwitchFeedback, mDASFCMMainSwitchFeedback,
					                                       mDASFCMMainSensitivityFeedback, mDASLKASwitchAvailableFeedback, mDASLKAMainSwitchFeedback);
		}
	}
	public interface OnAdasStatusChangeListener{
		void onAdasStatusChange(int stsr, int fcws, int fcwss, int lkaEnable, int lka);
	}
	private void parse6F01H(byte[] pack) {
		int len = pack[4];
		System.arraycopy(pack, 5, mBuffer, 0, len);
		byte data = mBuffer[len - 1];
		
		int IPKDispdFuelWrnng = (data & 0x10) >> 4;//4bit
		int IPKDispedEMSWrnng = (data & 0x20) >> 5;//51bit
		int IPKDispHDCWrnng = (data & 0x80) >> 7;//51bit
		
		int IPKDispdEPBWrnng = (mBuffer[len - 2] & 0x01);//8bit
		int IPKDispdAutoholdWrnng = (mBuffer[len - 2] & 0x02) >> 1;//9bit
		int IPKDispdEPSWrnng = (mBuffer[len - 2] & 0x08) >> 3;//11bit
		int IPKDispdOilPrsLowWrnng = (mBuffer[len - 2] & 0x20) >> 5;//13bit
		int IPKDispdClntTempWrnng = (mBuffer[len - 2] & 0x80) >> 7;//15bit
		
		int IPKDispdSctKyBatLowWrnng = (mBuffer[len - 3] & 0x04) >> 2;//18bit
		int IPKDispTPMSWrnng = (mBuffer[len - 3] & 0x10) >> 4;//20bit
		int IPKDispDASWrnng = (mBuffer[len - 3] & 0x20) >> 5;//21bit
		int IPKDispAPAWrnng = (mBuffer[len - 3] & 0x40) >> 6;//22bit
		mIPKOverSpdFnSts = (mBuffer[len - 3] & 0x80) >> 7;//23bit
		
		mPKOverSpdThreshholdVal = mBuffer[len - 4] & 0xff;//24-31bit
		
		int IPKLanguageFeadback = mBuffer[len - 5] & 0x7f;
		data = mBuffer[len - 8];
		mOtsdAirTemCrVal = (data & 0xff);//56-63bit
		
		if(null != mOnOverSpeedStatusChangeListener) {
			mOnOverSpeedStatusChangeListener.onOverSpeedStatusChange(mIPKOverSpdFnSts, mPKOverSpdThreshholdVal);
		}
	}

	private void resetSendBuffer() {
		for(int i = 0; i < mSendBuffer.length; i ++) {
			mSendBuffer[i] = 0;
		}
	}
	private String byte2Binary(byte data[], int len) {
		String ret = "";
		
		for(int i = 0; i < len; i ++) {
			byte tByte = data[i];  
			String tString = Integer.toBinaryString((tByte & 0xFF) + 0x100).substring(1);
			ret += tString;
			ret += " ";
		}
		Log.v(TAG, "byte2Binary:"+ret);
		return ret;
	}
	public void sendCollisionWarningParam(byte fcm, byte fcmMode, int fcmSensitivity, byte frontPdc) {
		resetSendBuffer();
		mSendBuffer[0] = 0;
		mSendBuffer[1] = 0;	
		mSendBuffer[2] = 0x56;
		mSendBuffer[3] = 0x02;
		mSendBuffer[4] = 0x08;
		
		mSendBuffer[10] |= (0x00);//16bit
		mSendBuffer[10] |= (0x00);//BSDSwitchStatus 17-18bit
		mSendBuffer[10] |= (0x00);//FICMFrontPDCEnableRequestV 19bit
		
		mSendBuffer[11] |= (mDASLKASwitchAvailableFeedback & 0x01);//8bit
		mSendBuffer[11] |= (mDASLKAMainSwitchFeedback & 0x07) << 1;//9-11bit
		mSendBuffer[11] |= (mDASLKAMainSensitivityFeedback & 0x07) << 4;//12-14bit
		mSendBuffer[11] |= (0x00 & 0x01) << 7;//FICMFrontPDCEnbReqV 15bit
		
		mSendBuffer[12] |= (fcm & 0x01);//0bit
		mSendBuffer[12] |= (fcmMode & 0x07) << 1;//1-3bit
		mSendBuffer[12] |= (fcmSensitivity & 0x07) << 4;//4-6bit
		mSendBuffer[12] |= (frontPdc & 0x01) << 7;//7bit
		byte2Binary(mSendBuffer, mSendBuffer.length);
		sendPackage(mSendBuffer);
	}
	private void sendPackage(byte pack[]) {
		try {
			mMcuMananger.RPC_SendCANInfo(pack, pack.length);
		}catch(Exception e) {
			Log.v(TAG, e.toString());
		}
	}
	private OnOverSpeedStatusChangeListener mOnOverSpeedStatusChangeListener;
	private int mIPKOverSpdFnSts = 0;//0: OFF,1: ON
	private int mPKOverSpdThreshholdVal = 120;
	public void setOnOverSpeedStatusChangeListener(OnOverSpeedStatusChangeListener l) {
		mOnOverSpeedStatusChangeListener = l;
		
		if(null != mOnOverSpeedStatusChangeListener) {
			mOnOverSpeedStatusChangeListener.onOverSpeedStatusChange(mIPKOverSpdFnSts, mPKOverSpdThreshholdVal);
		}
	}
	public interface OnOverSpeedStatusChangeListener{
		void onOverSpeedStatusChange(int status, int speed);
	}
	public void setOnAirTemChangeListener(OnAirTemChangeListener l) {
		mOnAirTemChangeListener = l;
	}
	public interface OnAirTemChangeListener{
		void OnAirTemChange(int istdTem, int otsdTem);
	}
	
	public void setOnSeatHeatLvlChangeListener(OnSeatHeatLvlChangeListener l) {
		mOnSeatHeatLvlChangeListener = l;
		
		if(null != mOnSeatHeatLvlChangeListener) {
			mOnSeatHeatLvlChangeListener.OnSeatHeatLvlChange(mFLSeatHeatLvl, mFRSeatHeatLvl, mRLSeatHeatLvl, mRRSeatHeatLvl);
		}
	}
	public interface OnSeatHeatLvlChangeListener{
		void OnSeatHeatLvlChange(int fl, int fr, int rl, int rr);
	}
	private List<WeakReference<OnAirClnrStatusChangeListener>> mOnAirClnrStatusChangeListenerList = new ArrayList<WeakReference<OnAirClnrStatusChangeListener>>();
	public void setOnAirClnrStatusChangeListener(OnAirClnrStatusChangeListener l) {
		WeakReference<OnAirClnrStatusChangeListener> callbacksTmp = new WeakReference<OnAirClnrStatusChangeListener>(
				l);

		if (!mOnAirClnrStatusChangeListenerList.contains(callbacksTmp)) {
			mOnAirClnrStatusChangeListenerList.add(callbacksTmp);
		}
		processAirClnrStatusChange();
	}
	public interface OnAirClnrStatusChangeListener{
		void OnAirClnrStatusChange(int power, int pm, int userCustAutoWDC);
	}
	
	public void setOnConditionerStatusChangeListener(OnConditionerStatusChangeListener l) {
		mOnConditionerStatusChangeListener = l;
		
		if(null != mOnConditionerStatusChangeListener) {
			mOnConditionerStatusChangeListener.OnACStatusChange(mHVACPowerStatus, mHVACAutoIndicationStatus,
					mHVACOnRequestIndication, mHVACCycleMode, mHVACMode, mHVACAirVolume, mHVACSetTemperature);
		}
	}
	public interface OnConditionerStatusChangeListener{
		void OnACStatusChange(int power, int auto, int ac, int cycleMode, int acMode, int airVolume, int temperature);
	}
}
