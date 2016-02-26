package com.qing.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qing.utils.UIUtil;

/**
 * Created by zwq on 2016/2/25 10:42.<br/><br/>
 */
public class PageDot extends LinearLayout{
    private static final String TAG = PageDot.class.getName();

    private int normal;
    private int selected;
    private int margin = UIUtil.getRealPixel720(5);

    private boolean hasInit;
    private int maxNum;
    private int currentIndex = -1;
    private ImageView dotView;

    public PageDot(Context context) {
        super(context);
    }

    public void setDotRes(int normal, int selected) {
        this.normal = normal;
        this.selected = selected;
    }

    public void setDotMargin(int margin) {
        this.margin = margin;
    }

    public void setMax(int max) {
        maxNum = max;
    }

    public void setSelection(int index) {
        if (maxNum <= 0 || index < 0 || index >= maxNum) {
            return;
        }

        if (hasInit) {
            if (index != currentIndex) {
                if (currentIndex >= 0) {
                    dotView = (ImageView) getChildAt(currentIndex);
                    if (dotView != null) {
                        dotView.setImageResource(normal);
                    }
                }

                dotView = (ImageView) getChildAt(index);
                if (dotView != null) {
                    dotView.setImageResource(selected);
                }

                currentIndex = index;
            }

        } else {
            int orientation = getOrientation();
            LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (orientation == LinearLayout.HORIZONTAL) {
                lParams.setMargins(margin, 0, margin, 0);
            } else {
                lParams.setMargins(0, margin, 0, margin);
            }
            for (int i = 0; i < maxNum; i++) {
                dotView = new ImageView(getContext());
                dotView.setImageResource(normal);
                addView(dotView, lParams);
            }
            hasInit = true;
            setSelection(index);
        }
    }
}
