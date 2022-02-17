package com.leo.okdownload.util;

import android.util.Log;

public class LogUtls {
    private static final String TAG = "leo";

    public static void debug(String log){
        Log.d(TAG,log);
    }

    public static void info(String log){
        Log.i(TAG,log);
    }

    public static void error(String log){
        Log.e(TAG,log);
    }
}
