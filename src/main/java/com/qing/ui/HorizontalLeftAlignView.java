package com.qing.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zwq on 2015/09/06 12:22.<br/><br/>
 * 横向左对齐排列的布局
 */
public class HorizontalLeftAlignView extends ViewGroup {

    private static final String TAG = HorizontalLeftAlignView.class.getName();
    private Context mContext;
    private int vWidth, vHeight;
    private int pL, pT, pR, pB;
    private int lineNum;

    private int hasEllipsisView;

    public HorizontalLeftAlignView(Context context) {
        this(context, null);
    }

    public HorizontalLeftAlignView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        Log.i(TAG, "widthMode:"+widthMode+",heightMode:"+heightMode+",widthSize:"+widthSize+",heightSize:"+heightSize);

        pL = getPaddingLeft();
        pT = getPaddingTop();
        pR = getPaddingRight();
        pB = getPaddingBottom();

        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        /** 记录如果是wrap_content时设置的宽和高 */
        int width = 0;
        int height = 0;

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY){
            int childCount = getChildCount();
            if (childCount > 0) {
                MarginLayoutParams params = null;
                int cWidth, cHeight;
                int itemWidth, itemHeight;
                int tempMaxWidth = pL;
                int tempMaxHeight = pT;

                width = tempMaxWidth;
                height = tempMaxHeight;

                /** 根据childView计算的出的宽和高，以及设置的margin计算容器的宽和高，主要用于容器是warp_content时 */
                for (int i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
                    params = (MarginLayoutParams) childView.getLayoutParams();
                    cWidth = childView.getMeasuredWidth();
                    cHeight = childView.getMeasuredHeight();

                    itemWidth = cWidth + params.leftMargin + params.rightMargin;
                    itemHeight = cHeight + params.topMargin + params.bottomMargin;

                    if (tempMaxWidth + itemWidth > widthSize){
                        width = Math.max(width, tempMaxWidth);
                        tempMaxWidth = pL;

                        height += tempMaxHeight;
                        tempMaxHeight = 0;
                    }
                    tempMaxWidth += itemWidth;
                    tempMaxHeight = Math.max(tempMaxHeight, itemHeight);
                }
                width += pR;
                height += pB;
            }
        }

        /**
         * 如果是wrap_content设置为我们计算的值
         * 否则：直接设置为父容器计算的值
         */
        vWidth = (widthMode == MeasureSpec.EXACTLY) ? widthSize : width;
        vHeight = (heightMode == MeasureSpec.EXACTLY) ? heightSize : height;
//        Log.i(TAG, "vWidth:" + vWidth + ",vHeight:" + vHeight);
        setMeasuredDimension(vWidth, vHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int wcount = pL;
            int hcount = pT;
            lineNum = 1;

            MarginLayoutParams params;
            int cWidth, cHeight, maxHeight=0;
            int itemWidth, itemHeight;
            for (int i=0; i<childCount; i++){
                View childView = getChildAt(i);
                params = (MarginLayoutParams) childView.getLayoutParams();
                cWidth = childView.getMeasuredWidth();
                cHeight = childView.getMeasuredHeight();

                itemWidth = params.leftMargin + cWidth + params.rightMargin;
                itemHeight = params.topMargin + cHeight + params.bottomMargin;
                if (wcount + itemWidth > vWidth){
                    hcount += maxHeight + params.topMargin + params.bottomMargin;

                    /** 当childView总高度大于parentView的高度时显示 有省略号的childView */
                    if (hcount + itemHeight > vHeight){
                        hcount -= maxHeight + params.topMargin + params.bottomMargin;

                        /** 如果有自定义省略号的childView则显示，否则不显示 */
                        if (hasEllipsisView == 1){
                            childView = getChildAt(childCount);//ellipsisView;
                            if (childView==null) break;

                            MarginLayoutParams eParams = (MarginLayoutParams) childView.getLayoutParams();
                            if (eParams!=null){
                                if (eParams.leftMargin==0 || eParams.topMargin==0){
                                    eParams.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin);
                                }
                                params = eParams;
                            }
                            cWidth = childView.getMeasuredWidth();
//                            cHeight = childView.getMeasuredHeight();
                            itemWidth = params.leftMargin + cWidth + params.rightMargin;

                            if (wcount + itemWidth <= vWidth){
//                                Log.i(TAG, "--ellipsisView--"+wcount+","+hcount+","+cWidth+","+cHeight);
                                childView.layout(wcount + params.leftMargin, hcount + params.topMargin,
                                        wcount + cWidth + params.rightMargin, hcount + cHeight + params.bottomMargin);
                            }
                        }
                        break;
                    }
                    wcount = pL;
                    lineNum++;
                }
                childView.layout(wcount + params.leftMargin, hcount + params.topMargin,
                        wcount + cWidth + params.rightMargin, hcount + cHeight + params.bottomMargin);

                wcount += itemWidth;
                maxHeight = Math.max(maxHeight, cHeight);
            }
        }
    }

    /**
     * childView的行数
     * @return
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * 省略时显示的View
     * @param v
     */
    public void setEllipsisView(View v){
        setEllipsisView(v, new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT));
    }

    /**
     * 省略时显示的View
     * @param v
     * @param params
     */
    public void setEllipsisView(View v, LayoutParams params){
        if (v!=null){
            if (params==null){
                params = new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);
            }
            addView(v, params);
            hasEllipsisView = 1;
        }
    }

    @Override
    public int getChildCount() {
        return super.getChildCount()-hasEllipsisView;
    }
}
