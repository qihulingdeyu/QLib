package com.qing.callback;

/**
 * Created by zwq on 2015/08/31 16:56.<br/><br/>
 */
public abstract class DownloadCallback extends AbstractTaskCallback {

    private static final String TAG = DownloadCallback.class.getName();

    public static final int START = 0x3;
    public static final int DOWNLOADING = 0x4;

    public abstract void onStart();
    public abstract void onDownloading(long progress);
    public abstract void onSuccess();
    public abstract void onFail(String msg);

    @Override
    public void dispatchResult(int state, Object... data) {
        switch (state){
            case START:
                onStart();
                break;
            case DOWNLOADING:
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
            default:
                break;
        }
    }
}
