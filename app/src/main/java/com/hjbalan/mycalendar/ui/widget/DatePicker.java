/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.hjbalan.mycalendar.ui.widget;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.utils.CVArrays;
import com.hjbalan.mycalendar.utils.ChineseCalendar;
import com.hjbalan.mycalendar.utils.LunarCalendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This class is a widget for selecting a date. The date can be selected by a
 * year, month, and day spinners or a {@link CalendarView}. The set of spinners
 * and the calendar view are automatically synchronized. The client can
 * customize whether only the spinners, or only the calendar view, or both to be
 * displayed. Also the minimal and maximal date from which dates to be selected
 * can be customized.
 * <p>
 * See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.
 * </p>
 * <p>
 * For a dialog using this view, see {@link android.app.DatePickerDialog}.
 * </p>
 *
 * @attr ref android.R.styleable#DatePicker_startYear
 * @attr ref android.R.styleable#DatePicker_endYear
 * @attr ref android.R.styleable#DatePicker_maxDate
 * @attr ref android.R.styleable#DatePicker_minDate
 * @attr ref android.R.styleable#DatePicker_spinnersShown
 * @attr ref android.R.styleable#DatePicker_calendarViewShown
 */
// @Widget
@SuppressLint("NewApi")
public class DatePicker extends FrameLayout {

    private static final String LOG_TAG = DatePicker.class.getSimpleName();

    private static final String DATE_FORMAT = "MM/dd/yyyy";

    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    private static final int DEFAULT_START_YEAR = 1900;

    private static final int DEFAULT_END_YEAR = 2100;

    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;

    private static final boolean DEFAULT_SPINNERS_SHOWN = true;

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    private final LinearLayout mSpinners;

    private final NumberPicker mDaySpinner;

    private final NumberPicker mMonthSpinner;

    private final NumberPicker mYearSpinner;

    private final EditText mDaySpinnerInput;

    // private final CalendarView mCalendarView;
    private final EditText mMonthSpinnerInput;

    private final EditText mYearSpinnerInput;

    private Locale mCurrentLocale;

    private OnDateChangedListener mOnDateChangedListener;

    private String[] mShortMonths;

    private String[] mChineseMonths;

    private String[] mNumberOfChineseDays;

    private int mNumberOfMonths;

    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;

    private Calendar mSelectedDate;

    private int mCurrentChineseYear = 0;

    private int mCurrentChineseMonth = 1;

    private int mCurrentChineseDay = 0;

    private int[] mMinChineseDate;

    private int[] mMaxChineseDate;

    private boolean mIsChineseCalendar = false;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.datePickerStyle);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.DatePicker,
                defStyle, 0);
        boolean spinnersShown = attributesArray.getBoolean(R.styleable.DatePicker_dp_spinnersShown,
                DEFAULT_SPINNERS_SHOWN);
        boolean calendarViewShown = attributesArray.getBoolean(
                R.styleable.DatePicker_dp_calendarViewShown, DEFAULT_CALENDAR_VIEW_SHOWN);
        int startYear = attributesArray.getInt(R.styleable.DatePicker_dp_startYear,
                DEFAULT_START_YEAR);
        int endYear = attributesArray.getInt(R.styleable.DatePicker_dp_endYear, DEFAULT_END_YEAR);
        String minDate = attributesArray.getString(R.styleable.DatePicker_dp_minDate);
        String maxDate = attributesArray.getString(R.styleable.DatePicker_dp_maxDate);
        int layoutResourceId = attributesArray.getResourceId(
                R.styleable.DatePicker_dp_internalLayout, R.layout.date_picker_holo);
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        NumberPicker.OnValueChangeListener onChangeListener
                = new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateInputState();
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());

                // take care of wrapping of days and months to update greater
                // fields
                if (picker == mDaySpinner) {
                    // Log.d("picker", "day picker is changed newVal = " +
                    // newVal + "& oldVal = "
                    // + oldVal);
                    int maxDayOfMonth = mIsChineseCalendar ? mNumberOfChineseDays.length
                            : mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (mIsChineseCalendar) {
                        if (oldVal == maxDayOfMonth && newVal == 0) {
                            mCurrentChineseDay = newVal + 1;
                            mCurrentChineseMonth++;
                        } else if (oldVal == 0 && newVal == (maxDayOfMonth - 1)) {
                            mCurrentChineseDay = newVal + 1;
                            mCurrentChineseMonth--;
                        } else {
                            mCurrentChineseDay += newVal - oldVal;
                        }
                    } else {
                        if (oldVal == maxDayOfMonth && newVal == 1) {
                            mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                        } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                            mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                        } else {
                            mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                        }
                    }
                } else if (picker == mMonthSpinner) {
                    // Log.d("picker", "month picker is changed newVal = " +
                    // newVal + "& oldVal = "
                    // + oldVal);
                    if (mIsChineseCalendar) {
                        if (oldVal == 11 && newVal == 0) {
                            mCurrentChineseMonth = newVal + 1;
                            if (mCurrentChineseYear < mMaxChineseDate[0]) {
                                mCurrentChineseYear++;
                            }
                        } else if (oldVal == 0 && newVal == 11) {
                            mCurrentChineseMonth = newVal + 1;
                            if (mCurrentChineseYear > mMinChineseDate[0]) {
                                mCurrentChineseYear--;
                            }
                        } else {
                            mCurrentChineseMonth += newVal - oldVal;
                        }
                    } else {
                        if (oldVal == 11 && newVal == 0) {
                            mTempDate.add(Calendar.MONTH, 1);
                        } else if (oldVal == 0 && newVal == 11) {
                            mTempDate.add(Calendar.MONTH, -1);
                        } else {
                            mTempDate.add(Calendar.MONTH, newVal - oldVal);
                        }
                    }
                } else if (picker == mYearSpinner) {
                    // Log.d("picker", "year picker is changed newVal = " +
                    // newVal + "& oldVal = "
                    // + oldVal);
                    if (mIsChineseCalendar) {
                        mCurrentChineseYear += newVal - oldVal;
                    } else {
                        mTempDate.add(Calendar.YEAR, newVal - oldVal);
                    }
                } else {
                    throw new IllegalArgumentException();
                }

//				Log.d("picker", "selected chinese date is " + mCurrentChineseYear + "/"
//						+ mCurrentChineseMonth + "/" + mCurrentChineseDay);

                // convert Chinese date to gregorian date
                if (mIsChineseCalendar) {
                    int numberOfDays = LunarCalendar.daysInMonth(mCurrentChineseYear,
                            mCurrentChineseMonth, false);
                    if (numberOfDays <= mCurrentChineseDay) {
                        mCurrentChineseDay = numberOfDays;
                    }
                    int[] solar = LunarCalendar.lunarToSolar(mCurrentChineseYear,
                            mCurrentChineseMonth, mCurrentChineseDay, false);
//					Log.d("picker", "compute solar date is " + solar[0] + "/" + solar[1] + "/"
//							+ solar[2]);

                    mTempDate.set(solar[0], solar[1] - 1, solar[2]);
                }
//				Log.d("picker",
//						"selected date is " + mTempDate.get(Calendar.YEAR) + "/"
//								+ mTempDate.get(Calendar.MONTH) + "/"
//								+ mTempDate.get(Calendar.DAY_OF_MONTH));

                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH));
                computeChineseDate();
                updateSpinners();
                // updateCalendarView();
                notifyDateChanged();
            }
        };

        mSpinners = (LinearLayout) findViewById(R.id.pickers);

        // calendar view day-picker
        // mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
        // mCalendarView.setOnDateChangeListener(new
        // CalendarView.OnDateChangeListener() {
        // public void onSelectedDayChange(CalendarView view, int year, int
        // month, int monthDay) {
        // setDate(year, month, monthDay);
        // updateSpinners();
        // notifyDateChanged();
        // }
        // });

        // day
        mDaySpinner = (NumberPicker) findViewById(R.id.day);
        mDaySpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        mDaySpinner.setOnLongPressUpdateInterval(100);
        mDaySpinner.setOnValueChangedListener(onChangeListener);
        mDaySpinnerInput = (EditText) mDaySpinner.findViewById(R.id.np__numberpicker_input);

        // month
        mMonthSpinner = (NumberPicker) findViewById(R.id.month);
        mMonthSpinner.setMinValue(0);
        mMonthSpinner.setMaxValue(mNumberOfMonths - 1);
        mMonthSpinner.setDisplayedValues(mShortMonths);
        mMonthSpinner.setOnLongPressUpdateInterval(200);
        mMonthSpinner.setOnValueChangedListener(onChangeListener);
        mMonthSpinnerInput = (EditText) mMonthSpinner.findViewById(R.id.np__numberpicker_input);

        // year
        mYearSpinner = (NumberPicker) findViewById(R.id.year);
        mYearSpinner.setOnLongPressUpdateInterval(100);
        mYearSpinner.setOnValueChangedListener(onChangeListener);
        mYearSpinnerInput = (EditText) mYearSpinner.findViewById(R.id.np__numberpicker_input);

        // show only what the user required but make sure we
        // show something and the spinners have higher priority
        if (!spinnersShown && !calendarViewShown) {
            setSpinnersShown(true);
        } else {
            setSpinnersShown(spinnersShown);
            // setCalendarViewShown(calendarViewShown);
        }

        // set the min date giving priority of the minDate over startYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(minDate)) {
            if (!parseDate(minDate, mTempDate)) {
                mTempDate.set(startYear, 0, 1);
            }
        } else {
            mTempDate.set(startYear, 0, 1);
        }
        setMinDate(mTempDate.getTimeInMillis());

        // set the max date giving priority of the maxDate over endYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(maxDate)) {
            if (!parseDate(maxDate, mTempDate)) {
                mTempDate.set(endYear, 11, 31);
            }
        } else {
            mTempDate.set(endYear, 11, 31);
        }
        setMaxDate(mTempDate.getTimeInMillis());

        // initialize to current date
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH), null);

        computeCurrentChineseDate();

        // re-order the number spinners to match the current date format
        reorderSpinners();

        // accessibility
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    private void computeCurrentChineseDate() {
        ChineseCalendar chinese = new ChineseCalendar(mCurrentDate);
        mCurrentChineseYear = chinese.get(ChineseCalendar.CHINESE_YEAR);
        mCurrentChineseMonth = chinese.get(ChineseCalendar.CHINESE_MONTH);
        mCurrentChineseDay = chinese.get(ChineseCalendar.CHINESE_DATE);
    }

    /**
     * Sets the minimal date supported by this {@link NumberPicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link java.util.TimeZone#getDefault()} time zone.
     *
     * @param minDate The minimal supported date.
     */
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);

        mMinChineseDate = new int[]{1901, 1, 1};
//		Log.d("picker", "min chinese date is " + mMinChineseDate[0] + "/" + mMinChineseDate[1]
//				+ "/" + mMinChineseDate[2]);
        // mCalendarView.setMinDate(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
            // updateCalendarView();
        }
        updateSpinners();
    }

    /**
     * Gets the minimal date supported by this {@link DatePicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link java.util.TimeZone#getDefault()} time zone.
     * <p>
     * Note: The default minimal date is 01/01/1900.
     * <p>
     *
     * @return The minimal supported date.
     */
    // public long getMinDate() {
    // return mCalendarView.getMinDate();
    // }

    /**
     * Sets the maximal date supported by this {@link DatePicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link java.util.TimeZone#getDefault()} time zone.
     *
     * @param maxDate The maximal supported date.
     */
    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);

        mMaxChineseDate = new int[]{2099, 1, 1};
//		Log.d("picker", "max chinese date is " + mMaxChineseDate[0] + "/" + mMaxChineseDate[1]
//				+ "/" + mMaxChineseDate[2]);
        // mCalendarView.setMaxDate(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
            // updateCalendarView();
        }
        updateSpinners();
    }

    /**
     * Gets the maximal date supported by this {@link DatePicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link java.util.TimeZone#getDefault()} time zone.
     * <p/>
     * Note: The default maximal date is 12/31/2100.
     * <p/>
     *
     * @return The maximal supported date.
     */
    // public long getMaxDate() {
    // return mCalendarView.getMaxDate();
    // }
    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mDaySpinner.setEnabled(enabled);
        mMonthSpinner.setEnabled(enabled);
        mYearSpinner.setEnabled(enabled);
        // mCalendarView.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    public void setIsChineseCalendar(boolean isChineseCalendar) {
        mIsChineseCalendar = isChineseCalendar;
        updateDate(mSelectedDate.get(Calendar.YEAR), mSelectedDate.get(Calendar.MONTH),
                mSelectedDate.get(Calendar.DAY_OF_MONTH));
    }

    public boolean isChineseCalendar() {
        return mIsChineseCalendar;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(),
                mCurrentDate.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(DatePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(DatePicker.class.getName());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Gets whether the {@link CalendarView} is shown.
     *
     * @return True if the calendar view is shown.
     * @see #getCalendarView()
     */
    public boolean getCalendarViewShown() {
        // return mCalendarView.isShown();
        return false;
    }

    /**
     * Gets whether the spinners are shown.
     *
     * @return True if the spinners are shown.
     */
    public boolean getSpinnersShown() {
        return mSpinners.isShown();
    }

    /**
     * Gets the {@link CalendarView}.
     *
     * @return The calendar view.
     * @see #getCalendarViewShown()
     */
    // public CalendarView getCalendarView() {
    // return mCalendarView;
    // }

    /**
     * Sets whether the {@link CalendarView} is shown.
     *
     * @param shown
     *            True if the calendar view is to be shown.
     */
    // public void setCalendarViewShown(boolean shown) {
    // mCalendarView.setVisibility(shown ? VISIBLE : GONE);
    // }

    /**
     * Sets whether the spinners are shown.
     *
     * @param shown True if the spinners are to be shown.
     */
    public void setSpinnersShown(boolean shown) {
        mSpinners.setVisibility(shown ? VISIBLE : GONE);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);

        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
        mShortMonths = new String[mNumberOfMonths];
        mChineseMonths = new String[mNumberOfMonths];
        for (int i = 0; i < mNumberOfMonths; i++) {
            mShortMonths[i] = String.format("%d", i + 1);
            mChineseMonths[i] = ChineseCalendar.chineseMonthNames[i + 1] + "月";
        }

        computeChineseDate();
    }

    private void computeChineseDate() {
        ChineseCalendar c = new ChineseCalendar(mCurrentDate);
        int numberOfDays = LunarCalendar.daysInMonth(c.get(ChineseCalendar.CHINESE_YEAR),
                c.get(ChineseCalendar.CHINESE_MONTH), false);
//		Log.d("picker", "numbers of days chinese month is " + numberOfDays);
        mNumberOfChineseDays = new String[numberOfDays];
        for (int i = 0; i < numberOfDays; i++) {
            mNumberOfChineseDays[i] = ChineseCalendar.chineseDateNames[i + 1];
        }
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given
     * calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale      The locale.
     */
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    /**
     * Reorders the spinners according to the date format that is explicitly set
     * by the user and if no such is set fall back to the current locale's
     * default format.
     */
    private void reorderSpinners() {
        mSpinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.DATE:
                    mSpinners.addView(mDaySpinner);
                    setImeOptions(mDaySpinner, spinnerCount, i);
                    break;
                case DateFormat.MONTH:
                    mSpinners.addView(mMonthSpinner);
                    setImeOptions(mMonthSpinner, spinnerCount, i);
                    break;
                case DateFormat.YEAR:
                    mSpinners.addView(mYearSpinner);
                    setImeOptions(mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Updates the current date.
     *
     * @param year       The year.
     * @param month      The month which is <strong>starting from zero</strong>.
     * @param dayOfMonth The day of the month.
     */
    public void updateDate(int year, int month, int dayOfMonth) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return;
        }
        setDate(year, month, dayOfMonth);
        if (mSelectedDate == null) {
            mSelectedDate = Calendar.getInstance();
        }
        mSelectedDate.set(year, month, dayOfMonth);
        computeCurrentChineseDate();
        updateSpinners();
        // updateCalendarView();
        notifyDateChanged();
    }

    // Override so we are in complete control of save / restore for this widget.
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        // updateCalendarView();
    }

    /**
     * Initialize the state. If the provided values designate an inconsistent
     * date the values are normalized before updating the spinners.
     *
     * @param year                  The initial year.
     * @param monthOfYear           The initial month <strong>starting from zero</strong>.
     * @param dayOfMonth            The initial day of the month.
     * @param onDateChangedListener How user is notified date is changed by user, can be null.
     */
    public void init(int year, int monthOfYear, int dayOfMonth,
            OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        // updateCalendarView();
        mOnDateChangedListener = onDateChangedListener;
    }

    /**
     * Parses the given <code>date</code> and in case of success sets the result
     * to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth || mCurrentDate
                .get(Calendar.DAY_OF_MONTH) != month);
    }

    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (!mIsChineseCalendar) {
            if (mCurrentDate.before(mMinDate)) {
                mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
            } else if (mCurrentDate.after(mMaxDate)) {
                mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
            }
        }
    }

    private void updateSpinners() {
        if (mIsChineseCalendar) {
            if (isCurrentChineseDate(mMinChineseDate)) {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(mCurrentChineseDay - 1);
                mDaySpinner.setMaxValue(mNumberOfChineseDays.length - 1);
                mDaySpinner.setWrapSelectorWheel(false);

                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(mCurrentChineseMonth - 1);
                mMonthSpinner.setMaxValue(11);
                mMonthSpinner.setWrapSelectorWheel(false);
            } else if (isCurrentChineseDate(mMaxChineseDate)) {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(0);
                mDaySpinner.setMaxValue(mCurrentChineseDay - 1);
                mDaySpinner.setWrapSelectorWheel(false);
                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(0);
                mMonthSpinner.setMaxValue(mCurrentChineseMonth - 1);
                mMonthSpinner.setWrapSelectorWheel(false);
            } else {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(0);
                mDaySpinner.setMaxValue(mNumberOfChineseDays.length - 1);
                mDaySpinner.setWrapSelectorWheel(true);
                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(0);
                mMonthSpinner.setMaxValue(11);
                mMonthSpinner.setWrapSelectorWheel(true);
            }
        } else {
            // set the spinner ranges respecting the min and max dates
            if (mCurrentDate.equals(mMinDate)) {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
                mDaySpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                mDaySpinner.setWrapSelectorWheel(false);

                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(mCurrentDate.get(Calendar.MONTH));
                mMonthSpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.MONTH));
                mMonthSpinner.setWrapSelectorWheel(false);
            } else if (mCurrentDate.equals(mMaxDate)) {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
                mDaySpinner.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
                mDaySpinner.setWrapSelectorWheel(false);
                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(mCurrentDate.getActualMinimum(Calendar.MONTH));
                mMonthSpinner.setMaxValue(mCurrentDate.get(Calendar.MONTH));
                mMonthSpinner.setWrapSelectorWheel(false);
            } else {
                mDaySpinner.setDisplayedValues(null);
                mDaySpinner.setMinValue(1);
                mDaySpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                mDaySpinner.setWrapSelectorWheel(true);
                mMonthSpinner.setDisplayedValues(null);
                mMonthSpinner.setMinValue(0);
                mMonthSpinner.setMaxValue(11);
                mMonthSpinner.setWrapSelectorWheel(true);
            }
        }

        // make sure the month names are a zero based array
        // with the months in the month spinner
        String[] displayedValues = mIsChineseCalendar ? mChineseMonths : CVArrays.copyOfRange(
                mShortMonths, mMonthSpinner.getMinValue(), mMonthSpinner.getMaxValue() + 1);
        mMonthSpinner.setDisplayedValues(displayedValues);

        if (mIsChineseCalendar) {
            mDaySpinner.setDisplayedValues(mNumberOfChineseDays);
        }

        // year spinner range does not change based on the current date
        mYearSpinner.setMinValue(mIsChineseCalendar ? mMinChineseDate[0] : mMinDate
                .get(Calendar.YEAR));
        mYearSpinner.setMaxValue(mIsChineseCalendar ? mMaxChineseDate[0] : mMaxDate
                .get(Calendar.YEAR));
        mYearSpinner.setWrapSelectorWheel(false);

        // set the spinner values
        mYearSpinner.setValue(mIsChineseCalendar ? mCurrentChineseYear : mCurrentDate
                .get(Calendar.YEAR));

        mMonthSpinner.setValue(mIsChineseCalendar ? mCurrentChineseMonth - 1 : mCurrentDate
                .get(Calendar.MONTH));

        mDaySpinner.setValue(mIsChineseCalendar ? mCurrentChineseDay - 1 : mCurrentDate
                .get(Calendar.DAY_OF_MONTH));
    }

    private boolean isCurrentChineseDate(int[] date) {
        return date[0] == mCurrentChineseYear && date[1] == mCurrentChineseMonth
                && date[2] == mCurrentChineseDay;
    }

    /**
     * @return The selected year.
     */
    public int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    /**
     * Updates the calendar view with the current date.
     */
    // private void updateCalendarView() {
    // mCalendarView.setDate(mCurrentDate.getTimeInMillis(), false, false);
    // }

    /**
     * @return The selected month.
     */
    public int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    /**
     * @return The selected day of month.
     */
    public int getDayOfMonth() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    /**
     * Sets the IME options for a spinner based on its ordering.
     *
     * @param spinner      The spinner.
     * @param spinnerCount The total spinner count.
     * @param spinnerIndex The index of the given spinner.
     */
    private void setImeOptions(NumberPicker spinner, int spinnerCount, int spinnerIndex) {
        final int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        TextView input = (TextView) spinner.findViewById(R.id.np__numberpicker_input);
        input.setImeOptions(imeOptions);
    }

    @SuppressWarnings("unused")
    private void setContentDescriptions() {
        if (true) {
            return; // increment/decrement buttons don't exist in backport
        }
        // Day
        trySetContentDescription(mDaySpinner, R.id.np__increment,
                R.string.date_picker_increment_day_button);
        trySetContentDescription(mDaySpinner, R.id.np__decrement,
                R.string.date_picker_decrement_day_button);
        // Month
        trySetContentDescription(mMonthSpinner, R.id.np__increment,
                R.string.date_picker_increment_month_button);
        trySetContentDescription(mMonthSpinner, R.id.np__decrement,
                R.string.date_picker_decrement_month_button);
        // Year
        trySetContentDescription(mYearSpinner, R.id.np__increment,
                R.string.date_picker_increment_year_button);
        trySetContentDescription(mYearSpinner, R.id.np__decrement,
                R.string.date_picker_decrement_year_button);
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(getContext().getString(contDescResId));
        }
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        // InputMethodManager inputMethodManager =
        // InputMethodManager.peekInstance();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mYearSpinnerInput)) {
                mYearSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMonthSpinnerInput)) {
                mMonthSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mDaySpinnerInput)) {
                mDaySpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    /**
     * The callback used to indicate the user changes\d the date.
     */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with
         *                    {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private final int mYear;

        private final int mMonth;

        private final int mDay;

        /**
         * Constructor called from {@link DatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
        }
    }
}