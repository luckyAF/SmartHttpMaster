package com.luckyaf.smarthttp.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public class DownloadInterceptor implements Interceptor {

    private ReadCallback callback;
    private int speedLimit = -1;

    public DownloadInterceptor(int limitSpeed, ReadCallback readListener){
        this.callback = readListener;
        this.speedLimit = limitSpeed;
    }
    public DownloadInterceptor(ReadCallback readListener){
        this.callback = readListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拦截
        Response originalResponse = chain.proceed(chain.request());
        //包装响应体并返回
        return originalResponse.newBuilder()
                .body(DownloadResponseBody.upgrade(originalResponse.body(),speedLimit, callback))
                .build();
    }
}
