package com.carocean.media.constants;

import com.carocean.media.constants.MediaScanConstans.YeconMediaFilesColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaAlbumColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaArtistColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaDirColumns;

public final class MediaPlayerContants {
	
	// 记忆播放的磁盘
	public static final String LAST_MEMORY_DEVICE = "LAST_MEMORY_DEVICE";
	
	// 磁盘路径
	public static final String PATH = MediaScanConstans.PATH;

	// 列表类型
	public static final class ListType {
		// 磁盘中所有音乐文件列表
		public static final int ALL_MUSIC_FILE = 0x01;
		// 磁盘中所有视频文件列表
		public static final int ALL_VIDEO_FILE = 0x02;
		// 磁盘中所有图片文件列表
		public static final int ALL_IMAGE_FILE = 0x03;
		
		public static final int MUSIC_MIX = 0x04;
		public static final int VIDEO_MIX = 0x05;
		public static final int IMAGE_MIX = 0x06;
		
		public static final int ALL_DIR = 0x07;
		
		public static final int ARTIST_MIX = 0x08;
		public static final int ALBUM_MIX = 0x09;
		
		public static final int ALL_ARTIST = 0x0A;
		public static final int ALL_ALBUM = 0x0B;
	}
	
	// 媒体类型
	public static final class MediaType {
		// 音频
		public static final int MEDIA_AUDIO = 1;
		// 视频
		public static final int MEDIA_VIDEO = 2;
		// 图片
		public static final int MEDIA_IMAGE = 3;
		// 文件夹
		public static final int MEDIA_DIR = 4;
		// 艺术家
		public static final int MEDIA_ARTIST = 5;
		// 专辑
		public static final int MEDIA_ALBUM = 6;
	}
	
	// 循环播放模式
	public static final class RepeatMode{
		// 循环关闭
		public static final int OFF = 0;
		// 单曲循环
		public static final int SINGLE = 1;
		// 全部循环
		public static final int ALL = 2;
		// 文件夹循环
		public static final int DIR = 3;
		// 随机模式
		public static final int RANDOM = 4;
	}
	
	// 播放状态
	public static final class PlayStatus {
		// 空闲状态
		public static final int IDLE = 0;
		// 解码中
		public static final int DECODING = 1;
		// 解码完成
		public static final int DECODED  = 2;
		// seek
		public static final int SEEKING = 3;
		// 播放状态
		public static final int STARTED = 4;
		// 暂停状态
		public static final int PAUSED = 5;
		// 停止状态
		public static final int STOPED = 6;
		// 播放出错
		public static final int ERROR = 7;
		// 播放完成
		public static final int FINISH = 8;
	}
	
	// 服务状态
	public static final class ServiceStatus {
		// 扫描构建
		public final static int SCANING = 1;
		// 扫描完成
		public final static int SCANED = 2;
		// 扫描超时
		public final static int SCAN_TIMEOUT = 3;
		// 播放完成
		public final static int PLAYED = 4;
		// 空列表(不存在播放文件)
		public final static int EMPTY_STORAGE = 5;
		// 当前磁盘丢失
		public final static int LOST_CUR_STORAGE = 6;
		// 磁盘丢失
		public final static int LOST_STORAGE = 7;
		// 不存在磁盘
		public final static int NO_STORAGE = 8;
		// 失去Audio Focus
		public final static int LOST_AUDIO_FOCUS = 9;
		// 静默关机
		public final static int QB_POWER = 10;
		// 切换磁盘
		public final static int SWITCH_STORAGE = 11;
		// 通过语音切换列表类型
		public final static int SWITCH_MEDIA_LIST = 12;
		// 蓝牙音乐状态改变
		public final static int UPDATE_BT_DEVICE = 13;
		// 释放播放界面
		public final static int RELEASE_ACTIVITY = 14;
	}
	
	// 多媒体播放相关的消息
	public static final class MediaPlayerMessage {
		// 媒体服务状态
		public final static int UPDATE_SERVICE_STATE = 0;
		// 列表数据更新
		public final static int UPDATE_LIST_DATA = 1;
		// 播放状态
		public final static int UPDATE_PLAY_STATE = 2;
		// 随机状态
		public final static int UPDATE_RANDOM_STATE = 3;
		// 重复状态
		public final static int UPDATE_REPEAT_STATE = 4;
		// 播放进度(由于刷新太频繁,单独拿出来处理)
		public final static int UPDATE_PLAY_PROGRESS = 5;
		// 媒体状态
		public final static int UPDATE_MEDIA_INTO = 6;
		// 视频大小
		public final static int UPDATE_VIDEO_SIZE = 7;
		// 媒体类型
		public final static int UPDATE_MEDIA_TYPE = 8;
		// 曲目索引
		public final static int UPDATE_PLAY_INDEX = 9;
		// 更新歌词列表
		public final static int UPDATE_LRC_LIST = 10;
		// 更新歌词索引
		public final static int UPDATE_LRC_INDEX = 11;
		// 服务绑定成功
		public final static int UPDATE_BIND_SUCCESS = 12;
		// 拷贝文件结果返回
		public final static int UPDATE_DWONLOAD_STATE = 13;
	}
	
	/*
	 * requestPlayList 查询时需要查询所有的ID则使用-1
	 */
	public static final int ID_INVALID = -1;
	public static final int ID_ALL = ID_INVALID;
	
	/*
	 * 需要从文件数据库中查询的列
	 */
	public static final String[] FILE_COLUMNS = new String[] {
		YeconMediaFilesColumns._ID,
		YeconMediaFilesColumns.DATA,
		YeconMediaFilesColumns.NAME,
		YeconMediaFilesColumns.PARENT,
		YeconMediaFilesColumns.MEDIA_TYPE,
		YeconMediaFilesColumns.PARENT_ID,
		YeconMediaFilesColumns.DAMAGE,
		YeconMediaFilesColumns.TITLE,
		YeconMediaFilesColumns.ALBUM, 
		YeconMediaFilesColumns.ARTIST
	};
	
	/*
	 * 文件数据库查询结果中各列的位置
	 */
	public static final int FILE_COLUMNS_INDEX_ID = 0;
	public static final int FILE_COLUMNS_INDEX_DATA = 1;
	public static final int FILE_COLUMNS_INDEX_NAME = 2;
	public static final int FILE_COLUMNS_INDEX_PARENT = 3;
	public static final int FILE_COLUMNS_INDEX_MEDIA_TYPE = 4;
	public static final int FILE_COLUMNS_INDEX_PARENT_ID = 5;
	public static final int FILE_COLUMNS_INDEX_DAMAGE = 6;
	public static final int FILE_COLUMNS_INDEX_TITLE = 7;
	public static final int FILE_COLUMNS_INDEX_ALBUM = 8;
	public static final int FILE_COLUMNS_INDEX_ARTIST = 9;
	
	/*
	 * 需要从文件夹数据库中查询的列
	 */
	public static final String[] DIR_COLUMNS = new String[] {
		YeconMediaDirColumns._ID,
		YeconMediaDirColumns.DATA, 
		YeconMediaDirColumns.NAME,
		YeconMediaDirColumns.AMOUNT_AUDIO,
		YeconMediaDirColumns.AMOUNT_IMAGE,
		YeconMediaDirColumns.AMOUNT_VIDEO
	};
	
	/*
	 * 文件夹数据库查询结果中各列的位置
	 */
	public static final int DIR_COLUMNS_INDEX_ID = 0;
	public static final int DIR_COLUMNS_INDEX_DATA = 1;
	public static final int DIR_COLUMNS_INDEX_NAME = 2;
	public static final int DIR_COLUMNS_INDEX_AMOUNT_AUDIO = 3;
	public static final int DIR_COLUMNS_INDEX_AMOUNT_IMAGE = 4;
	public static final int DIR_COLUMNS_INDEX_AMOUNT_VIDEO = 5;
	
	/*
	 * 需要从专辑数据库中查询的列
	 */
	public static final String[] ALBUM_COLUMNS = new String[] {
		YeconMediaAlbumColumns._ID,
		YeconMediaAlbumColumns.NAME,
		YeconMediaAlbumColumns.AMOUNT
	};
	
	/*
	 * 专辑数据库查询结果中各列的位置
	 */
	public static final int ALBUM_COLUMNS_INDEX_ID = 0;
	public static final int ALBUM_COLUMNS_INDEX_NAME = 1;
	public static final int ALBUM_COLUMNS_INDEX_AMOUNT = 2;
	
	/*
	 * 需要从艺术家数据库中查询的列
	 */
	public static final String[] ARTIST_COLUMNS = new String[] {
		YeconMediaArtistColumns._ID,
		YeconMediaArtistColumns.NAME,
		YeconMediaArtistColumns.AMOUNT
	};
	
	/*
	 * 艺术家数据库查询结果中各列的位置
	 */
	public static final int ARTIST_COLUMNS_INDEX_ID = 0;
	public static final int ARTIST_COLUMNS_INDEX_NAME = 1;
	public static final int ARTIST_COLUMNS_INDEX_AMOUNT = 2;
	
	// add for Hj ATE 
	public static final String AUTOMATION_MEDIA_BROADCAST_RECV = "AUTOMATION_MEDIA_BROADCAST_RECV";
	public static final String AUTOMATION_MEDIA_BROADCAST_SEND = "AUTOMATION_MEDIA_BROADCAST_SEND";
	
	// ATE广播
	public static final String CKX_ATE_STANDARD_BROADCAST_SEND = "CKX_ATE_STANDARD_BROADCAST_SEND";
	public static final String ATE_KEY_TYPE = "KEY_TYPE";
	public static final String ATE_SUB_KEY_TYPE = "SUB_KEY_TYPE";
	public static final String ATE_PLAY_SOURCE = "playsource";		// 0x01:音频播放，0x02:图片播放，0x03:视频播放
	public static final String ATE_SWITCH_USB = "switchusb";		// 0x00:USB1, 0x01:USB2
	public static final String ATE_PLAY_IMAGE = "visualtest";		// 0x01:/mnt/udisk1/1.png，0x02:/mnt/udisk1/2.png，0x03:/mnt/udisk1/3.png
	
	// 亿连申请解码器资源广播
	public static final String ACTION_EASYCONN_ANDROID_IN = "net.easyconn.android.mirror.in";
	public static final String ACTION_EASYCONN_ANDROID_RESUME = "net.easyconn.screen.resume";
	public static final String ACTION_EASYCONN_IPHONE_IN = "net.easyconn.iphone.inner.mirror.in";
	public static final String ACTION_EASYCONN_IPHONE_RESUME = "net.easyconn.iphone.inner.mirror.resume";
	// carplay申请解码器资源
	public static final String ACTION_CARPLAY_INSERT = "com.jsbd.carplay.insert.notification";
	
	// 语音启动关闭音乐视频图片参数
	public static final String MEDIA_TYPE = "media_type";
	// 语音播放某某歌曲
	public static final String ACTION_IFLY_VOICE_MUSIC = "action.com.carocean.iflyvoice.music";//发送音乐相关处理的广播
	public static final String EXTRA_MUSIC_CMD="cmd";
	public static final String EXTRA_MUSIC_PATH="path";     //歌曲完整路径
    public static final String EXTRA_KWMUSIC_NAME="kw_name";        //歌曲name
    public static final String EXTRA_KWMUSIC_SINGER="kw_singer";    //歌曲singer
    public static final String EXTRA_KWMUSIC_ALBUM="kw_album";      //歌曲album
	public static final int MUSIC_CMD_PLAY_SONG=9;        //播放指定歌曲
    public static final int MUSIC_CMD_PLAY_SONG_BY_KW=11;   //启动酷我播放，给 name, singer或者album
    public static final int MUSIC_CMD_FAVORITE_SONG=12;         //收藏当前歌曲
    public static final int MUSIC_CMD_PLAY_FAVORITE=13;         //播放收藏歌曲
    public static final int MUSIC_CMD_GOTO_FAVORITELIST=14;     //打开收藏歌曲列表
    public static final int MUSIC_CMD_OPEN_MUSICLIST=15;        //打开音乐列表
    public static final int MUSIC_CMD_PLAY_USB1=16;        //播放USB1音乐
    public static final int MUSIC_CMD_PLAY_USB2=17;        //播放USB2音乐
    public static final int MUSIC_CMD_PLAY_LOCAL=18;        //播放本地音乐
    public static final int MUSIC_CMD_PLAY_BTMUSIC=19;        //播放蓝牙音乐
}
