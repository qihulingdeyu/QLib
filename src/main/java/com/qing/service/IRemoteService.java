package com.qing.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by zwq on 2015/09/05 21:49.<br/><br/>
 * 要实现代理服务，启动的服务必须实现此接口
 */
public abstract class IRemoteService {

    private static final String TAG = IRemoteService.class.getName();
    /**
     START_STICKY_COMPATIBILITY = 0：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
     */
    public static final int START_STICKY_COMPATIBILITY = 0;
    /**
     START_STICKY = 1：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。<br/>
     随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。<br/>
     如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
     */
    public static final int START_STICKY = 1;
    /**
     START_NOT_STICKY = 2：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。<br/>
     */
    public static final int START_NOT_STICKY = 2;
    /**
     START_REDELIVER_INTENT = 3：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，<br/>
     系统会自动重启该服务，并将Intent的值传入。
     */
    public static final int START_REDELIVER_INTENT = 3;

    public abstract void onCreate(Context context, Intent intent);

    /**
     * @param intent
     * @param flags
     * @param startId
     * @return 有四种返回值类型：START_STICKY_COMPATIBILITY,START_STICKY,<br/>
     * START_NOT_STICKY,START_REDELIVER_INTENT<br/>
     * 默认为START_STICKY，Android 2.0以下的用START_STICKY_COMPATIBILITY替换START_STICKY
     */
    public abstract int onStartCommand(Intent intent, int flags, int startId);

    public abstract IBinder onBind(Intent intent);

    public abstract boolean onUnbind(Intent intent);

    public abstract void onRebind(Intent intent);

    public abstract void onDestroy();

    public boolean stopSelf(Context context, Class<?> clazz){
        try {
            Intent intent = new Intent();
            intent.setAction(ProxyService.STOP_REMOTE_SERVICE_ACTION);
            intent.putExtra(ProxyService.REMOTE_SERVICE_CLASS, clazz.getName());
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Context mContext;
    private ClassLoader mClassLoader;

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.mClassLoader = classLoader;
    }
}
