package com.name.myassistant;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.name.myassistant.util.LogUtil;

public class RobotSettingActivity extends AppCompatActivity{
    private AlarmReceiver alarmReceiver;
    int hourOfDay;
    int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_setting);
        CheckBox readShortMessageCheckBox=(CheckBox)findViewById(R.id.read_short_message_check);
        CheckBox reportWeatherOnTimeCheckBox=(CheckBox)findViewById(R.id.report_weather_on_time);
        
        readShortMessageCheckBox.setChecked(AppConfig.READ_SHORT_MESSAGE_PERMISSION);
        reportWeatherOnTimeCheckBox.setChecked(AppConfig.REPORT_WEATHER_ON_TIME);
        
        readShortMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtil.d("xzx", "readShortMessage=> " + isChecked);
                AppConfig.READ_SHORT_MESSAGE_PERMISSION = isChecked;
                SharedPreferences sharedPreferences = getSharedPreferences("myAssistant", MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("readShortMessagePermission", isChecked).apply();
            }
        });
        reportWeatherOnTimeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarmReceiver=new AlarmReceiver();
                hourOfDay=9;
                minute=30;
                if(isChecked){
                    // TODO: 16-2-23 弹出时间选择滚轮给用户设置时间
                    TimePickerDialog dialog=new TimePickerDialog(RobotSettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            LogUtil.d("xzx","hourOfDay=> "+hourOfDay+" minute=> "+minute);
                            alarmReceiver.setAlarm(RobotSettingActivity.this,hourOfDay,minute);
                        }
                    },hourOfDay,minute,true);
                    dialog.show();
                }else{
                    alarmReceiver.cancelAlarm(RobotSettingActivity.this);
                }
            }
        });
    }
}
