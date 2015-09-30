package com.qing.ie;

import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * Created by zwq on 2015/09/29 15:59.<br/><br/>
 */
interface IAllMethodListener {

    /** WebViewClient **/

    /** 更新访问历史 */
    void doUpdateVisitedHistory(WebView view, String url, boolean isReload);

    void onFormResubmission(WebView view, Message dontResend, Message resend);

    void onLoadResource(WebView view, String url);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageFinished(WebView view, String url);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

    void onReceivedLoginRequest(WebView view, String realm, String account, String args);

    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

    void onScaleChanged(WebView view, float oldScale, float newScale);

    void onUnhandledKeyEvent(WebView view, KeyEvent event);

    WebResourceResponse shouldInterceptRequest(WebView view, String url);

    boolean shouldOverrideKeyEvent(WebView view, KeyEvent event);

    /** 打开url对应的网页 */
    boolean shouldOverrideUrlLoading(WebView view, String url);

//----------------------------------
    /** WebChromeClient **/

    /** 获取访问历史 */
    void getVisitedHistory(ValueCallback<String[]> callback);

    /** 加载状态改变 */
    void onProgressChanged(WebView view, int newProgress);

    void onReceivedIcon(WebView view, Bitmap icon);

    void onReceivedTitle(WebView view, String title);

    void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed);

    void onRequestFocus(WebView view);

    /** Android 5.0 Lollipop WebView */
//    boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);

    /** 上传文件的时候调用此方法 */
    void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture);

//----------------------------------
    /** DownloadListener **/
    /** 当下载的时候调用此方法 */
    void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength);

}
