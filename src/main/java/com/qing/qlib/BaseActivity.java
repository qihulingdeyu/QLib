package com.qing.qlib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qing.log.MLog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zwq on 2015/03/24 10:36.<br/><br/>
 * Activity的基类，封装了一些方法
 */
public abstract class BaseActivity extends Activity {

    protected String TAG = this.getClass().getName();
    protected Context mContext;
    public static BaseActivity mActivity;

    private int mLastPage = -1;
    /** 当前打开的页面的Id */
    private int mCurrentPage = -1;
    /** 当前打开的页面，类似activity */
    protected IPage mPage = null;
    /** 最顶部的页面 */
    protected IPage mTopPage = null;
    /** 当前弹出的页面，类似Dialog */
    private IPage mPopupPage = null;
    private FrameLayout mPopupPageContainer;
    private FrameLayout mContainer;

    protected FrameLayout mMainContainer;
    protected LinearLayout mDebugContainer;

    // 页面页面堆栈
    protected ArrayList<Integer> mPageStack = new ArrayList<Integer>();
    protected ArrayList<IPage> mPopupPageStack = new ArrayList<IPage>();

    private HashMap<Integer, Object[]> mMapStackInfo = new HashMap<Integer, Object[]>();

    private boolean isFirstRun = false;
    private long time;
    private boolean killApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        mActivity = this;
        LayoutParams fParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mMainContainer = new FrameLayout(mContext);
        setContentView(mMainContainer, fParams);

        fParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mContainer = new FrameLayout(mContext);
        mMainContainer.addView(mContainer, fParams);

        fParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mPopupPageContainer = new FrameLayout(mContext);
        mPopupPageContainer.setVisibility(View.GONE);
        mMainContainer.addView(mPopupPageContainer, fParams);
        
        
        fParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        mDebugContainer = new LinearLayout(mContext);
        mDebugContainer.setOrientation(LinearLayout.VERTICAL);
        mDebugContainer.setVisibility(View.GONE);
        mMainContainer.addView(mDebugContainer, fParams);
        
        fParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        TextView debugTip = new TextView(mContext);
        debugTip.setText("调试模式");
        debugTip.setTextColor(Color.RED);
        mDebugContainer.addView(debugTip, fParams);
    }

    /**
     * 设为调试模式可查看调试信息
     * @param debug
     */
    public void setDebugMode(boolean debug){
        mDebugContainer.setVisibility(debug == true ? View.VISIBLE : View.GONE);
    }

    public void refreshPage() {
        if (mPage != null) {
            mPage.onDestroy();
            mPage.onClose();
            mContainer.removeAllViews();
        }

        View view = (View) instantiatePage(mCurrentPage);
        mContainer.setFocusable(true);
        mContainer.setFocusableInTouchMode(true);
        mContainer.requestFocus();
        if (view != null) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mContainer.addView(view, params);
            mPage = (IPage) view;
        }
    }

    public IPage setActivePage(int page) {
        return setActivePage(page, false);
    }

    public IPage setActivePage(int page, boolean PageToStack) {
        return setActivePage(page, PageToStack, false, null);
    }

    public IPage setActivePage(int page, boolean pushToStack, boolean DataToStack, Object[] args) {
        mLastPage = mCurrentPage;
        if (page == -1 || page == mCurrentPage) {
            return mPage;
        }
        if (mPage != null) {
            mPage.onPause();
            mPage.onStop();
            mPage.onDestroy();
            mPage.onClose();
            mContainer.removeAllViews();
        }else{
            isFirstRun = true;
        }
        if (pushToStack == true) {
            pushToPageStack(page);
        }
        mCurrentPage = page;

        View view = (View) instantiatePage(page);
        mContainer.setFocusable(true);
        mContainer.setFocusableInTouchMode(true);
        mContainer.requestFocus();
        mContainer.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    MLog.i(TAG, "mContainer focus!");
                } else {
                    MLog.i(TAG, "mContainer unfocus!");
                }

            }
        });
        if (view != null) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mContainer.addView(view, params);
            mPage = (IPage) view;
            mPage.onStart();
			mPage.onResume();
            if (mPopupPage == null) {
                mTopPage = mPage;
            }
            if (DataToStack) {
                setStackInfo(page, args);
            }
        }
        return mPage;
    }

    protected void restorePage(int page) {
        Object[] lastPageDatas = null;
        if (mPage != null) {
            lastPageDatas = mPage.transferPageData();
        }
        Object[] args = getStackInfo(page);
        IPage pg = restorePage(page, args);
        if (pg != null) {
            pg.onRestore();
            pg.onPageStateChange(true, lastPageDatas);
        }
    }

    /**
     * 返回到上一页,成功返回true，不成功返回false
     * 
     * @param filter 页面过滤器，例如过滤主页
     * @return
     */
    public boolean backToLastPage(int[] filter) {
//        MLog.i(TAG, "------backToLastPage");
        // 弹出页 不为空
        if (mPopupPage != null) {
            // 弹出被覆盖的页面做为当前页
            popPopupPage();
            return true;
        }
        // 类似于页面过滤器
        if (filter != null && filter.length != 0) {
            for (int i = 0; i < filter.length; i++) {
                int tmpPage = filter[i];
                if (tmpPage == mCurrentPage) {
                    return false;
                }
            }
        }
        int page = popFromPageStack();
        if (page == -1) {
            return false;
        }
//        else if (page == mCurrentPage){
//            return false;
//        }
        restorePage(page);
        return true;
    }

    /** 直接跳回到主页
     *
     * @return
     */
    public boolean backToHomePage() {
        if (mPopupPage != null) {
            popPopupPage();
            return true;
        }
        int page = backToHomePageStack();
        if (page == -1) {
            return false;
        }
        backHomePage();
        restorePage(page);
        clearStackInfo();
        return true;
    }

    /**
     * 弹出一个页面置于顶部，类似Dialog
     * 
     * @param page
     */
    public void popupPage(IPage page) {
        popupPage(page, false);
    }

    /**
     * 弹出一个页面置于顶部，类似Dialog
     * @param page 要弹出的页面
     * @param closeCurrentPage 是否关闭当前弹出的页面
     */
    public void popupPage(IPage page, boolean closeCurrentPage) {
        if (page != null && page != mPopupPage) {
            if (mTopPage != null) {
//              //弹出页面之间的数据传递
                mTopPage.onPageStateChange(false, null);
                if (closeCurrentPage && mPopupPage != null) {
                    mTopPage.onPause();
                    mTopPage.onStop();
                    mTopPage.onDestroy();
                    mTopPage.onClose();
                    mPopupPageContainer.removeView((View) mTopPage);
                    mPopupPageStack.remove(mTopPage);
                }
            }
            mPopupPage = page;
            mTopPage = mPopupPage;
            mTopPage.onStart();
            mTopPage.onResume();
            mPopupPageStack.add(page);
            View view = (View) page;
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            view.setClickable(true);
            mPopupPageContainer.addView(view, params);
            if (mPopupPageContainer.getVisibility() != View.VISIBLE) {
                mPopupPageContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 销毁所有弹出页面
     */
    public void closeAllPopupPage() {
        IPage page = null;
        Object[] datas = null;
        if (mPopupPage != null) {
            datas = mPopupPage.transferPageData();
            mPopupPage.onPause();
            mPopupPage.onStop();
            mPopupPage.onDestroy();
            mPopupPage.onClose();
        }
        for (int i = 0; i < mPopupPageStack.size(); i++) {
            page = mPopupPageStack.get(i);
            page.onPause();
            page.onStop();
            page.onDestroy();
            page.onClose();
        }
        if (mPopupPageStack.size() > 0 && mTopPage != mPage) {
            if (mPage != null) {
                mPage.onPageStateChange(true, datas);
//                mPage.onStart();
//                mPage.onResume();
            }
        }
        mPopupPageContainer.removeAllViews();
        mPopupPageStack.clear();
        mPopupPage = null;
        mTopPage = mPage;
        mPopupPageContainer.setVisibility(View.GONE);
    }

    /**
     * 销毁指定弹出页面
     *
     * @param page
     */
    public void closePopupPage(IPage page) {
        if (page != null && mPopupPageStack.contains(page)) {
            View view = (View) page;
            if (page == mPopupPage) {
                page.onPause();
                page.onStop();
            }
            page.onDestroy();
            page.onClose();
            mPopupPageContainer.removeView(view);
            mPopupPageStack.remove(page);
            if (mPopupPageStack.size() == 0) {
                mPopupPage = null;
                if (mTopPage != mPage) {
                    if (mPage != null) {
                        mPage.onStart();
                        mPage.onResume();
                    }
                }
                mTopPage = mPage;
                mPopupPageContainer.setVisibility(View.GONE);
            } else if (page == mPopupPage) {
                mTopPage = mPopupPageStack.get(mPopupPageStack.size() - 1);
                mPopupPage = mTopPage;
                mPopupPage.onStart();
                mPopupPage.onResume();
            }
        }
    }

    /**
     * 销毁顶层弹出页面
     */
    public void popPopupPage() {
        if (mPopupPageStack.contains(mPopupPage)) {
            Object[] datas = mPopupPage.transferPageData();
            mPopupPage.onPause();
            mPopupPage.onStop();
            mPopupPage.onDestroy();
            mPopupPage.onClose();
            mPopupPageContainer.removeView((View) mPopupPage);
            mPopupPageStack.remove(mPopupPage);

            if (mPopupPageStack.size() == 0) {
                mPopupPage = null;
                if (mTopPage != mPage) {
                    if (mPage != null) {
                        mPage.onPageStateChange(true, datas);
//                        mPage.onStart();
//                        mPage.onResume();
                    }
                }
                mTopPage = mPage;
                mPopupPageContainer.setVisibility(View.GONE);
            } else {
                mTopPage = mPopupPageStack.get(mPopupPageStack.size() - 1);
                mPopupPage = mTopPage;
                mPopupPage.onPageStateChange(true, datas);
//                mPopupPage.onStart();
//                mPopupPage.onResume();
            }
        }
    }

    /**
     * 获取当前页
     *
     * @return
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * 获取最后一页
     *
     * @return
     */
    public int getLastPage() {
        return mLastPage;
    }

    /**
     * 存储page页面对应的信息(args)
     * 
     * @param page
     * @param args
     * @return
     */
    protected abstract IPage restorePage(int page, Object[] args);

    /**
     * 初始化page对应的页面
     * 
     * @param page
     * @return
     */
    protected abstract IPage instantiatePage(int page);

    /**
     * 返回主页面
     */
    protected abstract void backHomePage();

    @Override
    protected void onStart() {
        if (mTopPage != null && !isFirstRun) {
            mTopPage.onStart();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (mTopPage != null && !isFirstRun) {
            mTopPage.onResume();
        }
        super.onResume();
    }

    /**
     * 返回上一个页面
     */
    protected abstract void onBack();

    @Override
    public void onBackPressed() {
//        MLog.i(TAG, "------size:"+mPageStack.size());
//        for (int i = 0; i < mPageStack.size(); i++) {
//            MLog.i(TAG, "------i:"+mPageStack.get(i));
//        }
        boolean handled = false;
        if (mTopPage != null) {
//            MLog.i(TAG, "------mTopPage:"+mTopPage.getClass().getName());
            handled = mTopPage.onBack();
        }
        if (handled == false) {
//            if(backToLastPage(null) == false) {
                onBack();
//            }
        }
    }

    @Override
    protected void onPause() {
        if (mTopPage != null) {
            mTopPage.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mTopPage != null) {
            mTopPage.onStop();
        }
        super.onStop();
    }

    /**
     * 其它操作 要在调用super.onDestroy();之前执行
     */
    @Override
    protected void onDestroy() {
        if (mTopPage != null) {
            mTopPage.onDestroy();
        }
        clearPageStack();
        clearStackInfo();
        super.onDestroy();
        killApp();
    }

    /**
     * 过滤主页，判断是否要退出应用
     * @param page
     * @return
     */
    protected boolean filterHomePage(int page) {
        if (backToLastPage(new int[] { page }) == false) {
            confirmExit();
            return true;
        }
        return false;
    }

    /**
     * 确认是否退出应用
     * @return
     */
    protected final boolean confirmExit() {
        time = System.currentTimeMillis() - time;
        if(time>3000L){
            Toast.makeText(mContext, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            time = System.currentTimeMillis();
            return false;
        }else{
            killApp = true;
            mActivity.finish();
            return true;
        }
    }

    /**
     * 强制杀死进程
     * @return
     */
    protected final boolean killApp(){
        if (killApp){
            mActivity = null;
            MLog.i(TAG, "killProcess");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return killApp;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = false;
        if (mTopPage != null) {
            handled = mTopPage.onActivityResult(requestCode, resultCode, data);
        }
        if (handled == false) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mTopPage != null) {
            if (true == mTopPage.onActivityKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mTopPage != null) {
            if (true == mTopPage.onActivityKeyUp(keyCode, event)) {
                return true;
            }
            if (keyCode == 168 || keyCode == 169 || keyCode == 256
                    || keyCode == 261)// samsung galasy zoom
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 从页面栈中移除最顶的页面
     * @return
     */
    public int popStackTopPage() {
        int len = mPageStack.size();
        if (len < 1) {
            return -1;
        }
        int page = mPageStack.remove(len - 1);
        return page;
    }

    /**
     * 将相对于当前页的前一页取出来，没有删除栈顶，只是取
     * 
     * @return
     */
    public int peekLastPage() {
        int len = mPageStack.size();
        if (len > 1) {
            return mPageStack.get(len - 2);
        }
        return -1;
    }

    /**
     * 清空所有页面堆栈数据
     */
    public void clearPageStack() {
        mPageStack.clear();
    }

    /**
     * 将倒数第二个栈顶数据取出来，并且将栈顶数据删除
     * 
     * @return
     */
    public int popFromPageStack() {
        int len = mPageStack.size();
        if (len < 1)
            return -1;
        int page = mPageStack.remove(len - 1);
        return page;
    }

    /**
     * 将最栈底的页面显示出来，这里相当于直接回到主页面
     * 
     * @return
     */
    public int backToHomePageStack() {
        int len = mPageStack.size();
        while (len > 1) {
            mPageStack.remove(len - 1);
            len = mPageStack.size();
        }
        int page = mPageStack.remove(len - 1);
        return page;
    }

    /**
     * 将页面压入堆栈顶
     * 
     * @param page
     */
    public void pushToPageStack(int page) {
        if (mPageStack.contains(page) == true) {
            mPageStack.remove(mPageStack.indexOf(page));
        }
        mPageStack.add(page);
    }

    public void clearStackInfo() {
        mMapStackInfo.clear();
    }

    public Object[] getStackInfo(int page) {
        return mMapStackInfo.get(page);
    }

    public void setStackInfo(int page, Object[] infos) {
        mMapStackInfo.put(page, infos);
    }

    public void OpenAnimationTransition(int enterAnim, int exitAnim){

    }

    public void CloseAnimationTransition(int enterAnim, int exitAnim){

    }

    public interface OnEnterAnimationEnd{
        void OnAnimationEnd();
    }

    public interface OnBackAnimationEnd{
        void OnAnimationEnd();
    }

    public void startBackAnimation(){
        startBackAnimation(null);
    }

    public void startEnterAnimation(){
        startEnterAnimation(null);
    }

    public void startBackAnimation(final OnBackAnimationEnd anim){
        Animation slide_left_in = new TranslateAnimation(-1.0f, 0.0f, 0, 0);
        slide_left_in.setDuration(250);
        Animation slide_right_out = new TranslateAnimation(0.0f, 1.0f, 0, 0);
        slide_right_out.setDuration(250);

        slide_left_in.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(anim != null){
                    anim.OnAnimationEnd();
                }
//                mContainer.removeView(mLastView);
//                mLastView = null;
            }
        });
//        mLastView.startAnimation(slide_right_out);
//        mCurrentView.startAnimation(slide_left_in);
    }

    public void startEnterAnimation(final OnEnterAnimationEnd anim){
//        if(mLastView != null && mCurrentView != null){
//            Animation slide_right_in = new TranslateAnimation(1.0f, 0.0f, 0, 0);
//            slide_right_in.setDuration(250);
//            Animation slide_left_out = new TranslateAnimation(0.0f, -1.0f, 0, 0);
//            slide_left_out.setDuration(250);
//
//            slide_right_in.setAnimationListener(new AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    if(anim != null){
//                        anim.OnAnimationEnd();
//                    }
//                    mContainer.removeView(mLastView);
//                    mLastView = null;
//                }
//            });
//            mLastView.startAnimation(slide_left_out);
//            mCurrentView.startAnimation(slide_right_in);
//        }
    }

    @Override
    public ComponentName startService(Intent service) {
        if (service!=null){//从5.0开始服务必须显式调用
            if (service.getPackage()==null || service.getPackage().trim().isEmpty()){
                //修复android5.0及以上版本的bug,这里你需要设置你应用的包名
                service.setPackage(getPackageName());
            }
        }
        return super.startService(service);
    }
}
