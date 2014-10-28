package com.hjbalan.mycalendar.utils;

import org.joda.time.DateTimeUtils;

import android.content.Context;
import android.os.Build;
import android.text.format.Time;

import java.util.Calendar;

/**
 * Created by alan on 14-9-24.
 */
public class MyUtils {

    public static final int YEAR_MIN;

    public static final int YEAR_MAX;

    private static Calendar sCurrentCalendar = Calendar.getInstance();

    static {
        YEAR_MIN = sCurrentCalendar.get(Calendar.YEAR);
        YEAR_MAX = YEAR_MIN + 1;
    }

    private MyUtils() {
    }

    /**
     * Returns whether the SDK is the Jellybean release or later.
     */
    public static boolean isJellybeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

//    /**
//     * format time millis to readable time
//     */
//    public static String getReadableTime(Context ctx, long timeMillis) {
//        sCurrentCalendar = Calendar.getInstance();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timeMillis);
//
//        Locale locale = ctx.getResources().getConfiguration().locale;
//        SimpleDateFormat sf = sCurrentCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) ?
//                new SimpleDateFormat(ctx.getString(R.string.format_date_simple_partten), locale)
//                : new SimpleDateFormat(ctx.getString(R.string.format_date_parttern), locale);
//
//        return sf.format(new Date(timeMillis));
//    }

    /**
     * Get first day of week as java.util.Calendar constant.
     *
     * @return the first day of week as a java.util.Calendar constant
     */
    public static int getFirstDayOfWeekAsCalendar(Context context) {
        return convertDayOfWeekFromTimeToCalendar(getFirstDayOfWeek(context));
    }

    /**
     * Converts the day of the week from android.text.format.Time to java.util.Calendar
     */
    public static int convertDayOfWeekFromTimeToCalendar(int timeDayOfWeek) {
        switch (timeDayOfWeek) {
            case Time.MONDAY:
                return Calendar.MONDAY;
            case Time.TUESDAY:
                return Calendar.TUESDAY;
            case Time.WEDNESDAY:
                return Calendar.WEDNESDAY;
            case Time.THURSDAY:
                return Calendar.THURSDAY;
            case Time.FRIDAY:
                return Calendar.FRIDAY;
            case Time.SATURDAY:
                return Calendar.SATURDAY;
            case Time.SUNDAY:
                return Calendar.SUNDAY;
            default:
                throw new IllegalArgumentException("Argument must be between Time.SUNDAY and " +
                        "Time.SATURDAY");
        }
    }

    /**
     * Get first day of week as android.text.format.Time constant.
     *
     * @return the first day of week in android.text.format.Time
     */
    public static int getFirstDayOfWeek(Context context) {
//        SharedPreferences prefs = GeneralPreferences.getSharedPreferences(context);
//        String pref = prefs.getString(
//                GeneralPreferences.KEY_WEEK_START_DAY, GeneralPreferences.WEEK_START_DEFAULT);
//
//        int startDay;
//        if (GeneralPreferences.WEEK_START_DEFAULT.equals(pref)) {
//            startDay = Calendar.getInstance().getFirstDayOfWeek();
//        } else {
//            startDay = Integer.parseInt(pref);
//        }

//        if (startDay == Calendar.SATURDAY) {
//            return Time.SATURDAY;
//        } else if (startDay == Calendar.MONDAY) {
        return Time.MONDAY;
//        } else {
//            return Time.SUNDAY;
//        }
    }

    public static int getJulianDay(final long timeMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeMillis);
        // add 12 hours to fix julian day bug such as 2014/10/19 00:00:00
        if (c.get(Calendar.HOUR_OF_DAY) < 8) {
            c.set(Calendar.HOUR_OF_DAY, 8);
        }
        return (int) DateTimeUtils.toJulianDayNumber(c.getTimeInMillis());
    }

}
