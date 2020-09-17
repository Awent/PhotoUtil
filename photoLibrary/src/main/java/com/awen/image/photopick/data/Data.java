package com.awen.image.photopick.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.awen.image.R;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoDirectory;
import com.awen.image.photopick.util.SDKVersionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DURATION;
import static android.provider.MediaStore.MediaColumns.HEIGHT;
import static android.provider.MediaStore.MediaColumns.RELATIVE_PATH;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.WIDTH;

/**
 * Created by Awen <Awentljs@gmail.com>
 */
public class Data {
    public final static int INDEX_ALL_PHOTOS = 0;

    /**
     * 获取所有图片
     */
    public static List<PhotoDirectory> getDataFromCursor(Context context, Cursor data) {
        List<PhotoDirectory> directories = new ArrayList<>();
        PhotoDirectory photoDirectoryAll = new PhotoDirectory();
        photoDirectoryAll.setName(context.getString(R.string.all_photo));
        photoDirectoryAll.setId("ALL");

        while (data.moveToNext()) {
            int id = data.getInt(data.getColumnIndexOrThrow(_ID));
            String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
            String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
            String path = data.getString(data.getColumnIndexOrThrow(SDKVersionUtil.isAndroid_Q() ? RELATIVE_PATH : DATA));
            long size = data.getLong(data.getColumnIndexOrThrow(SIZE));
            String mimeType = data.getString(data.getColumnIndexOrThrow(MIME_TYPE));
            int width = data.getInt(data.getColumnIndexOrThrow(WIDTH));
            int height = data.getInt(data.getColumnIndexOrThrow(HEIGHT));
            long dateAdd = data.getLong(data.getColumnIndexOrThrow(DATE_ADDED));
            Photo photo = new Photo();

            if (size <= 0) {
                continue;
            }
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            photo.setId(id);
            photo.setUri(uri);
            photo.setPath(path);
            photo.setSize(size);
            photo.setMimeType(mimeType);
            photo.setWidth(width);
            photo.setHeight(height);
            photo.setDateAdd(dateAdd);

            PhotoDirectory photoDirectory = new PhotoDirectory();
            photoDirectory.setId(bucketId);
            photoDirectory.setName(name);
            if (!directories.contains(photoDirectory)) {
                photoDirectory.setCoverPath(path);
                photoDirectory.addPhoto(photo);
                photoDirectory.setUri(uri);
                photoDirectory.setDateAdded(dateAdd);
                directories.add(photoDirectory);
            } else {
                directories.get(directories.indexOf(photoDirectory)).addPhoto(photo);
            }
            photoDirectoryAll.addPhoto(photo);
        }
        if (photoDirectoryAll.getPhotos().size() > 0) {
            Photo p = photoDirectoryAll.getPhotos().get(0);
            photoDirectoryAll.setCoverPath(p.getPath());
            photoDirectoryAll.setUri(p.getUri());
        }
        photoDirectoryAll.setDateAdded(new Date().getTime());
        directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);
        return directories;
    }

    /**
     * 获取所有视频，存到一个文件夹bean里面
     */
    public static PhotoDirectory getDataFromVideoCursor(Context context, Cursor data) {
        PhotoDirectory videoDir = new PhotoDirectory();
        videoDir.setName(context.getString(R.string.all_video));
        videoDir.setId("ALL_VIDEO");

        while (data.moveToNext()) {
            int id = data.getInt(data.getColumnIndexOrThrow(_ID));
            String path = data.getString(data.getColumnIndexOrThrow(SDKVersionUtil.isAndroid_Q() ? RELATIVE_PATH : DATA));
            long size = data.getLong(data.getColumnIndexOrThrow(SIZE));
            String mimeType = data.getString(data.getColumnIndexOrThrow(MIME_TYPE));
            int width = data.getInt(data.getColumnIndexOrThrow(WIDTH));
            int height = data.getInt(data.getColumnIndexOrThrow(HEIGHT));
            long dateAdd = data.getLong(data.getColumnIndexOrThrow(DATE_ADDED));
            long duration = data.getLong(data.getColumnIndexOrThrow(DURATION));

            Photo photo = new Photo();

            if (size <= 0) {
                continue;
            }
            Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            photo.setId(id);
            photo.setUri(uri);
            photo.setPath(path);
            photo.setSize(size);
            photo.setMimeType(mimeType);
            photo.setWidth(width);
            photo.setHeight(height);
            photo.setDateAdd(dateAdd);
            photo.setDuration(duration);

            if (TextUtils.isEmpty(videoDir.getCoverPath())) {
                videoDir.setCoverPath(path);
                videoDir.setUri(uri);
            }
            videoDir.addPhoto(photo);
        }
        videoDir.setDateAdded(new Date().getTime());
        videoDir.setVideo(true);
        return videoDir;
    }

    /**
     * 合并图片跟视频
     * @param parent 所有图片
     * @param child 所有视频
     */
    public static void mergePhotoAndVideo(Context context,PhotoDirectory parent, PhotoDirectory child) {
        parent.setName(context.getString(R.string.select_photo));
        List<Photo> list = child.getPhotos();
        for (Photo photo : list) {
            parent.addPhoto(photo);
        }
        sort(parent.getPhotos());
    }

    /**
     * 把合并后的图片视频进行时间排序
     * @param list
     */
    public static void sort(List<Photo> list) {
        Collections.sort(list, new Comparator<Photo>() {
            @Override
            public int compare(Photo o1, Photo o2) {
                try {
                    if (o1.getDateAdd() < o2.getDateAdd()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    public static String getRealVideoPath(int id){
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
    }

    public static String getRealImagePath(int id){
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
    }
}
