package com.carocean.backcar;

import android.mcu.McuExternalConstant;

public class BackCarConstants {

	// backcar properties
	public final static String PERSYS_BACKCAR_ENABLE = "persist.sys.backcar_enable";
	public final static String PERSYS_BACKCAR_TRACE = "persist.sys.trace_enable";
	public final static String PERSYS_BACKCAR_RADAR = "persist.sys.radar_enable";
	public final static String PERSYS_BACKCAR_MUTE = "persist.sys.backcar_mute_enable";
	public final static String PERSYS_BACKCAR_MIRROR = "persist.sys.backcar_mirror";

	public final static String PERSYS_BACKCAR_BRIGHT = "persist.sys.backcar_bright";
	public final static String PERSYS_BACKCAR_CONTRAST = "persist.sys.backcar_contrast";
	public final static String PERSYS_BACKCAR_HUE = "persist.sys.backcar_hue";
	public final static String PERSYS_BACKCAR_SATRATION = "persist.sys.backcar_saturation";

	// backcar status
	public static final int BACKCAR_STATUS_START = McuExternalConstant.MCU_DATA_SWITCH_ON;
	public static final int BACKCAR_STATUS_STOP = McuExternalConstant.MCU_DATA_SWITCH_OFF;
}
