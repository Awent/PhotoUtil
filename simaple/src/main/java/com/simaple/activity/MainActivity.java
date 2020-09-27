package com.simaple.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.awen.image.PhotoUtil;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoResultBean;
import com.awen.image.photopick.controller.CameraBuilder;
import com.awen.image.photopick.controller.PhotoPagerConfig;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.controller.PhotoPreviewConfig;
import com.awen.image.photopick.listener.OnItemCallBack;
import com.awen.image.photopick.listener.OnPhotoResultCallback;
import com.awen.image.photopick.listener.OnPhotoSaveCallback;
import com.awen.image.photopick.ui.PhotoPagerActivity;
import com.simaple.ImageProvider;
import com.simaple.bean.MyPhotoBean;
import com.simaple.R;
import com.simaple.bean.UserBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片归版权者所有
 * 你可以完全使用{@link PhotoUtil}图片工具类来完成所有功能的调用，你也可以使用new的方式。
 * 注：onclick方法里面所有注释了的代码都可以打开来直接运行的，注释是为了展示不同的调用方式
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
        findViewById(R.id.button9).setOnClickListener(this);
        findViewById(R.id.button10).setOnClickListener(this);
        findViewById(R.id.button11).setOnClickListener(this);
        findViewById(R.id.button12).setOnClickListener(this);
        findViewById(R.id.button13).setOnClickListener(this);
    }

    private void startAc(ArrayList<Photo> list) {
        startActivity(new Intent(MainActivity.this, SampleListActivity.class)
                .putParcelableArrayListExtra("photos", list));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button://图库
                PhotoUtil.pick(this)
                        .pickModeMulti()
                        .maxPickSize(15)
                        .showCamera(false)
                        .setToolbarBackGround(R.color.colorAccent)
//                        .showGif(false)
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NotNull PhotoResultBean result) {
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
            case R.id.button4://图库(可以启动拍照)
                PhotoUtil.pick(this)
                        .pickModeMulti()
                        .maxPickSize(15)
//                        .onlyVideo()
//                        .onlyImage()
                        .showCamera(true)
                        .setOriginalPicture(true)//让用户可以选择原图
                        .setToolbarBackGround(R.color.colorPrimary)
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NotNull PhotoResultBean result) {
                                Log.e("MainActivity", "result = " + result.getPhotoLists().size());
                                Log.e("MainActivity", "result photos= " + result.getList().size());
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
            case R.id.button2://裁剪头像
                PhotoUtil.pick(this)
//                        .clipPhoto()//启动裁剪，但不是系统裁剪
                        .clipPhotoWithSystem()//启动系统裁剪功能
                        .setToolbarBackGround(R.color.navigationBarColor)
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NotNull PhotoResultBean result) {
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
            case R.id.button3://查看(网络)大图
                PhotoUtil.browser(this)
                        .setBigImageUrls(ImageProvider.getImageUrls())
                        .setSaveImage(true)
//                        .setPosition(2)
//                        .setSaveImageLocalPath("这里是你想保存的图片地址")
                        .setOnPhotoSaveCallback(new OnPhotoSaveCallback() {//保存网络图片到本地图库的回调
                            @Override
                            public void onSaveImageResult(String localFilePath) {
                                if (localFilePath != null) {
                                    Toast.makeText(MainActivity.this, "图片保存成功，本地地址：" + localFilePath, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "图片保存失败", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .build();
                break;
            case R.id.button5://大图展示前先显示小图
                PhotoUtil.browser(this)
                        .setBigImageUrls(ImageProvider.getBigImgUrls())
                        .setSmallImageUrls(ImageProvider.getSmallImgUrls())
                        .setSaveImage(true)
                        .build();
                break;
            case R.id.button6://获取缓存大小:
                ((Button) findViewById(R.id.button6)).setText("缓存大小：" + PhotoUtil.getSDCacheSizeFormat(this));
                break;
            case R.id.button7://清除所有缓存
                PhotoUtil.clearCaches(this);
                ((Button) findViewById(R.id.button6)).setText("缓存大小：" + PhotoUtil.getSDCacheSizeFormat(this));
                break;
            case R.id.button8://跳到自定义view界面
                /**
                 * 1、传递数据：实现Parcelable接口，把你想传递的数据封装进Bundle，然后.setBundle(bundle)即可
                 * 2、获取数据：Bundle bundle = getBundle();
                 if(bundle != null) {
                 list = bundle.getParcelableArrayList("test_bundle");
                 if (list != null && !list.isEmpty()){
                 //you can do something
                 }
                 }
                 */
                ArrayList<MyPhotoBean> list = new ArrayList<>();
                for (int i = 0; i < ImageProvider.getImageUrls().size(); i++) {
                    MyPhotoBean bean = new MyPhotoBean();
                    bean.setId(i);
                    bean.setContent("content = 你是否还记得？那年我们在春暖花开里相遇，我们都用真情，守护着相遇后的每一秒光阴。每一次与你目光碰触，你的眼睛清澈如水，深邃如诗，绽不尽的芳华，浪漫依依。" + "---" + i);
                    list.add(bean);
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("test_bundle", list);

                //注意这里调用的是browserCustom
                PhotoUtil.browserCustom(this, MyPhotoPagerActivity.class)
                        .setBigImageUrls(ImageProvider.getImageUrls())
                        .setSaveImage(true)
                        .setBundle(bundle) //传递自己的数据，如果数据中包含java bean，必须实现Parcelable接口
                        .setOpenDownAnimate(false)
                        .build();
                break;
            case R.id.button9://跳到自定义的PhotoPagerActivity(kotlin写法)
                Bundle mBundle = new Bundle();
                mBundle.putLong("user_id", 100000L);

                PhotoUtil.browserCustom(this, CustomPhotoPageActivity.class)
                        .setBigImageUrls(ImageProvider.getImageUrls())
                        .setSaveImage(true)
                        .setBundle(mBundle)
                        .build();
                break;
            case R.id.button10://实际开发常用写法（查看网络大图）
                //fromList的使用
                List<UserBean.User> userList = initUserData().getList();
//                PhotoUtil.browser(this,UserBean.User.class,CustomPhotoPageActivity.class/**或者这里传入你自定义的CustomPhotoPageActivity*/)
                PhotoUtil.browser(this, UserBean.User.class)
                        .fromList(userList, new OnItemCallBack<UserBean.User>() {
                            @Override
                            public void nextItem(@NotNull UserBean.User item, @NotNull PhotoPagerConfig.Builder<UserBean.User> builder) {
                                //一定要在这里获取你的图片字段，然后设置进去即可
                                builder.addSingleBigImageUrl(item.getAvatar());
                                builder.addSingleSmallImageUrl(item.getSmallAvatar());
                            }
                        })
                        .setSaveImage(true)
                        .build();


                //或者是以下的fromMap使用
//                Map<Integer, UserBean.User> map = initUserDataMap();
//                PhotoUtil.browser(this,UserBean.User.class)
//                        .fromMap(map, new PhotoPagerConfig.Builder.OnItemCallBack<UserBean.User>() {
//                            @Override
//                            public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
//                                //在这里获取你的图片字段，然后设置进去即可
//                                builder.addSingleBigImageUrl(item.getAvatar());
//                            }
//                        })
//                        .setSaveImage(true)
//                        .build();
                break;
            case R.id.button11://相机-拍照（单独调用相机）
                PhotoUtil.camera(this)
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NonNull PhotoResultBean result) {
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
            case R.id.button12://相机-拍照并裁剪（单独调用相机）
                PhotoUtil.camera(this)
//                        .clipPhoto()
                        .clipPhotoWithSystem()
//                        .setAspectX(1) //裁剪框 X 比值
//                        .setAspectY(1) //裁剪框 Y 比值
//                        .setOutputX(400) //裁剪后输出宽度
//                        .setOutputY(400) //裁剪后输出高度
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NonNull PhotoResultBean result) {
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
            case R.id.button13://相机-系统录像（单独调用相机）
                PhotoUtil.camera(this)
                        .takeVideo()
//                        .setVideoMaxSize(10 * 1024 * 1024)//10M
//                        .setVideoDuration(10)//10秒,很多国产机中这个参数无效
                        .setOnPhotoResultCallback(new OnPhotoResultCallback() {
                            @Override
                            public void onResult(@NonNull PhotoResultBean result) {
                                startAc(result.getList());
                            }
                        })
                        .build();
                break;
        }
    }

    private UserBean initUserData() {
        UserBean bean = new UserBean();
        bean.setCode(200);
        bean.setMsg("success");
        List<UserBean.User> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            UserBean.User user = new UserBean.User();
            user.setAvatar("https://wx1.sinaimg.cn/mw690/7325792bly1fx9oma87k1j21900u04jf.jpg");
            user.setSmallAvatar("https://wx1.sinaimg.cn/mw690/7325792bly1fx9oma87k1j21900u04jf.jpg");
            user.setName("name = " + i);
            user.setAge(i);
            list.add(user);
        }
        bean.setList(list);
        return bean;
    }

    private Map<Integer, UserBean.User> initUserDataMap() {
        Map<Integer, UserBean.User> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            UserBean.User user = new UserBean.User();
            user.setAvatar("https://wx1.sinaimg.cn/mw690/7325792bly1fx9oma87k1j21900u04jf.jpg");
            user.setSmallAvatar("https://wx1.sinaimg.cn/mw690/7325792bly1fx9oma87k1j21900u04jf.jpg");
            user.setName("name = " + i);
            user.setAge(i);
            map.put(i, user);
        }
        return map;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        MenuItem menuItem = menu.findItem(R.id.ok);
        menuItem.setTitle("示例");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            startActivity(new Intent(this, ListActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
