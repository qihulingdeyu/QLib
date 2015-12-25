package com.qing.utils;

/**
 * Created by zwq on 2015/09/22 13:59.<br/><br/>
 * 启动线程时调用start方法
 */
public abstract class ThreadUtil implements Runnable {

    private static final String TAG = ThreadUtil.class.getName();
    private volatile Thread mThread;
    private boolean isRunning;
    private long sleepTime;

    public ThreadUtil(){
        if (mThread == null){
            mThread = new Thread(this);
        }
    }

    public abstract void execute();

    public Thread getThread() {
        return mThread;
    }

    public boolean isAlive() {
        if (mThread != null){
            return mThread.isAlive();
        }
        return false;
    }

    public boolean isInterrupted() {
        if (mThread != null){
            return mThread.isInterrupted();
        }
        return false;
    }

    public void interrupt() {
        if (mThread != null){
            mThread.interrupt();
        }
    }

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
        if (isRunning())
            execute();
        if (isRunning())
            finish();
    }

    public void stop() {
        isRunning = false;
    }

    public void finish() {
        isRunning = false;
    }

    public void clearAll() {
        if (isAlive()){
            mThread.interrupt();
        }
        mThread = null;
        isRunning = false;
    }
}
