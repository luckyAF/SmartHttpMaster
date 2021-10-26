package com.luckyaf.smarthttp.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
public final class AppExecutors {
    private static int corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;


    private static Executor backgroundIO = new ThreadPoolExecutor(
            1,
            3,
            5000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new NameCustomizedThreadFactory("backgroundIO"));

    private static Executor diskIO = new ThreadPoolExecutor(
            1,
            3,
            5000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new NameCustomizedThreadFactory("diskIO"));
    private static Executor networkThread = new ThreadPoolExecutor(
            3,
            8,
            10000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new NameCustomizedThreadFactory("diskIO"));
    private static Executor mainThread =new  MainThreadExecutor();

    public AppExecutors() {

    }


    public void runOnBackgroundIO(Runnable command){
        backgroundIO.execute(command);
    }
    public void runOnDiskIoThread(Runnable command){
        diskIO.execute(command);
    }

    public void runOnNetIoThread(Runnable command){
        networkThread.execute(command);
    }
    public void runOnMainThread(Runnable command){
        mainThread.execute(command);
    }



    static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }

}
