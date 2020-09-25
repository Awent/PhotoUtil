package com.awen.image.photopick.controller;

public class CameraOptions {

    private long videoMaxSize;//限制录制大小(10M = 10 * 1024 * 1024L)，默认0，不限制
    private long videoDuration;//限制录制时间(秒)，默认0，不限制

    private int aspectX = 1;//裁剪框 X 比值
    private int aspectY = 1;//裁剪框 Y 比值
    private int outputX = 400;//裁剪后输出宽度
    private int outputY = 400;//裁剪后输出高度

    private boolean clipPhoto; //是否启动裁剪图片
    private boolean systemClipPhoto; //是否启动系统裁剪图片
    private int mediaType;//默认0，显示所有图片跟视频，1：只显示图片，2：只显示视频

    public long getVideoMaxSize() {
        return videoMaxSize;
    }

    public void setVideoMaxSize(long videoMaxSize) {
        this.videoMaxSize = videoMaxSize;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public int getAspectX() {
        return aspectX;
    }

    public void setAspectX(int aspectX) {
        this.aspectX = aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public void setAspectY(int aspectY) {
        this.aspectY = aspectY;
    }

    public int getOutputX() {
        return outputX;
    }

    public void setOutputX(int outputX) {
        this.outputX = outputX;
    }

    public int getOutputY() {
        return outputY;
    }

    public void setOutputY(int outputY) {
        this.outputY = outputY;
    }

    public boolean isClipPhoto() {
        return clipPhoto;
    }

    public void setClipPhoto(boolean clipPhoto) {
        this.clipPhoto = clipPhoto;
    }

    public boolean isSystemClipPhoto() {
        return systemClipPhoto;
    }

    public void setSystemClipPhoto(boolean systemClipPhoto) {
        this.systemClipPhoto = systemClipPhoto;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }
}
