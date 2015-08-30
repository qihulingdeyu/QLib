package com.qing.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * 有背景选择器的按钮
 */
public class SelectorButton extends Button{

	private Context mContext;
	private int mNormalResId = -1;
	private int mPressResId = -1;
	private Bitmap mNormalBmp = null;
	private Bitmap mPressBmp = null;
	private StateListDrawable bgSelector = null;
	private ColorStateList colorsList = null;
	
	public SelectorButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	public SelectorButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	public SelectorButton(Context context) {
		super(context);
		mContext = context;
	}
	
	public SelectorButton(Context context, int resNormal, int resPress) {
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
			if(mNormalResId==-1 || mPressResId==-1){
				return;
			}
			bgSelector  = new StateListDrawable();
			bgSelector.addState(new int[]{android.R.attr.state_pressed}, res.getDrawable(mPressResId));
			bgSelector.addState(new int[]{android.R.attr.state_enabled}, res.getDrawable(mNormalResId));
		}else if(type==2){
			if(mNormalBmp==null || mPressBmp==null){
				return;
			}
			bgSelector  = new StateListDrawable();
			bgSelector.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(res, mPressBmp));
			bgSelector.addState(new int[]{android.R.attr.state_enabled}, new BitmapDrawable(res, mNormalBmp));
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
		
		int[][] states = new int[2][];
		states[0] = new int[]{android.R.attr.state_pressed};
		states[1] = new int[]{android.R.attr.state_enabled};
		
		int[] colors = new int[]{pressColor,normalColor,};
		
		colorsList = new ColorStateList(states, colors);
		this.setTextColor(colorsList);
	}
}


