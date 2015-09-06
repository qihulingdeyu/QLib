package com.qing.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qing.utils.UIUtils;

/**
 * Created by zwq on 2015/09/06 12:06.<br/><br/>
 */
public class TextViewPager extends RelativeLayout {

    private static final String TAG = TextViewPager.class.getName();

    private String[] color_nm = new String[]{
            "Original",  "Jasmine", "Camillia", "Rosa", "Lavender", "Sunflower", "Clover", "Peach", "Dandelion", "Lilac", "Tulip"
    };

    private static int screen_w = 720;
    private static int position = 0;

    private int[] item_w = new int[color_nm.length+2];
    private int[] item_c = new int[color_nm.length+2];

    private TextView[] tvs = new TextView[color_nm.length];
    private MyHorizontalScrollView sc;

    public TextViewPager(Context context) {
        super(context);
        screen_w = UIUtils.getScreenW();
        init();
    }

    private RelativeLayout srr;
    private int lastId = -1;
    @SuppressLint("ClickableViewAccessibility")
    void init() {
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setPadding(0, UIUtils.getRealPixel720(10), 0, UIUtils.getRealPixel720(10));

        sc = new MyHorizontalScrollView(this.getContext());
        sc.setHorizontalFadingEdgeEnabled(false);
        if(Build.VERSION.SDK_INT >= 11){
            sc.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        ImageView iv = new 	ImageView(this.getContext());
        iv.setId(10001);
//        iv.setImageResource(R.drawable.camera_filter_selected);//中间的小圆点

        LinearLayout ll = new LinearLayout(this.getContext());

        RelativeLayout srr_first = new RelativeLayout(this.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screen_w/2, LayoutParams.WRAP_CONTENT);
        ll.addView(srr_first, lp);

        for(int i=0;i<color_nm.length;i++) {
            srr = new RelativeLayout(this.getContext());
            TextView tv = new TextView(this.getContext());
            tv.setId(i);
            tv.setText(color_nm[i]);
            tv.setTextColor(Color.WHITE);

            tvs[i] = tv;
            int w = color_nm[i].length()*UIUtils.getRealPixel720(12) + UIUtils.getRealPixel720(30)*2;
            item_w[1+i] = w;

            lp = new LinearLayout.LayoutParams(w, LayoutParams.WRAP_CONTENT);
            ll.addView(srr, lp);

            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp2.addRule(RelativeLayout.CENTER_HORIZONTAL);
            srr.addView(tv, lp2);

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();

                    position = id;
                    sc.scrollTo(item_c[id], 0);

//                    if(mListener!=null)
//                        mListener.onClickColor_selector(id-1);

                    setSelectedTextColor(lastId, id);
                    lastId = id;
                }
            });
            if(i==0){
                setSelectedTextColor(lastId, i);
                lastId = i;
            }
        }
        RelativeLayout srr_last = new RelativeLayout(this.getContext());
        lp = new LinearLayout.LayoutParams(screen_w/2, LayoutParams.WRAP_CONTENT);
        ll.addView(srr_last, lp);

        item_w[0] = screen_w/2 - item_w[1]/2;
        item_w[color_nm.length+1] = screen_w/2 - item_w[color_nm.length]/2;
        int old_ws = 0;

        for(int i=1;i<item_w.length-1;i++) {
            int s = old_ws + item_w[i]/2 + item_w[i+1]/2;
            item_c[i] = s;
            old_ws = s;
        }

        LinearLayout.LayoutParams lpt = (android.widget.LinearLayout.LayoutParams) srr_first.getLayoutParams();
        lpt.width = item_w[0];
        srr_first.setLayoutParams(lpt);

        lpt =  (android.widget.LinearLayout.LayoutParams) srr_last.getLayoutParams();
        lpt.width = item_w[color_nm.length+1];
        srr_last.setLayoutParams(lpt);

        sc.addView(ll);
        sc.setHorizontalScrollBarEnabled(false);

        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        this.addView(iv,lp1);

        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.BELOW,10001);
        lp2.topMargin = UIUtils.getRealPixel720(5);
        this.addView(sc,lp2);

        sc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    changeSelector = true;

                }else if(event.getAction() == MotionEvent.ACTION_UP) {
//					int xx = sc.getScrollX();
//					Log.i("bbb", "--------xx:"+xx);
////					change(xx);
                }
                return false;
            }
        });
    }

//    private CameraLayout.CameraControlClickListener mListener;
//    public void setClickListener(CameraLayout.CameraControlClickListener listener) {
//        mListener = listener;
//    }

    public int getCurrentColorId(){
        return position;
    }

    private int lastColorIndex;
    public int getLastColorIndex() {
        return lastColorIndex;
    }

    public void setLastColorIndex(int lastColorIndex) {
        this.lastColorIndex = lastColorIndex;
    }

    private boolean changing = false;
    public void move_one(int d) {
        if(changing) return;
        changing = true;

        dx = 0;
        if(d<0){
            position--;
            if(position<0)position=0;
        }else{
            position++;
            if(position>=color_nm.length)position=0;//color_nm.length-1;
        }

        sc.scrollTo(item_c[position], 0);
        setSelectedTextColor(lastId, position);
        lastId = position;

        changing = false;
    }

    public void initFilterColor(int id){
        if(id<0 || id>=item_c.length){
            id = 0;
        }
        position = id;
        sc.post(new Runnable() {
            @Override
            public void run() {
                sc.scrollTo(item_c[position], 0);
            }
        });

        setSelectedTextColor(lastId, position);
        lastId = position;
    }

    private void change(int xx) {
        if(changing) return;
        changing = true;

        int id = position;
        for(int i=0;i<item_c.length-1;i++) {
//			Log.i("bbb", "i:"+i+",it_c:"+item_c[i]);
            if(xx>=(item_c[i]-item_w[i+1]/2)){
                id = i;
            }
        }
        position = id;
        sc.scrollTo(item_c[id], 0);

//        if(mListener!=null)
//            mListener.onClickColor_selector(id-1);

        setSelectedTextColor(lastId, id);
        lastId = id;
        changeSelector = false;

        changing = false;
    }

    private boolean changeSelector = false;
    public boolean isChangeSelector() {
        return changeSelector;
    }
    public void setChangeSelector(boolean changeSelector) {
        this.changeSelector = changeSelector;
    }

    /**设置被选中的文字颜色*/
    private void setSelectedTextColor(int lastId, int curId){
        if(srr==null) return;
//		Log.i("bbb", "lastId:"+lastId+",curId:"+curId+",count:"+color_nm.length);
        if(lastId>=0 && lastId<tvs.length){
            TextView tv = tvs[lastId];
            if(tv!=null)
                tv.setTextColor(Color.WHITE);
        }
        if(curId>=0 && curId<tvs.length){
            TextView tv = tvs[curId];
            if(tv!=null)
                tv.setTextColor(0xff32bea0);
        }
    }

    private int lx,cx;
    private int dx = 0;
    private int move;
    public boolean setScrollEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            move = 0;
            lx = (int) event.getX();
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            cx = (int) event.getX();
            if(Math.abs(lx-cx)<UIUtils.getScreenW()/4){
                dx += (lx-cx);
            }else{
                dx = 0;
            }
            sc.scrollBy((lx-cx)/3, 0);
            lx = cx;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            lx = 0;
            if(Math.abs(dx)>=UIUtils.getScreenW()/5){
//				Log.i("bbb", "dx:"+dx);
                move = dx;
                move_one(move>0?1:-1);
                dx = 0;
            }
        }
        return move!=0?true:false;
    }

    class MyHorizontalScrollView extends HorizontalScrollView {
        public MyHorizontalScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onScrollChanged(int x, int y, int oldx, int oldy) {
            super.onScrollChanged(x, y, oldx, oldy);
            changeSelector = true;
//			Log.i("bbb", "x:"+x);//+",oldx;"+oldx);
            change(x);
        }
    }
}
