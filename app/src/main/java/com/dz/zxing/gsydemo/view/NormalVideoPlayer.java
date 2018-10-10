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
public class NormalVideoPlayer extends FrameLayout {
    private Context mContext;
    private FrameLayout mContainer;

    private String mUrl;
    private Map<String, String> mHeaders;
    private int videoWidth;
    private int videoHeight;

    private MediaPlayer mediaPlayer;
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
    public void setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    public void start() {
        initMediaPlayer();
        initTextureView();
        addTextureView();
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoWidth = mp.getVideoWidth();
                    videoHeight = mp.getVideoHeight();
                    textureView.setVideoWidthAndHeight(videoWidth, videoHeight);
                    mp.start();
                }
            });
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {

                }
            });
        }
    }

    private void initTextureView() {
        if (textureView == null) {
            textureView = new DzTextureView(mContext);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    try {
                        mediaPlayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
                        mediaPlayer.setSurface(new Surface(surface));
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    private void addTextureView() {
        mContainer.removeView(textureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.addView(textureView, 0,  params);
    }

}
