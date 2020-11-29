package com.awen.image.photopick.loader;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import com.awen.image.photopick.bean.PhotoPickBean;
import com.awen.image.photopick.data.Data;
import com.awen.image.photopick.bean.PhotoDirectory;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Awen <Awentljs@gmail.com>
 */
public class MediaStoreHelper {

    /**
     * 查询图片和视频
     *
     * @param context       Activity
     * @param resultCallback PhotosResultCallback
     */
    public static void getPhotoDirs(final Activity context,final PhotoPickBean config,final PhotosResultCallback resultCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = context.getContentResolver();
                PhotoDirectory videoDir = null;
                List<PhotoDirectory> directories = null;
                int mediaType = config.getMediaType();
                if (mediaType != MediaType.ONLY_IMAGE) {//不是只显示图片，要查询视频
                    //视频
                    VideoCursorLoader vLoader = new VideoCursorLoader();
                    Cursor vCursor = contentResolver.query(vLoader.getUri(),
                            vLoader.getProjection(),
                            vLoader.getSelection(),
                            vLoader.getSelectionArgs(),
                            vLoader.getSortOrder());
                    if (vCursor != null) {
                        videoDir = Data.getDataFromVideoCursor(context, vCursor);
                        vCursor.close();
                    }
                }

                if (mediaType != MediaType.ONLY_VIDEO) {//不是只显示视频，要查询图片
                    //图片
                    PhotoCursorLoader loader = new PhotoCursorLoader();
                    loader.setShowGif(config.isShowGif());
                    Cursor cursor = contentResolver.query(loader.getUri(),
                            loader.getProjection(),
                            loader.getSelection(),
                            loader.getSelectionArgs(),
                            loader.getSortOrder());
                    if (cursor != null) {
                        directories = Data.getDataFromCursor(context, cursor);
                        cursor.close();
                    }
                }

                if (directories == null) {
                    directories = new ArrayList<>();
                }
                if (videoDir != null && videoDir.getPhotos().size() > 0) {
                    if (directories.size() > 0) {
                        directories.add(1, videoDir);
                        Data.mergePhotoAndVideo(context, directories.get(0), videoDir);
                    } else {
                        directories.add(videoDir);
                    }
                }
                if (resultCallback != null) {
                    resultCallback.onResultCallback(directories);
                }
            }
        }).start();
    }

    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }

}
