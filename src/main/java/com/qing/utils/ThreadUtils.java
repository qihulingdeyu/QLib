package com.qing.utils;

/**
 * Created by zwq on 2015/09/22 13:59.<br/><br/>
 */
public abstract class ThreadUtils implements Runnable {

    private static final String TAG = ThreadUtils.class.getName();
    protected boolean isRunning;

    public abstract void execute();

    public void finish() {
        isRunning = false;
    }

    @Override
    public final void run() {
        isRunning = true;
        execute();
        finish();
    }

    public final void start(){
        run();
    }

    public final boolean isRunning() {
        return isRunning;
    }

}
