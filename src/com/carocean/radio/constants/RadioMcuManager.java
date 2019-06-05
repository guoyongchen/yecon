package com.carocean.radio.constants;

import android.mcu.McuListener;
import android.mcu.McuManager;
import android.os.RemoteException;
import static android.mcu.McuExternalConstant.*;

import com.carocean.vmedia.radio.PageRadio;

public class RadioMcuManager {

	private McuManager mMcuManager;

	public RadioMcuManager(McuManager mcuManager) {
		mMcuManager = mcuManager;
	}

	public void onOpenRadioSource() {
		try {
			mMcuManager.RPC_SetSource(MCU_SOURCE_RADIO, 0x00);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onCloseRadioSource() {
		try {
			mMcuManager.RPC_SetSource(MCU_SOURCE_OFF, 0x00);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onRequestListener(McuListener mcuListener) {
		try {
			mMcuManager.RPC_RequestMcuInfoChangedListener(mcuListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onReleaseListener(McuListener mcuListener) {
		try {
			mMcuManager.RPC_RemoveMcuInfoChangedListener(mcuListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onSeekUp() {
		try {
			mMcuManager.RPC_KeyCommand(T_RADIO_SEEK_UP, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onSeekDown() {
		try {
			mMcuManager.RPC_KeyCommand(T_RADIO_SEEK_DOWN, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onAS() {
		try {
			mMcuManager.RPC_KeyCommand(T_RADIO_AS, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void sendBandFreqInfo(int band, int freq) {
		try {
			byte[] para = new byte[3];
			if (PageRadio.isSlide) {
				para[0] = (byte) ((byte)1<<4|band);
				PageRadio.isSlide = false;
			} else {
				para[0] = (byte) band;
			}
			para[1] = (byte) ((freq >> 8) & 0xFF);
			para[2] = (byte) (freq & 0xFF);
			mMcuManager.RPC_SendRadioFreqInfo(para, 3);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setBand(byte[] bandData) {
		try {
			if (null != mMcuManager && null != bandData) {
				mMcuManager.RPC_SendExtendCmd(0xE8, bandData, bandData.length);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
