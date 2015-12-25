package com.qing.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.qing.utils.FileUtil;
import com.qing.utils.StringUtil;
import com.qing.utils.UIUtil;

import java.util.List;

/**
 * Created by zwq on 2015/10/10 15:16.<br/><br/>
 */
public class ImageListAdapter extends BaseAdapter {

    private static final String TAG = ImageListAdapter.class.getName();

    private Context mContext;
    private List<ImageInfo> mImageInfos;
    private int mItemWidth;

    public ImageListAdapter(Context context){
        mContext = context;
        setItemWidth(4, 3);
    }

    public List<ImageInfo> getImageInfos() {
        return mImageInfos;
    }

    public void setImageInfos(List<ImageInfo> mImageInfos) {
        this.mImageInfos = mImageInfos;
        notifyDataSetChanged();
    }

    public void setItemWidth(int numColumns, int spacing) {
        mItemWidth = (UIUtil.getScreenW()-(numColumns+1)* UIUtil.getRealPixel720(spacing))/numColumns;
    }

    @Override
    public int getCount() {
        return mImageInfos==null?0:mImageInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageItem imageItem = null;
        if (convertView == null){
            imageItem = new ImageItem(mContext);
            imageItem.setLayoutParams(new AbsListView.LayoutParams(mItemWidth, mItemWidth));
            convertView = imageItem;
            convertView.setTag(imageItem);
        }else{
            imageItem = (ImageItem) convertView.getTag();
        }

        Bitmap bitmap = null;
        if (mImageInfos != null && position < mImageInfos.size()){
            ImageInfo imageInfo = mImageInfos.get(position);
            if (imageInfo != null){
                if (StringUtil.isNullOrEmpty(imageInfo.getThumb_path())){
                    bitmap = ImageStore.getImageThumbnail(imageInfo);
                }else{
                    bitmap = FileUtil.getSDBitmap(imageInfo.getThumb_path());
                }
            }
        }
        imageItem.image.setImageBitmap(bitmap);
        return convertView;
    }
}
