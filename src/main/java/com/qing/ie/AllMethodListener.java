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
 * Created by zwq on 2015/09/29 15:57.<br/><br/>
 */
public class AllMethodListener implements IAllMethodListener {
    private static final String TAG = AllMethodListener.class.getName();

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {

    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {

    }

    @Override
    public void onLoadResource(WebView view, String url) {

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {

    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {

    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {

    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {

    }

    @Override
    public void onReceivedTitle(WebView view, String title) {

    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {

    }

    @Override
    public void onRequestFocus(WebView view) {

    }

//    @Override
//    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
//        return false;
//    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
////            intent.addCategory(Intent.CATEGORY_DEFAULT);
//
//        intent.setType("image/*");
//        intent.putExtra("return-data", true);

//        openFileChooser1(uploadFile, intent, requestCode);
//
//        /** 上传文件的时候调用此方法 */
//        public void openFileChooser1(ValueCallback<Uri> uploadFile, Intent intent, int requestCode) {
//            mUploadMessage = uploadFile;
//            ((Activity) mContext).startActivityForResult(intent, requestCode);
////            ((Activity) getContext()).startActivityForResult(Intent.createChooser(intent, "File Chooser"), actRequestCode);
//        }
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

    }
}
