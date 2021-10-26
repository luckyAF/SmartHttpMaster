package com.luckyaf.smarthttp.listener;

import java.io.File;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public interface OnDownloadListener {

    /**
     * 下载开始
     */
    void onStart(File file);

    /**
     * 下载进度改变
     *
     * @param progress              下载进度值（0-100）
     */
    default void onProgress(File file,long downloadByte,long totalByte,  float progress,long speed){
    }
    /**
     * 下载完成
     */
    void onComplete(File file);

    /**
     * 下载出错
     */
    void onError(File file, Exception e);

    /**
     * 下载结束
     */
    void onEnd(File file);
}