package com.awen.image.photopick.listener;

import androidx.annotation.Nullable;

/**
 * 保存图片到本地图库的回调
 */
public interface OnPhotoSaveCallback {

    /**
     * 成功则返回路径，失败返回null
     *
     * @param localFilePath
     */
    void onSaveImageResult(@Nullable String localFilePath);
}
