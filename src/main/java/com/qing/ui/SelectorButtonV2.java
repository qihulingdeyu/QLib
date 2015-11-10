package com.qing.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.qing.utils.DrawableUtils;

/**
 * Created by zwq on 2015/04/13 15:22.<br/><br/>
 * 有背景选择器 并且可设置文字的按钮
 */
public class SelectorButtonV2 extends Button{

	private Context mContext;
	private int mNormalResId = -1;
	private int mPressResId = -1;
	private Bitmap mNormalBmp = null;
	private Bitmap mPressBmp = null;
	private StateListDrawable bgSelector = null;

	public SelectorButtonV2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	public SelectorButtonV2(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	public SelectorButtonV2(Context context) {
		super(context);
		mContext = context;
	}
	
	public SelectorButtonV2(Context context, int resNormal, int resPress) {
		super(context);
		mContext = context;
		setBgSelector(resNormal, resPress);
	}
	
	public void setBgSelector(int resNormal, int resPress) {
		mNormalResId = resNormal;
		mPressResId = resPress;
		initBgSelector(1);
	}
	
	public void setBgSelector(Bitmap bmpNormal, Bitmap bmpPress) {
		mNormalBmp = bmpNormal;
		mPressBmp = bmpPress;
		initBgSelector(2);
	}
	
	private void initBgSelector(int type) {
		Resources res = mContext.getResources();
		if(type==1){
			bgSelector = DrawableUtils.pressedSelector(mContext, mNormalResId, mPressResId);
		}else if(type==2){
			bgSelector = DrawableUtils.pressedSelector(mContext, mNormalBmp, mPressBmp);
		}
		
		if(bgSelector!=null){
			this.setBackgroundDrawable(bgSelector);
		}
		res = null;
	}
	
	public void setText(String text, int normalColor, int pressColor){
		if(text==null || text.trim().equals("")){
			return;
		}
		setTextSelector(text, normalColor, pressColor);
	}
	
	public void setText(String text, String normalColor, String pressColor){
		if(text==null || text.trim().equals("")){
			return;
		}
		int color1 = Color.WHITE;
		if(normalColor!=null && !normalColor.equals("")){
			color1 = Color.parseColor(normalColor);
		}
		
		int color2 = Color.WHITE;
		if(pressColor!=null && !pressColor.equals("")){
			color2 = Color.parseColor(pressColor);
		}
		setTextSelector(text, color1, color2);
	}
	
	private void setTextSelector(String text, int normalColor, int pressColor){
		this.setText(text);
		this.setClickable(true);

		ColorStateList colorsList = DrawableUtils.colorPressedDrawable2(normalColor, pressColor);
		if (colorsList!=null){
			this.setTextColor(colorsList);
		}
	}
}


