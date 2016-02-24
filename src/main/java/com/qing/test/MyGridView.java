package com.qing.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.qing.qlib.IPage;
import com.qing.widget.RefreshAdaterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwq on 2015/07/17 16:47.<br/><br/>
 */
public class MyGridView extends RefreshAdaterView<AbsListView> implements IPage {

    public MyGridView(Context context) {
        super(context);
        this.setBackgroundColor(0xFFF7AFB2);
    }

    @Override
    protected View setHeaderView(Context context) {
        return null;


    }
    @Override
    protected View setContentView(Context context) {
        GridView gridView = new GridView(context);
        gridView.setNumColumns(2);
        gridView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        gridView.setVerticalFadingEdgeEnabled(false);
        gridView.setBackgroundColor(Color.CYAN);
        final MAdapter ad = new MAdapter();
        mContentView = gridView;
        setAdapter(ad);


        //        gridView.smoothScrollBy(800, 0);
        //        gridView.invalidate();
        ////        mContentView.setSelection(6);
        return null;
    }

    @Override
    protected View setFooterView(Context context) {
        return null;
    }

    class MAdapter extends BaseAdapter {
        private List<String> list = new ArrayList<String>();
        private int count = 100;
        public MAdapter(){
            for (int i = 0; i < count; i++) {
                list.add("item**"+i);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = null;
            if(convertView==null){
                AbsListView.LayoutParams ap = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, 500);
                //                    Utils.getRealPixel3(100),Utils.getRealPixel3(40));
                tv = new TextView(getContext());
                //                tv.setBackgroundColor(Color.RED);
                tv.setTextSize(24);
                tv.setPadding(10, 10, 10, 10);
                tv.setTextColor(Color.BLACK);
                tv.setLayoutParams(ap);

                convertView = tv;
                tv.setTag(tv);
            }else{
                tv = (TextView) convertView.getTag();
            }
            tv.setText(list.get(position));
            return convertView;
        }
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public boolean onStop() {
        return false;
    }

    @Override
    public boolean onPause() {
        return false;
    }

    @Override
    public boolean onDestroy() {
        return false;
    }

    @Override
    public boolean onStart() {
        return false;
    }

    @Override
    public boolean onResume() {
        return false;
    }

    @Override
    public boolean onPageStateChange(boolean isTop, Object[] params) {
        return false;
    }

    @Override
    public Object[] transferPageData() {
        return null;
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public boolean onActivityKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onActivityKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onRestore() {
    }

}
