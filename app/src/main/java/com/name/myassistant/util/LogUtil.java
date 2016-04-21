package com.name.myassistant.util;

import android.util.Log;

import com.name.myassistant.AppConfig;

/**
 * Created by xu on 16-2-19.
 */
public class LogUtil {
    public static final String BY_S_AT_S_S = "(by: %s at: %s) %s";
    public static final String AT_S_S = "(at: %s) %s";


    private static String sTag = "myassistant";


    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR = 4;
    public static final int LEVEL_FATAL = 5;
    private static int sLevel = AppConfig.DEBUG_LEVEL;

    /**
     * set log level, the level lower than this level will not be logged
     *
     * @param level
     */
    public static void setLogLevel(int level) {
        sLevel = level;
    }

    private static boolean sIsLogEnable = AppConfig.DEBUG;


    public static void v(String msg) {
        if (sLevel > LEVEL_VERBOSE) {
            return;
        }
        if (sIsLogEnable) {
            Log.v(sTag, String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }

    /**
     * 打印消息，不加标识
     * @param msg：日志消息
     */
    public static void d(String msg) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        if (sIsLogEnable) {
            Log.d(sTag, String.format(AT_S_S, getStackTraceMsg(),msg));
        }
    }

    public static void d2(String msg) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        if (sIsLogEnable) {
            Log.d(sTag, msg);
        }
    }

    public static void i(String msg) {
        if (sLevel > LEVEL_INFO) {
            return;
        }
        if (sIsLogEnable) {
            Log.i(sTag, String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }

    public static void w(String msg) {
        if (sLevel > LEVEL_WARNING) {
            return;
        }
        if (sIsLogEnable) {
            Log.w(sTag, String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }

    public static void e(String msg) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        if (sIsLogEnable) {
            Log.e(sTag, String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }

    public static void e(Throwable e) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }

        String msg = "";
        if (null != e) {
            msg = e.getMessage();
        }

        if (msg==null){
            msg="";
        }

        if (sIsLogEnable) {
            Log.d(sTag, String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }


    public static void f(String msg) {
        if (sLevel > LEVEL_FATAL) {
            return;
        }
        if (sIsLogEnable) {
            Log.wtf(sTag,String.format(AT_S_S, msg, getStackTraceMsg()));
        }
    }


    public static void v(String tag, String msg) {
        if (sLevel > LEVEL_VERBOSE) {
            return;
        }
        if (sIsLogEnable) {
            Log.v(sTag, String.format(BY_S_AT_S_S, msg,tag, getStackTraceMsg()));
        }
    }

    /**
     * 打印加标识的日志
     * @param tag：标识
     * @param msg：日志消息
     */
    public static void d(String tag, String msg) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        if (sIsLogEnable) {
            Log.d(sTag, String.format(BY_S_AT_S_S,tag, getStackTraceMsg(),msg));
        }
    }

    public static void i(String tag, String msg) {

        if (sLevel > LEVEL_INFO) {
            return;
        }
        if (sIsLogEnable) {
            Log.i(sTag, String.format(BY_S_AT_S_S, msg,tag, getStackTraceMsg()));
        }
    }

    public static void w(String tag, String msg) {
        if (sLevel > LEVEL_WARNING) {
            return;
        }
        if (sIsLogEnable) {
            Log.w(sTag, String.format(BY_S_AT_S_S, msg,tag, getStackTraceMsg()));
        }
    }

    public static void e(String tag, String msg) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        if (sIsLogEnable) {
            Log.e(sTag, String.format(BY_S_AT_S_S, msg,tag, getStackTraceMsg()));
        }
    }


    public static void f(String tag, String msg) {
        if (sLevel > LEVEL_FATAL) {
            return;
        }
        if (sIsLogEnable) {
            Log.wtf(sTag, String.format(BY_S_AT_S_S, msg, tag, getStackTraceMsg()));
        }
    }

    /**
     * 定位代码位置（类名(line:行数)->方法名() ），只能在该类内部使用
     */
    private static String getStackTraceMsg() {
        String fileInfo = "";
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 4) {
            StackTraceElement stackTrace = stackTraceElements[4];
            fileInfo = String.format(" %s(line:%s)->%s() ",stackTrace.getFileName(),stackTrace.getLineNumber(),stackTrace.getMethodName());
        }
        return fileInfo;
    }

}
