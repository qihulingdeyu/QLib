package com.qing.camera;

import android.hardware.Camera;

/**
 * Created by zwq on 2015/11/16 14:42.<br/><br/>
 * 相机的所有回调
 */
public interface CameraAllCallback extends Camera.AutoFocusCallback, Camera.ShutterCallback, Camera.ErrorCallback, Camera.PreviewCallback, Camera.PictureCallback {

    public enum States {

        CAMERA_IDLE(1),//空闲
        CAMERA_START(2),//开始拍照
        CAMERA_DOING(3),//正在拍照。。保存照片。。
        CAMERA_SUCCESS(4),//拍照成功
        CAMERA_FAIL(5),//拍照失败
        CAMERA_STOP(6),//停止拍照

        PREVIEW_IDLE(11),
        PREVIEW_START(12),
        PREVIEW_SUCCESS(13),
        PREVIEW_DOING(14),
        PREVIEW_FAIL(15),
        PREVIEW_STOP(16),

        FOCUS_IDLE(21),
        FOCUS_START(22),
        FOCUS_DOING(23),
        FOCUS_SUCCESS(24),
        FOCUS_FAIL(25),
        FOCUS_STOP(26),
        FOCUS_DOING_ON_TOUCH(27),
        TAKE_PICTURE_AFTER_FOCUS_FINISH(28);

        public int state;
        States(int i) {
            state = i;
        }
    }


    /**
     * 屏幕方向改变，前置拍照的时候 照片可能要左右镜像翻转一下
     * @param orientation 屏幕实际旋转角度
     * @param pictureDegree 拍照时照片的修正角度
     * @param fromDegree 屏幕旋转时，按钮等UI控件的旋转角度
     * @param toDegree 屏幕旋转时，按钮等UI控件的旋转角度
     */
    void onScreenOrientationChanged(int orientation, int pictureDegree, float fromDegree, float toDegree);

}
