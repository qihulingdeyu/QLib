package com.qing.image;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by zwq on 2015/10/10 17:52.<br/><br/>
 */
class ImageItem extends RelativeLayout {

    public ImageItem(Context context) {
        super(context);
        initView();
    }

    public ImageView image;
    private void initView() {
        LayoutParams rParams = new LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//                UIUtil.getScreenW()/4, UIUtil.getScreenW()/4);
        image = new ImageView(getContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(image, rParams);
    }
}
