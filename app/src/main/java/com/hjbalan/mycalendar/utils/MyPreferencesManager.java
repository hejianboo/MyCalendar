package com.hjbalan.mycalendar.utils;

import com.hjbalan.mycalendar.CalendarApplication;

import android.content.SharedPreferences;

public class MyPreferencesManager {

    private static final String APP_PREFERENCES = "m15_cal_pre";

    private static final String SHOW_CHINESE_CAL = "show_chinese_cal";

    private static final String SELECTED_CALENDAR = "selected_calendar";

    private static MyPreferencesManager mManager;

    private SharedPreferences mPreferences;

    private MyPreferencesManager() {
        mPreferences = CalendarApplication.getInstance().getSharedPreferences(
                APP_PREFERENCES, 0);
    }

    public synchronized static MyPreferencesManager getInstance() {
        if (mManager == null) {
            mManager = new MyPreferencesManager();
        }
        return mManager;
    }

    public void saveShowChineseCalendarSetting(boolean showChinese) {
        putBoolean(SHOW_CHINESE_CAL, showChinese);
    }

    public boolean isShowChineseCalendar() {
        return getBoolean(SHOW_CHINESE_CAL, false);
    }

    public void saveSelectedCalendarId(long selectedCalendarId) {
        putLong(SELECTED_CALENDAR, selectedCalendarId);
    }

    public long getSelectedCalendarId() {
        return getLong(SELECTED_CALENDAR);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean def) {
        return mPreferences.getBoolean(key, def);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key) {
        return mPreferences.getLong(key, -1);
    }

    private SharedPreferences.Editor getEditor() {
        return mPreferences.edit();
    }

    public void clear() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        editor.commit();
    }

}
