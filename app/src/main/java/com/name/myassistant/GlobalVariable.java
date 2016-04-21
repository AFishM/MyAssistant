package com.name.myassistant;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.name.myassistant.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xu on 16-3-9.
 */
public class GlobalVariable {
    private static GlobalVariable ourInstance = null;

    private boolean READ_SHORT_MESSAGE_PERMISSION=true;
    private boolean ALLOW_TO_SAY =true;
    private boolean USER_HAS_IMG;

    //<语音助手回答答案所在坐标，该答案对应问题的搜索链接>
    private Map<Integer,String> linkMap=new HashMap<>();

    private GlobalVariable() {
    }

    public static GlobalVariable getInstance() {
        if(ourInstance==null){
            synchronized (GlobalVariable.class){
                if(ourInstance==null){
                    ourInstance=new GlobalVariable();
                }
            }
        }
        return ourInstance;
    }

    public boolean isREAD_SHORT_MESSAGE_PERMISSION() {
        return READ_SHORT_MESSAGE_PERMISSION;
    }

    public void setREAD_SHORT_MESSAGE_PERMISSION(Context context,boolean READ_SHORT_MESSAGE_PERMISSION) {
        this.READ_SHORT_MESSAGE_PERMISSION = READ_SHORT_MESSAGE_PERMISSION;
        save(context);
    }

    public boolean isALLOW_TO_SAY() {
        return ALLOW_TO_SAY;
    }

    public void setALLOW_TO_SAY(boolean ALLOW_TO_SAY) {
        this.ALLOW_TO_SAY = ALLOW_TO_SAY;
    }

    public Map<Integer, String> getLinkMap() {
        return linkMap;
    }

    public boolean isUSER_HAS_IMG() {
        return USER_HAS_IMG;
    }

    public void setUSER_HAS_IMG(boolean USER_HAS_IMG) {
        this.USER_HAS_IMG = USER_HAS_IMG;
    }

    /**
     * 将全局变量转化为字符串并保存到永久存储SharedPreferences中
     * @param context：上下文
     */
    public static void save(Context context){
        Gson gson=new Gson();
        String data=gson.toJson(ourInstance);
        LogUtil.d("xzx","data=> "+data);

        SharedPreferences sharedPreferences=context.getSharedPreferences("myassistant", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("GlobalVariable",data).apply();
    }

    /**
     * 从SharedPreferences中读取全局变量字符串并转化为对象
     * @param context：上下文
     */
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
                ", ALLOW_TO_SAY=" + ALLOW_TO_SAY +
                ", USER_HAS_IMG=" + USER_HAS_IMG +
                ", linkMap=" + linkMap +
                '}';
    }
}
