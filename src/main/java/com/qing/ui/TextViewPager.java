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

import com.qing.utils.UIUtil;

/**
 * Created by zwq on 2015/09/06 12:06.<br/><br/>
 */
public class TextViewPager extends RelativeLayout {

    private static final String TAG = TextViewPager.class.getName();

    private String[] textArr = new String[]{
            "Original",  "Jasmine", "Camillia", "Rosa", "Lavender", "Sunflower", "Clover", "Peach", "Dandelion", "Lilac", "Tulip"
    };
    private int[] itemWidth = new int[textArr.length+2];
    private int[] itemCenterPoint = new int[textArr.length+2];
    private TextView[] textViewArr = new TextView[textArr.length];

    private MyHorizontalScrollView myHorizontalScrollView;
    private static int screenW = 720;
    private static int position = 0;
    private int lastId = -1;
    private Context mContext;
    private RelativeLayout textLayout;

    private final int ID_DOT_VIEW = 1;

    public TextViewPager(Context context) {
        super(context);
        mContext = context;
        screenW = UIUtil.getScreenW();
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setPadding(0, UIUtil.getRealPixel720(10), 0, UIUtil.getRealPixel720(10));

        // 小圆点
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ImageView dotView = new ImageView(mContext);
        dotView.setId(ID_DOT_VIEW);
//        iv.setImageResource(R.drawable.camera_filter_selected);//中间的小圆点
        this.addView(dotView, rParams);

        //文字布局
        rParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.BELOW, ID_DOT_VIEW);
        rParams.topMargin = UIUtil.getRealPixel720(5);
        myHorizontalScrollView = new MyHorizontalScrollView(mContext);
        myHorizontalScrollView.setHorizontalFadingEdgeEnabled(false);
        myHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        if(Build.VERSION.SDK_INT >= 11){
            myHorizontalScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        myHorizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    changeSelector = true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//					int xx = myHorizontalScrollView.getScrollX();
//					Log.i("bbb", "--------xx:"+xx);
////					change(xx);
                }
                return false;
            }
        });
        this.addView(myHorizontalScrollView, rParams);

        LinearLayout contentLayout = new LinearLayout(mContext);
        myHorizontalScrollView.addView(contentLayout);

        //第一个空白布局
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(screenW /2, LayoutParams.WRAP_CONTENT);
        RelativeLayout firstEmpty = new RelativeLayout(mContext);
        contentLayout.addView(firstEmpty, lParams);

        for(int i=0; i<textArr.length; i++) {
            textLayout = new RelativeLayout(mContext);
            TextView textView = new TextView(mContext);
            textView.setId(i);
            textView.setText(textArr[i]);
            textView.setTextColor(Color.WHITE);

            int textWidth = textArr[i].length() * UIUtil.getRealPixel720(12) + UIUtil.getRealPixel720(30)*2;
            itemWidth[1+i] = textWidth;
            textViewArr[i] = textView;

            lParams = new LinearLayout.LayoutParams(textWidth, LayoutParams.WRAP_CONTENT);
            contentLayout.addView(textLayout, lParams);

            rParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            textLayout.addView(textView, rParams);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    position = id;
                    myHorizontalScrollView.scrollTo(itemCenterPoint[id], 0);
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
        //最后一个空白布局
        RelativeLayout lastEmpty = new RelativeLayout(mContext);
        lParams = new LinearLayout.LayoutParams(screenW /2, LayoutParams.WRAP_CONTENT);
        contentLayout.addView(lastEmpty, lParams);

        itemWidth[0] = screenW /2 - itemWidth[1]/2;
        itemWidth[textArr.length+1] = screenW /2 - itemWidth[textArr.length]/2;

        //相邻两个的中心距离
        int oldWidthSpace = 0;
        for(int i=1; i<itemWidth.length-1; i++) {
            int space = oldWidthSpace + itemWidth[i]/2 + itemWidth[i+1]/2;
            itemCenterPoint[i] = space;
            oldWidthSpace = space;
        }

        lParams = (LinearLayout.LayoutParams) firstEmpty.getLayoutParams();
        lParams.width = itemWidth[0];
        firstEmpty.setLayoutParams(lParams);

        lParams = (LinearLayout.LayoutParams) lastEmpty.getLayoutParams();
        lParams.width = itemWidth[textArr.length+1];
        lastEmpty.setLayoutParams(lParams);
    }

    private boolean changing = false;

    /**
     * 移动一个
     * @param dir
     */
    public void moveOne(int dir) {
        if(changing) return;
        changing = true;

        distanceX = 0;
        if(dir < 0){
            position--;
            if(position<0)position=0;
        }else{
            position++;
            if(position>= textArr.length)position=0;//textArr.length-1;
        }

        myHorizontalScrollView.scrollTo(itemCenterPoint[position], 0);
        setSelectedTextColor(lastId, position);
        lastId = position;

        changing = false;
    }

    public void initFilterColor(int id){
        if(id<0 || id>= itemCenterPoint.length){
            id = 0;
        }
        position = id;
        myHorizontalScrollView.post(new Runnable() {
            @Override
            public void run() {
                myHorizontalScrollView.scrollTo(itemCenterPoint[position], 0);
            }
        });

        setSelectedTextColor(lastId, position);
        lastId = position;
    }

    private void change(int distance) {
        if(changing) return;
        changing = true;

        int id = position;
        for(int i=0; i<itemCenterPoint.length-1; i++) {
//			Log.i("bbb", "i:"+i+",it_c:"+itemCenterPoint[i]);
            if(distance >= (itemCenterPoint[i] - itemWidth[i+1]/2)){
                id = i;
            }
        }
        position = id;
        myHorizontalScrollView.scrollTo(itemCenterPoint[id], 0);

//        if(mListener!=null)
//            mListener.onClickColor_selector(id-1);

        setSelectedTextColor(lastId, id);
        lastId = id;
        changeSelector = false;

        changing = false;
    }

    public int getCurrentPosition(){
        return position;
    }

    private int lastColorIndex;
    public int getLastColorIndex() {
        return lastColorIndex;
    }

    public void setLastColorIndex(int lastColorIndex) {
        this.lastColorIndex = lastColorIndex;
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
        if(textLayout == null) return;
//		Log.i("bbb", "lastId:"+lastId+",curId:"+curId+",count:"+textArr.length);
        if(lastId>=0 && lastId<textViewArr.length){
            TextView tv = textViewArr[lastId];
            if(tv!=null)
                tv.setTextColor(Color.WHITE);
        }
        if(curId>=0 && curId<textViewArr.length){
            TextView tv = textViewArr[curId];
            if(tv!=null)
                tv.setTextColor(0xff32bea0);
        }
    }

    private int lastX, currentX;
    private int distanceX = 0;
    private int move;
    public boolean setScrollEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            move = 0;
            lastX = (int) event.getX();
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            currentX = (int) event.getX();
            if(Math.abs(lastX - currentX)< UIUtil.getScreenW()/4){
                distanceX += (lastX - currentX);
            }else{
                distanceX = 0;
            }
            myHorizontalScrollView.scrollBy((lastX - currentX) / 3, 0);
            lastX = currentX;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            lastX = 0;
            if(Math.abs(distanceX)>= UIUtil.getScreenW()/5){
//				Log.i("bbb", "distanceX:"+distanceX);
                move = distanceX;
                moveOne(move > 0 ? 1 : -1);
                distanceX = 0;
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
