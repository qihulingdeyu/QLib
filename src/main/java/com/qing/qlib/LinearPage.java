package com.qing.qlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by zwq on 2015/09/24 11:11.<br/><br/>
 */
public abstract class LinearPage extends LinearLayout implements IPage {

    protected static final String TAG = LinearPage.class.getName();
    protected Context mContext;

    public LinearPage(Context context) {
        this(context, null);
    }

    public LinearPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LinearPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    protected abstract void initView();
}
