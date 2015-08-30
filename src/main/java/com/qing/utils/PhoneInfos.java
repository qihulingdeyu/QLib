package com.qing.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.qing.qlib.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PhoneInfos {

    private static final String FILE_MEMORY = "/proc/meminfo";
    private static final String FILE_CPU = "/proc/cpuinfo";

    /**
     * 手机的IMEI
     */
    public String mIMEI;
    /**
     * 手机的制式类型，GSM OR CDMA 手机
     */
    public int mPhoneType;
    /**
     * SDK版本
     */
    public int mSdkVersion;
    /**
     * 手机OS版本
     */
    public String mOsVersion;
    /**
     * APP名称
     */
    public String mAppName;
    /**
     * APP版本
     */
    public String mAppVersion;
    /**
     * 手机网络国家编码
     */
    public String mNetWorkCountryIso;
    /**
     * 手机网络运营商ID
     */
    public String mNetWorkOperator;
    /**
     * 手机网络运营商名称
     */
    public String mNetWorkOperatorName;
    /**
     * 手机的数据链接类型
     */
    public String mNetWorkType;
    /**
     * 是否有可用数据链接
     */
    public boolean mIsOnLine;
    /**
     * 当前的数据链接类型
     */
    public String mConnectTypeName;
    /**
     * 手机剩余内存
     */
    public long mFreeMem;
    /**
     * 手机总内存
     */
    public long mTotalMem;
    /**
     * 手机CPU型号
     */
    public String mCupInfo;
    /**
     * 手机名称
     */
    public String mProductName;
    /**
     * 手机型号
     */
    public String mModelName;
    /**
     * 手机设备制造商名称
     */
    public String mManufacturerName;
    /**
     * 基带版本
     */
    public String mBasebandVersion;
    /**
     * 手机指纹,也就是唯一标识
     */
    public String mFingerprint;

    public PhoneInfos() {
    }

    /**
     * String工具, 去掉"-"和空格" ";
     *
     * @param strParams
     * @return
     */
    public static String replaceX(String strParams) {
        if (!TextUtils.isEmpty(strParams)) {
            // strParams=strParams.trim().replace("-","_").replace(" ", "_");
            strParams = strParams.trim().replace("-", "_").replace(" ", "_")
                    .replace("/", "_");
            return strParams.toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * 获取手机的IMEI
     *
     * @param context
     * @return
     */
    public String getIMEI(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        // check if has the permission
        if (PackageManager.PERMISSION_GRANTED == context.getPackageManager()
                .checkPermission(Manifest.permission.READ_PHONE_STATE,
                        context.getPackageName())) {
            return manager.getDeviceId();
        } else {
            return null;
        }
    }

    /**
     * 获取网络制式,like :GSM/CDMA/unKnow;
     *
     * @param context
     * @return
     */
    public int getPhoneType(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        return manager.getPhoneType();
    }

    /**
     * 获取系统OS版本;
     *
     * @return
     */
    public String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机系统SDK版本;
     *
     * @return
     */
    public int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 返回国家代码;
     *
     * @param context
     * @return
     */
    public String getNetWorkCountryIso(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        return manager.getNetworkCountryIso();
    }

    /**
     * 返回手机网络运营商ID
     *
     * @param context
     * @return
     */
    public String getNetWorkOperator(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        return manager.getNetworkOperator();
    }

    /**
     * 返回手机网络运营商名称
     *
     * @param context
     * @return
     */
    public String getNetWorkOperatorName(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    /**
     * 返回当前网络cnwap/cnnet/wifi;
     *
     * @param context
     * @return
     */
    public String getNetworkType(Context context) {
        String networkType = "unKnow";
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobNetInfo != null) {
            networkType = mobNetInfo.getExtraInfo();
        }
        return networkType;
    }

    /**
     * 判断当前网络是否可用;
     *
     * @param context
     * @return
     */
    public boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 返回当前的数据链接类型;
     *
     * @param context
     * @return
     */
    public String getConnectTypeName(Context context) {
        if (!isOnline(context)) {
            return "OFFLINE";
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return info.getTypeName();
        } else {
            return "OFFLINE";
        }
    }

    /**
     * 返回剩余内存;
     *
     * @param context
     * @return
     */
    public long getFreeMem(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        MemoryInfo info = new MemoryInfo();
        manager.getMemoryInfo(info);
        long free = info.availMem / 1024 / 1024;
        return free;
    }

    /**
     * 返回可用内存;
     *
     * @param context
     * @return
     */
    public long getTotalMem(Context context) {
        try {
            FileReader fr = new FileReader(FILE_MEMORY);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split("\\s+");
            return Long.valueOf(array[1]) / 1024;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取CPU信息;
     *
     * @return
     */
    public String getCpuInfo() {
        try {
            FileReader fr = new FileReader(FILE_CPU);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备名字;
     *
     * @return
     */
    public String getProductName() {
        return Build.PRODUCT;
    }

    /**
     * 获取基带版本;
     *
     * @return
     */
    public String getBasebandVersion() {
        return Build.VERSION.INCREMENTAL;
    }

    /**
     * 获取手机指纹识别
     *
     * @return
     */
    public String getFingerprint() {
        return Build.FINGERPRINT;
    }

    /**
     * 获取手机型号;
     *
     * @return
     */
    public String getModelName() {
        return Build.MODEL;
    }

    /**
     * 返回制造商名;
     *
     * @return
     */
    public String getManufacturerName() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取应用的版本;
     *
     * @param context
     * @return
     */
    public String getAppVersion(Context context) {
        String appVer = "null";
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            PackageInfo pi;
            try {
                pi = pm.getPackageInfo(context.getPackageName(), 0);
                if (pi != null) {
                    appVer = pi.versionName;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

        }
        return appVer;
    }

    /**
     * 获取应用程序名;
     *
     * @param context
     * @return
     */
    public String getAppName(Context context) {
        Resources res = context.getResources();
        String appName = res.getString(R.string.app_name);
        if (TextUtils.isEmpty(appName)) {
            appName = "Patui";
        }
        return appName;
    }

    /**
     * 返回一个PhoneInfo实例;
     *
     * @param context
     * @return
     */
    public PhoneInfos getPhoneInfo(Context context) {
        PhoneInfos result = new PhoneInfos();
        result.mIMEI = getIMEI(context);
        result.mPhoneType = getPhoneType(context);
        result.mSdkVersion = getSdkVersion();
        result.mOsVersion = getOsVersion();
        result.mAppName = getAppName(context);

        result.mAppVersion = getAppVersion(context);
        result.mNetWorkCountryIso = getNetWorkCountryIso(context);
        result.mNetWorkOperator = getNetWorkOperator(context);
        result.mNetWorkOperatorName = getNetWorkOperatorName(context);
        result.mNetWorkType = getNetworkType(context);

        result.mIsOnLine = isOnline(context);
        result.mConnectTypeName = getConnectTypeName(context);
        result.mFreeMem = getFreeMem(context);
        result.mTotalMem = getTotalMem(context);
        result.mCupInfo = getCpuInfo();

        result.mProductName = getProductName();
        result.mModelName = getModelName();
        result.mManufacturerName = getManufacturerName();
        result.mBasebandVersion = getBasebandVersion();
        result.mFingerprint = getFingerprint();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nmMd5 : " + mIMEI + "\n");
        builder.append("mAppName: " + mAppName + "\n");
        builder.append("mAppVersion : " + mAppVersion + "\n");
        builder.append("mManufacturerName : " + mManufacturerName + "\n");
        builder.append("mProductName : " + mProductName + "\n");

        builder.append("mModelName : " + mModelName + "\n");
        builder.append("mSysVersion : " + mSdkVersion + "\n");
        builder.append("mOsVersion : " + mOsVersion + "\n");
        builder.append("mCupInfo : " + mCupInfo + "\n");
        builder.append("mFreeMem : " + mFreeMem + "M\n");

        builder.append("mTotalMem : " + mTotalMem + "M\n");
        builder.append("mPhoneType : " + mPhoneType + "\n");
        builder.append("mNetWorkCountryIso : " + mNetWorkCountryIso + "\n");
        builder.append("mNetWorkOperator : " + mNetWorkOperator + "\n");
        builder.append("mNetWorkOperatorName : " + mNetWorkOperatorName + "\n");

        builder.append("mNetWorkType : " + mNetWorkType + "\n");
        builder.append("mIsOnLine : " + mIsOnLine + "\n");
        builder.append("mConnectTypeName : " + mConnectTypeName + "\n");
        builder.append("mBasebandVersion : " + mBasebandVersion + "\n");
        builder.append("mFingerprint : " + mFingerprint + "\n");
        return builder.toString();
    }

    public HashMap<String, String> toHashMap() {
        HashMap<String, String> mAllMap = new HashMap<String, String>();
        mAllMap.put("mMd5", mIMEI);
        mAllMap.put("mAppName", mAppName);
        mAllMap.put("mAppVersion", mAppVersion);
        mAllMap.put("mManufacturerName", mManufacturerName);
        mAllMap.put("mProductName", mProductName);

        mAllMap.put("mModelName", mModelName);
        mAllMap.put("mSdkVersion", String.valueOf(mSdkVersion));
        mAllMap.put("mOsVersion", mOsVersion);
        mAllMap.put("mCupInfo", mCupInfo);
        mAllMap.put("mFreeMem", mFreeMem + "M");

        mAllMap.put("mTotalMem", mTotalMem + "M");
        mAllMap.put("mPhoneType", String.valueOf(mPhoneType));
        mAllMap.put("mNetWorkCountryIso", mNetWorkCountryIso);
        mAllMap.put("mNetWorkOperator", mNetWorkOperator);
        mAllMap.put("mNetWorkOperatorName", mNetWorkOperatorName);

        mAllMap.put("mNetWorkType", mNetWorkType);
        mAllMap.put("mIsOnLine", String.valueOf(mIsOnLine));
        mAllMap.put("mConnectTypeName", mConnectTypeName);
        mAllMap.put("mBasebandVersion", String.valueOf(mBasebandVersion));
        mAllMap.put("mFingerprint", mFingerprint);
        return mAllMap;
    }

}
