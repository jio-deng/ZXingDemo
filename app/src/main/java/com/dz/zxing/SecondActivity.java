package com.dz.zxing;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

/**
 * @Description getWidth / getMeasuredWidth
 * Created by deng on 2018/9/9.
 */
public class SecondActivity extends Activity {
    private static final String TAG = "SecondActivity";

    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.d(TAG, "onCreate: ");

        textView = findViewById(R.id.tv_second);

        //1.view.post
//        textView.post(new Runnable() {
//            @Override
//            public void run() {
//                logTextViewWidth();
//            }
//        });

        //2.viewTreeObserver
//        final ViewTreeObserver observer = textView.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                logTextViewWidth();
//                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });

        //3.view.measure
        //具体的数值
//        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
//        textView.measure(widthMeasureSpec, heightMeasureSpec);
//        logTextViewWidth();
        //wrap_content
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        logTextViewWidth();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    //4.onWindowFocusChanged
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            logTextViewWidth();
//        }
//    }

    /**
     * 打印控件的宽和高
     */
    private void logTextViewWidth() {
        if (textView != null) {
            int width = textView.getWidth();
            int height = textView.getHeight();
            Log.d(TAG, "get: width=" + width + ",height=" + height);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight();
            Log.d(TAG, "getMeasured: width=" + measuredWidth + ",height=" + measuredHeight);

        }
    }
}
