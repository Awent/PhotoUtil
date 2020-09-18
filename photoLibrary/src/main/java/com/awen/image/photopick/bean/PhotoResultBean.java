package com.awen.image.photopick.bean;

import java.util.ArrayList;

/**
 * 从图库选择了或裁剪了的图片，视频
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoResultBean {
    private boolean isOriginalPicture;//用户选择的是否是原图
    /**
     * 这个只是单纯得到用户选择的图片或视频地址path，如果是android Q,需要通过此path获取到Uri在进行其他操作
     */
    private ArrayList<String> photoLists;

    /**
     * 包含所选图片或视频的详细信息
     * 注意：除了拍照和截图的没有详细信息，只有一个path
     * @see Photo
     */
    private ArrayList<Photo> list;

    public ArrayList<Photo> getList() {
        return list;
    }

    public void setList(ArrayList<Photo> list) {
        this.list = list;
    }

    public boolean isOriginalPicture() {
        return isOriginalPicture;
    }

    public void setOriginalPicture(boolean originalPicture) {
        isOriginalPicture = originalPicture;
    }

    public ArrayList<String> getPhotoLists() {
        return photoLists;
    }

    public void setPhotoLists(ArrayList<String> photoLists) {
        this.photoLists = photoLists;
    }
}
