package com.dz.zxing.gsydemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.dz.zxing.R;
import com.dz.zxing.gsydemo.view.NormalVideoPlayer;
import com.dz.zxing.gsydemo.view.TxVideoPlayerController;

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
        TxVideoPlayerController controller = new TxVideoPlayerController(this);
        controller.setTitle("This is Dz testing.");
        controller.setLenght(120000);//todo
        Glide.with(this)
                .load("http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg")
                .placeholder(R.drawable.ic_launcher_background)
                .crossFade()
                .into(controller.imageView());
        mTexNormalVideo.setController(controller);
    }

}
