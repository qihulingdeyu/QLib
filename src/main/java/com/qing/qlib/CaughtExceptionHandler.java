package com.qing.qlib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.qing.utils.XmlTag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zwq on 2015/08/24 10:39.<br/>
 * 异常信息处理
 */
public class CaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = CaughtExceptionHandler.class.getName();
    private Context mContext;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private String savePath = null;

    public CaughtExceptionHandler(){ }

    public void Init(Context context){
        Log.i(TAG, "--Init--");
        mContext = context;
        //获取程序默认的异常处理
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //把自定义的异常处理替换默认的
        Thread.setDefaultUncaughtExceptionHandler(this);

        //sd目录：包名/log
//        savePath = Environment.getExternalStorageDirectory()+mContext.getPackageName()+"/log";
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.i(TAG, "--uncaughtException--");
        handleException(throwable);

        if (defaultHandler!=null){
            //还原默认的异常处理
            defaultHandler.uncaughtException(thread, throwable);
        }
    }

    /**
     * 处理异常信息
     * @param throwable
     */
    private void handleException(Throwable throwable) {
        XmlTag info = new XmlTag(-1, "info");

        XmlTag device = new XmlTag(info.level, "device_info");
        String deviceInfo = CollectDeviceInfo(device, mContext);
        info.addChildTag(device);

        String errorInfo = CollectErrorInfo(throwable);
        info.addChildTag("error", verifyString(errorInfo));

        Log.i(TAG, info.toString());

        save2File(info.toTrimString());
//        save2File(info.toString());
    }

    /**
     * 将文本信息保存到文件
     * @param info
     * @return
     */
    private String save2File(String info) {
        if (info==null || info.trim().isEmpty()) return info;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = sdf.format(new Date())+""+this.hashCode()+".xml";
        try {
            if(savePath==null || savePath.trim().isEmpty()){
                savePath = mContext.getDir("log", Context.MODE_PRIVATE).getPath();
            }else{
                File dir = new File(savePath);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                dir = null;
            }
            File file = new File(savePath + File.separator + fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(info.getBytes());
            fos.flush();
            fos.close();
            fos = null;
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 收集硬件信息
     * @param device
     * @param context
     * @return
     */
    private static String CollectDeviceInfo(XmlTag device, Context context) {
        // 硬件信息
        device.addChildTag("system",verifyString(Build.VERSION.RELEASE));
        device.addChildTag("brand",verifyString(Build.BRAND));
        device.addChildTag("manufacturer",verifyString(Build.MANUFACTURER));
        device.addChildTag("fingerprint",verifyString(Build.FINGERPRINT));
        device.addChildTag("model",verifyString(Build.MODEL));
        device.addChildTag("cpu",verifyString(Build.CPU_ABI));
        device.addChildTag("net",verifyString(""+getNetType(context)));

        // 可用内存
        device.addChildTag("memory", verifyString(Runtime.getRuntime().maxMemory()/1048576 +""));
        // 软件版本
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if(pi != null) {
                device.addChildTag("ver", verifyString(pi.versionName));
            }
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return device.toString();
    }

    /**
     * 获取网络类型
     * @param context
     * @return
     */
    private static int getNetType(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if(info != null) {
            return info.getType();
        }
        return -1;
    }

    /**
     * 获取错误信息
     * @param throwable
     * @return
     */
    private static String CollectErrorInfo(Throwable throwable) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);

        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause!=null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        cause = null;
        printWriter.close();
        printWriter = null;
        return info.toString();
    }

    /**
     * 校验文本信息
     * @param str
     * @return
     */
    private static String verifyString(String str) {
        if(str != null) {
            return str.replaceAll("<", "[").replaceAll(">", "]").replaceAll("&", "_");
        }
        return "";
    }
}
