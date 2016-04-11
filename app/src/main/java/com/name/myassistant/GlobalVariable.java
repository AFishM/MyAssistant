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
    private boolean READ_SHORT_MESSAGE_PERMISSION=true;
    private boolean allowToSay=true;
    private List<Alarm> alarmList=new ArrayList<>();
    private String link;
    private boolean USER_HAS_IMG;
    private int robotImgStatus;
    public static final int ORIGINAL_IMG=0;
    public static final int NEW_IMG=1;
    public static final int NO_IMG=2;



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

    public boolean isAllowToSay() {
        return allowToSay;
    }

    public void setAllowToSay(boolean allowToSay) {
        this.allowToSay = allowToSay;
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    public boolean isUSER_HAS_IMG() {
        return USER_HAS_IMG;
    }

    public void setUSER_HAS_IMG(boolean USER_HAS_IMG) {
        this.USER_HAS_IMG = USER_HAS_IMG;
    }

    public int getRobotImgStatus() {
        return robotImgStatus;
    }

    public void setRobotImgStatus(int robotImgStatus) {
        this.robotImgStatus = robotImgStatus;
    }

    public static void save(Context context){
        LogUtil.d("xzx");
        Gson gson=new Gson();
        String data=gson.toJson(ourInstance);
        LogUtil.d("xzx","data=> "+data);

        SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("GlobalVariable",data).apply();
    }

    public static void recoverData(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant", Context.MODE_PRIVATE);
        String data=sharedPreferences.getString("GlobalVariable",null);
        if(data==null){
            return;
        }
        Gson gson=new Gson();
        ourInstance=gson.fromJson(data,GlobalVariable.class);
        LogUtil.d("xzx","GlobalVariable=> "+ourInstance.toString());
    }

    @Override
    public String toString() {
        return "GlobalVariable{" +
                "READ_SHORT_MESSAGE_PERMISSION=" + READ_SHORT_MESSAGE_PERMISSION +
                ", allowToSay=" + allowToSay +
                ", alarmList=" + alarmList +
                ", link='" + link + '\'' +
                ", USER_HAS_IMG=" + USER_HAS_IMG +
                ", robotImgStatus=" + robotImgStatus +
                '}';
    }
}
