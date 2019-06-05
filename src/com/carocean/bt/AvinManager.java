package com.carocean.bt;

import com.autochips.inputsource.AVIN;
import com.autochips.inputsource.InputSource;
import com.autochips.inputsource.InputSourceClient;

import android.os.SystemProperties;
import android.util.Log;

/**
 * @ClassName AvinManager.java
 * @Description AvinManager
 * @author LIUZHIYUAN
 * @Date 2019年4月13日下午1:11:46
 */

public class AvinManager {

	private final String TAG = getClass().getSimpleName();
	private static AvinManager mInstance = null;
	private AVIN mAvinA;
	private int mAvinState2AudioFocus = InputSource.STATUS_NONE;
	private InputSourceClient.OnCbmCmdListener mOnCbmCmdListener;
	private static String PROPERTY_AVIN_TYPE = "persist.sys.avintype";

	public static AvinManager getInstance() {
		if (mInstance == null) {
			synchronized (AvinManager.class) {
				if (mInstance == null) {
					mInstance = new AvinManager();
				}
			}
		}
		return mInstance;
	}

	public AvinManager() {
		// TODO Auto-generated constructor stub

	}

	public void setOnCbmCmdListener(InputSourceClient.OnCbmCmdListener listener) {
		mOnCbmCmdListener = listener;
	}

	public void initAvin() {
		// 语音、通话过程中、power off状态，禁止切换收音机
		//if (RadioUtils.isVoiceStartup() || RadioUtils.isPhoneStartup() || RadioUtils.isPowerOff()) {
		//	return;
		//}

		if (mAvinA == null) {
			mAvinA = new AVIN();

			mAvinA.setOnCbmCmdListener(mOnCbmCmdListener);

			mAvinA.setDestination(InputSource.DEST_TYPE_FRONT);
			//调试机器是AVIN.PORT2
			int ret = mAvinA.setSource(InputSource.SOURCE_TYPE_AVIN, AVIN.PORT_NONE, AVIN.PORT4,
					AVIN.PRIORITY_IN_CBM_LEVEL_DEFAULT);

			//mAvinA.setAudioBypass(1);

			if (InputSource.ERR_FAILED == ret) {
				deinitAvin();
			} else {
				ret = mAvinA.play();
				if (InputSource.ERR_FAILED == ret) {
					deinitAvin();
				} else {
					SystemProperties.set(PROPERTY_AVIN_TYPE, "aux_type");
					// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
					// RadioDBManager.RADIO_STATUS_PLAY);
				}
			}
			Log.i(TAG,"---initAvin");
		} else {
			resumeAvin();
		}
	}

	public void deinitAvin() {
		if (mAvinA != null) {
			int ret = mAvinA.stop();
			mAvinA.release();
			mAvinA = null;
			if (InputSource.ERR_OK == ret) {
				// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
				// RadioDBManager.RADIO_STATUS_PAUSE);
			}
			Log.i(TAG,"---deinitAvin");
		}
	}

	public void resumeAvin() {
		if (mAvinA != null && mAvinA.getState() != InputSource.STATUS_STARTED) {
			int ret = mAvinA.play();
			if (InputSource.ERR_OK == ret) {
				SystemProperties.set(PROPERTY_AVIN_TYPE, "aux_type");
				// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
				// RadioDBManager.RADIO_STATUS_PLAY);
			}
			Log.i(TAG,"---resumeAvin");
		}
	}

	public void pauseAvin() {
		if (mAvinA != null && mAvinA.getState() != InputSource.STATUS_STOPED) {
			int ret = mAvinA.stop();
			if (InputSource.ERR_OK == ret) {
				// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
				// RadioDBManager.RADIO_STATUS_PAUSE);
			}
			Log.i(TAG , "---pauseAvin");
		}
	}

	/**
	 * @Method setAudioBypass
	 * @Description
	 * @param bypass
	 *            1:bypass,适用场景:蓝牙通话时,解决声音延时导致通话回声问题;
	 *            0:nobypass,适用场景:收音机播放时，内部DSP音效处理
	 * @author LIUZHIYUAN
	 * @Date 2019年4月13日
	 */
	public void setAudioBypass(int bypass) {
		if (mAvinA != null) {
			Log.i(TAG,"---setAudioBypass - bypass = " + bypass);
			//mAvinA.setAudioBypass(bypass);
		}
	}

	/**
	 * @Method getAvinState
	 * @Description
	 * @return int STATUS_NONE = 0x00 int STATUS_STOPED = 0x01 int
	 *         STATUS_STARTED = 0x02 int STATUS_STOP_FRONT = 0x03; int
	 *         STATUS_STOP_REAR = 0x04; int STATUS_RESUME_FRONT_REAR = 0x05;
	 * @author LIUZHIYUAN
	 * @Date 2019年4月13日
	 */
	public int getAvinState() {
		int ret = InputSource.STATUS_NONE;
		if (mAvinA != null)
			ret = mAvinA.getState();
		return ret;
	}

	public void pauseAvin2AudioFocus() {
		if (mAvinA != null)
			mAvinState2AudioFocus = mAvinA.getState();
		if (mAvinA != null && mAvinA.getState() != InputSource.STATUS_STOPED) {
			int ret = mAvinA.stop();
			if (InputSource.ERR_OK == ret) {
				// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
				// RadioDBManager.RADIO_STATUS_PAUSE);
			}
			Log.i(TAG ,"---pauseAvin");
		}
		Log.i(TAG, "---pauseAvin2AudioFocus");
	}

	public void resumeAvin2AudioFocus() {
		if (mAvinState2AudioFocus == InputSource.STATUS_STARTED) {
			if (mAvinA != null && mAvinA.getState() != InputSource.STATUS_STARTED) {
				int ret = mAvinA.play();
				if (InputSource.ERR_OK == ret) {
					//SystemProperties.set(PROPERTY_AVIN_TYPE, "aux_type");
					// RadioDBManager.updateRadioStatus(ApplicationManage.getContext(),
					// RadioDBManager.RADIO_STATUS_PLAY);
					Log.i(TAG,"---resumeAvin");
				}
			}
		}
		Log.i(TAG,"---resumeAvin2AudioFocus");
	}

}
