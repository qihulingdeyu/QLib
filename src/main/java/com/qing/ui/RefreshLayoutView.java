package com.qing.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by zwq on 2015/07/14 17:42.<br/><br/>
 * Scroller内嵌套ListView, GridView
 * @param <T>
 */
public abstract class RefreshLayoutView<T extends View> extends ViewGroup implements OnScrollListener{

    protected String TAG = this.getClass().getSimpleName();
    
    /**
     * 
     */
    protected Scroller mScroller;

    /**
     * 下拉时显示的header view
     */
    protected View mHeaderView;

    /**
     * 内容视图, 即用户触摸导致下拉刷新、上拉加载的主视图. 比如ListView, GridView等.
     */
    protected T mContentView;
    
    /**
     * 上拉时显示的footer view
     */
    protected View mFooterView;

    /** 最初的滚动位置.第一次布局时滚动header的高度的距离 */
    protected int mInitScrollY = 0;
    
    private int mInitHeaderHeight;
    private int mInitFooterHeight;
    
    private int mScreenHeight;
    private int mHeaderHeight;//真实高度
    private int mFooterHeight;//真实高度
    
    private int maxPullDownHeight;//最大下拉高度
    private int maxPullUpHeight;//最大上拉高度
    

    @SuppressWarnings("unused")
    public enum ScrollState {
        IsNormal(0),
        IsTop(1),
        IsBottom(2);
        private int state;
        private ScrollState(int state){
            this.state = state;
        }
    }
    private ScrollState currentState = ScrollState.IsTop;
    
    private boolean interceptTouch = false;//是否拦截
    
    private boolean headerShow;//headerview是否显示
    private boolean footerShow;//footerview是否显示
    
    /**
     * @param context
     */
    public RefreshLayoutView(Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public RefreshLayoutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public RefreshLayoutView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        // 初始化Scroller对象
        mScroller = new Scroller(context);

        // 获取屏幕高度
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
        // header 的高度为屏幕高度的 1/4
        // mHeaderHeight = mScreenHeight / 4;
        Log.i(TAG, "mScreenHeight =  " + mScreenHeight);

        // header 的高度为屏幕高度的 1/5
        mInitHeaderHeight = mScreenHeight / 5;
        Log.i(TAG, "mHeaderHeight =  " + mInitHeaderHeight);

        mInitFooterHeight = mScreenHeight / 5;

        // 初始化整个布局
        initLayout(context);
    }

    /**
     * 初始化整个布局
     * @param context
     */
    @SuppressWarnings("unchecked")
    private final void initLayout(Context context) {
        // header view
        View header = setHeaderView(context);
        if(mHeaderView==null){
            if(header==null){
                setDefaultHeaderView(context);
            }else{
                mHeaderView = header;
            }
        }
        mHeaderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mInitHeaderHeight));
        addView(mHeaderView);

        // 设置内容视图
        View view = setContentView(context);
        if(mContentView==null){
            if(view==null){
                setDefaultContentView(context);
//                throw new NullPointerException("ContentView is null!!");
            }else{
                mContentView = (T) view;
            }
        }
        // 设置布局参数
        setDefaultContentLayoutParams();
        addView(mContentView);

        // footer view
        View footer = setFooterView(context);
        if(mFooterView==null){
            if(footer==null){
                setDefaultFooterView(context);
            }else{
                mFooterView = footer;
            }
        }
        mFooterView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mInitFooterHeight));
        addView(mFooterView);
    }

    /**
     * 初始化 header view
     */
    protected abstract View setHeaderView(Context context);
    
    /**
     * 默认的header view
     * @param context
     */
    protected void setDefaultHeaderView(Context context) {
        TextView header = new TextView(context);
        header.setGravity(Gravity.CENTER);
        header.setText("--"+TAG+"--\n view header");
        header.setTextSize(18);
        mHeaderView = header;
    }

    /**
     * 初始化Content View, 子类覆写.
     */
    protected abstract View setContentView(Context context);
    
    @SuppressWarnings("unchecked")
    private void setDefaultContentView(Context context){
        TextView content = new TextView(context);
        content.setTextSize(20);
        content.setTextColor(Color.BLACK);
        content.setGravity(Gravity.CENTER);
        content.setBackgroundColor(Color.GRAY);
        content.setText("--ContentView--");
        mContentView = (T) content;
    }
    /**
     * 设置Content View的默认布局参数
     */
    protected void setDefaultContentLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(params);
    }
    
    /**
     * 初始化footer view
     */
    protected abstract View setFooterView(Context context);
    
    /**
     * 默认的footer view
     * @param context
     */
    protected void setDefaultFooterView(Context context) {
        TextView footer = new TextView(context);
        footer.setGravity(Gravity.CENTER);
        footer.setText("--"+TAG+"--\n view footer");
        footer.setTextSize(18);
        mFooterView = footer;
    }
    
    /**
     * 丈量视图的宽、高。宽度为用户设置的宽度，高度则为header, content view, footer这三个子控件的高度只和。
     * @see View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i(TAG, "  onMeasure(int widthMeasureSpec= " + widthMeasureSpec + " heightMeasureSpec= " + heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        int finalHeight = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != null) {
                // measure
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                // 该view所需要的总高度
                finalHeight += child.getMeasuredHeight();
            }
        }
//        Log.i(TAG, " width= " + width + " finalHeight= " + finalHeight);
        setMeasuredDimension(width, finalHeight);
    }
    

    /**
     * 布局函数，将header, content view,
     * footer这三个view从上到下布局。布局完成后通过Scroller滚动到header的底部，即滚动距离为header的高度 +
     * 本视图的paddingTop，从而达到隐藏header的效果.
     * @see ViewGroup#onLayout(boolean, int, int, int, int)
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int top = getPaddingTop();
        
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(0, top, child.getMeasuredWidth(), child.getMeasuredHeight() + top);
            top += child.getMeasuredHeight();
        }

        mHeaderHeight = mHeaderView.getMeasuredHeight();
        mFooterHeight = mFooterView.getMeasuredHeight();
        
        // 计算初始化滑动的y轴距离
        mInitScrollY = mHeaderHeight + getPaddingTop();
        
        if(mInitScrollY >= mScreenHeight/2){
            maxPullDownHeight = mInitScrollY;
        }else{
            maxPullDownHeight = mScreenHeight/2 - mInitScrollY;
        }
        
        if(mFooterHeight + getPaddingBottom() >= mScreenHeight/2){
            maxPullUpHeight = mFooterHeight + getPaddingBottom();
        }else{
            maxPullUpHeight = mScreenHeight/2 + mInitScrollY;
        }
        // 滑动到header view高度的位置, 从而达到隐藏header view的效果
        scrollTo(0, mInitScrollY);
    }
    
    /**
     * 设置当前的滚动状态
     * @param state
     */
    protected void setCurrentScrollState(ScrollState state){
        if(state!=currentState){
            currentState = state;
            if(currentState==ScrollState.IsNormal){
                interceptTouch = false;
            }else if(currentState==ScrollState.IsTop){
                interceptTouch = true;
            }else if(currentState==ScrollState.IsBottom){
                interceptTouch = true;
            }
        }
    }

    /**
     * 是否已经到了最顶部,子类需覆写该方法,使得mContentView滑动到最顶端时返回true, 如果到达最顶端用户继续下拉则拦截事件;
     * @return
     */
    protected final boolean isAbsListViewTop(){
        if(currentState==ScrollState.IsTop){
            return true;
        }
        return false;
    }

    /**
     * 是否已经到了最底部,子类需覆写该方法,使得mContentView滑动到最底端时返回true;从而触发自动加载更多的操作
     * @return
     */
    protected final boolean isAbsListViewBottom(){
        if(currentState==ScrollState.IsBottom){
            return true;
        }
        return false;
    }
    
    /**
     * header是否显示
     * @return
     */
    protected final boolean headerIsShow(){
        return headerShow;
    }
    
    /**
     * footer是否显示
     * @return
     */
    protected final boolean footerIsShow(){
        return footerShow;
    }
    
    private int mScrollY;
    protected void setContentViewScrollY(int scrollY){
        mScrollY = scrollY;
    }
    public int getContentViewScrollY(){
        return mScrollY;
    }

    /**
     * 在适当的时候拦截触摸事件，这里指的适当的时候是当mContentView滑动到顶部，并且是下拉时拦截触摸事件，否则不拦截，交给其child
     * view 来处理。
     * @see
     * ViewGroup#onInterceptTouchEvent(MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.i(TAG, "--onInterceptTouchEvent--ACTION_DOWN--");
                y1 = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.i(TAG, "--onInterceptTouchEvent--ACTION_MOVE--");
                y2 = event.getRawY();
                int dx = (int) Math.abs(y2-y1);
                if(dx>minStep){
                    dir = getMoveDir(y1, y2);
                    if(dir==-1 && currentState==ScrollState.IsTop){
                        interceptTouch = true;
                    }else if(dir==1 && currentState==ScrollState.IsBottom){
                        interceptTouch = true;
                    }else{
                        interceptTouch = false;
                    }
                }
                y1 = y2;
                break;
            case MotionEvent.ACTION_UP:
//                Log.i(TAG, "--onInterceptTouchEvent--ACTION_UP--");
                break;
            default:
                break;
        }
        return interceptTouch;// 子view处理该事件
    }

    
    /**
     * 在这里处理触摸事件以达到下拉刷新或者上拉自动加载的问题
     * @see View#onTouchEvent(MotionEvent)
     */
    private int minStep = 3;
    private float y1,y2;
    private int dir = 1;
    private boolean needMove;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
//            Log.i(TAG, "--onTouchEvent--ACTION_DOWN--");
            y1 = event.getRawY();
            break;
        case MotionEvent.ACTION_MOVE:
//            Log.i(TAG, "--onTouchEvent--ACTION_MOVE--");
            if(headerShow || footerShow){
                interceptTouch = true;
            }else{
                interceptTouch = false;
            }
            
            y2 = event.getRawY();
            int dx = (int) Math.abs(y2-y1);
            if(dx>minStep){
                dir = getMoveDir(y1, y2);
                
                if(interceptTouch){
                    int sy = dir*dx;
                    if(currentState==ScrollState.IsTop){
                        if(dir==1 && getScrollY()>mInitScrollY){
                            break;
                        }
                        //当headerView显示时, 向上滑动headerView高度的1/4则隐藏headerView
                        if(dir==1 && getScrollY() >= mHeaderHeight/4){
                            needMove = true;
                        }
                        //当下拉高度大于或等于 最大下拉高度时停止滚动
                        if(getScrollY() >= -maxPullDownHeight){
                            scrollBy(0, sy);
                        }
                        
                    }else if(currentState==ScrollState.IsBottom){
                        if(dir==-1 && getScrollY()<mInitScrollY){
                            break;
                        }
                        //当footerView显示时, 向下滑动footerView高度的1/4则隐藏footerView
                        if(dir==-1 && getScrollY() <= mInitScrollY+mFooterHeight/4*3){
                            needMove = true;
                        }
                        //当上拉高度大于或等于 最大上拉高度时停止滚动
                        if(getScrollY() <= maxPullUpHeight){
                            scrollBy(0, sy);
                        }
                    }
                    
                }else{
                    if(dir==-1 && currentState==ScrollState.IsTop){
                        interceptTouch = true;
                        headerShow = true;
                    }else if(dir==1 && currentState==ScrollState.IsBottom){
                        interceptTouch = true;
                        footerShow = true;
                    }
                }
            }
            y1 = y2;
            break;
        case MotionEvent.ACTION_UP:
//            Log.i(TAG, "--onTouchEvent--ACTION_UP--");
            
            if(needMove){
                //滚动到初始状态
                scrollTo(0, mInitScrollY);
                headerShow = false;
                footerShow = false;
                interceptTouch = false;
            }
            
            if(!needMove && headerShow && !footerShow){
                if(getScrollY() <= mInitScrollY/2){
                    //headerView完全显示
                    scrollTo(0, 0);
                    headerShow = true;
                }else{
                    scrollTo(0, mInitScrollY);
                    headerShow = false;
                    interceptTouch = false;
                }
                footerShow = false;
            }
            
            if(!needMove && !headerShow && footerShow){
                if(getScrollY() >= mInitScrollY + mFooterHeight/2){
                    //footerView完全显示
                    scrollTo(0, mInitScrollY + mFooterHeight);
                    footerShow = true;
                }else{
                    scrollTo(0, mInitScrollY);
                    footerShow = false;
                    interceptTouch = false;
                }
                headerShow = false;
            }
            
            needMove = false;
            break;
            
        default:
            break;
        }
        return true;
    }
    
    /**
     * 获取滑动方向
     * @param y1
     * @param y2
     * @return  1：向上滑 ，  -1：向下滑
     */
    private int getMoveDir(float y1, float y2){
        if(y2-y1>=0){
            return -1;
        }
        return 1;
    }
    
    /**
     * 与Scroller合作,实现平滑滚动。在该方法中调用Scroller的computeScrollOffset来判断滚动是否结束。如果没有结束，
     * 那么滚动到相应的位置，并且调用postInvalidate方法重绘界面，从而再次进入到这个computeScroll流程，直到滚动结束。
     */
    @Override
    public void computeScroll() {
//        Log.i(TAG, " computeScroll()"+getScrollY());
        if(getScrollY() < mInitScrollY){
            headerShow = true;
            footerShow = false;
        }else if(getScrollY() > mInitScrollY){
            headerShow = false;
            footerShow = true;
        }
    }

    /**
     * 返回Header View
     * @return
     */
    public View getHeaderView() {
        return mHeaderView;
    }
    
    /**
     * 返回Content View
     * @return
     */
    public T getContentView() {
        return mContentView;
    }

    /**
     * 返回Footer View
     * @return
     */
    public View getFooterView() {
        return mFooterView;
    }
    
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.i("scroll", "onScrollStateChanged()-- mScroller.getCurrY()---> " + mScroller.getCurrY());
    }
    
    /**
     * 滚动监听，当滚动到最底部，且用户设置了加载更多的监听器时触发加载更多操作.
     * @see OnScrollListener
     * AbsListView, int, int, int)
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        Log.i("scroll", "onScroll()-- mScroller.getCurrY()---> " + mScroller.getCurrY());
        // 用户设置了加载更多监听器，且到了最底部，并且是上拉操作，那么执行加载更多.
    }
    
    public interface OnViewStateChange{
        public void onShow();
        /**
         * @param direction 向上/向左：1，向下/向右：-1
         */
        public void onChanging(int direction);
        public void onHide();
    }
}
