package com.name.myassistant.m;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.name.myassistant.alarm.AlarmReceiver;

import java.util.Calendar;

/**
 * 闹钟实体类
 */
public class Alarm {
    public int id;
    public String hour;
    public String minute;
    public String note;
    public String weatherAddress;
    public boolean open;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    public Alarm(Context context,int id, String hour, String minute, String note, String weatherAddress) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.note = note;
        this.weatherAddress = weatherAddress;
        this.open = true;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("info",note);

        // The pending intent that is triggered when the alarm fires.
        alarmIntent= PendingIntent.getBroadcast(context, id, intent, 0);

        setOpen(context,true);
    }

    public void setOpen(Context context,boolean open){
        if(this.open==open){
            return;
        }
        this.open=open;
        if(open){
            alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
            calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
            return;
        }
        if(alarmManager!=null){
            alarmManager.cancel(alarmIntent);
        }
    }
}
