package com.luckyaf.smarthttp.listener;

import java.io.File;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public interface OnUploadListener<T> extends OnHttpListener<T>{
    /**
     * 上传进度改变
     *
     * @param progress              下载进度值（0-100）
     */
    default void onProgress(long uploadByte, long totalByte, float progress, long speed){
    }



}
