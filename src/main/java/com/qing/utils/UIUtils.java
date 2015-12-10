package com.qing.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.qing.log.MLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zwq on 2015/04/15 11:28.<br/><br/>
 * 获取屏幕大小、将对应数值转换为实际像素值、显示Toast
 */
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

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context){
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int height = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    /**
     * 是否有实体Home键，返回true则有，否则为false
     */
    public static boolean hasPhysicalMenuKey(Context context) {
        boolean hasPermanentMenuKey = false;
        if(Build.VERSION.SDK_INT >= 14){
            ViewConfiguration vc = ViewConfiguration.get(context);
            try {
                Method m = ViewConfiguration.get(context).getClass().getMethod("hasPermanentMenuKey", new Class<?>[]{});
                try {
                    hasPermanentMenuKey = (Boolean) m.invoke(vc,new Object[]{});
                } catch (IllegalArgumentException e) {
                    hasPermanentMenuKey = false;
                } catch (IllegalAccessException e) {
                    hasPermanentMenuKey = false;
                } catch (InvocationTargetException e) {
                    hasPermanentMenuKey = false;
                }
            } catch (NoSuchMethodException e) {
                hasPermanentMenuKey = false;
            }
        }
        return hasPermanentMenuKey;
    }

    /**
     * 获取NavigationBar（虚拟按键栏）的高度
     */
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && hasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    /**
     * 获取是否存在NavigationBar（虚拟按键栏）
     * @param context
     * @return
     */
    public static boolean hasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method method = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) method.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }
}
