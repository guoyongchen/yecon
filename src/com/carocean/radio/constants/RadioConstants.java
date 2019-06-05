package com.carocean.radio.constants;

public class RadioConstants {

	public static final String PROPERTY_KEY_AVIN_TYPE = "persist.sys.avintype";
	public static final int BAND_ID_FM = 0;
	public static final int BAND_ID_AM = 3;

	public static final int DEFAULT_FM_STATION_MIN = 8750;
	public static final int DEFAULT_FM_STATION_MAX = 10800;
	public static final int DEFAULT_AM_STATION_MIN = 531;
	public static final int DEFAULT_AM_STATION_MAX = 1629;

    //for voice interface start
    public static final String ACTION_IFLY_VOICE_RADIO="action.com.carocean.iflyvoice.radio";//发送收音相关处理的广播
    public static final String EXTRA_RADIO_CMD="cmd";       //调频 调幅  上一台  下一台 int
    public static final String EXTRA_RADIO_FREQ="freq";    //频率 String,当CMD=1  2的时候才有效
    public static final int RADIO_CMD_PLAY_FM=1;            //调频87.5
    public static final int RADIO_CMD_PLAY_AM=2;            //调幅1620
    public static final int RADIO_CMD_PREV_PRESET=3;        //上一预存台
    public static final int RADIO_CMD_NEXT_PRESET=4;        //下一预存台
    public static final int RADIO_CMD_PREV_STEP=5;          //上一步进
    public static final int RADIO_CMD_NEXT_STEP=6;          //下一步进
    public static final int RADIO_CMD_SEARCH=7;             //搜台
    public static final int RADIO_CMD_PREV_SEARCH=8;       //向上搜台
    public static final int RADIO_CMD_NEXT_SEARCH=9;       //向下搜台
    public static final int RADIO_CMD_FAVOR_CURFREQ=10;       //收藏当前电台
    public static final int RADIO_CMD_GOTO_FM=11;           //切换到FM
    public static final int RADIO_CMD_GOTO_AM=12;           //切换到AM
    //end
}
