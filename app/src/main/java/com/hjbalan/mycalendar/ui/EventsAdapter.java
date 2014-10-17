package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.event.Event;

import org.joda.time.DateTimeUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class EventsAdapter extends BaseAdapter implements
        StickyListHeadersAdapter {

    private final ArrayList<Event> mEvents = new ArrayList<>();

    private LayoutInflater mInflater;

    public EventsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public ArrayList<Event> getEvents() {
        return mEvents;
    }

    public void setEvents(ArrayList<Event> events) {
        this.mEvents.clear();
        this.mEvents.addAll(events);
        notifyDataSetChanged();
    }

    public int getMinJulianDay() {
        if (getCount() == 0) {
            return 0;
        }
        return ((Event) getItem(0)).startDay;
    }

    public int getMaxJulianDay() {
        if (getCount() == 0) {
            return 0;
        }
        return ((Event) getItem(getCount() - 1)).startDay;
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_list_event, parent,
                    false);
            holder.ivImage = (ImageView) convertView
                    .findViewById(R.id.iv_image);
            holder.tvEvent = (TextView) convertView.findViewById(R.id.tv_event);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tvNoEvent = (TextView) convertView
                    .findViewById(R.id.tv_no_event);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = (Event) getItem(position);
        if (event.id <= 0) {
            holder.ivImage.setImageResource(R.drawable.ic_list_none);
            holder.tvNoEvent.setText(mInflater.getContext().getString(
                    R.string.no_event));
            holder.tvEvent.setText("");
            holder.tvTime.setText("");
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_list_default);
            holder.tvEvent.setText(event.title);
            if (event.allDay) {
                holder.tvTime.setText(mInflater.getContext().getString(R.string.edit_all_day));
            } else {

            }
            holder.tvNoEvent.setText("");
        }

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header_list_event, parent,
                    false);
            holder.tvText = (TextView) convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // set header text as first char in name
        holder.tvText
                .setText(getHeaderTitle(mEvents.get(position).startMillis));

        return convertView;
    }

    String getHeaderTitle(long millis) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(millis);
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int day2 = c2.get(Calendar.DAY_OF_MONTH);

        int diff = (int) Math
                .ceil((DateTimeUtils.toJulianDay(millis))
                        - (int) Math.ceil(DateTimeUtils.toJulianDay(c
                        .getTimeInMillis())));
        String s1 = "";
        String s2 = "";
        if (year == year2) {
            s1 = mInflater.getContext().getString(R.string.data_simple_format,
                    month2, day2);
        } else {
            s1 = mInflater.getContext().getString(R.string.date_full_format,
                    year2, month2, day2);
        }
        if (diff == 0) {
            s2 = mInflater.getContext().getString(R.string.today);
        } else if (diff == 1) {
            s2 = mInflater.getContext().getString(R.string.tomorrow);
        } else if (diff == 2) {
            s2 = mInflater.getContext().getString(R.string.day_after_tomorrow);
        } else if (diff == -1) {
            s2 = mInflater.getContext().getString(R.string.yestoday);
        } else if (diff > 0) {
            s2 = mInflater.getContext().getString(R.string.days_remain, diff);
        } else {
            s2 = mInflater.getContext().getString(R.string.days_passed, -diff);
        }
        return s1 + "·" + s2;
    }

    /**
     * Remember that these have to be static, postion=1 should always return the
     * same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        return mEvents.get(position).startDay;
    }

    public int indexOfStartDay(final long timeMillies) {
        int count = getCount();
        int startDay = (int) Math.ceil(DateTimeUtils.toJulianDay(timeMillies));
        for (int i = 0; i < count; i++) {
            Event event = (Event) getItem(i);
            if (event.startDay == startDay) {
                return i;
            }
        }
        return -1;
    }

    class HeaderViewHolder {

        TextView tvText;
    }

    class ViewHolder {

        ImageView ivImage;

        TextView tvEvent;

        TextView tvTime;

        TextView tvNoEvent;
    }

}
