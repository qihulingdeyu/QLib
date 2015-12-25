package com.qing.utils;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by zwq on 2015/10/22 10:54.<br/><br/>
 * 缩放、旋转
 */
public class GesturesUtil {

    private static final String TAG = GesturesUtil.class.getName();
    private static GesturesUtil instance;
    private GesturesListener mGesturesListener;

    private boolean twoPoint;

    //旋转
    public static final float INVALID_DEGREE = 1000;
    private float startDegree;
    private float targetDegree = INVALID_DEGREE;
    private float oldDegree;
    private float rotationDegree;//旋转的角度
    private int rotationStep = 1;
    private int rotationDirection = 1;//旋转方向

    //缩放
    private float startScale;
    private float oldDistance;
    private float scaleStep = 0.01f;
    private float scaleRate = 1;//缩放方向

    private GesturesUtil() { }

    public static GesturesUtil getInstance() {
        if (instance == null) {
            synchronized (GesturesUtil.class) {
                if (instance == null) {
                    instance = new GesturesUtil();
                }
            }
        }
        return instance;
    }

    public void setGesturesListener(GesturesListener listener) {
        mGesturesListener = listener;
    }

    public interface GesturesListener {
        /**
         * 当前的角度
         * @return
         */
        float getCurrentDegree();
        /**
         * MotionEvent.ACTION_MOVE时被调用
         * @param degree 每次旋转的角度，默认1个角度
         */
        void onDegreeChange(float degree);
        /**
         * MotionEvent.ACTION_UP时被调用
         * @param startDegree 初始时的角度
         * @param degree 实际旋转的角度，大于0顺时针旋转，反之逆时针旋转
         * @param targetDegree 目标角度
         */
        void onRotation(float startDegree, float degree, float targetDegree);

        float getCurrentScale();
        void onScaleChange(float scaleRate);
        void onScale(float startScale, float scale, float endScale);
    }

    public final void setMotionEvent(MotionEvent event){
        if (mGesturesListener != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
//                    MLog.i("bbb", "--MotionEvent--ACTION_DOWN--");
                    twoPoint = false;
                    startDegree = mGesturesListener.getCurrentDegree();
                    startScale = mGesturesListener.getCurrentScale();
                    break;
//                case MotionEvent.ACTION_POINTER_2_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
//                    MLog.i("bbb", "--MotionEvent--ACTION_POINTER_DOWN--");
                    oldDegree = rotation(event);
                    oldDistance = spacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() > 1){
                        float newDegree = rotation(event);
                        float degree = newDegree - oldDegree;
                        if (Math.abs(degree) > 1){
                            if (degree < 0){
                                rotationDirection = -1 * rotationStep;
                            }else{
                                rotationDirection = 1 * rotationStep;
                            }
//                        MLog.i("bbb", "degree:" + degree);
                            mGesturesListener.onDegreeChange(rotationDirection);
                        }
                        oldDegree = newDegree;

                        float newDistance = spacing(event);
                        float distance = newDistance - oldDistance;
                        if (Math.abs(distance) > 5){
                            if (distance < 0){
                                scaleRate = -1 * scaleStep;
                            }else{
                                scaleRate = 1 * scaleStep;
                            }
                            mGesturesListener.onScaleChange(scaleRate);
                        }
                        oldDistance = newDistance;
                    }
                    break;
//                case MotionEvent.ACTION_POINTER_2_UP:
                case MotionEvent.ACTION_POINTER_UP:
//                    MLog.i("bbb", "--MotionEvent--ACTION_POINTER_UP--");
                    twoPoint = true;
                    break;
                case MotionEvent.ACTION_UP:
//                    MLog.i("bbb", "--MotionEvent--ACTION_UP--");
                    if (twoPoint) {
                        float endDegree = mGesturesListener.getCurrentDegree();
                        rotationDegree = endDegree - startDegree;
                        targetDegree = checkDegree(endDegree);

//                    MLog.i("bbb", "startDegree:" + startDegree + ",endDegree:" + endDegree + ", targetDegree:" + targetDegree);
                        mGesturesListener.onRotation(startDegree, rotationDegree, targetDegree);

                        float endScale = mGesturesListener.getCurrentScale();
                        mGesturesListener.onScale(startScale, endScale - startScale, endScale);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 触碰两点间距离
     * @param event
     * @return
     */
    public static float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 取手势中心点
     * @param point
     * @param event
     */
    public static void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 取旋转角度
     * @param event
     * @return
     */
    public static float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 校正角度
     * @param degree
     * @return
     */
    public static float checkDegree(float degree){
        float absDegree = Math.abs(degree);
        if (absDegree != 0){
            int a = (int) (absDegree / 45);
            float b = absDegree % 45;
            if (a%2 != 0 && b >= 0){
                a += 1;
            }
            degree = degree/absDegree * a*45;
        }
        return degree;
    }

}
