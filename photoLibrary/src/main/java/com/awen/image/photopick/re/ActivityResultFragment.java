package com.awen.image.photopick.re;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import static com.awen.image.PhotoSetting.DEBUG;

/**
 * 只是作为一个中间桥梁，主要是为了能获取到onActivityResult回调和相机权限判断
 */
public class ActivityResultFragment extends Fragment {

    private static final String TAG = "ActivityResultFragment";
    private final int REQUEST_CODE_PERMISSION_CAMERA = 100;//获取拍照权限
    private OnActivityResultCallBack callBack;
    private Intent intent;
    private int requestCode;

    public void setCallBack(OnActivityResultCallBack callBack) {
        this.callBack = callBack;
    }

    public void setAcResult(Intent intent, int requestCode) {
        this.intent = intent;
        this.requestCode = requestCode;
        PermissionGen.needPermission(this, REQUEST_CODE_PERMISSION_CAMERA, Manifest.permission.CAMERA);
    }

    public ActivityResultFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            Log.e(TAG, "onActivityResult requestCode = " + requestCode + ",resultCode = " + resultCode);
        }
        if (callBack != null) {
            callBack.onActivityResultX(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (DEBUG) {
            Log.e(TAG, "--onRequestPermissionsResult--");
        }
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_CAMERA)
    private void selectPicFromCameraSuccess() {
        if (DEBUG) {
            Log.e(TAG, "--selectPicFromCameraSuccess--");
        }
        if (intent == null) {
            return;
        }
        startActivityForResult(intent, requestCode);
        if (callBack != null) {
            callBack.requestCameraPermissionSuccess();
        }
    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_CAMERA)
    private void selectPicFromCameraFailed() {
        if (DEBUG) {
            Log.e(TAG, "--selectPicFromCameraFailed--");
        }
        if (callBack != null) {
            callBack.requestCameraPermissionFailed();
        }
    }

}
