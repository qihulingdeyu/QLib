package com.qing.callback;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Author: zwq <br/>
 * Date: 2015-7-21 <br/>
 * Time: 下午4:10:03 <br/>
 * 简化消息的响应，并可自定义扩展实现不同的功能
 */
public abstract class AbstractTaskCallback {
    protected String TAG = this.getClass().getSimpleName();
    
    private static final int HANDLER_SUCCESS = 0x1;
    private static final int HANDLER_FAIL = 0x2;
    private static final InternalHandler mHandler = new InternalHandler();
    
    /**
     * 内部消息处理器
     */
    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TaskResult result = (TaskResult) msg.obj;
            switch (msg.what) {
                case HANDLER_SUCCESS :
                    result.mCallback.dispatchResult(result.mData);
                    break;
                case HANDLER_FAIL :
                    result.mCallback.handlerFail();
                    break;
                default :
                    result.mCallback.handlerFail();
                    break;
            }
        }
    }
    
    private static class TaskResult {
        public final AbstractTaskCallback mCallback;
        public final Object[] mData;
        
        public TaskResult(AbstractTaskCallback callback, Object... data) {
            mCallback = callback;
            mData = data;
        }
    }
    
    /**
     * 提交消息结果
     * @param result
     */
    public final void postResult(Object... result) {
        Message message = mHandler.obtainMessage(HANDLER_SUCCESS, new TaskResult(this, result));
        message.sendToTarget();
    }
    
    /**
     * 子类必须实现此方法对消息进行处理（分发）
     * @param data
     */
    public abstract void dispatchResult(Object... data);
    
    /**
     * 事件处理失败
     */
    private void handlerFail(){
        Log.i(TAG, "--handlerFail--");
    }
}
