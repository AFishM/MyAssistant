package com.name.myassistant.deskclock.v;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.name.myassistant.R;
import com.name.myassistant.deskclock.Alarm;
import com.name.myassistant.deskclock.Alarms;
import com.name.myassistant.deskclock.DigitalClock;
import com.name.myassistant.deskclock.ToastMaster;
import com.name.myassistant.util.LogUtil;

import java.util.Calendar;

public class DeskClockMainActivity extends AppCompatActivity implements OnItemClickListener{


    private ListView mAlarmsList;
    private Cursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_clock);
        Toolbar toolbar=(Toolbar)findViewById(R.id.alarm_setting_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mAlarmsList = (ListView) findViewById(R.id.alarms_list);
        mCursor = Alarms.getAlarmsCursor(getContentResolver());
        AlarmTimeAdapter adapter = new AlarmTimeAdapter(this, mCursor);
        mAlarmsList.setAdapter(adapter);
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnItemClickListener(this);
        mAlarmsList.setOnCreateContextMenuListener(this);
    }

    /**
     * listview的适配器继承CursorAdapter
     * @author wangxianming
     * 也可以使用BaseAdapter
     */
    private class AlarmTimeAdapter extends CursorAdapter {
        public AlarmTimeAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = LayoutInflater.from(DeskClockMainActivity.this).inflate(R.layout.alarm_time, parent, false);

            DigitalClock digitalClock =
                    (DigitalClock) ret.findViewById(R.id.digitalClock);
            digitalClock.setLive(false);
            return ret;
        }

        //把view绑定cursor的每一项
        public void bindView(View view, Context context, Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);

            View indicator = view.findViewById(R.id.indicator);

            // Set the initial resource for the bar image.
            final ImageView barOnOff =
                    (ImageView) indicator.findViewById(R.id.bar_onoff);
            barOnOff.setImageResource(alarm.enabled ?
                    R.drawable.ic_indicator_on : R.drawable.ic_indicator_off);

            // Set the initial state of the clock "checkbox"
            final CheckBox clockOnOff =
                    (CheckBox) indicator.findViewById(R.id.clock_onoff);
            clockOnOff.setChecked(alarm.enabled);

            // Clicking outside the "checkbox" should also change the state.
            //对checkbox设置监听，使里外一致
            indicator.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        clockOnOff.toggle();
                        updateIndicatorAndAlarm(clockOnOff.isChecked(),
                                barOnOff, alarm);
                    }
            });

            DigitalClock digitalClock =
                    (DigitalClock) view.findViewById(R.id.digitalClock);

            // set the alarm text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minutes);
            digitalClock.updateTime(c);
            digitalClock.setTypeface(Typeface.DEFAULT);

            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView =
                    (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr =
                    alarm.daysOfWeek.toString(DeskClockMainActivity.this, false);
            if (!TextUtils.isEmpty(daysOfWeekStr)) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView =
                    (TextView) view.findViewById(R.id.label);
            if (!TextUtils.isEmpty(alarm.label)) {
                labelView.setText(alarm.label);
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }
        }
    }

    //更新checkbox
    private void updateIndicatorAndAlarm(boolean enabled, ImageView bar, Alarm alarm) {
        bar.setImageResource(enabled ? R.drawable.ic_indicator_on
                : R.drawable.ic_indicator_off);

        Alarms.enableAlarm(this, alarm.id, enabled);
        if (enabled) {
            SetAlarmActivity.popAlarmSetToast(this, alarm.hour, alarm.minutes,
                    alarm.daysOfWeek);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        // Inflate the menu from xml.
        getMenuInflater().inflate(R.menu.context_menu, menu);

        // Use the current item to create a custom view for the header.
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final Cursor c =
                (Cursor) mAlarmsList.getAdapter().getItem(info.position);
        final Alarm alarm = new Alarm(c);

        // Construct the Calendar to compute the time.
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
        cal.set(Calendar.MINUTE, alarm.minutes);
        final String time = Alarms.formatTime(this, cal);

        // Inflate the custom view and set each TextView's text.
        final View v = LayoutInflater.from(this).inflate(R.layout.context_menu_header, null);
        TextView timeTextView = (TextView) v.findViewById(R.id.header_time);
        timeTextView.setText(time);
        TextView labelTextView = (TextView) v.findViewById(R.id.header_label);
        labelTextView.setText(alarm.label);

        // Set the custom view on the menu.
        menu.setHeaderView(v);
        // Change the text based on the state of the alarm.
        if (alarm.enabled) {
            menu.findItem(R.id.enable_alarm).setTitle(R.string.disable_alarm);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_alarm:
                // Confirm that the alarm will be deleted.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_alarm))
                        .setMessage(getString(R.string.delete_alarm_confirm))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d,
                                                        int w) {
                                        Alarms.deleteAlarm(DeskClockMainActivity.this, id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.enable_alarm:
                final Cursor c = (Cursor) mAlarmsList.getAdapter()
                        .getItem(info.position);
                final Alarm alarm = new Alarm(c);
                Alarms.enableAlarm(this, alarm.id, !alarm.enabled);
                if (!alarm.enabled) {
                    SetAlarmActivity.popAlarmSetToast(this, alarm.hour, alarm.minutes,
                            alarm.daysOfWeek);
                }
                return true;

            case R.id.edit_alarm:
                Intent intent = new Intent(this, SetAlarmActivity.class);
                intent.putExtra(Alarms.ALARM_ID, id);
                startActivity(intent);
                return true;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_add_alarm:
                startActivity(new Intent(this, SetAlarmActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
        LogUtil.d("xzx","id=> "+id);
		Intent intent = new Intent(this, SetAlarmActivity.class);
        intent.putExtra(Alarms.ALARM_ID, (int) id);
        startActivity(intent);

	}

   @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
        mCursor.close();
    }
}