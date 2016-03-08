package com.name.myassistant.alarm;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.name.myassistant.R;

/**
 *
 */
public class AlarmSettingFragment extends Fragment implements View.OnClickListener{
    AlarmSettingActivity activity;
    int hour;
    int displayMinute;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_alarm_setting,container,false);
        activity=(AlarmSettingActivity)getActivity();
        LinearLayout timeLayout=(LinearLayout)view.findViewById(R.id.time_layout);
        final TextView hourTextView=(TextView)view.findViewById(R.id.hour);
        final TextView minuteTextView=(TextView)view.findViewById(R.id.minute);
        TextView setAlarmTextView=(TextView)view.findViewById(R.id.set_alarm);
        setAlarmTextView.setOnClickListener(this);


        hour=Integer.valueOf(hourTextView.getText().toString());
        displayMinute =Integer.valueOf(minuteTextView.getText().toString());
        timeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour=hourOfDay;
                        displayMinute=minute;
                        hourTextView.setText(String.valueOf(hourOfDay));
                        minuteTextView.setText(String.valueOf(minute));
                    }
                },hour, displayMinute,true).show();
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_alarm:
                AlarmReceiver alarmReceiver=new AlarmReceiver();
                alarmReceiver.setAlarm(activity,hour, displayMinute);
                break;
            case R.id.time_layout:
                break;
            default:
                break;
        }
    }
}
