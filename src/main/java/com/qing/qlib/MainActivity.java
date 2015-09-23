/**
 from net
 #####################################################
 #                       _oo0oo_                     #
 #                      o8888888o                    #
 #                      88" . "88                    #
 #                      (| -_- |)                    #
 #                      0\  =  /0                    #
 #                    ___/`---'\___                  #
 #                  .' \\|     |// '.                #
 #                 / \\|||  :  |||// \               #
 #                / _||||| -:- |||||_ \              #
 #               |   | \\\  -  /// |   |             #
 #               | \_|  ''\---/''  |_/ |             #
 #               \  .-\__  '-'  ___/-. /             #
 #             ___'. .'  /--.--\  `. .'___           #
 #          ."" '<  `.___\_<|>_/___.' >' "".         #
 #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 #                       `=---='                     #
 #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 #                佛祖保佑      永无BUG               #
 #####################################################
 */
package com.qing.qlib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.qing.log.MLog;

public class MainActivity extends BaseActivity {

    private static Context mContext = null;
    public static MainActivity mActivity = null;
    // 主页id
    private static final int PAGE_MAIN = 0x1;
    private static final int PAGE_HOME = 0x2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivity = this;
    }


    @Override
    protected IPage restorePage(int page, Object[] args) {
        final IPage pg = setActivePage(page, true);
        return pg;
    }

    @Override
    protected IPage instantiatePage(int page) {
        IPage pg = null;
        switch (page) {
        case PAGE_MAIN:
//            pg = new MainPage(mContext);
            break;
        default:
            break;
        }
        return pg;
    }
    @Override
    protected void backHomePage() {
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onBack() {
        if (backToLastPage(new int[] { PAGE_HOME }) == false) {
            confirmExit(mContext);
        }
    }

    private long time;
    private boolean killApp;
    private void confirmExit(Context context) {
        time = System.currentTimeMillis()-time;
        if(time>3000L){
            Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            time = System.currentTimeMillis();
        }else{
            killApp = true;
            mActivity.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mActivity == this) {
            mActivity = null;
        }
        if (killApp == true) {
            android.os.Process.killProcess(android.os.Process.myPid());
            MLog.i("killProcess");
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
