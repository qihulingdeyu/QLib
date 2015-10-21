package com.qing.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by zwq on 2015/09/18 16:41.<br/><br/>
 * 可循环的ViewPager
 */
public class CycleViewPager extends ViewPager {

    private static final String TAG = CycleViewPager.class.getName();
    private CyclePagerAdapter mCyclePagerAdapter;
    private boolean isAutoScroll;//是否自动滚动
    private boolean cycle;//是否循环
    private final int SCROLL_WHAT = 1;
    private long interval = 3000;//自动滚动默认时间间隔

    private boolean stopScrollWhenTouch = true;
    private boolean stopByTouch;
    private boolean canChangeDirection;
    private int direction = 1;

    public CycleViewPager(Context context) {
        super(context);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        cycle = false;
    }

    /** 可循环的适配器 **/
    public final void setCycleAdapter(CyclePagerAdapter adapter){
        if (adapter != null) {
            cycle = true;
            mCyclePagerAdapter = adapter;
            adapter.setCycle(cycle);
        }
        super.setAdapter(adapter);
        if (cycle){
            setCurrentItem(1, false);
        }
    }

    public boolean isCycle(){
        return cycle;
    }

    public void setStopScrollWhenTouch(boolean stop){
        stopScrollWhenTouch = stop;
    }

    public void setCanChangeDirection(boolean change){
        canChangeDirection = change;
    }

    private float p1x1;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (stopScrollWhenTouch) {
            if (isAutoScroll && ev.getAction() == MotionEvent.ACTION_DOWN) {
                stopByTouch = true;
                stopAutoScroll();
                p1x1 = ev.getX();
            }else if (stopByTouch && ev.getAction() == MotionEvent.ACTION_UP) {
                checkCycle(getCurrentItem());
                stopByTouch = false;
                startAutoScroll();
                if (canChangeDirection){
                    float dx = ev.getX() - p1x1;
                    if (dx > 0){
                        direction = -1;
                    }else{
                        direction = 1;
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SCROLL_WHAT) {
                scrollOnce();
                sendScrollMessage(interval);
            }
        }
    };

    private void scrollOnce() {
        int item = getCurrentItem() + direction;
        setCurrentItem(item, true);
    }

    /** remove messages before, keeps one message is running at most **/
    private void sendScrollMessage(long delayTimeInMills) {
        mHandler.removeMessages(SCROLL_WHAT);
        mHandler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        checkCycle(getCurrentItem());
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
        checkCycle(getCurrentItem());
    }

    /**
     * 检查是否达到循环点
     * @param item
     * @return
     */
    private final boolean checkCycle(int item){
        if (cycle){
            if (item <= 0){
                setCurrentItem(mCyclePagerAdapter.getRealCount(), false);
                return true;
            }else if(item >= mCyclePagerAdapter.getRealCount() + 1){
                setCurrentItem(1, false);
                return true;
            }
        }
        return false;
    }

    /**
     * 开始自动滚动
     */
    public final void startAutoScroll(){
        isAutoScroll = true;
        sendScrollMessage(interval);
    }

    /**
     * 停止自动滚动
     */
    public final void stopAutoScroll(){
        isAutoScroll = false;
        mHandler.removeMessages(SCROLL_WHAT);
    }

    /**
     * 自动滚动时间间隔，默认 3s
     * @param interval
     */
    public void setInterval(long interval){
        if (interval != this.interval){
            if (interval <= 0){
                interval = 1000;
            }
            this.interval = interval;
            stopAutoScroll();
            startAutoScroll();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
    }

    /**
     * Created by zwq on 2015/09/18 16:11.<br/><br/>
     * 可以循环的PagerAdapter
     */
    public static abstract class CyclePagerAdapter extends PagerAdapter {

        private final String TAG = CyclePagerAdapter.class.getName();
        private int cycle = 0;

        public final void setCycle(boolean cycle){
            if (cycle){
                this.cycle = 2;
            }else{
                this.cycle = 0;
            }
        }

        public boolean isCycle(){
            return cycle==2? true: false;
        }

        /** 真正的大小 */
        public abstract int getRealCount();

        @Override
        public final int getCount() {
            return getRealCount()+cycle;
        }

        public final int getPosition(int position){
            //   0 1 2
            // 0 1 2 3 4
            // 2 0 1 2 0
            if (isCycle() && getRealCount() > 1){
                if (position == 0){
                    position = getRealCount()-1;
                }else if(position == getCount()-1){
                    position = 0;
                }else{
                    position = position-1;
                }
            }
            return position;
        }

        @Override
        public final boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        public abstract Object instantiateCycleItem(ViewGroup container, int position);
        public abstract void destroyCycleItem(ViewGroup container, int position, Object object);

        @Override
        public final Object instantiateItem(ViewGroup container, int position) {
            return instantiateCycleItem(container, getPosition(position));
        }

        @Override
        public final void destroyItem(ViewGroup container, int position, Object object) {
            destroyCycleItem(container, position, object);
//        destroyCycleItem(container, getPosition(position), object);
        }

        @Deprecated
        public Object instantiateCycleItem(View container, int position) {
            return null;
        }
        @Deprecated
        public void destroyCycleItem(View container, int position, Object object){ }

        @Override
        public final Object instantiateItem(View container, int position) {
            return instantiateCycleItem(container, getPosition(position));
        }

        @Override
        public final void destroyItem(View container, int position, Object object) {
            destroyCycleItem(container, position, object);
//        destroyCycleItem(container, getPosition(position), object);
        }
    }
}
