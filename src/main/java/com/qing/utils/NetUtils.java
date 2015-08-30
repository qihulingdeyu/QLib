package com.qing.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetUtils {

    /**
     * 判断Network是否开启(包括移动网络和wifi)
     * 
     * @return
     */
    public static boolean isNetworkEnabled(Context mContext) {
        return (isNetEnabled(mContext) || isWIFIEnabled(mContext));
    }

    /**
     * 判断Network是否连接成功(包括移动网络和wifi)
     * 
     * @return
     */
    public static boolean isNetworkConnected(Context mContext) {
        return (isWifiContected(mContext) || isNetContected(mContext));
    }

    /**
     * 判断是否开启移动网络
     * 
     * @param context
     * @return
     */
    public static boolean isNetEnabled2(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断移动网络是否开启
     * 
     * @return
     */
    public static boolean isNetEnabled(Context context) {
        boolean enable = false;
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (telephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
                enable = true;
            }
        }

        return enable;
    }

    /**
     * 判断移动网络是否连接成功！
     * 
     * @param context
     * @return
     */
    public static boolean isNetContected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetworkInfo.isConnected()) {
            return true;
        }
        return false;

    }

    /**
     * 判断是否开启wifi
     * 
     * @param context
     * @return
     */
    public static boolean isWIFIEnabled2(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断wifi是否开启
     */
    public static boolean isWIFIEnabled(Context context) {
        boolean enable = false;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            enable = true;
        }
        return enable;
    }

    /**
     * 判断wifi是否连接成功
     * 
     * @param context
     * @return
     */
    public static boolean isWifiContected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;

    }

}
