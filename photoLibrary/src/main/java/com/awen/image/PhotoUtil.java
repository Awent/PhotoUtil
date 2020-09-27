package com.awen.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.awen.image.photopick.controller.CameraBuilder;
import com.awen.image.photopick.controller.PhotoPagerConfig;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.ui.PhotoPagerActivity;
import com.awen.image.photopick.util.GlideCacheUtil;
import com.awen.image.photopick.ui.VideoPlayActivity;

/**
 * 1、图库可调用下面两个方法：
 * @see #pick(Context)
 * @see #pick(Fragment)
 *
 * 2、查看网络大图可以调用以下方法：
 * @see #browser(Context)
 * @see #browser(Fragment)
 * @see #browser(Context, Class)
 * @see #browser(Fragment, Class)
 * @see #browser(Context, Class, Class)
 * @see #browser(Fragment, Class, Class)
 * @see #browserCustom(Context, Class) //自定义查看网络大图界面
 *
 * 3、单独启动相机：
 * @see #camera(FragmentActivity)
 *
 * 以下四个方法可能是你想用的
 * @see #setToolbarBackGround(int)
 * @see #setSaveImageLocalPath(String)
 * @see #getSDCacheSizeFormat(Context) ()
 * @see #clearCaches(Context) ()
 *
 *
 *
 * 1、查看网络大图常用API:
 * {@code
 *
 * PhotoUtil.browser(this, Class<T> t)
 *     .fromList(Iterable<? extends T> iterable, OnItemCallBack<T> listener)//接收集合数据并提供内循环返回所有item
 *     .fromMap(Map<?, T> map, OnItemCallBack<T> listener)                  //接收集合数据并提供内循环返回所有item
 *     .setBigImageUrls(ImageProvider.getImageUrls())      //大图片url,可以是sd卡res，asset，网络图片.
 *     .setSmallImageUrls(ArrayList<String> smallImgUrls)  //小图图片的url,用于大图展示前展示的
 *     .addSingleBigImageUrl(String bigImageUrl)           //一张一张大图add进ArrayList
 *     .addSingleSmallImageUrl(String smallImageUrl)       //一张一张小图add进ArrayList
 *     .setSaveImage(true)                                 //开启保存图片，默认false
 *     .setPosition(2)                                     //默认展示第2张图片
 *     .setSaveImageLocalPath("Android/SD/xxx/xxx")        //这里是你想保存大图片到手机的地址,可在手机图库看到，不传会有默认地址，android Q会忽略此参数
 *     .setBundle(bundle)                                  //传递自己的数据，如果数据中包含java bean，必须实现Parcelable接口
 *     .setOpenDownAnimate(false)                          //是否开启下滑关闭activity，默认开启。类似微信的图片浏览，可下滑关闭一样
 *     .setOnPhotoSaveCallback(new OnPhotoSaveCallback()   //保存网络图片到本地图库的回调,保存成功则返回本地图片路径，失败返回null
 *     .error(@DrawableRes int resourceId)                 //网络图片加载失败时显示的错误图片,不传会有默认的
 *     .build();
 *
 * 自定义界面：
 *
 * PhotoUtil.browserCustom(this,Class<? extends PhotoPagerActivity> clazz)       //这里传入你自定义的Activity class,自定义的activity必须继承PhotoPagerActivity
 *     ...
 *     ...
 *     ...
 *     .build();
 *
 * }
 *
 *
 *
 * 2、图库选图常用API：
 * {@code
 *
 * PhotoUtil.pick(this)
 *     .pickModeMulti()                            //多选
 *     .pickModeSingle()                           //单选
 *     .onlyImage()                                //只显示图片,不包含视频，默认是图片跟视频都展示,如果showCamera(true)，只会启动系统拍照，并返回图片路径
 *     .onlyVideo()                                //只显示视频,不包含图片，默认是图片跟视频都展示,如果showCamera(true)，只会启动系统视频录制，并返回视频路径
 *     .maxPickSize(15)                            //最多可选15张
 *     .showCamera(false)                          //是否展示拍照icon,默认展示
 *     .clipPhoto()                                //是否选完图片进行图片裁剪，默认是false,如果这里设置成true,就算设置了是多选也会被配置成单选
 *     .clipPhotoWithSystem()                      //是否选完图片进行图片裁剪，默认是false,如果这里设置成true,就算设置了是多选也会被配置成单选，这里调用的是系统裁剪功能
 *     .spanCount(4)                               //图库的列数，默认3列，这个数建议不要太大
 *     .showGif(true)//default true                //是否展示gif
 *     .setToolbarBackGround(@ColorRes int toolbarBackGroundId) //设置toolbar颜色
 *     .setOnPhotoResultCallback(OnPhotoResultCallback onPhotoResultCallback) //设置数据回调，如果不想在Activity通过onActivityResult()获取回传的数据，可实现此接口
 *     .build();
 *
 * }
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
            .setOnPhotoResultCallback(new OnPhotoResultCallback() {
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
     * @param fragment fragment
     * @return PhotoPickConfig.Builder
     */
    @NonNull
    @CheckResult
    public static PhotoPickConfig.Builder pick(@NonNull Fragment fragment) {
        return pick(getFragmentContext(fragment));
    }

    /**
     * 新建一个默认的查看网络大图Builder，泛型为{@code String.class}
     * 例子：
     * {@code
     *        PhotoUtil.browser(this)
     *                 .setBigImageUrls(ImageProvider.getImageUrls())
     *                 .setSaveImage(true)
     *                 .build();
     * }
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
     *                 .fromList(userList, new OnItemCallBack<UserBean.User>() {        //使用fromList循环userList
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
     *        PhotoUtil.browser(this, UserBean.User.class, CustomPhotoPageActivity.class) //跳到自定义查看网络大图界面
     *                 .fromList(userList, new OnItemCallBack<UserBean.User>() { //使用fromList循环userList
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
        return browser(getFragmentContext(fragment));
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
        return browser(getFragmentContext(fragment),t);
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
        return browser(getFragmentContext(fragment),t,clazz);
    }

    /**
     /**
     * 单独启动系统相机，并自动判断相机权限
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
     *                         .setAspectX(1) //裁剪框 X 比值，不设置会有默认
     *                         .setAspectY(1) //裁剪框 Y 比值，不设置会有默认
     *                         .setOutputX(400) //裁剪后输出宽度，不设置会有默认
     *                         .setOutputY(400) //裁剪后输出高度，不设置会有默认
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
     * //                      .setVideoMaxSize(10 * 1024 * 1024)   //10M
     *                         .setVideoDuration(10)                //10秒,很多国产机中这个参数无效
     *                         .setOnPhotoResultCallback(new OnPhotoResultCallback() {
     *                             @Override
     *                             public void onResult(@NonNull PhotoResultBean result) {
     *                                 startAc(result.getList());
     *                             }
     *                         })
     *                         .build();
     * }
     * @param context 必须为 FragmentActivity
     * @return {@link CameraBuilder}
     */
    public static CameraBuilder camera(FragmentActivity context){
        return CameraBuilder.create(context);
    }

    public static CameraBuilder camera(Fragment fragment){
        return CameraBuilder.create(fragment.getActivity());
    }

    private static Context getFragmentContext(@NonNull Fragment fragment){
        if(fragment.getContext() == null){
            return fragment.getActivity();
        }
        return fragment.getContext();
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
