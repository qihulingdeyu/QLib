package com.qing.log;

import android.util.Log;

public class MLog {

    private static final String TAG = MLog.class.getName();
    public static boolean DEBUG = true;

    public static void i(String log) {
        i(TAG, log);
    }

    public static void i(int log) {
        i(TAG, ""+log);
    }

    public static void i(Object log) {
        if (log == null) {
            i(TAG, "null");
        }else{
            i(TAG, log.toString());
        }
    }

    public static void i(String tag, String log) {
        if (DEBUG == false)
            return;
        if (log == null) {
            log = "null";
        }
        Log.i(tag, log);
    }

    public static void d(String tag, String log) {
        if (DEBUG == false)
            return;
        if (log == null) {
            log = "null";
        }
        Log.d(tag, log);
    }

    public static void e(String tag, String log) {
        if (DEBUG == false) {
            return;
        }
        if (log == null) {
            log = "null";
        }
        Log.e(tag, log);
    }

    public static void _assert(boolean condition, String msg) {
        if (DEBUG == false)
            return;
        if (condition) {
            throw new AssertionError(msg);
        }
    }

    public static void _assert(String msg) {
        if (DEBUG == false)
            return;
        throw new AssertionError(msg);
    }
}
