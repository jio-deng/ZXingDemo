package com.dz.zxing.gsydemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.WindowManager;

/**
 * @Description get/set system brightness
 * Created by deng on 2018/10/10.
 */
public class BrightnessUtil {

    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * 获取当前屏幕亮度
     * @param context activity
     * @return brightness of activity
     */
    public static float getCurrentBrightness(Context context) {
        Activity activity = scanForActivity(context);
        if (activity == null) {
            return (float) 0.5;
        }
        return activity.getWindow().getAttributes().screenBrightness;
    }

    /**
     * 设置当前屏幕亮度
     * @param context activity
     * @param newBrightness brightness
     */
    public static void setCurrentBrightness(Context context, float newBrightness) {
        Activity activity = scanForActivity(context);
        if (activity != null) {
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.screenBrightness = newBrightness;
            activity.getWindow().setAttributes(params);
        }
    }
}
