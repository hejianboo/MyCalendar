package com.hjbalan.mycalendar.utils;

import com.hjbalan.mycalendar.R;

import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alan on 14-9-24.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Returns whether the SDK is the Jellybean release or later.
     */
    public static boolean isJellybeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }


    /**
     * format time millis to readable time
     */
    public static String getReadableTime(Context ctx, long timeMillis) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);

        Locale locale = ctx.getResources().getConfiguration().locale;
        SimpleDateFormat sf = currentCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) ?
                new SimpleDateFormat(ctx.getString(R.string.format_date_simple_partten), locale)
                : new SimpleDateFormat(ctx.getString(R.string.format_date_parttern), locale);

        return sf.format(new Date(timeMillis));
    }

}
