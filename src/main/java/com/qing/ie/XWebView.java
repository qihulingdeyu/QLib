package com.qing.ie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwq on 2015/09/29 14:10.<br/><br/>
 */
public class XWebView extends WebView {

    private static final String TAG = XWebView.class.getName();
    private static final String charsetName = "utf-8";
    public static final String GPS_DB_PATH = "gps_db";
    public static final String WEB_DB_PATH = "web_db";
    public static final String WEB_CACHE_PATH = "web_cache";
    public static final String JS_INVOKE_LOCAL_OBJECT = "android";

    private Context mContext;
    private WebSettings settings;
    public WebViewClient mWebViewClient;
    public WebChromeClient mWebChromeClient;
    public DownloadListener mDownloadListener;
    private IAllMethodListener mAllMethodListener;

    private boolean cacheTitleEnable;
    private List<String> cacheTitle = new ArrayList<String>();

    public XWebView(Context context) {
        super(context);
        mContext = context;

        initSetting();
    }

    /** WebView的所有设置 最好在此方法内初始化 */
    @SuppressLint("JavascriptInterface")
    public void initSetting(){
        settings = getSettings();
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);

        addJavascriptInterface(new JavaScriptInvokeLocalObject(), JS_INVOKE_LOCAL_OBJECT);

        setWebViewClient(null);
        setWebChromeClient(null);
        setDownloadListener(null);

        settings.setDefaultTextEncodingName(charsetName);
//        settings.setPluginsEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        settings.setSaveFormData(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // settings.setUseWideViewPort(true);
        // settings.setLoadWithOverviewMode(true);
        // settings.setSupportZoom(true);
        // settings.setBuiltInZoomControls(true);
        // settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 20);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCachePath(getContext().getDir(WEB_CACHE_PATH, Context.MODE_PRIVATE).getPath());

        settings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < 19) {
            settings.setDatabasePath(getContext().getDir(WEB_DB_PATH, Context.MODE_PRIVATE).getPath());
        }

        settings.setGeolocationEnabled(true);
        settings.setGeolocationDatabasePath(getContext().getDir(GPS_DB_PATH, Context.MODE_PRIVATE).getPath());
    }

    public void setUserAgentString(String ua){
        if (ua != null && settings != null) {
            settings.setUserAgentString(settings.getUserAgentString() + " "+ ua);
        }
    }

    @Override
    public final void setWebViewClient(WebViewClient client) {
        if (client == null) {
            client = new MWebViewClient();
        }
        mWebViewClient = client;
        super.setWebViewClient(client);
    }
    @Override
    public final void setWebChromeClient(WebChromeClient client) {
        if (client == null) {
            client = new MWebChromeClient();
        }
        mWebChromeClient = client;
        super.setWebChromeClient(client);
    }
    @Override
    public final void setDownloadListener(DownloadListener listener) {
        if (listener == null) {
            listener = new MDownloadListener();
        }
        mDownloadListener = listener;
        super.setDownloadListener(listener);
    }

    /** MethodListener会监听WebViewClient、WebChromeClient、DownloadListener内的所有方法 */
    public void setAllMethodListener(AllMethodListener listener){
        mAllMethodListener = listener;
    }

    /** js调用本地方法对象 */
    public class JavaScriptInvokeLocalObject {

        public void showLog(String fromHtml) {
            Log.i(TAG, "html content:\n" + fromHtml);
        }
    }

    class MWebViewClient extends WebViewClient {
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            if (mAllMethodListener != null) {
                mAllMethodListener.doUpdateVisitedHistory(view, url, isReload);
            }
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
            if (mAllMethodListener != null) {
                mAllMethodListener.onFormResubmission(view, dontResend, resend);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (mAllMethodListener != null) {
                mAllMethodListener.onLoadResource(view, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mAllMethodListener != null) {
                mAllMethodListener.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mAllMethodListener != null) {
                mAllMethodListener.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedHttpAuthRequest(view, handler, host, realm);
            }
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedLoginRequest(view, realm, account, args);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedSslError(view, handler, error);
            }
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            if (mAllMethodListener != null) {
                mAllMethodListener.onScaleChanged(view, oldScale, newScale);
            }
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
            if (mAllMethodListener != null) {
                mAllMethodListener.onUnhandledKeyEvent(view, event);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (mAllMethodListener != null) {
                return mAllMethodListener.shouldInterceptRequest(view, url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (mAllMethodListener != null) {
                return mAllMethodListener.shouldOverrideKeyEvent(view, event);
            }
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mAllMethodListener != null) {
                return mAllMethodListener.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    class MWebChromeClient extends WebChromeClient {
        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
            if (mAllMethodListener != null) {
                mAllMethodListener.getVisitedHistory(callback);
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (mAllMethodListener != null) {
                mAllMethodListener.onProgressChanged(view, newProgress);
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedIcon(view, icon);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (cacheTitleEnable) {
                cacheTitle.add(title);
            }
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedTitle(view, title);
            }
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedTouchIconUrl(view, url, precomposed);
            }
        }

        @Override
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
            if (mAllMethodListener != null) {
                mAllMethodListener.onRequestFocus(view);
            }
        }

//        @Override
//        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//            if (mAllMethodListener != null) {
//                return mAllMethodListener.onShowFileChooser(webView, filePathCallback, fileChooserParams);
//            }
//            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
//        }

        // 网页获取本地图库图片
        // android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadFile) {
            openFileChooser(uploadFile, null);
        }

        // android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
            openFileChooser(uploadFile, acceptType, null);
        }

        // android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            if (mAllMethodListener != null) {
                mAllMethodListener.openFileChooser(uploadFile, acceptType, capture);
            }
        }
    }

    /**
     * webview的下载监听
     */
    class MDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (mAllMethodListener != null) {
                mAllMethodListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
            }
        }
    }

    /** 是否缓存网页标题，某些机型 后退时不调用onReceivedTitle方法 */
    public void setTitleCacheEnable(boolean enable) {
        cacheTitleEnable = enable;
    }

    @Override
    public boolean canGoBack() {
        return super.canGoBack();
    }

    @Override
    public void goBack() {
        super.goBack();
        if (cacheTitleEnable && cacheTitle!=null && !cacheTitle.isEmpty()) {
            if (mAllMethodListener != null) {
                mAllMethodListener.onReceivedTitle(this, cacheTitle.remove(cacheTitle.size()-1));
            }
        }
    }

    public void clearAllCache() {
        if (cacheTitle != null) {
            cacheTitle.clear();
        }
        clearHistory();
        clearCache(true);
    }

}
