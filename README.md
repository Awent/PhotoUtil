
 # PhotoUtil (仅支持AndroidX，本项目已适配Android Q)
 1、图库选图或视频，可多选，单选，可选完图片后进行裁剪，2、查看网络大图(自动识别是否为超长图)，并可以保存到手机图库,3、适配沉浸式状态栏，4、Glide加载
 
 [demo-apk下载](https://github.com/Awent/PhotoUtil/blob/master/simaple/release/simaple-release.apk)
 
 下面来看几张效果图：

![image](https://github.com/Awent/PhotoPick-Master/blob/master/pictrue/304079-052c8fd0c9d22efd.gif)

![image](https://github.com/Awent/PhotoPick-Master/blob/master/pictrue/304079-8d726553c6c0b6ba.gif)

![image](https://github.com/Awent/PhotoPick-Master/blob/master/pictrue/304079-e4c819f695ed83c0.gif)

![image](https://github.com/Awent/PhotoPick-Master/blob/master/pictrue/device-2017-10-25-033458.png)


# 下面讲解如何导入到你的项目中

1、在你的root gradle(也就是项目最外层的gradle)添加如下代码

```

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}

```

2、然后在module gradle dependencies 添加以下依赖，如果添加失败，请打开vpn后重试

```
    implementation "androidx.appcompat:appcompat:1.2.0"
    implementation "androidx.recyclerview:recyclerview:1.1.0"
    implementation 'androidx.constraintlayout:constraintlayout:2.0.1'
    implementation "androidx.legacy:legacy-support-v4:1.0.0"
    //跟随viewpager的点
    implementation 'me.relex:circleindicator:1.1.8@aar'
    //上滑控制面板,项目中的potopick中有使用案例
    implementation 'com.sothree.slidinguppanel:library:3.3.0'
    //android6.0权限工具类
    implementation 'com.lovedise:permissiongen:0.1.1'
    //加载超长图必备库
    implementation 'com.davemorrissey.labs:subsampling-scale-image-view:3.10.0'
    implementation("com.github.bumptech.glide:glide:4.11.0") {
        exclude group: "com.android.support"
    }
    annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
    //使用okhttp拦截器进行图片加载进度监听
    implementation 'com.github.bumptech.glide:okhttp3-integration:4.10.0'
    //解决Glide找不到Android声明库问题
    annotationProcessor 'androidx.annotation:annotation:1.1.0'
    implementation 'com.github.chrisbanes:PhotoView:2.3.0'
    implementation 'com.github.chrisbanes:PhotoView:2.3.0'
    //图库
    implementation 'com.github.Awent:PhotoUtil:v1.0'
```


## 下面讲解如何使用(可进行动态设置参数，不用的可以不设置)

- 图库

```
new PhotoPickConfig.Builder(this)
    .pickModeMulti()                            //多选
    .pickModeSingle()                           //单选
    .onlyImage()                                //只显示图片,不包含视频，默认是图片跟视频都展示,如果showCamera(true)，只会启动系统拍照，并返回图片路径
    .onlyVideo()                                //只显示视频,不包含图片，默认是图片跟视频都展示,如果showCamera(true)，只会启动系统视频录制，并返回视频路径
    .maxPickSize(15)                            //最多可选15张
    .showCamera(false)                          //是否展示拍照icon,默认展示
    .clipPhoto()                                //是否选完图片进行图片裁剪，默认是false,如果这里设置成true,就算设置了是多选也会被配置成单选
    .clipPhotoWithSystem()                      //是否选完图片进行图片裁剪，默认是false,如果这里设置成true,就算设置了是多选也会被配置成单选，这里调用的是系统裁剪功能
    .spanCount(4)                               //图库的列数，默认3列，这个数建议不要太大
    .showGif(true)//default true                //是否展示gif
    .setToolbarBackGround(@ColorRes int toolbarBackGroundId) //设置toolbar颜色
    .setOnPhotoResultCallback(OnPhotoResultCallback onPhotoResultCallback) //设置数据回调，如果不想在Activity通过onActivityResult()获取回传的数据，可实现此接口
    .build();

```

- 获取图库选择了(或裁剪好)的图片地址

```
方法一：
new PhotoPickConfig.Builder(this)
    .pickModeMulti()
    .maxPickSize(15)
    .showCamera(true)
    .setOriginalPicture(true)//让用户可以选择原图
    .setOnPhotoResultCallback(new PhotoPickConfig.Builder.OnPhotoResultCallback() {
          @Override
          public void onResult(PhotoResultBean result) {
                 Log.e("MainActivity", "result = " + result.getPhotoLists().size());
          }
    })
    .build();
    
    
方法二：
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK || data == null) {
        return;
    }
    switch (requestCode) {
        case PhotoPickConfig.PICK_REQUEST_CODE:
            //返回的图片地址集合
            ArrayList<String> photoLists = data.getStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST);
            if (photoLists != null && !photoLists.isEmpty()) {
                File file = new File(photoLists.get(0));//获取第一张图片
                if (file.exists()) {
                    //you can do something

                } else {
                    //toast error
                }
            }
            break;
    }
}

```

- 查看网络大图

```
new PhotoPagerConfig.Builder<T>(this)
    .fromList(Iterable<? extends T> iterable, OnItemCallBack<T> listener)//接收集合数据并提供内循环返回所有item
    .fromMap(Map<?, T> map, OnItemCallBack<T> listener)                  //接收集合数据并提供内循环返回所有item
    .setBigImageUrls(ImageProvider.getImageUrls())      //大图片url,可以是sd卡res，asset，网络图片.
    .setSmallImageUrls(ArrayList<String> smallImgUrls)  //小图图片的url,用于大图展示前展示的
    .addSingleBigImageUrl(String bigImageUrl)           //一张一张大图add进ArrayList
    .addSingleSmallImageUrl(String smallImageUrl)       //一张一张小图add进ArrayList
    .setSaveImage(true)                                 //开启保存图片，默认false
    .setPosition(2)                                     //默认展示第2张图片
    .setSaveImageLocalPath("Android/SD/xxx/xxx")        //这里是你想保存大图片到手机的地址,可在手机图库看到，不传会有默认地址，android Q会忽略此参数
    .setBundle(bundle)                                  //传递自己的数据，如果数据中包含java bean，必须实现Parcelable接口
    .setOpenDownAnimate(false)                          //是否开启下滑关闭activity，默认开启。类似微信的图片浏览，可下滑关闭一样
    .setOnPhotoSaveCallback(new OnPhotoSaveCallback()   //保存网络图片到本地图库的回调,保存成功则返回本地图片路径，失败返回null
    .error(@DrawableRes int resourceId)                 //网络图片加载失败时显示的错误图片,不传会有默认的
    .build();
    
自定义界面，详细自定义用法参考demo：

new PhotoPagerConfig.Builder(this,Class<?> clazz)       //这里传入你自定义的Activity class,自定义的activity必须继承PhotoPagerActivity
    ...
    ...
    ...
    .build();
```

- 混淆

```
参考simple中的proguard-rules文件

```

- 开发中常用的查看网络大图`fromList`和`fromMap`用法介绍

    1、使用场景：
    图片`url`存在于集合实体类里，例如：`Map<Integer, UserBean.User>` 或 `List<UserBean.User> userList`，
    这时候不需要自己循环这些集合取出图片url了，本方法会提供内部循环，你只需关注你的图片`url`字段就行

    2、使用示例：用户头像`avatar`存在于`User`实体类里面，是通过服务器返回来的

```
List<UserBean.User> list = getUserInfo();           //使用fromList
or
Map<Integer, UserBean.User> map = new HashMap<>();  //使用fromMap

java写法：
            new PhotoPagerConfig.Builder<UserBean.User>(this)
                        .fromList(list, new PhotoPagerConfig.Builder.OnItemCallBack<UserBean.User>() {
                            @Override
                            public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
                                builder.addSingleBigImageUrl(item.getAvatar());
                            }
                        })
                        .setOnPhotoSaveCallback(new PhotoPagerConfig.Builder.OnPhotoSaveCallback() {
                            @Override
                            public void onSaveImageResult(String localFilePath) {
                                Toast(localFilePath != null ? "保存成功" : "保存失败");
                            }
                        })
                        .build();
                                  
                                  
kotlin写法：
              list?.let {
                PhotoPagerConfig.Builder<UserBean.User>(this)
                        .fromList(it) { item, builder ->
                            builder.addSingleBigImageUrl(item.avatar)//这个avatar就是你需要关注的字段，在这里设置进去即可
                        }.build()
              }
                                  
```

### 注意事项：

如果自定义`activity`（必须继承`PhotoPagerActivity`）开启了下滑关闭功能，记得自定义`activity`的`Theme`要引用`{@style/PhoAppTheme.Transparent}`
 * 例子如下：
 ```
 <activity
            android:theme="@style/PhoAppTheme.Transparent"
            android:name=".activity.CustomPhotoPageActivity"/>
 ```
            
 
[更多使用方法参考点这里](https://github.com/Awent/PhotoUtil/blob/master/simaple/src/main/java/com/simaple/activity/MainActivity.java)

[Glide图片加载进度参考](https://juejin.im/post/6847902221951041549)

### v1.0：
implementation 'com.github.Awent:PhotoUtil:v1.0'

