package com.hjbalan.mycalendar.event;

import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by alan on 14/10/30.
 */
public class CalendarEventModel {

    public static final int REMINDER_MINUTES_DEFAULT = 5;

    public String mUri = null;

    public long mId = -1;

    public long mCalendarId = -1;

    public String mCalendarDisplayName = ""; // Make sure this is in sync with the mCalendarId

    public String mCalendarAccountName;

    public String mCalendarAccountType;

    public String mCalendarAllowedReminders;

    public int mCalendarMaxReminders;

    public String mOwnerAccount = null;

    public String mTitle = null;

    public String mLocation = null;

    public String mDescription = null;

    public String mDuration = null;

    // This should be set the same as mStart when created and is used for making changes to
    // recurring events. It should not be updated after it is initially set.
    public long mOriginalStart = -1;

    public long mStart = -1;

    // This should be set the same as mEnd when created and is used for making changes to
    // recurring events. It should not be updated after it is initially set.
    public long mOriginalEnd = -1;

    public long mEnd = -1;

    public boolean mAllDay = false;

    public boolean mHasAlarm = false;

    public ArrayList<ReminderEntry> mReminders;

    public ArrayList<ReminderEntry> mDefaultReminders;

    public boolean mModelUpdatedWithEventCursor;

    public CalendarEventModel() {
        mReminders = new ArrayList<ReminderEntry>();
        mDefaultReminders = new ArrayList<ReminderEntry>();
        mHasAlarm = true;
        mReminders.add(ReminderEntry.valueOf(REMINDER_MINUTES_DEFAULT));
        mDefaultReminders.add(ReminderEntry.valueOf(REMINDER_MINUTES_DEFAULT));
    }

    public boolean isValid() {
        if (mCalendarId == -1) {
            return false;
        }
        if (TextUtils.isEmpty(mOwnerAccount)) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        if (mTitle != null && mTitle.trim().length() > 0) {
            return false;
        }

        if (mLocation != null && mLocation.trim().length() > 0) {
            return false;
        }

        if (mDescription != null && mDescription.trim().length() > 0) {
            return false;
        }

        return true;
    }

    public void clear() {
        mUri = null;
        mId = -1;
        mCalendarId = -1;
        mOwnerAccount = null;

        mTitle = null;
        mLocation = null;
        mDescription = null;

        mOriginalStart = -1;
        mStart = -1;
        mOriginalEnd = -1;
        mEnd = -1;
        mDuration = null;
        mAllDay = false;
        mHasAlarm = false;

        mModelUpdatedWithEventCursor = false;

        mReminders = new ArrayList<ReminderEntry>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mAllDay ? 1231 : 1237);
        result = prime * result + (int) (mCalendarId ^ (mCalendarId >>> 32));
        result = prime * result + ((mDescription == null) ? 0 : mDescription.hashCode());
        result = prime * result + ((mDuration == null) ? 0 : mDuration.hashCode());
        result = prime * result + (int) (mEnd ^ (mEnd >>> 32));
        result = prime * result + (mHasAlarm ? 1231 : 1237);
        result = prime * result + (int) (mId ^ (mId >>> 32));
        result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
        result = prime * result + (int) (mOriginalEnd ^ (mOriginalEnd >>> 32));
        result = prime * result + (int) (mOriginalStart ^ (mOriginalStart >>> 32));
        result = prime * result + ((mOwnerAccount == null) ? 0 : mOwnerAccount.hashCode());
        result = prime * result + ((mReminders == null) ? 0 : mReminders.hashCode());
        result = prime * result + (int) (mStart ^ (mStart >>> 32));
        result = prime * result + ((mTitle == null) ? 0 : mTitle.hashCode());
        result = prime * result + ((mUri == null) ? 0 : mUri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CalendarEventModel)) {
            return false;
        }

        CalendarEventModel other = (CalendarEventModel) obj;
        if (!checkOriginalModelFields(other)) {
            return false;
        }

        if (mLocation == null) {
            if (other.mLocation != null) {
                return false;
            }
        } else if (!mLocation.equals(other.mLocation)) {
            return false;
        }

        if (mTitle == null) {
            if (other.mTitle != null) {
                return false;
            }
        } else if (!mTitle.equals(other.mTitle)) {
            return false;
        }

        if (mDescription == null) {
            if (other.mDescription != null) {
                return false;
            }
        } else if (!mDescription.equals(other.mDescription)) {
            return false;
        }

        if (mDuration == null) {
            if (other.mDuration != null) {
                return false;
            }
        } else if (!mDuration.equals(other.mDuration)) {
            return false;
        }

        if (mEnd != other.mEnd) {
            return false;
        }

        if (mOriginalEnd != other.mOriginalEnd) {
            return false;
        }

        if (mOriginalStart != other.mOriginalStart) {
            return false;
        }

        if (mStart != other.mStart) {
            return false;
        }

        return true;
    }

    /**
     * Whether the event has been modified based on its original model.
     *
     * @return true if the model is unchanged, false otherwise
     */
    public boolean isUnchanged(CalendarEventModel originalModel) {
        if (this == originalModel) {
            return true;
        }
        if (originalModel == null) {
            return false;
        }

        if (!checkOriginalModelFields(originalModel)) {
            return false;
        }

        if (TextUtils.isEmpty(mLocation)) {
            if (!TextUtils.isEmpty(originalModel.mLocation)) {
                return false;
            }
        } else if (!mLocation.equals(originalModel.mLocation)) {
            return false;
        }

        if (TextUtils.isEmpty(mTitle)) {
            if (!TextUtils.isEmpty(originalModel.mTitle)) {
                return false;
            }
        } else if (!mTitle.equals(originalModel.mTitle)) {
            return false;
        }

        if (TextUtils.isEmpty(mDescription)) {
            if (!TextUtils.isEmpty(originalModel.mDescription)) {
                return false;
            }
        } else if (!mDescription.equals(originalModel.mDescription)) {
            return false;
        }

        if (TextUtils.isEmpty(mDuration)) {
            if (!TextUtils.isEmpty(originalModel.mDuration)) {
                return false;
            }
        } else if (!mDuration.equals(originalModel.mDuration)) {
            return false;
        }

        if (mEnd != mOriginalEnd) {
            return false;
        }
        if (mStart != mOriginalStart) {
            return false;
        }

        return true;
    }

    /**
     * Checks against an original model for changes to an event. This covers all
     * the fields that should remain consistent between an original event model
     * and the new one if nothing in the event was modified. This is also the
     * portion that overlaps with equality between two event models.
     *
     * @return true if these fields are unchanged, false otherwise
     */
    protected boolean checkOriginalModelFields(CalendarEventModel originalModel) {
        if (mAllDay != originalModel.mAllDay) {
            return false;
        }

        if (mCalendarId != originalModel.mCalendarId) {
            return false;
        }
        if (mHasAlarm != originalModel.mHasAlarm) {
            return false;
        }
        if (mId != originalModel.mId) {
            return false;
        }

        if (mOwnerAccount == null) {
            if (originalModel.mOwnerAccount != null) {
                return false;
            }
        } else if (!mOwnerAccount.equals(originalModel.mOwnerAccount)) {
            return false;
        }

        if (mReminders == null) {
            if (originalModel.mReminders != null) {
                return false;
            }
        } else if (!mReminders.equals(originalModel.mReminders)) {
            return false;
        }

        if (mUri == null) {
            if (originalModel.mUri != null) {
                return false;
            }
        } else if (!mUri.equals(originalModel.mUri)) {
            return false;
        }

        return true;
    }

    /**
     * Sort and uniquify mReminderMinutes.
     *
     * @return true (for convenience of caller)
     */
    public boolean normalizeReminders() {
        if (mReminders.size() <= 1) {
            return true;
        }

        // sort
        Collections.sort(mReminders);

        // remove duplicates
        ReminderEntry prev = mReminders.get(mReminders.size() - 1);
        for (int i = mReminders.size() - 2; i >= 0; --i) {
            ReminderEntry cur = mReminders.get(i);
            if (prev.equals(cur)) {
                // match, remove later entry
                mReminders.remove(i + 1);
            }
            prev = cur;
        }

        return true;
    }

    public static class ReminderEntry implements Comparable<ReminderEntry>, Serializable {

        private final int mMinutes;

        private final int mMethod;

        private ReminderEntry(int minutes, int method) {
            mMinutes = minutes;
            mMethod = method;
        }

        public static ReminderEntry valueOf(int minutes, int method) {
            return new ReminderEntry(minutes, method);
        }

        public static ReminderEntry valueOf(int minutes) {
            return valueOf(minutes, Reminders.METHOD_ALERT);
        }

        @Override
        public int hashCode() {
            return mMinutes * 10 + mMethod;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReminderEntry)) {
                return false;
            }

            ReminderEntry re = (ReminderEntry) obj;

            if (re.mMinutes != mMinutes) {
                return false;
            }

            // Treat ALERT and DEFAULT as equivalent.  This is useful during the "has anything
            // "changed" test, so that if DEFAULT is present, but we don't change anything,
            // the internal conversion of DEFAULT to ALERT doesn't force a database update.
            return re.mMethod == mMethod ||
                    (re.mMethod == Reminders.METHOD_DEFAULT && mMethod == Reminders.METHOD_ALERT) ||
                    (re.mMethod == Reminders.METHOD_ALERT && mMethod == Reminders.METHOD_DEFAULT);
        }

        @Override
        public String toString() {
            return "ReminderEntry min=" + mMinutes + " meth=" + mMethod;
        }

        /**
         * Comparison function for a sort ordered primarily descending by minutes,
         * secondarily ascending by method type.
         */
        @Override
        public int compareTo(ReminderEntry re) {
            if (re.mMinutes != mMinutes) {
                return re.mMinutes - mMinutes;
            }
            if (re.mMethod != mMethod) {
                return mMethod - re.mMethod;
            }
            return 0;
        }

        /** Returns the minutes. */
        public int getMinutes() {
            return mMinutes;
        }

        /** Returns the alert method. */
        public int getMethod() {
            return mMethod;
        }
    }
}
