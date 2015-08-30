package com.qing.test;


/**
 * Author: zwq <br/>
 * Date: 2015-7-22 <br/>
 * Time: 下午3:50:54 <br/>
 */
public class ItemInfo {

    private int id;
    private String name;
    private String value;

    public ItemInfo(int id, String name, String value) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    private DownloadTask downloadTask;
    public DownloadTask getDownloadTask() {
        if(downloadTask==null){
            downloadTask = new DownloadTask(this);
        }
        return downloadTask;
    }
    
    /**
     * 数据变化监听
     */
    public interface onDataChangeListener{
        void onChange(ItemInfo info);
    }
    
    private onDataChangeListener listener;
    public onDataChangeListener getChangeListener() {
        return listener;
    }
    public void setDataChangeListener(onDataChangeListener listener) {
        this.listener = listener;
    }


}
