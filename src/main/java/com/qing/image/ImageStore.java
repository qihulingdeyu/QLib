package com.qing.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import com.qing.log.MLog;
import com.qing.utils.StringUtils;
import com.qing.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by zwq on 2015/10/10 10:54.<br/><br/>
 */
public class ImageStore {

    private static final String TAG = ImageStore.class.getName();
    private static final String thumbFolder = "thumb";

    private static Context mContext;
    private static boolean hasLoad;
    private static List<ImageInfo> imageInfos = new ArrayList<>();
    private static Map<String, List<ImageInfo>> folderInfos = new HashMap<>();
    private static ImageStoreChangeListener mListener;
    private static Handler mHandler;

    public interface ImageStoreChangeListener{
        void onChange();
    }

    public static void setImageStoreChangeListener(ImageStoreChangeListener listener){
        mListener = listener;
    }

    public static void loadImage(Context context){
        MLog.i(TAG, "--loadImage--");
        mContext = context;

//        getImageThumbNailsInfo();
//        getImageInfo(0);

        if (mHandler == null){
            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    hasLoad = true;
                    if (mListener != null) {
                        mListener.onChange();
                    }
                    return false;
                }
            });
        }

        ThreadUtils threadUtils = new ThreadUtils() {
            @Override
            public void execute() {
                //获取图片数据
                getImageInfos();

                //获取缩略图数据
                getImageThumbInfos();

                //按文件夹分类
                sortByFolder();

//                printList();
            }

            @Override
            public void finish() {
                super.finish();
                if (mHandler != null)
                    mHandler.sendEmptyMessage(1);
            }
        };
        threadUtils.start();
    }

    private static synchronized void getImageInfos(){
        Cursor cursor = null;
        ImageInfo imageInfo = null;
        cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, null, null, " "+ Media._ID+" desc ");

        if (cursor != null){
            imageInfos.clear();
            while (cursor.moveToNext()){
                imageInfo = new ImageInfo();

                imageInfo.set_id(cursor.getInt(cursor.getColumnIndex(Media._ID)));
                imageInfo.setImage_id(imageInfo.get_id());
                imageInfo.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
                imageInfo.setName(cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME)));
                imageInfo.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));

                imageInfo.setDate_added(cursor.getLong(cursor.getColumnIndex(Media.DATE_ADDED)));
                imageInfo.setDate_modified(cursor.getLong(cursor.getColumnIndex(Media.DATE_MODIFIED)));
                imageInfo.setWidth(cursor.getInt(cursor.getColumnIndex(Media.WIDTH)));
                imageInfo.setHeight(cursor.getInt(cursor.getColumnIndex(Media.HEIGHT)));

                imageInfo.setSize(cursor.getInt(cursor.getColumnIndex(Media.SIZE)));
                imageInfo.setOrientation(cursor.getInt(cursor.getColumnIndex(Media.ORIENTATION)));
                imageInfo.setFolder_id(cursor.getInt(cursor.getColumnIndex(Media.BUCKET_ID)));
                imageInfo.setFolder_name(cursor.getString(cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME)));

                imageInfos.add(imageInfo);
            }
            cursor.close();
        }
        cursor = null;
    }

    private static synchronized void getImageThumbInfos() {
        //判断thumb_path是否为null
        //
        if (imageInfos!=null && !imageInfos.isEmpty()){
            String folderName = null;

            synchronized (imageInfos){
                for (ImageInfo imageInfo : imageInfos) {
                    if (imageInfo!=null){
                        folderName = imageInfo.getFolder_name();
                        if (StringUtils.isNullOrEmpty(imageInfo.getThumb_path())){
                            getImageThumbInfo(imageInfo);
                        }
                    }
                }
            }
            folderName = null;
        }
    }

    private static void getImageThumbInfo(ImageInfo imageInfo){
        if (imageInfo != null){
            Cursor cursor = mContext.getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI,
                    null, Thumbnails.IMAGE_ID+"=?", new String[]{""+imageInfo.get_id()}, null);
            if (cursor != null){
                while (cursor.moveToNext()){
                    imageInfo.setThumb_id(cursor.getInt(cursor.getColumnIndex(Thumbnails._ID)));
                    imageInfo.setThumb_path(cursor.getString(cursor.getColumnIndex(Thumbnails.DATA)));
                    imageInfo.setThumb_width(cursor.getInt(cursor.getColumnIndex(Thumbnails.WIDTH)));
                    imageInfo.setThumb_height(cursor.getInt(cursor.getColumnIndex(Thumbnails.HEIGHT)));
                    imageInfo.setThumb_kind(cursor.getInt(cursor.getColumnIndex(Thumbnails.KIND)));
                }
                cursor.close();
            }
            cursor = null;
        }
    }

    private static synchronized void sortByFolder() {
        if (imageInfos!=null && !imageInfos.isEmpty()){
            String folderName = null;
            List<ImageInfo> folderItem = null;

            synchronized (imageInfos){
                folderInfos.clear();
                for (ImageInfo imageInfo : imageInfos) {
                    if (imageInfo!=null){
                        folderName = imageInfo.getFolder_name();

                        if (!folderInfos.containsKey(folderName)){
                            folderItem = new ArrayList<>();
                            folderInfos.put(folderName, folderItem);
                        }else{
                            folderItem = folderInfos.get(folderName);
                        }
                        if (folderItem != null){
                            folderItem.add(imageInfo);
                        }
                    }
                }
            }
            //按文件夹名称升序排序
            folderInfos = sortMapByKey(folderInfos);

            folderItem = null;
            folderName = null;
        }
    }

    private static Map<String, List<ImageInfo>> sortMapByKey(Map<String, List<ImageInfo>> src){
        if (src == null || src.isEmpty()){
            return src;
        }
        Map<String, List<ImageInfo>> sortMap = new TreeMap<String, List<ImageInfo>>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
        sortMap.putAll(src);
        return sortMap;
    }

    public static List<ImageInfo> getImageInfosList(){
        return imageInfos;
    }

    public static Map<String, List<ImageInfo>> getImageInfosFolderList(){
        return folderInfos;
    }

    private static void printList(){
        for (Map.Entry<String, List<ImageInfo>> entry: folderInfos.entrySet()) {
            for (ImageInfo imageInfo : entry.getValue()) {
                MLog.i(TAG, imageInfo.toString());
            }
        }
    }

    public static void clearAll(){
        if (imageInfos != null){
            imageInfos.clear();
            imageInfos = null;
        }
        if (folderInfos != null){
            folderInfos.clear();
            folderInfos = null;
        }
        if (mListener != null){
            mListener = null;
        }
        if (mHandler != null){
            mHandler = null;
        }
    }

    /**
     * 获取缩略图
     * @param id
     * @return
     */
    public static Bitmap getThumb(int id){
        Bitmap bitmap = null;
        if (mContext != null)
            bitmap = Thumbnails.getThumbnail(mContext.getContentResolver(), id, Thumbnails.MINI_KIND, null);

        return bitmap;
    }

    private static void getImageThumbNailsInfo(){
        Cursor cursor = mContext.getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI,
                null, null, null, " "+ Thumbnails._ID+" desc ");
        if (cursor != null){
            while (cursor.moveToNext()){
                ImageInfo thumbInfo = new ImageInfo();

                thumbInfo.setThumb_id(cursor.getInt(cursor.getColumnIndex(Thumbnails._ID)));
                thumbInfo.setImage_id(cursor.getInt(cursor.getColumnIndex(Thumbnails.IMAGE_ID)));
                thumbInfo.setThumb_path(cursor.getString(cursor.getColumnIndex(Thumbnails.DATA)));
                thumbInfo.setThumb_width(cursor.getInt(cursor.getColumnIndex(Thumbnails.WIDTH)));
                thumbInfo.setThumb_height(cursor.getInt(cursor.getColumnIndex(Thumbnails.HEIGHT)));
                thumbInfo.setThumb_kind(cursor.getInt(cursor.getColumnIndex(Thumbnails.KIND)));

                Log.i("bbb", thumbInfo.toString());
                getImageInfo(thumbInfo.getImage_id());

            }
            cursor.close();
        }
        cursor = null;
    }

    private static void getImageInfo(int id){
        Cursor cursor = null;
        ImageInfo imageInfo = null;
        if (id>0){
            cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, Media._ID+"=?", new String[]{""+id}, null);
        }else{
            cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, null, null, " "+ Media._ID+" desc ");
        }
        if (cursor != null){
            while (cursor.moveToNext()){
                imageInfo = new ImageInfo();

                imageInfo.set_id(cursor.getInt(cursor.getColumnIndex(Media._ID)));
                imageInfo.setImage_id(imageInfo.get_id());
                imageInfo.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
                imageInfo.setName(cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME)));
                imageInfo.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));

                imageInfo.setDate_added(cursor.getLong(cursor.getColumnIndex(Media.DATE_ADDED)));
                imageInfo.setDate_modified(cursor.getLong(cursor.getColumnIndex(Media.DATE_MODIFIED)));
                imageInfo.setWidth(cursor.getInt(cursor.getColumnIndex(Media.WIDTH)));
                imageInfo.setHeight(cursor.getInt(cursor.getColumnIndex(Media.HEIGHT)));

                imageInfo.setSize(cursor.getInt(cursor.getColumnIndex(Media.SIZE)));
                imageInfo.setOrientation(cursor.getInt(cursor.getColumnIndex(Media.ORIENTATION)));
                imageInfo.setFolder_id(cursor.getInt(cursor.getColumnIndex(Media.BUCKET_ID)));
                imageInfo.setFolder_name(cursor.getString(cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME)));


                Log.i(TAG, imageInfo.toString());
            }
            cursor.close();
        }
        cursor = null;
    }

}
