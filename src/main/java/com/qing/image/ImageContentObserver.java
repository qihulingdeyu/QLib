package com.qing.image;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.qing.log.MLog;

/**
 * Created by zwq on 2015/10/09 10:52.<br/><br/>
 * 系统图库观察者，监听图库的变化
 */
public class ImageContentObserver extends ContentObserver {

    private static final String TAG = ImageContentObserver.class.getName();
    public static final String URI_IMAGE = "content://media/external/images/media";
    private Context mContext;

    public ImageContentObserver(Context context, Handler handler) {
        this(handler);
        mContext = context;
    }

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ImageContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        MLog.i(TAG, "--onChange--selfChange:" + selfChange + ", uri:" + uri.toString());
        //删除: content://media/external
        //添加: content://media/external/images/media
        if (uri.toString().equals("content://media/external") ||
                uri.toString().equals(URI_IMAGE)){
            ImageStore.loadImage(mContext);
        }
    }
}
