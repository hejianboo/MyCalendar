package com.hjbalan.mycalendar.event;

import com.hjbalan.mycalendar.utils.MyUtils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Debug;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alan on 14-9-24.
 */
public class EventLoader {

    private static final String TAG = "CalEvent";

    private static final boolean PROFILE = false;

    /**
     * The sort order is:
     * 1) events with an earlier start (begin for normal events, startday for allday)
     * 2) events with a later end (end for normal events, endday for allday)
     * 3) the title (unnecessary, but nice)
     *
     * The start and end day is sorted first so that all day events are
     * sorted correctly with respect to events that are >24 hours (and
     * therefore show up in the allday area).
     */
    private static final String SORT_EVENTS_BY =
            "begin ASC, end DESC, title ASC";

    private static final String SORT_ALLDAY_BY =
            "startDay ASC, endDay DESC, title ASC";

    private static final String DISPLAY_AS_ALLDAY = "dispAllday";

    private static final String EVENTS_WHERE = DISPLAY_AS_ALLDAY + "=0";

    private static final String ALLDAY_WHERE = DISPLAY_AS_ALLDAY + "=1";

    // The projection to use when querying instances to build a list of events
    public static final String[] EVENT_PROJECTION = new String[]{
            Instances.TITLE,                 // 0
            Instances.EVENT_LOCATION,        // 1
            Instances.ALL_DAY,               // 2
            Instances.DISPLAY_COLOR,         // 3 If SDK < 16, set to Instances.CALENDAR_COLOR.
            Instances.EVENT_TIMEZONE,        // 4
            Instances.EVENT_ID,              // 5
            Instances.BEGIN,                 // 6
            Instances.END,                   // 7
            Instances._ID,                   // 8
            Instances.START_DAY,             // 9
            Instances.END_DAY,               // 10
            Instances.START_MINUTE,          // 11
            Instances.END_MINUTE,            // 12
            Instances.HAS_ALARM,             // 13
            Instances.RRULE,                 // 14
            Instances.RDATE,                 // 15
            Instances.SELF_ATTENDEE_STATUS,  // 16
            Events.ORGANIZER,                // 17
            Events.GUESTS_CAN_MODIFY,        // 18
            Instances.ALL_DAY + "=1 OR (" + Instances.END + "-" + Instances.BEGIN + ")>="
                    + DateUtils.DAY_IN_MILLIS + " AS " + DISPLAY_AS_ALLDAY, // 19
    };

    // The indices for the projection array above.
    private static final int PROJECTION_TITLE_INDEX = 0;

    private static final int PROJECTION_LOCATION_INDEX = 1;

    private static final int PROJECTION_ALL_DAY_INDEX = 2;

    private static final int PROJECTION_COLOR_INDEX = 3;

    private static final int PROJECTION_TIMEZONE_INDEX = 4;

    private static final int PROJECTION_EVENT_ID_INDEX = 5;

    private static final int PROJECTION_BEGIN_INDEX = 6;

    private static final int PROJECTION_END_INDEX = 7;

    private static final int PROJECTION_START_DAY_INDEX = 9;

    private static final int PROJECTION_END_DAY_INDEX = 10;

    private static final int PROJECTION_START_MINUTE_INDEX = 11;

    private static final int PROJECTION_END_MINUTE_INDEX = 12;

    private static final int PROJECTION_HAS_ALARM_INDEX = 13;

    private static final int PROJECTION_RRULE_INDEX = 14;

    private static final int PROJECTION_RDATE_INDEX = 15;

    private static final int PROJECTION_SELF_ATTENDEE_STATUS_INDEX = 16;

    private static final int PROJECTION_ORGANIZER_INDEX = 17;

    private static final int PROJECTION_GUESTS_CAN_INVITE_OTHERS_INDEX = 18;

    private static final int PROJECTION_DISPLAY_AS_ALLDAY = 19;

    static {
        if (!MyUtils.isJellybeanOrLater()) {
            EVENT_PROJECTION[PROJECTION_COLOR_INDEX] = Instances.CALENDAR_COLOR;
        }
    }

    /**
     * 加载置顶时间段内的事件
     *
     * @param context  the context
     * @param startDay the time range to query in UTC millis since epoch
     * @param days     the number of day to query
     * @return the list of event
     */
    public static ArrayList<Event> loadEvents(Context context, int startDay, int days) {

        if (PROFILE) {
            Debug.startMethodTracing("loadEvents");
        }

        ArrayList<Event> events = new ArrayList<>();
        Cursor cEvents = null;
        try {
            int endDay = startDay + days - 1;
            cEvents = instancesQuery(context.getContentResolver(),
                    EVENT_PROJECTION, startDay, endDay, null, null,
                    SORT_EVENTS_BY);

            buildEventsFromCursor(events, cEvents, context, startDay, endDay);
            return events;
        } finally {
            if (cEvents != null) {
                cEvents.close();
            }
            if (PROFILE) {
                Debug.stopMethodTracing();
            }
        }
    }

    /**
     * Performs a query to return all visible instances in the given range that
     * match the given selection. This is a blocking function and should not be
     * done on the UI thread. This will cause an expansion of recurring events
     * to fill this time range if they are not already expanded and will slow
     * down for larger time ranges with many recurring events.
     *
     * @param cr            The ContentResolver to use for the query
     * @param projection    The columns to return
     * @param startDay      The start of the time range to query in UTC millis since epoch
     * @param endDay        The end of the time range to query in UTC millis since epoch
     * @param selection     Filter on the query as an SQL WHERE statement
     * @param selectionArgs Args to replace any '?'s in the selection
     * @param orderBy       How to order the rows as an SQL ORDER BY statement
     * @return A Cursor of instances matching the selection
     */
    private static final Cursor instancesQuery(ContentResolver cr,
            String[] projection, int startDay, int endDay, String selection,
            String[] selectionArgs, String orderBy) {
        String WHERE_CALENDARS_SELECTED = Calendars.VISIBLE + "=?";
        String[] WHERE_CALENDARS_ARGS = {"1"};
        String DEFAULT_SORT_ORDER = "begin ASC";

        Uri.Builder builder = Instances.CONTENT_BY_DAY_URI.buildUpon();
        ContentUris.appendId(builder, startDay);
        ContentUris.appendId(builder, endDay);
        if (TextUtils.isEmpty(selection)) {
            selection = WHERE_CALENDARS_SELECTED;
            selectionArgs = WHERE_CALENDARS_ARGS;
        } else {
            selection = "(" + selection + ") AND " + WHERE_CALENDARS_SELECTED;
            if (selectionArgs != null && selectionArgs.length > 0) {
                selectionArgs = Arrays.copyOf(selectionArgs,
                        selectionArgs.length + 1);
                selectionArgs[selectionArgs.length - 1] = WHERE_CALENDARS_ARGS[0];
            } else {
                selectionArgs = WHERE_CALENDARS_ARGS;
            }
        }
        return cr.query(builder.build(), projection, selection, selectionArgs,
                orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
    }

    /**
     * Adds all the events from the cursors to the events list.
     *
     * @param events  The list of events
     * @param cEvents Events to add to the list
     */
    public static void buildEventsFromCursor(ArrayList<Event> events,
            Cursor cEvents, Context context, int startDay, int endDay) {
        if (cEvents == null || events == null) {
            Log.e(TAG,
                    "buildEventsFromCursor: null cursor or null events list!");
            return;
        }

        int count = cEvents.getCount();

        if (count == 0) {
            return;
        }

        // Sort events in two passes so we ensure the allday and standard events
        // get sorted in the correct order
        cEvents.moveToPosition(-1);
        while (cEvents.moveToNext()) {
            Event e = generateEventFromCursor(cEvents);
            if (e.startDay > endDay || e.endDay < startDay) {
                continue;
            }
            events.add(e);
        }
    }

    /**
     * @param cEvents Cursor pointing at event
     * @return An event created from the cursor
     */
    private static Event generateEventFromCursor(Cursor cEvents) {
        Event e = new Event();

        e.id = cEvents.getLong(PROJECTION_EVENT_ID_INDEX);
        e.title = cEvents.getString(PROJECTION_TITLE_INDEX);
        e.location = cEvents.getString(PROJECTION_LOCATION_INDEX);
        e.allDay = cEvents.getInt(PROJECTION_ALL_DAY_INDEX) != 0;
        e.organizer = cEvents.getString(PROJECTION_ORGANIZER_INDEX);
        e.guestsCanModify = cEvents
                .getInt(PROJECTION_GUESTS_CAN_INVITE_OTHERS_INDEX) != 0;

        if (e.title == null || e.title.length() == 0) {
            e.title = "";
        }

        if (!cEvents.isNull(PROJECTION_COLOR_INDEX)) {
            // Read the color from the database
            // e.color =
            // Utils.getDisplayColorFromColor(cEvents.getInt(PROJECTION_COLOR_INDEX));
        } else {
            e.color = 0;
        }

        long eStart = cEvents.getLong(PROJECTION_BEGIN_INDEX);
        long eEnd = cEvents.getLong(PROJECTION_END_INDEX);

        e.startMillis = eStart;
        e.startTime = cEvents.getInt(PROJECTION_START_MINUTE_INDEX);
        e.startDay = cEvents.getInt(PROJECTION_START_DAY_INDEX);

        e.endMillis = eEnd;
        e.endTime = cEvents.getInt(PROJECTION_END_MINUTE_INDEX);
        e.endDay = cEvents.getInt(PROJECTION_END_DAY_INDEX);

        e.hasAlarm = cEvents.getInt(PROJECTION_HAS_ALARM_INDEX) != 0;

        // Check if this is a repeating event
        String rrule = cEvents.getString(PROJECTION_RRULE_INDEX);
        String rdate = cEvents.getString(PROJECTION_RDATE_INDEX);
        if (!TextUtils.isEmpty(rrule) || !TextUtils.isEmpty(rdate)) {
            e.isRepeating = true;
        } else {
            e.isRepeating = false;
        }

        e.selfAttendeeStatus = cEvents
                .getInt(PROJECTION_SELF_ATTENDEE_STATUS_INDEX);
        return e;
    }

}
