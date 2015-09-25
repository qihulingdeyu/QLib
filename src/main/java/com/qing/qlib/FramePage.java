package com.qing.qlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * Created by zwq on 2015/09/24 11:18.<br/><br/>
 */
public abstract class FramePage extends FrameLayout implements IPage {

    protected static final String TAG = FramePage.class.getName();
    protected Context mContext;

    public FramePage(Context context) {
        this(context, null);
    }

    public FramePage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FramePage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FramePage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    protected abstract void initView();

    @Override
    public Object[] transferPageData() {
        return null;
    }

    @Override
    public boolean onActivityKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onActivityKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}
