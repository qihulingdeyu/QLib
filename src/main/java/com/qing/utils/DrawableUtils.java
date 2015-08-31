package com.qing.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

public class DrawableUtils {

    /**
     * 颜色背景
     * @param color
     * @return
     */
    public static Drawable colorDrawable(int color){
        if(color!=0){
            return new ColorDrawable(color);
        }
        return null;
    }

    /**
     * 按压切换颜色
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable colorPressedDrawable(int normal, int pressed){
        StateListDrawable selector  = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        selector.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(normal));
        return selector;
    }

    public static ColorStateList colorPressedDrawable2(int normal, int pressed){
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_enabled}};
        int[] colors = new int[]{pressed,normal};
        ColorStateList colorsList = new ColorStateList(states, colors);
        return colorsList;
    }

    /**
     * 选中状态切换颜色
     * @param normal
     * @param checked 选中时的颜色
     * @return
     */
    public static ColorStateList colorCheckedDrawable(int normal, int checked){
        if(normal!=-1 && checked!=-1){
            //style 1:
            int[][] states = new int[2][];
            states[0] = new int[]{android.R.attr.state_checked};
            states[1] = new int[]{android.R.attr.state_enabled};
            
            int[] colors = {checked, normal};
            ColorStateList list = new ColorStateList(states, colors);
            return list;

            //style 2:
//            StateListDrawable selector  = new StateListDrawable();
//            selector.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(checked));
//            selector.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(normal));
//            return selector;
        }
        return null;
    }

    /**
     * 按压切换图片
     * @param context
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable pressedSelector(Context context, int normal, int pressed){
        if(normal!=-1 && pressed!=-1){
            Resources res = context.getResources();
            StateListDrawable selector  = new StateListDrawable();
            selector.addState(new int[]{android.R.attr.state_pressed}, res.getDrawable(pressed));
            selector.addState(new int[]{android.R.attr.state_enabled}, res.getDrawable(normal));
            return selector;
        }
        return null;
    }

    public static StateListDrawable pressedSelector(Context context, Bitmap normal, Bitmap pressed){
        if(normal!=null && pressed!=null){
            Resources res = context.getResources();
            StateListDrawable selector  = new StateListDrawable();
            selector.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(res, pressed));
            selector.addState(new int[]{android.R.attr.state_enabled}, new BitmapDrawable(res, normal));
            return selector;
        }
        return null;
    }

    /**
     * 选中时切换图片
     * @param context
     * @param normal
     * @param checked
     * @return
     */
    public static StateListDrawable checkedDrawable(Context context, int normal, int checked){
        if(normal!=-1 && checked!=-1){
            Resources res = context.getResources();
            StateListDrawable selector  = new StateListDrawable();
            selector.addState(new int[]{android.R.attr.state_checked}, res.getDrawable(checked));
            selector.addState(new int[]{android.R.attr.state_enabled}, res.getDrawable(normal));
            return selector;
        }
        return null;
    }

    /** 圆角大小 */
    private static int mRadius = 15;
    public static void setRadius(int radius) {
        mRadius = radius;
    }

    /**
     * 圆角边框
     * @param color
     * @param radius
     * @param borderWidth
     * @return
     */
    public static ShapeDrawable shapeBorderDrawable(int color, int radius, int borderWidth){
        float[] outerR = new float[]{radius, radius, radius, radius, radius, radius, radius, radius };
        RoundRectShape roundRectShape = new RoundRectShape(outerR, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setStrokeWidth(borderWidth);
        shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
        return shapeDrawable;
    }

    /**
     * 圆角边框
     * @param tl 左上
     * @param tr 右上
     * @param br 右下
     * @param bl 左下
     * @param color 颜色
     * @param radius 圆角大小
     * @return
     */
    public static ShapeDrawable shapeDrawable(boolean tl, boolean tr, boolean br, boolean bl, int color, int radius){
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
        // 构造一个圆角矩形,可以使用其他形状，这样ShapeDrawable 就会根据形状来绘制。
        RoundRectShape round = new RoundRectShape(outerRadii, null, null);
        // 如果要构造直角矩形可以
        //RectShape rectShape = new RectShape();
        // 组合圆角矩形和ShapeDrawable
        ShapeDrawable shape = new ShapeDrawable(round);
        // 设置形状的颜色
        shape.getPaint().setColor(color);
        // 设置绘制方式为填充
        shape.getPaint().setStyle(Paint.Style.FILL);
        return shape;
    }

    /**
     * 设置圆角并填充颜色
     * @param color
     * @param radius
     * @return
     */
    public static ShapeDrawable shapeDrawable(int color, int radius){
        return shapeDrawable(true, true, true, true, color, radius);
    }

    /**
     * 四个角都是圆角
     * @return
     */
    public static ShapeDrawable shapeDrawable(int color){
        return shapeDrawable(true, true, true, true, color, mRadius);
    }

    /**
     * 下边两个角是圆角
     */
    public static ShapeDrawable shapeDrawable(boolean isBottom, int color){
        return shapeDrawable(!isBottom, !isBottom, isBottom, isBottom, color, mRadius);
    }

    /**
     * 四个角可以设为圆角 并有按压效果
     * @param tl
     * @param tr
     * @param br
     * @param bl
     * @param normal
     * @param pressed 按压时的颜色
     * @return
     */
    public static StateListDrawable colorShapePressedDrawable(boolean tl, boolean tr,  boolean br, boolean bl, int normal, int pressed){
        StateListDrawable selector  = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, shapeDrawable(tl, tr, br, bl, pressed, mRadius));
        selector.addState(new int[]{android.R.attr.state_enabled}, shapeDrawable(tl, tr, br, bl, normal, mRadius));
        return selector;
    }

    /**
     * 左下或右下角圆角边框
     * @param isLeft 是否是左下
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable colorShapePressedDrawableL(boolean isLeft, int normal, int pressed){
        StateListDrawable selector  = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, shapeDrawable(false, false, !isLeft, isLeft, pressed, mRadius));
        selector.addState(new int[]{android.R.attr.state_enabled}, shapeDrawable(false, false, !isLeft, isLeft, normal, mRadius));
        return selector;
    }

    /**
     * 上边或下班圆角 并且有按压效果
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable colorShapePressedDrawableB(boolean isBottom, int normal, int pressed){
        return colorShapePressedDrawable(!isBottom, !isBottom, isBottom, isBottom, normal, pressed);
    }

}
