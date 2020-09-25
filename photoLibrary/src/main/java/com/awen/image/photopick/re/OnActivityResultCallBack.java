package com.awen.image.photopick.re;

import android.content.Intent;

import androidx.annotation.Nullable;

public interface OnActivityResultCallBack {

    void onActivityResultX(int requestCode, int resultCode, @Nullable Intent data);

    void requestCameraPermissionSuccess();

    void requestCameraPermissionFailed();
}
