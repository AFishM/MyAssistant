package com.name.myassistant;

/**
 * Created by xu on 2016/3/8.
 */
public class GlobalVariable {
    private static GlobalVariable ourInstance = new GlobalVariable();

    public static GlobalVariable getInstance() {
        return ourInstance;
    }

    private GlobalVariable() {
    }
}
