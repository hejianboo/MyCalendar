package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.event.Event;
import com.hjbalan.mycalendar.event.EventLoader;
import com.hjbalan.mycalendar.ui.widget.CalendarView;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;
import com.hjbalan.mycalendar.utils.MyUtils;

import org.joda.time.DateTimeUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainActivity extends Activity implements CalendarView.OnFocusdMonthChangeListener,
        CalendarView.OnDateChangeListener, AbsListView.OnScrollListener, View.OnClickListener,
        CalendarView.OnTouchDateListener, StickyListHeadersListView.OnStickyHeaderChangedListener,
        Animation.AnimationListener {

    private static final int REQUEST_ADD_EVENT = 1;

    private static final int REQUEST_EDIT_EVENT = 2;

    private StickyListHeadersListView mLvEvents;

    private EventsAdapter mAdapter;

    private CalendarView mCalendarView;

    private Button mBtnCurrentDate;

    private Calendar mTodayDate;

    private Animation mFadeInAnim;

    private Animation mFadeOutAnim;

    private boolean mIsLoading = false;

    private boolean mIsItemFitScreen = false;

    private boolean mIsAnimStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCalendarView = (CalendarView) findViewById(R.id.cv);
        mLvEvents = (StickyListHeadersListView) findViewById(R.id.lv_events);

        mBtnCurrentDate = (Button) findViewById(R.id.btn_today);
        mBtnCurrentDate.setOnClickListener(this);

        mCalendarView.setOnFocusdMonthChangeListener(this);
        mCalendarView.setOnTouchDateListener(this);
        mCalendarView.setOnDateChangeListener(this);

        mLvEvents.setOnStickyHeaderChangedListener(this);
        mLvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event e = (Event) mAdapter.getItem(position);
                if (e.id > 0) {
                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, e.id);
//                    Intent intent = new Intent(getActivity(), EventInfoActivity.class).setData(uri);
//                    intent.putExtra(EXTRA_EVENT_BEGIN_TIME, e.startMillis);
//                    intent.putExtra(EXTRA_EVENT_END_TIME, e.endMillis);
//                    intent.putExtra(EXTRA_EVENT_ALL_DAY, e.allDay);allDay

//                    startActivity(intent);
                }
            }
        });

        mFadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mFadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mFadeInAnim.setAnimationListener(this);
        mFadeOutAnim.setAnimationListener(this);

        mLvEvents.setOnScrollListener(this);
        setActionBarCustomView();

    }

    private void setActionBarCustomView() {
        ActionBar actionbar = getActionBar();
        View customView = getLayoutInflater().inflate(
                R.layout.actionbar_custom_view, null);
        customView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSelectDateDialog();
            }
        });
        actionbar.setCustomView(customView);
    }

    private void showSelectDateDialog() {
        SelectDateDialogFragment fragment = SelectDateDialogFragment.newInstance(this,
                new BaseDialogFragment.MyDialogInterface.OnDialogButtonClickListener() {

                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        Calendar calendar = ((SelectDateDialogFragment) dialog).getSelectedDate();
                        if (calendar != null) {
                            updateActionBarTitle(calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH));
                            skipToSelectedDate(calendar);
                            updateTodayButton();
                        }
                    }

                });
        Bundle b = new Bundle();
        b.putLong(SelectDateDialogFragment.SELECTED_DATE, mCalendarView.getDate());
        fragment.setArguments(b);
        fragment.show(getFragmentManager(), SelectDateDialogFragment.TAG);
    }

    private void skipToSelectedDate(final Calendar calendar) {
        final long toMillis = calendar.getTimeInMillis();
        if (toMillis > mCalendarView.getMaxDate()) {
            mCalendarView.setDate(mCalendarView.getMaxDate());
        } else {
            mCalendarView.setDate(toMillis);
        }

        if (mAdapter.getCount() == 0) {
            return;
        }
        int julianDay = MyUtils.getJulianDay(toMillis);
        if (julianDay > mAdapter.getMaxJulianDay() || julianDay < mAdapter.getMinJulianDay()) {
            loadEvents(toMillis, calendar);
        } else {
            scrollTo(calendar);
        }
    }

    private void loadEvents(final long startMillis, final Calendar scrollToCalendar) {
        mIsLoading = true;
        final int minDay = MyUtils.getJulianDay(mCalendarView.getMinDate());
        final int maxDay = MyUtils.getJulianDay(mCalendarView.getMaxDate());
        new AsyncTask<Void, Void, ArrayList<Event>>() {

            @Override
            protected ArrayList<Event> doInBackground(Void... params) {
                int startDay = MyUtils.getJulianDay(startMillis);
                ArrayList<Event> events = new ArrayList<>();
                int start = startDay - 365;
                if (start < minDay) {
                    start = minDay;
                }
                int end = startDay + 365;
                if (end > maxDay) {
                    end = maxDay + 1;
                }
                ArrayList<Event> validEvents = EventLoader.loadEvents(MainActivity.this, start,
                        end - start);
                for (int i = start; i < end; i++) {
                    boolean hasEvent = false;
                    for (Event event : validEvents) {
                        if (event.startDay == i) {
                            hasEvent = true;
                            events.add(event);
                        }
                    }
                    if (hasEvent) {
                        continue;
                    }
                    Event event = Event.newInstance();
                    event.startDay = i;
                    event.startMillis = DateTimeUtils.fromJulianDay(i);
                    events.add(event);
                }

                return events;
            }

            @Override
            protected void onPostExecute(ArrayList<Event> result) {
                super.onPostExecute(result);
                mAdapter = new EventsAdapter(MainActivity.this);
                mLvEvents.setAdapter(mAdapter);
                mAdapter.setEvents(result);
                computeIsItemFitsScreen();
                scrollTo(scrollToCalendar);
                mIsLoading = false;
            }
        }.execute();
    }

    private void computeIsItemFitsScreen() {
        int last = mLvEvents.getLastVisiblePosition();
        if (mLvEvents.getChildAt(last) != null) {
            mIsItemFitScreen = (last == (mLvEvents.getCount() - 1))
                    && mLvEvents.getChildAt(last).getBottom() <= mLvEvents.getHeight();
        }
    }

    private void scrollTo(final Calendar scrollToCalendar) {
        final int position = mAdapter.indexOfStartDay(scrollToCalendar.getTimeInMillis());
        if (position >= 0) {
            mLvEvents.setSelection(position);
        }
    }

    private void updateActionBarTitle(int year, int month) {
        ActionBar actionbar = getActionBar();
        View view = actionbar.getCustomView();
        if (view != null) {
            TextView tvMonth = (TextView) view.findViewById(R.id.tv_title_month);
            TextView tvYear = (TextView) view.findViewById(R.id.tv_title_year);
            tvMonth.setText((++month) + "");
            String yearString = getString(R.string.month) + "\r\n" + year
                    + getString(R.string.year);
            tvYear.setText(yearString);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_add) {
            startActivityForResult(new Intent(this, EditEventActivity.class), REQUEST_ADD_EVENT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isShowChineseCalendar = MyPreferencesManager.getInstance().isShowChineseCalendar();
        if (mCalendarView.isShowChineseDate() != isShowChineseCalendar) {
            mCalendarView.setShowChineseDate(isShowChineseCalendar);
        }

        if (mTodayDate == null) {
            mTodayDate = Calendar.getInstance(TimeZone.getDefault());
            loadEvents(mTodayDate.getTimeInMillis(), mTodayDate);
            mBtnCurrentDate.startAnimation(mFadeOutAnim);
        } else {
            Calendar currentDate = Calendar.getInstance(TimeZone.getDefault());
            if (mTodayDate.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
                return;
            } else {
                mTodayDate.setTimeInMillis(currentDate.getTimeInMillis());
            }
        }
        updateActionBarTitle(mTodayDate.get(Calendar.YEAR), mTodayDate.get(Calendar.MONTH));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_today:
                if (mTodayDate != null) {
                    skipToSelectedDate(mTodayDate);
                    updateActionBarTitle(mTodayDate.get(Calendar.YEAR),
                            mTodayDate.get(Calendar.MONTH));
                }
                v.startAnimation(mFadeOutAnim);
                break;

            default:
                break;
        }
    }

    @Override
    public void onFocusedMonthChange(CalendarView view, int year, int month, int dayOfMonth) {
        updateActionBarTitle(year, month);
    }

    @Override
    public void onTouchDate(CalendarView view, Calendar touchedDate) {
        scrollTo(touchedDate);
        updateTodayButton();
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                updateTodayButton();
                break;

            default:
                break;
        }
    }

    private void updateTodayButton() {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(mCalendarView.getDate());
        if (selectedDate.get(Calendar.YEAR) == mTodayDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.MONTH) == mTodayDate.get(Calendar.MONTH)
                && selectedDate.get(Calendar.DAY_OF_MONTH) == mTodayDate
                .get(Calendar.DAY_OF_MONTH)) {
            if (mBtnCurrentDate.isShown() && !mIsAnimStarted) {
                mBtnCurrentDate.startAnimation(mFadeOutAnim);
            }
        } else {
            if (!mBtnCurrentDate.isShown() && !mIsAnimStarted) {
                mBtnCurrentDate.startAnimation(mFadeInAnim);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {

        if (mIsItemFitScreen) {
            return;
        }
        Calendar scrollToCalendar = Calendar.getInstance();
        Event event = null;
        if (totalItemCount > 0 && ((firstVisibleItem + visibleItemCount) == totalItemCount)
                && !mIsLoading) {

            event = (Event) mAdapter.getItem(totalItemCount - 1);
            Event scrollToEvent = (Event) mAdapter.getItem(firstVisibleItem);
            if (mCalendarView.isDateAfterMaxDate(event.startMillis)) {
                return;
            }
            scrollToCalendar.setTimeInMillis(scrollToEvent.startMillis);
            loadEvents(event.startMillis, scrollToCalendar);
        } else if (totalItemCount > 0 && firstVisibleItem == 0 && !mIsLoading) {
            event = (Event) mAdapter.getItem(0);
            if (mCalendarView.isDateBeforeMinDate(event.startMillis)) {
                return;
            }
            scrollToCalendar.setTimeInMillis(event.startMillis);
            loadEvents(event.startMillis, scrollToCalendar);
        }
    }

    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView stickyListHeadersListView,
            View view, int i, long l) {
        Event event = (Event) mAdapter.getItem(i);
        updateCalendarViewSelectedDate(event.startMillis);
    }

    private void updateCalendarViewSelectedDate(long timeMillis) {
        mCalendarView.setDate(timeMillis);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        mIsAnimStarted = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        boolean isShown = mBtnCurrentDate.isShown();
        mBtnCurrentDate.setVisibility(isShown ? View.GONE : View.VISIBLE);
        mIsAnimStarted = false;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
