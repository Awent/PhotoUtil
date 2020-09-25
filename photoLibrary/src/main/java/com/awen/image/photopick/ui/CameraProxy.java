package com.awen.image.photopick.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.awen.image.R;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.controller.CameraOptions;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.loader.MediaType;
import com.awen.image.photopick.re.AcResultUtil;
import com.awen.image.photopick.re.OnActivityResultCallBack;
import com.awen.image.photopick.util.AppPathUtil;
import com.awen.image.photopick.util.PermissionUtil;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.awen.image.PhotoSetting.DEBUG;

/**
 * 负责拍照、视频录制、图片裁剪
 *
 * @see #selectPicFromCamera()
 * @see #selectVideoFromCamera()
 * @see #callSystemImageCropper(Uri)
 * @see #onActivityResult(int, int, Intent)
 */
public class CameraProxy {

    private final int REQUEST_CODE_CAMERA = 0;// 拍照
    private final int REQUEST_CODE_CLIPIC = 1;//裁剪头像
    private final int REQUEST_CODE_VIDEO = 2;//拍视频
    private final int REQUEST_CODE_SYSTEM_CLI_IMAGE = 3;//系统裁剪头像
    private FragmentActivity context;
    private String systemCliImagePath;//制定的裁剪后的图片要保存的路径所转成的图片路径
    private Uri systemCliImageUri;//制定的裁剪后的图片要保存的路径所转成的Uri
    private Uri cameraUri;
    private Uri cameraVideoUri;
    private OnCameraProxyCallBack callBack;
    private CameraOptions options;
    private boolean isOnlyUseCamera;//是否只是启动相机拍照或相机录视频
    private AcResultUtil acResultUtil;

    public CameraProxy(@NonNull FragmentActivity context, @NonNull OnCameraProxyCallBack onCameraProxyCallBack) {
        this(context, onCameraProxyCallBack, null);
    }

    public CameraProxy(@NonNull FragmentActivity context, @NonNull OnCameraProxyCallBack onCameraProxyCallBack, CameraOptions options) {
        this(context, onCameraProxyCallBack, options, false);
    }

    public CameraProxy(@NonNull final FragmentActivity context, @NonNull OnCameraProxyCallBack onCameraProxyCallBack, CameraOptions ops, boolean isOnlyUseCamera) {
        this.context = context;
        this.callBack = onCameraProxyCallBack;
        this.options = ops;
        if (options == null) {
            this.options = new CameraOptions();
        }
        this.isOnlyUseCamera = isOnlyUseCamera;
        if (isOnlyUseCamera) {//只是启动系统相机
            acResultUtil = new AcResultUtil(context,new OnActivityResultCallBack() {
                @Override
                public void onActivityResultX(int requestCode, int resultCode, @Nullable Intent data) {
                    onActivityResult(requestCode, resultCode, data);
                }

                @Override
                public void requestCameraPermissionSuccess() {
//                    acResultUtil.getFragment().startAcForResult();
                }

                @Override
                public void requestCameraPermissionFailed() {
                    PermissionUtil.showSystemSettingDialog(context, context.getString(R.string.permission_tip_camera));
                }
            });
        }
    }

    /**
     * 启动Camera录制视频
     */
    public void selectVideoFromCamera() {
        if (!isSdCardExit()) {
            return;
        }
        // 直接将拍到的视频存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (options.getVideoMaxSize() > 0) {
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, options.getVideoMaxSize());//限制录制大小(10M=10 * 1024 * 1024L)
        }
        if (options.getVideoDuration() > 0) {
            //很多国产机中这个参数无效
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, options.getVideoDuration());
        }
        ContentValues values = new ContentValues();
        cameraVideoUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraVideoUri);
        startAc(intent, REQUEST_CODE_VIDEO);
    }

    /**
     * 启动Camera拍照
     */
    public void selectPicFromCamera() {
        if (!isSdCardExit()) {
            return;
        }
        // 直接将拍到的照片存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        cameraUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startAc(intent, REQUEST_CODE_CAMERA);
    }

    private boolean isSdCardExit() {
        if (!AppPathUtil.isSdCardExit()) {
            Toast.makeText(context, R.string.cannot_take_pic, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CAMERA://相机拍照
                findPhotoOrVideo(cameraUri, 0);
                break;
            case REQUEST_CODE_VIDEO://相机录视频
                findPhotoOrVideo(cameraVideoUri, 1);
                break;
            case REQUEST_CODE_SYSTEM_CLI_IMAGE://系统裁剪
                cropBack(systemCliImagePath);
                break;
            case REQUEST_CODE_CLIPIC://头像裁剪
                if (data != null) {
                    String photoPath = data.getStringExtra(ClipPictureActivity.CLIPED_PHOTO_PATH);
                    cropBack(photoPath);
                } else {
                    Toast.makeText(context, R.string.unable_find_pic, Toast.LENGTH_LONG).show();
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
            context.setResult(Activity.RESULT_OK, intent);
            //因为图片裁剪是没有获取图片详情的，这里制造一个
            ArrayList<Photo> photos = new ArrayList<>(1);
            Photo photo = new Photo();
            photo.setPath(path);
            photos.add(photo);
//                        Toast.makeText(this, "已裁剪的图片地址 = \n" + photPath, Toast.LENGTH_LONG).show();
            callBack.onPhotoResultBack(pic, photos, false);
        } else {
            Toast.makeText(context, R.string.unable_find_pic, Toast.LENGTH_LONG).show();
        }
    }

    private void findPhotoOrVideo(Uri uri, int type) {
        String picturePath = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
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
            if (callBack.isClipPhoto()) {//拍完照之后，如果要启动头像裁剪，则去裁剪再吧地址传回来
                if (callBack.isSystemClipPhoto()) {//启动系统裁剪
                    setCropPath();
                    callSystemImageCropper(uri);
                    return;
                }
                startClipPic(picturePath, uri.toString());
            } else {
                ArrayList<String> pic = new ArrayList<>(1);
                pic.add(picturePath);
                ArrayList<Photo> list = new ArrayList<>(1);
                Photo photo = new Photo();
                photo.setUri(uri.toString());
                photo.setPath(picturePath);
                if (type == 1) {
                    photo.setMimeType("video/mp4");
                }
                list.add(photo);
                Intent intent = new Intent();
                intent.putStringArrayListExtra(PhotoPickConfig.EXTRA_STRING_ARRAYLIST, pic);
                context.setResult(Activity.RESULT_OK, intent);
//                Toast.makeText(this, "已返回的拍照图片地址 = \n" + picturePath, Toast.LENGTH_LONG).show();
                callBack.onPhotoResultBack(pic, list, false);
            }
        }
    }

    private void toastTakeMediaError() {
        Toast.makeText(context, (callBack.getMediaType() == MediaType.ONLY_VIDEO) ?
                R.string.unable_find_pic : R.string.unable_find_pic, Toast.LENGTH_LONG).show();
    }

    private void setCropPath() {
        String photoName = String.valueOf(System.currentTimeMillis());
        systemCliImagePath = AppPathUtil.getClipPhotoPath() + photoName;
        systemCliImageUri = Uri.fromFile(new File(systemCliImagePath));
    }

    /**
     * 调用系统裁剪功能
     *
     * @param srcUri 原始图片的Uri
     */
    public void callSystemImageCropper(Uri srcUri) {
        setCropPath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, systemCliImageUri);
        intent.putExtra("aspectX", options.getAspectX());
        intent.putExtra("aspectY", options.getAspectY());
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
        intent.putExtra("outputX", options.getOutputX());
        intent.putExtra("outputY", options.getOutputY());
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", false);
        startAc(intent, REQUEST_CODE_SYSTEM_CLI_IMAGE);
    }

    public void startClipPic(String path, String uri) {
        Intent intent = new Intent(context, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_PATH, path);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_URI, uri);
        startAc(intent, REQUEST_CODE_CLIPIC);
    }

    private void startAc(Intent intent, int requestCode) {
        if (isOnlyUseCamera) {
            acResultUtil.startAcResult(intent, requestCode);
        } else {
            context.startActivityForResult(intent, requestCode);
            if (requestCode == REQUEST_CODE_CLIPIC) {
                context.overridePendingTransition(R.anim.bottom_in, 0);
            }
        }
    }

    public interface OnCameraProxyCallBack {

        /**
         * 图片回调
         *
         * @param photos
         * @param list
         * @param originalPicture
         */
        void onPhotoResultBack(ArrayList<String> photos, ArrayList<Photo> list, boolean originalPicture);

        /**
         * @return
         * @see MediaType
         */
        int getMediaType();

        /**
         * 是否是图片裁剪
         *
         * @return
         */
        boolean isClipPhoto();

        /**
         * 是否启动系统图片裁剪
         *
         * @return
         */
        boolean isSystemClipPhoto();

    }
}
