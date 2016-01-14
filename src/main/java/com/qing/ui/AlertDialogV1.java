package com.qing.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

/**
 * Created by zwq on 2015/07/23 10:11.<br/><br/>
 * 自定义提示对话框
 */
public class AlertDialogV1 extends Dialog {

	private AlertDialogV1 dialog;
	private Context mContext;

	private boolean hasView;
	private LinearLayout containerLayout;

	public AlertDialogV1(Context context) {
		super(context);
		dialog = this;
		mContext = context;

//		WindowManager manager = getWindow().getWindowManager();
//		vWidth = (int) (manager.getDefaultDisplay().getWidth());
//		vHeight = (int) (manager.getDefaultDisplay().getHeight());

		initView();
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//去边框
		getWindow().setBackgroundDrawable(getShapeDrawable(Color.WHITE));//new ColorDrawable(0x00000000));
        
		setContentView(containerLayout);
		setCanceledOnTouchOutside(false);
	}

	private void initView() {
		containerLayout = new LinearLayout(mContext);
		containerLayout.setOrientation(LinearLayout.VERTICAL);
	}

	/**
	 * 添加自定义的View
	 * @param view
	 */
	public final void addContentView(View view) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		addContentView(view, params);
	}

	/**
     * 添加自定义的View
	 * @param view
     * @param params
	 */
	public final void addContentView(View view, LinearLayout.LayoutParams params) {
		if (!hasView && containerLayout != null) {
			containerLayout.addView(view, params);
			containerLayout.setBackgroundDrawable(getShapeDrawable(0x00000000));
			hasView = true;
		}
	}

	/** 圆角大小 */
	private int radius = 15;
	/**
	 * 圆角边框
	 * @param tl 左上
	 * @param tr 右上
	 * @param br 右下
	 * @param bl 左下
	 * @param color 颜色
	 * @return
	 */
	protected final ShapeDrawable getShapeDrawable(boolean tl, boolean tr, boolean br, boolean bl, int color){
		float[] outerRadii = new float[8];
		if(tl){
			outerRadii[0] = radius;
			outerRadii[1] = radius;
		}
		if(tr){
			outerRadii[2] = radius;
			outerRadii[3] = radius;
		}
		if(br){
			outerRadii[4] = radius;
			outerRadii[5] = radius;
		}
		if(bl){
			outerRadii[6] = radius;
			outerRadii[7] = radius;
		}
		RoundRectShape round = new RoundRectShape(outerRadii, null, null);
		ShapeDrawable shape = new ShapeDrawable(round);
		shape.getPaint().setColor(color);
		shape.getPaint().setStyle(Paint.Style.FILL);
		return shape;
	}
	
	/**
	 * 四个角都是圆角
	 * @return
	 */
	protected final ShapeDrawable getShapeDrawable(int color){
		return getShapeDrawable(true, true, true, true, color);
	}
	
	/**
	 * 下边两个角是圆角
	 */
	protected ShapeDrawable getShapeDrawable(boolean isBottom, int color){
		return getShapeDrawable(!isBottom, !isBottom, isBottom, isBottom, color);
	}
	
	/**
	 * 四个角可以设为圆角 并有按压效果
	 * @param normal
	 * @param pressed
	 * @return
	 */
	protected final StateListDrawable getShapePressedDrawable(boolean tl, boolean tr,  boolean br, boolean bl, int normal, int pressed){
		StateListDrawable selector  = new StateListDrawable();
		selector.addState(new int[]{android.R.attr.state_pressed}, getShapeDrawable(tl, tr, br, bl, pressed));
		selector.addState(new int[]{android.R.attr.state_enabled}, getShapeDrawable(tl, tr, br, bl, normal));
		return selector;
	}

	protected final StateListDrawable getPressedDrawable(int normal, int pressed){
		StateListDrawable selector  = new StateListDrawable();
		selector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
		selector.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(normal));
		return selector;
//		return getShapePressedDrawable(false, false, false, false, normal, pressed);
	}
	
	/**
	 * 上边或下班圆角 并且有按压效果
	 * @param normal
	 * @param pressed
	 * @return
	 */
	protected final StateListDrawable getShapePressedDrawable(boolean isBottom, int normal, int pressed){
		return getShapePressedDrawable(!isBottom, !isBottom, isBottom, isBottom, normal, pressed);
	}
	
	/**
	 * 左下或右下角圆角边框
	 * @param isLeft 是否是左下
	 * @param normal
	 * @param pressed
	 * @return
	 */
	protected final StateListDrawable getPressedDrawable(boolean isLeft, int normal, int pressed){
		StateListDrawable selector  = new StateListDrawable();
		selector.addState(new int[]{android.R.attr.state_pressed}, getShapeDrawable(false, false, !isLeft, isLeft, pressed));
		selector.addState(new int[]{android.R.attr.state_enabled}, getShapeDrawable(false, false, !isLeft, isLeft, normal));
		return selector;
	}
	
}
