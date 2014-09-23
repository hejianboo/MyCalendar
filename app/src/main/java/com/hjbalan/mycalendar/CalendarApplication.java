package com.hjbalan.mycalendar;

import android.app.Application;

/**
 * Created by alan on 14-9-23.
 */
public class CalendarApplication extends Application {

    private static CalendarApplication sInstance;

    public synchronized static CalendarApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
