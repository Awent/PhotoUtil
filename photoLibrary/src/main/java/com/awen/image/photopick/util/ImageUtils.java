package com.awen.image.photopick.util;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.awen.image.PhotoSetting;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Awen <Awentljs@gmail.com>
 */
public class ImageUtils {

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 读取照片旋转角度
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /**
     * 按比例压缩图片
     *
     * @param path
     * @param w
     * @param h
     * @return
     */
    public static Bitmap getBitmap(String path, String uri, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        int angel = readPictureDegree(path);
        // opts.inJustDecodeBounds = true
        // ,设置该属性为true，不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你，
        // 这样就不会占用太多的内存，也就不会那么频繁的发生OOM了
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int test = 1;
        if (opts.outWidth > opts.outHeight) {
            if (opts.outWidth >= w)
                test = opts.outWidth / w;
            opts.inSampleSize = test; // 限制处理后的图片最大宽为w*2
        } else {
            if (opts.outHeight >= h)
                test = opts.outHeight / h;
            opts.inSampleSize = test; // 限制处理后的图片最大高为h*2
        }
        opts.inJustDecodeBounds = false;
        if (SDKVersionUtil.isAndroid_Q()) {
            return rotaingImageView(angel, getBitmapByUri(uri, opts));
        } else {
            return rotaingImageView(angel, BitmapFactory.decodeFile(path, opts));
        }
    }

    public static Bitmap getBitmapByUri(String uri) {
        return getBitmapByUri(uri, null);
    }

    public static Bitmap getBitmapByUri(String uri, BitmapFactory.Options opts) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    PhotoSetting.getContext().getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
            if (parcelFileDescriptor == null) {
                return null;
            }
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            if (opts != null) {
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);
            }
            return BitmapFactory.decodeFileDescriptor(fileDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 保存图片
     *
     * @param bmp
     * @return
     */
    public static boolean saveImage2(String filePath, Bitmap bmp) {
        if (bmp == null)
            return false;
        File f = new File(filePath);
        if (f.exists()) {
            f.delete(); // 删除原图片
        }
        if (!f.isFile()) {
            String dir = filePath.substring(0, filePath.lastIndexOf("/"));
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    return false;
                }
            }
            FileOutputStream fOut = null;
            boolean isSuccesse = false;
            try {
                f.createNewFile();
                fOut = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                isSuccesse = true;
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (fOut != null) {
                    try {
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return isSuccesse;
        }
        return false;
    }

    /**
     * 保存图片到公告存储目录 Android Q
     *
     * @param fileName
     * @param b
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri saveImageQ(String fileName, byte[] b) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, getImageMimeType(fileName));
        Uri uri = PhotoSetting.getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        OutputStream outputStream = null;
        try {
            if (uri == null) {
                return null;
            }
            outputStream = PhotoSetting.getContext().getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                return null;
            }
            outputStream.write(b);
            outputStream.flush();
            return uri;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 保存图片，并且可以再手机的图库中看到
     *
     * @param filePath 图片的本地保存路径，包括图片名
     * @return true:保存成功，false:保存失败
     */
    public static boolean saveImageToGallery(String filePath, String fileName, byte[] b) {
        if (b == null)
            return false;
        filePath = filePath + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete(); // 删除原图片
        }
        String dir;
        if (!file.isFile()) {
            dir = filePath.substring(0, filePath.lastIndexOf("/"));
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    return false;
                }
            }
            FileOutputStream fOut = null;
            boolean isSuccesse = false;
            try {
                file.createNewFile();
                fOut = new FileOutputStream(file);
                fOut.write(b);
                fOut.flush();
                // 最后通知图库更新
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                PhotoSetting.getContext().sendBroadcast(intent);
                isSuccesse = true;
            } catch (Exception e1) {
                if (file.exists()) {
                    file.delete(); // 删除原图片
                }
                e1.printStackTrace();
            } finally {
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return isSuccesse;
        }
        return false;
    }

    public static String getImageMimeType(String fileName) {
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith("jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else if (fileName.endsWith(".png")) {
            return "image/jpeg";
        }
        return "image/jpeg";
    }

}
