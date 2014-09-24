package com.hjbalan.mycalendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
            CalendarContract.Calendars.NAME
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;

    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;

    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final int PROJECTION_NAME = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queryCalendar();
    }

    private void queryCalendar() {
        Cursor c = null;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        c = contentResolver.query(uri, EVENT_PROJECTION, null, null, null);
        while (c.moveToNext()) {
            Log.d("calendar", "calendar id = " + c.getInt(PROJECTION_ID_INDEX));
            Log.d("calendar", "account name = " + c.getString(PROJECTION_ACCOUNT_NAME_INDEX));
            Log.d("calendar", "display name = " + c.getString(PROJECTION_DISPLAY_NAME_INDEX));
            Log.d("calendar", "owner account = " + c.getString(PROJECTION_OWNER_ACCOUNT_INDEX));
            Log.d("calendar", "calendar name = " + c.getString(PROJECTION_NAME));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
