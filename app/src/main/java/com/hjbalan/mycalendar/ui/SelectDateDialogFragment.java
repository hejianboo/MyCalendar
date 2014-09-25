package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.ui.BaseDialogFragment.MyDialogInterface.OnDialogButtonClickListener;
import com.hjbalan.mycalendar.ui.widget.DatePicker;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.util.Calendar;

public class SelectDateDialogFragment extends BaseDialogFragment
        implements OnCheckedChangeListener {

    public static final String TAG = "selectDate";

    public static final String SELECTED_DATE = "selected_date";

    private DatePicker mDatePicker;

    private RadioGroup mRgChangeDateType;

    public static SelectDateDialogFragment newInstance(Context ctx,
            OnDialogButtonClickListener listener) {
        BaseDialogFragment.Builder builder = new Builder(ctx);
        builder.setCancelable(true).setCancelableOutside(false)
                .setContentView(LayoutInflater.from(ctx).inflate(R.layout.view_select_date, null))
                .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.ok, listener);
        SelectDateDialogFragment fragment = new SelectDateDialogFragment();
        fragment.setBuilder(builder);
        return fragment;
    }

    @Override
    protected void customContentView(View contentView) {
        boolean isShowChineseCalendar = MyPreferencesManager.getInstance().isShowChineseCalendar();
        mRgChangeDateType = (RadioGroup) contentView.findViewById(R.id.rg_date_type);
        mDatePicker = (DatePicker) contentView.findViewById(R.id.date_picker);
        if (isShowChineseCalendar) {
            mRgChangeDateType.setVisibility(View.VISIBLE);
            mRgChangeDateType.setOnCheckedChangeListener(this);
        } else {
            mRgChangeDateType.setVisibility(View.GONE);
            mRgChangeDateType.setOnCheckedChangeListener(null);
        }

        long selectedDate = getArguments().getLong(SELECTED_DATE, System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selectedDate);
        mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
    }

    public Calendar getSelectedDate() {
        Calendar calendar = null;
        if (mDatePicker != null) {
            calendar = Calendar.getInstance();
            calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(),
                    mDatePicker.getDayOfMonth());
        }
        return calendar;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        boolean isShowChineseCalendar = false;
        switch (checkedId) {
            case R.id.rb_chinese:
                isShowChineseCalendar = true;
                break;

            case R.id.rb_gregorian:
                isShowChineseCalendar = false;
                break;

            default:
                break;
        }
        mDatePicker.setIsChineseCalendar(isShowChineseCalendar);
    }

}
