package com.name.myassistant.alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.name.myassistant.MainActivity;
import com.name.myassistant.util.LogUtil;


/**
 * 闹钟广播接收器
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.


    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d("xzx", "AlarmReceiver onReceive");
        String noteStr=intent.getStringExtra("info");
        Intent intent1=new Intent(context, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(!TextUtils.isEmpty(noteStr)){
            intent1.putExtra("info",noteStr);
        }
        context.startActivity(intent1);
    }

}
