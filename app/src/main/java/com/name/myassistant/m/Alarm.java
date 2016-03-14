package com.name.myassistant.m;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.name.myassistant.alarm.AlarmReceiver;
import com.name.myassistant.util.LogUtil;

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

    public Alarm(int id, String hour, String minute, String note, String weatherAddress) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.note = note;
        this.weatherAddress = weatherAddress;
    }

    public void setOpen(Context context,boolean open){
        LogUtil.d("xzx");
        this.open=open;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("info",note);
        PendingIntent alarmIntent= PendingIntent.getBroadcast(context, id, intent, 0);

        AlarmManager alarmManager= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(open){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
            calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }else{
            alarmManager.cancel(alarmIntent);
        }
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", hour='" + hour + '\'' +
                ", minute='" + minute + '\'' +
                ", note='" + note + '\'' +
                ", weatherAddress='" + weatherAddress + '\'' +
                ", open=" + open +
                '}';
    }
}
