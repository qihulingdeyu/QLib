package com.qing.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;

public abstract class RefreshAdaterView<T extends AbsListView> extends RefreshLayoutView<T> {

    public RefreshAdaterView(Context context) {
        this(context, null);
    }

    public RefreshAdaterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshAdaterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setOnScrollListener();
    }
    
    public void setAdapter(ListAdapter adapter) {
        mContentView.setAdapter(adapter);
    }

    public ListAdapter getAdapter() {
        return mContentView.getAdapter();
    }
    
    public int getFirstVisiblePosition(){
        return mContentView.getFirstVisiblePosition();
    }
    
    public int getLastVisiblePosition(){
        return mContentView.getLastVisiblePosition();
    }
    
    private void setOnScrollListener(){
        if(mContentView!=null){
            mContentView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
                
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                        int visibleItemCount, int totalItemCount) {
                    
                    setScrollState(view, firstVisibleItem, visibleItemCount,
                            totalItemCount);
                }
            });
        }
    }
    
    /**
     * 计算第position项距离view顶部的距离
     * @param view
     * @param position
     * @return
     */
    public int getAbsListViewScrollY(AbsListView view, int position) {
        View item = view.getChildAt(position);
        if (item == null) {
            return 0;
        }
        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = item.getTop();
        return -top + firstVisiblePosition * item.getHeight() ;
    }
    
    /**
     * 判断AbsListView是否滚动顶部或底部<br/>
     * 此方法必须在AbsListView的setOnScrollListener的onScroll方法内调用
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */
    public void setScrollState(AbsListView view, int firstVisibleItem, 
            int visibleItemCount, int totalItemCount) {
        
        int scrollY = getAbsListViewScrollY(view, 0);
        
        if(scrollY<=0 && firstVisibleItem==0){
            setCurrentScrollState(ScrollState.IsTop);
            
        }else if(firstVisibleItem+visibleItemCount==totalItemCount){
            View lastItem = view.getChildAt(visibleItemCount-1);
            int lastItemHeight = lastItem.getHeight();
            int[] lastItemLocation = new int[2];
            lastItem.getLocationOnScreen(lastItemLocation);
            
            int viewHeight = view.getHeight();
            int[] viewLocation = new int[2];
            view.getLocationOnScreen(viewLocation);
            
            if(lastItemLocation[1]+lastItemHeight >= viewLocation[1]+viewHeight){
                setCurrentScrollState(ScrollState.IsBottom);
            }
            
        }else{
            setCurrentScrollState(ScrollState.IsNormal);
        }
        
        setContentViewScrollY(scrollY);
//        Log.i(TAG, "--onScroll--sy:"+scrollY);
    }
}
