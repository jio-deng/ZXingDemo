package com.dz.zxing.gsydemo.controller;

/**
 * @Description interface : control video player
 * Created by deng on 2018/10/9.
 */
public interface NormalVideoPlayerControl {
    void start();
    void restart();
    void pause();
    void seekTo(int pos);

    boolean isIdle();
    boolean isPreparing();
    boolean isPrepared();
    boolean isBufferingPlaying();
    boolean isBufferingPaused();
    boolean isPlaying();
    boolean isPaused();
    boolean isError();
    boolean isCompleted();

    boolean isFullScreen();
    boolean isTinyWindow();
    boolean isNormal();

    int getDuration();
    int getCurrentPosition();
    int getBufferPercentage();

    void enterFullScreen();
    boolean exitFullScreen();
    void enterTinyWindow();
    boolean exitTinyWindow();

    void release();
}
