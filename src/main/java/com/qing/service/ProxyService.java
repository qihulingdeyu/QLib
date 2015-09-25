package com.qing.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.qing.log.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwq on 2015/09/05 21:46.<br/><br/>
 * 代理服务，所有服务都由此服务代理运行
 */
public class ProxyService extends Service {

    private static final String TAG = ProxyService.class.getName();
    /**停止被代理的服务 */
    public static final String STOP_REMOTE_SERVICE_ACTION = "android.intent.action.STOP_REMOTE_SERVICE_RECEIVER";
    /**停止代理服务 */
    public static final String STOP_PROXY_SERVICE_ACTION = "android.intent.action.STOP_PROXY_SERVICE_RECEIVER";
    /**当前存在的服务数量 */
    public static final String SERVICES_COUNT = "services_count";
    public static final String REMOTE_SERVICE_CLASS = "remote_service_class";


    protected IRemoteService mRemoteService;

    private List<IRemoteService> serviceList;
    private List<String> serviceNameList;

    private Context mContext;
    private StopRemoteServiceReceiver receiver;
    @Override
    public void onCreate() {
        super.onCreate();
        MLog.i(TAG, "-----onCreate");
        mContext = this;
        if(serviceList==null){
            serviceList = new ArrayList<IRemoteService>();
        }
        if(serviceNameList==null){
            serviceNameList = new ArrayList<String>();
        }

        receiver = new StopRemoteServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(STOP_REMOTE_SERVICE_ACTION);
        //注册
        mContext.registerReceiver(receiver, filter);
    }

    private synchronized IRemoteService onCreate(Intent intent){
        IRemoteService service = null;
        String originService = getServiceName(intent);
        if(originService!=null && !originService.trim().equals("")){
            int id = getServiceId(originService);
            if(id != -1){
                MLog.i(TAG, "---服务已经存在---"+originService);
                service = serviceList.get(id);
            }else{
                MLog.i(TAG, "---服务不存在---"+originService);
                MLog.i(TAG, "-----onCreate:"+originService);
                Class<?> clazz = null;
                try {
                    clazz = mContext.getClassLoader().loadClass(originService);
//					clazz = Class.forName(originService);
                    service = (IRemoteService) clazz.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if(service != null){
                    int key = serviceList.size();
                    serviceList.add(key, service);
                    serviceNameList.add(key, originService);
                    service.onCreate(mContext,intent);
                    service.setContext(mContext);
                    service.setClassLoader(getClassLoader());
                    MLog.i(TAG, "-----onCreate--id:"+key);
                }
            }
        }
        return service;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IRemoteService service = onCreate(intent);
        MLog.i(TAG, "---onStartCommand");
        if(service!=null){
            return service.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        MLog.i(TAG, "-----onBind");
        IRemoteService service = onCreate(intent);
        if(service!=null){
            return service.onBind(intent);
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MLog.i(TAG, "-----onUnbind");
        IRemoteService service = getService(intent);
        if(service!=null){
            return service.onUnbind(intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        MLog.i(TAG, "-----onRebind");
        IRemoteService service = getService(intent);
        if(service!=null){
            service.onRebind(intent);
        }else {
            super.onRebind(intent);
        }
    }

    private synchronized int destory(Intent intent) {
        IRemoteService service = getService(intent);
        if(service!=null){
            if(destoryId!= -1){
                serviceList.remove(destoryId);
                serviceNameList.remove(destoryId);
                MLog.i(TAG, "--onDestroy--remove--service:"+serviceName);
            }
            service.onDestroy();
            service = null;
        }else{
            MLog.i(TAG, "--Service=null--:");
        }
        int size = -1;
        if(serviceList!=null){
            size = serviceList.size();
            MLog.i(TAG, "--onDestroy--serviceList.size--:"+size);
        }else{
            MLog.i(TAG, "---服务还没开启---");
        }
        return size;
    }

    @Override
    public void onDestroy() {
        MLog.i(TAG, "---onDestroy");
        restartService();

        if(serviceList.size()==0){
            serviceList = null;
            serviceNameList = null;
            if(receiver!=null){
                mContext.unregisterReceiver(receiver);
                receiver = null;
            }
            MLog.i(TAG, "---停止服务---");
            super.onDestroy();
            //强制杀掉进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 停止服务的广播接收器
     */
    class StopRemoteServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int serviceCount = destory(intent);
            if(serviceCount==0){
                Intent sIntent = new Intent();
                sIntent.setAction(STOP_PROXY_SERVICE_ACTION);
                sIntent.putExtra(SERVICES_COUNT, serviceCount);
                context.sendBroadcast(sIntent);
                MLog.i(TAG, "---StopServiceReceiver---");
            }
        }
    }

    /**
     * 通过serviceName得到在serviceNameList中的id
     * @param serviceName
     * @return
     */
    private int destoryId = -1;
    private synchronized int getServiceId(String serviceName){
        int id = -1;
        if(serviceName!=null && !serviceName.trim().equals("")){
            for (int i = 0;serviceNameList!=null && i < serviceNameList.size(); i++) {
                if(serviceNameList.get(i).equals(serviceName)){
                    id = i;
                    destoryId = id;
                    break;
                }
            }
        }
        return id;
    }

    /**
     * 获得intent保存的原来的服务名
     * @param intent
     * @return
     */
    private String serviceName;
    private synchronized String getServiceName(Intent intent){
        String originService = null;
        if(intent!=null){
            originService = intent.getStringExtra(REMOTE_SERVICE_CLASS);
            serviceName = originService;
//			MLog.i(TAG, "service:"+originService);
        }
        return originService;
    }

    /**
     * 通过intent得到保存在serviceList的service
     * @param intent
     * @return
     */
    private synchronized IRemoteService getService(Intent intent){
        IRemoteService service = null;
        int id = getServiceId(getServiceName(intent));
        if(id!=-1){
            service = serviceList.get(id);
        }
        return service;
    }

    private void restartService() {
//		if(!taskComplete){
//			//当有任务未完成，但用户退出了应用则自启服务继续完成任务
//			PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), 0, new Intent(DLConstants.REBOOT_PROXY_SERVICE_ACTION), 0);
//			long firstTime = SystemClock.elapsedRealtime();//相对时间
//	//        	System.currentTimeMillis();//绝对时间
//	//        	Date date = new Date(2015, 2, 5, 14, 1);
//			AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//			am.cancel(pIntent);
//			//设置一次性闹钟
//			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, pIntent);
//		}
    }
}
