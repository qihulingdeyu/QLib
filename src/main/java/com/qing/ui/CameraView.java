package com.qing.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qing.log.MLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by zwq on 2015/03/23 10:11.<br/><br/>
 * 摄像头预览
 */
public class CameraView extends RelativeLayout implements SurfaceHolder.Callback {

    private static final String TAG = CameraView.class.getName();
	private Context mContext;
	private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ca/";
	private static final String photoFileName = "yyyyMMdd-HHmmss-";
	private static final String photoExt = ".jpg";
	private int previewW = 768;
	private int previewH = 1280;
	private int photoW = 1280;
	private int photoH = 768;

	public CameraView(Context context) {
		this(context, null);
	}
    
    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();

        // 拍照之前设置
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        // 保存当前的音量
        int volumn = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        if (volumn != 0) {
            // if("mute".equals(manager.getMode()) && volumn != 0){
            // 如果需要静音并且当前未静音（muteMode的设置可以放在Preference中）
            manager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }

        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return;
            }
        }
    }

	private ControllerBar controllerBar;
	private int ID_BAR = 1;
	private SurfaceView surfaceView;
	private int ID_CAMERA = 2;
	private SurfaceHolder surfaceHolder;
	private Camera mCamera;
	private TextView count;
	private void initView() {
		MLog.i(TAG, "------initView");
		DisplayMetrics outMetrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		previewW = outMetrics.widthPixels;
		previewH = outMetrics.heightPixels;
//		previewH = previewW * 4 / 3;

		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		surfaceView = new SurfaceView(mContext);
		surfaceView.setId(ID_CAMERA);
		addView(surfaceView, params);

		params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		count = new TextView(mContext);
		count.setText("0");
		count.setTextSize(20);
		count.setBackgroundColor(0x6087cefa);
		count.setVisibility(View.GONE);
		addView(count, params);

		params = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_BOTTOM, ID_CAMERA);
		controllerBar = new ControllerBar(mContext, this);
		controllerBar.setId(ID_BAR);
		controllerBar.setBackgroundColor(0x3e000000);
		addView(controllerBar, params);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
	}

	private void initCamera() {
		if (mCamera != null) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
				/* 设定相片大小为1024*768， 格式为JPG */
				parameters.setPictureFormat(PixelFormat.JPEG);
				parameters.setPreviewSize(previewW, previewH);
				parameters.setPictureSize(photoW, photoH);
				mCamera.setParameters(parameters);
				/* 打开预览画面 */
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void resetCamera() {
		if (mCamera != null) {
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		MLog.i("bbb", "------surfaceCreated");
		try {
			/* 打开相机， */
			mCamera = Camera.open();
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(surfaceHolder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		/* 相机初始化 */
		initCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		resetCamera();
		mCamera.release();
		mCamera = null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		// viewHeight = MeasureSpec.getSize(heightMeasureSpec);
	}

	private boolean isLongClick = false;
	private int photoCount = 0;
	private PictureCallback jpeg = new PictureCallback() {
		@Override
		public void onPictureTaken(final byte[] data, Camera camera) {
			// 保存照片
			if (allData == null) {
				allData = new ArrayList<byte[]>();
			}
			allData.add(data);
			if (isLongClick) {
				photoCount++;
				count.setVisibility(View.VISIBLE);
				count.setText("" + photoCount);
			}
			if (!isLongClick && photoCount != 0) {
				photoCount++;
				count.setText("" + photoCount);
				photoCount = 0;
				MLog.i("bbb", "----------last");
				count.setVisibility(View.GONE);
			}
			savePic();

			resetCamera();
			initCamera();
		}
	};
	public void takePic() {
		if (mCamera != null) {
			try {
				mCamera.takePicture(null, null, jpeg);
			} catch (Exception e) {
				Toast.makeText(mContext, "出现了异常", Toast.LENGTH_SHORT).show();
				//连拍时可能会出现异常
				resetCamera();
				initCamera();
			}
		}
	}

	private List<byte[]> allData;
	private SimpleDateFormat sdf;
	private Thread saveThread;
	private BufferedOutputStream out;
	protected void savePic() {
		if (allData == null || allData.isEmpty()) {
			return;
		}
		if (sdf == null) {
			sdf = new SimpleDateFormat(photoFileName, Locale.CHINA);
		}
		if (saveThread != null && !saveThread.isAlive()) {
			saveThread = null;
		}
		if (saveThread == null) {
			saveThread = new Thread() {
				public void run() {
					Iterator<byte[]> iter = allData.iterator();
					Bitmap mbitmap = null;
					while (iter.hasNext()) {
						byte[] data = iter.next();

						String ram = "" + Math.random();
						String name = sdf.format(new Date())
								+ ram.substring(2, 8) + photoExt;
						
						if(data==null){
							continue;
						}
						mbitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						Matrix matrix = new Matrix();
						matrix.postRotate(90f);
						mbitmap = Bitmap.createBitmap(mbitmap, 0, 0, mbitmap.getWidth(), mbitmap.getHeight(), matrix, true);
						if(mbitmap==null){
							continue;
						}
						
						try {
							out = new BufferedOutputStream(
									new FileOutputStream(new File(path + name)));
							mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//							out.write(data, 0, data.length);
							out.flush();
							out.close();
							out = null;
							mbitmap.recycle();
							iter.remove();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					allData = null;
				};
			};
			saveThread.start();
		}
	}
	public void showControllerBar() {
		controllerBar.setVisibility(View.VISIBLE);
	}
	public void hideControllerBar() {
		controllerBar.setVisibility(View.GONE);
	}

	class ControllerBar extends RelativeLayout implements OnClickListener, OnLongClickListener, OnTouchListener {
		private Context context;
		private CameraView camera;
		public ControllerBar(Context context, CameraView camera) {
			super(context);
			this.context = context;
			this.camera = camera;
			init();
		}
		private void init() {
			LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//			CustomButton tackP = new CustomButton(context);
//			tackP.setText("拍照");
//			tackP.setTextSize(20);
//			tackP.setOnClickListener(this);
//			tackP.setOnLongClickListener(this);
//			tackP.setOnTouchListener(this);
//			addView(tackP, params);
		}

		@Override
		public void onClick(View v) {
			if (camera != null) {
				camera.takePic();
			}
		}
		
		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					camera.takePic();
				}
				super.handleMessage(msg);
			}
		};
		
		private boolean fingerUp = false;
		@Override
		public boolean onLongClick(View v) {
			isLongClick = true;
			fingerUp = false;
			photoCount = 0;
			new Thread() {
				public void run() {
					while (!fingerUp) {
						mHandler.sendEmptyMessage(1);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
			return true;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				fingerUp = true;
				isLongClick = false;
			}
			return false;
		}
	}
	
	public ShapeDrawable shapeDrawable() {
		int radius = 20;
		float[] outerR = new float[]{radius, radius, radius, radius, radius,
				radius, radius, radius};
		// 构造一个圆角矩形,可以使用其他形状，这样ShapeDrawable 就会根据形状来绘制。
		RoundRectShape roundRectShape = new RoundRectShape(outerR, null, null);
		// 如果要构造直角矩形可以
		// RectShape rectShape = new RectShape();
		// 组合圆角矩形和ShapeDrawable
		ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
		// 设置形状的颜色
		shapeDrawable.getPaint().setColor(0x6087cefa);
		// 设置绘制方式为填充
		shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
		return shapeDrawable;
	}
	
}
