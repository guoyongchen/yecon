package com.carocean.utils;

public class Constants {
	public final static boolean isDebug = true;

	public final static String XML_PATH_DEF = "/usr1/factoryconfig.xml";
	public final static String XML_PATH_USER = "/usr1/factoryconfig_user.xml";
	public final static String ACTION_RESET_FACTORY = "com.yecon.action.FACTORY_RESET";
	public final static String ACTION_TAB_CHANGE = "com.yecon.action.TAB_CHANGE";
	public final static String ACTION_AUX_PORT_SET = "action.aux_port_set";
	public final static String ACTION_RADIO_UPDATE = "com.yecon.action.radioindex";
	public final static String ACTION_BRIGHTNESS_CHANGED = "com.yecon.action_BRIGHTNESS_CHANGED";
	public final static String ACTION_DVD_EJECT_CHANGED = "com.yecon.action_DVD_EJECT_CHANGED";
	public static final String ACTION_SETTING_EXIT = "com.yecon.CarSetting.exit";
	public static final String ACTION_QB_POWERON = "autochips.intent.action.QB_POWERON";
	public static final String ACTION_QB_POWEROFF = "autochips.intent.action.QB_POWEROFF";
	public final static String ACTION_ASSISTIVETOUCH = "com.yecon.action.ACTION_ASSISTIVETOUCH";
	public final static String ACTION_VOICE_BTN_ENABLE = "com.yecon.action.ACTION_VOICE_BTN_ENABLE";
	public final static String ACTION_VOICE_WAKEUP_ENABLE = "com.yecon.action.ACTION_VOICE_WAKEUP_ENABLE";
	public final static String ACTION_CLOCK_TYPE = "com.yecon.action.ACTION_CLOCK_TYPE";
	public final static String PERSYS_FIRST_UPGRADE = "persist.sys.first_upgrade";
	public final static String PERSYS_BT_UPGRADE_OK = "persist.sys.bt_upgrade_ok";
	public static final String PERSIST_YECON_SCAN = "persist.sys.yecon_scan"; // bit0: 1 or 0 : scan or not scan internal storage memory
	public static final String PERSIST_PERMISSION_INSTALLATION = "persist.sys.install"; // 0:默认允许安装 1:默认禁止安装(可通过设置打开) 3:默认禁止安装(无法通过设置打开)

	public static final String SP_KEY_SOURCE = "source";

	public static final int PLAY_STATUS_PLAYING = 1;
	public static final int PLAY_STATUS_PAUSE = 2;
	public static final int PLAY_STATUS_STOP = 3;
	
	public static final int RADIO_ID=1;		//收音
	public static final int MUSIC_ID=2;		//音乐
	public static final int VIDEO_ID=3;		//视频
	public static final int BT_ID=4;		//蓝牙
	public static final int IMAGE_ID=5;		//图片
	public static final int SETTING_ID=6;	//设置
	public static final int IE_ID=7;		//浏览器
	public static final int FILEBROWSE_ID=8;//文件浏览器
	public static final int CALENDAR_ID=9;	//日历
	public static final int CAR_ID=10;		//车辆设置
	public static final int MEDIA_ID=11;	//多媒体
	public static final int KUWO_ID=12;		//酷我
	public static final int KAOLA_ID=13;	//同听
	public static final int NAVI_ID=14;		//导航
	public static final int CARPLAY_ID=15;	//carplay
	public static final int EASYNET_ID=16;	//亿联
	
	public static final int PHONE_ID=18;	//电话  也是直接进蓝牙界面
	public static final int PHONEBOOK1_ID=20;//联系人  下面这4个都是进入蓝牙联系人界面
	public static final int PHONEBOOK2_ID=21;//通讯录
	public static final int PHONEBOOK3_ID=22;//电话本
	public static final int PHONEBOOK4_ID=23;//电话簿

	public static final int CALLHISTORY_ID=24;//进入蓝牙通话记录界面
	
	public static final int BT_MUSIC_ID = 25;   // BT Music
	public static final int VOICE_ID = 26;      // Voice
	
	public static final int KEY_SRC_MODE_FM = 1;
	public static final int KEY_SRC_MODE_AM = 2;
	public static final int KEY_SRC_MODE_USB1 = 3;
	public static final int KEY_SRC_MODE_USB2 = 4;
	public static final int KEY_SRC_MODE_BT_MUSIC = 5;
	public static final int KEY_SRC_MODE_EXTERNAL = 6;
	
	/** action from ATE to open UI */
	public static final String ACTION_CAROCEAN_OPEN_UI = "com.carocean.action.OPEN_UI";
	
	/** action from navi to open wifi */
	public static final String ACTION_OPEN_WIFI_SETTING = "com.yecon.action.open_wifi_setting";
	
	public static final String UI_TYPE = "ui_type";
	
	public static final String SRC_ID = "src_id";
	
	public static final int UI_TYPE_SOUND = 10001;
	
	public static final int UI_TYPE_BALANCE = 10002;
	
	public static final int SOURCE_MAX = 255;
	/** action from ATE ,request srcId(in fact it is UI state) */
	public static final String ACTION_REQUEST_SRC = "com.carocean.action.REQUEST_SRC";
	
	public static final String ACTION_REPLY_SRC = "com.carocean.action.REPLY_SRC";
	
	public static final String ACTION_REQUESET_APP_VERSION = "com.carocean.requeset.APP_VERSION"; 
	
	public static final String ACTION_RETURN_APP_VERSION = "com.carocean.return.APP_VERSION";
	
	public static final String APP_VERSION_EXTRA = "app_version";
	// Source type for ATE
	public static final int TYPE_RADIO = 0;
	public static final int TYPE_NAVI = 3;
	public static final int TYPE_AVIN = 4;
	public static final int TYPE_BT_MUSIC = 6;
	public static final int TYPE_BT = 7;
	public static final int TYPE_AUDIO = 8;
	public static final int TYPE_VIDEO = 9;
	public static final int TYPE_PHOTO = 11;
	public static final int TYPE_VOICE = 0x0D;
	
	public static final String AUTOMATION_CONTROL_BROADCAST_RECV = "AUTOMATION_CONTROL_BROADCAST_RECV";
	public static final String AUTOMATION_CONTROL_BROADCAST_SEND = "AUTOMATION_CONTROL_BROADCAST_SEND";
}
