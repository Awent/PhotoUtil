package com.awen.image.photopick.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import com.awen.image.ImageBaseActivity;
import com.awen.image.R;
import com.awen.image.photopick.adapter.SamplePagerAdapter;
import com.awen.image.photopick.listener.OnPhotoSaveCallback;
import com.awen.image.photopick.pro.ProgressInterceptor;
import com.awen.image.photopick.bean.PhotoPagerBean;
import com.awen.image.photopick.util.ImageUtils;
import com.awen.image.photopick.util.PermissionUtil;
import com.awen.image.photopick.controller.PhotoPagerConfig;
import com.awen.image.photopick.widget.ScalePhotoView;
import com.github.chrisbanes.photoview.OnViewTapListener;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import me.relex.circleindicator.CircleIndicator;

/**
 * 图片查看器<br>
 * <p>
 * 你可重写以下方法：<br>
 * {@link #setCustomView(int)} <br>
 * {@link #init()}}<br>
 * {@link #getCustomView()}<br>
 * {@link #setIndicatorVisibility(boolean)}<br>
 * {@link #onSingleClick()}<br>
 * {@link #onLongClick(View)}<br>
 * {@link #saveImage()}<br>
 * {@link #onPageSelected(int)} ()}<br>
 * </p>
 *
 * 使用示例：
 *
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
 *     .setOnPhotoSaveCallback(new OnPhotoSaveCallback())  //保存网络图片到本地图库的回调,保存成功则返回本地图片路径，失败返回null
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
 * @author Homk-M <Awentljs@gmail.com>
 */
public class PhotoPagerActivity extends ImageBaseActivity implements ViewPager.OnPageChangeListener, View.OnLongClickListener {
    private static final String TAG = PhotoPagerActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 100;
    private static final String STATE_POSITION = "STATE_POSITION";
    private ViewPager viewPager;
    private CircleIndicator indicator;
    protected PhotoPagerBean photoPagerBean;
    private String saveImageLocalPath;
    private boolean saveImage;
    protected int currentPosition;

    private ScalePhotoView scalePhotoView;

    private FrameLayout rootLayout;
    private View customView;

    private static OnPhotoSaveCallback onPhotoSaveCallback;

    /**
     * 设置自定义view layout
     *
     * @param layoutId
     */
    protected void setCustomView(@LayoutRes int layoutId) {
        if (layoutId == -1) {
            return;
        }
        customView = LayoutInflater.from(this).inflate(layoutId, null);
        rootLayout.addView(customView);
        init();
    }

    /**
     * 获取自定义的view
     *
     * @return
     */
    protected View getCustomView() {
        return customView;
    }

    /**
     * 初始化,可以在这里findViewById和数据初始化工作<br>
     * findViewById要用getCustomView(),例如: <code>getCustomView().findViewById(R.id.button)</code>
     */
    protected void init() {
    }

    protected Bundle getBundle() {
        return getIntent().getBundleExtra(PhotoPagerConfig.EXTRA_USER_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOpenToolBar(false);
        Bundle bundle = getIntent().getBundleExtra(PhotoPagerConfig.EXTRA_PAGER_BUNDLE);
        if (bundle == null) {
            Log.e(TAG, "PhotoPagerConfig bundle is null，build failed");
            onBackPressed();
            return;
        }
        photoPagerBean = bundle.getParcelable(PhotoPagerConfig.EXTRA_PAGER_BEAN);
        if (photoPagerBean == null) {
            Log.e(TAG, "PhotoPagerConfig#photoPagerBean is null，build failed");
            onBackPressed();
            return;
        }
        saveImage = photoPagerBean.isSaveImage();
        saveImageLocalPath = photoPagerBean.getSaveImageLocalPath();

        rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        final View content = LayoutInflater.from(this).inflate(R.layout.activity_photo_detail_pager, rootLayout);
        setContentView(rootLayout);

        indicator = content.findViewById(R.id.indicator);
        viewPager = content.findViewById(R.id.pager);
        SamplePagerAdapter adapter = new SamplePagerAdapter(this, photoPagerBean);
        setListener(adapter);
        viewPager.setAdapter(adapter);
        setIndicatorVisibility(true);
        if (savedInstanceState != null) {
            photoPagerBean.setPagePosition(savedInstanceState.getInt(STATE_POSITION));
        }
        currentPosition = photoPagerBean.getPagePosition();
        viewPager.setCurrentItem(currentPosition);
        viewPager.addOnPageChangeListener(this);

        setCustomView(-1);//设置用户自定义的view

        //类似微信图片下拉关闭
        scalePhotoView = content.findViewById(R.id.scalePhotoView);
        scalePhotoView.setOpenDownAnimate(photoPagerBean.isOpenDownAnimate());
        if (photoPagerBean.isOpenDownAnimate()) {
            scalePhotoView.setOnViewTouchListener(new ScalePhotoView.onViewTouchListener() {
                @Override
                public void onFinish() {
//                    onBackPressed();
                    indicator.setVisibility(View.GONE);
                    finish();
                    overridePendingTransition(0, 0);
                }

                @Override
                public void onFinishStart() {
                    indicator.setVisibility(View.GONE);
                }

                @Override
                public void onMoving(float deltaX, float deltaY) {

                }
            });
        }
    }

    private void setListener(SamplePagerAdapter adapter) {
        //图片单击的回调
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSingleClick();
            }
        });
        adapter.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                onSingleClick();
            }
        });
        adapter.setOnLongClickListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, viewPager.getCurrentItem());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        scalePhotoView.setScaleFinish(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 图片单击回调
     */
    protected boolean onSingleClick() {
        if (!hasStop) {
            onBackPressed();
        }
        return false;
    }

    /**
     * //图片长按回调
     */
    @Override
    public boolean onLongClick(View view) {
        if (saveImage) {
            saveImageDialog();
        }
        return false;
    }

    protected String getCurrentImageUrl() {
        return photoPagerBean.getBigImgUrls().get(currentPosition);
    }

    /**
     * 保存图片到图库
     */
    protected void saveImage() {
        //以下操作会回调这两个方法:#startPermissionSDSuccess(), #startPermissionSDFaild()
        PermissionGen.needPermission(PhotoPagerActivity.this, REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 设置Indicator图片下标的可见状态
     */
    protected void setIndicatorVisibility(boolean visibility) {
        if (photoPagerBean.getBigImgUrls().size() == 1 || !visibility) {
            indicator.setVisibility(View.GONE);
        } else {
            indicator.setViewPager(viewPager);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 保存图片到本地图库，可在图库中看到，自定义界面的可以直接调用该方法可保存图片
     * 注意，Android Q版本即使设置了{@link #saveImageLocalPath}，也是无效，只会保存到系统公共的图片目录下：/storage/emulated/0/Pictures/
     */
    @PermissionSuccess(requestCode = REQUEST_CODE)
    public void startPermissionSDSuccess() {//获取读写sd卡权限成功回调
        //保存图片到本地
        String bigImgUrl = photoPagerBean.getBigImgUrls().get(currentPosition);
        ImageUtils.saveToLocal(bigImgUrl,saveImageLocalPath,onPhotoSaveCallback);
    }

    @PermissionFail(requestCode = REQUEST_CODE)
    public void startPermissionSDFaild() {
        if (!isFinishing()) {
            new android.app.AlertDialog.Builder(this)
                    .setMessage(getString(R.string.permission_tip_SD))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PermissionUtil.startSystemSettingActivity(PhotoPagerActivity.this);
                }
            }).setCancelable(false).show();
        }
    }

    private void saveImageDialog() {
        if (!isFinishing()) {
            new AlertDialog.Builder(PhotoPagerActivity.this)
                    .setItems(new String[]{getString(R.string.save_big_image)}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveImage();
                        }
                    }).show();
        }
    }

    public static void setOnPhotoSaveCallback(OnPhotoSaveCallback onPhotoSaveCallback) {
        PhotoPagerActivity.onPhotoSaveCallback = onPhotoSaveCallback;
    }

    /**
     * 设置是否可保存图片
     *
     * @param saveImage true:可以保存，false：不可保存
     */
    public void setSaveImage(boolean saveImage) {
        this.saveImage = saveImage;
    }

    /**
     * 只在android Q版本以下才会生效
     *
     * @param saveImageLocalPath sd卡路径
     */
    public void setSaveImageLocalPath(String saveImageLocalPath) {
        this.saveImageLocalPath = saveImageLocalPath;
    }

    private boolean hasStop;

    @Override
    protected void onResume() {
        super.onResume();
        hasStop = false;
    }

    @Override
    protected void onDestroy() {
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(this);
            viewPager.setAdapter(null);
            viewPager = null;
        }
        photoPagerBean = null;
        hasStop = true;
        onPhotoSaveCallback = null;
        ProgressInterceptor.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        indicator.setVisibility(View.GONE);
        finish();
        overridePendingTransition(0, R.anim.image_pager_exit_animation);
        super.onBackPressed();
    }

}
