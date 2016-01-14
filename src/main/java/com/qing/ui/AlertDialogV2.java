package com.qing.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qing.utils.UIUtil;

/**
 * Created by zwq on 2015/07/23 10:11.<br/><br/>
 * 自定义提示对话框
 */
public class AlertDialogV2 extends AlertDialogV1 {

    private AlertDialogV2 dialog;
    private Context mContext;
    private int mWidth;
    private int mHeight;

    private LinearLayout containerLayout;
    public LinearLayout titleLayout;
    public LinearLayout contentLayout;
    public LinearLayout btnLayout;
    private TextView title;
    private TextView content;
    private LinearLayout.LayoutParams lParams;

    private boolean flag;
    private int btnPressedColor = 0xf4f4f4f4;

    public AlertDialogV2(Context context) {
        this(context, (int)(UIUtil.getScreenW()*0.8f), (int)(UIUtil.getScreenH()*0.05f));
    }

    /**
     * width和height是dialog中间内容布局的宽高
     *
     * @param context
     * @param width
     * @param height
     */
    public AlertDialogV2(Context context, int width, int height) {
        super(context);
        dialog = this;
        mContext = context;

        mWidth = width;
        mHeight = height;

        initView();
    }
    
    private int getDpi(int px) {
        return UIUtil.getRealPixel720(px);
    }

    private void initView() {
        containerLayout = new LinearLayout(mContext);
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        addContentView(containerLayout);

        int padding = getDpi(15);
        //标题布局
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getDpi(100));
        titleLayout = new LinearLayout(mContext);
        titleLayout.setPadding(padding, getDpi(10), padding, 0);
        containerLayout.addView(titleLayout, lParams);

        addTitle("提示");

        //内容布局
        lParams = new LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentLayout = new LinearLayout(mContext);
        contentLayout.setMinimumHeight(mHeight);
        contentLayout.setPadding(padding, 0, padding, getDpi(20));
        contentLayout.setGravity(Gravity.CENTER);
        containerLayout.addView(contentLayout, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        content = new TextView(mContext);
        content.setTextSize(18);
        content.setTextColor(Color.BLACK);
        content.setMinLines(1);
//		content.setMinHeight(mHeight);
        contentLayout.addView(content, lParams);

        //按钮布局
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getDpi(100));
        btnLayout = new LinearLayout(mContext);
        btnLayout.setPadding(0, 1, 0, 0);
        btnLayout.setBackgroundDrawable(getShapeDrawable(true, 0x1e000000));
        containerLayout.addView(btnLayout, lParams);

        addButton("确定", null);
    }

    public void setTitleView(View view) {
        if (titleLayout != null) {
            titleLayout.removeAllViewsInLayout();
            lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lParams.gravity = Gravity.CENTER;
            titleLayout.addView(title, lParams);
        }
    }

    public void addTitle(String text) {
        title = new TextView(mContext);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setSingleLine(true);
        title.setEllipsize(TruncateAt.END);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setText(text);
        setTitleView(title);
    }

    /**
     * 设置标题
     *
     * @param text
     * @return
     */
    public AlertDialogV2 setTitle(String text) {
        if (title != null) {
            title.setText(text);
        }
        return dialog;
    }

    public TextView getMessageView() {
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param msg
     * @return
     */
    public AlertDialogV2 setMessage(String msg) {
        if (content != null) {
            content.setText(msg);
        }
        return dialog;
    }

    /**
     * 中间内容布局
     *
     * @param view
     */
    public void setMiddleContentView(View view) {
        if (containerLayout != null) {
            containerLayout.removeAllViewsInLayout();
            containerLayout.addView(view);
        }
    }

    /**
     * 添加按钮
     *
     * @param text
     * @param listener
     * @return
     */
    public AlertDialogV2 addButton(String text, final OnClickListener listener) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog, Integer.parseInt(v.getTag().toString()));
                }
                dialog.dismiss();
            }
        };

        TextView btn = null;
        int buttonCount = btnLayout.getChildCount();
        if (!flag && buttonCount == 1) {
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

        if (buttonCount < 1) {
            btn.setBackgroundDrawable(getShapePressedDrawable(true, Color.WHITE, btnPressedColor));
        } else {
            View preBtn = btnLayout.getChildAt(buttonCount - 1);
            if (buttonCount == 1) {
                preBtn.setBackgroundDrawable(getPressedDrawable(true, Color.WHITE, btnPressedColor));
            } else {
                //不用圆角
                preBtn.setBackgroundDrawable(getPressedDrawable(Color.WHITE, btnPressedColor));
            }
            lParams.leftMargin = 1;
            btn.setBackgroundDrawable(getPressedDrawable(false, Color.WHITE, btnPressedColor));
        }
        btnLayout.addView(btn, lParams);
        return dialog;
    }
}
