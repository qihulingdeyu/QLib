package com.qing.image;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;

import com.qing.log.MLog;
import com.qing.utils.FileUtils;
import com.qing.utils.StringUtils;
import com.qing.utils.ThreadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zwq on 2015/10/10 10:54.<br/><br/>
 * 图库、相册扫描，将图片插入到图库<br/>
 * 或从图库删除图片
 */
public class ImageStore {

    private static final String TAG = ImageStore.class.getName();
    private static final String thumbSuffix = ".thumb";

    private static Context mContext;
    private static boolean hasLoad;
    private static List<ImageInfo> imageInfos = new ArrayList<>();
    private static Map<String, List<ImageInfo>> folderInfos = new HashMap<>();

    private static Handler mHandler;
    private static ThreadUtils threadUtils;
    private static ImageStoreChangeListener mListener;

    private static String cacheThumbPath;
    private static boolean deleteInvalidThumb;
    private static List<String> tempThumbPathList;

    public interface ImageStoreChangeListener{
        void onChange();
    }

    public static void setImageStoreChangeListener(ImageStoreChangeListener listener){
        mListener = listener;
    }

    /** 是否已经加载过 */
    public static boolean isHasLoad() {
        return hasLoad;
    }

    /** 自定义缩略图缓存目录 */
    public static void setCacheThumbPath(String path){
        if (StringUtils.isNullOrEmpty(path))
            return;
        File file = new File(path);
        if (!file.exists()){
            if (file.mkdirs()){
                cacheThumbPath = path;
                deleteInvalidThumb = true;
            }
        }else{
            cacheThumbPath = path;
            deleteInvalidThumb = true;
        }
        file = null;
    }

    public static void loadImage(Context context){
        mContext = context;

        if (mHandler == null){
            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 1){
                        MLog.i(TAG, "--loadImage-finish-");
                        hasLoad = true;
                        if (mListener != null) {
                            mListener.onChange();
                        }
                    }else if (msg.what == 2){
                        if (threadUtils != null){
                            threadUtils.clearAll();
                            threadUtils = null;
                        }
                    }
                    return false;
                }
            });
        }

        if (threadUtils != null){
            MLog.i(TAG, "--loadImage-stop running thread-");
            threadUtils.stop();
            threadUtils.clearAll();
            threadUtils = null;
        }
        threadUtils = new ThreadUtils() {
            @Override
            public void execute() {
                MLog.i(TAG, "--loadImage-start-");

                //获取图片数据
                if (!isRunning()){ return; }
                getImageInfos();

                //获取缩略图数据
                if (!isRunning()){ return; }
                getImageThumbInfos();

                //按文件夹分类
                if (!isRunning()){ return; }
                sortByFolder();

//                printList();
            }

            @Override
            public void finish() {
                super.finish();
                if (mHandler != null)
                    mHandler.sendEmptyMessage(1);

                if (deleteInvalidThumb && cacheThumbPath != null){
                    deleteInvalidThumb = false;
                    deleteInvalidThumb();
                }
                if (mHandler != null)
                    mHandler.sendEmptyMessage(2);
            }
        };
        threadUtils.start();
    }

    private static synchronized void getImageInfos(){
        Cursor cursor = null;
        ImageInfo imageInfo = null;
        cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, null, null, " "+ Media._ID+" desc ");

        if (cursor != null){
            synchronized (imageInfos) {
                imageInfos.clear();
                while (cursor.moveToNext()){
                    imageInfo = new ImageInfo();

                    imageInfo.set_id(cursor.getInt(cursor.getColumnIndex(Media._ID)));
                    imageInfo.setImage_id(imageInfo.get_id());
                    imageInfo.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
                    imageInfo.setName(cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME)));
                    imageInfo.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));

                    imageInfo.setDate_taken(cursor.getLong(cursor.getColumnIndex(Media.DATE_TAKEN)));
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
            }
            cursor.close();
        }
        cursor = null;
    }

    /**
     * 获取缩略图信息
     */
    private static synchronized void getImageThumbInfos() {
        //判断thumb_path是否为null
        //
        if (imageInfos!=null && !imageInfos.isEmpty()){
            synchronized (imageInfos){
                if (deleteInvalidThumb){
                    if (tempThumbPathList == null) {
                        tempThumbPathList = new ArrayList<>();
                    }
                    tempThumbPathList.clear();
                }
                for (ImageInfo imageInfo : imageInfos) {
                    if (imageInfo != null && StringUtils.isNullOrEmpty(imageInfo.getThumb_path())){
                        if (cacheThumbPath == null){
//                            MLog.i(TAG, "--Thumb_path-use system thumb-");
                            getImageThumbInfo(imageInfo);

                        }else{
                            //从自定义缩略图目录获取
                            String thumbName = StringUtils.getMD5(imageInfo.getPath()) + thumbSuffix;
                            File file = new File(cacheThumbPath, thumbName);

                            imageInfo.setThumb_id(-1);
                            imageInfo.setThumb_kind(Thumbnails.MINI_KIND);
                            if (!file.exists()){
                                //创建缩略图
                                Bitmap thumb = null;
//                                thumb = getImageThumbnail(imageInfo);
                                thumb = getImageThumbnail(imageInfo.getPath(), Thumbnails.MINI_KIND);
                                if (thumb != null && !thumb.isRecycled()){
                                    imageInfo.setThumb_width(thumb.getWidth());
                                    imageInfo.setThumb_height(thumb.getHeight());
                                }

                                //保存缩略图
                                if (!FileUtils.write2SD(thumb, file.getAbsolutePath(), true)){
                                    continue;
                                }
                                MLog.i(TAG, "--create--Thumb_path:"+file.getAbsolutePath());
                            }else{
//                                MLog.i(TAG, "Thumb_path:"+file.getAbsolutePath());

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                                options.inJustDecodeBounds = false;

                                imageInfo.setThumb_width(options.outWidth);
                                imageInfo.setThumb_height(options.outHeight);
                            }
                            imageInfo.setThumb_path(file.getAbsolutePath());
                            if (deleteInvalidThumb){
//                                MLog.i(TAG, "file name:"+file.getName());
                                tempThumbPathList.add(file.getName());
                            }
                            file = null;
                        }
                    }
                }
            }
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

    /**
     * 获取图片缩略图
     * @param imagePath 原始图片目录
     * @param kind 缩略图类型
     * @return
     */
    public static Bitmap getImageThumbnail(String imagePath, int kind) {
        //64 48
        //128 96
        //192 144
        //256 192 压缩到一半
        //512 384 压缩到四分之一
        //768 576 八分之一
        //1024 768 十六分之一
        if (kind == Thumbnails.MINI_KIND){//1
            //512 x 384
            return getImageThumbnail(imagePath, 192, 144, true);//512/4, 384/4);
        }else if (kind == Thumbnails.MICRO_KIND){//3
            //96 x 96
            return getImageThumbnail(imagePath, 96, 96, false);
        }
        return null;
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     *        用这个工具生成的图像不会被拉伸。
     * @param imagePath 图像的路径
     * @param width 指定输出图像的宽度
     * @param height 指定输出图像的高度
     * @param changeSize 是否改变输出大小
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height, boolean changeSize) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int w = options.outWidth;
        int h = options.outHeight;

        int ratio = 1;
        if (w > 0 && h > 0){
            int scaleWidth = w / width;
            int ramainW = w % width;
            int scaleHeight = h / height;
            int ramainH = h % height;

//            MLog.i(TAG, "scaleWidth:"+scaleWidth+", ramainW:"+ramainW+", scaleHeight:"+scaleHeight+", ramainH:"+ramainH);
            if (scaleWidth < scaleHeight) {
                ratio = scaleWidth;// + (ramainW > 0 ? 1 : 0);
            } else {
                ratio = scaleHeight;// + (ramainH > 0 ? 1 : 0);
            }
            if (ratio < 1) {
                ratio = 1;
            }                       //0 1 2 4 8
            if (ratio > 1){         //1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
                int exp = ratio / 2;//0 1 2 2 3 3 3 3 4 4  4  4
                if (ratio % 2 > 0){
                    exp += 1;
                }
                if (exp > 4){//最小十六分之一
                    exp = 4;
                }
                int ratio1 = (int) Math.pow(2, exp-1);
                int ratio2 = (int) Math.pow(2, exp);
                if (ratio > ratio1 && ratio <= ratio2){
                    ratio = ratio2;
                }else{
                    ratio = ratio1;
                }
            }
            if (changeSize){
                width = w / ratio;
                height = h / ratio;
            }else{
                if (ratio > 4){
                    ratio = 4;
                }
            }
//            MLog.i(TAG, "w:"+w+", h:"+h+", ratio:"+ratio+", width:"+width+", height:"+height);
        }
        options.inSampleSize = ratio;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
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
            synchronized (folderInfos){
                folderInfos = sortMapByKey(folderInfos);
            }

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

    /**
     * 删除无效的缩略图
     */
    private static void deleteInvalidThumb() {
//        MLog.i(TAG, "delete 111");
        if (tempThumbPathList != null && !tempThumbPathList.isEmpty()){
//            MLog.i(TAG, "delete 222 size:"+tempThumbPathList.size());
            File directory = new File(cacheThumbPath);
            String[] files =  directory.list();

            if (files != null && files.length > 0){
                List<String> filesList = new ArrayList<>( Arrays.asList(files) );
//                MLog.i(TAG, "delete 333 filesList size:"+filesList.size());

                if (filesList != null && !filesList.isEmpty() && filesList.removeAll(tempThumbPathList)){
//                    MLog.i(TAG, "delete 444 filesList size:"+filesList.size());
                    for (String fileName : filesList) {
                        FileUtils.deleteSDFile(directory.getAbsolutePath() +"/"+ fileName);
                    }
                    filesList.clear();
                }
                filesList = null;
            }
            tempThumbPathList.clear();
        }
        tempThumbPathList = null;
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
     * @param imageInfo 原图信息
     * @return
     */
    public static Bitmap getImageThumbnail(ImageInfo imageInfo){
        imageInfo.setThumb_id(-1);
        imageInfo.setThumb_kind(Thumbnails.MINI_KIND);
        Bitmap bitmap = getImageThumbnail(imageInfo.getImage_id());
        if (bitmap != null && !bitmap.isRecycled()){
            imageInfo.setThumb_width(bitmap.getWidth());
            imageInfo.setThumb_height(bitmap.getHeight());
        }
        return bitmap;
    }

    /**
     * 获取缩略图
     * @param id 原图在ContentProvider中的id
     * @return
     */
    public static Bitmap getImageThumbnail(int id){
        Bitmap bitmap = null;
        if (mContext != null)
            bitmap = Thumbnails.getThumbnail(mContext.getContentResolver(), id, Thumbnails.MINI_KIND, null);
        return bitmap;
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

                MLog.i(TAG, imageInfo.toString());
            }
            cursor.close();
        }
        cursor = null;
    }

    /**
     * 保存图片
     * @param bitmap
     * @param path 目录必须以‘/’结尾，不包括图片名
     * @param name 图片名
     * @return
     */
    @TargetApi(12)
    public static boolean saveImage(Bitmap bitmap, String path, String name){
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setName(name);
        imageInfo.setPath(path + name);
        imageInfo.setWidth(bitmap.getWidth());
        imageInfo.setHeight(bitmap.getHeight());
        imageInfo.setSize(bitmap.getByteCount());
        boolean success = FileUtils.write2SD(bitmap, imageInfo.getPath(), false);
        if (success){
            success = insertToContentProvider(imageInfo);
        }
        return success;
    }

    /**
     * 删除图片并刷新数据库
     * @param imageInfo
     * @return
     */
    public static boolean deleteImage(ImageInfo imageInfo){
        boolean success = false;
        if (imageInfo == null){
            return success;
        }
        success = FileUtils.deleteSDFile(imageInfo.getPath());
        if (success){
            if (mContext != null){
                ContentResolver resolver = mContext.getContentResolver();
                int result = resolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID+"=?", new String[]{""+imageInfo.getImage_id()});
//                MLog.i(TAG, "delete image-->result:"+result);
                if (result == 1){
                    success = true;
                }
            }
        }
        return success;
    }

    /**
     * ImageInfo要包含名称、路径、大小
     * @param imageInfo
     * @return
     */
    public static boolean insertToContentProvider(ImageInfo imageInfo){
        if (imageInfo == null) {
            return false;
        }
        long dateTaken = System.currentTimeMillis();
        imageInfo.setDate_taken(dateTaken);
        imageInfo.setDate_added(dateTaken / 1000);
        imageInfo.setDate_modified(dateTaken / 1000);
        imageInfo.setOrientation(getImageRotation(imageInfo.getPath()));

        ContentValues values = new ContentValues();
        values.put(Media.DISPLAY_NAME, imageInfo.getName());//文件名;
        values.put(Media.DATA, imageInfo.getPath());//路径;
        values.put(Media.DATE_TAKEN, imageInfo.getDate_taken());//时间;
        values.put(Media.DATE_ADDED, imageInfo.getDate_added());//时间;
        values.put(Media.DATE_MODIFIED, imageInfo.getDate_modified());//时间;

        values.put(Media.WIDTH, imageInfo.getWidth());
        values.put(Media.HEIGHT, imageInfo.getHeight());
        values.put(Media.ORIENTATION, imageInfo.getOrientation());//角度;
        values.put(Media.SIZE, imageInfo.getSize());//图片的大小;

        Uri data = null;
        ContentResolver resolver = mContext.getContentResolver();
        try {
            if (resolver != null) {
                data = resolver.insert(Media.EXTERNAL_CONTENT_URI, values);
//                MLog.i(TAG,"insert into content provider");
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (mContext != null) {
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
        }
        return true;
    }

    /**
     * 插入到数据库
     * @param imagePath
     * @return
     */
    public static Uri insertToSystemDB(Context context, String imagePath) {
        File file = new File(imagePath);
        if (!file.exists()) {
            return null;
        }
        long dateTaken = System.currentTimeMillis();
        int degree = getImageRotation(imagePath);
        long size = file.length();
        String fileName = file.getName();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);//时间;
        values.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken / 1000);//时间;
        values.put(MediaStore.Images.Media.DATE_ADDED, dateTaken / 1000);//时间;
        values.put(MediaStore.Images.ImageColumns.DATA, imagePath);//路径;
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);//文件名;
        values.put(MediaStore.Images.Media.ORIENTATION, degree);//角度;
        values.put(MediaStore.Images.Media.SIZE, size);//图片的大小;

        Uri uri = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            if (resolver != null) {
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                MLog.i(TAG,"insert into content provider");
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return uri;
    }

    /**
     * 获取图片旋转角度
     * @param image
     * @return
     */
    public static int getImageRotation(String image) {
        if (image == null)
            return 0;
        try {
            ExifInterface exif = new ExifInterface(image);
            String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (orientation != null && orientation.length() > 0) {
                int ori = Integer.parseInt(orientation);
                switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
