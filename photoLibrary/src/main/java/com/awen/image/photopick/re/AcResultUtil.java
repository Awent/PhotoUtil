package com.awen.image.photopick.re;


import android.content.Intent;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * 用于处理activity#onActivityResult回调
 */
public class AcResultUtil {
    private static final String TAG = "AcResultUtil";
    private ActivityResultFragment fragment;

    public AcResultUtil(FragmentActivity activity, OnActivityResultCallBack callBack) {
        fragment = getResultFragment(activity);
        fragment.setCallBack(callBack);
    }

    public void startAcResult(Intent intent, int requestCode) {
        fragment.setAcResult(intent,requestCode);
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

    private ActivityResultFragment findResultFragment(FragmentActivity activity) {
        return (ActivityResultFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
    }
}
