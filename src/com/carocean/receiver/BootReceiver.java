package com.carocean.receiver;

import static android.constant.YeconConstants.ACTION_QB_POWEROFF;
import static android.mcu.McuExternalConstant.MCU_ACTION_ACC_OFF;

import com.carocean.settings.utils.SettingConstants;
import com.carocean.settings.utils.SettingMethodManager;
import com.carocean.utils.DataShared;
import com.carocean.utils.sLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

public class BootReceiver extends BroadcastReceiver {

	private String TAG = BootReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		sLog.d("BootReceiver---intent.getAction():........." + action);
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			// ApplicationManage.initall(context);
			// timeUtils.initLocation(context);
			SystemProperties.set("persist.sys.t19_number", "T19EV");// 写入系统属性，供carplay调用
			DataShared.getInstance(context).putBoolean(SettingConstants.key_show_navi_flag, false);// 重启后，恢复低电充电桩一键导航可以弹窗
			
		} else if (action.equals(MCU_ACTION_ACC_OFF) || action.equals(ACTION_QB_POWEROFF)) {
			SettingMethodManager.getInstance(context).saveData();
		}

	}
}
