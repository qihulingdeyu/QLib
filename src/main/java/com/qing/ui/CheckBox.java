package com.qing.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by zwq on 2015/10/22 17:27.<br/><br/>
 */
public class CheckBox extends RelativeLayout implements Checkable, View.OnClickListener {

    private static final String TAG = CheckBox.class.getName();

    private final int ID_BUTTON = 0x1;
    private final int ID_TEXT = 0x2;
    private CheckBoxButton mButton;
    private TextView mTextView;

    private int buttonVerb = RelativeLayout.CENTER_VERTICAL;
    private int textViewVerb = RelativeLayout.RIGHT_OF;
    private int currentGravity = Gravity.RIGHT;

    private LayoutParams rParams;
    private boolean isChecked;
    private int normalPic = -1;
    private int checkedPic = -1;
    private int normalColor = 0;
    private int checkedColor = 0;

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckBox(Context context) {
        this(context, null);
    }

    public CheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }

    private void initView() {
        rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rParams.addRule(buttonVerb);
        initButton();
        addView(mButton, rParams);

        rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rParams.addRule(buttonVerb);
        rParams.addRule(textViewVerb, ID_BUTTON);
        initTextView();
        addView(mTextView, rParams);
    }

    private void initButton() {
        if (mButton == null){
            mButton = new CheckBoxButton(getContext());
            mButton.setId(ID_BUTTON);
            mButton.setBackgroundColor(Color.TRANSPARENT);
            mButton.setOnClickListener(this);
        }
    }

    private void initTextView() {
        if (mTextView == null){
            mTextView = new TextView(getContext());
            mTextView.setId(ID_TEXT);
            mTextView.setPadding(3, 3, 3, 3);
            mTextView.setVisibility(View.GONE);
            mTextView.setOnClickListener(this);
        }
    }

    /**
     * 在比较listener中的CompoundButton时，
     * 需要通过getCompoundButton方法得到真正的CompoundButton对象再进行判断
     * @param listener
     */
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener){
        mOnCheckedChangeListener = listener;
    }

    public void setButtonImage(int normal, int checked){
        normalPic = normal;
        checkedPic = checked;
        if (normalPic != -1){
            mButton.setBackgroundResource(normalPic);
        }
    }

    public CheckBoxButton getCompoundButton() {
        return mButton;
    }

    public void setText(String text) {
        if (text != null){
            mTextView.setText(text);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setTextColor(int color) {
        mTextView.setTextColor(color);
    }

    public void setTextColor(ColorStateList colors) {
        if (colors != null){
            mTextView.setTextColor(colors);
        }
    }

    public void setTextColor(int normal, int checked) {
        normalColor = normal;
        checkedColor = checked;
        setTextColor(normal);
    }

    public void setTextSize(int size) {
        if (size > 8){
            mTextView.setTextSize(size);
        }
    }

    public TextView getTextView() {
        return mTextView;
    }

    /**
     * 设置文字位置:LEFT，TOP，RIGHT，BOTTOM，默认:RIGHT
     * @param gravity
     */
    public void setTextViewGravity(int gravity){
        if (gravity != currentGravity){
            boolean remove = false;
            int bVer = buttonVerb;
            int tVer = textViewVerb;
            switch (gravity){
                case Gravity.LEFT:
                    bVer = RelativeLayout.CENTER_VERTICAL;
                    tVer = RelativeLayout.RIGHT_OF;
                    remove = true;
                    break;
                case Gravity.TOP:
                    bVer = RelativeLayout.CENTER_HORIZONTAL;
                    tVer = RelativeLayout.BELOW;
                    remove = true;
                    break;
                case Gravity.RIGHT:
                    bVer = RelativeLayout.CENTER_VERTICAL;
                    tVer = RelativeLayout.RIGHT_OF;
                    remove = false;
                    break;
                case Gravity.BOTTOM:
                    bVer = RelativeLayout.CENTER_HORIZONTAL;
                    tVer = RelativeLayout.BELOW;
                    remove = false;
                    break;
                default:
                    return;
            }
            currentGravity = gravity;
            if (remove) {
                removeAllViewsInLayout();

                rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                rParams.addRule(bVer);
                initTextView();
                addView(mTextView, rParams);

                rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                rParams.addRule(bVer);
                rParams.addRule(tVer, ID_TEXT);
                initButton();
                addView(mButton, rParams);

            }else{
                LayoutParams rParams = (LayoutParams) mButton.getLayoutParams();
                rParams.addRule(buttonVerb, 0);
                rParams.addRule(textViewVerb, 0);
                rParams.addRule(bVer);
                mButton.setLayoutParams(rParams);

                rParams = (LayoutParams) mTextView.getLayoutParams();
                rParams.addRule(buttonVerb, 0);
                rParams.addRule(textViewVerb, 0);
                rParams.addRule(bVer);
                rParams.addRule(tVer, ID_BUTTON);
                mTextView.setLayoutParams(rParams);
            }
            buttonVerb = bVer;
            textViewVerb = tVer;

            invalidate();
            requestLayout();
        }
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public final void onClick(View v) {
        if (isChecked && normalColor != 0) {
            setTextColor(normalColor);
        }if (!isChecked && checkedColor != 0) {
            setTextColor(checkedColor);
        }
        setChecked(!isChecked);
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked != isChecked){
            isChecked = checked;
            if (isChecked){
                if (checkedPic != -1){
                    mButton.setBackgroundResource(checkedPic);
                }
            }else{
                if (normalPic != -1){
                    mButton.setBackgroundResource(normalPic);
                }
            }
            if (mOnCheckedChangeListener != null){
                mOnCheckedChangeListener.onCheckedChanged(mButton, isChecked);
            }
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mButton.setBackgroundDrawable(null);
        mButton = null;
        mTextView = null;
        rParams = null;
    }

    class CheckBoxButton extends CompoundButton {
        public CheckBoxButton(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public CheckBoxButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CheckBoxButton(Context context) {
            super(context);
        }
    }
}
