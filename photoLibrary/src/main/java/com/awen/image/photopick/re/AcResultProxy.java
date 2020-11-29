package com.awen.image.photopick.re;


import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * 用于处理activity#onActivityResult回调
 */
public class AcResultProxy {
    private static final String TAG = "AcResultUtil";
    private ActivityResultFragment fragment;

    public AcResultProxy(FragmentActivity activity, OnActivityResultCallBack callBack) {
        fragment = getResultFragment(activity);
        fragment.setCallBack(callBack);
    }

    public void startAcResult(Intent intent, int requestCode) {
        fragment.setAcResult(intent, requestCode);
    }

    public ActivityResultFragment getFragment() {
        return fragment;
    }

    private ActivityResultFragment getResultFragment(FragmentActivity activity) {
        ActivityResultFragment fragment = findResultFragment(activity);
        if (fragment == null) {
            fragment = new ActivityResultFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    public void removeResultFragment(FragmentActivity activity) {
        Fragment fragment = findResultFragment(activity);
        if (fragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    @Nullable
    private ActivityResultFragment findResultFragment(FragmentActivity activity) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment != null) {
            return (ActivityResultFragment) fragment;
        }
        return null;
    }
}
