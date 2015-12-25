package com.qing.image;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qing.utils.FileUtil;
import com.qing.utils.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zwq on 2015/10/10 16:16.<br/><br/>
 */
public class ImageFolderListAdapter extends BaseAdapter {

    private static final String TAG = ImageFolderListAdapter.class.getName();
    private Context mContext;
    private Map<String, List<ImageInfo>> mFolderList;

    public ImageFolderListAdapter(Context context){
        mContext = context;
    }

    public Map<String, List<ImageInfo>> getFolderList() {
        return mFolderList;
    }

    public void setFolderList(Map<String, List<ImageInfo>> folderList) {
        this.mFolderList = folderList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolderList==null?0:mFolderList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFolderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageFolderItem folderItem = null;
        if (convertView == null){
            folderItem = new ImageFolderItem(mContext);

            convertView = folderItem;
            convertView.setTag(folderItem);
        }else{
            folderItem = (ImageFolderItem) convertView.getTag();
        }
        if (mFolderList != null){
            List<ImageInfo> imageInfoList = mFolderList.get(getImageFolderName(position));
            if (imageInfoList != null && !imageInfoList.isEmpty()){
                ImageInfo imageInfo = imageInfoList.get(0);
                if (imageInfo != null){
                    if (StringUtil.isNullOrEmpty(imageInfo.getThumb_path())){
                        folderItem.image.setImageBitmap(ImageStore.getImageThumbnail(imageInfo.getImage_id()));
                    }else{
                        folderItem.image.setImageBitmap(FileUtil.getSDBitmap(imageInfo.getThumb_path()));
                    }
                    folderItem.name.setText(""+imageInfo.getFolder_name());
                    folderItem.des.setText("("+imageInfoList.size()+")");
                }
            }
        }
        return convertView;
    }

    public String getImageFolderName(int position){
        if (mFolderList != null){
            int i = 0;
            for (Map.Entry<String, List<ImageInfo>> entry : mFolderList.entrySet()) {
                if (i == position){
                    return entry.getKey();
                }
                i++;
            }
        }
        return null;
    }
}
