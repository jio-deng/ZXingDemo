package com.dz.zxing.gsydemo.utils;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * @Description measure view size
 * Created by deng on 2018/10/10.
 */
public class MeasureUtil {
    private WeakReference<View> mView;

    private int videoWidth;
    private int videoHeight;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    private MeasureFormVideoParamsListener listener;

    public MeasureUtil(View view, MeasureFormVideoParamsListener listener) {
        mView = new WeakReference<>(view);
        this.listener = listener;
    }

    /**
     * measure width and height based on spec
     * @param widthMeasureSpec w
     * @param heightMeasureSpec h
     */
    public void prepareMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (listener != null) {
            int width = listener.getCurrentVideoWidth();
            int height = listener.getCurrentVideoHeight();
            if (width > 0 && height > 0) {
                videoWidth = width;
                videoHeight = height;
            }
            doMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Must be called by View.onMeasure(int, int)
     *
     * @param widthMeasureSpec widthMeasureSpec
     * @param heightMeasureSpec heightMeasureSpec
     */
    private void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(videoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(videoHeight, heightMeasureSpec);

        if (videoWidth > 0 && videoHeight > 0) {
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
            //display ratio VS spec ratio
            float displayRatio = videoWidth / videoHeight;
            float specRatio = widthSpecSize / heightSpecSize;
            if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
                if (displayRatio > specRatio) {
                    width = widthSpecSize;
                    height = (int) (width / displayRatio);
                } else {
                    height = heightSpecSize;
                    width = (int) (height * displayRatio);
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = heightSpecSize;
                if (displayRatio > specRatio) {
                    height = width * videoHeight / videoWidth;
                } else {
                    width = height * videoWidth / videoHeight;
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }

        }

        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    /**
     * 构造宽高所需要的视频相关参数
     */
    public interface MeasureFormVideoParamsListener {
        int getCurrentVideoWidth();

        int getCurrentVideoHeight();
    }
}
