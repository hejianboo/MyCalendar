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

    public int id;

    public String accountName;

    public String displayName;

    public String ownerAccount;

    public String calendarName;

    public String accountType;

    public int visible;

    public CalendarInfo() {
        id = 0;
        accountName = "";
        displayName = "";
        ownerAccount = "";
        calendarName = "";
        accountType = "";
        visible = 1;
    }

    public CalendarInfo(Parcel in) {
        id = in.readInt();
        accountName = in.readString();
        displayName = in.readString();
        ownerAccount = in.readString();
        calendarName = in.readString();
        accountType = in.readString();
        visible = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(accountName);
        dest.writeString(displayName);
        dest.writeString(ownerAccount);
        dest.writeString(calendarName);
        dest.writeString(accountType);
        dest.writeInt(visible);
    }

    @Override
    public String toString() {
        return "id = " + id + "\r\n" +
                "accountName = " + accountName + "\r\n" +
                "displayName = " + displayName + "\r\n" +
                "ownerAccount = " + ownerAccount + "\r\n" +
                "calendarName = " + calendarName + "\r\n" +
                "accountType = " + accountType + "\r\n" +
                "visible = " + visible;
    }
}
