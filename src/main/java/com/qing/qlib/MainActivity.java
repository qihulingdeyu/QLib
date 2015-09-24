package com.qing.qlib;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends BaseActivity {

    // 主页id
    private static final int PAGE_MAIN = 0x1;
    private static final int PAGE_HOME = 0x2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        filterHomePage(PAGE_HOME);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
