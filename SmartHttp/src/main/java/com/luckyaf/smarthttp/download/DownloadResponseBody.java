package com.luckyaf.smarthttp.download;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public class DownloadResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final ReadCallback readListener;
    private int limitSpeed;
    private BufferedSource progressSource;
    private BandWidthLimiter bandWidthLimiter;

    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    public static ResponseBody upgrade(ResponseBody responseBody, int limitSpeed, ReadCallback readListener) {
        return new DownloadResponseBody(responseBody, limitSpeed, readListener);
    }

    public static ResponseBody upgrade(ResponseBody responseBody, ReadCallback readListener) {
        return new DownloadResponseBody(responseBody, -1, readListener);
    }


    private DownloadResponseBody(ResponseBody responseBody, int limitSpeed, ReadCallback readListener) {
        this.responseBody = responseBody;
        this.limitSpeed = limitSpeed;
        this.readListener = readListener;
        if (limitSpeed > 0) {
            bandWidthLimiter = new BandWidthLimiter(limitSpeed);
        }

    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }


    @Override
    public BufferedSource source() {
        if (readListener == null) {
            return responseBody.source();
        }
        readListener.callTotalSize(responseBody.contentLength());
        if(progressSource == null) {
            //SuperInputStream inputStream = new SuperInputStream(responseBody.source().inputStream(), limitSpeed, readListener);
            progressSource = Okio.buffer(source(responseBody.source()));
        }
        return progressSource;
    }


    private Source source(Source source) {
        return new ForwardingSource(source) {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                if (null != bandWidthLimiter) {
                    bandWidthLimiter.limitNextBytes(byteCount);
                }
                long bytesRead = super.read(sink, byteCount);
                if(readListener != null){
                    readListener.read(bytesRead);
                }
                return bytesRead;
            }
        };
    }

    @Override
    public void close() {
        if (progressSource != null) {
            try {
                progressSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}