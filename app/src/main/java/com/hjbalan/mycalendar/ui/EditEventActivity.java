package com.hjbalan.mycalendar.ui;

import com.android.common.Rfc822Validator;
import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;
import com.hjbalan.mycalendar.event.CalendarEventModel;
import com.hjbalan.mycalendar.event.EditEventHelper;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;
import com.hjbalan.mycalendar.utils.MyUtils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

public class EditEventActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener,
        SelectCalendarDialogFragment.SelectCalendarListener {

    public static final String EXTRA_EVENT_REMINDERS = "reminders";

    private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";

    private static final int TOKEN_CALENDARS = 1;

    private static final int TOKEN_EVENT = 2;

    private static final int TOKEN_REMINDERS = 3;

    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";

    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";

    private static final String TAG = "EditActivity";

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

    private Spinner mSpinnerRepeat;

    private DatePickerDialog mDatePickerDialog;

    private TimePickerDialog mTimePickerDialog;

    private String mTimezone;

    private boolean mAllDay = false;

    private Rfc822Validator mEmailValidator;

    private CalendarEventModel mModel;

    private CalendarEventModel mOriginalModel;

    private EventHandler mHandler;

    private EventBundle mEventBundle;

    private EditEventHelper mHelper;

    private Uri mUri = null;

    private Time mStartTime;

    private Time mEndTime;

    private long mBegin = -1;

    private long mEnd = -1;

    private long mCalendarId = -1;

    private ArrayList<CalendarEventModel.ReminderEntry> mReminders;

    private ArrayList<CalendarInfo> mCalendarInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        setTitle(R.string.title_activity_new_event);

        mHandler = new EventHandler(getContentResolver());
        mModel = new CalendarEventModel();
        mReminders = getReminderEntriesFromIntent();

        mTimezone = Time.getCurrentTimezone();
        mStartTime = new Time(mTimezone);
        mEndTime = new Time(mTimezone);

        mEmailValidator = new Rfc822Validator(null);

        mHelper = new EditEventHelper(this);

        initView();

        initEventInfo();

        preRenderView();

        startQuery();

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
        mSpinnerRepeat = (Spinner) findViewById(R.id.sp_repeat);

        mCbAllDay.setOnCheckedChangeListener(this);
        mCbChineseDate.setOnCheckedChangeListener(this);
        mBtnSelectStartDate.setOnClickListener(this);
        mBtnSelectStartTime.setOnClickListener(this);
        mBtnSelectEndDate.setOnClickListener(this);
        mBtnSelectEndTime.setOnClickListener(this);
        mBtnSelectCalendar.setOnClickListener(this);
        mSpinnerReminder.setOnItemSelectedListener(this);
        mSpinnerRepeat.setOnItemSelectedListener(this);
    }

    private void initEventInfo() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        long eventId = -1;
        mCalendarId = -1;
        if (data != null) {
            try {
                eventId = Long.parseLong(data.getLastPathSegment());
            } catch (NumberFormatException e) {

            }
            if (eventId != -1) {
                mModel.mId = eventId;
                mUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            }
            mBegin = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
            mEnd = intent.getLongExtra(EXTRA_EVENT_END_TIME, -1);

        } else if (mEventBundle != null) {
            if (mEventBundle.id != -1) {
                mModel.mId = mEventBundle.id;
                mUri = ContentUris
                        .withAppendedId(CalendarContract.Events.CONTENT_URI, mEventBundle.id);
            }
            mBegin = mEventBundle.start;
            mEnd = mEventBundle.end;
        }

        if (mReminders != null) {
            mModel.mReminders = mReminders;
        }

        if (mBegin <= 0) {
            // use a default value instead
            mBegin = mHelper.constructDefaultStartTime(System.currentTimeMillis());
        }
        if (mEnd < mBegin) {
            // use a default value instead
            mEnd = mHelper.constructDefaultEndTime(mBegin);
        }

        mStartTime.set(mBegin);
        mEndTime.set(mEnd);

    }

    private void startQuery() {
        // Kick off the query for the event
        boolean newEvent = mUri == null;
        if (!newEvent) {
            mHandler.startQuery(TOKEN_EVENT, null, mUri, EditEventHelper.EVENT_PROJECTION,
                    null /* selection */, null /* selection args */, null /* sort order */);
        } else {
            mModel.mOriginalStart = mBegin;
            mModel.mOriginalEnd = mEnd;
            mModel.mStart = mBegin;
            mModel.mEnd = mEnd;
            mModel.mCalendarId = mCalendarId;

            // Start a query in the background to read the list of calendars
            mHandler.startQuery(TOKEN_CALENDARS, null, CalendarContract.Calendars.CONTENT_URI,
                    EditEventHelper.CALENDARS_PROJECTION,
                    EditEventHelper.CALENDARS_WHERE_WRITEABLE_VISIBLE, null /* selection args */,
                    null /* sort order */);
        }
    }

    private void preRenderView() {
        setDate(mBtnSelectStartDate, mStartTime.toMillis(false));
        setTime(mBtnSelectStartTime, mStartTime.toMillis(false));
        setDate(mBtnSelectEndDate, mEndTime.toMillis(false));
        setTime(mBtnSelectEndTime, mEndTime.toMillis(false));
    }

    private ArrayList<CalendarEventModel.ReminderEntry> getReminderEntriesFromIntent() {
        Intent intent = getIntent();
        return (ArrayList<CalendarEventModel.ReminderEntry>) intent
                .getSerializableExtra(EXTRA_EVENT_REMINDERS);
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
                    long selectedCalendarId = MyPreferencesManager.getInstance()
                            .getSelectedCalendarId();
                    SelectCalendarDialogFragment
                            .newInstance(EditEventActivity.this, mCalendarInfos, selectedCalendarId)
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
    public void onCalendarSelected(CalendarInfo selectedCalendar) {
        MyPreferencesManager.getInstance().saveSelectedCalendarId(selectedCalendar.id);
        mModel.mCalendarId = selectedCalendar.id;
        mBtnSelectCalendar.setText(selectedCalendar.displayName);
    }

    private void setDefaultCalendar() {
        long selectedCalendarId = MyPreferencesManager.getInstance().getSelectedCalendarId();
        if (mCalendarInfos != null && mCalendarInfos.size() > 0) {
            CalendarInfo selectedCalendar = null;
            for (CalendarInfo calendarInfo : mCalendarInfos) {
                if (calendarInfo.id == selectedCalendarId) {
                    selectedCalendar = calendarInfo;
                    break;
                }
            }
            if (selectedCalendar == null) {
                selectedCalendar = mCalendarInfos.get(0);
            }

            MyPreferencesManager.getInstance().saveSelectedCalendarId(selectedCalendar.id);
            mBtnSelectCalendar.setText(selectedCalendar.displayName);
        }
    }

    private static class EventBundle implements Serializable {

        private static final long serialVersionUID = 1L;

        long id = -1;

        long start = -1;

        long end = -1;
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
                return;
            }
            long eventId;
            switch (token) {
                case TOKEN_CALENDARS:
                    try {
                        if (mModel.mId == -1) {
                            // Populate Calendar spinner only if no event id is set.
//                            MatrixCursor matrixCursor = CursorUtils.matrixCursorFromCursor(cursor);
                            mCalendarInfos = EditEventHelper.getCalendarInfoFromCursor(cursor);
                            setDefaultCalendar();
                        } else {
                            // Populate model for an existing event
                            EditEventHelper.setModelFromCalendarCursor(mModel, cursor);
                            EditEventHelper.setModelFromCalendarCursor(mOriginalModel, cursor);
                            mBtnSelectCalendar.setText(mModel.mCalendarDisplayName);
                        }
                    } finally {
                        cursor.close();
                    }
                    break;

                case TOKEN_EVENT:
                    if (cursor.getCount() == 0) {
                        cursor.close();
                        return;
                    }

                    mOriginalModel = new CalendarEventModel();

                    EditEventHelper.setModelFromCursor(mOriginalModel, cursor);
                    EditEventHelper.setModelFromCursor(mModel, cursor);
                    cursor.close();

                    mOriginalModel.mUri = mUri.toString();

                    mModel.mUri = mUri.toString();
                    mModel.mOriginalStart = mBegin;
                    mModel.mOriginalEnd = mEnd;
                    mModel.mStart = mBegin;
                    mModel.mEnd = mEnd;
                    eventId = mModel.mId;

                    // TOKEN_REMINDERS
                    if (mModel.mHasAlarm && mReminders == null) {
                        Uri rUri = CalendarContract.Reminders.CONTENT_URI;
                        String[] remArgs = {
                                Long.toString(eventId)
                        };
                        mHandler.startQuery(TOKEN_REMINDERS, null, rUri,
                                EditEventHelper.REMINDERS_PROJECTION,
                                EditEventHelper.REMINDERS_WHERE /* selection */,
                                remArgs /* selection args */, null /* sort order */);
                    } else {
                        if (mReminders == null) {
                            // mReminders should not be null.
                            mReminders = new ArrayList<CalendarEventModel.ReminderEntry>();
                        } else {
                            Collections.sort(mReminders);
                        }
                        mOriginalModel.mReminders = mReminders;
                        mModel.mReminders =
                                (ArrayList<CalendarEventModel.ReminderEntry>) mReminders.clone();
                    }

                    // TOKEN_CALENDARS
                    String[] selArgs = {
                            Long.toString(mModel.mCalendarId)
                    };
                    mHandler.startQuery(TOKEN_CALENDARS, null,
                            CalendarContract.Calendars.CONTENT_URI,
                            EditEventHelper.CALENDARS_PROJECTION, EditEventHelper.CALENDARS_WHERE,
                            selArgs /* selection args */, null /* sort order */);
                    // TODO: set event ui
                    break;

                case TOKEN_REMINDERS:
                    try {
                        // Add all reminders to the models
                        while (cursor.moveToNext()) {
                            int minutes = cursor.getInt(EditEventHelper.REMINDERS_INDEX_MINUTES);
                            int method = cursor.getInt(EditEventHelper.REMINDERS_INDEX_METHOD);
                            CalendarEventModel.ReminderEntry re = CalendarEventModel.ReminderEntry
                                    .valueOf(minutes, method);
                            mModel.mReminders.add(re);
                            mOriginalModel.mReminders.add(re);
                        }

                        // Sort appropriately for display
                        Collections.sort(mModel.mReminders);
                        Collections.sort(mOriginalModel.mReminders);
                    } finally {
                        cursor.close();
                    }

                    // TODO: set reminder ui
                    break;

                default:
                    cursor.close();
                    break;

            }
        }
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

}
