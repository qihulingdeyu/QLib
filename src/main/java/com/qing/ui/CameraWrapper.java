package com.qing.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import com.qing.log.MLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwq on 2015/11/11 14:19.<br/><br/>
 * <p/>
 * 预览（start/success/previewing/fail/stop）√
 * 拍照 (start/success/fail) √
 * 缩放 (support/) √
 * 焦点 (support/) √
 * 闪关灯 (support/open / mode /close) √
 * 快门声音 (support/open / close) √
 * 震动 (support/open mode/ close)
 * 屏幕常亮
 *
 * 缩放---正在聚焦-取消聚焦-缩放-继续聚焦
 * 拍照---是否聚焦-正在聚焦-聚焦完成后拍照
 */
@SuppressWarnings("ALL")
public final class CameraWrapper implements CameraAllCallback {

    private static final String TAG = CameraWrapper.class.getName();

    public interface CameraSurfaceView {
        /**
         * 获取预览显示对象
         */
        SurfaceHolder getSurfaceHolder();
        SurfaceTexture getSurfaceTexture();
        /**
         * 刷新界面
         */
        void requestView();
    }

    private Context mContext;
    private CameraSurfaceView mCameraSurfaceView;
    private boolean isLandscape = false;//是否横屏

    private CameraAllCallback mCameraAllCallback;
    private CameraHandler mCameraHandler;
    private final int MSG_REQUEST_VIEW = 1;
    private final int MSG_CANCLE_FOCUS = 2;
    private final int MSG_LOOP_FOCUS = 3;
    private final int MSG_RESUME_LOOP_FOCUS = 4;

    /**
     * 相机配置
     */
    private SharedPreferences sharedPreferences;
    private final String camera_config = "camera_config";
    private final String camera_preview_orientation = "_camera_preview_orientation";//镜头的预览旋转角度
    private final String camera_pic_orientation = "_camera_pic_orientation";//照片旋转角度
    private final String camera_patch_degree = "_camera_patch_degree";//校正的角度
    private int defaultPatchDegree = 90;
    /**
     * 0:PreviewDegree, 1:PictureDegree, 2:PatchDegree
     */
    private int[][] CameraOrientationInfoArray = null;

    /**
     * 屏幕旋转监听
     */
    private OrientationEventListener mOrientationEventListener;
    private boolean orientationEventListenerEnable = true;//默认开启监听

    /**
     * 初始化参数
     **/
    private Camera mCamera;
    private int mNumberOfCameras;// 镜头数量;
    private final int mDefaultCameraId = 0;
    private int mCurrentCameraId = mDefaultCameraId;  // Camera ID currently chosen
    private int mCameraCurrentlyLocked = -1;  // Camera ID that's actually acquired

    private Camera.Parameters mDefaultParameters;// 默认属性;
    private Camera.Parameters mCurrentParameters;// 当前镜头设置的属性;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;

    private boolean isViewCreate;
    private int previewWidth, previewHeight;
    private int mCurrentOrientation;

    private int pictureWidth, pictureHeight;
    private int mCurrentPictureOrientation;
    private int mPictureDegree;

    /**
     * 拍照状态
     */
    private States mCameraState = States.CAMERA_IDLE;
    private boolean takePicture;

    /**
     * 预览状态
     */
    private States mPreviewState = States.PREVIEW_IDLE;

    /**
     * 缩放
     */
    private boolean mZoomSupported;// 是否支持zoom;
    private int mMaxZoom;// 支持的zoom最大值;
    private boolean isZooming;// 正在设置焦距
    private int zoomFlag = 1;
    private int mCurrentZoom;

    /**
     * 对焦
     */
    private boolean mFocusAreaSupported;// 是否支持区域对焦;
    private List<String> mFocusModesValues;
    private States mFocusState = States.FOCUS_IDLE;// 设置为正在对焦状态;
    private String mFocusMode;
    private long mFocusStartTime;
    private boolean isFocusTimeOut;
    private boolean isLoopFocus;//是否自动循环对焦
    private long interval = 5000;//循环对焦间隔

    /**
     * 闪关灯
     */
    private boolean mFlashSupported;// 是否支持闪光灯;
    private int mFlashSupportedModeNums;// 支持闪光模式的数量;
    /**
     * 0:off, 1:on, 2:auto, 3:red-eye, 4:torch(常亮) 注：设置为on/auto/red-eye时，只有在对焦或拍照时闪光灯才会亮
     */
    private List<String> mFlashModesValues;// 支持的闪光灯值;
    private int mCurrentFlashMode;

    /**
     * 是否支持测光
     */
    private boolean mMeteringSupported;
    private int mMaxNumMeteringAreas;

    /**
     * 快门声音
     */
    private boolean mSilenceOnTaken;

    public CameraWrapper(Context context, CameraSurfaceView cameraSurfaceView) {
        mContext = context;
        if (cameraSurfaceView == null) {
            throw new IllegalArgumentException("cameraSurfaceView is null");
        }
        mCameraSurfaceView = cameraSurfaceView;

        int screenOrientation = context.getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        } else if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;
        }

        initOrientationEventListener();
        getCameraInfo();
    }

    /**
     * 获取镜头信息
     */
    private void getCameraInfo() {
        sharedPreferences = mContext.getSharedPreferences(camera_config, Context.MODE_PRIVATE);
        mNumberOfCameras = sharedPreferences.getInt("numberOfCameras", 0);
        if (mNumberOfCameras == 0) {
            mNumberOfCameras = Camera.getNumberOfCameras();

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            CameraOrientationInfoArray = new int[mNumberOfCameras][3];

            for (int i = 0; i < mNumberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCurrentCameraId = i;
//                MLog.i(TAG, "ori:"+mCurrentOrientation);
//                    CameraOrientationInfoArray[mCurrentCameraId][0] = 90;
                }
                CameraOrientationInfoArray[i][0] = defaultPatchDegree;
                CameraOrientationInfoArray[i][1] = cameraInfo.orientation;
                CameraOrientationInfoArray[i][2] = defaultPatchDegree;
            }

            //保存
            saveCameraConfig("numberOfCameras", mNumberOfCameras);
            saveCameraConfig("currentCameraId", mCurrentCameraId);
            for (int i = 0; i < mNumberOfCameras; i++) {
                saveCameraConfig(i + camera_preview_orientation, CameraOrientationInfoArray[i][0]);
                saveCameraConfig(i + camera_pic_orientation, CameraOrientationInfoArray[i][1]);
                saveCameraConfig(i + camera_patch_degree, CameraOrientationInfoArray[i][2]);
            }

        } else {
            defaultPatchDegree = 0;
            CameraOrientationInfoArray = new int[mNumberOfCameras][3];
            mCurrentCameraId = sharedPreferences.getInt("currentCameraId", 0);
            for (int i = 0; i < mNumberOfCameras; i++) {
                CameraOrientationInfoArray[i][0] = sharedPreferences.getInt(i + camera_preview_orientation, 0);
                CameraOrientationInfoArray[i][1] = sharedPreferences.getInt(i + camera_pic_orientation, 0);
                CameraOrientationInfoArray[i][2] = sharedPreferences.getInt(i + camera_patch_degree, 0);
            }
        }
    }

    /**
     * 保存镜头信息
     *
     * @param name
     * @param value
     */
    public void saveCameraConfig(String name, int value) {
        if (sharedPreferences == null) {
            sharedPreferences = mContext.getSharedPreferences(camera_config, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(name, value).commit();
    }

    /**
     * 获取相机的默认参数，此方法需在打开镜头后 设置参数之前 调用，否则获取的是上一个镜头的参数
     */
    private final void getCameraDefaultParameters() {
        if (mCamera != null) {
//            MLog.i(TAG, "--getCameraDefaultParameters--");
            mDefaultParameters = mCamera.getParameters();

            mSupportedPreviewSizes = mDefaultParameters.getSupportedPreviewSizes();
//            List<Camera.Size> picSizes = mDefaultParameters.getSupportedPictureSizes();
//            for (int i = 0; i < picSizes.size(); i++) {
//                MLog.i(TAG, "pic width:"+picSizes.get(i).width+", height:"+picSizes.get(i).height);
//            }

            mZoomSupported = mDefaultParameters.isZoomSupported();
            mMaxZoom = mDefaultParameters.getMaxZoom();// 获取zoom的最大值;

            // 检查区域对焦;
            mFocusModesValues = mDefaultParameters.getSupportedFocusModes();
            try {
                mFocusAreaSupported = ((mDefaultParameters.getMaxNumFocusAreas() > 0)
                        && (mFocusModesValues == null ? false : mFocusModesValues.indexOf(Camera.Parameters.FOCUS_MODE_AUTO) >= 0));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mFlashModesValues = mDefaultParameters.getSupportedFlashModes();
            if (mFlashModesValues != null && mFlashModesValues.size() >= 3) {
                mFlashSupportedModeNums = mFlashModesValues.size();
                mFlashSupported = true;
            } else {
                mFlashSupportedModeNums = 0;
                mFlashSupported = false;
            }

            // 检测测光
            try {
                mMaxNumMeteringAreas = mDefaultParameters.getMaxNumMeteringAreas();
                mMeteringSupported = mMaxNumMeteringAreas > 0;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

//            MLog.i(TAG, "mNumberOfCameras:"+mNumberOfCameras+", mCurrentCameraId:"+mCurrentCameraId
//                    +", mZoomSupported:"+mZoomSupported+", mFocusAreaSupported:"+mFocusAreaSupported
//                    +", mFlashSupported:"+mFlashSupported+", mMeteringSupported:"+mMeteringSupported);
        }
    }

    /**
     * 打开镜头
     */
    public void openCamera() {
        openCamera(mCurrentCameraId);
    }

    /**
     * 打开指定镜头
     *
     * @param cameraId
     */
    public void openCamera(int cameraId) {
        if (mNumberOfCameras <= 0 || cameraId < 0 || cameraId == mCameraCurrentlyLocked) {
            return;
        }
        stopPreview();
        releaseCamera();
        mCurrentCameraId = cameraId % mNumberOfCameras;
        try{
            mCamera = Camera.open(mCurrentCameraId);
        } catch (RuntimeException e){
            e.printStackTrace();
        }
        mCameraCurrentlyLocked = mCurrentCameraId;
        MLog.i(TAG, "openCamera:" + mCurrentCameraId);
        getCameraDefaultParameters();

        mCameraState = States.CAMERA_IDLE;
        saveCameraConfig("currentCameraId", mCurrentCameraId);

        setPreviewSize(previewWidth, previewHeight);
        setPictureSize(pictureWidth, pictureHeight);

        if (mCameraSurfaceView.getSurfaceHolder() != null) {
//            MLog.i(TAG, "openCamera--SurfaceHolder:" + mPreviewState.state);
            setPreviewDisplay(mCameraSurfaceView.getSurfaceHolder());

        } else if (mCameraSurfaceView.getSurfaceTexture() != null) {
//            MLog.i(TAG, "openCamera--SurfaceTexture:" + mPreviewState.state);
            setPreviewDisplay(mCameraSurfaceView.getSurfaceTexture());
        }

        mCurrentOrientation = CameraOrientationInfoArray[mCurrentCameraId][0];
        setPreviewOrientation(mCurrentOrientation);
        setPictureOrientation(mCurrentOrientation);

        resetPreviewSize();

        startPreview();
    }

    /**
     * 重新打开当前镜头
     */
    public void reopenCamera() {
        mCameraCurrentlyLocked = -1;
        openCamera(mCurrentCameraId);
    }

    /**
     * 切换镜头
     */
    public void switchCamera() {
        if (mCameraState == States.CAMERA_IDLE) {
            openCamera(mCameraCurrentlyLocked + 1);
        }
    }

    /**
     * 是否是前置镜头
     *
     * @return
     */
    public boolean isFront() {
        return mCurrentCameraId == 0 ? false : true;
    }

    /**
     * 获取镜头
     *
     * @return
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 获取当前镜头的参数
     *
     * @return
     */
    public Camera.Parameters getCameraParameters() {
        if (mCurrentParameters == null) {
            if (mCamera != null) {
                mCurrentParameters = mCamera.getParameters();
            }
        }
        return mCurrentParameters;
    }

    /**
     * 获取当前镜头Id
     * @return
     */
    public int getCurrentCameraId() {
        return mCurrentCameraId;
    }

    /**
     * 设置最佳的大小，在开始预览之前设置，否则可能会无效
     *
     * @param width
     * @param height
     */
    public void setOptimalPreviewSize(int width, int height) {
        if (!isViewCreate && mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height, 4.0f/3);
            setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        }
    }

    /**
     * 最佳的预览大小
     *
     * @param sizes
     * @param width
     * @param height
     * @param previewRatio 强制4:3的比例
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height, float previewRatio) {
//        MLog.i(TAG, "getOptimalPreviewSize width:"+width+", height:"+height);
        if (sizes == null) {
            return null;
        }
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = 0;
        int targetHeight = 0;
        if (width > height) {
            targetRatio = (double) width / height;
            targetHeight = height;
        } else {
            targetRatio = (double) height / width;
            targetHeight = width;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
//            MLog.i(TAG, "support size-> width:"+size.width+", size:"+size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
//            MLog.i(TAG, "optimalSize is null");
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
//        MLog.i(TAG, "optimalSize--size width:" + optimalSize.width + ", height:" + optimalSize.height);
//        if (optimalSize != null) {
//            optimalSize.width = (int) (previewRatio * optimalSize.height);
//        }

//        MLog.i(TAG, "optimalSize width:" + optimalSize.width + ", height:" + optimalSize.height);
        return optimalSize;
    }

    /**
     * 设置预览大小，在开始预览之前设置，否则可能会无效
     * @param width
     * @param height
     */
    public void setPreviewSize(int width, int height) {
        if (width == 0 || height == 0) {
            width = 800;
            height = 600;
        }
        previewWidth = width;
        previewHeight = height;
        if (mCamera != null) {
            getCameraParameters();
            mCurrentParameters.setPreviewSize(previewWidth, previewHeight);
            try {
                mCamera.setParameters(mCurrentParameters);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置图片大小，在开始预览之前设置，否则可能会无效
     * @param width
     * @param height
     */
    public void setPictureSize(int width, int height) {
//        MLog.i(TAG, "picture width:" + width + ", height:" + height);
        if (width == 0 || height == 0) {
            width = 960;
            height = 720;
        }
        pictureWidth = width;
        pictureHeight = height;
        if (mCamera != null) {
            getCameraParameters();
            mCurrentParameters.setPictureFormat(PixelFormat.JPEG);
            mCurrentParameters.setPictureSize(pictureWidth, pictureHeight);
            try {
                mCamera.setParameters(mCurrentParameters);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置预览和图片大小
     *
     * @param width
     * @param height
     */
    public void setPreviewAndPictureSize(int width, int height) {
        setPreviewSize(width, height);
        setPictureSize(width, height);
    }

    /**
     * 重置预览大小
     */
    public void resetPreviewSize() {
        if (mCameraHandler != null) {
            mCameraHandler.sendEmptyMessage(MSG_REQUEST_VIEW);
        }
    }

    /**
     * 校正预览方向（角度）
     */
    public int patchPreviewDegree() {
        int degree = (mCurrentOrientation + 90) % 360;
        CameraOrientationInfoArray[mCurrentCameraId][0] = degree;
        CameraOrientationInfoArray[mCurrentCameraId][2] = degree;
//        MLog.i(TAG, "patchPreviewDegree degree:" + degree);
        setPreviewOrientation(degree);
        saveCameraConfig(mCurrentCameraId + camera_preview_orientation, mCurrentOrientation);
        saveCameraConfig(mCurrentCameraId + camera_patch_degree, mCurrentOrientation);
        return mCurrentOrientation;
    }

    /**
     * 校正照片（角度）
     */
    public int patchPictureDegree() {
        int degree = (mCurrentPictureOrientation + 90) % 360;
//        MLog.i(TAG, "patchPictureDegree degree:" + degree);
        setPictureOrientation(degree);
        return mCurrentPictureOrientation;
    }

    /**
     * 校正预览方向和照片（角度）
     */
    public void patchPreviewAndPictureDegree() {
        int degree = (mCurrentOrientation + 90) % 360;
        CameraOrientationInfoArray[mCurrentCameraId][0] = degree;
        CameraOrientationInfoArray[mCurrentCameraId][2] = degree;
//        MLog.i(TAG, "patchPreviewAndPictureDegree degree:" + degree);

        setPreviewOrientation(degree);
        setPictureOrientation((isLandscape == true ? 0 : Math.abs(CameraOrientationInfoArray[mCurrentCameraId][0] - CameraOrientationInfoArray[mCurrentCameraId][1])) + mCurrentOrientation);

        saveCameraConfig(mCurrentCameraId + camera_preview_orientation, mCurrentOrientation);
        saveCameraConfig(mCurrentCameraId + camera_patch_degree, mCurrentOrientation);
    }

    /**
     * 设置预览方向（角度）
     *
     * @param orientation
     */
    public void setPreviewOrientation(int orientation) {
        if (mCamera != null) {
            if (orientation == -1) {
                orientation = mCurrentOrientation + 90;
            }
            mCurrentOrientation = (orientation + 360) % 360;
//            MLog.i(TAG, "setPreviewOrientation:" + mCurrentOrientation + ", isLandscape:" + isLandscape);

            mCamera.setDisplayOrientation(isLandscape == true ? 0 : mCurrentOrientation);

        }
    }

    /**
     * 设置照片方向（角度）
     *
     * @param orientation
     */
    public void setPictureOrientation(int orientation) {
        if (mCamera != null) {
            orientation = (isLandscape == true ? 0 : Math.abs(CameraOrientationInfoArray[mCurrentCameraId][0] - CameraOrientationInfoArray[mCurrentCameraId][1])) + orientation;
            if (orientation == -1) {
                orientation = mCurrentPictureOrientation + 90;
            }
            mCurrentPictureOrientation = (orientation + 360) % 360;;

            getCameraParameters();
            mCurrentParameters.setRotation(mCurrentPictureOrientation);
            try {
                mCamera.setParameters(mCurrentParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前预览旋转角度
     * @return
     */
    public int getCurrentPreviewOrientation() {
        return mCurrentOrientation;
    }

    /**
     * 获取当前照片旋转角度
     * @return
     */
    public int getCurrentPictureOrientation() {
        return mCurrentPictureOrientation;
    }

    /**
     * 设置预览显示
     */
    public void setPreviewDisplay(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);// 设置预览失败,释放镜头;
            } catch (IOException e) {
                e.printStackTrace();
                MLog.e(TAG, "IOException caused by setPreviewDisplay()");
                releaseCamera();
            }
        }
    }

    /**
     * OpenGL
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setPreviewDisplay(SurfaceTexture surface) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);// 设置预览失败,释放镜头;
            } catch (IOException e) {
                e.printStackTrace();
                releaseCamera();
            }
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
//        MLog.i(TAG, "startPreview--:" + mPreviewState.state);
        if (mCamera != null && mPreviewState == States.PREVIEW_IDLE) {
            mCamera.setErrorCallback(this);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            mPreviewState = States.PREVIEW_START;
            autoLoopFocus(true);
//            MLog.i(TAG, "startPreview-22-:" + mPreviewState.state);
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
//        MLog.i(TAG, "stopPreview--:"+ mPreviewState.state);
        if (mCamera != null && mPreviewState == States.PREVIEW_DOING) {
            autoLoopFocus(false);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mPreviewState = States.PREVIEW_STOP;
            mPreviewState = States.PREVIEW_IDLE;
        }
    }

    /**
     * 释放镜头
     */
    public void releaseCamera() {
//        MLog.i(TAG, "releaseCamera");
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCameraState = States.CAMERA_IDLE;
            mPreviewState = States.PREVIEW_IDLE;
        }
    }

    /**
     * 拍照
     */
    public void takePicture() {
        takePicture = true;
        if (mFocusState != States.FOCUS_SUCCESS) {
            doFocus(true);
        }
        checkFocusStateAndTakePicture();
    }

    /**
     * 检查对焦状态，如果成功则拍照
     */
    private synchronized void checkFocusStateAndTakePicture() {
        //1.不能对焦状态;2.对焦成功;3.对焦失败;
        if (takePicture && (mFocusMode.equals(Camera.Parameters.FOCUS_MODE_INFINITY)
                || mFocusMode.equals(Camera.Parameters.FOCUS_MODE_FIXED)
                || mFocusMode.equals("edof")
                || (mFocusState == States.FOCUS_SUCCESS
                || mFocusState == States.FOCUS_FAIL))) {
//            MLog.i(TAG, "checkFocusStateAndTakePicture:"+takePicture);
            takePicture = false;
            startTakePicture();
        } else if (mFocusState == States.FOCUS_DOING) {
            //如果是正在对焦,这个时候设置一个标签,让对焦完成之后直接拍照;
            mFocusState = States.TAKE_PICTURE_AFTER_FOCUS_FINISH;

        } else if (mFocusState == States.FOCUS_START) {
            //还未对焦;
        }
    }

    /**
     * 是否可以拍照
     *
     * @return
     */
    public boolean canTakePicture() {
        return mCameraState == States.CAMERA_IDLE && mPreviewState == States.PREVIEW_DOING;
    }

    /**
     * 开始拍照
     */
    private void startTakePicture() {
//        MLog.i(TAG, "startTakePicture");
        if (canTakePicture()) {
            mCamera.setPreviewCallback(null);
            try {
                mCameraState = States.CAMERA_START;
                mCamera.takePicture(mSilenceOnTaken ? null : this, null, null, this);
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(, "出现了异常", Toast.LENGTH_SHORT).show();
                //连拍时可能会出现异常
                stopPreview();
                startPreview();
            }
        }
    }

    /**
     * SurfaceView 创建时调用
     */
    public void onSurfaceViewCreate() {
        isViewCreate = true;
        if (mCameraHandler == null) {
            mCameraHandler = new CameraHandler(mContext.getMainLooper());
        }
        if (mPreviewState == States.PREVIEW_IDLE) {
//            MLog.i(TAG, "--onSurfaceViewCreate--resume--:" + mPreviewState.state);
            reopenCamera();

        } else {
            setPreviewSize(previewWidth, previewHeight);
            if (mCameraSurfaceView.getSurfaceHolder() != null) {
//                MLog.i(TAG, "onSurfaceViewCreate--SurfaceHolder:" + mPreviewState.state);
                setPreviewDisplay(mCameraSurfaceView.getSurfaceHolder());

            }else if (mCameraSurfaceView.getSurfaceTexture() != null) {
//                MLog.i(TAG, "onSurfaceViewCreate--SurfaceTexture:" + mPreviewState.state);
                setPreviewDisplay(mCameraSurfaceView.getSurfaceTexture());

            }else{
                throw new NullPointerException("SurfaceHolder or SurfaceTexture is null");
            }
            if (mCameraHandler != null) {
                mCameraHandler.sendEmptyMessage(MSG_REQUEST_VIEW);
            }
        }
        initOrientationEventListener();
        setOrientationEventListenerEnable(true);
        autoLoopFocus(true);
    }

    /**
     * SurfaceView 改变时调用
     */
    public void onSurfaceViewChange() {
//        MLog.i(TAG, "onSurfaceViewChange " + mPreviewState.state);
        if (mPreviewState == States.PREVIEW_IDLE) {
            if (mPreviewSize != null && (previewWidth <= 0 || previewHeight <= 0)) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }
            setPreviewSize(previewWidth, previewHeight);
            if (mCameraHandler != null) {
                mCameraHandler.sendEmptyMessage(MSG_REQUEST_VIEW);
            }
            startPreview();
        }
    }

    /**
     * SurfaceView 销毁时调用
     */
    public void onSurfaceViewDestory() {
//        MLog.i(TAG, "onSurfaceViewDestory");
        isViewCreate = false;
        autoLoopFocus(false);
        setOrientationEventListenerEnable(false);
        removeAllMsg();

        stopPreview();
        releaseCamera();
    }

    /**
     * 获取手机镜头数量
     *
     * @return
     */
    public int getNumberOfCameras() {
        return mNumberOfCameras;
    }

    /**
     * 是否支持缩放
     *
     * @return
     */
    public boolean isZoomSupported() {
        return mZoomSupported;
    }

    /**
     * 设置焦距
     * @param inOrOut 1:In, -1:Out
     */
    public void setCameraZoomInOrOut(int inOrOut) {
        if (inOrOut == 1 || inOrOut == -1){
            setCameraZoom(mCurrentZoom + inOrOut);
        }
    }

    /**
     * 设置焦距
     *
     * @param value 焦距:0~10
     */
    public void setCameraZoom(int value) {
        autoLoopFocus(false);
        mCameraHandler.removeMessages(MSG_RESUME_LOOP_FOCUS);

        if (mZoomSupported && (mCameraState == States.CAMERA_IDLE)
                && mPreviewState == States.PREVIEW_DOING
                && (mFocusState != States.FOCUS_DOING || mFocusState != States.FOCUS_DOING_ON_TOUCH)) {
            if (value > mMaxZoom) {
                value = mMaxZoom;
            } else if (value < 0) {
                value = 0;
            }
            if (value == mCurrentZoom) {
                return;
            }
            isZooming = true;
            mCurrentZoom = value;

            getCameraParameters();
            mCurrentParameters.setZoom(value);
            try {
                mCamera.setParameters(mCurrentParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isZooming = false;
        mCameraHandler.sendEmptyMessageDelayed(MSG_RESUME_LOOP_FOCUS, interval);
    }

    /**
     * 当前缩放大小
     *
     * @return
     */
    public int getCurrentZoom() {
        return mCurrentZoom;
    }

    /**
     * 是否支持对焦
     *
     * @return
     */
    public boolean isFocusAreaSupported() {
        return mFocusAreaSupported;
    }

    /**
     * 设置自动对焦（默认是自动循环对焦）
     * @param focus
     */
    public void setAutoFocus(boolean focus) {
        autoLoopFocus(focus);
        if (!focus) {
            mFocusState = States.FOCUS_SUCCESS;
        }
    }

    /**
     * 手动对焦
     */
    public void setFocusOnTouch(MotionEvent event) {
        if (isZooming)
            return;
        autoLoopFocus(false);//取消自动循环对焦
//        MLog.i(TAG, "setFocusOnTouch");
        if (mFocusState == States.FOCUS_DOING || mFocusState == States.FOCUS_DOING_ON_TOUCH)
            return;

        Rect focusRect = calculateTouchArea(event.getRawX(), event.getRawY(), 1f);
        Rect meteringRect = calculateTouchArea(event.getRawX(), event.getRawY(), 1.5f);

        if (getCameraParameters() == null)
            return;

        if (!Camera.Parameters.FOCUS_MODE_AUTO.equals(mFocusMode)) {
            mFocusMode = Camera.Parameters.FOCUS_MODE_AUTO;
            mCurrentParameters.setFocusMode(mFocusMode);
        }
        if (mFocusAreaSupported) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            mCurrentParameters.setFocusAreas(focusAreas);
        }
        if (mMeteringSupported) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));
            mCurrentParameters.setMeteringAreas(meteringAreas);
        }
        try {
            mCamera.setParameters(mCurrentParameters);
            mFocusStartTime = System.currentTimeMillis();
            mCamera.autoFocus(this);
            mFocusState = States.FOCUS_DOING_ON_TOUCH;
        } catch (Exception e) {
            e.printStackTrace();
            mFocusState = States.FOCUS_FAIL;
        }
    }

    /**
     * 计算触摸区域
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     */
    private Rect calculateTouchArea(float x, float y, float coefficient) {
        if (mPreviewSize == null)
            return null;
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x / mPreviewSize.width * 2000 - 1000);
        int centerY = (int) (y / mPreviewSize.height * 2000 - 1000);

        int left = checkBound(centerX - areaSize / 2, -1000, 1000);
        int right = checkBound(left + areaSize, -1000, 1000);
        int top = checkBound(centerY - areaSize / 2, -1000, 1000);
        int bottom = checkBound(top + areaSize, -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    /**
     * 检查边界
     *
     * @param x
     * @param min
     * @param max
     * @return
     */
    private int checkBound(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        }
        return x;
    }

    /**
     * 设置对焦模式
     *
     * @param focusMode
     */
    private void setFocusMode(String focusMode) {
        if (mFocusMode != null && mFocusMode.equals(focusMode)) {
            return;
        }

        autoLoopFocus(false);
//        if (mFocusState == States.FOCUS_DOING || mFocusState == States.FOCUS_DOING_ON_TOUCH) {
//            return;
//        }
        try {
            if (mFocusModesValues != null && mFocusModesValues.size() > 0) {
                if (mFocusModesValues.contains(focusMode)) {
                    getCameraParameters();
                    mCurrentParameters.setFocusMode(focusMode);
                    mCamera.setParameters(mCurrentParameters);
                    mFocusMode = focusMode;
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        autoLoopFocus(true);
    }

    /**
     * 是否强制对焦
     *
     * @param isMust
     */
    public void doFocus(boolean isMust) {
        if (mFocusMode == null) {// 如果对焦模式为null,设置为自动对焦模式;
            mFocusMode = Camera.Parameters.FOCUS_MODE_AUTO;
        }
        if (!mFocusAreaSupported) {// 前置对焦,直接返回对焦成功的状态
            mFocusState = States.FOCUS_SUCCESS;
            return;
        }
        if (!isMust) {// 如果是不强制对焦,直接返回对焦成功的状态;
            mFocusState = States.FOCUS_SUCCESS;
            return;
        }
        if (mFocusState == States.FOCUS_DOING) {// 如果正在对焦直接返回;
            return;
        }
        // 如果是FOCUS_MODE_INFINITY,不能开始对焦;
        if (!mFocusMode.equals(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            if (!isFront()) {
                mFocusState = States.FOCUS_START;
                if (isFocusTimeOut) {// 如果有对焦超时的情况;
                    mFocusState = States.FOCUS_SUCCESS;
                    isFocusTimeOut = false;
                    checkFocusStateAndTakePicture();
                } else {
                    autoFocus(true);
                }
            } else {
                mFocusState = States.FOCUS_SUCCESS;
            }
        }
    }

    /**
     * 自动对焦
     *
     * @param focus
     */
    private void autoFocus(boolean focus) {
        if (canTakePicture()) {
            if (focus && Camera.Parameters.FLASH_MODE_AUTO.equals(mFocusMode)) {
                try {
                    mFocusState = States.FOCUS_DOING;
                    //4秒之后自动清理对焦状态;
                    mCameraHandler.sendEmptyMessageDelayed(MSG_CANCLE_FOCUS, interval-1000);
                    mFocusStartTime = System.currentTimeMillis();
                    mCamera.autoFocus(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    mFocusState = States.FOCUS_SUCCESS;
                }
            } else {
                mFocusState = States.FOCUS_SUCCESS;
            }
        }
    }

    /**
     * 设置对焦区域和测光区域
     * @param focusArea
     * @param meteringArea
     */
    public void setFocusAndMeteringArea(String focusArea, String meteringArea) {
        getCameraParameters();
        if (mFocusAreaSupported && mMeteringSupported && mCurrentParameters != null) {
            mCurrentParameters.set("focus-areas", focusArea);
            mCurrentParameters.set("metering-areas", meteringArea);
            try {
                mCamera.setParameters(mCurrentParameters);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否支持闪光灯
     *
     * @return
     */
    public boolean isFlashSupported() {
        return mFlashSupported;
    }

    /**
     * 获取闪关灯模式 数量
     *
     * @return
     */
    public int getFlashSupportedModeNums() {
        return mFlashSupportedModeNums;
    }

    /**
     * 设置闪光灯默认
     *
     * @param flashMode 0:off, 1:on, 2:auto, 3:red-eye, 4:torch(常亮)
     */
    public void setFlashMode(int flashMode) {
        if (mFlashSupported && mFlashSupportedModeNums > 0) {
            if (flashMode < 0) {
                flashMode = 0;
            } else if (flashMode > mFlashSupportedModeNums - 1) {
                flashMode = mFlashSupportedModeNums - 1;
            }
            if (flashMode == mCurrentFlashMode) {
                return;
            }
            getCameraParameters();
            mCurrentParameters.setFlashMode(mFlashModesValues.get(flashMode));
            try {
                mCamera.setParameters(mCurrentParameters);
                mCurrentFlashMode = flashMode;
            } catch (Exception e) {
                e.printStackTrace();
            }
//            MLog.i(TAG, "mCurrentFlashMode:" + mCurrentFlashMode);
        }
    }

    /**
     * 当前闪关灯模式
     *
     * @return
     */
    public int getCurrentFlashMode() {
        return mCurrentFlashMode;
    }

    /**
     * 是否支持测光
     *
     * @return
     */
    public boolean isMeteringSupported() {
        return mMeteringSupported;
    }

    /**
     * 最大测光区域数量
     * @return
     */
    public int getMaxNumMeteringAreas() {
        return mMaxNumMeteringAreas;
    }

    /**
     * 快门声是否开启
     *
     * @return
     */
    public boolean isSilenceOnTaken() {
        return mSilenceOnTaken;
    }

    /**
     * 是否开启快门声音
     *
     * @param isSilence
     */
    public void setSilenceOnTaken(boolean isSilence) {
        mSilenceOnTaken = isSilence;
    }

    /**
     * 初始化屏幕方向监听
     */
    private void initOrientationEventListener() {
        setOrientationEventListenerEnable(false);
        mOrientationEventListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_UI) {
            float currentScreenDegree;

            @Override
            public void onOrientationChanged(int orientation) {
                float picDegree = checkPictureDegree(orientation);
                float degree = (360 - picDegree) % 360;
                if (degree >= 0 && degree != currentScreenDegree) {
                    if (currentScreenDegree - degree < -180) {
                        degree = degree - 360;
                    }
                    if (currentScreenDegree - degree > 180) {
                        degree = 360 - degree;
                    }
//                    isLandscape = (picDegree == 90 || picDegree == 270);
//                    setPictureOrientation(mCurrentOrientation);
//                    MLog.i(TAG, "picDegree:"+picDegree+", degree:"+degree);
                    onScreenOrientationChanged(orientation, isFront() ? (int) ((360 - picDegree) % 360) : (int) picDegree, currentScreenDegree, degree);
                    currentScreenDegree = (360 - picDegree) % 360;
                }
            }
        };
    }

    /**
     * 是否开启屏幕方向监听
     *
     * @param enable
     */
    public void setOrientationEventListenerEnable(boolean enable) {
        if (mOrientationEventListener != null && orientationEventListenerEnable) {
            if (enable) {
                mOrientationEventListener.enable();
            } else {
                mOrientationEventListener.disable();
                mOrientationEventListener = null;
            }
        }
    }

    /**
     * 校正照片角度
     *
     * @param degree
     * @return
     */
    public static float checkPictureDegree(float degree) {
        float absDegree = Math.abs(degree);
        if (absDegree != 0.0f) {
            int a = (int) (absDegree / 45);
            float b = absDegree % 45;
            if (a % 2 != 0 && b >= 0) {
                a += 1;
            }
            degree = degree / absDegree * a * 45;
        }
        return Math.abs(degree) % 360;
    }

    /**
     * 自动对焦时间间隔
     * @param time 毫秒值，默认5000
     */
    public void setAutoFocusInterval(long time) {
        if (time > 0) {
            interval = time;
        }
    }

    /**
     * 自动循环对焦
     *
     * @param focus
     */
    private void autoLoopFocus(boolean focus) {
        if (mFocusAreaSupported && mCameraHandler != null) {
            mCameraHandler.removeMessages(MSG_LOOP_FOCUS);
            isLoopFocus = focus;
            if (focus) {
                mCameraHandler.sendEmptyMessageDelayed(MSG_LOOP_FOCUS, interval);
            }
        }
    }

    class CameraHandler extends Handler {
        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REQUEST_VIEW:
                    if (mPreviewSize == null) {
                        //GLSurfaceView 是用线程执行的，因此需绑定UI线程的Looper来刷新UI
                        mCameraSurfaceView.requestView();
                    }
                    break;
                case MSG_CANCLE_FOCUS:
                    if (mFocusState != States.FOCUS_SUCCESS) {
                        mFocusState = States.FOCUS_SUCCESS;
                        isFocusTimeOut = true;
                    }
                    break;
                case MSG_LOOP_FOCUS:
                    doFocus(true);
                    autoLoopFocus(true);
                    break;
                case MSG_RESUME_LOOP_FOCUS:
                    autoLoopFocus(true);
                default:
                    break;
            }
        }
    }

    private void removeAllMsg() {
        if (mCameraHandler != null) {
            mCameraHandler.removeMessages(MSG_REQUEST_VIEW);
            mCameraHandler.removeMessages(MSG_CANCLE_FOCUS);
            mCameraHandler.removeMessages(MSG_LOOP_FOCUS);
            mCameraHandler.removeMessages(MSG_RESUME_LOOP_FOCUS);
            mCameraHandler = null;
        }
    }

//    ---------------------AllCallback-------------------------------

    /**
     * 设置所有回调
     *
     * @param cameraAllCallback
     */
    public void setCameraAllCallback(CameraAllCallback cameraAllCallback) {
        mCameraAllCallback = cameraAllCallback;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
//        MLog.i(TAG, "onAutoFocus:" + success + ", time:" + (System.currentTimeMillis() - mFocusStartTime));
        if (mCameraAllCallback != null) {
            mCameraAllCallback.onAutoFocus(success, camera);
        }
        isFocusTimeOut = false;
        if (success && mCameraHandler != null) {
            mCameraHandler.removeMessages(MSG_CANCLE_FOCUS);
        }
        if (mFocusState == States.TAKE_PICTURE_AFTER_FOCUS_FINISH) {
            if (success) {
                mFocusState = States.FOCUS_SUCCESS;
            } else {
                mFocusState = States.FOCUS_FAIL;
            }
            checkFocusStateAndTakePicture();
        } else if (mFocusState == States.FOCUS_DOING || mFocusState == States.FOCUS_DOING_ON_TOUCH) {
            //如果正在对焦
            if (success) {
                mFocusState = States.FOCUS_SUCCESS;
            } else {
                mFocusState = States.FOCUS_FAIL;
            }
        }

    }

    @Override
    public void onError(int error, Camera camera) {
        MLog.i(TAG, "onError:" + error);
        mCameraState = States.CAMERA_IDLE;
        if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
            MLog.i(TAG, "onError: CAMERA ERROR SERVER DIED ->" + error);
        }
        if (mCameraAllCallback != null) {
            mCameraAllCallback.onError(error, camera);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
//        MLog.i(TAG, "----------onPictureTaken--------");
        mCameraState = States.CAMERA_DOING;
        if (data != null && data.length > 0) {
            mCameraState = States.CAMERA_SUCCESS;
            if (mCameraAllCallback != null) {
                mCameraAllCallback.onPictureTaken(data, camera);
//                boolean horizontal = (mPictureDegree % 180 == 0);
//                mCameraAllCallback.onPictureTaken(isFront() ? mirrorConver(data, !horizontal, horizontal) : data, camera);
            }
        } else {
            mCameraState = States.CAMERA_FAIL;
        }
        mCameraState = States.CAMERA_IDLE;
        
        //如果只拍一次则不用重新预览
//        stopPreview();
//        startPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        MLog.i(TAG, "----------onPreviewFrame--------");
        mPreviewState = States.PREVIEW_DOING;
        if (data != null && data.length > 0) {
//            mPreviewState = States.PREVIEW_SUCCESS;
            if (mCameraAllCallback != null) {
                mCameraAllCallback.onPreviewFrame(data, camera);
            }
        } else {
//            mPreviewState = States.PREVIEW_FAIL;
        }
    }

    @Override
    public void onShutter() {
//        MLog.i(TAG, "----------onShutter--------");
        mFocusState = States.FOCUS_IDLE;
        if (mCameraAllCallback != null) {
            mCameraAllCallback.onShutter();
        }
    }

    @Override
    public void onScreenOrientationChanged(int orientation, int pictureDegree, float fromDegree, float toDegree) {
//        MLog.i(TAG, "----------onScreenOrientationChanged--------");
        mPictureDegree = pictureDegree;
        if (mCameraAllCallback != null) {
            mCameraAllCallback.onScreenOrientationChanged(orientation, pictureDegree, fromDegree, toDegree);
        }
    }

    /**
     * 镜像翻转
     *
     * @param data
     * @return
     */
    public byte[] mirrorConver(byte[] data, boolean vertical, boolean horizontal) {
        if (data != null && data.length > 0) {
            getCameraParameters();
            Camera.Size size = mCurrentParameters.getPictureSize();
            Bitmap temp = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (temp != null) {
                Matrix matrix = new Matrix();
                if (vertical) {
                    matrix.postScale(1, -1);//垂直翻转
                }
                if (horizontal) {
                    matrix.postScale(-1, 1);//水平翻转
                }
                Bitmap bitmap = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
                temp.recycle();
                temp = null;

                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    return baos.toByteArray();
                }
            }
        }
        return data;
    }
}
