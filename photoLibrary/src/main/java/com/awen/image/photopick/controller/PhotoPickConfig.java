package com.awen.image.photopick.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.ColorRes;

import com.awen.image.PhotoSetting;
import com.awen.image.R;
import com.awen.image.photopick.bean.PhotoPickBean;
import com.awen.image.photopick.bean.PhotoResultBean;
import com.awen.image.photopick.loader.MediaType;
import com.awen.image.photopick.ui.PhotoPickActivity;

/**
 * 使用方法,hao to use：<br><code>
 * new PickConfig.Builder(this)<br>
 * .pickMode(PickConfig.MODE_MULTIP_PICK)<br>
 * .maxPickSize(30)<br>
 * .spanCount(3)<br>
 * .showCamera(false) //default true<br>
 * .clipPhoto(true)   //default false<br>
 * .setOriginalPicture(true)//default false<br>
 * .showGif(true)//default true<br>
 * .build();<br>
 * </code>
 * Created by Awen <Awentljs@gmail.com>
 */
public class PhotoPickConfig {

    public static int MODE_SINGLE_PICK = 1;//default 单选模式

    public static int MODE_MULTIP_PICK = 2;//多选

    private static int DEFAULT_SPANCOUNT = 3;//gridview列数

    private static boolean DEFAULT_START_CLIP = false;//默认不开启图片裁剪

    public final static String EXTRA_STRING_ARRAYLIST = "extra_string_array_list";
    public final static String EXTRA_PICK_BUNDLE = "extra_pick_bundle";
    public final static String EXTRA_PICK_BEAN = "extra_pick_bean";
    public final static int PICK_REQUEST_CODE = 10507;

    /**
     * 关于数据的回传，新增了{@link Builder#setOnPhotoResultCallback(Builder.OnPhotoResultCallback)}接口,可实现此接口获取回传数据，当然Activity的回传方式也可用，两种方式可同时获取到数据
     * 使用方法,hao to use：<br><code>
     * new PickConfig.Builder(this)<br>
     * .pickMode(PickConfig.MODE_MULTIP_PICK)<br>
     * .maxPickSize(30)<br>
     * .spanCount(3)<br>
     * .showCamera(false) //default true<br>
     * .clipPhoto(true)   //default false<br>
     * .setOriginalPicture(true)//default false<br>
     * .showGif(true)//default true<br>
     * .setOnPhotoResultCallback(OnPhotoResultCallback onPhotoResultCallback)
     * .build();<br>
     * </code>
     * Created by Awen <Awentljs@gmail.com>
     */
    private PhotoPickConfig(Activity context, PhotoPickConfig.Builder builder) {
        if (builder.pickBean == null) {
            throw new NullPointerException("builder#pickBean is null");
        }
        PhotoPickActivity.setOnPhotoResultCallback(builder.pickBean.getOnPhotoResultCallback());
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PICK_BEAN, builder.pickBean);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PICK_BUNDLE, bundle);
        intent.setClass(context, PhotoPickActivity.class);
        context.startActivityForResult(intent, PICK_REQUEST_CODE);
        context.overridePendingTransition(R.anim.bottom_in, 0);
    }

    public static class Builder {
        private Activity context;
        private PhotoPickBean pickBean;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            if (!(context instanceof Activity)) {
                throw new NullPointerException("context must is activity");
            }
            PhotoSetting.init(context);
            this.context = (Activity) context;
            pickBean = new PhotoPickBean();
            pickBean.setSpanCount(DEFAULT_SPANCOUNT);//列数
            pickBean.setMaxPickSize(9);//默认可以选择的图片数目,9张
            pickBean.setShowCamera(true);//默认展示拍照那个icon
            pickBean.setClipPhoto(DEFAULT_START_CLIP);//默认不开启图片裁剪
            pickBean.setShowGif(true);//默认展示gif
            pickModeMulti();//默认多选
        }

        /**
         * 多选
         */
        public PhotoPickConfig.Builder pickModeMulti() {
            pickBean.setPickMode(MODE_MULTIP_PICK);
            return this;
        }

        /**
         * 单选
         */
        public PhotoPickConfig.Builder pickModeSingle() {
            pickBean.setPickMode(MODE_SINGLE_PICK);
            return this;
        }

        /**
         * gridview的列数
         *
         * @param spanCount defult value 3
         * @return
         */
        public PhotoPickConfig.Builder spanCount(int spanCount) {
            pickBean.setSpanCount(spanCount);
            if (pickBean.getSpanCount() == 0) {
                pickBean.setSpanCount(DEFAULT_SPANCOUNT);
            }
            return this;
        }

        /**
         * 单选还是多选
         *
         * @param pickMode {@link PhotoPickConfig#MODE_SINGLE_PICK} <br> {@link PhotoPickConfig#MODE_MULTIP_PICK}
         * @return
         */
        public PhotoPickConfig.Builder pickMode(int pickMode) {
            pickBean.setPickMode(pickMode);
            if (pickMode == MODE_SINGLE_PICK) {
                pickBean.setMaxPickSize(1);
            } else if (pickMode == MODE_MULTIP_PICK) {
                pickBean.setClipPhoto(false);
            } else {
                throw new IllegalArgumentException("unkonw pickMod : " + pickMode);
            }
            return this;
        }

        /**
         * 最多可以选择的图片数目
         *
         * @param maxPickSize default value is 9
         * @return
         */
        public PhotoPickConfig.Builder maxPickSize(int maxPickSize) {
            pickBean.setMaxPickSize(maxPickSize);
            if (maxPickSize == 0) {
                pickBean.setMaxPickSize(1);
                pickBean.setPickMode(MODE_SINGLE_PICK);
            } else if (pickBean.getPickMode() == MODE_SINGLE_PICK) {
                pickBean.setMaxPickSize(1);
                pickBean.setClipPhoto(DEFAULT_START_CLIP);
            }
            return this;
        }

        /**
         * 是否展示拍照那个icon
         *
         * @param showCamera default true
         * @return
         */
        public PhotoPickConfig.Builder showCamera(boolean showCamera) {
            pickBean.setShowCamera(showCamera);
            return this;
        }

        /**
         * 是否开启选择完图片之后进行图片裁剪<br>
         * 如果传true，pickMode会自动设置为单选
         *
         * @return
         */
        public PhotoPickConfig.Builder clipPhoto() {
            onlyImage();//裁剪头像只显示图片，不显示视频
            pickBean.setClipPhoto(true);
            return this;
        }

        /**
         * 是否开启选择完图片之后进行图片裁剪，这里调用的是系统裁剪功能<br>
         * 如果传true，pickMode会自动设置为单选
         *
         * @return
         */
        public PhotoPickConfig.Builder clipPhotoWithSystem() {
            onlyImage();//裁剪头像只显示图片，不显示视频
            pickBean.setClipPhoto(true);
            pickBean.setSystemClipPhoto(true);
            return this;
        }

        /**
         * 是否让用户选择原图
         *
         * @param originalPicture default false
         * @return
         */
        public PhotoPickConfig.Builder setOriginalPicture(boolean originalPicture) {
            //是否设置原图,默认false
            this.pickBean.setOriginalPicture(originalPicture);
            return this;
        }

        /**
         * 是否展示gif图片<br>
         *
         * @param showGif default true
         * @return
         */
        public PhotoPickConfig.Builder showGif(boolean showGif) {
            pickBean.setShowGif(showGif);
            return this;
        }

        /**
         * 只显示图片,不包含视频，默认是图片跟视频都展示
         * p:如果启动了拍照，只会启动系统拍照，并返回图片路径
         * <p>
         * 如果图片跟视频都显示，只是启动拍照，而不是录制视频
         */
        public PhotoPickConfig.Builder onlyImage() {
            pickBean.setMediaType(MediaType.ONLY_IMAGE);
            return this;
        }

        /**
         * 只显示视频,不包含图片，默认是图片跟视频都展示
         * p:如果启动了拍照，只会启动系统视频录制，并返回视频路径
         * <p>
         * 如果图片跟视频都显示，只是启动拍照，而不是录制视频
         */
        public PhotoPickConfig.Builder onlyVideo() {
            pickBean.setMediaType(MediaType.ONLY_VIDEO);
            return this;
        }

        /**
         * 如果不想在Activity通过onActivityResult()获取回传的数据，可实现此接口。
         *
         * @param onPhotoResultCallback
         * @return
         */
        public PhotoPickConfig.Builder setOnPhotoResultCallback(OnPhotoResultCallback onPhotoResultCallback) {
            pickBean.setOnPhotoResultCallback(onPhotoResultCallback);
            return this;
        }

        /**
         * 设置toolbar颜色
         *
         * @param toolbarBackGroundId resId
         */
        public PhotoPickConfig.Builder setToolbarBackGround(@ColorRes int toolbarBackGroundId) {
            PhotoSetting.setToolbarBackGround(toolbarBackGroundId);
            return this;
        }

        /**
         * 查看网络大图时，你想把图片保存的地址,保存的图片是可以在手机图库可以看到的
         *
         * @param saveImageLocalPath 大图存储的地址
         */
        public PhotoPickConfig.Builder setSaveImageLocalPath(String saveImageLocalPath) {
            PhotoSetting.setSaveImageLocalPath(saveImageLocalPath);
            return this;
        }

        public PhotoPickConfig.Builder setPhotoPickBean(PhotoPickBean bean) {
            this.pickBean = bean;
            return this;
        }

        public PhotoPickConfig build() {
            if (pickBean.isClipPhoto()) {
                pickBean.setMaxPickSize(1);
                pickBean.setPickMode(MODE_SINGLE_PICK);
            }
            return new PhotoPickConfig(context, this);
        }

        public interface OnPhotoResultCallback {
            void onResult(PhotoResultBean result);
        }
    }

}
