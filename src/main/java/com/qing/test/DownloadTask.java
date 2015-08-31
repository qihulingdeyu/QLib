package com.qing.test;

import com.qing.callback.HttpCallback;
import com.qing.log.MLog;

import java.io.InputStream;

/**
 * Author: zwq <br/>
 * Date: 2015-7-23 <br/>
 * Time: 上午9:51:59 <br/>
 */
public class DownloadTask {
    
    private ItemInfo itemInfo;
    private int taskId;
    
    public DownloadTask(ItemInfo info) {
        itemInfo = info;
        taskId = info.getId();
    }

    private HttpCallback callback;
    public HttpCallback getCallback(){
        if(callback==null){
            callback = new HttpCallback() {
                @Override
                public void onSuccess(String content, InputStream is) {
                    MLog.i("bbb", taskId + "-onSuccess->" + content);

                    itemInfo.setValue(content);
                    if(itemInfo.getChangeListener()!=null)
                        itemInfo.getChangeListener().onChange(itemInfo);
                }

                @Override
                public void onFail(String msg) {

                }
            };
        }

        return callback;
    }
    
    private void download(final HttpCallback cb){
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                MLog.i("bbb", "----run---"+taskId);
                for (int i = 0; i < 100; i++) {
                    try {
                        cb.postResult(HttpCallback.SUCCESS, i);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    /**
     * 开始下载
     */
    public void startDownload(){
        download(getCallback());
    }
    
}
