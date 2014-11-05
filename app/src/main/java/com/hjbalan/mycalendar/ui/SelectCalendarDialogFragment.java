package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alan on 14/10/20.
 */
public class SelectCalendarDialogFragment extends BaseDialogFragment {

    public static final String TAG = "selectCalendar";

    private SelectCalendarListener mListener;

    public static SelectCalendarDialogFragment newInstance(Context ctx,
            ArrayList<CalendarInfo> calendars, long selectedCalendarId) {
        BaseDialogFragment.Builder builder = new Builder(ctx);
        builder.setContentView(
                LayoutInflater.from(ctx).inflate(R.layout.dialog_select_calendar, null))
                .setCancelable(true).setCancelableOutside(false)
                .setTitle(R.string.edit_event_select_calendar);
        SelectCalendarDialogFragment fragment = new SelectCalendarDialogFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList("calendars", calendars);
        b.putLong("selected_calendar_id", selectedCalendarId);
        fragment.setArguments(b);
        fragment.setBuilder(builder);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SelectCalendarListener) {
            mListener = (SelectCalendarListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundResource(R.color.c5);
        }
        return view;
    }

    @Override
    protected void customContentView(View contentView) {
        Bundle b = getArguments();
        long selectedCalendarId = b.getLong("selected_calendar_id", -1);
        ArrayList<CalendarInfo> calendars = b.getParcelableArrayList("calendars");
        ListView lv = (ListView) contentView.findViewById(R.id.lv_calendars);
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), calendars);
        adapter.setSelectedCalendarId(selectedCalendarId);
        lv.setAdapter(adapter);
    }

    public interface SelectCalendarListener {

        public void onCalendarSelected(CalendarInfo selectedCalendar);
    }

    static class ViewHolder {

        TextView tvCalendarAccount;

        RadioButton rbSelected;
    }

    private class CalendarAdapter extends BaseAdapter implements View.OnClickListener {

        private long selectedCalendarId = -1;

        private LayoutInflater inflater;

        private ArrayList<CalendarInfo> data;

        private CalendarAdapter(Context context, ArrayList<CalendarInfo> calendars) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            data = calendars;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_calendar_list, parent, false);
                holder.tvCalendarAccount = (TextView) convertView.findViewById(
                        R.id.tv_calendar_account);
                holder.rbSelected = (RadioButton) convertView.findViewById(R.id.rb_select_calendar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CalendarInfo calendarInfo = (CalendarInfo) getItem(position);
            holder.tvCalendarAccount.setText(calendarInfo.accountName);
            holder.rbSelected.setText(calendarInfo.displayName);
            holder.rbSelected.setChecked(calendarInfo.id == selectedCalendarId);
            holder.rbSelected.setOnClickListener(this);
            holder.rbSelected.setTag(position);

            return convertView;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setSelectedCalendarId(long selectedCalendarId) {
            this.selectedCalendarId = selectedCalendarId;
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            CalendarInfo calendarInfo = data.get(position);
            setSelectedCalendarId(calendarInfo.id);
            notifyDataSetChanged();
            dismiss();
            if (mListener != null) {
                mListener.onCalendarSelected(calendarInfo);
            }
        }
    }

}
