package com.qing.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qing.callback.HttpCallback;
import com.qing.qlib.IPage;
import com.qing.ui.RefreshAdaterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwq on 2015/07/14 17:55.<br/><br/>
 */
public class MyListView extends RefreshAdaterView<AbsListView> implements IPage {

    /**
     * @param context
     */
    
    public MyListView(Context context) {
        super(context);
        
        this.setBackgroundColor(0xFFF7AFB2);
    }

    @Override
    protected View setHeaderView(Context context) {
        return null;
    }
    ListView listView;
    @Override
    protected View setContentView(final Context context) {
        
        listView = new ListView(context);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        listView.setVerticalFadingEdgeEnabled(false);
        listView.setBackgroundColor(Color.CYAN);
        final MAdapter ad = new MAdapter();
        
//        listView.setAdapter(ad);
        mContentView = listView;
        setAdapter(ad);
        return listView;
    }
    
    class MAdapter extends BaseAdapter {
        
        private List<ItemInfo> list = new ArrayList<ItemInfo>();
        private int count = 100;
        
        public MAdapter(){
            for (int i = 0; i < count; i++) {
                list.add(new ItemInfo(i,"item**"+i, "onclick"));
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
        ViewHolder holder = null;
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                AbsListView.LayoutParams ap = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
                
                holder = new ViewHolder(getContext());
                holder.setLayoutParams(ap);
                
                convertView = holder;
                convertView.setTag(holder);
                
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            
            ItemInfo info = list.get(position);
            holder.setChangeListener(info);//
            holder.tv.setText(info.getName());
            
            holder.tv1.setTag(position);
            holder.tv1.setText(info.getValue());
            
            
            holder.tv1.setOnClickListener(new MOnClickListener(info));
            return convertView;
        }
        
        class MOnClickListener implements View.OnClickListener{
            boolean isClick;
            ItemInfo mInfo;
            public MOnClickListener(ItemInfo info) {
                mInfo = info;
            }
            @Override
            public void onClick(View v) {
                if(isClick) return;
                isClick = true;
                
                mInfo.getDownloadTask().startDownload();
            }
        }
        
        class ViewHolder extends LinearLayout{
            public ViewHolder(Context context) {
                super(context);
                initView(context);
            }
            TextView tv;
            TextView tv1;
            HttpCallback cb;
            private void initView(Context context) {
                LayoutParams lP = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT, 1);
                tv = new TextView(context);
                tv.setTextSize(24);
                tv.setPadding(10, 10, 10, 10);
                tv.setTextColor(Color.BLACK);
                this.addView(tv, lP);
                
                tv1 = new TextView(context);
                tv1.setTextSize(24);
                tv1.setPadding(10, 10, 10, 10);
                tv1.setTextColor(Color.BLACK);
                this.addView(tv1, lP);
            }
            
            public void setChangeListener(final ItemInfo info){
                info.setDataChangeListener(new ItemInfo.onDataChangeListener() {
                    @Override
                    public void onChange(ItemInfo info) {
//                        Log.i("bbb", tv1.getTag().toString());
                        if(Integer.parseInt(tv1.getTag().toString())==info.getId()){
                            tv1.setText(info.getValue());
                        }
                    }
                });
            }
        }
    }
    

    @Override
    protected View setFooterView(Context context) {
        return null;
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
