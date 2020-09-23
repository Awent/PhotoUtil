package com.awen.image.photopick.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awen.image.ImageBaseActivity;
import com.awen.image.R;
import com.awen.image.photopick.adapter.PhotoGalleryAdapter;
import com.awen.image.photopick.adapter.PhotoPickAdapter;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoDirectory;
import com.awen.image.photopick.bean.PhotoPickBean;
import com.awen.image.photopick.bean.PhotoResultBean;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.controller.PhotoPreviewConfig;
import com.awen.image.photopick.loader.MediaStoreHelper;
import com.awen.image.photopick.loader.MediaType;
import com.awen.image.photopick.util.PermissionUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

/**
 * 图片选择器<br>
 * 使用方法：<br><code>
 * new PickConfig.Builder(this)<br>
 * .pickMode(PickConfig.MODE_MULTIP_PICK)<br>
 * .maxPickSize(30)<br>
 * .spanCount(3)<br>
 * .showCamera(false) //default true<br>
 * .clipPhoto()   //default false<br>
 * .build();<br>
 * </code>
 * Created by Awen <Awentljs@gmail.com>
 */
public class PhotoPickActivity extends ImageBaseActivity implements CameraProxy.OnCameraProxyCallBack, PhotoPickAdapter.OnUpdateListener {
    private final String TAG = getClass().getSimpleName();
    private final int REQUEST_CODE_PERMISSION_SD = 200;//获取sd卡读写权限
    private final int REQUEST_CODE_PERMISSION_CAMERA = 100;//获取拍照权限

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private PhotoGalleryAdapter galleryAdapter;
    private PhotoPickAdapter adapter;
    private PhotoPickBean pickBean;
    private static PhotoPickConfig.Builder.OnPhotoResultCallback onPhotoResultCallback;
    private CameraProxy cameraProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick);
        //you can set some configs in bundle
        Bundle bundle = getIntent().getBundleExtra(PhotoPickConfig.EXTRA_PICK_BUNDLE);
        if (bundle == null) {
            throw new NullPointerException("bundle is null,please init it");
        }
        pickBean = bundle.getParcelable(PhotoPickConfig.EXTRA_PICK_BEAN);
        if (pickBean == null) {
            finish();
            return;
        }

        //以下操作会回调这两个方法:#startPermissionSDSuccess(), #startPermissionSDFaild()
        PermissionGen.needPermission(this, REQUEST_CODE_PERMISSION_SD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void startInit() {
        if (pickBean.getMediaType() == MediaType.ONLY_VIDEO) {
            toolbar.setTitle(R.string.video);
        } else if (pickBean.getMediaType() == MediaType.ONLY_IMAGE) {
            toolbar.setTitle(R.string.photo);
        } else {
            toolbar.setTitle(R.string.select_photo);
        }

        final RecyclerView recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, pickBean.getSpanCount()));
        adapter = new PhotoPickAdapter(this, pickBean);
        adapter.setOnUpdateListener(this);
        recyclerView.setAdapter(adapter);

        final RecyclerView gallery_rv = this.findViewById(R.id.gallery_rv);
        gallery_rv.setLayoutManager(new LinearLayoutManager(this));
        galleryAdapter = new PhotoGalleryAdapter(this);
        gallery_rv.setAdapter(galleryAdapter);

        galleryAdapter.setOnItemClickListener(new PhotoGalleryAdapter.OnItemClickListener() {
            @Override
            public void onClick(List<Photo> photos) {
                if (adapter != null) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    adapter.refresh(photos);
                }
            }
        });
        MediaStoreHelper.getPhotoDirs(this, pickBean, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(final List<PhotoDirectory> directories) {
                if (directories.isEmpty()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.refresh(directories.get(0).getPhotos());
                        galleryAdapter.refresh(directories);
                    }
                });

            }
        });

        slidingUpPanelLayout = this.findViewById(R.id.slidingUpPanelLayout);
        slidingUpPanelLayout.setAnchorPoint(0.5f);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
        slidingUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        cameraProxy = new CameraProxy(this, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_CAMERA)
    private void selectPicFromCameraSuccess() {
        if (pickBean.getMediaType() == MediaType.ONLY_VIDEO) {//只显示视频，那就启动的是视频录像
            cameraProxy.selectVideoFromCamera();
        } else {//拍照
            cameraProxy.selectPicFromCamera();
        }
    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_CAMERA)
    private void selectPicFromCameraFailed() {
//        Log.e(TAG, "selectPicFromCameraFailed");
        PermissionUtil.showSystemSettingDialog(this, getString(R.string.permission_tip_camera));
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_SD)
    private void startPermissionSDSuccess() {
        startInit();
//        Log.e(TAG, "startPermissionSuccess");
    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_SD)
    private void startPermissionSDFailed() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_tip_SD))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PermissionUtil.startSystemSettingActivity(PhotoPickActivity.this);
                finish();
            }
        }).setCancelable(false).show();
//        Log.e(TAG, "startPermissionFailed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!pickBean.isClipPhoto()) {
            getMenuInflater().inflate(R.menu.menu_pick, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            if (adapter != null) {
                ArrayList<String> photos = adapter.getSelectPhotos();
                if (photos != null && !photos.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, photos);
                    setResult(Activity.RESULT_OK, intent);
//                String s = "已选择的图片大小 = " + adapter.getSelectPhotos().size() + "\n" + adapter.getSelectPhotos().toString();
//                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                    onPhotoResultBack(photos, adapter.getSelectPhotoList(), false);
                }
            }
            return true;
        } else if (item.getItemId() == R.id.preview) {//图片预览
            if (adapter != null) {
                ArrayList<String> photos = adapter.getSelectPhotos();
                if (photos != null && !photos.isEmpty()) {
                    new PhotoPreviewConfig.Builder(this)
                            .setMaxPickSize(pickBean.getMaxPickSize())
                            .setOriginalPicture(pickBean.isOriginalPicture())
                            .setPreview(true)
                            .build();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PhotoPreviewConfig.REQUEST_CODE) {
            boolean isBackPressed = data.getBooleanExtra("isBackPressed", false);
            if (!isBackPressed) {//如果上个activity不是按了返回键的，就是按了"发送"按钮
                setResult(Activity.RESULT_OK, data);
                ArrayList<String> photoLists = data.getStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST);
                boolean isOriginalPicture = data.getBooleanExtra(PhotoPreviewConfig.EXTRA_ORIGINAL_PIC, false);
                onPhotoResultBack(photoLists, adapter.getSelectPhotoList(), isOriginalPicture);
            } else {//用户按了返回键，合并用户选择的图片集合
                adapter.notifyDataSetChanged();
                toolbar.setTitle(adapter.getTitle());
            }
        } else {
            cameraProxy.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 数据的回传
     *
     * @param photos
     * @param originalPicture
     */
    @Override
    public void onPhotoResultBack(ArrayList<String> photos, ArrayList<Photo> list, boolean originalPicture) {
        PhotoResultBean bean = new PhotoResultBean();
        bean.setOriginalPicture(originalPicture);
        bean.setPhotoLists(photos);
        bean.setList(list);
        if (onPhotoResultCallback != null) {
            onPhotoResultCallback.onResult(bean);
        }
        finish();
    }

    @Override
    public int getMediaType() {
        return pickBean.getMediaType();
    }

    @Override
    public boolean isClipPhoto() {
        return pickBean.isClipPhoto();
    }

    @Override
    public boolean isSystemClipPhoto() {
        return pickBean.isSystemClipPhoto();
    }

    @Override
    public void updateToolBarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void startSystemCrop(Uri uri) {
        cameraProxy.callSystemImageCropper(uri);
    }

    @Override
    public void startClipPic(String path, String uri) {
        cameraProxy.startClipPic(path, uri);
    }

    @Override
    public void requestCameraPermission() {
        //以下操作会回调Activity中的#selectPicFromCameraSuccess()或selectPicFromCameraFailed()
        PermissionGen.needPermission(this, REQUEST_CODE_PERMISSION_CAMERA, Manifest.permission.CAMERA);
    }

    public static void setOnPhotoResultCallback(PhotoPickConfig.Builder.OnPhotoResultCallback onPhotoResultCallback) {
        PhotoPickActivity.onPhotoResultCallback = onPhotoResultCallback;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.bottom_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.destroy();
            adapter = null;
        }

        if (galleryAdapter != null) {
            galleryAdapter.destroy();
            galleryAdapter = null;
        }
        onPhotoResultCallback = null;
        slidingUpPanelLayout = null;
    }

}
