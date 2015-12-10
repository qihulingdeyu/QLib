package com.qing.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.qing.utils.GesturesUtils;

/**
 * Created by zwq on 2015/11/11 14:43.<br/><br/>
 *
 * 必要权限：
 * <uses-feature android:name="android.hardware.camera" />
 * <uses-feature android:name="android.hardware.camera.autofocus" />
 * <uses-permission android:name="android.permission.CAMERA" />
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getName();

    private SurfaceHolder mHolder;
    private CameraWrapper cameraWrapper;
    private boolean mSurfaceCreated = false;

    private float ratio = 4.0f/3;
    private GesturesUtils gesturesUtils;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraWrapper = new CameraWrapper(getContext(), new CameraWrapper.CameraSurfaceView() {
            @Override
            public SurfaceHolder getSurfaceHolder() {
                return mHolder;
            }

            @Override
            public SurfaceTexture getSurfaceTexture() {
                return null;
            }

            @Override
            public void requestView() {
                if (mSurfaceCreated){
//                    MLog.i("bbb", "requestView");
//                    MLog.i("bbb", "requestLayout");
                    requestLayout();
                }
            }
        });
        cameraWrapper.openCamera();

//        gesturesUtils = GesturesUtils.getInstance();
//        gesturesUtils.setGesturesListener(new GesturesUtils.GesturesListener() {
//            @Override
//            public float getCurrentDegree() {
//                return 0;
//            }
//
//            @Override
//            public void onDegreeChange(float degree) {
//
//            }
//
//            @Override
//            public void onRotation(float startDegree, float degree, float targetDegree) {
//
//            }
//
//            @Override
//            public float getCurrentScale() {
//                return 0;
//            }
//
//            @Override
//            public void onScaleChange(float scaleRate) {
//                MLog.i("bbb", "onScaleChange:" + scaleRate);
//                getCamera().setCameraZoom(getCamera().getCurrentZoom() + (int) (scaleRate * 100));
//            }
//
//            @Override
//            public void onScale(float startScale, float scale, float endScale) {
//
//            }
//        });
    }

    public CameraWrapper getCamera() {
        return cameraWrapper;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        if (width > height) {
            width = (int) (height / ratio);
        }else{
            height = (int) (width * ratio);
        }
        setMeasuredDimension(width, height);
        cameraWrapper.setOptimalPreviewSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;
        cameraWrapper.onSurfaceViewCreate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        cameraWrapper.onSurfaceViewChange();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraWrapper.onSurfaceViewDestory();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        gesturesUtils.setMotionEvent(event);
//        cameraWrapper.setFocusOnTouch(event);
        return true;
    }
}
