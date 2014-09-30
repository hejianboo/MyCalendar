package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;

import org.jraf.android.backport.switchwidget.Switch;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 14-9-25.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT,                 // 3
            Calendars.NAME,
            Calendars.ACCOUNT_TYPE,
            Calendars.VISIBLE
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;

    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;

    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final int PROJECTION_NAME = 4;

    private static final int PROJECTION_ACCOUNT_TYPE = 5;

    private static final int PROJECTION_VISIBLE = 6;

    private Switch mSwitchShowChineseCalendar;

    private TextView mTvVisibleCalendar;

    private LinearLayout mLlAccounts;

    private View mViewReminderSetting;

    private boolean mLastShowChineseCalendarSetting;

    private Map<String, ArrayList<CalendarInfo>> mCalendarInfos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle(R.string.action_settings);

        mSwitchShowChineseCalendar = (Switch) findViewById(R.id.switch_show_chinese);
        findViewById(R.id.tv_feedback).setOnClickListener(this);

        mLastShowChineseCalendarSetting = MyPreferencesManager.getInstance()
                .isShowChineseCalendar();
        mSwitchShowChineseCalendar.setChecked(mLastShowChineseCalendarSetting);
        mTvVisibleCalendar = (TextView) findViewById(R.id.tv_visible_calendar);
        mLlAccounts = (LinearLayout) findViewById(R.id.ll_account_container);
        mViewReminderSetting = findViewById(R.id.ll_reminder_setting);

        mTvVisibleCalendar.setOnClickListener(this);
        mViewReminderSetting.setOnClickListener(this);

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_feedback:
                sendFeedback();
                break;
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "hjbalan@gmail.com", null));
        String subject = getString(R.string.settings_feedback);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, subject));
    }

    @Override
    public void onBackPressed() {
        if (mLastShowChineseCalendarSetting != mSwitchShowChineseCalendar.isChecked()) {
            mLastShowChineseCalendarSetting = mSwitchShowChineseCalendar.isChecked();
            MyPreferencesManager.getInstance().saveShowChineseCalendarSetting(
                    mLastShowChineseCalendarSetting);
        }
        super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = Calendars.ACCOUNT_TYPE + " = ? ";
        return new CursorLoader(this, Calendars.CONTENT_URI, EVENT_PROJECTION, selection,
                new String[]{"com.google"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCalendarInfos.clear();
        if (data != null && data.getCount() > 0) {
            while (data.moveToNext()) {
                CalendarInfo calendarInfo = new CalendarInfo();
                calendarInfo.id = data.getInt(PROJECTION_ID_INDEX);
                calendarInfo.accountName = data.getString(
                        PROJECTION_ACCOUNT_NAME_INDEX);
                calendarInfo.displayName = data.getString(
                        PROJECTION_DISPLAY_NAME_INDEX);
                calendarInfo.ownerAccount = data.getString(
                        PROJECTION_OWNER_ACCOUNT_INDEX);
                calendarInfo.calendarName = data.getString(PROJECTION_NAME);
                calendarInfo.accountType = data.getString(
                        PROJECTION_ACCOUNT_TYPE);
                calendarInfo.visible = data.getInt(PROJECTION_VISIBLE);

                ArrayList<CalendarInfo> calendars;
                if (mCalendarInfos.containsKey(calendarInfo.accountName)) {
                    calendars = mCalendarInfos.get(calendarInfo.accountName);
                } else {
                    calendars = new ArrayList<>();
                    mCalendarInfos.put(calendarInfo.accountName, calendars);
                }
                calendars.add(calendarInfo);
                Log.d("Setting", "calendar info is \r\n" + calendarInfo.toString());
            }
        }
        renderAccountContainerView();
    }

    private void renderAccountContainerView() {
        int count = mLlAccounts.getChildCount();
        if (count > 1) {
            mLlAccounts.removeViews(1, count - 1);
        }
        for (String accountName : mCalendarInfos.keySet()) {
            mLlAccounts.addView(createAccountView(accountName));
        }
        mLlAccounts.addView(createAddAccountView());
    }

    private View createAccountView(String accountName) {
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.view_calendar_name, null);
        tv.setText(accountName);
        tv.setOnClickListener(this);
        return tv;
    }

    private View createAddAccountView() {
        View view = getLayoutInflater().inflate(R.layout.view_add_account, null);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
