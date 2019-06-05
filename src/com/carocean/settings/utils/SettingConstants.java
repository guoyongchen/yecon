package com.carocean.settings.utils;

import com.yecon.metazone.YeconMetazone;

public class SettingConstants {

	public static final String MCU_ACTION_LOUDNESS_KEY = "com.yecon.action.LOUDNESS_KEY";
	public static final String ACTION_RESET_FACTORY = "com.yecon.action.FACTORY_RESET";

	public static final String key_ui_theme = "ui_theme";
	public static final String key_ui_wallpaper = "ui_wallpaper";
	public static final String key_screensaver_bg = "screensaver_bg";
	public static final String key_screensaver_time = "screensaver_time";
	public static final String key_backlight = "backlight";
	public final static String key_rgb_video[] = { "screen_bright", "screen_contrast", "screen_hue",
			"screen_saturation" };
	public static final String key_autonavi = "autonavi";
	public static final String key_collision = "collision";
	public static final String key_brakeassist = "brakeassist";
	public static final String key_handlebrake = "handlebrake";
	public static final String key_ligth_control = "ligth_control";
	public static final String key_ldw = "ldw";
	public static final String key_air_show_time = "air_show_time";
	public static final String key_intelligent_charging_pile = "intelligent_charging_pile";// 充电站一键导航开关
	public static final String key_show_navi_flag = "show_navi_flag";// 充电站一键导航弹出标志
	public static final String key_speed_compensation_flag = "speed_compensation_flag";// 车速补偿标志

	// 保存经纬度
	public static final String key_gps_longitude = "gps_longitude";
	public static final String key_gps_altitude = "gps_altitude";

	// 车辆设置记忆
	public static final String key_speed_warning_switch = "speed_warning_switch";// 车速报警设置开关
	public static final String key_fatigue_driving_switch = "fatigue_driving_switch";// 疲劳驾驶设置开关
	public static final String key_speed_warning = "speed_warning";// 车速报警
	public static final String key_fatigue_driving = "fatigue_driving";// 疲劳驾驶
	public static final String key_intelligent_speed_limit_warning = "intelligent_speed_limit_warning";// 智能限速提示

	public enum LANGUAGE_T {
		CN, TW, US,
	}

	public enum BACKLIGHT_T {
		DAY, NIGHT, AUTO,
	}

	public static int defaultVolume = 12;
	public static boolean def_volume_switch = true;
	public static int backlightTab[] = { 204, 80, 150 };
	public static LANGUAGE_T eLanguage = LANGUAGE_T.CN;
	public static final int[] DEFAULT_RGB_VALUES = { 60, 25, 60, 60 };
	public static int[] videoPara = { 40, 25, 50, 50 };
	public static int brightnessMode = 0;
	public static int timeFormat = 0;
	public static int dateFormat = 2;
	public static int DEFAULT_BACKLIGHT_VALUE = YeconMetazone.GetBacklightness() * 255 / 100;
	public static int backlight = DEFAULT_BACKLIGHT_VALUE;
	public static boolean remix_enable = false;
	public static int remix_ratio = 40;
	public static int backcar_enable = 1;
	public static int backcar_port = 0;
	public static int backcar_mirror = 0;
	public static int backcar_guidelines = 0;
	public static int backcar_staticlines = 0;
	public static int backcar_radar = 0;
	public static boolean backcar_mute = false;
	public static boolean backcar_reduce_volume = true;

	public static String factory_password = "168";
	public static boolean autonavi = false;
	public static boolean collision = false;
	public static boolean brakeassist = false;
	public static boolean handlebrake = false;
	public static boolean ligth_control = false;
	public static boolean ldw = false;
	public static boolean intelligent_charging_pile = true;
	public static int speed_compensation_flag = 0;
	// 仪表设置默认开关
	public static int speed_warning_switch = 0;
	public static int fatigue_driving_switch = 0;

	// EQ constants for ATE.
	public static final String AUTOMATION_EQ_BROADCAST_RECV = "AUTOMATION_EQ_BROADCAST_RECV";
	public static final String AUTOMATION_EQ_BROADCAST_SEND = "AUTOMATION_EQ_BROADCAST_SEND";
	public static final int ATE_CMD_EQ_TREB_ALTO_BASS_SET = 1;
	public static final int ATE_CMD_EQ_FADE_BALANCE_SET = 2;
	public static final int ATE_CMD_EQ_RESTORE = 5;

}
