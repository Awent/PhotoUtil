package com.awen.image.photopick.controller;

import androidx.fragment.app.FragmentActivity;

import com.awen.image.PhotoSetting;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoResultBean;
import com.awen.image.photopick.listener.OnPhotoResultCallback;
import com.awen.image.photopick.loader.MediaType;
import com.awen.image.photopick.ui.CameraProxy;

import java.util.ArrayList;

/**
 * 单独启动系统相机
 * 功能：
 * 1、拍照
 * 2、拍照后进行裁剪
 * 3、视频录制
 *
 * 1、拍照
 * {@code
 *                PhotoUtil.camera(this)
 *                         .setOnPhotoResultCallback(new OnPhotoResultCallback() {
 *                             @Override
 *                             public void onResult(@NonNull PhotoResultBean result) {
 *                                 startAc(result.getList());
 *                             }
 *                         })
 *                         .build();
 * }
 *
 * 2、拍照后进行裁剪：
 * {@code
 *
 *                PhotoUtil.camera(this)
 * //                      .clipPhoto()
 *                         .clipPhotoWithSystem() //系统裁剪
 *                         .setAspectX(1) //裁剪框 X 比值
 *                         .setAspectY(1) //裁剪框 Y 比值
 *                         .setOutputX(400) //裁剪后输出宽度
 *                         .setOutputY(400) //裁剪后输出高度
 *                         .setOnPhotoResultCallback(new OnPhotoResultCallback() {
 *                             @Override
 *                             public void onResult(@NonNull PhotoResultBean result) {
 *                                 startAc(result.getList());
 *                             }
 *                         })
 *                         .build();
 * }
 *
 *
 * 3、视频录制
 * {@code
 *                PhotoUtil.camera(this)
 *                         .takeVideo()
 * //                      .setVideoMaxSize(10 * 1024 * 1024)//10M
 *                         .setVideoDuration(10)//10秒,很多国产机中这个参数无效
 *                         .setOnPhotoResultCallback(new OnPhotoResultCallback() {
 *                             @Override
 *                             public void onResult(@NonNull PhotoResultBean result) {
 *                                 startAc(result.getList());
 *                             }
 *                         })
 *                         .build();
 * }
 */
public class CameraBuilder implements CameraProxy.OnCameraProxyCallBack {

    private CameraOptions options;
    private CameraProxy cameraProxy;
    private OnPhotoResultCallback onPhotoResultCallback;

    private CameraBuilder(FragmentActivity context) {
        PhotoSetting.init(context);
        options = new CameraOptions();
        cameraProxy = new CameraProxy(context, this, options, true);
        takeImage();
    }

    public static CameraBuilder create(FragmentActivity context) {
        return new CameraBuilder(context);
    }

    public CameraBuilder setVideoMaxSize(long videoMaxSize) {
        options.setVideoMaxSize(videoMaxSize);
        return this;
    }

    public CameraBuilder setVideoDuration(long videoDuration) {
        options.setVideoDuration(videoDuration);
        return this;
    }

    public CameraBuilder setAspectX(int aspectX) {
        options.setAspectX(aspectX);
        return this;
    }

    public CameraBuilder setAspectY(int aspectY) {
        options.setAspectY(aspectY);
        return this;
    }

    public CameraBuilder setOutputX(int outputX) {
        options.setOutputX(outputX);
        return this;
    }

    public CameraBuilder setOutputY(int outputY) {
        options.setOutputY(outputY);
        return this;
    }

    /**
     * 是否开启选择完图片之后进行图片裁剪<br>
     */
    public CameraBuilder clipPhoto() {
        options.setClipPhoto(true);
        return this;
    }

    /**
     * 是否开启选择完图片之后进行图片裁剪，这里调用的是系统裁剪功能<br>
     */
    public CameraBuilder clipPhotoWithSystem() {
        takeImage();
        clipPhoto();
        options.setSystemClipPhoto(true);
        return this;
    }

    /**
     * 拍照，默认
     * @return
     */
    public CameraBuilder takeImage() {
        options.setMediaType(MediaType.ONLY_IMAGE);
        return this;
    }

    /**
     * 视频录像
     * @return
     */
    public CameraBuilder takeVideo() {
        options.setMediaType(MediaType.ONLY_VIDEO);
        return this;
    }

    public CameraBuilder setOnPhotoResultCallback(OnPhotoResultCallback onPhotoResultCallback) {
        this.onPhotoResultCallback = onPhotoResultCallback;
        return this;
    }

    public void build() {
        if (options.getMediaType() == MediaType.ONLY_IMAGE) {
            cameraProxy.selectPicFromCamera();
        } else {
            cameraProxy.selectVideoFromCamera();
        }
    }

    @Override
    public void onPhotoResultBack(ArrayList<String> photos, ArrayList<Photo> list, boolean originalPicture) {
        PhotoResultBean bean = new PhotoResultBean();
        bean.setOriginalPicture(originalPicture);
        bean.setPhotoLists(photos);
        bean.setList(list);
        if (onPhotoResultCallback != null) {
            onPhotoResultCallback.onResult(bean);
        }
    }

    @Override
    public int getMediaType() {
        return options.getMediaType();
    }

    @Override
    public boolean isClipPhoto() {
        return options.isClipPhoto();
    }

    @Override
    public boolean isSystemClipPhoto() {
        return options.isSystemClipPhoto();
    }
}
