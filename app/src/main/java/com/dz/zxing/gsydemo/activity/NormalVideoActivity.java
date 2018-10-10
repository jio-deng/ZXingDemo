package com.dz.zxing.gsydemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dz.zxing.R;
import com.dz.zxing.gsydemo.view.NormalVideoPlayer;

/**
 * @Description normal video activity
 * Created by deng on 2018/10/9.
 */
public class NormalVideoActivity extends Activity {
    private NormalVideoPlayer mTexNormalVideo;

    private String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_video);

        mTexNormalVideo = findViewById(R.id.texture_normal_video);
        mTexNormalVideo.setUp(source1, null);
        mTexNormalVideo.start();
    }

}
