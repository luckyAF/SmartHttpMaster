package com.luckyaf.smarthttp.model;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public final class CallProxy implements Call {

    private Call mCall;

    public CallProxy(Call call) {
        mCall = call;
    }

    public void setCall(Call call) {
        mCall = call;
    }

    @Override
    public Request request() {
        if (mCall == null) {
            return null;
        }
        return mCall.request();
    }

    @Override
    public Response execute() throws IOException {
        if (mCall == null) {
            return null;
        }
        return mCall.execute();
    }

    @Override
    public void enqueue(Callback responseCallback) {
        if (mCall == null) {
            return;
        }
        mCall.enqueue(responseCallback);
    }

    @Override
    public void cancel() {
        if (mCall == null) {
            return;
        }
        mCall.cancel();
    }

    @Override
    public boolean isExecuted() {
        if (mCall == null) {
            return false;
        }
        return mCall.isExecuted();
    }

    @Override
    public boolean isCanceled() {
        if (mCall == null) {
            return false;
        }
        return mCall.isCanceled();
    }

    @Override
    public Timeout timeout() {
        if (mCall == null) {
            return null;
        }
        return mCall.timeout();
    }

    @Override
    public Call clone() {
        if (mCall == null) {
            return null;
        }
        return mCall.clone();
    }
}