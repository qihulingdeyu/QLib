package com.qing.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

import com.qing.callback.DownloadCallback;
import com.qing.callback.HttpCallback;
import com.qing.log.MLog;
import com.qing.ui.AlertDialog;
import com.qing.utils.DownloadUtils;
import com.qing.utils.HttpUtils;
import com.qing.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Date;

/**
 * Created by zwq on 2015/08/31 15:54.<br/><br/>
 */
public class UpdateApk extends BaseService {

    private static final String TAG = UpdateApk.class.getName();
    private Context mContext;
    private UpdateApkReceiver receiver;
    private boolean isRunning;
    private boolean showDialog;
    private AlertDialog dl;
    private ProgressDialog pdl;

    private String updateUrl;
    private int verCode;
    private String downloadUrl;
    private String path;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        receiver = new UpdateApkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TAG);
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) return -1;
        isRunning = true;

        if (intent!=null){
            Bundle bundle = intent.getExtras();
            showDialog = bundle.getBoolean("showDialog", false);
            updateUrl = bundle.getString("updateUrl");
            verCode = bundle.getInt("verCode", 1);
            path = bundle.getString("path");
        }

        if (StringUtils.isNullOrEmpty(updateUrl) || !updateUrl.startsWith("http")){
            //地址错误
            if (showDialog){
                dl = new AlertDialog(mContext);
                dl.setMessage("请求地址错误");
                dl.show();
            }
        }else{
            updateUrl = updateUrl+"?random="+new Date().getTime();
            getNewVersionInfo(updateUrl);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void getNewVersionInfo(String url){
        if (showDialog){
            pdl = new ProgressDialog(mContext);
            pdl.setMessage("正在检查更新...");
            pdl.show();
        }
        HttpUtils.doGet(url, new HttpCallback(){
            @Override
            public void onSuccess(String content, InputStream is) {
                if (pdl!=null){
                    pdl.dismiss();
                }
                //{"ver":"15", "url":"","desc":""}
                boolean isNew = true;
                String desc = "是否下载更新？";
                if (!StringUtils.isNullOrEmpty(content) && content.startsWith("{")){
                    try {
                        JSONObject obj = new JSONObject(content);
                        if (obj.has("ver")){
                            int ver = obj.getInt("ver");
                            if (ver>verCode){
                                isNew = false;
                            }
                        }
                        if (obj.has("url")){
                            downloadUrl = obj.getString("url");
                        }
                        if (obj.has("desc")){
                            desc = obj.getString("desc");//更新内容
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (showDialog && isNew){
                    dl = new AlertDialog(mContext);
                    dl.setMessage("已是最新版本！");
                    dl.show();
                }else if(showDialog){
                    dl = new AlertDialog(mContext);
                    dl.setTitle("新版本");
                    dl.setMessage(desc);
                    dl.addButton("取消", null);
                    dl.addButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            download(downloadUrl);
                        }
                    });
                    dl.show();
                }
            }

            @Override
            public void onFail(String msg) {
                if (pdl!=null){
                    pdl.dismiss();
                }
            }
        });
    }

    private void download(String url){
        if (StringUtils.isNullOrEmpty(url) || !url.startsWith("http")){
            if (showDialog){
                dl = new AlertDialog(mContext);
                dl.setMessage("下载地址错误");
                dl.show();
            }
        }else{
            //下载
            DownloadUtils.download(url, path, new DownloadCallback() {
                @Override
                public void onStart() {
                    //开启通知栏通知
                }

                @Override
                public void onDownloading(long progress) {
                    MLog.i(TAG, "progress:"+progress);
                }

                @Override
                public void onSuccess() {
                    //自动安装更新
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiver!=null)
            mContext.unregisterReceiver(receiver);
    }

    class UpdateApkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
