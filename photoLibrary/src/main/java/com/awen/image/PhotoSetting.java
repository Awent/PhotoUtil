package com.awen.image;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

/**
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoSetting {

    protected PhotoSetting() {
    }

    @ColorRes
    private static int toolbarBackGround;
    private static Context mContext;
    public static boolean DEBUG;
    /**
     * 保存图片到本地的地址
     */
    private static String path;

    public static void init(Context context) {
        init(context, android.R.color.holo_red_light);
    }

    public static void init(Context context, @ColorRes int toolbarBackGroundId) {
        init(context, toolbarBackGroundId, null);
    }

    /**
     * @param context
     * @param toolbarBackGroundId
     * @param saveImageLocalPath  保存图片的路径地址，可以不设置
     */
    public static void init(Context context, @ColorRes int toolbarBackGroundId, String saveImageLocalPath) {
        if (mContext != null) {//说明已经初始化过了,不用重复初始化
            return;
        }
        toolbarBackGround = toolbarBackGroundId;
        mContext = context.getApplicationContext();
        path = saveImageLocalPath;
    }

    public static void destroy() {
        mContext = null;
        path = null;
    }

    @ColorInt
    public static int getToolbarBackGround() {
        return mContext.getResources().getColor(toolbarBackGround);
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getSaveImageLocalPath() {
        return path;
    }

    /**
     * 设置toolbar颜色
     *
     * @param toolbarBackGroundId resId
     */
    public static void setToolbarBackGround(@ColorRes int toolbarBackGroundId) {
        toolbarBackGround = toolbarBackGroundId;
    }

    /**
     * 查看网络大图时，你想把图片保存的地址,保存的图片是可以在手机图库可以看到的
     *
     * @param saveImageLocalPath 大图存储的地址
     */
    public static void setSaveImageLocalPath(String saveImageLocalPath) {
        path = saveImageLocalPath;
    }

    public static void setDEBUG(boolean DEBUG) {
        PhotoSetting.DEBUG = DEBUG;
    }
}
