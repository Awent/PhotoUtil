package com.awen.image.photopick.bean;

import java.util.ArrayList;

/**
 * 从图库选择了或裁剪了的图片
 * Created by Awen <Awentljs@gmail.com>
 */

public class PhotoResultBean {
    private boolean isOriginalPicture;//用户选择的是否是原图
    private ArrayList<String> photoLists;
    private ArrayList<Photo> list;//android Q之后使用这个来获取用户选择的图片或视频

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
