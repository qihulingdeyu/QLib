package com.qing.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.DownloadListener;
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
 * 自定义浏览器页面
 */
public class BrowserPage extends RelativeLayout implements IPage {
	
	private RelativeLayout titleLayout;
	private TextView titleName;
	private ImageView backBtn;
	private ImageView cancleBtn;
	private WebView webView;
	private LinearLayout tipsLayout;
	private ImageView mTipIcon;
	private TextView mTip1;
	private TextView mTip2;
	private String mText1 = "加载失败";
	private String mText2 = "请检查您的网络";
	public static final String WEB_CACHE_FILE_NAME = "ie_cache";
	public static final String GPS_DB_FILE_NAME = "gps_db";

	private Context mContext;
	public BrowserPage(Context context) {
		super(context);
		mContext = context;
		Initialize();
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void Initialize(){
		LayoutParams rParams;
		//浏览器
		rParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		rParams.topMargin = UIUtils.getRealPixel720(100);
		webView = new WebView(mContext);
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


		this.addView(webView, rParams);

		rParams = new LayoutParams(LayoutParams.MATCH_PARENT,UIUtils.getRealPixel720(100));
		titleLayout = new RelativeLayout(mContext);
//		titleLayout.setBackgroundResource(R.drawable.main_topbar_bg_fill);
		this.addView(titleLayout, rParams);

		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		titleName = new TextView(mContext);
		titleName.setTextColor(Color.WHITE);
		titleName.setTextSize(17);
		titleName.setText("Jane/简·拼");
		titleName.setMaxEms(15);
		titleName.setSingleLine(true);
		titleName.setEllipsize(TruncateAt.END);
		titleLayout.addView(titleName, rParams);

		//返回按钮
		rParams = new LayoutParams(
//				Utils.getRealPixel3(60),Utils.getRealPixel3(60));
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		rParams.topMargin = Utils.getRealPixel3(50);
		rParams.leftMargin = UIUtils.getRealPixel720(15);
		backBtn = new ImageView(mContext);
//		backBtn.setImageResource(R.drawable.business_back);
//		backBtn.setImageResource(R.drawable.puzzles_cancel_btn);
		backBtn.setOnClickListener(mOnClickListener);
		titleLayout.addView(backBtn, rParams);

		rParams = new LayoutParams(
//				Utils.getRealPixel3(60),Utils.getRealPixel3(60));
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		rParams.topMargin = Utils.getRealPixel3(50);
		rParams.rightMargin = UIUtils.getRealPixel720(40);
		cancleBtn = new ImageView(mContext);
//		backBtn.setImageResource(R.drawable.business_back);
//		cancleBtn.setImageResource(R.drawable.puzzles_stop_btn);
		cancleBtn.setOnClickListener(mOnClickListener);
		titleLayout.addView(cancleBtn, rParams);

		//提示内容布局
		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
		}
	};
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
//					MainActivity.mActivity.startActivity(intent);
//					MainActivity.mActivity.onBackPressed();
				}
			});
			dialog.show();
		}
	};

	public void setPageData(Bitmap bg, String url){
		if(bg!=null){
//			this.setBackgroundDrawable(Utils.largeRblur(bg));
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

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v==backBtn){
//				MainActivity.mActivity.onBackPressed();
			}else if(v == cancleBtn){
				stop = true;
//				MainActivity.mActivity.onBackPressed();
			}
		}
	};
	private boolean stop = false;

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
	public boolean onBack() {
		if(!stop){
			boolean out = false;
			if(webView != null){
				if(webView.canGoBack()){
					webView.goBack();
					out = true;
				}
			}
			return out;
		}else{
			return false;
		}

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
		return false;
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