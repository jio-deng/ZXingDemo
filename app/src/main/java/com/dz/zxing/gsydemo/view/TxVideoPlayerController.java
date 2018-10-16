package com.dz.zxing.gsydemo.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dz.zxing.R;
import com.dz.zxing.gsydemo.utils.DzUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Description 仿腾讯播放界面
 * copy from : https://www.jianshu.com/p/420f7b14d6f6
 * Created by deng on 2018/10/15.
 */
public class TxVideoPlayerController extends DzVideoPlayerController
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Context mContext;

    //底图
    private ImageView mIvDefault;
    //加载动画
    private LinearLayout mLlLoading;
    private TextView mTvLoading;
    //改变播放位置
    private LinearLayout mLlChangePosition;
    private TextView mTvChangePosition;
    private ProgressBar mPbChangePosition;
    //改变亮度
    private LinearLayout mLlChangeBrightness;
    private ProgressBar mPbChangeBrightness;
    //改变声音
    private LinearLayout mLlChangeVolume;
    private ProgressBar mPbChangeVolume;
    //播放完毕显示页面
    private LinearLayout mLlComplete;
    private TextView mTvReplay;
    //播放错误显示页面
    private LinearLayout mLlError;
    private TextView mTvError;
    //顶部控制区
    private LinearLayout mLlTop;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private LinearLayout mLlBattery;
    private ImageView mIvBattery;
    private TextView mTvBatteryTime;
    //底部控制区
    private LinearLayout mLlBottom;
    private ImageView mIvStartOrPause;
    private TextView mTvPosition;
    private TextView mTvDuration;
    private SeekBar mSeekBar;
    private TextView mTvClarity;
    private ImageView mIvFullScreen;
    //初始显示时长和中间播放键
    private TextView mTvLength;
    private ImageView mIvCenterStart;

    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;


    public TxVideoPlayerController(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_controller_tengxun, null, false);

        mIvDefault = view.findViewById(R.id.image_default);

        mLlLoading = view.findViewById(R.id.loading);
        mTvLoading = view.findViewById(R.id.load_text);

        mLlChangePosition = view.findViewById(R.id.change_position);
        mTvChangePosition = view.findViewById(R.id.change_position_current);
        mPbChangePosition = view.findViewById(R.id.change_position_progress);

        mLlChangeBrightness = view.findViewById(R.id.change_brightness);
        mPbChangeBrightness = view.findViewById(R.id.change_brightness_progress);

        mLlChangeVolume = view.findViewById(R.id.change_volume);
        mPbChangeVolume = view.findViewById(R.id.change_volume_progress);

        mLlComplete = view.findViewById(R.id.completed);
        mTvReplay = view.findViewById(R.id.replay);

        mLlError = view.findViewById(R.id.error);
        mTvError = view.findViewById(R.id.retry);

        mLlTop = view.findViewById(R.id.top);
        mIvBack = view.findViewById(R.id.back);
        mTvTitle = view.findViewById(R.id.title);

        mLlBattery = view.findViewById(R.id.battery_time);
        mIvBattery = view.findViewById(R.id.battery);
        mTvBatteryTime = view.findViewById(R.id.time);

        mLlBottom = view.findViewById(R.id.bottom);
        mIvStartOrPause = view.findViewById(R.id.restart_or_pause);
        mTvPosition = view.findViewById(R.id.position);
        mTvDuration = view.findViewById(R.id.duration);
        mSeekBar = view.findViewById(R.id.seek);
        mTvClarity = view.findViewById(R.id.clarity);
        mIvFullScreen = view.findViewById(R.id.full_screen);

        mTvLength = view.findViewById(R.id.length);
        mIvCenterStart = view.findViewById(R.id.center_start);

        mIvCenterStart.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvStartOrPause.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
        mTvClarity.setOnClickListener(this);
        mTvError.setOnClickListener(this);
        mTvReplay.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
    }

    /**
     * 设置标题
     * @param title 视频标题
     */
    @Override
    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    /**
     * 设置底图
     * @param resId 视频底图资源
     */
    @Override
    public void setImage(int resId) {
        mIvDefault.setImageResource(resId);
    }

    @Override
    public ImageView imageView() {
        return mIvDefault;
    }

    @Override
    public void setLenght(long length) {
        mTvLength.setText(DzUtil.formatTime(length));
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                mIvBattery.setImageResource(R.drawable.battery_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                mIvBattery.setImageResource(R.drawable.battery_full);
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (percentage <= 10) {
                    mIvBattery.setImageResource(R.drawable.battery_10);
                } else if (percentage <= 20) {
                    mIvBattery.setImageResource(R.drawable.battery_20);
                } else if (percentage <= 50) {
                    mIvBattery.setImageResource(R.drawable.battery_50);
                } else if (percentage <= 80) {
                    mIvBattery.setImageResource(R.drawable.battery_80);
                } else if (percentage <= 100) {
                    mIvBattery.setImageResource(R.drawable.battery_100);
                }
            }
        }
    };

    @Override
    protected void updateProgress() {
        long position = mVideoPlayer.getCurrentPosition();
        long duration = mVideoPlayer.getDuration();
        int bufferPercentage = mVideoPlayer.getBufferPercentage();
        mSeekBar.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeekBar.setProgress(progress);
        mTvPosition.setText(DzUtil.formatTime(position));
        mTvDuration.setText(DzUtil.formatTime(duration));
        // 更新时间
        mTvBatteryTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        switch (playState) {
            case NormalVideoPlayer.STATE_IDLE:
                break;
            case NormalVideoPlayer.STATE_PREPARING:
                mIvDefault.setVisibility(View.GONE);
                mLlLoading.setVisibility(View.VISIBLE);
                mTvLoading.setText("正在准备...");
                mLlError.setVisibility(View.GONE);
                mLlComplete.setVisibility(View.GONE);
                mLlTop.setVisibility(View.GONE);
                mLlBottom.setVisibility(View.GONE);
                mIvCenterStart.setVisibility(View.GONE);
                mTvLength.setVisibility(View.GONE);
                break;
            case NormalVideoPlayer.STATE_PREPARED:
                startUpdateTimerTask();
                break;
            case NormalVideoPlayer.STATE_PLAYING:
                mLlLoading.setVisibility(View.GONE);
                mIvStartOrPause.setImageResource(R.drawable.ic_player_pause);
                startDismissTopBottomTimer();
                break;
            case NormalVideoPlayer.STATE_PAUSED:
                mLlLoading.setVisibility(View.GONE);
                mIvStartOrPause.setImageResource(R.drawable.ic_player_start);
                cancelDismissTopBottomTimer();
                break;
            case NormalVideoPlayer.STATE_BUFFERING_PLAYING:
                mLlLoading.setVisibility(View.VISIBLE);
                mIvStartOrPause.setImageResource(R.drawable.ic_player_pause);
                mTvLoading.setText("正在缓冲...");
                startDismissTopBottomTimer();
                break;
            case NormalVideoPlayer.STATE_BUFFERING_PAUSED:
                mLlLoading.setVisibility(View.VISIBLE);
                mIvStartOrPause.setImageResource(R.drawable.ic_player_start);
                mTvLoading.setText("正在缓冲...");
                cancelDismissTopBottomTimer();
                break;
            case NormalVideoPlayer.STATE_ERROR:
                cancelUpdateTimerTask();
                setTopBottomVisible(false);
                mLlTop.setVisibility(View.VISIBLE);
                mLlError.setVisibility(View.VISIBLE);
                break;
            case NormalVideoPlayer.STATE_COMPLETED:
                cancelUpdateTimerTask();
                setTopBottomVisible(false);
                mIvDefault.setVisibility(View.VISIBLE);
                mLlComplete.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void reset() {
        topBottomVisible = false;
        cancelUpdateTimerTask();
        cancelDismissTopBottomTimer();
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);

        mIvCenterStart.setVisibility(View.VISIBLE);
        mIvDefault.setVisibility(View.VISIBLE);

        mLlBottom.setVisibility(View.GONE);
        mIvFullScreen.setImageResource(R.drawable.ic_player_enlarge);

        mTvLength.setVisibility(View.VISIBLE);

        mLlTop.setVisibility(View.VISIBLE);
        mIvBack.setVisibility(View.GONE);

        mLlLoading.setVisibility(View.GONE);
        mLlError.setVisibility(View.GONE);
        mLlComplete.setVisibility(View.GONE);
    }

    @Override
    protected void onPlayModeChanged(int playMode) {
        //todo:mode change
    }

    /**
     * 点击事件
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.center_start:
                if (mVideoPlayer != null) {
                    mVideoPlayer.start();
                }
                break;
            case R.id.back:
                if (mVideoPlayer.isFullScreen()) {
                    mVideoPlayer.exitFullScreen();
                } else if (mVideoPlayer.isTinyWindow()) {
//                    mVideoPlayer.exitTinyWindow();
                }
                break;
            case R.id.restart_or_pause:
                if (mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying()) {
                    mVideoPlayer.pause();
                } else if (mVideoPlayer.isPaused() || mVideoPlayer.isBufferingPaused()) {
                    mVideoPlayer.restart();
                }
                break;
            case R.id.full_screen:
                if (mVideoPlayer.isNormal() || mVideoPlayer.isTinyWindow()) {
                    mVideoPlayer.enterFullScreen();
                } else if (mVideoPlayer.isFullScreen()) {
                    mVideoPlayer.exitFullScreen();
                }
                break;
            case R.id.clarity:
                //todo
                break;
            case R.id.retry:
            case R.id.replay:
                mVideoPlayer.restart();
                break;
            default:
                if (mVideoPlayer.isPlaying()
                        || mVideoPlayer.isPaused()
                        || mVideoPlayer.isBufferingPlaying()
                        || mVideoPlayer.isBufferingPaused()) {
                    setTopBottomVisible(!topBottomVisible);
                }
                break;
        }
    }

    /** seek bar listener */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mVideoPlayer.isBufferingPaused() || mVideoPlayer.isPaused()) {
            mVideoPlayer.restart();
        }
        long position = (long) (mVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mVideoPlayer.seekTo(position);

    }

    /** DzBideoPlayerController->onTouch中调用的UI控制 */
    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
        mLlChangePosition.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mTvChangePosition.setText(DzUtil.formatTime(newPosition));
        mPbChangePosition.setProgress(newPositionProgress);
        mSeekBar.setProgress(newPositionProgress);
        mTvPosition.setText(DzUtil.formatTime(newPosition));
    }

    @Override
    protected void hideChangePosition() {
        mLlChangePosition.setVisibility(View.GONE);
    }

    @Override
    protected void showChangeVolume(int newVolumeProgress) {
        mLlChangeVolume.setVisibility(View.VISIBLE);
        mPbChangeVolume.setProgress(newVolumeProgress);
    }

    @Override
    protected void hideChangeVolume() {
        mLlChangeVolume.setVisibility(View.GONE);
    }

    @Override
    protected void showChangeBrightness(int newBrightnessProgress) {
        mLlChangeBrightness.setVisibility(View.VISIBLE);
        mPbChangeBrightness.setProgress(newBrightnessProgress);
    }

    @Override
    protected void hideChangeBrightness() {
        mLlChangeBrightness.setVisibility(View.GONE);
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        mLlTop.setVisibility(visible ? View.VISIBLE : View.GONE);
        mLlBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        topBottomVisible = visible;
        if (visible) {
            if (!mVideoPlayer.isPaused() && !mVideoPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }
}
