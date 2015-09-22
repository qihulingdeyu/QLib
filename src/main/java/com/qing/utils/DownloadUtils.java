package com.qing.utils;

import com.qing.callback.DownloadCallback;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zwq on 2015/08/31 16:56.<br/><br/>
 * 下载是耗时操作，需要开启新线程执行
 */
public class DownloadUtils {

    private static final String TAG = DownloadUtils.class.getName();

    public static void download(String url, String path, DownloadCallback callback){
        download(url, path, null, callback);
    }

    public static void download(final String url, final String path, final String fileName, final DownloadCallback callback){
        ThreadUtils mThread = new ThreadUtils() {
            @Override
            public void execute() {
                if(url == null || path==null){
                    if (callback!=null) callback.postResult(DownloadCallback.FAIL, "下载地址或保存目录不能为空");
                }
                boolean isFile = false;
                File file = new File(path);
                if (file.isFile()){
                    file = file.getParentFile();
                    isFile = true;
                }else{
                    if (StringUtils.isNullOrEmpty(fileName)) {
                        if (callback!=null) callback.postResult(DownloadCallback.FAIL, "文件名不能为空");
                    }
                }
                if(!file.exists()){
                    if(!file.mkdirs()){
                        if (callback!=null) callback.postResult(DownloadCallback.FAIL, "目录创建失败");
                    }
                }
                if (isFile) {
                    file = new File(path);
                }else{
                    file = new File(path, fileName);
                }

                if(file.exists()) {
                    if (callback!=null) callback.postResult(DownloadCallback.SUCCESS);
                }else{
                    try {
                        if(!file.createNewFile()){
                            if (callback!=null) callback.postResult(DownloadCallback.FAIL, "文件创建失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final long fileSize = file.length();
                int contentSize = 0;

                InputStream inputStream = null;
                ByteArrayOutputStream byteStream = null;
                FileOutputStream fileOutput = null;
                try {
                    URL _url = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(40*1000);
                    conn.setReadTimeout(90*1000);
                    conn.setDoInput(true);
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        if (callback!=null) callback.postResult(DownloadCallback.START);
                        contentSize = conn.getContentLength();

                        if(fileSize != contentSize) {
                            inputStream = conn.getInputStream();
                            byteStream = new ByteArrayOutputStream();

                            int size = 0;
                            int len = 0;
                            byte buffer[] = new byte[4096];
                            fileOutput = new FileOutputStream(file.getAbsolutePath());

                            while((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                                fileOutput.write(buffer, 0, len);
                                if (callback!=null) {
                                    size += len;
                                    callback.postResult(DownloadCallback.LOADING, size/contentSize);
                                }
                            }
                        }
                        if (callback!=null) callback.postResult(DownloadCallback.SUCCESS);
                    }else{
                        if (callback!=null) callback.postResult(DownloadCallback.FAIL, "下载失败:"+conn.getResponseMessage());
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    if (callback!=null) callback.postResult(DownloadCallback.FAIL, "下载失败:"+e.getMessage());
                } finally {
                    try {
                        if (fileOutput!=null) {
                            fileOutput.close();
                            fileOutput = null;
                        }
                        if (byteStream!=null){
                            byteStream.close();
                            byteStream = null;
                        }
                        if (inputStream!=null){
                            inputStream.close();
                            inputStream = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (callback!=null) callback.postResult(DownloadCallback.FINISH);
            }
        };
        mThread.start();
    }

}
