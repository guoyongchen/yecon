package com.carocean.settings.utils;

public class PersistUtils {
    public final static String PERSYS_PWRON_VOLUME = "persist.sys.pwron_volume";
    public final static String PERSYS_PWRON_VOLUME_ENABLE = "persist.sys.pwron_volume_enable";

	public final static String PERSYS_TXZMUSIC_TYPE = "persist.sys.txzmusic_type";
	public final static String PERSYS_SYSTEM_FIRST_FLAG = "persist.sys.sys_first_flag";
	public final static String PERSYS_REBOOT_FLAG = "persist.sys.app_reboot_flag";

	public final static String PERSYS_AUX_PORT = "persist.sys.auxport";

	public final static String PERSYS_LANGUAGE = "persist.sys.language";
	public final static String PERSYS_COUNTYR = "persist.sys.country";
	public final static String PERSYS_LOCALEVAR = "persist.sys.localevar";
	public final static String PERSYS_LANGUAGE_USER = "user.language";
	public final static String PERSYS_COUNTYR_USER = "user.region";
	public final static String TIMEZONE_PROPERTY = "persist.sys.timezone";

	public final static String PERSYS_HEADLIGHT_ENABLE = "persist.sys.headlight_enable";
	public final static String PERSYS_BRAKE_ENABLE = "persist.sys.parking_enable";
	public final static String PERSYS_USB_MODE = "persist.current.usb.mode";
	public final static String PERSYS_USB_TYPE = "persist.current.usb.protocol";
	public final static String PERSYS_CVBS_ENABLE = "persist.sys.cvbs_enable";

	// back car
	public final static String PERSYS_BACKCAR_ENABLE = "persist.sys.backcar_enable";
	public final static String PERSYS_BACKCAR_MUTE = "persist.sys.backcar_mute_enable";
	public final static String PERSYS_BACKCAR_TRACE = "persist.sys.trace_enable";
	public final static String PERSYS_BACKCAR_DYNAMIC_TRACE = "persist.sys.dyntrace_enable";
	public final static String PERSYS_BACKCAR_RADAR = "persist.sys.radar_enable";
	public final static String PERSYS_BACKCAR_MIRROR = "persist.sys.backcar_mirror";
	public final static String PERSYS_BACKCAR_REDUCE_VOLUME = "persist.sys.backcar_subvolume";

	public static final String PERSYS_BKLIGHT[] = { "persist.sys.yeconBacklight", "persist.sys.Backlight_low", "persist.sys.Backlight_mid",
			"persist.sys.Backlight_high" };

	public final static String PERSYS_VOLUME[] = { "persist.sys.volume_front", "persist.sys.volume_rear", "persist.sys.volume_gis",
			"persist.sys.volume_backcar", "persist.sys.volume_bt" };

	public final static String PERSYS_RGB_VIDEO[][] = {
			{ "persist.sys.screen_bright", "persist.sys.screen_contrast", "persist.sys.screen_hue", "persist.sys.screen_saturation" },
			{ "persist.sys.aux1_bright", "persist.sys.aux1_contrast", "persist.sys.aux1_hue", "persist.sys.aux1_saturation" },
			{ "persist.sys.aux2_bright", "persist.sys.aux2_contrast", "persist.sys.aux2_hue", "persist.sys.aux2_saturation" },
			{ "persist.sys.aux3_bright", "persist.sys.aux3_contrast", "persist.sys.aux3_hue", "persist.sys.aux3_saturation" },
			{ "persist.sys.aux4_bright", "persist.sys.aux4_contrast", "persist.sys.aux4_hue", "persist.sys.aux4_saturation" },
			{ "persist.sys.aux5_bright", "persist.sys.aux5_contrast", "persist.sys.aux5_hue", "persist.sys.aux5_saturation" },
			{ "persist.sys.backcar_bright", "persist.sys.backcar_contrast", "persist.sys.backcar_hue", "persist.sys.backcar_saturation" },
			{ "persist.sys.dgi_bright", "persist.sys.dgi_contrast", "persist.sys.dgi_hue", "persist.sys.dgi_saturation" },
			{ "persist.sys.dvd_bright", "persist.sys.dvd_contrast", "persist.sys.dvd_hue", "persist.sys.dvd_saturation" },
			{ "persist.sys.hdmi_bright", "persist.sys.hdmi_contrast", "persist.sys.hdmi_hue", "persist.sys.hdmi_saturation" },
			{ "persist.sys.usb_bright", "persist.sys.usb_contrast", "persist.sys.usb_hue", "persist.sys.usb_saturation" },
			{ "persist.sys.vga_bright", "persist.sys.vga_contrast", "persist.sys.vga_hue", "persist.sys.vga_saturation" },
			{ "persist.sys.ypbpr_bright", "persist.sys.ypbpr_contrast", "persist.sys.ypbpr_hue", "persist.sys.ypbpr_saturation" } };

	public final static String PERSYS_YUV_GAIN[][] = { { "persist.sys.aux1_y_gain", "persist.sys.aux1_u_gain", "persist.sys.aux1_v_gain" },
			{ "persist.sys.aux2_y_gain", "persist.sys.aux2_u_gain", "persist.sys.aux2_v_gain" },
			{ "persist.sys.aux3_y_gain", "persist.sys.aux3_u_gain", "persist.sys.aux3_v_gain" },
			{ "persist.sys.aux4_y_gain", "persist.sys.aux4_u_gain", "persist.sys.aux4_v_gain" },
			{ "persist.sys.aux5_y_gain", "persist.sys.aux5_u_gain", "persist.sys.aux5_v_gain" },
			{ "persist.sys.backcar_y_gain", "persist.sys.backcar_u_gain", "persist.sys.backcar_v_gain" },
			{ "persist.sys.dgi_y_gain", "persist.sys.dgi_u_gain", "persist.sys.dgi_v_gain" },
			{ "persist.sys.dvd_y_gain", "persist.sys.dvd_u_gain", "persist.sys.dvd_v_gain" },
			{ "persist.sys.hdmi_y_gain", "persist.sys.hdmi_u_gain", "persist.sys.hdmi_v_gain" },
			{ "persist.sys.usb_y_gain", "persist.sys.usb_u_gain", "persist.sys.usb_v_gain" },
			{ "persist.sys.vga_y_gain", "persist.sys.vga_u_gain", "persist.sys.vga_v_gain" },
			{ "persist.sys.ypbpr_y_gain", "persist.sys.ypbpr_u_gain", "persist.sys.ypbpr_v_gain" } };

	public final static String PERSYS_AUDIO[] = { "persist.sys.audio_treble", "persist.sys.audio_alto", "persist.sys.audio_bass",
			"persist.sys.audio_subwoofer", "persist.sys.audio_loundness", "persist.sys.balance_f_r", "persist.sys.balance_l_r" };

	public final static String PERSYS_AVIN_CHANNEL[] = { "persist.sys.avin.avin1_v", "persist.sys.avin.avin1_a", "persist.sys.avin.avin2_v",
			"persist.sys.avin.avin2_a", "persist.sys.avin.tv_v", "persist.sys.avin.tv_a", "persist.sys.avin.dvr_v", "persist.sys.avin.cam_v",
			"persist.sys.avin.fm_a", "persist.sys.avin.ipod_a", };

	// BT
	public final static String PERSYS_BT_NOICE = "persist.sys.bt_noice_enable";
	public final static String PERSYS_BT_DEVICE = "persist.sys.bt_device";
	public final static String PERSYS_BT_PAIR = "persist.sys.bt_pair";
	public final static String PERSYS_BT_AUTO_CONNECT = "persist.sys.bt_auto_connect";
	public final static String PERSYS_BT_AUTO_ANSWER = "persist.sys.bt_auto_answer";
	public final static String PERSYS_BT_DISCOVER = "persist.sys.bt_discover";
	public final static String PERSYS_BT_DEVICE_AUTO_SET = "persist.sys.bt_device_auto_set";
	public final static String PERSYS_BT_DEVICE_NAME_OEM = "persist.sys.bt_device_name_oem";
	public final static String PERSYS_BT_SSP = "persist.sys.bt_ssp";

	public final static String PERSYS_NAVI_PATH = "persist.sys.navi_path";
	public final static String PERSYS_NAVI_REMIX = "persist.sys.navi_remix";
	public final static String PERSYS_NAVI_LISTEN = "persist.sys.navi_listen";
	public final static String PERSYS_REMIX_RATIO = "persist.sys.remix_ratio";

	public final static String PERSYS_RADIO_TYPE = "persist.sys.radio_type";
	public final static String PERSYS_RADIO_AREA = "persist.sys.radio_area";

	public final static String PERSYS_RADIO_AREA_ENABLE[] = { "persist.sys.area.europe", "persist.sys.area.usa", "persist.sys.area.oirt",
			"persist.sys.area.japan", "persist.sys.area.m_east", "persist.sys.area.latin", "persist.sys.area.australia", "persist.sys.area.asia",
			"persist.sys.area.samer1", "persist.sys.area.arabia", "persist.sys.area.china", "persist.sys.area.samer2", "persist.sys.area.korea",
			"persist.sys.area.russian", "persist.sys.area.fm" };

	public final static String PERSYS_CAN_TYPE = "persist.sys.can_type";
	public final static String PERSYS_CAR_TYPE = "persist.sys.car_type";
	public final static String PERSYS_VIDEO_OUT_FORMAT = "persist.sys.video_out_format";
	public final static String PERSYS_TV_TYPE = "persist.sys.tv_type";
	public final static String PERSYS_TV_AREA = "persist.sys.tv_area";
	public final static String PERSYS_DVR_TYPE = "persist.sys.dvr_type";
	public final static String PERSYS_AUDIO_PORT_CRV = "persist.sys.aport_crv";
	public final static String PERSYS_MIC_VOLUME = "persist.sys.mic_volume";
	public final static String PERSYS_DVD_AREA_CODE = "persist.sys.dvd_area_code";
	public final static String PERSYS_TYRE_COM = "persist.sys.tyre_com";
	public final static String PERSYS_OUTSIDE_TEMP_TEST = "persist.sys.outside_temp_test";
	public final static String PERSYS_LCD_ENABLE = "persist.sys.lcd_enable";
	public final static String PERSYS_ANTENNA_CTRL = "persist.sys.antenna_ctrl_enable";
	public final static String PERSYS_FIXED_AIR_DISPLAY = "persist.sys.fixed.air.display";

	public final static String PERSYS_RDS = "persist.sys.rds";
	public final static String PERSYS_FM_DX = "persist.sys.fm_dx";
	public final static String PERSYS_FM_LOC = "persist.sys.fm_loc";
	public final static String PERSYS_AM_DX = "persist.sys.am_dx";
	public final static String PERSYS_AM_LOC = "persist.sys.am_loc";
	public final static String PERSYS_OIRT_DX = "persist.sys.oirt_dx";
	public final static String PERSYS_OIRT_LOC = "persist.sys.oirt_loc";

	public final static String PERSYS_VIDEO_PORT_DRIVE_RECORD = "persist.sys.vport_drive_record";
	public final static String PERSYS_VIDEO_PORT_FRONT_CAMERA = "persist.sys.vport_front_camera";
	public final static String PERSYS_GAIN_BT_PHONE = "persist.sys.gain_bt_phone";
	public final static String PERSYS_GAIN_GIS = "persist.sys.gain_gis";
	public final static String PERSYS_GAIN_OTHER = "persist.sys.gain_other";

	public final static String PERSYS_POWER_AMPLIFIER_CTRL = "persist.sys.power_amp_ctrl";
	public final static String PERSYS_AIR_TEMP_CTRL = "persist.sys.air_temp_ctrl";
	public final static String PERSYS_CHRAMATIC_LAMP = "persist.sys.chramatic_lamp";

	public final static String PERSYS_DB_AUX = "persist.sys.db_aux";
	public final static String PERSYS_DB_A2DP = "persist.sys.db_a2dp";
	public final static String PERSYS_DB_FM = "persist.sys.db_fm";
	public final static String PERSYS_DB_AM = "persist.sys.db_am";
	public final static String PERSYS_DB_TV = "persist.sys.db_tv";

	public final static String PERSYS_FUN_DVD_ENABLE = "persist.sys.dvd_eject_enable";
	public final static String PERSYS_FUN_FM_ENABLE = "persist.sys.fun.fm.enable";
	public final static String PERSYS_FUN_GPS_ENABLE = "persist.sys.fun.gps.enable";
	public final static String PERSYS_FUN_AVIN_ENABLE = "persist.sys.fun.avin.enable";
	public final static String PERSYS_FUN_AVIN2_ENABLE = "persist.sys.fun.avin2.enable";
	public final static String PERSYS_FUN_TV_ENABLE = "persist.sys.fun.tv.enable";
	public final static String PERSYS_FUN_IPOD_ENABLE = "persist.sys.fun.ipod.enable";
	public final static String PERSYS_FUN_MUSIC_ENABLE = "persist.sys.fun.music.enable";
	public final static String PERSYS_FUN_VIDEO_ENABLE = "persist.sys.fun.video.enable";
	public final static String PERSYS_FUN_BT_ENABLE = "persist.sys.fun.bt.enable";
	public final static String PERSYS_FUN_DVR_ENABLE = "persist.sys.fun.dvr.enable";
	public final static String PERSYS_FUN_DVR_UART_ENABLE = "persist.sys.fun.dvr_uart.enable";
	public final static String PERSYS_FUN_MICRACAST_ENABLE = "persist.sys.fun.miracast.enable";
	public final static String PERSYS_FUN_CANBUS_ENABLE = "persist.sys.fun.canbus.enable";
	public final static String PERSYS_FUN_TPMS_ENABLE = "persist.sys.fun.tpms.enable";

	public final static String PERSYS_QICAIDENG_LIGHT_MODE = "persist.sys.light.mode";
	public final static String PERSYS_QICAIDENG_LIGHT_R = "persist.sys.light.r";
	public final static String PERSYS_QICAIDENG_LIGHT_G = "persist.sys.light.g";
	public final static String PERSYS_QICAIDENG_LIGHT_B = "persist.sys.light.b";

	public final static String PERSYS_TOUCHKEYLEAN_KBTN = "persist.sys.kbtn";
	public final static String PERSYS_TOUCHKEYLEAN_KTHRESOLD = "persist.sys.thresold";

	public final static String PERSYS_ASSISTIVE_TOUCH[] = { "persist.sys.assistivetouch", "persist.sys.assis_home", "persist.sys.assis_return",
			"persist.sys.assis_power", "persist.sys.assis_screen_off", "persist.sys.assis_mode", "persist.sys.assis_navi", "persist.sys.assis_band",
			"persist.sys.assis_play", "persist.sys.assis_pre", "persist.sys.assis_next", "persist.sys.assis_mute", "persist.sys.assis_vol_add",
			"persist.sys.assis_vol_minus", "persist.sys.assis_phone_answer", "persist.sys.assis_phone_hungup" };

	public final static String PERSYS_VOICE_TOUCH = "persist.sys.voice_btn_enable";
	public final static String PERSYS_VOICE_WAKEUP = "persist.sys.voice_wakeup_enable";
	public final static String PERSYS_VOICE_AEC_ENABLE = "persist.sys.voice_aec_enable";
	public final static String PERSYS_VOICE_WAKEUP_WORDS = "persist.sys.voice_wakeup_words";

	public final static String PERSYS_EXTERN_RADIO12V_ENABLE = "persist.sys.radio12v_enable";
	public final static String PERSYS_EXTERN_AMP_ENABLE = "persist.sys.extern_amp_enable";
	public final static String PERSYS_VCOMM_VOLTAGE = "persist.sys.vcomm_voltage";

	public final static String PERSYS_QICAIDENG_ENABLE = "persist.sys.qicaideng_enable";
	public static final String PROPERTY_KEY_ENABLE_BOOT_ANIM = "persist.sys.enablebootanim";
	public static final String PROPERTY_KEY_CVBS_EANBLE = "persist.sys.cvbs_enable";
	public final static String PERSYS_FUNS_OTHER[] = { "persist.sys.fun.headlight", "persist.sys.fun.assi_touch", "persist.sys.fun.voice_touch",
			"persist.sys.fun.swc", "persist.sys.fun.wallpaper", "persist.sys.fun.wifi", "persist.sys.fun.audio.alto",
			"persist.sys.fun.audio.subwoofer", "persist.sys.fun.clock", "persist.sys.fun.startup_warning" };
	public final static String PERSYS_CLOCK_TYPE = "persist.sys.clock_type";

	public final static String PERSYS_BOOT_LOGO_ENABLE = "persist.sys.bootlogo";
	public final static String PERSYS_SLEEP_READY_TIME = "persist.sys.sleep.ready.time";
	public final static String PERSYS_SLEEP_TIME = "persist.sys.sleep.time";
	public final static String PERSYS_SLEEP_POWER_ENABLE = "persist.sys.sleep.power";
	public final static String PERSYS_CUSTOM_ID = "persist.sys.custom.id";
	public final static String PERSYS_UI_THEME = "persist.sys.ui_theme";
	public final static String PERSYS_FUNC_FLAG = "persist.sys.func_flag";
	public final static String PERSYS_LCD_TYPE = "persist.sys.lcd.type"; // lcd接口类型
																			// ttl
																			// or
																			// lvds
	public final static String PERSYS_LVDS_BIT = "persist.sys.lvds.bit"; // lvds数据位宽
	public final static String PERSYS_LCD_RESOLUTION = "persist.sys.lcd.resolution"; // lcd分辨率
	public final static String PERSYS_TFT_DRIVE_CURRENT = "persist.sys.tft.drive.current"; // TFT驱动电流
	public final static String PERSYS_LVDS_DRIVE_CURRENT = "persist.sys.lvds.drive.current"; // lvds驱动电流
	public final static String PERSYS_DITHERING_OUTPUT = "persist.sys.dithering.output"; // dithering输出控制

	public final static String PERSYS_IS_FIRST_START = "persist.sys.start.first";
	public final static String PERSYS_BACK_LIGHT_R = "persist.sys.backlight.r";
	public final static String PERSYS_BACK_LIGHT_G = "persist.sys.backlight.g";
	public final static String PERSYS_BACK_LIGHT_B = "persist.sys.backlight.b";

	////////////////////////////////////////////////
	public static final String PERSYS_BT_MODULE_VER = "persist.sys.bt_module_ver";

	public static final String PERSYS_CAN_BOX = "persist.sys.can_box";
	public static final String PERSYS_CAN_CAR_MODE = "persist.sys.can_car_mode";
	public static final String PERSYS_CAN_CAR_TYPE = "persist.sys.can_car_type";
	public static final String PERSYS_CAN_CAR_CFG = "persist.sys.can_car_cfg";
	public static final String PERSYS_CAN_NEED_ICON = "persist.sys.fun.canbus";
	public static final String PERSYS_CAN_AIR_ICON = "persist.sys.can_air_icon";
	public static final String PERSYS_CAN_BAND = "persist.sys.can_band";
	public static final String PERSYS_CAN_SPEED_FUNC = "persist.sys.can_speed_func";

	public static final String PERSYS_MUSIC_PREV_BEGIN = "persist.sys.music.tobegin";
	public static final String PERSYS_MUSIC_PREV_BEGIN_TIME = "persist.sys.music.tobegin.time";
	public static final String PROPERTY_KEY_AUTOMUTE = "persist.sys.automute";
	public static final String PERSYS_HW_ROTATION = "persist.sys.hwrotation";

	public static final String PRV_NEXT_FAN = "persist.sys.mirr_pri_next";
	public static final String PERSYS_AUTO_TIME = "persist.sys.auto_time";
	public static final String PERSYS_PASSWORD = "persist.sys.password";
	
	public static final String PERSYS_MCU_VER = "persist.sys.mcu_ver";
}
