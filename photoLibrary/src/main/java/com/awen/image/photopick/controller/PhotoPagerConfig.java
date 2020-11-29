package com.awen.image.photopick.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.awen.image.PhotoSetting;
import com.awen.image.R;
import com.awen.image.photopick.bean.PhotoPagerBean;
import com.awen.image.photopick.listener.OnItemCallBack;
import com.awen.image.photopick.listener.OnPhotoSaveCallback;
import com.awen.image.photopick.ui.PhotoPagerActivity;
import com.awen.image.photopick.util.AppPathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认不开启保存图片功能，但是会有默认的保存图片地址,默认展示第一张图片<br>
 * 可以这样使用:
 * <p>
 * {@code
 * <p>
 * PhotoUtil.browser(this, Class<T> t)
 *          .fromList(Iterable<? extends T> iterable, OnItemCallBack<T> listener)//接收集合数据并提供内循环返回所有item
 *          .fromMap(Map<?, T> map, OnItemCallBack<T> listener)                  //接收集合数据并提供内循环返回所有item
 *          .setBigImageUrls(ImageProvider.getImageUrls())      //大图片url,可以是sd卡res，asset，网络图片.
 *          .setSmallImageUrls(ArrayList<String> smallImgUrls)  //小图图片的url,用于大图展示前展示的
 *          .addSingleBigImageUrl(String bigImageUrl)           //一张一张大图add进ArrayList
 *          .addSingleSmallImageUrl(String smallImageUrl)       //一张一张小图add进ArrayList
 *          .setSaveImage(true)                                 //开启保存图片，默认false
 *          .setPosition(2)                                     //默认展示第2张图片
 *          .setSaveImageLocalPath("Android/SD/xxx/xxx")        //这里是你想保存大图片到手机的地址,可在手机图库看到，不传会有默认地址，android Q会忽略此参数
 *          .setBundle(bundle)                                  //传递自己的数据，如果数据中包含java bean，必须实现Parcelable接口
 *          .setOpenDownAnimate(false)                          //是否开启下滑关闭activity，默认开启。类似微信的图片浏览，可下滑关闭一样
 *          .setOnPhotoSaveCallback(new OnPhotoSaveCallback()   //保存网络图片到本地图库的回调,保存成功则返回本地图片路径，失败返回null
 *          .error(@DrawableRes int resourceId)                 //网络图片加载失败时显示的错误图片,不传会有默认的
 *          .build();
 * <p>
 * 自定义界面：
 * <p>
 * PhotoUtil.browserCustom(this,Class<? extends PhotoPagerActivity> clazz)       //这里传入你自定义的Activity class,自定义的activity必须继承PhotoPagerActivity
 *          ...
 *          ...
 *          ...
 *          .build();
 * <p>
 * }
 * Created by Awen <Awentljs@gmail.com>
 */
public class PhotoPagerConfig {
    public static final String EXTRA_PAGER_BUNDLE = "extra_pager_bundle";
    public static final String EXTRA_USER_BUNDLE = "extra_user_bundle";
    public static final String EXTRA_PAGER_BEAN = "extra_pager_bean";

    private PhotoPagerConfig(Activity activity, Builder<?> builder) {
        PhotoPagerBean photoPagerBean = builder.photoPagerBean;
        if (photoPagerBean == null) {
            throw new NullPointerException("Builder#photoPagerBean is null");
        }
        if (photoPagerBean.getBigImgUrls() == null || photoPagerBean.getBigImgUrls().isEmpty()) {
            throw new NullPointerException("bigImageUrls is null or size is 0");
        }
        if (photoPagerBean.getPagePosition() > photoPagerBean.getBigImgUrls().size()) {
            throw new IndexOutOfBoundsException("show position out bigImageUrls size,position = " + photoPagerBean.getPagePosition() + ",bigImageUrls size = " + photoPagerBean.getBigImgUrls().size());
        }
        PhotoPagerActivity.setOnPhotoSaveCallback(builder.photoPagerBean.getOnPhotoSaveCallback());
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PAGER_BEAN, photoPagerBean);
        Intent intent = new Intent(activity, builder.clazz);
        intent.putExtra(EXTRA_PAGER_BUNDLE, bundle);
        if (builder.bundle != null) {
            intent.putExtra(EXTRA_USER_BUNDLE, builder.bundle);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.image_pager_enter_animation, 0);
    }

    public static class Builder<T> {
        private Activity context;
        private PhotoPagerBean photoPagerBean;
        private Class<?> clazz;
        private Bundle bundle;

        public Builder(@NonNull Context context) {
            this(context, PhotoPagerActivity.class);
        }

        public Builder(@NonNull Context context, @NonNull Class<? extends PhotoPagerActivity> clazz) {
            if (!(context instanceof Activity)) {
                throw new NullPointerException("context must is activity");
            }
            PhotoSetting.init(context);
            this.clazz = clazz;
            this.context = (Activity) context;
            photoPagerBean = new PhotoPagerBean();
            photoPagerBean.setPagePosition(0);//默认展示第1张图片
            photoPagerBean.setSaveImage(false);//默认不开启保存图片到本地
            photoPagerBean.setSaveImageLocalPath(AppPathUtil.getBigBitmapCachePath());//默认保存到本地的图片地址
            photoPagerBean.setOpenDownAnimate(true);
        }

        public Builder<T> create() {
            return this;
        }

        /**
         * 为了方便开发者能快速调用查看大图功能，提供了此方法
         * <p>
         * 使用场景：
         * 图片url存在于集合实体类里，例如：Map<Integer, UserBean.User> 或 List<UserBean.User> userList
         * 这时候不需要自己循环这些集合取出图片url了，本方法会提供内部循环，你只需关注你的图片url字段就行
         * <p>
         * 使用示例：用户头像avatar存在于User实体类里面，是通过服务器返回来的
         * <pre>{@code
         * List<UserBean.User> list = getUserInfo();
         * java写法：
         *                PhotoUtil.browser(this,UserBean.User.class)
         *                         .fromList(list, new OnItemCallBack<UserBean.User>() {
         *                             @Override
         *                             public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
         *                                 builder.addSingleBigImageUrl(item.getAvatar());
         *                             }
         *                         })
         *                         .setOnPhotoSaveCallback(new OnPhotoSaveCallback() {
         *                             @Override
         *                             public void onSaveImageResult(String localFilePath) {
         *                                 Toast(localFilePath != null ? "保存成功" : "保存失败");
         *                             }
         *                         })
         *                         .build();
         *
         * kotlin写法：
         * list?.let { it ->
         *                PhotoUtil.browser(this,UserBean.User::java.class)
         *                         .fromList(it){ item, builder ->
         *                             builder.addSingleBigImageUrl(item.avatar)
         *                         }).build()
         * }</pre>
         *
         * @param iterable
         * @param listener
         * @return
         * @see #fromMap(Map, OnListItemCallBack)
         */
        public Builder<T> fromList(Iterable<? extends T> iterable, OnItemCallBack<T> listener) {
            if (iterable == null) {
                throw new NullPointerException("list must not null");
            }
            if (listener == null) {
                throw new NullPointerException("OnItemCallBack must not null");
            }
            for (T t : iterable) {
                listener.nextItem(t, this);
            }
            return this;
        }

        /**
         * 为了方便开发者能快速调用查看大图功能，提供了此方法
         * <p>
         * 使用场景：
         * 图片url存在于集合实体类里，例如：Map<Integer, UserBean.User> 或 List<UserBean.User> userList
         * 这时候不需要自己循环这些集合取出url了，本方法会提供内部循环，你只需关注你的图片url字段就行
         * <p>
         * 使用示例：用户头像avatar存在于User实体类里面，是通过服务器返回来的
         * <pre>{@code
         * Map<Integer, UserBean.User> map = new HashMap<>();
         *                PhotoUtil.browser(this,UserBean.User.class)
         *                         .fromMap(map, new OnItemCallBack<UserBean.User>() {
         *                             @Override
         *                             public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
         *                                 builder.addSingleBigImageUrl(item.getAvatar());
         *                             }
         *                         })
         *                         .setOnPhotoSaveCallback(new OnPhotoSaveCallback() {
         *                             @Override
         *                             public void onSaveImageResult(String localFilePath) {
         *                                 Toast(localFilePath != null ? "保存成功" : "保存失败");
         *                             }
         *                         })
         *                         .build();
         * }</pre>
         *
         * @param map
         * @param listener
         * @return
         * @see #fromList(Iterable, OnItemCallBack)
         */
        public Builder<T> fromMap(Map<?, T> map, OnItemCallBack<T> listener) {
            if (map == null) {
                throw new NullPointerException("map must not null");
            }
            if (listener == null) {
                throw new NullPointerException("OnItemCallBack must not null");
            }
            for (T t : map.values()) {
                listener.nextItem(t, this);
            }
            return this;
        }

        /**
         * 默认展示第一张图片
         *
         * @param position 默认展示第一张图片
         * @return Builder
         */
        public Builder<T> setPosition(int position) {
            if (position < 0) {
                position = 0;
            }
            photoPagerBean.setPagePosition(position);
            return this;
        }

        /**
         * 大图图片的url,后续可扩展更多
         *
         * @param bigImgUrls 图片的url
         * @return Builder
         */
        public Builder<T> setBigImageUrls(@NonNull List<String> bigImgUrls) {
            if (bigImgUrls.isEmpty()) {
                throw new NullPointerException("imageUrls size is 0");
            }
            photoPagerBean.setBigImgUrls((ArrayList<String>) bigImgUrls);
            return this;
        }

        /**
         * 默认不开启图片保存到本地功能
         *
         * @param saveImage true is open,false is close, default false;
         * @return Builder
         */
        public Builder<T> setSaveImage(boolean saveImage) {
            photoPagerBean.setSaveImage(saveImage);
            return this;
        }

        /**
         * 会有个默认的地址，不传也可以
         * <p>
         * 注意：targetSdkVersion>=Q，图片要保存在公共存储目录才可在图库看到（/storage/emulated/0/Pictures）
         *
         * @param saveImageLocalPath 保存图片到本地的地址，如果sdk >= 29，设置此参数将会没有意义，因为会默认保存到公共存储目录(Pictures)
         * @return Builder
         */
        public Builder<T> setSaveImageLocalPath(String saveImageLocalPath) {
            photoPagerBean.setSaveImageLocalPath(saveImageLocalPath);
            return this;
        }

        /**
         * p:更名为small,不使用low了，感觉很不好
         *
         * @param lowImgUrls 小图图片的url
         * @return Builder
         * @deprecated use {@link #setSmallImageUrls(List)}
         */
        public Builder<T> setLowImageUrls(@NonNull List<String> lowImgUrls) {
            setSmallImageUrls(lowImgUrls);
            return this;
        }

        /**
         * 小图图片的url,后续可扩展更多
         *
         * @param smallImgUrls 小图图片的url
         * @return Builder
         */
        public Builder<T> setSmallImageUrls(@NonNull List<String> smallImgUrls) {
            if (smallImgUrls.isEmpty()) {
                throw new NullPointerException("smallImgUrls size is 0");
            }
            photoPagerBean.setSmallImgUrls((ArrayList<String>) smallImgUrls);
            return this;
        }

        /**
         * 一张一张大图add进ArrayList
         *
         * @param bigImageUrl 大图url
         * @return Builder
         */
        public Builder<T> addSingleBigImageUrl(String bigImageUrl) {
            if (TextUtils.isEmpty(bigImageUrl)) {
                throw new NullPointerException("bigImageUrl must not null");
            }
            photoPagerBean.addSingleBigImageUrl(bigImageUrl);
            return this;
        }

        /**
         * 一张一张小图add进ArrayList
         *
         * @param smallImageUrl 小图url
         * @return Builder
         */
        public Builder<T> addSingleSmallImageUrl(String smallImageUrl) {
            if (TextUtils.isEmpty(smallImageUrl)) {
                throw new NullPointerException("smallImageUrl must not null");
            }
            photoPagerBean.addSingleSmallImageUrl(smallImageUrl);
            return this;
        }

        /**
         * @deprecated use {@link #addSingleSmallImageUrl(String)}
         */
        public Builder<T> addSingleLowImageUrl(@NonNull String lowImageUrl) {
            photoPagerBean.addSingleLowImageUrl(lowImageUrl);
            return this;
        }

        public Builder<T> error(@DrawableRes int resourceId) {
            photoPagerBean.setErrorResId(resourceId);
            return this;
        }

        /**
         * 设置用户想传递的bundle
         *
         * @param bundle
         * @return
         */
        public Builder<T> setBundle(@NonNull Bundle bundle) {
            this.bundle = bundle;
            return this;
        }

        /**
         * 是否开启下滑关闭activity，默认开启。类似微信的图片浏览，可下滑关闭一样，但是没有图片归位效果
         *
         * @param isOpenDownAnimate default true
         * @return
         */
        public Builder<T> setOpenDownAnimate(boolean isOpenDownAnimate) {
            photoPagerBean.setOpenDownAnimate(isOpenDownAnimate);
            return this;
        }

        /**
         * 你也可以不设置任何参数，除了此参数以为，{@link PhotoPagerBean}是参数配置的一个实体，你可以直接传个实体进来也可以
         *
         * @param photoPagerBean PhotoPagerBean
         * @return Builder
         */
        public Builder<T> setPhotoPagerBean(@NonNull PhotoPagerBean photoPagerBean) {
            this.photoPagerBean = photoPagerBean;
            return this;
        }

        /**
         * 保存图片到图库的回调
         *
         * @param onPhotoSaveCallback
         * @return
         */
        public Builder<T> setOnPhotoSaveCallback(OnPhotoSaveCallback onPhotoSaveCallback) {
            if (onPhotoSaveCallback != null) {
                setSaveImage(true);
            }
            photoPagerBean.setOnPhotoSaveCallback(onPhotoSaveCallback);
            return this;
        }

        public PhotoPagerConfig build() {
            return new PhotoPagerConfig(context, this);
        }

    }

}
