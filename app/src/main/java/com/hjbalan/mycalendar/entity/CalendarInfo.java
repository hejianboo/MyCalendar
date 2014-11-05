package com.hjbalan.mycalendar.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alan on 14-9-30.
 */
public class CalendarInfo implements Parcelable {

    public static final Creator<CalendarInfo> CREATOR = new Creator<CalendarInfo>() {

        @Override
        public CalendarInfo createFromParcel(Parcel source) {
            return new CalendarInfo(source);
        }

        @Override
        public CalendarInfo[] newArray(int size) {
            return new CalendarInfo[size];
        }
    };

    public long id;

    public String accountName;

    public String displayName;

    public String ownerAccount;

    public String accountType;

    public String allowedReminders;

    public int maxReminders;


    public CalendarInfo() {
        id = 0;
        accountName = "";
        displayName = "";
        ownerAccount = "";
        accountType = "";
        allowedReminders = "";
        maxReminders = 0;
    }

    public CalendarInfo(Parcel in) {
        id = in.readLong();
        accountName = in.readString();
        displayName = in.readString();
        ownerAccount = in.readString();
        accountType = in.readString();
        allowedReminders = in.readString();
        maxReminders = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(accountName);
        dest.writeString(displayName);
        dest.writeString(ownerAccount);
        dest.writeString(accountType);
        dest.writeString(allowedReminders);
        dest.writeInt(maxReminders);
    }

    @Override
    public String toString() {
        return "id = " + id + "\r\n" +
                "accountName = " + accountName + "\r\n" +
                "displayName = " + displayName + "\r\n" +
                "ownerAccount = " + ownerAccount + "\r\n" +
                "accountType = " + accountType + "\r\n" +
                "allowedReminders = " + allowedReminders +
                "maxReminders = " + maxReminders;
    }
}
