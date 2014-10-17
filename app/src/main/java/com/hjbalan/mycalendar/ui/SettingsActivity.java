package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;
import com.hjbalan.mycalendar.utils.MyPreferencesManager;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by alan on 14-9-25.
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private Switch mSwitchShowChineseCalendar;

    private TextView mTvVisibleCalendar;

    private boolean mLastShowChineseCalendarSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mSwitchShowChineseCalendar = (Switch) findViewById(R.id.switch_show_chinese);
        findViewById(R.id.tv_feedback).setOnClickListener(this);

        mLastShowChineseCalendarSetting = MyPreferencesManager.getInstance()
                .isShowChineseCalendar();
        mSwitchShowChineseCalendar.setChecked(mLastShowChineseCalendarSetting);
        mTvVisibleCalendar = (TextView) findViewById(R.id.tv_visible_calendar);

        mTvVisibleCalendar.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_feedback:
                sendFeedback();
                break;
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "hjbalan@gmail.com", null));
        String subject = getString(R.string.settings_feedback);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, subject));
    }

    @Override
    public void onBackPressed() {
        if (mLastShowChineseCalendarSetting != mSwitchShowChineseCalendar.isChecked()) {
            mLastShowChineseCalendarSetting = mSwitchShowChineseCalendar.isChecked();
            MyPreferencesManager.getInstance().saveShowChineseCalendarSetting(
                    mLastShowChineseCalendarSetting);
        }
        super.onBackPressed();
    }

}
