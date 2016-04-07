package com.qing.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by zwq on 2016/3/11 10:38.<br/><br/>
 */
public class AnimationUtil {
    private static final String TAG = AnimationUtil.class.getName();

    private static final long DEFAULT_DURATION = 300;

    /**
     * 位置、方向
     */
    public enum Direction {
        Left(0),
        Top(1),
        Right(2),
        Bottom(3);

        Direction(int direction) {
        }
    }

    /**
     * 清除动画
     * @param view
     */
    public static void clearViewAnim(View view) {
        if (view == null) {
            return;
        }
        Animation anim = view.getAnimation();
        if (anim != null) {
            anim.cancel();
            view.setAnimation(null);
        }
        anim = null;
    }

    /**
     * 旋转动画，循环
     * @param fromDegrees
     * @param toDegrees
     * @param duration 如果duration <= 0 则默认300ms
     * @return
     */
    public static RotateAnimation getRotateAnim(float fromDegrees, float toDegrees, long duration) {
        if (duration <= 0) {
            duration = DEFAULT_DURATION;
        }
        RotateAnimation anim = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        return anim;
    }

    public static RotateAnimation getRotateAnim(long duration) {
        RotateAnimation anim = getRotateAnim(0, 359, duration);
        anim.setRepeatCount(-1);
        return anim;
    }

    /**
     * 旋转动画，循环
     * @param view
     * @param duration 如果duration <= 0 则默认300ms
     * @param listener
     */
    public static void viewRotateAnim(View view, long duration, Animation.AnimationListener listener) {
        if (view == null) {
            return;
        }
        AnimationSet animSet = new AnimationSet(true);
        RotateAnimation anim = getRotateAnim(duration);
        anim.setAnimationListener(listener);
        animSet.addAnimation(anim);
        view.startAnimation(animSet);
    }

    /**
     * @param fromScale
     * @param toScale
     * @param duration 如果duration <= 0 则默认300ms
     * @return
     */
    public static ScaleAnimation getScaleAnim(float fromScale, float toScale, long duration) {
        if (duration <= 0) {
            duration = DEFAULT_DURATION;
        }
        ScaleAnimation anim = new ScaleAnimation(fromScale, toScale, fromScale, toScale, 0.5f, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        return anim;
    }

    /**
     * 缩放动画， 基于中心
     * @param view
     * @param fromScale
     * @param toScale
     * @param duration 如果duration <= 0 则默认300ms
     * @param listener
     */
    public static void viewScaleAnim(View view, float fromScale, float toScale, long duration, Animation.AnimationListener listener) {
        if (view == null) {
            return;
        }
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation anim = getScaleAnim(fromScale, toScale, duration);
        anim.setAnimationListener(listener);
        animSet.addAnimation(anim);
        view.startAnimation(animSet);
    }

    /**
     *
     * @param from 位移(开始)方向
     * @param isEnter 是否是进入
     * @param duration 如果duration <= 0 则默认300ms
     * @return
     */
    public static TranslateAnimation getTranslateAnim(Direction from, boolean isEnter, long duration) {
        float fromX = 0;
        float toX = 0;
        float fromY = 0;
        float toY = 0;

        if (from == Direction.Left) {
            if (isEnter) {
                fromX = -1.0f;
            } else {
                toX = -1.0f;
            }
        } else if (from == Direction.Top) {
            if (isEnter) {
                fromY = -1.0f;
            } else {
                toY = -1.0f;
            }
        } else if (from == Direction.Right) {
            if (isEnter) {
                fromX = 1.0f;
            } else {
                toX = 1.0f;
            }
        } else if (from == Direction.Bottom) {
            if (isEnter) {
                fromY = 1.0f;
            } else {
                toY = 1.0f;
            }
        }
        if (duration <= 0) {
            duration = DEFAULT_DURATION;
        }

        TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromX, Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_SELF, fromY, Animation.RELATIVE_TO_SELF, toY);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        return anim;
    }

    /**
     * 位移动画
     * @param view
     * @param from 位移(开始)方向
     * @param isEnter 是否是进入
     * @param duration 如果duration <= 0 则默认300ms
     * @param listener
     */
    public static void viewTranslateAnim(View view, Direction from, boolean isEnter, long duration, Animation.AnimationListener listener) {
        if (view == null) {
            return;
        }
        AnimationSet animSet = new AnimationSet(true);
        TranslateAnimation anim = getTranslateAnim(from, isEnter, duration);
        anim.setAnimationListener(listener);
        animSet.addAnimation(anim);
        view.startAnimation(animSet);
    }

}
