package com.hjbalan.mycalendar.utils;

import com.hjbalan.mycalendar.R;

import android.content.Context;
import android.os.Build;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alan on 14-9-24.
 */
public class Utils {

    public static final int MONDAY_BEFORE_JULIAN_EPOCH = Time.EPOCH_JULIAN_DAY - 3;

    // The name of the shared preferences file. This name must be maintained for
    // historical
    // reasons, as it's what PreferenceManager assigned the first time the file
    // was created.
    static final String SHARED_PREFS_NAME = "com.hjbalan.calendar_preferences";

    private static final CalendarUtils.TimeZoneUtils mTZUtils = new CalendarUtils.TimeZoneUtils(
            SHARED_PREFS_NAME);

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

    /**
     * Takes a number of weeks since the epoch and calculates the Julian day of
     * the Monday for that week.
     *
     * This assumes that the week containing the {@link Time#EPOCH_JULIAN_DAY}
     * is considered week 0. It returns the Julian day for the Monday
     * {@code week} weeks after the Monday of the week containing the epoch.
     *
     * @param week Number of weeks since the epoch
     * @return The julian day for the Monday of the given week since the epoch
     */
    public static int getJulianMondayFromWeeksSinceEpoch(int week) {
        return MONDAY_BEFORE_JULIAN_EPOCH + week * 7;
    }

    /**
     * Formats a date or a time range according to the local conventions.
     *
     * @param context     the context is required only if the time is shown
     * @param startMillis the start time in UTC milliseconds
     * @param endMillis   the end time in UTC milliseconds
     * @param flags       a bit mask of options See {@link android.text.format.DateUtils#formatDateRange(Context,
     *                    java.util.Formatter, long, long, int, String) formatDateRange}
     * @return a string containing the formatted date/time range.
     */
    public static String formatDateRange(
            Context context, long startMillis, long endMillis, int flags) {
        return mTZUtils.formatDateRange(context, startMillis, endMillis, flags);
    }

    /**
     * Returns the week since {@link Time#EPOCH_JULIAN_DAY} (Jan 1, 1970)
     * adjusted for first day of week.
     *
     * This takes a julian day and the week start day and calculates which
     * week since {@link Time#EPOCH_JULIAN_DAY} that day occurs in, starting
     * at 0. *Do not* use this to compute the ISO week number for the year.
     *
     * @param julianDay      The julian day to calculate the week number for
     * @param firstDayOfWeek Which week day is the first day of the week,
     *                       see {@link Time#SUNDAY}
     * @return Weeks since the epoch
     */
    public static int getWeeksSinceEpochFromJulianDay(int julianDay, int firstDayOfWeek) {
        int diff = Time.THURSDAY - firstDayOfWeek;
        if (diff < 0) {
            diff += 7;
        }
        int refDay = Time.EPOCH_JULIAN_DAY - diff;
        return (julianDay - refDay) / 7;
    }

}
