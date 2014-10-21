package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.entity.CalendarInfo;

import android.app.Activity;
import android.app.DialogFragment;
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
            ArrayList<CalendarInfo> calendars,
            int selectedPosition) {
        BaseDialogFragment.Builder builder = new Builder(ctx);
        builder.setContentView(
                LayoutInflater.from(ctx).inflate(R.layout.dialog_select_calendar, null))
                .setCancelable(true).setCancelableOutside(false)
                .setTitle(R.string.edit_event_select_calendar);
        SelectCalendarDialogFragment fragment = new SelectCalendarDialogFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList("calendars", calendars);
        b.putInt("selected_position", selectedPosition);
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
        int selectedPosition = b.getInt("selected_position", 0);
        ArrayList<CalendarInfo> calendars = b.getParcelableArrayList("calendars");
        ListView lv = (ListView) contentView.findViewById(R.id.lv_calendars);
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), calendars);
        adapter.setSelectedPosition(selectedPosition);
        lv.setAdapter(adapter);
    }

    public interface SelectCalendarListener {

        public void onCalendarSelected(DialogFragment dialogFragment, int position);
    }

    static class ViewHolder {

        TextView tvCalendarAccount;

        RadioButton rbSelected;
    }

    private class CalendarAdapter extends BaseAdapter implements View.OnClickListener {

        private int selectedPosition = 0;

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
            holder.rbSelected.setChecked(position == selectedPosition);
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

        public void setSelectedPosition(int selectedPosition) {
            this.selectedPosition = selectedPosition;
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            setSelectedPosition(position);
            notifyDataSetChanged();
            dismiss();
            if (mListener != null) {
                mListener.onCalendarSelected(SelectCalendarDialogFragment.this, position);
            }
        }
    }

}
