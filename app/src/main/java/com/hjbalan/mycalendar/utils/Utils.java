package com.hjbalan.mycalendar.utils;

import android.os.Build;

/**
 * Created by alan on 14-9-24.
 */
public class Utils {

    /**
     * Returns whether the SDK is the Jellybean release or later.
     */
    public static boolean isJellybeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
}
