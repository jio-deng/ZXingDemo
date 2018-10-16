package com.dz.zxing.gsydemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Map;

/**
 * @Description media player for normal video activity
 * Created by deng on 2018/10/9.
 */
public class NormalVideoPlayer extends FrameLayout implements IVideoPlayer, TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = "NormalVideoPlayer";

    /**
     * 播放错误
     **/
    public static final int STATE_ERROR = -1;
    /**
     * 播放未开始
     **/
    public static final int STATE_IDLE = 0;
    /**
     * 播放准备中
     **/
    public static final int STATE_PREPARING = 1;
    /**
     * 播放准备就绪
     **/
    public static final int STATE_PREPARED = 2;
    /**
     * 正在播放
     **/
    public static final int STATE_PLAYING = 3;
    /**
     * 暂停播放
     **/
    public static final int STATE_PAUSED = 4;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    public static final int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     **/
    public static final int STATE_BUFFERING_PAUSED = 6;
    /**
     * 播放完成
     **/
    public static final int STATE_COMPLETED = 7;
    /**
     * 普通模式
     **/
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    public static final int MODE_FULL_SCREEN = 11;

    private int mCurrentState = STATE_IDLE;
    private int mCurrentMode = MODE_NORMAL;

    private Context mContext;
    private FrameLayout mContainer;

    private String mUrl;
    private Map<String, String> mHeaders;
    private int videoWidth;
    private int videoHeight;
    private int mBufferPercentage;
    private long skipPosition;//从何处开始播放

    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private DzVideoPlayerController videoPlayerController;
    private DzTextureView textureView;

    public NormalVideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public NormalVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mContainer, params);
    }

    /**
     * 设置视频url和headers
     * @param url url
     * @param headers headers
     */
    @Override
    public void setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    /**
     * 设置视频播放控制界面
     * @param videoPlayerController controller
     */
    public void setController(DzVideoPlayerController videoPlayerController) {
        mContainer.removeView(this.videoPlayerController);
        this.videoPlayerController = videoPlayerController;
        this.videoPlayerController.reset();
        this.videoPlayerController.setVideoPlayer(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(videoPlayerController, params);
    }

    @Override
    public void start() {
        if (mCurrentState == STATE_IDLE) {
            initAudioManager();
            initMediaPlayer();
            initTextureView();
            addTextureView();
        } else {
            Log.e(TAG, "start: can not start at state:" + mCurrentState);
        }
    }

    @Override
    public void start(long position) {
        skipPosition = position;
        start();
    }

    @Override
    public void restart() {
        if (mCurrentState == STATE_PAUSED) {
            mediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            videoPlayerController.onPlayStateChanged(mCurrentState);
//            LogUtil.d("STATE_PLAYING");
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            videoPlayerController.onPlayStateChanged(mCurrentState);
//            LogUtil.d("STATE_BUFFERING_PLAYING");
        } else if (mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR) {
            mediaPlayer.reset();
            openMediaPlayer();
        } else {
//            LogUtil.d("NiceVideoPlayer在mCurrentState == " + mCurrentState + "时不能调用restart()方法.");
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            videoPlayerController.onPlayStateChanged(mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            videoPlayerController.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public void seekTo(long pos) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) pos);
        }
    }

    @Override
    public void setVolume(int volume) {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
    }

    /** 获取播放器状态start */

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public boolean isTinyWindow() {
        return false;
    }

    @Override
    public boolean isNormal() {
        return false;
    }

    /** 获取播放器状态end */

    @Override
    public int getMaxVolume() {
        if (mAudioManager != null) {
            mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    @Override
    public int getVolume() {
        if (mAudioManager != null) {
            mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    @Override
    public long getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    @Override
    public long getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void enterFullScreen() {

    }

    @Override
    public boolean exitFullScreen() {
        return false;
    }

    @Override
    public void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mContainer.removeView(textureView);


        mCurrentState = STATE_IDLE;
    }

    @Override
    public void release() {
        // 保存播放位置 todo
//        if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
//            NiceUtil.savePlayPosition(mContext, mUrl, getCurrentPosition());
//        } else if (isCompleted()) {
//            NiceUtil.savePlayPosition(mContext, mUrl, 0);
//        }
        // 退出全屏或小窗口
        if (isFullScreen()) {
            exitFullScreen();
        }
        if (isTinyWindow()) {
//todo            exitTinyWindow();
        }
        mCurrentMode = MODE_NORMAL;

        // 释放播放器
        releasePlayer();

        // 恢复控制器
        if (videoPlayerController != null) {
            videoPlayerController.reset();
        }
        Runtime.getRuntime().gc();
    }

    /**
     * 初始化AudioManager，用于音量调节、焦点获取和监听
     */
    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    /**
     * init media player
     */
    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
        }
    }

    /**
     * init TextureView : draw
     */
    private void initTextureView() {
        if (textureView == null) {
            textureView = new DzTextureView(mContext);
            textureView.setSurfaceTextureListener(this);
        }
    }

    /**
     * add texture into layout
     */
    private void addTextureView() {
        mContainer.removeView(textureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.addView(textureView, 0,  params);
    }

    /** SurfaceTextureListener */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            openMediaPlayer();
        } else {
            textureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     * 打开MediaPlayer
     */
    private void openMediaPlayer() {
        //屏幕常量
        mContainer.setKeepScreenOn(true);
        //设置监听
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);

        try {
            mediaPlayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mediaPlayer.setSurface(mSurface);
            mediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            videoPlayerController.onPlayStateChanged(mCurrentState);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "openMediaPlayer: 打开播放器错误！");
        }
    }

    /** listener for media player */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferPercentage = percent;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = STATE_COMPLETED;
        videoPlayerController.onPlayStateChanged(mCurrentState);
        mContainer.setKeepScreenOn(false);
        Log.d(TAG, "onCompletion");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = STATE_ERROR;
        videoPlayerController.onPlayStateChanged(mCurrentState);
        Log.e(TAG, "onError: what=" + what + ", extra=" + extra);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            //播放器开始渲染
            mCurrentState = STATE_PLAYING;
            videoPlayerController.onPlayStateChanged(mCurrentState);
            Log.d(TAG, "onInfo: MEDIA_INFO_VIDEO_RENDERING_START");
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            //MediaPlayer暂时不播放，以缓冲更多数据
            if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_BUFFERING_PAUSED;
            } else {
                mCurrentState = STATE_BUFFERING_PLAYING;
            }
            videoPlayerController.onPlayStateChanged(mCurrentState);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            //缓冲填充完毕
            if (mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_PAUSED;
                videoPlayerController.onPlayStateChanged(mCurrentState);
                Log.d(TAG, "onInfo: MEDIA_INFO_BUFFERING_END, STATE_BUFFERING_PAUSED->STATE_PAUSED");
            } else if (mCurrentState == STATE_BUFFERING_PLAYING) {
                mCurrentState = STATE_PLAYING;
                videoPlayerController.onPlayStateChanged(mCurrentState);
                Log.d(TAG, "onInfo: MEDIA_INFO_BUFFERING_END, STATE_BUFFERING_PLAYING->STATE_PLAYING");
            }
        } else {
            Log.d(TAG, "onInfo: what = " + what);
        }
        //TODO:rotation noseekto


        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        textureView.setVideoWidthAndHeight(videoWidth, videoHeight);

        mCurrentState = STATE_PREPARED;
        videoPlayerController.onPlayStateChanged(mCurrentState);
        mp.start();
        // 从上次的保存位置播放
//        if (continueFromLastPosition) {
//            long savedPlayPosition = NiceUtil.getSavedPlayPosition(mContext, mUrl);
//            mp.seekTo(savedPlayPosition);
//        }
        // 跳到指定位置播放
        if (skipPosition != 0) {
            mp.seekTo((int) skipPosition);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        textureView.adaptVideoSize(width, height);
        Log.d(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
    }
}
