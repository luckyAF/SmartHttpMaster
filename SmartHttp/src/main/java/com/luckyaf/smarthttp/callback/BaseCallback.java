package com.luckyaf.smarthttp.callback;

import com.luckyaf.smarthttp.SmartHttpConfig;
import com.luckyaf.smarthttp.SmartHttpUtils;
import com.luckyaf.smarthttp.model.CallProxy;
import com.luckyaf.smarthttp.request.CommonRequest;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public abstract class BaseCallback implements Callback {

    /** 请求配置 */
    private final CommonRequest mBaseRequest;

    /** 请求任务对象 */
    private CallProxy mCall;

    /** 当前重试次数 */
    private int mRetryCount;

    public BaseCallback(CommonRequest request) {
        mBaseRequest = request;
    }

    public BaseCallback setCall(CallProxy call) {
        mCall = call;
        return this;
    }

    public void start() {
        mCall.enqueue(this);
        onStart(mCall);
    }

    protected CallProxy getCall() {
        return mCall;
    }

    @Override
    public void onResponse(Call call, Response response) {
        try {
            // 收到响应
            onResponse(response);
        } catch (Exception e) {
            // 回调失败
            onFailure(e);
        } finally {
            // 关闭响应
            response.close();
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        // 服务器请求超时重试
        if (e instanceof SocketTimeoutException && mRetryCount < SmartHttpConfig.Companion.getRetryCount()) {
            // 设置延迟 N 秒后重试该请求
            SmartHttpUtils.postDelayed(() -> {
                mRetryCount++;
                Call newCall = call.clone();
                mCall.setCall(newCall);
                newCall.enqueue(BaseCallback.this);
            }, SmartHttpConfig.Companion.getRetryTime());

            return;
        }
        onFailure(e);
    }

    /**
     * 请求开始
     */
    protected abstract void onStart(Call call);

    /**
     * 请求成功
     */
    protected abstract void onResponse(Response response) throws Exception;

    /**
     * 请求失败
     */
    protected abstract void onFailure(Exception e);
}