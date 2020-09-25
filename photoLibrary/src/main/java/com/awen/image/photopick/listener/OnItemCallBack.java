package com.awen.image.photopick.listener;

import androidx.annotation.NonNull;

import com.awen.image.photopick.controller.PhotoPagerConfig;

/**
 * 提供集合循环，返回集合entry
 * 例子：
 * {@code
 *                  List<UserBean.User> list = getUserInfo();
 *                  PhotoUtil.browser(this,UserBean.User.class)
 *                         .fromList(list, new OnItemCallBack<UserBean.User>() {
 *                             @Override
 *                             public void nextItem(UserBean.User item, PhotoPagerConfig.Builder<UserBean.User> builder) {
 *                                 builder.addSingleBigImageUrl(item.getAvatar());
 *                             }
 *                         })
 *                         .setOnPhotoSaveCallback(new OnPhotoSaveCallback() {
 *                             @Override
 *                             public void onSaveImageResult(String localFilePath) {
 *                                 Toast(localFilePath != null ? "保存成功" : "保存失败");
 *                             }
 *                         })
 *                         .build();
 * }
 * @param <T>
 */
public interface OnItemCallBack<T> {

    /**
     * 你只需要关注item里面的图片url字段即可。你应该在此方法调用：{@code builder.addSingleBigImageUrl(imageUrl)}
     * 如果还有缩略图，还可调用：{@code builder.addSingleSmallImageUrl(smallImageUrl)}
     *
     * @param item 集合item
     * @param builder 查看网络大图builder
     */
    void nextItem(@NonNull T item, @NonNull PhotoPagerConfig.Builder<T> builder);
}
