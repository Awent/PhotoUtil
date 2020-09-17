package com.awen.image.photopick.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.awen.image.PhotoSetting;
import com.awen.image.ImageBaseActivity;
import com.awen.image.R;
import com.awen.image.photopick.adapter.PhotoPickAdapter;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoPreviewBean;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.controller.PhotoPreviewConfig;
import com.awen.image.photopick.util.FileUtil;
import com.awen.image.photopick.util.ViewUtil;
import com.awen.image.photopick.widget.HackyViewPager;
import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

/**
 * 图片预览,包括长图(跟微信微博一样),后期会继续添加视频预览
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoPreviewActivity extends ImageBaseActivity implements ViewPager.OnPageChangeListener {

    private final String TAG = getClass().getSimpleName();
    private ArrayList<Photo> photos;
    private ArrayList<String> selectPhotos;
    private ArrayList<Photo> selectPhotoList;
    private ArrayList<Photo> previewPhotos;
    private OnViewTapListener onViewTapListener;//图片单击
    private View.OnClickListener onClickListener;//长图单击
    private CheckBox checkbox;
    private RadioButton radioButton;
    private int pos;
    private int maxPickSize;
    private boolean isChecked = false;
    private boolean originalPicture;
    private int screenWith, screenHeight;
    private HackyViewPager viewPager;
    private LinearLayout bottom_ll;

    @Override
    protected void onCreate(@Nullable Bundle arg0) {
        setOpenNavigationBar(true);
        super.onCreate(arg0);
        Bundle bundle = getIntent().getBundleExtra(PhotoPreviewConfig.EXTRA_BUNDLE);
        if (bundle == null) {
            throw new NullPointerException("bundle is null,please init it");
        }
        PhotoPreviewBean bean = bundle.getParcelable(PhotoPreviewConfig.EXTRA_BEAN);
        if (bean == null) {
            finish();
            return;
        }
        photos = bean.isPreview() ? PhotoPickAdapter.getPreviewPhotos() : PhotoPickAdapter.photos;
        if (photos == null || photos.isEmpty()) {
            finish();
            return;
        }
        originalPicture = bean.isOriginalPicture();
        maxPickSize = bean.getMaxPickSize();
        selectPhotos = PhotoPickAdapter.selectPhotos;
        selectPhotoList = PhotoPickAdapter.selectPhotoList;
        previewPhotos = PhotoPickAdapter.previewPhotos;
        final int beginPosition = bean.getPosition();
        setOpenToolBar(false);
        setContentView(R.layout.activity_photo_select);

        radioButton = (RadioButton) findViewById(R.id.radioButton);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        viewPager = (HackyViewPager) findViewById(R.id.pager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(PhotoSetting.getToolbarBackGround());
        toolbar.setTitle((beginPosition + 1) + "/" + photos.size());
        setSupportActionBar(toolbar);

        if (selectPhotoList != null && selectPhotoList.contains(photos.get(0))) {//pos=0的时候
            checkbox.setChecked(true);
        } else {
            checkbox.setChecked(false);
        }
        viewPager.addOnPageChangeListener(this);

        //图片单击的回调
        onViewTapListener = new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                onSingleClick();
            }
        };

        //长图单击回调
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSingleClick();
            }
        };

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectPhotoList == null) {
                    selectPhotoList = new ArrayList<>();
                }
                if (previewPhotos == null) {
                    previewPhotos = new ArrayList<>();
                }
                Photo photo = photos.get(pos);
                if (selectPhotoList.contains(photo)) {
                    selectPhotos.remove(photo.getPath());
                    selectPhotoList.remove(photo);
                    previewPhotos.remove(photos.get(pos));
                    checkbox.setChecked(false);
                } else {
                    if (maxPickSize == selectPhotoList.size()) {
                        checkbox.setChecked(false);
                        return;
                    }
                    selectPhotos.add(photo.getPath());
                    selectPhotoList.add(photo);
                    previewPhotos.add(photos.get(pos));
                    checkbox.setChecked(true);
                }
                updateMenuItemTitle();
            }
        });

        if (originalPicture) {
            Photo photo = photos.get(beginPosition);
            radioButton.setText(getString(photo.isVideo() ? R.string.video_size : R.string.image_size, FileUtil.formatFileSize(photo.getSize())));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isChecked) {
                        radioButton.setChecked(false);
                        isChecked = false;
                    } else {
                        radioButton.setChecked(true);
                        isChecked = true;
                    }
                }
            });
        } else {
            radioButton.setVisibility(View.GONE);
        }

        viewPager.setAdapter(new SamplePagerAdapter());
        viewPager.setCurrentItem(beginPosition);

        screenWith = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        //适配导航栏
        bottom_ll = (LinearLayout) findViewById(R.id.bottom_ll);
        int navigationBarHeight = ViewUtil.getNavigationBarHeight(this);
        if (navigationBarHeight > 0) {
            View navigation_bar_view = new View(this);
            navigation_bar_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, navigationBarHeight));
            bottom_ll.addView(navigation_bar_view);
        }
    }

    private void onSingleClick() {
        if (toolBarStatus) {
            hideViews();
        } else {
            showViews();
        }
    }

    private void updateMenuItemTitle() {
        if (selectPhotoList == null || selectPhotoList.isEmpty()) {
            menuItem.setTitle(R.string.send);
        } else {
            menuItem.setTitle(getString(R.string.sends, String.valueOf(selectPhotoList.size()), String.valueOf(maxPickSize)));
        }
    }

    private MenuItem menuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        menuItem = menu.findItem(R.id.ok);
        updateMenuItemTitle();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            if (selectPhotoList.isEmpty()) {
                Photo photo = photos.get(pos);
                selectPhotos.add(photo.getPath());
                selectPhotoList.add(photo);
                checkbox.setChecked(true);
            }

            Intent intent = new Intent();
            intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, selectPhotos);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            backTo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backTo() {
        Intent intent = new Intent();
        intent.putExtra("isBackPressed", true);
        intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, selectPhotos);
        intent.putExtra(PhotoPreviewConfig.EXTRA_ORIGINAL_PIC, radioButton.isChecked());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backTo();
        super.onBackPressed();
    }

    private boolean toolBarStatus = true;

    private void hideViews() {//隐藏toolbar
        toolBarStatus = false;
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        bottom_ll.animate().alpha(0.0f).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        toolBarStatus = true;
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        bottom_ll.animate().alpha(1.0f).setInterpolator(new DecelerateInterpolator(2));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        pos = position;
        position++;
        toolbar.setTitle(position + "/" + photos.size());
        if (selectPhotoList != null && selectPhotoList.contains(photos.get(pos))) {
            checkbox.setChecked(true);
            if (pos == 1 && selectPhotoList.contains(photos.get(pos - 1))) {
                checkbox.setChecked(true);
            }
        } else {
            checkbox.setChecked(false);
        }
        if (originalPicture) {
            Photo photo = photos.get(pos);
            radioButton.setText(getString(photo.isVideo() ? R.string.video_size : R.string.image_size, FileUtil.formatFileSize(photo.getSize())));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return photos == null ? 0 : photos.size();
        }

        @NonNull
        @Override
        public View instantiateItem(@NonNull ViewGroup container, final int position) {
            Photo photo = photos.get(position);
            float offsetW = 0.0f, offsetH = 0.0f;
            if (photo.getWidth() > 0 && photo.getHeight() > 0) {
                offsetW = (float) ((photo.getWidth() / photo.getHeight()) - (screenWith / screenHeight));
                offsetH = (float) ((photo.getHeight() / photo.getWidth()) - (screenHeight / screenWith));
            }
//            Log.e(TAG,"offsetW = " + offsetW + ",offsetH = " + offsetH);
            if (offsetW > 1.0f && !photo.isGif() && !photo.isWebp() && !photo.isVideo()) {//横向长图
                photos.get(position).setLongPhoto(true);
                SubsamplingScaleImageView subsamplingScaleImageView = loadLongPhoto(photo.getUri(), 0, screenHeight / photo.getHeight());
                container.addView(subsamplingScaleImageView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                return subsamplingScaleImageView;
            } else if (offsetH > 0.8f && !photo.isGif() && !photo.isWebp() && !photo.isVideo()) {//纵向长图
                photos.get(position).setLongPhoto(true);
                SubsamplingScaleImageView subsamplingScaleImageView = loadLongPhoto(photo.getUri(), 1);
                container.addView(subsamplingScaleImageView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                return subsamplingScaleImageView;
            } else {
                //不是长图，gif等
                PhotoView photoView = new PhotoView(container.getContext());
                photoView.setBackgroundColor(getResources().getColor(android.R.color.black));
                photoView.setOnViewTapListener(onViewTapListener);
                Glide.with(container.getContext()).load(photo.getUri()).error(R.mipmap.failure_image).into(photoView);

                if (photo.isVideo()) {
                    VideoPlayLayout videoPlayLayout = new VideoPlayLayout(container.getContext());
                    videoPlayLayout.setData(photoView, photo.getUri());
                    container.addView(videoPlayLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    return videoPlayLayout;
                }
                container.addView(photoView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                return photoView;
            }
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (photos != null && photos.size() > 0 && position < photos.size()) {
                Photo photo = photos.get(position);
                if (photo.isLongPhoto()) {
                    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) object;
                    imageView.recycle();
                }
                if (!photo.isVideo()) {
                    //清除缓存

                }
            }
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            try {
                super.finishUpdate(container);
            } catch (NullPointerException nullPointerException) {
                Log.d("finishUpdate", "Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
            }
        }
    }

    /**
     * 加载超长图
     */
    private SubsamplingScaleImageView loadLongPhoto(Uri uri, int orientation) {
        return loadLongPhoto(uri, orientation, 0);
    }

    /**
     * 加载超长图
     */
    private SubsamplingScaleImageView loadLongPhoto(Uri uri, int orientation, int hScale) {
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(this);
//        imageView.setDebug(true);
        imageView.setOnClickListener(onClickListener);
        imageView.setBackgroundColor(getResources().getColor(android.R.color.black));
        if (uri != null) {
            if (orientation == 1) {//纵向图
                imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                imageView.setImage(ImageSource.uri(uri), new ImageViewState(0, new PointF(0, 0), SubsamplingScaleImageView.ORIENTATION_0));
            } else {
//                imageView.setMaxScale(hScale);
                imageView.setImage(ImageSource.uri(uri));
            }
        } else {
            imageView.setImage(ImageSource.resource(R.mipmap.failure_image));
        }
        return imageView;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.image_pager_exit_animation);
    }

    @Override
    protected void onDestroy() {
        photos = null;
        selectPhotos = null;
        selectPhotoList = null;
        previewPhotos = null;
        onViewTapListener = null;
        onClickListener = null;
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(this);
            viewPager.setAdapter(null);
            viewPager = null;
        }
        super.onDestroy();
    }
}
