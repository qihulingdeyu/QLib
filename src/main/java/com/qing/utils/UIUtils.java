package com.qing.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;

public class UIUtils {

    private static float mDensity;
    private static float mDensityDpi;
    private static int mScreenW;
    private static int mScreenH;
    private static ActivityManager mActivityManager;

    public static void init(Activity activiy) {
        Display dis = activiy.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        dis.getMetrics(dm);
        int h = dis.getHeight();
        int w = dis.getWidth();
        mScreenW = w < h ? w : h;
        mScreenH = w < h ? h : w;
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mActivityManager = (ActivityManager) activiy
                .getSystemService(Activity.ACTIVITY_SERVICE);
    }

    public static float getDensity() {
        return mDensity;
    }
    public static float getDensityDpi() {
        return mDensityDpi;
    }
    public static int getScreenW() {
        return mScreenW;
    }
    public static int getScreenH() {
        return mScreenH;
    }
    public static ActivityManager getActivityManager() {
        return mActivityManager;
    }

    /**
     * 将dip转换成px
     */
    public static int dip2px(float dpValue) {
        return (int) (dpValue * mDensity + 0.5f);
    }

    /**
     * 将px转换成dip
     */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / mDensity + 0.5f);
    }

    /**
     * 转换为像素值
     */
    public static int getRealPixel(int pxSrc) {
        return (int) (pxSrc * mDensity / 1.5);
    }
    /**
     * 转换为480屏幕的像素值
     */
    public static int getRealPixel480(int pxSrc) {
        return (int) (pxSrc * mScreenW / 480);
    }
    /**
     * 转换为720屏幕的像素值
     */
    public static int getRealPixel720(int pxSrc) {
        return (int) (pxSrc * mScreenW / 720);
    }

    public static int getRealPixel_w(int pxSrc) {
        return (int) (pxSrc * mScreenW / 480);
    }

    public static int getRealPixel_h(int pxSrc) {
        return (int) (pxSrc * mScreenH / 800);
    }

    public static int computeSampleSize(int w, int h) {
        int bigOne = w > h ? w : h;
        return bigOne / 640;
    }
    
    public static void showToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
