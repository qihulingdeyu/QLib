package com.qing.callback;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zwq on 2015/07/21 16:20.<br/><br/>
 */
public abstract class HttpCallback extends AbstractTaskCallback {

    public void onWait() {}
    public void onStart() {}
    public abstract void onSuccess(String content, InputStream is);
    public abstract void onFail(String msg);
    public void onFinish() {}

    @Override
    public final void dispatchResult(int state, Object... data) {
        switch (state) {
            case WAIT :
                onWait();
                break;
            case START :
                onStart();
                break;
            case SUCCESS :
                verifyParams(2, data);
                String content = (String)data[0];
                InputStream is = (InputStream)data[1];
                onSuccess(content, is);
                if (is!=null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    is = null;
                }
                break;
            case FAIL :
                verifyParams(1, data);
                onFail((String)data[0]);
                break;
            case FINISH :
                onFinish();
                break;
            default :
                break;
        }
    }
}
