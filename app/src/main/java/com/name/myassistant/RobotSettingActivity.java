package com.name.myassistant;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.name.myassistant.util.LogUtil;

public class RobotSettingActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_setting);
        setTitle("");

        Toolbar toolbar=(Toolbar)findViewById(R.id.robot_setting_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        CheckBox readShortMessageCheckBox=(CheckBox)findViewById(R.id.read_short_message_check);
        TextView alarmSettingTextView=(TextView)findViewById(R.id.alarm_setting);
        
        readShortMessageCheckBox.setChecked(AppConfig.READ_SHORT_MESSAGE_PERMISSION);
        alarmSettingTextView.setOnClickListener(this);

        final SharedPreferences sharedPreferences = getSharedPreferences("myassistant", MODE_PRIVATE);
        
        readShortMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtil.d("xzx", "readShortMessage=> " + isChecked);
                AppConfig.READ_SHORT_MESSAGE_PERMISSION = isChecked;

                sharedPreferences.edit().putBoolean("readShortMessagePermission", isChecked).apply();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.alarm_setting:
                Intent intent=new Intent(RobotSettingActivity.this,AlarmSettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
