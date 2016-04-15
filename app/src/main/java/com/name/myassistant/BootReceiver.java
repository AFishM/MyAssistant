package com.name.myassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.name.myassistant.m.Alarm;
import com.name.myassistant.util.LogUtil;

import java.util.List;

/**
 * Created by xu on 16-2-24.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d("xzx","BOOT_COMPLETED,intent.getAction()=> " +intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            LogUtil.d("xzx");
            GlobalVariable.recoverData(context);
            List<Alarm> alarmList=GlobalVariable.getInstance().getAlarmList();
            for(int i=0;i<alarmList.size();i++){
                Alarm alarm=alarmList.get(i);
                if(alarm.open){
                    alarm.setOpen(context,true);
                }
            }
            LogUtil.d("xzx");
        }
    }
}
