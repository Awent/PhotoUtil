package com.awen.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.awen.image.photopick.controller.PhotoPagerConfig;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.ui.PhotoPagerActivity;
import com.awen.image.photopick.util.GlideCacheUtil;
import com.awen.image.photopick.ui.VideoPlayActivity;

/**
 * 图库可调用下面两个方法：
 * @see #pick(Context)
 * @see #pick(Fragment)
 *
 * 查看网络大图可以调用以下方法：
 * @see #browser(Context)
 * @see #browser(Fragment)
 * @see #browser(Context, Class)
 * @see #browser(Fragment, Class)
 * @see #browser(Context, Class, Class)
 * @see #browser(Fragment, Class, Class)
 * @see #browserCustom(Context, Class) //自定义查看网络大图界面
 *
 * 以下四个方法可能是你想用的
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
     * 新建一个图库选图、视频、拍照、视频录制、图片裁剪的Builder
     * 示例：
     *
     * {@code
            PhotoUtil.pick(this)
            .pickModeMulti()
            .maxPickSize(15)
            .showCamera(false)
            .setToolbarBackGround(R.color.colorAccent)
            .showGif(false)
            .setOnPhotoResultCallback(new PhotoPickConfig.Builder.OnPhotoResultCallback() {
                @Override
                public void onResult(PhotoResultBean result) {

                }
            })
            .build();
     * }
     * @param context activity
     * @return PhotoPickConfig.Builder
     */
    @NonNull
    @CheckResult
    public static PhotoPickConfig.Builder pick(@NonNull Context context) {
        return new PhotoPickConfig.Builder(context);
    }

    /**
     * 新建一个图库选图、视频、拍照、视频录制、图片裁剪的Builder
     *
     * @param fragment fragment
     * @return PhotoPickConfig.Builder
     */
    @NonNull
    @CheckResult
    public static PhotoPickConfig.Builder pick(@NonNull Fragment fragment) {
        return new PhotoPickConfig.Builder(fragment.getContext());
    }

    /**
     * 新建一个默认的查看网络大图Builder，泛型为{@code String.class}
     *
     * @param context activity
     * @return PhotoPagerConfig.Builder<String>
     */
    @NonNull
    @CheckResult
    public static PhotoPagerConfig.Builder<String> browser(@NonNull Context context) {
        return new PhotoPagerConfig.Builder<String>(context).create();
    }

    /**
     * 新建一个查看网络大图Builder
     例子：
     * {@code
     *        List<UserBean.User> userList = getUserList();                             //从网络获取数据
     *        PhotoUtil.browser(this,UserBean.User.class)                               //传入实体类
     *                 .fromList(userList, new PhotoPagerConfig.Builder.OnItemCallBack<UserBean.User>() { //使用fromList循环userList
     *                      @Override
     *                      public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
     *                         //一定要在这里获取你的图片字段，然后设置进去即可
     *                         builder.addSingleBigImageUrl(item.getAvatar());
     *                         builder.addSingleSmallImageUrl(item.getSmallAvatar());
     *                      }
     *                 })
     *                 .setSaveImage(true)
     *                 .setBundle(mBundle)
     *                 .build();
     * }
     * @param context activity
     * @param t       实体类
     * @param <T>     实体类
     * @return PhotoPagerConfig.Builder<T>
     */
    @NonNull
    @CheckResult
    public static <T> PhotoPagerConfig.Builder<T> browser(@NonNull Context context, @NonNull Class<T> t) {
        return new PhotoPagerConfig.Builder<T>(context).create();
    }

    /**
     * 新建一个查看网络大图Builder，跳转到自定义的activity
     * 例子：
     * {@code
     *        PhotoUtil.browserCustom(this, CustomPhotoPageActivity.class)
     *                 .setBigImageUrls(ImageProvider.getImageUrls())
     *                 .setSaveImage(true)
     *                 .setBundle(mBundle)
     *                 .build();
     * }
     * @param context activity
     * @param clazz   继承了{@link com.awen.image.photopick.ui.PhotoPagerActivity}的自定义activity
     * @param <T>     实体类
     * @return PhotoPagerConfig.Builder<T>
     */
    @NonNull
    @CheckResult
    public static <T> PhotoPagerConfig.Builder<T> browserCustom(@NonNull Context context, @NonNull Class<? extends PhotoPagerActivity> clazz) {
        return new PhotoPagerConfig.Builder<T>(context,clazz).create();
    }

    /**
     * 新建一个查看网络大图Builder，跳转到自定义的activity（extend PhotoPageActivity），并传递实体类，这里你只需关注你实体类里面的图片url字段就行
     * 一般的应用场景为：
     * 1、从网络获取数据回来，图片url存在于实体类里面，实体类存在于集合里面
     * 2、需要自定义实现‘查看大图’界面（extend PhotoPageActivity）
     * 3、点击查看大图
     * 4、循环取出图片url传到大图界面
     例子：
     * {@code
     *        List<UserBean.User> userList = getUserList();                             //从网络获取数据
     *        PhotoUtil.browser(this,UserBean.User.class,CustomPhotoPageActivity.class) //跳到自定义查看网络大图界面
     *                 .fromList(userList, new PhotoPagerConfig.Builder.OnItemCallBack<UserBean.User>() { //使用fromList循环userList
     *                      @Override
     *                      public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
     *                         //一定要在这里获取你的图片字段，然后设置进去即可
     *                         builder.addSingleBigImageUrl(item.getAvatar());
     *                         builder.addSingleSmallImageUrl(item.getSmallAvatar());
     *                      }
     *                 })
     *                 .setSaveImage(true)
     *                 .setBundle(mBundle)
     *                 .build();
     * }
     * @param context activity
     * @param t       实体类
     * @param clazz   继承了{@link com.awen.image.photopick.ui.PhotoPagerActivity}的自定义activity
     * @param <T>     实体类
     * @return PhotoPagerConfig.Builder<T>
     */
    @NonNull
    @CheckResult
    public static <T> PhotoPagerConfig.Builder<T> browser(@NonNull Context context, @NonNull Class<T> t, @NonNull Class<? extends PhotoPagerActivity> clazz) {
        return new PhotoPagerConfig.Builder<T>(context, clazz).create();
    }

    /**
     * 新建一个默认的查看网络大图Builder，泛型为{@code String.class}
     *
     * @param fragment fragment
     * @return PhotoPagerConfig.Builder<String>
     */
    @NonNull
    @CheckResult
    public static PhotoPagerConfig.Builder<String> browser(@NonNull Fragment fragment) {
        return new PhotoPagerConfig.Builder<String>(fragment.getContext()).create();
    }

    /**
     * 新建一个查看网络大图Builder
     *
     * @param fragment fragment
     * @param t        实体类
     * @param <T>      实体类
     * @return PhotoPagerConfig.Builder<T>
     */
    @NonNull
    @CheckResult
    public static <T> PhotoPagerConfig.Builder<T> browser(@NonNull Fragment fragment, @NonNull Class<T> t) {
        return new PhotoPagerConfig.Builder<T>(fragment.getContext()).create();
    }

    /**
     * 新建一个查看网络大图Builder
     *
     * @param fragment fragment
     * @param t        实体类
     * @param clazz    继承了{@link com.awen.image.photopick.ui.PhotoPagerActivity}的自定义activity
     * @param <T>      实体类
     * @return PhotoPagerConfig.Builder<T>
     */
    @NonNull
    @CheckResult
    public static <T> PhotoPagerConfig.Builder<T> browser(@NonNull Fragment fragment, @NonNull Class<T> t, @NonNull Class<? extends PhotoPagerActivity> clazz) {
        return new PhotoPagerConfig.Builder<T>(fragment.getContext(), clazz).create();
    }

    /**
     * 清除所有缓存（包括内存+磁盘）
     */
    public static void clearCaches(@NonNull Context context) {
        GlideCacheUtil.clearImageMemoryCache(context);
        GlideCacheUtil.clearImageDiskCache(context);
    }

    /**
     * 获取磁盘上缓存文件的大小,带单位，比如：20MB
     */
    public static String getSDCacheSizeFormat(@NonNull Context context) {
        return GlideCacheUtil.getCacheSize(context);
    }

    /**
     * 启动一个视频播放
     *
     * @param activity
     * @param videoUrl 本地视频地址或网络视频地址
     */
    public static void startVideoActivity(@NonNull Activity activity, @NonNull String videoUrl) {
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
    public static void startVideoActivity(@NonNull Activity activity, @NonNull Uri videoUri) {
        activity.startActivity(new Intent(activity, VideoPlayActivity.class)
                .putExtra("videoUrl", videoUri.toString()));
        activity.overridePendingTransition(R.anim.image_pager_enter_animation, 0);
    }
}
