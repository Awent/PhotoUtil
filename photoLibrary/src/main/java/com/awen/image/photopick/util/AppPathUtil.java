package com.awen.image.photopick.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.awen.image.PhotoSetting;
import com.awen.image.R;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.Util;

import java.io.File;
import java.security.MessageDigest;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by Awen <Awentljs@gmail.com>
 */
public class AppPathUtil {

    /**
     * 裁剪头像
     *
     * @return
     */
    public static String getClipPhotoPath() {
        return getPrivatePath("clip");
    }

    /**
     * 保存大图到本地的路径地址
     *
     * @return String
     */
    public static String getBigBitmapCachePath() {
        return getPrivatePath("imageCache");
    }

    /**
     * 这个是私有目录，会随着应用卸载而删除
     *
     * @param str
     * @return
     */
    public static String getPrivatePath(String str) {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (SDKVersionUtil.isAndroid_Q()) {
                File file = PhotoSetting.getContext().getExternalFilesDir(DIRECTORY_PICTURES);
                if (file != null) {
                    path = file.getAbsolutePath();
                }
            } else {
                path = Environment.getExternalStorageDirectory().getPath();
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = PhotoSetting.getContext().getCacheDir().getPath();
        }
        path = path + File.separator + str + File.separator;
        exitesFolder(path);
        return path;
    }

    public static boolean isSdCardExit(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
    }

    private static String getPath(String str) {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        if (TextUtils.isEmpty(path)) {
            path = PhotoSetting.getContext().getCacheDir().getPath();
        }
        //地址如下:path/appname/appname_photo/
        String app_root_name = PhotoSetting.getContext().getString(R.string.app_root_name);
        path = path + File.separator + app_root_name + File.separator + app_root_name + "_" + str + File.separator;
        exitesFolder(path);
        return path;
    }

    /**
     * 判断文件夹是否存在,不存在则创建
     *
     * @param path
     */
    public static void exitesFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getFileName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".gif") && !fileName.endsWith(".webp")) {
            //防止有些图片没有后缀名
            fileName = fileName + ".jpg";
        }
        return fileName;
    }

    public static String getRandomFileName(String url) {
        String fileName = String.valueOf(System.currentTimeMillis());
        if (!url.endsWith(".jpg") && !url.endsWith(".png") && !url.endsWith(".jpeg") && !url.endsWith(".gif") && !url.endsWith(".webp")) {
            //防止有些图片没有后缀名
            fileName = fileName + ".jpg";
        } else {
            String type = url.substring(url.lastIndexOf("."));
            fileName = fileName + type;
        }

        return fileName;
    }

    /**
     * Glide缓存存储路径：/data/data/your_packagexxxxxxx/cache/image_manager_disk_cache
     * Glide文件名生成规则函数 : 4.0+ 版本
     *
     * @param url 图片地址url
     * @return 返回图片在磁盘缓存的key值
     */
    public static String getGlide4_SafeKey(String url) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            EmptySignature signature = EmptySignature.obtain();
            signature.updateDiskCacheKey(messageDigest);
            new GlideUrl(url).updateDiskCacheKey(messageDigest);
            String safeKey = Util.sha256BytesToHex(messageDigest.digest());
            return safeKey + ".0";
        } catch (Exception e) {
        }
        return null;
    }

    public static String getGlideLocalCachePath(String url) {
        String key = getGlide4_SafeKey(url);
        if (!TextUtils.isEmpty(key)) {
            @SuppressLint("SdCardPath") String path = PhotoSetting.getContext().getCacheDir().getPath() + "/image_manager_disk_cache/%s";
            return String.format(path, key);
        }
        return null;
    }

}
