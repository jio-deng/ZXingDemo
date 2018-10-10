package com.dz.zxing.gsydemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.dz.zxing.gsydemo.utils.MeasureUtil;

/**
 * @Description 自定义TextureView
 * Created by deng on 2018/10/10.
 */
public class DzTextureView extends TextureView implements MeasureUtil.MeasureFormVideoParamsListener {
    private MeasureUtil measureUtil;

    private int videoWidth;  //video current width
    private int videoHeight; //video current height

    public DzTextureView(Context context) {
        super(context);
        init();
    }

    public DzTextureView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    /**
     * init measure util
     */
    private void init() {
        measureUtil = new MeasureUtil(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureUtil.prepareMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureUtil.getMeasuredWidth(), measureUtil.getMeasuredHeight());
    }

    /**
     * set when media player is prepared
     * @param videoWidth w
     * @param videoHeight h
     */
    public void setVideoWidthAndHeight(int videoWidth, int videoHeight) {
        if (videoWidth != this.videoWidth || videoHeight != this.videoHeight) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            requestLayout();
        }
    }

    @Override
    public int getCurrentVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getCurrentVideoHeight() {
        return videoHeight;
    }
}
