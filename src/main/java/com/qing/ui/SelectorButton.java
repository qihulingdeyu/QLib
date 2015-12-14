package com.qing.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.StateListDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.qing.utils.DrawableUtils;

/**
 * Created by zwq on 2015/04/13 12:22.<br/><br/>
 * 可以设置有按压效果的按钮
 */
public class SelectorButton extends ImageView {

	private Context mContext = null;
	private boolean isOrigin;
	private int mResIdNormal = -1;
	private int mResIdPress = -1;
	private Bitmap mBmpNormal = null;
	private Bitmap mBmpPress = null;
	private StateListDrawable selector = null;
	private int mAlpha = 255;
	private boolean isChecked = false;
	
	public SelectorButton(Context context) {
		super(context);
		mContext = context;
		setScaleType(ScaleType.CENTER_INSIDE);
	}
	/**
	 * @param resNormal		正常的图片
	 * @param resPress		按压时的图片
	 */
	public SelectorButton(Context context, int resNormal, int resPress) {
		super(context);
		mContext = context;
		setButtonImage(resNormal, resPress);
	}
	/**
	 * @param resNormal		正常的图片
	 * @param resPress		按压时的图片
	 */
	public void setButtonImage(int resNormal, int resPress) {
		setButtonImage(resNormal, resPress, mAlpha);
	}
	/**
	 * @param resNormal		正常的图片
	 * @param resPress		按压时的图片
	 * @param replaceOld	替换掉原来的图片
	 */
	public void setButtonImage(int resNormal, int resPress, boolean replaceOld) {
		isOrigin = !replaceOld;
		setButtonImage(resNormal, resPress, mAlpha);
	}
	/**
	 * @param resNormal		正常的图片
	 * @param resPress		按压时的图片
	 * @param alpha			按压时的透明度
	 */
	public void setButtonImage(int resNormal, int resPress, int alpha) {
		if(!isOrigin){
			mResIdNormal = resNormal;
			mResIdPress = resPress;
			mAlpha = alpha;
			isOrigin = true;
			isChecked = true;//默认选中
		}
		setSelector(resNormal,resPress);
	}
	
	public void setButtonImage(Bitmap bmpNormal, Bitmap bmpPress) {
		mBmpNormal = bmpNormal;
		mBmpPress = bmpPress;
		setSelector(mBmpNormal, mBmpPress);
		if(selector==null && mBmpNormal != null){
			this.setImageBitmap(mBmpNormal);
		}else{
			this.setImageDrawable(selector);
		}
	}

	private void setSelector(int normal, int pressed) {
		selector = DrawableUtils.pressedSelector(mContext, normal, pressed);
		if(selector == null && normal != -1){
			this.setImageResource(normal);
		}else{
			this.setImageDrawable(selector);
		}
	}
	
	private void setSelector(Bitmap normal, Bitmap pressed) {
		selector = DrawableUtils.pressedSelector(mContext, normal, pressed);
		if(selector == null){
			this.setImageDrawable(selector);
		}
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		super.setScaleType(scaleType);
	}
	
	/**
	 * 设置选中状态：选中~true，未选中~false
	 */
	public void setChecked(boolean isChecked){
		setSwitchState(!isChecked);
		this.isChecked = isChecked;
		if(checkedListener != null){
			checkedListener.onCheckedChanged(this, isChecked);
		}
	}
	/** 
	 * 设置开关状态：On开~true，Off关~false
	 */
	public void setSwitchState(boolean isOn){
		if(isOn){
			setButtonImage(mResIdNormal,mResIdPress);
		}else{
			setButtonImage(mResIdPress,mResIdNormal);
		}
		this.isChecked = isOn;
	}
	public boolean isChecked(){
		return isChecked;
	}
	
	public void setOnFocusState(){
		setButtonImage(mResIdNormal,mResIdPress);
		isChecked = true;
	}
	
	public void setOnReadyState(){
		setButtonImage(mResIdPress,mResIdNormal);
		isChecked = false;
	}
	
	public void setAlpha(int alpha){
		mAlpha = alpha;
//		this.setAlpha(mAlpha);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			this.setAlpha(mAlpha);
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			this.setAlpha(255);
		}
		return super.onTouchEvent(event);
	}
	
	private OnCheckedChangeListener checkedListener;
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
		checkedListener = listener;
	}
	
	public interface OnCheckedChangeListener{
		void onCheckedChanged(View view, boolean isChecked);
	}
}