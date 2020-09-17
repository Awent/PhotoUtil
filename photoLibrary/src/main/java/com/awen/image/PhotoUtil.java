package com.awen.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.awen.image.photopick.util.GlideCacheUtil;
import com.awen.image.photopick.ui.VideoPlayActivity;

/**
 * 以下四个方法可能是你想用的
 *
 * @see #setToolbarBackGround(int)
 * @see #setSaveImageLocalPath(String)
 * @see #getSDCacheSizeFormat(Context) ()
 * @see #clearCaches(Context) ()
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoUtil extends PhotoSetting {

    private PhotoUtil() {
    }


    /**
     * 清除所有缓存（包括内存+磁盘）
     */
    public static void clearCaches(Context context) {
        GlideCacheUtil.clearImageMemoryCache(context);
        GlideCacheUtil.clearImageDiskCache(context);
    }

    /**
     * 获取磁盘上缓存文件的大小,带单位，比如：20MB
     */
    public static String getSDCacheSizeFormat(Context context) {
        return GlideCacheUtil.getCacheSize(context);
    }

    /**
     * 启动一个视频播放
     *
     * @param activity
     * @param videoUrl 本地视频地址或网络视频地址
     */
    public static void startVideoActivity(Activity activity, String videoUrl) {
        activity.startActivity(new Intent(activity, VideoPlayActivity.class)
                .putExtra("videoUrl", videoUrl));
        activity.overridePendingTransition(R.anim.image_pager_enter_animation, 0);
    }

    /**
     * 启动一个视频播放
     *
     * @param activity
     * @param videoUri 本地视频地址或网络视频地址Uri
     */
    public static void startVideoActivity(Activity activity, Uri videoUri) {
        activity.startActivity(new Intent(activity, VideoPlayActivity.class)
                .putExtra("videoUrl", videoUri.toString()));
        activity.overridePendingTransition(R.anim.image_pager_enter_animation, 0);
    }
}
