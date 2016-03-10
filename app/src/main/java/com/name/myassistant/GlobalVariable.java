package com.name.myassistant;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.name.myassistant.m.Alarm;
import com.name.myassistant.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu on 16-3-9.
 */
public class GlobalVariable {
    private static GlobalVariable ourInstance = new GlobalVariable();
    private boolean READ_SHORT_MESSAGE_PERMISSION;
    private List<Alarm> alarmList=new ArrayList<>();

    public static GlobalVariable getInstance() {
        return ourInstance;
    }

    private GlobalVariable() {
    }

    public boolean isREAD_SHORT_MESSAGE_PERMISSION() {
        return READ_SHORT_MESSAGE_PERMISSION;
    }

    public void setREAD_SHORT_MESSAGE_PERMISSION(Context context,boolean READ_SHORT_MESSAGE_PERMISSION) {
        this.READ_SHORT_MESSAGE_PERMISSION = READ_SHORT_MESSAGE_PERMISSION;
        save(context);
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public void save(Context context){
        Gson gson=new Gson();
        String data=gson.toJson(ourInstance);
        LogUtil.d("xzx","data=> "+data);

        SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("GlobalVariable",data).apply();
    }

    public void recoverData(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant", Context.MODE_PRIVATE);
        String data=sharedPreferences.getString("GlobalVariable",null);
        if(data==null){
            return;
        }
        Gson gson=new Gson();
        ourInstance=gson.fromJson(data,GlobalVariable.class);
        LogUtil.d("xzx","ourInstance READ_SHORT_MESSAGE_PERMISSION=> "+ourInstance.isREAD_SHORT_MESSAGE_PERMISSION());
        LogUtil.d("xzx","ourInstance alarmList=> "+ourInstance.getAlarmList().toString());
    }
}
