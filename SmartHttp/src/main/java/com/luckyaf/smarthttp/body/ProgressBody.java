package com.luckyaf.smarthttp.body;

import android.os.Handler;
import android.os.Looper;

import com.luckyaf.smarthttp.listener.OnUploadListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public class ProgressBody extends RequestBody {

    private final RequestBody mRequestBody;
    private final OnUploadListener<?> mCallback;

    /** 总字节数 */
    private long mTotalByte;

    private volatile AtomicBoolean hasCompleted = new AtomicBoolean(false);
    private int notifyInterval = 100;
    private volatile AtomicLong tempReadSize = new AtomicLong(0);
    private volatile AtomicLong currentSize = new AtomicLong(0);
    private final List<Long> speedBuffer = new ArrayList<>(4);

    /**
     * 主线程的handler
     */
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    public ProgressBody(RequestBody body, OnUploadListener<?> listener) {
        mRequestBody = body;
        mCallback = listener;
    }


    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mTotalByte = contentLength();
        sendProgress();
        sink = Okio.buffer(new WrapperSink(sink));
        mRequestBody.writeTo(sink);
        sink.flush();
    }

    private class WrapperSink extends ForwardingSink {

        public WrapperSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            tempReadSize.getAndAdd(byteCount);
        }


    }
    private void sendProgress() {
        if(hasCompleted.get()){
            return;
        }
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                long speed = tempReadSize.get() * 1000 / notifyInterval;
                speed = bufferSpeed(speed);
                currentSize.getAndAdd(tempReadSize.get());
                tempReadSize.set(0);
                float fraction = currentSize.get() * 1.0f / mTotalByte;
                mCallback.onProgress(currentSize.get(),mTotalByte,fraction,speed);
                if(currentSize.get() == mTotalByte){
                    hasCompleted.set(true);
                }
            }
        });
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendProgress() ;
            }
        },notifyInterval);
    }
    /**
     * 平滑网速，避免抖动过大
     */
    private long bufferSpeed(long speed) {
        speedBuffer.add(speed);
        if (speedBuffer.size() > 3) {
            speedBuffer.remove(0);
        }
        long sum = 0;
        for (float speedTemp : speedBuffer) {
            sum += speedTemp;
        }
        return sum / speedBuffer.size();
    }

}