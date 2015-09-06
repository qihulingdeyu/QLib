package com.qing.exception;

import android.app.Application;
import android.util.Log;

/**
 * Created by zwq on 2015/08/24 10:37.<br/>
 * 捕获异常信息
 */
public class CaughtApplication extends Application {

    private static final String TAG = CaughtApplication.class.getName();
    private CaughtExceptionHandler mCaughtExceptionHandler;

    @Override
    public void onCreate() {
        Log.i(TAG, "--onCreate--");
        mCaughtExceptionHandler = new CaughtExceptionHandler();
        mCaughtExceptionHandler.Init(getApplicationContext());
        super.onCreate();
    }
}
