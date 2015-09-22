package com.qing.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zwq on 2015/09/18 16:41.<br/><br/>
 * 可循环的ViewPager
 */
public class CycleViewPager extends ViewPager {

    private static final String TAG = CycleViewPager.class.getName();
    private boolean cycle;
    private CyclePagerAdapter mCyclePagerAdapter;
    private OnPageChangeListener mOnPageChangeListener;
    private Timer mTimer;
    private long period = 2000;

    public CycleViewPager(Context context) {
        super(context);

    }

    public boolean isCycle(){
        return cycle;
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
            setOnPageChangeListener(null);
        }
        super.setAdapter(adapter);
        if (cycle){
            setCurrentItem(1);
        }
    }

    @Override
    public void setOnPageChangeListener(final OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
        if (cycle){
            mOnPageChangeListener = new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                    if (listener != null)
                        listener.onPageScrolled(i, v, i1);
                }

                @Override
                public void onPageSelected(int position) {
                    if (listener != null) {
                        listener.onPageSelected(position);
                    }
                    if (position == 0){
                        setCurrentItem(mCyclePagerAdapter.getRealCount(), false);
                    }else if(position == mCyclePagerAdapter.getRealCount()+1){
                        setCurrentItem(1, false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int position) {
                    if (listener != null)
                        listener.onPageScrollStateChanged(position);
                }
            };
        }
        super.setOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    public void addOnPageChangeListener(final OnPageChangeListener listener) {
        super.addOnPageChangeListener(listener);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                setCurrentItem(getCurrentItem() + 1, true);
            }
        }
    };

    /**
     * 开始自动滚动
     */
    public void startAutoScroll(){
        if (cycle) {
            if (mTimer == null){
                mTimer = new Timer();
            }
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (cycle) {
                        mHandler.sendEmptyMessage(1);
                    }
                }
            }, 1000, period);
        }
    }

    /**
     * 停止自动滚动
     */
    public void stopAutoScroll(){
        if (mTimer != null){
            mTimer.cancel();
//            mTimer = null;
        }
    }

    /**
     * 自动滚动时间间隔，默认 2s
     * @param period
     */
    public void setAutoScrollPeriodTime(long period){
        if (period != this.period){
            if (period <= 0){
                period = 1000;
            }
            this.period = period;
            stopAutoScroll();
            startAutoScroll();
        }
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
