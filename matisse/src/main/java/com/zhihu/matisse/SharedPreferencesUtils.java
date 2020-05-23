package com.zhihu.matisse;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences("Config", Context.MODE_PRIVATE);
    }

    public static void putInt(Context context, String key, int value) {
        getSp(context).edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key) {
        return getInt(context, key, 0);
    }

    public static int getInt(Context context, String key, int defVal) {
        return getSp(context).getInt(key,defVal);
    }

    public static void putLong(Context context, String key, long value) {
        getSp(context).edit().putLong(key, value).commit();
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, 0);
    }

    public static long getLong(Context context, String key, long defVal) {
        return getSp(context).getLong(key,defVal);
    }
}
