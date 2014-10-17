package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;
import com.hjbalan.mycalendar.event.Event;
import com.hjbalan.mycalendar.utils.Utils;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class EditEventActivity extends Activity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
            CalendarContract.Calendars.NAME,                          // 4
            CalendarContract.Calendars.ACCOUNT_TYPE,                  // 5
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;

    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;

    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final int PROJECTION_NAME = 4;

    private static final int PROJECTION_ACCOUNT_TYPE = 5;

    private static final int TOKEN_QUERY = 1;

    private Event mEvent = null;

    private ArrayList<CalendarInfo> mCalendarInfos = new ArrayList<>();

    private EventHandler mEventHandler;

    private EditText mEtTitle;

    private CheckBox mCbAllDay;

    private CheckBox mCbChineseDate;

    private Button mBtnSelectFromDate;

    private Button mBtnSelectFromTime;

    private Button mBtnSelectToDate;

    private Button mBtnSelectToTime;

    private EditText mEtDesc;

    private Button mBtnSelectCalendar;

    private Spinner mSpinnerReminder;

    private Spinner mSpinnerReminderMethod;

    private Spinner mSpinnerRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        mEvent = (Event) getIntent().getParcelableExtra("event");
        if (mEvent == null) {
            setTitle(R.string.title_activity_new_event);
        }

        mEventHandler = new EventHandler(getContentResolver());

        if (savedInstanceState == null) {
            mEventHandler.startQuery(TOKEN_QUERY, null, CalendarContract.Calendars.CONTENT_URI,
                    EVENT_PROJECTION, null, null, null);
        }

        initView();
        preRenderView();

    }

    private void initView() {
        mEtTitle = (EditText) findViewById(R.id.et_title);
        mCbAllDay = (CheckBox) findViewById(R.id.cb_all_day);
        mCbChineseDate = (CheckBox) findViewById(R.id.cb_chinese_event);
        mBtnSelectFromDate = (Button) findViewById(R.id.btn_date_from);
        mBtnSelectFromTime = (Button) findViewById(R.id.btn_time_from);
        mBtnSelectToDate = (Button) findViewById(R.id.btn_date_to);
        mBtnSelectToTime = (Button) findViewById(R.id.btn_time_to);
        mEtDesc = (EditText) findViewById(R.id.et_desc);
        mBtnSelectCalendar = (Button) findViewById(R.id.btn_select_calendar);
        mSpinnerReminder = (Spinner) findViewById(R.id.sp_reminder);
        mSpinnerReminderMethod = (Spinner) findViewById(R.id.sp_reminder_method);
        mSpinnerRepeat = (Spinner) findViewById(R.id.sp_repeat);

        mCbAllDay.setOnCheckedChangeListener(this);
        mCbChineseDate.setOnCheckedChangeListener(this);
        mBtnSelectFromDate.setOnClickListener(this);
        mBtnSelectFromTime.setOnClickListener(this);
        mBtnSelectToDate.setOnClickListener(this);
        mBtnSelectToTime.setOnClickListener(this);
        mBtnSelectCalendar.setOnClickListener(this);
        mSpinnerReminder.setOnItemSelectedListener(this);
        mSpinnerReminderMethod.setOnItemSelectedListener(this);
        mSpinnerRepeat.setOnItemSelectedListener(this);
    }

    private void preRenderView() {
        if (mEvent == null) {
            long currentTimeMillis = System.currentTimeMillis();
            renderSelectFromDateView(currentTimeMillis);
            currentTimeMillis += 60 * 60 * 1000;
            renderSelectToDateView(currentTimeMillis);
            return;
        } else {

        }
    }

    private void renderSelectFromDateView(long fromMillis) {
        String[] readableStrings = Utils.getReadableTime(this, fromMillis).split("-");
        mBtnSelectFromDate.setText(readableStrings[0]);
        mBtnSelectFromTime.setText(readableStrings[1]);
    }

    private void renderSelectToDateView(long toMillis) {
        String[] readableStrings = Utils.getReadableTime(this, toMillis).split("-");
        mBtnSelectToDate.setText(readableStrings[0]);
        mBtnSelectToTime.setText(readableStrings[1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_finish) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private long addEvent() {
//        long calID = 3;
//        long startMillis = 0;
//        long endMillis = 0;
//        Calendar beginTime = Calendar.getInstance();
//        beginTime.set(2012, 9, 14, 7, 30);
//        startMillis = beginTime.getTimeInMillis();
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(2012, 9, 14, 8, 45);
//        endMillis = endTime.getTimeInMillis();
//        ...
//
//        ContentResolver cr = getContentResolver();
//        ContentValues values = new ContentValues();
//        values.put(Events.DTSTART, startMillis);
//        values.put(Events.DTEND, endMillis);
//        values.put(Events.TITLE, "Jazzercise");
//        values.put(Events.DESCRIPTION, "Group workout");
//        values.put(Events.CALENDAR_ID, calID);
//        values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
//        Uri uri = cr.insert(Events.CONTENT_URI, values);
//
//        long eventID = Long.parseLong(uri.getLastPathSegment());
//    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_all_day:
                mBtnSelectFromTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                mBtnSelectToTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                break;

            case R.id.cb_chinese_event:

                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date_from:

                break;

            case R.id.btn_time_from:

                break;

            case R.id.btn_date_to:

                break;

            case R.id.btn_time_to:

                break;

            case R.id.btn_select_calendar:

                break;

            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_reminder:

                break;

            case R.id.sp_reminder_method:

                break;

            case R.id.sp_repeat:

                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class EventHandler extends AsyncQueryHandler {

        public EventHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            if (isFinishing()) {
                return;
            } else {

            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if (isFinishing()) {
                return;
            } else {

            }
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null) {
                return;
            }
            if (isFinishing()) {
                cursor.close();
            } else {
                mCalendarInfos.clear();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        if (!cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                                .equals(cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX))) {
                            continue;
                        }
                        CalendarInfo calendarInfo = new CalendarInfo();
                        calendarInfo.id = cursor.getInt(PROJECTION_ID_INDEX);
                        calendarInfo.accountName = cursor.getString(
                                PROJECTION_ACCOUNT_NAME_INDEX);
                        calendarInfo.displayName = cursor.getString(
                                PROJECTION_DISPLAY_NAME_INDEX);
                        calendarInfo.ownerAccount = cursor.getString(
                                PROJECTION_OWNER_ACCOUNT_INDEX);
                        calendarInfo.calendarName = cursor.getString(PROJECTION_NAME);
                        calendarInfo.accountType = cursor.getString(
                                PROJECTION_ACCOUNT_TYPE);
                        mCalendarInfos.add(calendarInfo);
                        Log.d("Setting", "calendar info is \r\n" + calendarInfo.toString());
                    }
                }

                if (mCalendarInfos.size() > 0) {
                    mBtnSelectCalendar.setText(mCalendarInfos.get(0).calendarName);
                }
            }
        }
    }
}
