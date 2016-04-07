package com.qing.image;

/**
 * Created by zwq on 2015/10/09 17:36.<br/><br/>
 * 图片(缩略图)信息
 */
public class ImageInfo {

    private static final String TAG = ImageInfo.class.getName();
    private int _id;
    private int imageId;
    private String title;
    private String name;
    private String path;
    private long dateTaken;
    private long dateAdded;
    private long dateModified;
    private int width;
    private int height;
    private int size;
    private int orientation;
    private int folderId;//文件夹id
    private String folderName;//文件夹名称

    private int thumbId;
    private String thumbPath;//缩略图目录
    private int thumbWidth;
    private int thumbHeight;
    private int thumbKind;//缩略图类型

    public ImageInfo() {

    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public int getThumbId() {
        return thumbId;
    }

    public void setThumbId(int thumbId) {
        this.thumbId = thumbId;
    }

    public int getThumbKind() {
        return thumbKind;
    }

    public void setThumbKind(int thumbKind) {
        this.thumbKind = thumbKind;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "_id=" + _id +
                ", imageId=" + imageId +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", dateTaken=" + dateTaken +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", width=" + width +
                ", height=" + height +
                ", size=" + size +
                ", orientation=" + orientation +
                ", folderId=" + folderId +
                ", folderName='" + folderName + '\'' +
                ", thumbId=" + thumbId +
                ", thumbPath='" + thumbPath + '\'' +
                ", thumbWidth=" + thumbWidth +
                ", thumbHeight=" + thumbHeight +
                ", thumbKind=" + thumbKind +
                '}';
    }

    public String toBeautyString() {
        return "ImageInfo{" +
                "\n imageId=" + imageId +
                ",\n title='" + title + '\'' +
                ",\n name='" + name + '\'' +
                ",\n path='" + path + '\'' +
                ",\n dateTaken=" + dateTaken +
                ",\n dateAdded=" + dateAdded +
                ",\n dateModified=" + dateModified +
                ",\n width=" + width +
                ",\n height=" + height +
                ",\n size=" + size +
                ",\n orientation=" + orientation +
                ",\n folderId=" + folderId +
                ",\n folderName='" + folderName + '\'' +
                ",\n thumbId=" + thumbId +
                ",\n thumbPath='" + thumbPath + '\'' +
                ",\n thumbWidth=" + thumbWidth +
                ",\n thumbHeight=" + thumbHeight +
                ",\n thumbKind=" + thumbKind +
                "\n}";
    }
}
