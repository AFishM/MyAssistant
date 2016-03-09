package com.name.myassistant.alarm;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.name.myassistant.GlobalVariable;
import com.name.myassistant.R;
import com.name.myassistant.m.Alarm;

/**
 *
 */
public class AlarmSettingFragment extends Fragment implements View.OnClickListener{
    AlarmSettingActivity activity;
    TextView hourTextView;
    TextView minuteTextView;
    EditText noteEditText;

    Alarm alarm;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_alarm_setting,container,false);
        activity=(AlarmSettingActivity)getActivity();

        LinearLayout timeLayout=(LinearLayout)view.findViewById(R.id.time_layout);
        hourTextView=(TextView)view.findViewById(R.id.hour);
        minuteTextView=(TextView)view.findViewById(R.id.minute);

        noteEditText=(EditText)view.findViewById(R.id.note);
        CheckBox weatherReportCheckCheckBox=(CheckBox)view.findViewById(R.id.weather_report_check);

        TextView setAlarmTextView=(TextView)view.findViewById(R.id.set_alarm);

        if(alarm!=null){
            hourTextView.setText(alarm.hour);
            minuteTextView.setText(alarm.minute);
            noteEditText.setText(alarm.note);
            if(alarm.weatherAddress!=null){
                weatherReportCheckCheckBox.setText(alarm.weatherAddress);
                weatherReportCheckCheckBox.setChecked(true);
            }
        }else{
            hourTextView.setText(getString(R.string.zero_eight));
            minuteTextView.setText(getString(R.string.zero_zero));
        }

        timeLayout.setOnClickListener(this);
        setAlarmTextView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_alarm:
                String hour=hourTextView.getText().toString();
                String minute=minuteTextView.getText().toString();
                String noteStr=noteEditText.getText().toString();
                if(alarm==null){
                    int alarmId= GlobalVariable.getInstance().getAlarmList().size();
                    alarm=new Alarm(v.getContext(),alarmId,hour,minute,noteStr,null);
                }else{
                    alarm.hour=hour;
                    alarm.minute=minute;
                    alarm.note=noteStr;
                }
                GlobalVariable.getInstance().save(v.getContext());
                break;
            case R.id.time_layout:
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour=String.valueOf(hourOfDay);
                        String displayMinute=String.valueOf(minute);
                        hourTextView.setText(hour);
                        minuteTextView.setText(displayMinute);
                    }
                },Integer.valueOf(hourTextView.getText().toString()),Integer.valueOf(minuteTextView.getText().toString()),true);
                break;
            default:
                break;
        }
    }
}
