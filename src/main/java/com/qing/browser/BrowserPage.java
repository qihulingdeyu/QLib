package com.qing.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qing.qlib.IPage;
import com.qing.qlib.MainActivity;
import com.qing.ui.AlertDialog;
import com.qing.utils.NetUtils;
import com.qing.utils.UIUtils;

/**
 * Created by zwq on 2015/04/07 14:51.<br/><br/>
 * 自定义浏览器布局
 */
public class BrowserPage extends RelativeLayout implements IPage {

	private static final String TAG = BrowserPage.class.getName();
	private int ID_TITLE_LAYOUT = 1;
	private RelativeLayout titleLayout;
	private TextView titleName;
	private RelativeLayout btnLayout;
	private ImageView backBtn;
	private ImageView closeBtn;

	private WebView webView;
	private LinearLayout tipsLayout;
	private ImageView mTipIcon;
	private TextView mTip1;
	private TextView mTip2;
	private String mText1 = "加载失败";
	private String mText2 = "请检查您的网络";
	public static final String WEB_CACHE_FILE_NAME = "web_cache";
	public static final String GPS_DB_FILE_NAME = "gps_db";

	private Context mContext;
	private boolean closeBrowser = false;

	public BrowserPage(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	public void initView(){
		RelativeLayout.LayoutParams rParams;

		//顶部标题布局
		rParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, UIUtils.getRealPixel720(100));
		titleLayout = new RelativeLayout(mContext);
		titleLayout.setId(ID_TITLE_LAYOUT);
//		titleLayout.setBackgroundResource(R.drawable.main_topbar_bg_fill);
		this.addView(titleLayout, rParams);

		rParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		titleName = new TextView(mContext);
		titleName.setTextColor(Color.WHITE);
		titleName.setTextSize(17);
		titleName.setText("Jane/简·拼");
		titleName.setMaxEms(15);
		titleName.setSingleLine(true);
		titleName.setEllipsize(TruncateAt.END);
		titleLayout.addView(titleName, rParams);

		//浏览器
		rParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
//		rParams.topMargin = UIUtils.getRealPixel720(100);
		rParams.addRule(RelativeLayout.BELOW, ID_TITLE_LAYOUT);
		webView = new WebView(mContext);
		webView.addJavascriptInterface(new InJavaScriptLocalObj(), "localObj");
		webView.setWebViewClient(mWebViewClient);
		webView.setWebChromeClient(mWebChromeClient);
		webView.setDownloadListener(mDownloadListener);
//		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.setVerticalScrollBarEnabled(false);
//		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setPluginState(PluginState.ON);

		webView.getSettings().setAppCachePath(getContext().getDir(WEB_CACHE_FILE_NAME, Context.MODE_PRIVATE).getPath());
		webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		webView.getSettings().setAppCacheEnabled(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setGeolocationDatabasePath(getContext().getDir(GPS_DB_FILE_NAME, Context.MODE_PRIVATE).getPath());
		webView.getSettings().setGeolocationEnabled(true);
		//settings.setUseWideViewPort(true);
		//settings.setLoadWithOverviewMode(true);
		webView.getSettings().setDatabaseEnabled(true);
		webView.getSettings().setSaveFormData(true);
		//settings.setSupportZoom(true);
		//settings.setBuiltInZoomControls(true);
		webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 20);
		webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
		webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		this.addView(webView, rParams);

		//顶部按钮布局
		rParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, UIUtils.getRealPixel720(100));
		btnLayout = new RelativeLayout(mContext);
		this.addView(btnLayout, rParams);

		//返回按钮
		rParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		rParams.topMargin = UIUtils.getRealPixel720(50);
		rParams.leftMargin = UIUtils.getRealPixel720(15);
		backBtn = new ImageView(mContext);
//		backBtn.setImageResource(R.drawable.puzzles_cancel_btn);
		backBtn.setOnClickListener(mOnClickListener);
		btnLayout.addView(backBtn, rParams);

		rParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		rParams.topMargin = UIUtils.getRealPixel720(50);
		rParams.rightMargin = UIUtils.getRealPixel720(40);
		closeBtn = new ImageView(mContext);
//		closeBtn.setImageResource(R.drawable.puzzles_stop_btn);
		closeBtn.setOnClickListener(mOnClickListener);
		btnLayout.addView(closeBtn, rParams);

		//提示内容布局
		rParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		tipsLayout = new LinearLayout(mContext);
		tipsLayout.setOrientation(LinearLayout.VERTICAL);
		tipsLayout.setVisibility(View.GONE);
		this.addView(tipsLayout, rParams);

		//提示的图片
		LinearLayout.LayoutParams lParams;
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		mTipIcon = new ImageView(mContext);
//		mTipIcon.setImageResource(R.drawable.business_fail);
		tipsLayout.addView(mTipIcon, lParams);

		//提示文字1
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		lParams.topMargin = UIUtils.getRealPixel720(40);
		mTip1 = new TextView(mContext);
		mTip1.setTextColor(Color.WHITE);
		mTip1.setText(mText1);
		tipsLayout.addView(mTip1, lParams);

		//提示文字2
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		mTip2 = new TextView(mContext);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		mTip2.setTextColor(Color.WHITE);
		mTip2.setText(mText2);
		tipsLayout.addView(mTip2, lParams);
	}

	/** 隐藏顶部标题布局 */
	public void hideTitleLayout(){
		titleLayout.setVisibility(View.GONE);
	}
	/** 隐藏顶部按钮布局 */
	public void hideBtnLayout(){
		btnLayout.setVisibility(View.GONE);
	}
	/** 隐藏后退按钮 */
	public void hideBackBtn(){
		backBtn.setVisibility(View.GONE);
	}
	/** 隐藏页面关闭按钮 */
	public void hideCloseBtn(){
		closeBtn.setVisibility(View.GONE);
	}

	/** js调用本地方法对象 */
	public class InJavaScriptLocalObj {
		public void showLog(String fromHtml){
			Log.i(TAG, "html content:\n"+fromHtml);
		}
	}

	private WebViewClient mWebViewClient = new WebViewClient(){
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			dimissLoading();
//			view.loadUrl("javascript:window.localObj.showLog('<html>\n' + document.getElementsByTagName('html')[0].innerHTML + '\n</html>');");
		}
	};

	private int actRequestCode = 1;
	private WebChromeClient mWebChromeClient = new WebChromeClient(){
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
		}
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			if(titleName!=null && title!=null){
				titleName.setText(title);
			}
		}

		//网页获取本地图库图片
		//android < 3.0
		public void openFileChooser(ValueCallback<Uri> uploadFile){
			openFileChooser(uploadFile, null);
		}
		//android  3.0+
		public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType){
			openFileChooser(uploadFile, acceptType, null);
		}
		//android > 4.1.1
		public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
			Log.i(TAG, "--选择文件--");

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setType("image/*");
			intent.putExtra("return-data", true);
			((Activity)mContext).startActivityForResult(intent, actRequestCode);
		}
	};

	private DownloadListener mDownloadListener = new DownloadListener() {
		@Override
		public void onDownloadStart(final String url, String userAgent,
									String contentDisposition, String mimetype, long contentLength) {
			Log.i("bbb", "url:"+url);
			String fileName = url.substring(url.lastIndexOf("/")+1);

			AlertDialog dialog = new AlertDialog(mContext);
			dialog.setMessage("是否下载"+fileName);
			dialog.addButton("否", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MainActivity.mActivity.onBackPressed();
				}
			});
			dialog.addButton("是", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					MainActivity.mActivity.startActivity(intent);
					MainActivity.mActivity.onBackPressed();
				}
			});
			dialog.show();
		}
	};

	public void setPageData(Bitmap bg, String url){
		if(bg!=null){
//			this.setBackgroundDrawable(Utils.largeRblur(bg));
		}else{
			this.setBackgroundColor(0xFFF7AFB2);
		}

		//判断是否有网络
		if(NetUtils.isNetworkConnected(mContext)){
			showLoading();
			webView.loadUrl(url.trim());
		}else{
			webView.setVisibility(View.GONE);
			tipsLayout.setVisibility(View.VISIBLE);
		}
	}

	private Animation mAnimation;
	private boolean animRunning;
	private void initAnim(){
		mAnimation = new RotateAnimation(0, 365, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mAnimation.setRepeatCount(-1);
		mAnimation.setDuration(500);
		mAnimation.setFillAfter(true);
		mAnimation.start();
		animRunning = true;
	}

	//显示下载loading
	public void showLoading(){
		tipsLayout.setVisibility(View.VISIBLE);
//		mTipIcon.setImageResource(R.drawable.business_loading);
		mTip1.setVisibility(View.GONE);
		mTip2.setVisibility(View.GONE);

		initAnim();
		mTipIcon.setAnimation(mAnimation);
	}

	public void dimissLoading(){
		if(mAnimation !=null && animRunning){
			mAnimation.cancel();
			animRunning = false;
			mTipIcon.clearAnimation();
			mAnimation = null;
			tipsLayout.setVisibility(View.GONE);
		}
	}

	private View.OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v==backBtn){
				MainActivity.mActivity.onBackPressed();

			}else if(v == closeBtn){
				closeBrowser = true;
				MainActivity.mActivity.onBackPressed();
			}
		}
	};

	@Override
	public boolean onBack() {
		if(!closeBrowser){
			boolean back = false;
			if(webView != null && webView.canGoBack()){
				webView.goBack();
				back = true;
			}
			return back;
		}else{
			return false;
		}
	}

	@Override
	public void onRestore() {

	}

	@Override
	public boolean onStart() {
		return false;
	}

	@Override
	public boolean onResume() {
		return false;
	}

	@Override
	public boolean onPause() {
		return false;
	}

	@Override
	public boolean onStop() {
		return false;
	}

	@Override
	public boolean onDestroy() {
		return false;
	}

	@Override
	public void onClose() {
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "--onActivityResult--");
		if (requestCode== actRequestCode && resultCode==Activity.RESULT_OK){
			if (data!=null){
				Uri uri = data.getData();
				Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
				if (cursor!=null && cursor.moveToFirst()){
					int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					String realPath = cursor.getString(index);
					Log.i(TAG, realPath);
					cursor.close();
					cursor = null;
//					changeImg("file://"+realPath);
				}
			}
		}
		return false;
	}

	private void changeImg(String path){
		String js = "javascript:(function(){" +
				"var img = document.getElementById(\"uimg\");" +
				"img.src = \""+path+"\";" +
				"img.innerHTML = \""+path+"\";" +
				"})();";

		Log.i(TAG, js);
		webView.loadUrl(js);
//		js = "javascript:changeImg1();";
//		webView.loadUrl(js);
	}

	@Override
	public boolean onActivityKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onActivityKeyUp(int keyCode, KeyEvent event) {
		return false;
	}
}
