package com.awen.image.photopick.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.awen.image.photopick.util.AppPathUtil;
import com.awen.image.photopick.util.PermissionUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

/**
 * 图片选择器<br>
 * 还可以扩展更多，暂时没时间扩展了<br>
 * 使用方法：<br><code>
 * new PickConfig.Builder(this)<br>
 * .pickMode(PickConfig.MODE_MULTIP_PICK)<br>
 * .maxPickSize(30)<br>
 * .spanCount(3)<br>
 * .showCamera(false) //default true<br>
 * .clipPhoto(true)   //default false<br>
 * .build();<br>
 * </code>
 * Created by Awen <Awentljs@gmail.com>
 */
public class PhotoPickActivity extends ImageBaseActivity {
    private final String TAG = getClass().getSimpleName();
    public static final int REQUEST_CODE_CAMERA = 0;// 拍照
    public static final int REQUEST_CODE_CLIPIC = 1;//裁剪头像
    public static final int REQUEST_CODE_VIDEO = 2;//拍视频
    public static final int REQUEST_CODE_SYSTEM_CLI_IMAGE = 3;//系统裁剪头像
    public static final int REQUEST_CODE_PERMISSION_SD = 200;//获取sd卡读写权限
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 100;//获取拍照权限

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private PhotoGalleryAdapter galleryAdapter;
    private PhotoPickAdapter adapter;
    private PhotoPickBean pickBean;
    private String systemCliImagePath;//制定的裁剪后的图片要保存的路径所转成的图片路径
    private Uri systemCliImageUri;//制定的裁剪后的图片要保存的路径所转成的Uri
    private Uri cameraUri;
    private Uri cameraVideoUri;
    private static PhotoPickConfig.Builder.OnPhotoResultCallback onPhotoResultCallback;

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

//        onPhotoResultCallback = pickBean.getOnPhotoResultCallback();
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
        recyclerView.setAdapter(adapter);

        final RecyclerView gallery_rv = this.findViewById(R.id.gallery_rv);
        gallery_rv.setLayoutManager(new LinearLayoutManager(this));
        galleryAdapter = new PhotoGalleryAdapter(this);
        gallery_rv.setAdapter(galleryAdapter);

        adapter.setOnUpdateListener(new PhotoPickAdapter.OnUpdateListener() {
            @Override
            public void updataToolBarTitle(String title) {
                toolbar.setTitle(title);
            }

            @Override
            public void startSystemCrop(Uri uri) {
                setCropPath();
                callSystemImageCropper(uri);
            }
        });

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_CAMERA)
    private void selectPicFromCameraSuccess() {
        if (pickBean.getMediaType() == MediaType.ONLY_VIDEO) {//只显示视频，那就启动的是视频录像
            selectVideoFromCamera();
        } else {//拍照
            selectPicFromCamera();
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

    /**
     * 启动Camera录制视频
     */
    public void selectVideoFromCamera() {
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.cannot_take_pic, Toast.LENGTH_SHORT).show();
            return;
        }
        // 直接将拍到的视频存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        ContentValues values = new ContentValues();
        cameraVideoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraVideoUri);
        startActivityForResult(intent, REQUEST_CODE_VIDEO);
    }

    /**
     * 启动Camera拍照
     */
    public void selectPicFromCamera() {
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.cannot_take_pic, Toast.LENGTH_SHORT).show();
            return;
        }
        // 直接将拍到的照片存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        cameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
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
        switch (requestCode) {
            case REQUEST_CODE_CAMERA://相机拍照
                findPhotoOrVideo(cameraUri,0);
                break;
            case REQUEST_CODE_VIDEO://相机录视频
                findPhotoOrVideo(cameraVideoUri,1);
                break;
            case REQUEST_CODE_SYSTEM_CLI_IMAGE://系统裁剪
                cropBack(systemCliImagePath);
                break;
            case REQUEST_CODE_CLIPIC://头像裁剪
                if (data != null) {
                    String photoPath = data.getStringExtra(ClipPictureActivity.CLIPED_PHOTO_PATH);
                    cropBack(photoPath);
                } else {
                    Toast.makeText(this, R.string.unable_find_pic, Toast.LENGTH_LONG).show();
                }
                break;
            case PhotoPreviewConfig.REQUEST_CODE:
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
                break;
        }
    }

    private void cropBack(String path) {
        if (!TextUtils.isEmpty(path)) {
            ArrayList<String> pic = new ArrayList<>();
            pic.add(path);
            Intent intent = new Intent();
            intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, pic);
            setResult(Activity.RESULT_OK, intent);
            //因为图片裁剪是没有获取图片详情的，这里制造一个
            ArrayList<Photo> photos = new ArrayList<>(1);
            Photo photo = new Photo();
            photo.setPath(path);
            photos.add(photo);
//                        Toast.makeText(this, "已裁剪的图片地址 = \n" + photPath, Toast.LENGTH_LONG).show();
            onPhotoResultBack(pic, photos, false);
        } else {
            Toast.makeText(this, R.string.unable_find_pic, Toast.LENGTH_LONG).show();
        }
    }

    private void findPhotoOrVideo(Uri uri,int type) {
        String picturePath = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex("_data");
                picturePath = cursor.getString(columnIndex);
            } else {
                if (TextUtils.isEmpty(uri.getPath())) {
                    toastTakeMediaError();
                    return;
                }
                File file = new File(uri.getPath());
                if (!file.exists()) {
                    toastTakeMediaError();
                    return;
                }
                picturePath = uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (picturePath == null) {
            toastTakeMediaError();
        } else {
            if (pickBean.isClipPhoto()) {//拍完照之后，如果要启动头像裁剪，则去裁剪再吧地址传回来
                if(pickBean.isSystemClipPhoto()){//启动系统裁剪
                    setCropPath();
                    callSystemImageCropper(uri);
                    return;
                }
                adapter.startClipPic(picturePath, uri.toString());
            } else {//视频录制
                ArrayList<String> pic = new ArrayList<>(1);
                pic.add(picturePath);
                ArrayList<Photo> list = new ArrayList<>(1);
                Photo photo = new Photo();
                photo.setUri(uri.toString());
                photo.setPath(picturePath);
                if(type == 1) {
                    photo.setMimeType("video/mp4");
                }
                list.add(photo);
                Intent intent = new Intent();
                intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, pic);
                setResult(Activity.RESULT_OK, intent);
//                Toast.makeText(this, "已返回的拍照图片地址 = \n" + picturePath, Toast.LENGTH_LONG).show();
                onPhotoResultBack(pic, list, false);
            }
        }
    }

    private void setCropPath(){
        String photoName = String.valueOf(System.currentTimeMillis());
        systemCliImagePath = AppPathUtil.getClipPhotoPath() + photoName;
        systemCliImageUri = Uri.fromFile(new File(systemCliImagePath));
    }

    private void toastTakeMediaError() {
        Toast.makeText(this, (pickBean.getMediaType() == MediaType.ONLY_VIDEO) ?
                R.string.unable_find_pic : R.string.unable_find_pic, Toast.LENGTH_LONG).show();
    }

    /**
     * 数据的回传
     *
     * @param photos
     * @param originalPicture
     */
    private void onPhotoResultBack(ArrayList<String> photos, ArrayList<Photo> list, boolean originalPicture) {
        PhotoResultBean bean = new PhotoResultBean();
        bean.setOriginalPicture(originalPicture);
        bean.setPhotoLists(photos);
        bean.setList(list);
        if (onPhotoResultCallback != null) {
            onPhotoResultCallback.onResult(bean);
        }
        finish();
    }

    public static void setOnPhotoResultCallback(PhotoPickConfig.Builder.OnPhotoResultCallback onPhotoResultCallback) {
        PhotoPickActivity.onPhotoResultCallback = onPhotoResultCallback;
    }

    /**
     * 调用系统裁剪功能
     *
     * @param srcUri 原始图片的Uri
     */
    private void callSystemImageCropper(Uri srcUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, systemCliImageUri);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 400);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, REQUEST_CODE_SYSTEM_CLI_IMAGE);
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
