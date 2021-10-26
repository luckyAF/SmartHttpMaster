package com.luckyaf.smarthttp.download;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public interface ReadCallback {
    void callTotalSize(long size);
    void read(long size);
}