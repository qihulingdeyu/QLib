package com.qing.callback;

/**
 * Created by zwq on 2015/08/31 16:56.<br/><br/>
 */
public abstract class DownloadCallback extends AbstractTaskCallback {

    private static final String TAG = DownloadCallback.class.getName();

    public void onStart() {}
    public abstract void onDownloading(long progress);
    public abstract void onSuccess();
    public abstract void onFail(String msg);
    public void onFinish() {}

    @Override
    public void dispatchResult(int state, Object... data) {
        switch (state){
            case START:
                onStart();
                break;
            case LOADING:
                verifyParams(1, data);
                onDownloading((Long)data[0]);
                break;
            case SUCCESS:
                onSuccess();
                break;
            case FAIL:
                verifyParams(1, data);
                onFail((String)data[0]);
                break;
            case FINISH:
                onFinish();
                break;
            default:
                break;
        }
    }
}
