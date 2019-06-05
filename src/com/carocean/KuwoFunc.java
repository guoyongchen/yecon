package com.carocean;

import com.carocean.utils.Utils;

import android.util.Log;

import cn.kuwo.autosdk.api.PlayMode;
import cn.kuwo.base.bean.Music;

public class KuwoFunc {
    private final static String TAG = "KuwoFunc";
    /**
     * 打开酷我
     * @param autoPlay 是否自动播放
     */
    public static void startKuwo(boolean autoPlay) {
        Utils.getKwapi().startAPP(autoPlay);
    }
    /**
     * 退出酷我
     */
    public static void exitKuwo() {
        Log.i(TAG, "kuwo---exitKuwo");
        Utils.getKwapi().exitAPP();
    }

    /**
     * 设置播放模式
     * @param mode
     */
    public static void setPlayMode(PlayMode mode) {
        //PlayMode.MODE_ALL_CIRCLE  循环
        //PlayMode.MODE_ALL_ORDER   顺序
        //PlayMode.MODE_ALL_RANDOM   随机
        //PlayMode.MODE_SINGLE_CIRCLE  单曲循环
    	Utils.getKwapi().setPlayMode(mode);
    }

    /**
     * 得到当前播放歌曲的信息，例如歌曲名
     * @return
     */
    public static Music getCurPlayingMusicInfo() {
        return Utils.getKwapi().getNowPlayingMusic();
    }

    /**
     * 获取当前播放歌曲时间总长度
     * @return
     */
    public static int getCurPlayingMusicTotaltime() {
        return Utils.getKwapi().getCurrentMusicDuration();
    }

    /**
     * 得到当前播放歌曲的当前时间
     * @return
     */
    public static int getCurPlayingMusicCurtime() {
        return Utils.getKwapi().getCurrentPos();
    }
}
