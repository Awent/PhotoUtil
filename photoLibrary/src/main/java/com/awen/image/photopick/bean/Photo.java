package com.awen.image.photopick.bean;

import android.net.Uri;

/**
 * 注意：如果是图片裁剪，这里只有{@link #path} 不为空，其他字段都是没有值的
 * Created by Awen <Awentljs@gmail.com>
 */
public class Photo {

    private int id;
    private Uri uri;
    private String path;
    private long size;//byte 字节
    private boolean isLongPhoto;//是否是超长图
    private int width; //图片真实宽度
    private int height;//图片真实高度
    private String mimeType;//图片类型：image/webp、image/jpeg、image/png、image/gif
    private long dateAdd;//添加的时间,用作视频跟图片组合后的排序
    private long duration;//视频的时长，毫秒

    public Photo(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;

        Photo photo = (Photo) o;

        return id == photo.id;
    }


    @Override
    public int hashCode() {
        return id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isLongPhoto() {
        return isLongPhoto;
    }

    public void setLongPhoto(boolean longPhoto) {
        isLongPhoto = longPhoto;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(long dateAdd) {
        this.dateAdd = dateAdd;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isVideo(){
        return mimeType.startsWith("video");
    }

    /**
     * 是否是gif图片
     * @return true代表是gif
     */
    public boolean isGif(){
        return mimeType != null && mimeType.equals("image/gif");
    }

    /**
     * 是否是webp图片
     * @return true代表是webp
     */
    public boolean isWebp(){
        return mimeType != null && mimeType.equals("image/webp");
    }
}
