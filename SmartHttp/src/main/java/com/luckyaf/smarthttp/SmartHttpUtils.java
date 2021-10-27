package com.luckyaf.smarthttp;

import android.os.Handler;
import android.os.Looper;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/27
 */
public final class SmartHttpUtils {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    /**
     * 在主线程中执行
     */
    public static void post(Runnable r) {
        HANDLER.post(r);
    }

    /**
     * 延迟一段时间执行
     */
    public static void postDelayed(Runnable r, long delayMillis) {
        HANDLER.postDelayed(r, delayMillis);
    }
}
