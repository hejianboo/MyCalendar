package com.hjbalan.mycalendar.ui;

import com.android.common.Rfc822Validator;
import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;
import com.hjbalan.mycalendar.event.Event;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;
import com.hjbalan.mycalendar.utils.MyUtils;

import android.app.DialogFragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
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
import android.widget.TextView;

import java.util.ArrayList;

public class EditEventActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener,
        SelectCalendarDialogFragment.SelectCalendarListener {

    public static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
            CalendarContract.Calendars.NAME,                          // 4
            CalendarContract.Calendars.ACCOUNT_TYPE                   // 5
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;

    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;

    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final int PROJECTION_NAME = 4;

    private static final int PROJECTION_ACCOUNT_TYPE = 5;

    private static final int TOKEN_QUERY_CALENDAR = 1;

    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";

    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";

    private static final String TAG = "EditActivity";

    private Event mEvent = null;

    private ArrayList<CalendarInfo> mCalendarInfos = new ArrayList<>();

    private EventHandler mEventHandler;

    private EditText mEtTitle;

    private CheckBox mCbAllDay;

    private CheckBox mCbChineseDate;

    private Button mBtnSelectStartDate;

    private Button mBtnSelectStartTime;

    private Button mBtnSelectEndDate;

    private Button mBtnSelectEndTime;

    private EditText mEtDesc;

    private Button mBtnSelectCalendar;

    private Spinner mSpinnerReminder;

    private Spinner mSpinnerReminderMethod;

    private Spinner mSpinnerRepeat;

    private DatePickerDialog mDatePickerDialog;

    private TimePickerDialog mTimePickerDialog;

    private Time mStartTime;

    private Time mEndTime;

    private String mTimezone;

    private boolean mAllDay = false;

    private Rfc822Validator mEmailValidator;

    private int mSelectedPosition = 0;

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
            mEventHandler
                    .startQuery(TOKEN_QUERY_CALENDAR, null, CalendarContract.Calendars.CONTENT_URI,
                            CALENDAR_PROJECTION, null, null, null);
        }

        mTimezone = Time.getCurrentTimezone();
        mStartTime = new Time(mTimezone);
        mEndTime = new Time(mTimezone);
        mEmailValidator = new Rfc822Validator(null);

        initView();
        preRenderView();

    }

    private void initView() {
        mEtTitle = (EditText) findViewById(R.id.et_title);
        mCbAllDay = (CheckBox) findViewById(R.id.cb_all_day);
        mCbChineseDate = (CheckBox) findViewById(R.id.cb_chinese_event);
        mBtnSelectStartDate = (Button) findViewById(R.id.btn_date_start);
        mBtnSelectStartTime = (Button) findViewById(R.id.btn_time_start);
        mBtnSelectEndDate = (Button) findViewById(R.id.btn_date_end);
        mBtnSelectEndTime = (Button) findViewById(R.id.btn_time_end);
        mEtDesc = (EditText) findViewById(R.id.et_desc);
        mBtnSelectCalendar = (Button) findViewById(R.id.btn_select_calendar);
        mSpinnerReminder = (Spinner) findViewById(R.id.sp_reminder);
        mSpinnerReminderMethod = (Spinner) findViewById(R.id.sp_reminder_method);
        mSpinnerRepeat = (Spinner) findViewById(R.id.sp_repeat);

        mCbAllDay.setOnCheckedChangeListener(this);
        mCbChineseDate.setOnCheckedChangeListener(this);
        mBtnSelectStartDate.setOnClickListener(this);
        mBtnSelectStartTime.setOnClickListener(this);
        mBtnSelectEndDate.setOnClickListener(this);
        mBtnSelectEndTime.setOnClickListener(this);
        mBtnSelectCalendar.setOnClickListener(this);
        mSpinnerReminder.setOnItemSelectedListener(this);
        mSpinnerReminderMethod.setOnItemSelectedListener(this);
        mSpinnerRepeat.setOnItemSelectedListener(this);

    }

    private void preRenderView() {
        if (mEvent == null) {
            mStartTime.set(System.currentTimeMillis());
            mEndTime.set(mStartTime.toMillis(false) + DateUtils.HOUR_IN_MILLIS);
            setDate(mBtnSelectStartDate, mStartTime.toMillis(false));
            setTime(mBtnSelectStartTime, mStartTime.toMillis(false));
            setDate(mBtnSelectEndDate, mEndTime.toMillis(false));
            setTime(mBtnSelectEndTime, mEndTime.toMillis(false));
            return;
        } else {

        }
    }

    private void setDate(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY;

        view.setText(DateUtils.formatDateTime(this, millis, flags));
    }

    private void setTime(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_NOON_MIDNIGHT
                | DateUtils.FORMAT_24HOUR;
        view.setText(DateUtils.formatDateTime(this, millis, flags));
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
            addEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_all_day:
                mBtnSelectStartTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                mBtnSelectEndTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                break;

            case R.id.cb_chinese_event:

                break;

            default:
                break;
        }
    }

    private long addEvent() {
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
        return 0;
    }

    private Event fillEventFromUI() {
        if (mEvent == null) {
            mEvent = Event.newInstance();
        }

        return mEvent;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date_start:
                showDatePickerDialog(v, mStartTime);
                break;

            case R.id.btn_time_start:
                showTimePickerDialog(v, mStartTime);
                break;

            case R.id.btn_date_end:
                showDatePickerDialog(v, mEndTime);
                break;

            case R.id.btn_time_end:
                showTimePickerDialog(v, mEndTime);
                break;

            case R.id.btn_select_calendar:
                if (mCalendarInfos != null && mCalendarInfos.size() > 0) {
                    SelectCalendarDialogFragment
                            .newInstance(EditEventActivity.this, mCalendarInfos, mSelectedPosition)
                            .show(getFragmentManager(), SelectCalendarDialogFragment.TAG);
                }

                break;

            default:
                break;
        }
    }

    private void showDatePickerDialog(View v, Time time) {
        mDatePickerDialog = (DatePickerDialog) getFragmentManager().findFragmentByTag(
                FRAG_TAG_DATE_PICKER);
        if (mDatePickerDialog != null) {
            mDatePickerDialog.dismiss();
        }
        mDatePickerDialog = DatePickerDialog.newInstance(new DateListener(v),
                time.year, time.month, time.monthDay);
        mDatePickerDialog.setFirstDayOfWeek(MyUtils.getFirstDayOfWeekAsCalendar(this));
        mDatePickerDialog.setYearRange(MyUtils.YEAR_MIN, MyUtils.YEAR_MAX);
        mDatePickerDialog.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
    }

    private void showTimePickerDialog(View v, Time time) {
        mTimePickerDialog = (TimePickerDialog) getFragmentManager().findFragmentByTag(
                FRAG_TAG_TIME_PICKER);
        if (mTimePickerDialog != null) {
            mTimePickerDialog.dismiss();
        }
        mTimePickerDialog = TimePickerDialog.newInstance(new TimeListener(v),
                time.hour, time.minute, DateFormat.is24HourFormat(this));
        mTimePickerDialog.show(getFragmentManager(), FRAG_TAG_TIME_PICKER);
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

    @Override
    public void onCalendarSelected(DialogFragment dialogFragment, int position) {
        mSelectedPosition = position;
        CalendarInfo calendarInfo = mCalendarInfos.get(position);
        MyPreferencesManager.getInstance().saveSelectedCalendarName(calendarInfo.calendarName);
        mBtnSelectCalendar.setText(calendarInfo.displayName);
    }

    private class DateListener implements DatePickerDialog.OnDateSetListener {

        View mView;

        public DateListener(View view) {
            mView = view;
        }

        @Override
        public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
            Log.d(TAG, "onDateSet: " + year + " " + monthOfYear + " " + dayOfMonth);
            // Cache the member variables locally to avoid inner class overhead.
            Time startTime = mStartTime;
            Time endTime = mEndTime;

            // Cache the start and end millis so that we limit the number
            // of calls to normalize() and toMillis(), which are fairly
            // expensive.
            long startMillis;
            long endMillis;
            if (mView == mBtnSelectStartDate) {
                // The start date was changed.
                int yearDuration = endTime.year - startTime.year;
                int monthDuration = endTime.month - startTime.month;
                int monthDayDuration = endTime.monthDay - startTime.monthDay;

                startTime.year = year;
                startTime.month = monthOfYear;
                startTime.monthDay = dayOfMonth;
                startMillis = startTime.normalize(true);

                // Also update the end date to keep the duration constant.
                endTime.year = year + yearDuration;
                endTime.month = monthOfYear + monthDuration;
                endTime.monthDay = dayOfMonth + monthDayDuration;
                endMillis = endTime.normalize(true);

                // If the start date has changed then update the repeats.
//                populateRepeats();

            } else {
                // The end date was changed.
                startMillis = startTime.toMillis(true);
                endTime.year = year;
                endTime.month = monthOfYear;
                endTime.monthDay = dayOfMonth;
                endMillis = endTime.normalize(true);

                // Do not allow an event to have an end time before the start
                // time.
                if (endTime.before(startTime)) {
                    endTime.set(startTime);
                    endMillis = startMillis;
                }
            }

            setDate(mBtnSelectStartDate, startMillis);
            setDate(mBtnSelectEndDate, endMillis);
        }
    }

    private class TimeListener implements TimePickerDialog.OnTimeSetListener {

        View mView;

        public TimeListener(View view) {
            mView = view;
        }

        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
            Log.d(TAG, "onTimeSet: " + hourOfDay + " " + minute);

            // Cache the member variables locally to avoid inner class overhead.
            Time startTime = mStartTime;
            Time endTime = mEndTime;

            // Cache the start and end millis so that we limit the number
            // of calls to normalize() and toMillis(), which are fairly
            // expensive.
            long startMillis;
            long endMillis;
            if (mView == mBtnSelectStartTime) {
                // The start time was changed.
                int hourDuration = endTime.hour - startTime.hour;
                int minuteDuration = endTime.minute - startTime.minute;

                startTime.hour = hourOfDay;
                startTime.minute = minute;
                startMillis = startTime.normalize(true);

                // Also update the end time to keep the duration constant.
                endTime.hour = hourOfDay + hourDuration;
                endTime.minute = minute + minuteDuration;

            } else {
                // The end time was changed.
                startMillis = startTime.toMillis(true);
                endTime.hour = hourOfDay;
                endTime.minute = minute;

                // Move to the start time if the end time is before the start
                // time.
                if (endTime.before(startTime)) {
                    endTime.monthDay = startTime.monthDay + 1;
                }
            }

            endMillis = endTime.normalize(true);
            setDate(mBtnSelectEndDate, endMillis);
            setTime(mBtnSelectStartTime, startMillis);
            setTime(mBtnSelectEndTime, endMillis);
        }
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
                    String name = MyPreferencesManager.getInstance().getSelectedCalendarName();
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
                        if (calendarInfo.calendarName.equals(name)) {
                            mSelectedPosition = cursor.getPosition();
                        }
                        Log.d("Setting", "calendar info is \r\n" + calendarInfo.toString());
                    }
                }
                if (mCalendarInfos.size() > 0) {
                    mBtnSelectCalendar.setText(mCalendarInfos.get(mSelectedPosition).displayName);
                }
            }
        }
    }

}
