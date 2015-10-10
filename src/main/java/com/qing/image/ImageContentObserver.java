package com.qing.image;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

/**
 * Created by zwq on 2015/10/09 10:52.<br/><br/>
 * 系统图库观察者，监听图库的变化
 */
public class ImageContentObserver extends ContentObserver {

    private static final String TAG = ImageContentObserver.class.getName();
//    public static final String URI_IMAGE = "content://media/external/images/media";
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

        if (uri.toString().equals(Media.EXTERNAL_CONTENT_URI.toString())){
            ImageStore.loadImage(mContext);
        }
    }
}
