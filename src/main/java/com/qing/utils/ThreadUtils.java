package com.qing.utils;

/**
 * Created by zwq on 2015/09/22 13:59.<br/><br/>
 * 启动线程时调用start方法
 */
public abstract class ThreadUtils implements Runnable {

    private static final String TAG = ThreadUtils.class.getName();
    private Thread mThread;
    private boolean isRunning;

    public ThreadUtils(){
        if (mThread == null){
            mThread = new Thread(this);
        }
    }

    public abstract void execute();

    public final boolean isRunning() {
        return isRunning;
    }

    public final synchronized void start(){
        if (mThread != null){
            mThread.start();
        }
    }

    @Override
    public final void run() {
        isRunning = true;
        execute();
        finish();
    }

    public void finish() {
        isRunning = false;
    }
}
