package com.awen.image.photopick.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.awen.image.R;
import com.awen.image.photopick.adapter.PhotoPickAdapter;
import com.awen.image.photopick.bean.PhotoPreviewBean;
import com.awen.image.photopick.ui.PhotoPreviewActivity;

/**
 * 仿微信图片预览<br>
 * 如何使用:<br>
 * <code>
 * new PhotoPreviewConfig.Builder((Activity) context)<br>
 * .setPosition(position)<br>
 * .setMaxPickSize(maxPickSize)<br>
 * .setPhotos(photos)<br>
 * .setSelectPhotos(selectPhotos)<br>
 * .setOriginalPicture(originalPicture)<br>
 * .build();<br>
 * </code>
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoPreviewConfig {
    public static final String EXTRA_BUNDLE = "extra_bundle";
    public static final String EXTRA_BEAN = "extra_bean";
    public static final String EXTRA_ORIGINAL_PIC = "original_picture";
    public final static int REQUEST_CODE = 10504;

    private PhotoPreviewConfig(Activity activity, Builder builder) {
        PhotoPreviewBean photoPreviewBean = builder.bean;
        if (photoPreviewBean == null) {
            throw new NullPointerException("Builder#photoPagerBean is null");
        }
        if (PhotoPickAdapter.selectPhotos != null && (PhotoPickAdapter.selectPhotos.size() > photoPreviewBean.getMaxPickSize())) {
            throw new IndexOutOfBoundsException("seleced photo size out maxPickSize size,select photo size = " + PhotoPickAdapter.selectPhotos.size() + ",maxPickSize size = " + photoPreviewBean.getMaxPickSize());
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_BEAN, photoPreviewBean);
        Intent intent = new Intent(activity, PhotoPreviewActivity.class);
        intent.putExtra(EXTRA_BUNDLE, bundle);
        activity.startActivityForResult(intent, REQUEST_CODE);
        activity.overridePendingTransition(R.anim.image_pager_enter_animation, 0);
    }

    public static class Builder {
        private Activity context;
        private PhotoPreviewBean bean;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            if (!(context instanceof Activity)) {
                throw new NullPointerException("context must is activity");
            }
            this.context = (Activity) context;
            bean = new PhotoPreviewBean();
        }

        public Builder setPhotoPreviewBean(PhotoPreviewBean bean) {
            this.bean = bean;
            return this;
        }

        public Builder setPosition(int position) {
            if (position < 0) {
                position = 0;
            }
            bean.setPosition(position);
            return this;
        }

        public Builder setOriginalPicture(boolean originalPicture) {//是否设置原图,默认false
            bean.setOriginalPicture(originalPicture);
            return this;
        }

        public Builder setMaxPickSize(int maxPickSize) {
            bean.setMaxPickSize(maxPickSize);
            return this;
        }

        /**
         * 是否是预览
         */
        public Builder setPreview(boolean isPreview){
            bean.setPreview(isPreview);
            return this;
        }

        public PhotoPreviewConfig build() {
            return new PhotoPreviewConfig(context, this);
        }

    }
}
