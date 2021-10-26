package com.luckyaf.smarthttp.listener;

import okhttp3.Call;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public interface OnHttpListener<T> {

    /**
     * 请求开始
     */
    default void onStart(Call call) {}

    /**
     * 请求成功
     *
     * @param cache         是否是通过缓存请求成功的
     */
    default void onSucceed(T result, boolean cache) {
        onSucceed(result);
    }
    /**
     * 请求成功
     */
    void onSucceed(T result);

    /**
     * 请求出错
     */
    void onFail(Exception e);

    /**
     * 请求结束
     */
    default void onEnd(Call call) {

    }
}