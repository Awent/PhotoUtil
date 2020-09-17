package com.awen.image.photopick.util;

import android.os.Build;

public class SDKVersionUtil {

    /**
     * 是否是Android Q
     */
    public static boolean isAndroid_Q() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
