package com.qing.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qing.ie.AllMethodListener;
import com.qing.ie.XWebView;
import com.qing.log.MLog;
import com.qing.qlib.MainActivity;
import com.qing.qlib.R;
import com.qing.qlib.RelativeLayoutPage;
import com.qing.ui.AlertDialog;
import com.qing.ui.HorizontalProgressBar;
import com.qing.utils.DrawableUtil;
import com.qing.utils.NetUtil;
import com.qing.utils.StringUtil;
import com.qing.utils.UIUtil;

/**
 * Created by zwq on 2015/04/07 14:51.<br/><br/>
 * 自定义浏览器布局
 */
public class BrowserLayoutPage extends RelativeLayoutPage {

	private static final String TAG = BrowserLayoutPage.class.getName();
	private int ID_TITLE_LAYOUT = 0x1;
	private int ID_WEBVIEW = 0x2;
	private int ID_PROGRESS_BAR = 0x3;
	private RelativeLayout titleLayout;
	private TextView titleName;
	private RelativeLayout btnLayout;
	private ImageView backBtn;
	private ImageView closeBtn;

	private HorizontalProgressBar progressBar;

	private XWebView webView;
	private LinearLayout tipsLayout;
	private ImageView mTipIcon;
	private TextView mTip1;
	private TextView mTip2;
	private String mText1 = "加载失败";
	private String mText2 = "请检查您的网络";

	protected boolean closeBrowser = false;
	protected RelativeLayout.LayoutParams rParams;

	private ValueCallback<Uri> uploadFileCallback;
	private int actRequestCode = 1;

	public BrowserLayoutPage(Context context) {
		super(context);
	}

	@Override
	public void initView() {
		topTitleLayout();

		webViewLayout();

		topButtonLayout();

		progressBarLayout();

		tipsLayout();
	}

	protected void topTitleLayout() {
		// 顶部标题布局
		rParams = new LayoutParams(LayoutParams.MATCH_PARENT, UIUtil.getRealPixel720(100));
		titleLayout = new RelativeLayout(mContext);
		titleLayout.setId(ID_TITLE_LAYOUT);
		this.addView(titleLayout, rParams);

		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		titleName = new TextView(mContext);
		titleName.setTextColor(Color.WHITE);
		titleName.setTextSize(17);
		titleName.setText(R.string.app_name);
		titleName.setMaxEms(15);
		titleName.setSingleLine(true);
		titleName.setEllipsize(TruncateAt.END);
		titleLayout.addView(titleName, rParams);
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	protected void webViewLayout() {
		// 浏览器
		rParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		rParams.topMargin = UIUtil.getRealPixel720(100);
		rParams.addRule(RelativeLayout.BELOW, ID_TITLE_LAYOUT);
		webView = new XWebView(mContext);
		webView.setId(ID_WEBVIEW);
		webView.setAllMethodListener(mAllMethodListener);
		this.addView(webView, rParams);
	}

    public XWebView getWebView() {
        return webView;
    }

	protected void topButtonLayout() {
		// 顶部按钮布局
		rParams = new LayoutParams(LayoutParams.MATCH_PARENT, UIUtil.getRealPixel720(100));
		btnLayout = new RelativeLayout(mContext);
		this.addView(btnLayout, rParams);

		// 返回按钮
		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		rParams.leftMargin = UIUtil.getRealPixel720(15);
		backBtn = new ImageView(mContext);
		backBtn.setImageDrawable(DrawableUtil.pressedSelector(mContext, R.mipmap.back_btn_normal, R.mipmap.back_btn_press));
		backBtn.setOnClickListener(mOnClickListener);
		btnLayout.addView(backBtn, rParams);

		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rParams.rightMargin = UIUtil.getRealPixel720(40);
		closeBtn = new ImageView(mContext);
		closeBtn.setImageDrawable(DrawableUtil.pressedSelector(mContext, R.mipmap.close_btn_normal, R.mipmap.close_btn_press));
		closeBtn.setOnClickListener(mOnClickListener);
		btnLayout.addView(closeBtn, rParams);
	}

	protected void progressBarLayout() {
		//进度条
		rParams = new LayoutParams(LayoutParams.MATCH_PARENT, UIUtil.getRealPixel720(8));
		rParams.addRule(RelativeLayout.ALIGN_TOP, ID_WEBVIEW);
		rParams.topMargin = UIUtil.getRealPixel720(100);
		progressBar = new HorizontalProgressBar(mContext);
		progressBar.setId(ID_PROGRESS_BAR);
		this.addView(progressBar, rParams);
	}

	protected void tipsLayout() {
		// 提示内容布局
		rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		tipsLayout = new LinearLayout(mContext);
		tipsLayout.setOrientation(LinearLayout.VERTICAL);
		tipsLayout.setVisibility(View.GONE);
		this.addView(tipsLayout, rParams);

		// 提示的图片
		LinearLayout.LayoutParams lParams;
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		mTipIcon = new ImageView(mContext);
//		mTipIcon.setImageResource(R.drawable.business_fail);
		tipsLayout.addView(mTipIcon, lParams);

		// 提示文字1
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		lParams.topMargin = UIUtil.getRealPixel720(40);
		mTip1 = new TextView(mContext);
		mTip1.setTextColor(Color.WHITE);
		mTip1.setText(mText1);
		tipsLayout.addView(mTip1, lParams);

		// 提示文字2
		lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mTip2 = new TextView(mContext);
		lParams.gravity = Gravity.CENTER_HORIZONTAL;
		mTip2.setTextColor(Color.WHITE);
		mTip2.setText(mText2);
		tipsLayout.addView(mTip2, lParams);
	}

	/** 隐藏顶部标题布局 */
	public void hideTitleLayout() {
		titleLayout.setVisibility(View.GONE);

		rParams = (LayoutParams) webView.getLayoutParams();
		rParams.topMargin = 0;
		webView.setLayoutParams(rParams);

		rParams = (LayoutParams) progressBar.getLayoutParams();
		rParams.topMargin = 0;
		progressBar.setLayoutParams(rParams);
	}

	/** 隐藏顶部按钮布局 */
	public void hideBtnLayout() {
		btnLayout.setVisibility(View.GONE);
	}

	/** 隐藏后退按钮 */
	public void hideBackBtn() {
		backBtn.setVisibility(View.GONE);
	}

	/** 隐藏页面关闭按钮 */
	public void hideCloseBtn() {
		closeBtn.setVisibility(View.GONE);
	}

	private AllMethodListener mAllMethodListener = new AllMethodListener(){

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			if (dealAction(url, view)) {
//				view.stopLoading();
//				return true;
//			}
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		private String injectJs = "" +
				"'<html>\\n' + document.getElementsByTagName('html')[0].innerHTML + '\\n</html>'";

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
//			dimissLoading();
			view.loadUrl(conbineJS("showLog", injectJs));
		}

		/** 拼接js */
		private String conbineJS(String methodName, String jsContent) {
			if (StringUtil.isNullOrEmpty(methodName)) return null;
			if (StringUtil.isNullOrEmpty(jsContent)) return null;
			return "javascript:window."+webView.JS_INVOKE_LOCAL_OBJECT+"."+methodName+"("+jsContent+");";
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
//			MLog.i("bbb", "newProgress:" + newProgress);
			if (progressBar != null) {
				if (progressBar.getVisibility() != View.VISIBLE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(newProgress);
				if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			if (titleName != null && title != null) {
				titleName.setText(title);
			}
		}

		@Override
		public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
			super.openFileChooser(uploadFile, acceptType, capture);
			MLog.i(TAG, "--选择文件--");
			uploadFileCallback = uploadFile;
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setType(acceptType);
			intent.putExtra("return-data", true);
			((Activity) mContext).startActivityForResult(intent, actRequestCode);
		}

		@Override
		public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
			MLog.i("bbb", "url:" + url);
			String fileName = url.substring(url.lastIndexOf("/") + 1);

			AlertDialog dialog = new AlertDialog(mContext);
			dialog.setMessage("是否下载" + fileName);
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


	public void setPageData(Bitmap bg, String url) {
		if (bg != null) {
//			this.setBackgroundDrawable(Utils.largeRblur(bg));
		} else {
			this.setBackgroundColor(0xFFF7AFB2);
		}

		// 判断是否有网络
		if (NetUtil.isNetworkConnected(mContext)) {
//			showLoading();
			webView.setAllMethodListener(mAllMethodListener);
			webView.loadUrl(url.trim());
		} else {
			webView.setVisibility(View.GONE);
			tipsLayout.setVisibility(View.VISIBLE);
		}
	}

	private Animation mAnimation;
	private boolean animRunning;

	private void initAnim() {
		mAnimation = new RotateAnimation(0, 365, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mAnimation.setRepeatCount(-1);
		mAnimation.setDuration(500);
		mAnimation.setFillAfter(true);
		mAnimation.start();
		animRunning = true;
	}

	// 显示下载loading
	public void showLoading() {
		tipsLayout.setVisibility(View.VISIBLE);
//		mTipIcon.setImageResource(R.drawable.business_loading);
		mTip1.setVisibility(View.GONE);
		mTip2.setVisibility(View.GONE);

		initAnim();
		mTipIcon.setAnimation(mAnimation);
	}

	public void dimissLoading() {
		if (mAnimation != null && animRunning) {
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
			if (v == backBtn) {
				MainActivity.mActivity.onBackPressed();

			} else if (v == closeBtn) {
				closeBrowser = true;
				MainActivity.mActivity.onBackPressed();
			}
		}
	};

	@Override
	public boolean onBack() {
		if (!closeBrowser) {
			boolean back = false;
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				back = true;
			}
			return back;
		} else {
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
	public boolean onPageStateChange(boolean isTop, Object[] params) {
		return false;
	}

	@Override
	public Object[] transferPageData() {
		return null;
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
		MLog.i(TAG, "--onActivityResult--");
//		if (requestCode == actRequestCode && resultCode == Activity.RESULT_OK) {
//			if (data != null) {
//				Uri uri = data.getData();
//				Uri thumb = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, uri.getLastPathSegment());
//				try {
//					//缩略图
////					Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), thumb);
//					//原图
//					Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
//				if (cursor != null && cursor.moveToFirst()) {
//					int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//					String realPath = cursor.getString(index);
//					MLog.i(TAG, realPath);
//					cursor.close();
//					cursor = null;
//					// changeImg("file://"+realPath);
//				}
//			}
//		}
		if (requestCode == actRequestCode && uploadFileCallback != null) {
			Uri result = (data == null || resultCode != Activity.RESULT_OK ? null : data.getData());
			uploadFileCallback.onReceiveValue(result);
			uploadFileCallback = null;
			return true;
		}
		return false;
	}

	private void changeImg(String path) {
		String js = "javascript:(function(){" + "var img = document.getElementById(\"uimg\");" + "img.src = \"" + path + "\";" + "img.innerHTML = \"" + path + "\";" + "})();";

		MLog.i(TAG, js);
		webView.loadUrl(js);
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
