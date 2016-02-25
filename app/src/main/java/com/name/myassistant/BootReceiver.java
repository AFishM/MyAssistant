package com.name.myassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.name.myassistant.util.LogUtil;

/**
 * Created by xu on 16-2-24.
 */
public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d("xzx","BOOT_COMPLETED");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            LogUtil.d("xzx","BOOT_COMPLETED");
            SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant",Context.MODE_PRIVATE);
            int hourOfDay=sharedPreferences.getInt("hourOfDay", 0);
            int minute=sharedPreferences.getInt("minute",0);
            alarm.setAlarm(context,hourOfDay,minute);
        }
    }
}
