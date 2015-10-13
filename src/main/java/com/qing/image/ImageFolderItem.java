package com.qing.image;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qing.utils.UIUtils;

/**
 * Created by zwq on 2015/10/10 17:52.<br/><br/>
 */
public class ImageFolderItem extends RelativeLayout {

    public ImageFolderItem(Context context) {
        super(context);
        initView();
    }

    private final int ID_PREVIEW = 0x1;
    private final int ID_NAME = 0x2;
    public ImageView image;
    public TextView name;
    public TextView des;
    private int margin = UIUtils.getRealPixel720(15);
    private void initView() {
        LayoutParams rParams = new LayoutParams(
                UIUtils.getRealPixel720(188), UIUtils.getRealPixel720(188));
//            rParams.setMargins(margin, margin, margin, margin);
        RelativeLayout preivewLayout = new RelativeLayout(getContext());
        preivewLayout.setId(ID_PREVIEW);
        preivewLayout.setPadding(margin, margin, margin, margin);
        addView(preivewLayout, rParams);

        rParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        image = new ImageView(getContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preivewLayout.addView(image, rParams);

        rParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.RIGHT_OF, ID_PREVIEW);
        rParams.addRule(RelativeLayout.ALIGN_TOP, ID_PREVIEW);
        rParams.addRule(RelativeLayout.ALIGN_BOTTOM, ID_PREVIEW);
        rParams.rightMargin = margin;
        name = new TextView(getContext());
        name.setId(ID_NAME);
        name.setTextSize(18);
        name.setMaxEms(10);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setGravity(Gravity.CENTER_VERTICAL);
        addView(name, rParams);

        rParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.RIGHT_OF, ID_NAME);
        rParams.addRule(RelativeLayout.ALIGN_TOP, ID_NAME);
        rParams.addRule(RelativeLayout.ALIGN_BOTTOM, ID_NAME);
        des = new TextView(getContext());
        des.setTextSize(16);
        des.setGravity(Gravity.CENTER_VERTICAL);
        addView(des, rParams);
    }
}
