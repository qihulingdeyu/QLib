package com.qing.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.qing.log.MLog;
import com.qing.utils.FileUtil;
import com.qing.utils.StringUtil;
import com.qing.utils.XmlTag;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by zwq on 2015/08/24 10:39.<br/>
 * 异常信息处理
 */
public class CaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = CaughtExceptionHandler.class.getName();
    private Context mContext;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private String logPath = null;

    public CaughtExceptionHandler(){ }

    public void Init(Context context){
        MLog.i(TAG, "--Init--"+TAG);
        mContext = context;
        //获取程序默认的异常处理
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //把自定义的异常处理替换默认的
        Thread.setDefaultUncaughtExceptionHandler(this);

        //sd目录：包名/log
        logPath = FileUtil.getAppPath(mContext) + "log";
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        MLog.i(TAG, "--uncaughtException--" + TAG);
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
        device.setTagValueToTrim(true);
        String deviceInfo = CollectDeviceInfo(device, mContext);
        info.addChildTag(device);

        String errorInfo = CollectErrorInfo(throwable);
        info.addChildTag("error", verifyString(errorInfo));

        save2File(info.toString(), false);
    }

    /**
     * 将文本信息保存到文件
     * @param info
     * @param sortByDate
     * @return
     */
    private String save2File(String info, boolean sortByDate) {
        if (StringUtil.isNullOrEmpty(info)) return null;

        String fileName = StringUtil.getDateTime("yyyyMMddHHmmss")+"_"+this.hashCode()+".log";

        if(StringUtil.isNullOrEmpty(logPath)){
            logPath = mContext.getDir("log", Context.MODE_PRIVATE).getPath();
        }
        if (sortByDate){
            logPath += File.separator + StringUtil.getDateTime("yyyyMMdd");
        }
        FileUtil.write2SD(info, logPath + File.separator + fileName, true);
        return fileName;
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
        device.addChildTag("model", verifyString(Build.MODEL));
        device.addChildTag("cpu",verifyString(Build.CPU_ABI));
        device.addChildTag("net", verifyString("" + getNetType(context)));

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
