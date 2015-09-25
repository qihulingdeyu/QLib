package com.qing.utils;

import com.qing.callback.HttpCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Created by zwq on 2015/04/22 11:27.<br/><br/>
 * http请求工具类
 */
@SuppressWarnings("ALL")
public class HttpUtils {

    private static List<Runnable> threadList = new ArrayList<>();
    private static List<Runnable> waitThreadList = new ArrayList<>();
    private static int maxCount = 5;

    /**
     * 设置HttpClient的参数
     * @return
     */
    private static HttpParams getHttpParams(){
        HttpParams params = new BasicHttpParams();
        //连接超时
        HttpConnectionParams.setConnectionTimeout(params, 30*1000);
        //响应超时
        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        return params;
    }
    
    public static void doGet(String url, final HttpCallback callback){
        doGet(url, null, callback);
    }

    public static void doGet(String url, HashMap<String, String> parameters, final HttpCallback callback){
        if(url==null || !url.startsWith("http")){
            if(callback!=null) callback.postResult(HttpCallback.FAIL, "url is error");
            return;
        }

        if (parameters!=null){
            StringBuffer params = new StringBuffer();
            for (Entry<String, String> item : parameters.entrySet()) {
                String name = item.getKey();
                if(name!=null && !name.trim().equals("")){
                    params.append(name).append("=").append(item.getValue()==null?"":item.getValue());
                    params.append("&");
                }
            }
            if (params.length()>0){
                params.deleteCharAt(params.length()-1);

                int pos1= url.indexOf("?");

                if (pos1<0){
                    url = url + "?" + params.toString();
                }else{
                    url = url + "&" + params.toString();
                }
            }
        }
        doGetThread(url, callback);
    }


    public static void doPost(final String url, HashMap<String, String> parameters, final HttpCallback callback){
        if(url==null || !url.startsWith("http")){
            if(callback!=null) callback.postResult(HttpCallback.FAIL, "url is error");
            return;
        }
        
        if(parameters==null){
            if(callback!=null) callback.postResult(HttpCallback.FAIL, "parameters is null");
            return;
        }
        //封装传递参数的集合  
        final List<NameValuePair> params = new ArrayList<NameValuePair>(); 
        for (Entry<String, String> item : parameters.entrySet()) {
            String name = item.getKey();
            if(name!=null && !name.trim().equals("")){
                params.add(new BasicNameValuePair(name, item.getValue()));  
            }
        }
        doPostThread(url, params, callback);
    }

    private static synchronized void notifyThreadManager(){
        if (waitThreadList.size() > 0){
            int num = maxCount - threadList.size();
            Iterator it = waitThreadList.iterator();
            int i = 0;
            while (it.hasNext()){
                if (i < num){
                    ThreadUtils thread = (ThreadUtils) it.next();
                    threadList.add(thread);
                    thread.start();
                    waitThreadList.remove(thread);
                    i++;
                }else{
                    break;
                }
            }
        }
    }

    private static synchronized void removeThread(Runnable thread){
        threadList.remove(thread);
        notifyThreadManager();
    }

    private static void doGetThread(final String url, final HttpCallback callback){
        ThreadUtils mThread = new ThreadUtils() {
            @Override
            public void execute() {
                if(callback!=null)
                    callback.postResult(HttpCallback.START);
                try {
                    HttpClient hc = new DefaultHttpClient();
                    HttpGet hg = new HttpGet(url);
                    hg.setParams(getHttpParams());
                    HttpResponse hr = hc.execute(hg);
                    int code = hr.getStatusLine().getStatusCode();
                    if(code==200){
                        if(callback!=null)
                            callback.postResult(HttpCallback.SUCCESS, EntityUtils.toString(hr.getEntity(), HTTP.UTF_8), null);
                    }else{
                        if(callback!=null)
                            callback.postResult(HttpCallback.FAIL, "request fail");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null)
                        callback.postResult(HttpCallback.FAIL, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null)
                        callback.postResult(HttpCallback.FAIL, e.getMessage());
                }
            }

            @Override
            public void finish() {
                super.finish();
                if(callback!=null)
                    callback.postResult(HttpCallback.FINISH);
                removeThread(this);
            }
        };
        if(callback!=null)
            callback.postResult(HttpCallback.WAIT);
        waitThreadList.add(mThread);
        notifyThreadManager();
    }

    private static void doPostThread(final String url, final List<NameValuePair> params, final HttpCallback callback){
        ThreadUtils mThread = new ThreadUtils() {
            @Override
            public void execute() {
                if(callback!=null)
                    callback.postResult(HttpCallback.START);
                try {
                    HttpClient hc = new DefaultHttpClient();
                    HttpPost hp = new HttpPost(url);
                    hp.setParams(getHttpParams());
                    //创建传递参数封装 实体对象
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);//设置传递参数的编码
                    //把实体对象存入到httpPost对象中
                    hp.setEntity(entity);
                    //3. 调用第一步中创建好的实例的 execute 方法来执行第二步中创建好的 method 实例
                    HttpResponse hr = hc.execute(hp); //HttpUriRequest的后代对象 //
                    int code = hr.getStatusLine().getStatusCode();
                    if(code==200){
                        if(callback!=null) callback.postResult(HttpCallback.SUCCESS, EntityUtils.toString(hr.getEntity(), HTTP.UTF_8), null);
                    }else{
                        if(callback!=null) callback.postResult(HttpCallback.FAIL, "request fail");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.postResult(HttpCallback.FAIL, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.postResult(HttpCallback.FAIL, e.getMessage());
                }
            }

            @Override
            public void finish() {
                super.finish();
                if(callback!=null)
                    callback.postResult(HttpCallback.FINISH);
                removeThread(this);
            }
        };
        if(callback!=null)
            callback.postResult(HttpCallback.WAIT);
        waitThreadList.add(mThread);
        notifyThreadManager();
    }
}
