package com.carocean.utils;

/**
 * 各种广播的定义 Created by xuhh on 2018/12/14.
 */

public class VoiceMsgDefine {
	// for air start
	public static final String ACTION_IFLY_VOICE_AIR = "action.com.carocean.iflyvoice.air";// 发送空调相关处理的广播
	public static final String EXTRA_AIR_CMD = "cmd"; // 空调命令
	public static final String EXTRA_AIR_TEMP = "temp"; // 空调温度
	public static final String EXTRA_AIR_SPEED = "speed"; // 空调风速
	// 以下为空调命令
	public static final int AIR_CMD_INC_FANSPEED = 1; // 增加风速 自动增加一级
	public static final int AIR_CMD_DEC_FANSPEED = 2; // 减少风速 自动减小一级
	public static final int AIR_CMD_INC_NUM_FANSPEED = 3; // 增加多少风速 需要去读speed
	public static final int AIR_CMD_DEC_NUM_FANSPEED = 4; // 减小多少风速 需要去读speed
	public static final int AIR_CMD_SET_NUM_FANSPEED = 5; // 风速设为多少 需要去读speed
	public static final int AIR_CMD_OPEN_AIR = 6; // 打开空调
	public static final int AIR_CMD_CLOSE_AIR = 7; // 关闭空调
	public static final int AIR_CMD_INC_TEMP = 8; // 空调温度调高
	public static final int AIR_CMD_DEC_TEMP = 9; // 空调温度调低
	public static final int AIR_CMD_INC_NUM_TEMP = 10; // 空调温度调高多少
	public static final int AIR_CMD_DEC_NUM_TEMP = 11; // 空调温度调低多少
	public static final int AIR_CMD_SET_NUM_TEMP = 12; // 空调温度设为多少
	public static final int AIR_CMD_SCAN_FAN = 13; // 空调扫风
	public static final int AIR_CMD_CHUI_HEAD = 14; // 空调吹头
	public static final int AIR_CMD_CHUI_FACE = 15; // 空调吹面
	public static final int AIR_CMD_CHUI_FOOT = 16; // 空调吹脚
	public static final int AIR_CMD_CHUI_FACEFOOT = 17; // 空调吹面吹脚
	public static final int AIR_CMD_CHUI_FOOTDEFROST = 18; // 空调吹脚除霜
	public static final int AIR_CMD_CHUI_DEFROST = 19; // 空调除霜
	public static final int AIR_CMD_OPEN_PTC = 20; // 打开空调制热
	public static final int AIR_CMD_OPEN_AC = 21; // 打开空调制冷
	public static final int AIR_CMD_CLOSE_PTC = 22; // 关闭空调制热
	public static final int AIR_CMD_CLOSE_AC = 23; // 关闭空调制冷
	public static final int AIR_CMD_CYCLE_IN = 24;// 内循环
	public static final int AIR_CMD_CYCLE_OUT = 25;// 外循环
	public static final int AIR_CMD_OPEN_AUTO = 26;// 打开自动模式
	public static final int AIR_CMD_CLOSE_AUTO = 27;// 关闭自动模式
	public static final int AIR_CMD_CHUI_DEFROST_BACK = 28; // 空调后除霜
	public static final int AIR_CMD_CHUI_DEFROST_FRONT = 29; // 空调前除霜
	public static final int AIR_CMD_CLOSE_DEFROST_FRONT = 31; // 关闭空调前除霜
	public static final int AIR_CMD_CLOSE_DEFROST_BACK = 32;// 关闭空调后除霜
	// for air end

	// for carcontrol start
	public static final String ACTION_IFLY_VOICE_CARCONTROL = "action.com.carocean.iflyvoice.carcontrol";// 发送车身控制相关处理的广播
	public static final String EXTRA_CARCONTROL_CMD = "cmd"; // int类型 命令 1 打开 2
																// 关闭
	public static final int CARCONTROL_OPERATION_OPEN = 1;
	public static final int CARCONTROL_OPERATION_CLOSE = 2;
	public static final String EXTRA_CARCONTROL_DEV_ID = "name";// int 类型
	public static final String EXTRA_CARCONTROL_LEVEL = "level";// int 类型 1 大一点
																// 2 小一点 3 一半
	public static final int CARCONTROL_LEVEL_LARGE = 1;// 大一点
	public static final int CARCONTROL_LEVEL_SMALL = 2;// 小一点
	public static final int CARCONTROL_LEVEL_MIDDLE = 3;// 一半
	// 【天窗|后备箱|近光灯|远光灯|雾灯|前雾灯|后雾灯|示廓灯|警示灯|车窗】
	public static final int CARCONTROL_NAME_UNKNOW = 0;// 未知
	public static final int CARCONTROL_NAME_TIANCHUANG = 1;// 天窗
	public static final int CARCONTROL_NAME_HOUBEIXIANG = 2;// 后备箱
	public static final int CARCONTROL_NAME_JINGUANGDENG = 3;// 近光灯
	public static final int CARCONTROL_NAME_YUANGUANGDENG = 4;// 远光灯
	public static final int CARCONTROL_NAME_WUDENG = 5;// 雾灯
	public static final int CARCONTROL_NAME_QIANWUDENG = 6;// 前雾灯
	public static final int CARCONTROL_NAME_HOUWUDENG = 7;// 后雾灯
	public static final int CARCONTROL_NAME_SHILANGDENG = 8;// 示廓灯
	public static final int CARCONTROL_NAME_JINGSHIDENG = 9;// 警示灯
	public static final int CARCONTROL_NAME_CHECHUANG = 10;// 车窗
	public static final int CARCONTROL_NAME_YUGUAQI = 11;// 雨刮器
	public static final int CARCONTROL_NAME_TIANCHUANG_LEFT_FRONT = 12;// 左前门车窗
	public static final int CARCONTROL_NAME_TIANCHUANG_RIGHT_FRONT = 13;// 右前门车窗
	public static final int CARCONTROL_NAME_TIANCHUANG_LEFT_BACK = 14;// 左后门车窗
	public static final int CARCONTROL_NAME_TIANCHUANG_RIGHT_BACK = 15;// 右后门车窗
	public static final int CARCONTROL_NAME_QIAOQI_TIANCHUANG=16;//翘起天窗
	// for carcontrol end

	// for vehicleInfo start
	public static final String ACTION_IFLY_VOICE_VEHICLEINFO = "action.com.carocean.iflyvoice.vehicleinfo";// 发送车身信息相关处理的广播
	public static final String EXTRA_VEHICLEINFO_CMD = "cmd"; // int 类型命令 QUERY
	public static final String EXTRA_VEHICLEINFO_DEV_ID = "name"; // int类型
	public static final int VEHICLEINFO_OPERATION_QUERY = 1;
	public static final int VEHICLEINFO_NAME_UNKNOW = 0;// 未知
	public static final int VEHICLEINFO_NAME_TRIP = 1;// 油量
	public static final int VEHICLEINFO_NAME_TPMS = 2;// 胎压
	public static final int VEHICLEINFO_NAME_ENGINE = 3;// 发动机
	public static final int VEHICLEINFO_NAME_AIRPORT = 4;// 空调
	public static final int VEHICLEINFO_NAME_WATER = 5;// 水温
	public static final int VEHICLEINFO_NAME_BRAKE = 6;// 刹车
	public static final int VEHICLEINFO_NAME_ZHIDONG = 7;// 制动
	public static final int VEHICLEINFO_NAME_STATUS = 8;// 整体车况
	// for carcontrol end

    public static final String ACTION_IFLY_VOICE_SCREEN="action.com.carocean.iflyvoice.screen";//发送屏幕相关处理的广播
    public static final String EXTRA_SCREEN_CMD="cmd";          //命令
    public static final int SCREEN_CMD_INC=1;                   //增加亮度
    public static final int SCREEN_CMD_DEC=2;                   //减小亮度
    public static final int SCREEN_CMD_MAX=3;                   //最大亮度
    public static final int SCREEN_CMD_MIN=4;                   //最小亮度
    public static final int SCREEN_CMD_OPEN=5;                  //开屏
    public static final int SCREEN_CMD_CLOSE=6;                 //关屏

}
