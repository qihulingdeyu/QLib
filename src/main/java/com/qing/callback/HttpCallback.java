package com.qing.callback;

public abstract class HttpCallback extends AbstractTaskCallback {

    public static final int SUCCESS = 0x1;
    public static final int FAIL = 0x2;
    
    public abstract void onSuccess(Object... data);
    public abstract void onFail(Object... data);
    
    @Override
    public final void dispatchResult(Object... data) {
        switch ((Integer)data[0]) {
            case SUCCESS :
                onSuccess(data);//Arrays.copyOfRange(data, 1, data.length)
                break;
            case FAIL :
                onFail(data);
                break;
            default :
                break;
        }
    }
}
