package com.qing.callback;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by zwq on 2015/07/21 16:10.<br/><br/>
 * 简化消息的响应，并可自定义扩展实现不同的功能
 */
public abstract class AbstractTaskCallback {
    protected String TAG = this.getClass().getSimpleName();

    public static final int WAIT = 0x1;
    public static final int START = 0x2;
    public static final int LOADING = 0x3;
    public static final int SUCCESS = 0x4;
    public static final int FAIL = 0x5;
    public static final int FINISH = 0x6;

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
                case SUCCESS :
                    result.mCallback.dispatchResult(result.mState, result.mData);
                    break;
                case FAIL :
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
        public final int mState;
        public final Object[] mData;

        public TaskResult(AbstractTaskCallback callback, int state, Object... data) {
            mCallback = callback;
            mState = state;
            mData = data;
        }
    }

    /**
     * 提交消息结果
     * @param result
     */
    public final void postResult(int state, Object... result) {
        Message message = mHandler.obtainMessage(SUCCESS, new TaskResult(this, state, result));
        message.sendToTarget();
    }

    /**
     * 子类必须实现此方法对消息进行处理（分发）
     * @param data
     */
    public abstract void dispatchResult(int state, Object... data);

    /**
     * 事件处理失败
     */
    private void handlerFail(){
        Log.i(TAG, TAG +" handle message fail!");
//        throw new Exception(TAG +" handle message fail!");
    }

    /**
     * 校验参数个数
     * @param size
     * @param data
     */
    protected final void verifyParams(int size, Object... data){
        if (data==null || data.length<size){
            throw new IllegalArgumentException("Number of parameters must be at least "+size);
        }
    }
}
