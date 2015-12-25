package com.qing.utils;

import com.qing.callback.HttpCallback;
import com.qing.log.MLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by zwq on 2015/04/22 11:27.<br/><br/>
 * http请求工具类
 */
@SuppressWarnings("ALL")
public class HttpUtil {

    private static final String TAG = HttpUtil.class.getName();

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

    private static HttpURLConnection setHttpParams(HttpURLConnection conn){
        if (conn == null) return null;
        //设置连接超时
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setDoInput(true);
        //设置容许输出
         conn.setDoOutput(true);
        //设置不使用缓存
         conn.setUseCaches(false);
        //设置使用POST的方式发送
        // conn.setRequestMethod("POST");
        //设置维持长连接
        // conn.setRequestProperty("Connection", "Keep-Alive");
        //设置文件字符集
         conn.setRequestProperty("Charset", "UTF-8");
        //设置文件长度
        // conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        //设置文件类型
        // conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        return conn;
    }
    
    public static void doGet(String url, final HttpCallback callback){
        doGet(url, null, callback);
    }

    public static void doGet(String url, HashMap<String, String> parameters, final HttpCallback callback){
        url = appendUrlParams(url, parameters);
//        MLog.i("bbb", url);
        doGetThread(url, callback);
    }

    public static String appendUrlParams(String url, HashMap<String, String> parameters){
        if(url==null || !url.startsWith("http")){
            MLog.i(TAG, "url is null or error");
            return url;
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
        return url;
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
                    ThreadUtil thread = (ThreadUtil) it.next();
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
        ThreadUtil mThread = new ThreadUtil() {
            @Override
            public void execute() {
                if(callback!=null)
                    callback.postResult(HttpCallback.START);
                HttpURLConnection conn = null;
                try {
                    URL real_url = new URL(url);
                    conn = (HttpURLConnection) real_url.openConnection();
                    conn.setRequestMethod("GET");
                    conn = setHttpParams(conn);
                    conn.connect();

//                    HttpClient hc = new DefaultHttpClient();
//                    HttpGet hg = new HttpGet(url);
//                    hg.setParams(getHttpParams());
//                    HttpResponse hr = hc.execute(hg);
                    int code = conn.getResponseCode();
                    if(code == HttpURLConnection.HTTP_OK) {
                        if(callback!=null) {
                            InputStream is = conn.getInputStream();
//                            is = hr.getEntity().getContent();//getContent()只能被使用一次，否则报异常
//                            MLog.i("bbb", "len:" + is.available() + ", cl:" + conn.getContentLength());
//                            String encode = conn.getContentEncoding();
//                            MLog.i("bbb", "encode:" + encode);

                            callback.postResult(HttpCallback.SUCCESS, FileUtil.stream2String(is, false), is);
                        }
                    }else{
                        if(callback!=null)
                            callback.postResult(HttpCallback.FAIL, "request fail, code:"+code+", msg:"+conn.getResponseMessage());
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null)
                        callback.postResult(HttpCallback.FAIL, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null)
                        callback.postResult(HttpCallback.FAIL, e.getMessage());
                }finally {
                    if (conn != null){
                        conn.disconnect();
                        conn = null;
                    }
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
        ThreadUtil mThread = new ThreadUtil() {
            @Override
            public void execute() {
                if(callback!=null)
                    callback.postResult(HttpCallback.START);
                HttpURLConnection conn = null;
                try {
                    URL real_url = new URL(url);
                    conn = (HttpURLConnection) real_url.openConnection();
                    conn.setRequestMethod("POST");
                    conn = setHttpParams(conn);

                    String data = "";
                    for (int i = 0; i < params.size(); i++) {
                        NameValuePair nameValue = params.get(i);
                        if (i > 0){
                            data += "&";
                        }
                        data += nameValue.getName()+"="+URLEncoder.encode(nameValue.getValue(), "UTF-8");
                    }
                    // 设置请求的头
                    conn.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
//                    conn.connect();
                    //获取输出流
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();
                    os.close();
                    os = null;

                    int code = conn.getResponseCode();
                    if(code == HttpURLConnection.HTTP_OK){
                        if(callback!=null) {
                            InputStream is = conn.getInputStream();
//                          MLog.i("bbb", "len:"+is.available());
                            callback.postResult(HttpCallback.SUCCESS, FileUtil.stream2String(is, false), is);
                        }
                    }else{
                        if(callback!=null) callback.postResult(HttpCallback.FAIL, "request fail, code:"+code+", msg:"+conn.getResponseMessage());
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.postResult(HttpCallback.FAIL, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(callback!=null) callback.postResult(HttpCallback.FAIL, e.getMessage());
                }finally {
                    if (conn != null){
                        conn.disconnect();
                        conn = null;
                    }
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

    private static void doPostThread1(final String url, final List<NameValuePair> params, final HttpCallback callback){
        ThreadUtil mThread = new ThreadUtil() {
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
                    if(code == 200){
                        if(callback!=null) {
                            InputStream is = hr.getEntity().getContent();
//                          MLog.i("bbb", "isS:" + httpEntity.isStreaming()+", len:"+is.available());
                            callback.postResult(HttpCallback.SUCCESS, FileUtil.stream2String(is, false), is);
                        }
                    }else{
                        if(callback!=null) callback.postResult(HttpCallback.FAIL, "request fail, code:"+code);
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
