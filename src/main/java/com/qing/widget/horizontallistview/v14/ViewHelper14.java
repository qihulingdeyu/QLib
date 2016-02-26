package com.qing.widget.horizontallistview.v14;

import android.annotation.TargetApi;
import android.view.View;

import com.qing.widget.horizontallistview.ViewHelperFactory;

public class ViewHelper14 extends ViewHelperFactory.ViewHelperDefault {
    public ViewHelper14(View view) {
        super(view);
    }

    @TargetApi(14)
    public void setScrollX(int value) {
        this.view.setScrollX(value);
    }

    @TargetApi(11)
    public boolean isHardwareAccelerated() {
        return this.view.isHardwareAccelerated();
    }
}
