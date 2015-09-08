package com.qing.qlib;

import android.content.Intent;
import android.view.KeyEvent;
/**
 * 所有的页面需实现此接口
 * @author zwq
 *
 */
public interface IPage {
    //    public static final String TAG = BasePage.class.getName();
    /**
     * 按返回键时调用此接口，如果处理了返回true否则返回false
     *
     * @return
     */
    public boolean onBack();

    /**
     * 从其它页面返回时调用
     */
    public void onRestore();

    /**
     * 主框架Activity的onStart事件
     *
     * @return
     */
    public boolean onStart();

    /**
     * 主框架Activity的onResume事件
     *
     * @return
     */
    public boolean onResume();

    /**
     * 主框架Activity的onPause事件
     *
     * @return
     */
    public boolean onPause();

    /**
     * 主框架Activity的onStop事件
     *
     * @return
     */
    public boolean onStop();

    /**
     * 主框架Activity的onDestroy事件
     *
     * @return
     */
    public boolean onDestroy();

    /**
     * 关闭页面时调用此接口通知页面
     */
    public void onClose();

    /**
     * 主框架Activity的onActivityResult事件
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * 主框架Activity的onKeyDown事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onActivityKeyDown(int keyCode, KeyEvent event);

    /**
     * 主框架Activity的onKeyUp事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onActivityKeyUp(int keyCode, KeyEvent event);
}
