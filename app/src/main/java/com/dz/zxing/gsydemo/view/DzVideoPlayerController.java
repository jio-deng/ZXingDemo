package com.dz.zxing.gsydemo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.dz.zxing.gsydemo.utils.BrightnessUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description base class for controlling video play
 * Created by deng on 2018/10/10.
 */
public abstract class DzVideoPlayerController extends FrameLayout implements View.OnTouchListener {
    private Context mContext;
    private IVideoPlayer mVideoPlayer;

    //移动方向判定界限
    private static final int THRESHORD = 80;
    //down触点
    private float mDownX;
    private float mDownY;
    //标记移动改变的属性
    private boolean mNeedChangePosition;
    private boolean mNeedChangeVolume;
    private boolean mNeedChangeBrightness;
    //记录初始数据
    private long mGestureDownPosition;
    private float mGestureDownBrightness;
    private int mGestureDownVolume;
    //up or cancel时的新position
    private long mNewPosition;
    //更新当前显示界面定时器
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;


    public DzVideoPlayerController(@NonNull Context context) {
        super(context);
        mContext = context;
        setOnTouchListener(this);
    }

    public void setVideoPlayer(IVideoPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
    }

    /**
     * 开启更新进度的计时器
     */
    protected void startUpdateTimerTask() {
        cancelUpdateTimerTask();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    updateProgress();
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 1000);
    }

    /**
     * 取消更新进度的计时器
     */
    protected void cancelUpdateTimerTask() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract void updateProgress();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //TODO:全屏时才能拖动位置，改变音量，亮度
        if (!mVideoPlayer.isFullScreen()) {
            return false;
        }

        //TODO:judge state

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mNeedChangePosition = false;
                mNeedChangeVolume = false;
                mNeedChangeBrightness = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    if (absDeltaX >= THRESHORD) {
                        //改变播放进度
                        cancelUpdateTimerTask();
                        mNeedChangePosition = true;
                        mGestureDownPosition = mVideoPlayer.getCurrentPosition();
                    } else if (absDeltaY >= THRESHORD) {
                        if (mDownX < getWidth() * 0.5) {
                            //左侧改变亮度
                            mNeedChangeBrightness = true;
                            mGestureDownBrightness = BrightnessUtil.getCurrentBrightness(mContext);
                        } else {
                            //右侧改变音量
                            mNeedChangeVolume = true;
                            mGestureDownVolume = mVideoPlayer.getVolume();
                        }
                    }
                }
                if (mNeedChangePosition) {
                    //计算当前移动的位置并更新UI
                    long duration = mVideoPlayer.getDuration();
                    long toPosition = (long) (mGestureDownPosition + deltaX / getWidth() * duration);
                    mNewPosition = Math.max(0, Math.min(toPosition, duration));
                    int newPositionProgress = (int) (100f * mNewPosition / duration);
                    showChangePosition(duration, newPositionProgress);
                } else if (mNeedChangeBrightness) {
                    //计算当前亮度，修改后更新UI
                    deltaY = -deltaY;
                    float deltaBrightness = deltaY * 3 / getHeight();
                    float newBrightness = mGestureDownBrightness + deltaBrightness;
                    newBrightness = Math.max(0, Math.min(newBrightness, 1));
                    BrightnessUtil.setCurrentBrightness(mContext, newBrightness);
                    int newBrightnessPercentage = (int) (100f * newBrightness);
                    showChangeBrightness(newBrightnessPercentage);
                } else if (mNeedChangeVolume) {
                    //
                    deltaY = -deltaY;
                    int maxVolume = mVideoPlayer.getMaxVolume();
                    int deltaVolume = (int) (maxVolume * deltaY * 3 / getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    mVideoPlayer.setVolume(newVolume);
                    int newVolumePercentage = (int) (100f * newVolume / maxVolume);
                    showChangeVolume(newVolumePercentage);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mNeedChangePosition) {
                    mVideoPlayer.seekTo(mNewPosition);
                    hideChangePosition();
                    startUpdateTimerTask();
                    return true;
                } else if (mNeedChangeBrightness) {
                    hideChangeBrightness();
                    return true;
                } else if (mNeedChangeVolume) {
                    hideChangeVolume();
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 手势左右滑动改变播放位置时，显示控制器中间的播放位置变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param duration            视频总时长ms
     * @param newPositionProgress 新的位置进度，取值0到100。
     */
    protected abstract void showChangePosition(long duration, int newPositionProgress);

    /**
     * 手势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangePosition();

    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    protected abstract void showChangeVolume(int newVolumeProgress);

    /**
     * 手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangeVolume();

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    protected abstract void showChangeBrightness(int newBrightnessProgress);

    /**
     * 手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangeBrightness();

    /**
     * 当播放器的播放状态发生变化，在此方法中国你更新不同的播放状态的UI
     *
     * @param playState 播放状态
     */
    protected abstract void onPlayStateChanged(int playState);

    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    protected abstract void reset();
}
