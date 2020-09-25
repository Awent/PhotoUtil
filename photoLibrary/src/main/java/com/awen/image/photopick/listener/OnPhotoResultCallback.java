package com.awen.image.photopick.listener;

import androidx.annotation.NonNull;

import com.awen.image.photopick.bean.PhotoResultBean;

/**
 * 图库选图后的回调
 */
public interface OnPhotoResultCallback {

    /**
     * 图库选择图片、视频、相机拍照、相机视频录制，都会回调此方法
     * @param result
     */
    void onResult(@NonNull PhotoResultBean result);
}
