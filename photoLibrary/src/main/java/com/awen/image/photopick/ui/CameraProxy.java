package com.awen.image.photopick.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.awen.image.R;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.loader.MediaType;
import com.awen.image.photopick.util.AppPathUtil;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * 负责拍照、视频录制、图片裁剪
 *
 * @see #selectPicFromCamera()
 * @see #selectVideoFromCamera()
 * @see #callSystemImageCropper(Uri)
 * @see #onActivityResult(int, int, Intent)
 *
 */
public class CameraProxy {

    private final int REQUEST_CODE_CAMERA = 0;// 拍照
    private final int REQUEST_CODE_CLIPIC = 1;//裁剪头像
    private final int REQUEST_CODE_VIDEO = 2;//拍视频
    private final int REQUEST_CODE_SYSTEM_CLI_IMAGE = 3;//系统裁剪头像
    private Activity context;
    private String systemCliImagePath;//制定的裁剪后的图片要保存的路径所转成的图片路径
    private Uri systemCliImageUri;//制定的裁剪后的图片要保存的路径所转成的Uri
    private Uri cameraUri;
    private Uri cameraVideoUri;
    private OnCameraProxyCallBack callBack;

    public CameraProxy(Activity context, OnCameraProxyCallBack onCameraProxyCallBack) {
        this.context = context;
        this.callBack = onCameraProxyCallBack;
        if (callBack == null) {
            throw new NullPointerException("you must implements OnCameraProxyCallBack ");
        }
    }

    /**
     * 启动Camera录制视频
     */
    public void selectVideoFromCamera() {
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, R.string.cannot_take_pic, Toast.LENGTH_SHORT).show();
            return;
        }
        // 直接将拍到的视频存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        ContentValues values = new ContentValues();
        cameraVideoUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraVideoUri);
        context.startActivityForResult(intent, REQUEST_CODE_VIDEO);
    }

    /**
     * 启动Camera拍照
     */
    public void selectPicFromCamera() {
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, R.string.cannot_take_pic, Toast.LENGTH_SHORT).show();
            return;
        }
        // 直接将拍到的照片存到手机默认的文件夹
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        cameraUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        context.startActivityForResult(intent, REQUEST_CODE_CAMERA);
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
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 400);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", false);
        context.startActivityForResult(intent, REQUEST_CODE_SYSTEM_CLI_IMAGE);
    }

    public void startClipPic(String path, String uri) {
        Intent intent = new Intent(context, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_PATH, path);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_URI, uri);
        context.startActivityForResult(intent, REQUEST_CODE_CLIPIC);
        context.overridePendingTransition(R.anim.bottom_in, 0);
    }

    public interface OnCameraProxyCallBack {

        /**
         * 图片回调
         * @param photos
         * @param list
         * @param originalPicture
         */
        void onPhotoResultBack(ArrayList<String> photos, ArrayList<Photo> list, boolean originalPicture);

        /**
         * @see MediaType
         * @return
         */
        int getMediaType();

        /**
         * 是否是图片裁剪
         * @return
         */
        boolean isClipPhoto();

        /**
         * 是否启动系统图片裁剪
         * @return
         */
        boolean isSystemClipPhoto();

    }
}
