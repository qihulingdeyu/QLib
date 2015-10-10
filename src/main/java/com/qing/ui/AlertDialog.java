package com.qing.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qing.utils.DrawableUtils;
import com.qing.utils.UIUtils;

/**
 * Created by zwq on 2015/07/23 10:11.<br/><br/>
 * 自定义提示对话框
 */
public class AlertDialog extends Dialog {

    private AlertDialog dialog;
    private Context mContext;
    private int vWidth;
    private int vHeight;
    
    private int mWidth;
    private int mHeight;
    
    private LinearLayout containerLayout;
    private LinearLayout.LayoutParams lParams;
    
    public AlertDialog(Context context) {
        this(context, (int)(UIUtils.getScreenW()*0.8f), (int)(UIUtils.getScreenH()*0.05f));
    }
    /**
     * width和height是dialog中间内容布局的宽高
     * @param context
     * @param width
     * @param height
     */
    public AlertDialog(Context context, int width, int height) {
        super(context);
        dialog = this;
        mContext = context;
        
//        WindowManager manager = getWindow().getWindowManager();
//        vWidth = (int) (manager.getDefaultDisplay().getWidth());
//        vHeight = (int) (manager.getDefaultDisplay().getHeight());
        
        mWidth = width;
        mHeight = height;
        
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去边框
        getWindow().setBackgroundDrawable(DrawableUtils.shapeDrawable(Color.WHITE));//new ColorDrawable(0x00000000));
        
        setContentView(containerLayout);
        setCanceledOnTouchOutside(false);
    }

    public LinearLayout titleLayout;
    public LinearLayout contentLayout;
    public LinearLayout btnLayout;
    
    private TextView title;
    public TextView content;
    private void initView() {
        containerLayout = new LinearLayout(mContext);
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        
        int padding = UIUtils.getRealPixel720(15);
        //标题布局
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.getRealPixel720(100));
        titleLayout = new LinearLayout(mContext);
        titleLayout.setPadding(padding, UIUtils.getRealPixel720(10), padding, 0);
        containerLayout.addView(titleLayout, lParams);
        
        addTitle("");
        
        //内容布局
        lParams = new LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentLayout = new LinearLayout(mContext);
        contentLayout.setMinimumHeight(mHeight);
        contentLayout.setPadding(padding, 0, padding, UIUtils.getRealPixel720(20));
        contentLayout.setGravity(Gravity.CENTER);
        containerLayout.addView(contentLayout, lParams);
        
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        content = new TextView(mContext);
        content.setTextSize(18);
        content.setMinLines(1);
        content.setMaxLines(18);
        content.setVerticalScrollBarEnabled(true);
        content.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        //必须添加一个滚动实例 TextView才能滚动
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
//        content.setMinHeight(mHeight);
        contentLayout.addView(content, lParams);
        
        //按钮布局
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.getRealPixel720(100));
        btnLayout = new LinearLayout(mContext);
        btnLayout.setPadding(0, 1, 0, 0);
        btnLayout.setBackgroundDrawable(DrawableUtils.shapeDrawable(true, 0x1e000000));
        containerLayout.addView(btnLayout, lParams);
        
        addButton("确定", null);
    }
    
    public void setTitleView(View view){
        if(titleLayout!=null){
            titleLayout.removeAllViewsInLayout();
            lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lParams.gravity = Gravity.CENTER;
            titleLayout.addView(title, lParams);
        }
    }
    
    public void addTitle(String text){
        title = new TextView(mContext);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setText(text);
        setTitleView(title);
    }
    
    /**
     * 设置标题
     * @param text
     */
    public void setTitle(String text){
        if(title!=null){
            title.setText(text);
        }
    }
    /**
     * 设置消息内容
     * @param msg
     */
    public void setMessage(String msg){
        if(content!=null){
            content.setText(msg);
        }
    }
    /**
     * 中间内容布局
     * @param view
     */
    public void setMiddleContentView(View view){
        if(containerLayout!=null){
            containerLayout.removeAllViewsInLayout();
            containerLayout.addView(view);
        }
    }
    
    private boolean flag;
    private int btnPressedColor = 0xf4f4f4f4;
    /**
     * 添加按钮
     * @param text
     * @param listener
     * @return
     */
    public AlertDialog addButton(String text, final DialogInterface.OnClickListener listener){
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onClick(dialog, Integer.parseInt(v.getTag().toString()));
                }
                dialog.dismiss();
            }
        };

        TextView btn = null;
        int buttonCount = btnLayout.getChildCount();
        if(!flag && buttonCount==1){
            flag = true;
            btn = (TextView) btnLayout.getChildAt(0);
            btn.setText(text);
            btn.setOnClickListener(clickListener);
            return dialog;
        }

        btn = new TextView(mContext);
        btn.setTag(buttonCount);
        btn.setGravity(Gravity.CENTER);
        btn.setTextSize(20);
        btn.setTextColor(0xff00a7ff);
        btn.setText(text);
        btn.setOnClickListener(clickListener);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);

        if(buttonCount<1){
            btn.setBackgroundDrawable(DrawableUtils.colorShapePressedDrawableB(true, Color.WHITE, btnPressedColor));
        }else{
            View preBtn = btnLayout.getChildAt(buttonCount-1);
            if(buttonCount==1){
                preBtn.setBackgroundDrawable(DrawableUtils.colorShapePressedDrawableL(true, Color.WHITE, btnPressedColor));
            }else{
                //不用圆角
                preBtn.setBackgroundDrawable(DrawableUtils.colorPressedDrawable(Color.WHITE, btnPressedColor));
            }
            lParams.leftMargin = 1;
            btn.setBackgroundDrawable(DrawableUtils.colorShapePressedDrawableL(false, Color.WHITE, btnPressedColor));
        }
        btnLayout.addView(btn, lParams);
        return dialog;
    }

}
