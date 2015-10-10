package com.qing.image;

/**
 * Created by zwq on 2015/10/09 17:36.<br/><br/>
 * 图片(缩略图)信息
 */
public class ImageInfo {

    private static final String TAG = ImageInfo.class.getName();
    private int _id;
    private int image_id;
    private String title;
    private String name;
    private String path;
    private long date_added;
    private long date_modified;
    private int width;
    private int height;
    private int size;
    private int orientation;
    private int folder_id;//文件夹id
    private String folder_name;//文件夹名称

    private int thumb_id;
    private String thumb_path;//缩略图目录
    private int thumb_width;
    private int thumb_height;
    private int thumb_kind;//缩略图类型

    public ImageInfo() {

    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getDate_added() {
        return date_added;
    }

    public void setDate_added(long date_added) {
        this.date_added = date_added;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(long date_modified) {
        this.date_modified = date_modified;
    }

    public int getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(int folder_id) {
        this.folder_id = folder_id;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public int getThumb_kind() {
        return thumb_kind;
    }

    public void setThumb_kind(int thumb_kind) {
        this.thumb_kind = thumb_kind;
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

    public int getThumb_height() {
        return thumb_height;
    }

    public void setThumb_height(int thumb_height) {
        this.thumb_height = thumb_height;
    }

    public int getThumb_id() {
        return thumb_id;
    }

    public void setThumb_id(int thumb_id) {
        this.thumb_id = thumb_id;
    }

    public String getThumb_path() {
        return thumb_path;
    }

    public void setThumb_path(String thumb_path) {
        this.thumb_path = thumb_path;
    }

    public int getThumb_width() {
        return thumb_width;
    }

    public void setThumb_width(int thumb_width) {
        this.thumb_width = thumb_width;
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
                ", image_id=" + image_id +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", date_added=" + date_added +
                ", date_modified=" + date_modified +
                ", width=" + width +
                ", height=" + height +
                ", size=" + size +
                ", orientation=" + orientation +
                ", folder_id=" + folder_id +
                ", folder_name='" + folder_name + '\'' +
                ", thumb_id=" + thumb_id +
                ", thumb_path='" + thumb_path + '\'' +
                ", thumb_width=" + thumb_width +
                ", thumb_height=" + thumb_height +
                ", thumb_kind=" + thumb_kind +
                '}';
    }

    public String toBeautyString() {
        return "ImageInfo{" +
                "\n image_id=" + image_id +
                ",\n title='" + title + '\'' +
                ",\n name='" + name + '\'' +
                ",\n path='" + path + '\'' +
                ",\n date_added=" + date_added +
                ",\n date_modified=" + date_modified +
                ",\n width=" + width +
                ",\n height=" + height +
                ",\n size=" + size +
                ",\n orientation=" + orientation +
                ",\n folder_id=" + folder_id +
                ",\n folder_name='" + folder_name + '\'' +
                ",\n thumb_id=" + thumb_id +
                ",\n thumb_path='" + thumb_path + '\'' +
                ",\n thumb_width=" + thumb_width +
                ",\n thumb_height=" + thumb_height +
                ",\n thumb_kind=" + thumb_kind +
                "\n}";
    }
}
