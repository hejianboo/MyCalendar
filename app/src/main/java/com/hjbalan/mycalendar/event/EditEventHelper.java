/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hjbalan.mycalendar.event;

import com.android.calendarcommon.EventRecurrence;
import com.hjbalan.mycalendar.ui.BaseActivity;
import com.hjbalan.mycalendar.utils.AsyncQueryService;
import com.hjbalan.mycalendar.utils.MyUtils;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.TimeZone;

public class EditEventHelper {

    public static final String[] EVENT_PROJECTION = new String[]{
            Events._ID, // 0
            Events.TITLE, // 1
            Events.DESCRIPTION, // 2
            Events.EVENT_LOCATION, // 3
            Events.ALL_DAY, // 4
            Events.HAS_ALARM, // 5
            Events.CALENDAR_ID, // 6
            Events.DTSTART, // 7
            Events.DTEND, // 8
            Events.DURATION, // 9
            Events.EVENT_TIMEZONE, // 10
            Events.RRULE, // 11
            Events._SYNC_ID, // 12
            Events.AVAILABILITY, // 13
            Events.ACCESS_LEVEL, // 14
            Events.OWNER_ACCOUNT, // 15
            Events.HAS_ATTENDEE_DATA, // 16
            Events.ORIGINAL_SYNC_ID, // 17
            Events.ORGANIZER, // 18
            Events.GUESTS_CAN_MODIFY, // 19
            Events.ORIGINAL_ID, // 20
            Events.STATUS, // 21
            Events.CALENDAR_COLOR, // 22
            Events.EVENT_COLOR, // 23
            Events.EVENT_COLOR_KEY // 24
    };

    public static final String[] REMINDERS_PROJECTION = new String[]{
            Reminders._ID, // 0
            Reminders.MINUTES, // 1
            Reminders.METHOD, // 2
    };

    public static final int REMINDERS_INDEX_MINUTES = 1;

    public static final int REMINDERS_INDEX_METHOD = 2;

    public static final String REMINDERS_WHERE = Reminders.EVENT_ID + "=?";

    public static final int DOES_NOT_REPEAT = 0;

    public static final int REPEATS_DAILY = 1;

    public static final int REPEATS_EVERY_WEEKDAY = 2;

    public static final int REPEATS_WEEKLY_ON_DAY = 3;

    public static final int REPEATS_MONTHLY_ON_DAY_COUNT = 4;

    public static final int REPEATS_MONTHLY_ON_DAY = 5;

    public static final int REPEATS_YEARLY = 6;

    public static final int REPEATS_CUSTOM = 7;

    /**
     * This is the symbolic name for the key used to pass in the boolean for
     * creating all-day events that is part of the extra data of the intent.
     * This is used only for creating new events and is set to true if the
     * default for the new event should be an all-day event.
     */
    public static final String EVENT_ALL_DAY = "allDay";

    public static final String CALENDARS_WHERE = Calendars._ID + "=?";

    public static final String[] CALENDARS_PROJECTION = new String[]{
            Calendars._ID, // 0
            Calendars.CALENDAR_DISPLAY_NAME, // 1
            Calendars.OWNER_ACCOUNT, // 2
            Calendars.CALENDAR_COLOR, // 3
            Calendars.CAN_ORGANIZER_RESPOND, // 4
            Calendars.CALENDAR_ACCESS_LEVEL, // 5
            Calendars.VISIBLE, // 6
            Calendars.MAX_REMINDERS, // 7
            Calendars.ALLOWED_REMINDERS, // 8
            Calendars.ALLOWED_ATTENDEE_TYPES, // 9
            Calendars.ALLOWED_AVAILABILITY, // 10
            Calendars.ACCOUNT_NAME, // 11
            Calendars.ACCOUNT_TYPE, //12
    };

    public static final String CALENDARS_WHERE_WRITEABLE_VISIBLE = Calendars.CALENDAR_ACCESS_LEVEL
            + ">="
            + Calendars.CAL_ACCESS_CONTRIBUTOR + " AND " + Calendars.VISIBLE + "=1";

    protected static final int EVENT_INDEX_ID = 0;

    protected static final int EVENT_INDEX_TITLE = 1;

    protected static final int EVENT_INDEX_DESCRIPTION = 2;

    protected static final int EVENT_INDEX_EVENT_LOCATION = 3;

    protected static final int EVENT_INDEX_ALL_DAY = 4;

    protected static final int EVENT_INDEX_HAS_ALARM = 5;

    protected static final int EVENT_INDEX_CALENDAR_ID = 6;

    protected static final int EVENT_INDEX_DTSTART = 7;

    protected static final int EVENT_INDEX_DTEND = 8;

    protected static final int EVENT_INDEX_DURATION = 9;

    protected static final int EVENT_INDEX_TIMEZONE = 10;

    protected static final int EVENT_INDEX_RRULE = 11;

    protected static final int EVENT_INDEX_SYNC_ID = 12;

    protected static final int EVENT_INDEX_AVAILABILITY = 13;

    protected static final int EVENT_INDEX_ACCESS_LEVEL = 14;

    protected static final int EVENT_INDEX_OWNER_ACCOUNT = 15;

    protected static final int EVENT_INDEX_HAS_ATTENDEE_DATA = 16;

    protected static final int EVENT_INDEX_ORIGINAL_SYNC_ID = 17;

    protected static final int EVENT_INDEX_ORGANIZER = 18;

    protected static final int EVENT_INDEX_GUESTS_CAN_MODIFY = 19;

    protected static final int EVENT_INDEX_ORIGINAL_ID = 20;

    protected static final int EVENT_INDEX_EVENT_STATUS = 21;

    protected static final int EVENT_INDEX_CALENDAR_COLOR = 22;

    protected static final int EVENT_INDEX_EVENT_COLOR = 23;

    protected static final int EVENT_INDEX_EVENT_COLOR_KEY = 24;

    protected static final int MODIFY_UNINITIALIZED = 0;

    protected static final int MODIFY_SELECTED = 1;

    protected static final int MODIFY_ALL_FOLLOWING = 2;

    protected static final int MODIFY_ALL = 3;

    protected static final int DAY_IN_SECONDS = 24 * 60 * 60;

    // Visible for testing
    static final String ATTENDEES_DELETE_PREFIX = Attendees.EVENT_ID + "=? AND "
            + Attendees.ATTENDEE_EMAIL + " IN (";

    static final int CALENDARS_INDEX_ID = 0;

    static final int CALENDARS_INDEX_DISPLAY_NAME = 1;

    static final int CALENDARS_INDEX_OWNER_ACCOUNT = 2;

    static final int CALENDARS_INDEX_COLOR = 3;

    static final int CALENDARS_INDEX_CAN_ORGANIZER_RESPOND = 4;

    static final int CALENDARS_INDEX_ACCESS_LEVEL = 5;

    static final int CALENDARS_INDEX_VISIBLE = 6;

    static final int CALENDARS_INDEX_MAX_REMINDERS = 7;

    static final int CALENDARS_INDEX_ALLOWED_REMINDERS = 8;

    static final int CALENDARS_INDEX_ALLOWED_ATTENDEE_TYPES = 9;

    static final int CALENDARS_INDEX_ALLOWED_AVAILABILITY = 10;

    static final int CALENDARS_INDEX_ACCOUNT_NAME = 11;

    static final int CALENDARS_INDEX_ACCOUNT_TYPE = 12;

    private static final String TAG = "EditEventHelper";

    private static final boolean DEBUG = false;

    private static final String NO_EVENT_COLOR = "";

    private final AsyncQueryService mService;

    // This allows us to flag the event if something is wrong with it, right now
    // if an uri is provided for an event that doesn't exist in the db.
    protected boolean mEventOk = true;

    // Used for parsing rrules for special cases.
    private EventRecurrence mEventRecurrence = new EventRecurrence();

    public EditEventHelper(Context context) {
        mService = ((BaseActivity) context).getAsyncQueryService();
    }

    /**
     * Compares two models to ensure that they refer to the same event. This is
     * a safety check to make sure an updated event model refers to the same
     * event as the original model. If the original model is null then this is a
     * new event or we're forcing an overwrite so we return true in that case.
     * The important identifiers are the Calendar Id and the Event Id.
     */
    public static boolean isSameEvent(CalendarEventModel model, CalendarEventModel originalModel) {
        if (originalModel == null) {
            return true;
        }

        if (model.mCalendarId != originalModel.mCalendarId) {
            return false;
        }
        if (model.mId != originalModel.mId) {
            return false;
        }

        return true;
    }

    /**
     * Saves the reminders, if they changed. Returns true if operations to
     * update the database were added.
     *
     * @param ops               the array of ContentProviderOperations
     * @param eventId           the id of the event whose reminders are being updated
     * @param reminders         the array of reminders set by the user
     * @param originalReminders the original array of reminders
     * @param forceSave         if true, then save the reminders even if they didn't change
     * @return true if operations to update the database were added
     */
    public static boolean saveReminders(ArrayList<ContentProviderOperation> ops, long eventId,
            ArrayList<CalendarEventModel.ReminderEntry> reminders,
            ArrayList<CalendarEventModel.ReminderEntry> originalReminders,
            boolean forceSave) {
        // If the reminders have not changed, then don't update the database
        if (reminders.equals(originalReminders) && !forceSave) {
            return false;
        }

        // Delete all the existing reminders for this event
        String where = Reminders.EVENT_ID + "=?";
        String[] args = new String[]{Long.toString(eventId)};
        ContentProviderOperation.Builder b = ContentProviderOperation
                .newDelete(Reminders.CONTENT_URI);
        b.withSelection(where, args);
        ops.add(b.build());

        ContentValues values = new ContentValues();
        int len = reminders.size();

        // Insert the new reminders, if any
        for (int i = 0; i < len; i++) {
            CalendarEventModel.ReminderEntry re = reminders.get(i);

            values.clear();
            values.put(Reminders.MINUTES, re.getMinutes());
            values.put(Reminders.METHOD, re.getMethod());
            values.put(Reminders.EVENT_ID, eventId);
            b = ContentProviderOperation.newInsert(Reminders.CONTENT_URI).withValues(values);
            ops.add(b.build());
        }
        return true;
    }

    /**
     * Saves the reminders, if they changed. Returns true if operations to
     * update the database were added. Uses a reference id since an id isn't
     * created until the row is added.
     *
     * @param ops       the array of ContentProviderOperations
     * @param forceSave if true, then save the reminders even if they didn't change
     * @return true if operations to update the database were added
     */
    public static boolean saveRemindersWithBackRef(ArrayList<ContentProviderOperation> ops,
            int eventIdIndex, ArrayList<CalendarEventModel.ReminderEntry> reminders,
            ArrayList<CalendarEventModel.ReminderEntry> originalReminders, boolean forceSave) {
        // If the reminders have not changed, then don't update the database
        if (reminders.equals(originalReminders) && !forceSave) {
            return false;
        }

        // Delete all the existing reminders for this event
        ContentProviderOperation.Builder b = ContentProviderOperation
                .newDelete(Reminders.CONTENT_URI);
        b.withSelection(Reminders.EVENT_ID + "=?", new String[1]);
        b.withSelectionBackReference(0, eventIdIndex);
        ops.add(b.build());

        ContentValues values = new ContentValues();
        int len = reminders.size();

        // Insert the new reminders, if any
        for (int i = 0; i < len; i++) {
            CalendarEventModel.ReminderEntry re = reminders.get(i);

            values.clear();
            values.put(Reminders.MINUTES, re.getMinutes());
            values.put(Reminders.METHOD, re.getMethod());
            b = ContentProviderOperation.newInsert(Reminders.CONTENT_URI).withValues(values);
            b.withValueBackReference(Reminders.EVENT_ID, eventIdIndex);
            ops.add(b.build());
        }
        return true;
    }

    // It's the first event in the series if the start time before being
    // modified is the same as the original event's start time
    static boolean isFirstEventInSeries(CalendarEventModel model,
            CalendarEventModel originalModel) {
        return model.mOriginalStart == originalModel.mStart;
    }

    /**
     * Uses a calendar cursor to fill in the given model This method assumes the
     * cursor used {@link #CALENDARS_PROJECTION} as it's query projection. It uses
     * the cursor to fill in the given model with all the information available.
     *
     * @param model  The model to fill in
     * @param cursor An event cursor that used {@link #CALENDARS_PROJECTION} for the query
     * @return returns true if model was updated with the info in the cursor.
     */
    public static boolean setModelFromCalendarCursor(CalendarEventModel model, Cursor cursor) {
        if (model == null || cursor == null) {
            Log.wtf(TAG, "Attempted to build non-existent model or from an incorrect query.");
            return false;
        }

        if (model.mCalendarId == -1) {
            return false;
        }

        if (!model.mModelUpdatedWithEventCursor) {
            Log.wtf(TAG,
                    "Can't update model with a Calendar cursor until it has seen an Event cursor.");
            return false;
        }

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            if (model.mCalendarId != cursor.getInt(CALENDARS_INDEX_ID)) {
                continue;
            }
            model.mCalendarDisplayName = cursor.getString(CALENDARS_INDEX_DISPLAY_NAME);
            model.mCalendarAccountName = cursor.getString(CALENDARS_INDEX_ACCOUNT_NAME);
            model.mCalendarAccountType = cursor.getString(CALENDARS_INDEX_ACCOUNT_TYPE);
            model.mCalendarMaxReminders = cursor.getInt(CALENDARS_INDEX_MAX_REMINDERS);
            model.mCalendarAllowedReminders = cursor.getString(CALENDARS_INDEX_ALLOWED_REMINDERS);

            return true;
        }
        return false;
    }

    /**
     * Uses an event cursor to fill in the given model This method assumes the
     * cursor used {@link #EVENT_PROJECTION} as it's query projection. It uses
     * the cursor to fill in the given model with all the information available.
     *
     * @param model  The model to fill in
     * @param cursor An event cursor that used {@link #EVENT_PROJECTION} for the query
     */
    public static void setModelFromCursor(CalendarEventModel model, Cursor cursor) {
        if (model == null || cursor == null || cursor.getCount() != 1) {
            Log.wtf(TAG, "Attempted to build non-existent model or from an incorrect query.");
            return;
        }

        model.clear();
        cursor.moveToFirst();

        model.mId = cursor.getInt(EVENT_INDEX_ID);
        model.mTitle = cursor.getString(EVENT_INDEX_TITLE);
        model.mDescription = cursor.getString(EVENT_INDEX_DESCRIPTION);
        model.mLocation = cursor.getString(EVENT_INDEX_EVENT_LOCATION);
        model.mAllDay = cursor.getInt(EVENT_INDEX_ALL_DAY) != 0;
        model.mHasAlarm = cursor.getInt(EVENT_INDEX_HAS_ALARM) != 0;
        model.mCalendarId = cursor.getInt(EVENT_INDEX_CALENDAR_ID);
        model.mStart = cursor.getLong(EVENT_INDEX_DTSTART);

        model.mOwnerAccount = cursor.getString(EVENT_INDEX_OWNER_ACCOUNT);
        model.mEnd = cursor.getLong(EVENT_INDEX_DTEND);
        model.mModelUpdatedWithEventCursor = true;
    }

    /**
     * Saves the event. Returns true if the event was successfully saved, false
     * otherwise.
     *
     * @param model         The event model to save
     * @param originalModel A model of the original event if it exists
     * @param modifyWhich   For recurring events which type of series modification to use
     * @return true if the event was successfully queued for saving
     */
    public boolean saveEvent(CalendarEventModel model, CalendarEventModel originalModel,
            int modifyWhich) {
        boolean forceSaveReminders = false;

        if (DEBUG) {
            Log.d(TAG, "Saving event model: " + model);
        }

        if (!mEventOk) {
            if (DEBUG) {
                Log.w(TAG, "Event no longer exists. Event was not saved.");
            }
            return false;
        }

        // It's a problem if we try to save a non-existent or invalid model or if we're
        // modifying an existing event and we have the wrong original model
        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return false;
        }
        if (!model.isValid()) {
            Log.e(TAG, "Attempted to save invalid model.");
            return false;
        }
        if (originalModel != null && !isSameEvent(model, originalModel)) {
            Log.e(TAG, "Attempted to update existing event but models didn't refer to the same "
                    + "event.");
            return false;
        }
        if (originalModel != null && model.isUnchanged(originalModel)) {
            return false;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int eventIdIndex = -1;

        ContentValues values = getContentValuesFromModel(model);

        if (model.mUri != null && originalModel == null) {
            Log.e(TAG, "Existing event but no originalModel provided. Aborting save.");
            return false;
        }
        Uri uri = null;
        if (model.mUri != null) {
            uri = Uri.parse(model.mUri);
        }

        // Update the "hasAlarm" field for the event
        ArrayList<CalendarEventModel.ReminderEntry> reminders = model.mReminders;
        int len = reminders.size();
        values.put(Events.HAS_ALARM, (len > 0) ? 1 : 0);

        if (uri == null) {
            // Add hasAttendeeData for a new event
            values.put(Events.HAS_ATTENDEE_DATA, 1);
            values.put(Events.STATUS, Events.STATUS_CONFIRMED);
            eventIdIndex = ops.size();
            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(
                    Events.CONTENT_URI).withValues(values);
            ops.add(b.build());
            forceSaveReminders = true;

        }

        // New Event or New Exception to an existing event
        boolean newEvent = (eventIdIndex != -1);
        ArrayList<CalendarEventModel.ReminderEntry> originalReminders;
        if (originalModel != null) {
            originalReminders = originalModel.mReminders;
        } else {
            originalReminders = new ArrayList<CalendarEventModel.ReminderEntry>();
        }

        if (newEvent) {
            saveRemindersWithBackRef(ops, eventIdIndex, reminders, originalReminders,
                    forceSaveReminders);
        } else if (uri != null) {
            long eventId = ContentUris.parseId(uri);
            saveReminders(ops, eventId, reminders, originalReminders, forceSaveReminders);
        }

        mService.startBatch(mService.getNextToken(), null,
                android.provider.CalendarContract.AUTHORITY, ops,
                MyUtils.UNDO_DELAY);

        return true;
    }

    /**
     * When we aren't given an explicit start time, we default to the next
     * upcoming half hour. So, for example, 5:01 -> 5:30, 5:30 -> 6:00, etc.
     *
     * @return a UTC time in milliseconds representing the next upcoming half
     * hour
     */
    public long constructDefaultStartTime(long now) {
        Time defaultStart = new Time();
        defaultStart.set(now);
        defaultStart.second = 0;
        defaultStart.minute = 30;
        long defaultStartMillis = defaultStart.toMillis(false);
        if (now < defaultStartMillis) {
            return defaultStartMillis;
        } else {
            return defaultStartMillis + 30 * DateUtils.MINUTE_IN_MILLIS;
        }
    }

    /**
     * When we aren't given an explicit end time, we default to an hour after
     * the start time.
     *
     * @param startTime the start time
     * @return a default end time
     */
    public long constructDefaultEndTime(long startTime) {
        return startTime + DateUtils.HOUR_IN_MILLIS;
    }

    /**
     * Goes through an event model and fills in content values for saving. This
     * method will perform the initial collection of values from the model and
     * put them into a set of ContentValues. It performs some basic work such as
     * fixing the time on allDay events and choosing whether to use an rrule or
     * dtend.
     *
     * @param model The complete model of the event you want to save
     * @return values
     */
    ContentValues getContentValuesFromModel(CalendarEventModel model) {
        String title = model.mTitle;
        boolean isAllDay = model.mAllDay;

        String timezone = TimeZone.getDefault().getID();

        Time startTime = new Time(timezone);
        Time endTime = new Time(timezone);

        startTime.set(model.mStart);
        endTime.set(model.mEnd);

        ContentValues values = new ContentValues();

        long startMillis;
        long endMillis;
        long calendarId = model.mCalendarId;
        if (isAllDay) {
            // Reset start and end time, ensure at least 1 day duration, and set
            // the timezone to UTC, as required for all-day events.
            timezone = Time.TIMEZONE_UTC;
            startTime.hour = 0;
            startTime.minute = 0;
            startTime.second = 0;
            startTime.timezone = timezone;
            startMillis = startTime.normalize(true);

            endTime.hour = 0;
            endTime.minute = 0;
            endTime.second = 0;
            endTime.timezone = timezone;
            endMillis = endTime.normalize(true);
            if (endMillis < startMillis + DateUtils.DAY_IN_MILLIS) {
                // EditEventView#fillModelFromUI() should treat this case, but we want to ensure
                // the condition anyway.
                endMillis = startMillis + DateUtils.DAY_IN_MILLIS;
            }
        } else {
            startMillis = startTime.toMillis(true);
            endMillis = endTime.toMillis(true);
        }

        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.EVENT_TIMEZONE, timezone);
        values.put(Events.TITLE, title);
        values.put(Events.ALL_DAY, isAllDay ? 1 : 0);
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DURATION, (String) null);
        values.put(Events.DTEND, endMillis);
        if (model.mDescription != null) {
            values.put(Events.DESCRIPTION, model.mDescription.trim());
        } else {
            values.put(Events.DESCRIPTION, (String) null);
        }
        if (model.mLocation != null) {
            values.put(Events.EVENT_LOCATION, model.mLocation.trim());
        } else {
            values.put(Events.EVENT_LOCATION, (String) null);
        }
        return values;
    }

}