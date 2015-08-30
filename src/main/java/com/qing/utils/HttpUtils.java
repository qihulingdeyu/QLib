/**
 * 
 */
package com.qing.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @author zwq
 * 2015-4-22
 * 下午11:27:29
 */
public class HttpUtils {

    public static final int SUCCESS = 1;
    public static final int FAIL = 0;
    
    /**
     * 设置HttpClient的参数
     * @return
     */
    private static HttpParams getHttpParams(){
        HttpParams params = new BasicHttpParams();
        //连接超时
        HttpConnectionParams.setConnectionTimeout(params, 15*1000);
        //响应超时
        HttpConnectionParams.setSoTimeout(params, 20*1000);
        return params;
    }
    
    public static void doGet(final String url, final HttpCallback callback){
        if(url==null || !url.startsWith("http")){
            if(callback!=null) callback.onFail("url is error");
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient hc = new DefaultHttpClient();
                    HttpGet hg = new HttpGet(url);
                    hg.setParams(getHttpParams());
                    HttpResponse hr = hc.execute(hg);
                    int code = hr.getStatusLine().getStatusCode();
                    if(code==200){
                        if(callback!=null) callback.onSuccess(code, EntityUtils.toString(hr.getEntity(), HTTP.UTF_8));
                    }else{
                        if(callback!=null) callback.onFail("request fail");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.onFail(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.onFail(e.getMessage());
                }
            }
        }).start();
    }
    
    public static void doPost(final String url,  HashMap<String, String> parameters, final HttpCallback callback){
        if(url==null || !url.startsWith("http")){
            if(callback!=null) callback.onFail("url is error");
            return;
        }
        
        if(parameters==null){
            if(callback!=null) callback.onFail("parameters is null");
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
        
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        if(callback!=null) callback.onSuccess(code, EntityUtils.toString(hr.getEntity(), HTTP.UTF_8));
                    }else{
                        if(callback!=null) callback.onFail("request fail");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.onFail(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.onFail(e.getMessage());
                }
            }
        }).start();
    }
    
    public interface HttpCallback{
        public void onSuccess(int code, String result);
        public void onFail(String msg);
    }
}
