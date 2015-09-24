package com.qing.qlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by zwq on 2015/09/24 10:53.<br/><br/>
 */
public abstract class RelativePage extends RelativeLayout implements IPage {

    protected static final String TAG = RelativePage.class.getName();
    protected Context mContext;

    public RelativePage(Context context) {
        this(context, null);
    }

    public RelativePage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelativePage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelativePage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    protected abstract void initView();
}
