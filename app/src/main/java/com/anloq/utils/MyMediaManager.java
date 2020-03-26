package com.anloq.utils;

import android.media.MediaPlayer;

import com.anloq.application.MyApplication;

/**
 * Created by xpf on 2017/6/25 :)
 * Function:开锁音效播放的管理者
 */

public class MyMediaManager {

    private MediaPlayer mMediaPlayer;  // 手机媒体播放器对象
    private static MyMediaManager instance = new MyMediaManager();

    private MyMediaManager() {
    }

    public static MyMediaManager getInstance() {
        return instance;
    }

    /**
     * 0 :开门失败 1：已开门
     *
     * @param ringID
     */
    public void playAudio(int ringID) {
        if (mMediaPlayer != null) {
            stopAudio();//停止铃声播放
        }
        try {
            //下面是调用振动的三行代码/之后调用的是铃声
            mMediaPlayer = MediaPlayer.create(MyApplication.getmContext(), ringID);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
            /*mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });*/
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 铃声关闭
     */
    public void stopAudio() {
        // 此处必须要进行判空！！！
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();//暂停
            }
            mMediaPlayer.release();//释放掉资源
            mMediaPlayer = null;
        }
    }
}
