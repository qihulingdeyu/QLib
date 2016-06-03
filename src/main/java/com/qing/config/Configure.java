package com.qing.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.qing.utils.StringUtil;

import java.util.Map;
import java.util.Set;

/**
 * Created by zwq on 2015/08/31 14:38.<br/><br/>
 * 配置
 */
public class Configure {

    private static final String TAG = Configure.class.getName();
    private static Configure config;
    private static Context mContext;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static boolean isChange;

    public static Configure init(Context context) {
        if (config == null) {
            synchronized (Configure.class) {
                if (config == null) {
                    config = new Configure(context);
                }
            }
        }
        return config;
    }

    private Configure(Context context) {
        mContext = context;
        getSharedPreferences("");
    }

    public static void getSharedPreferences(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            name = "config";
        }
        preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        isChange = true;
    }

    public static Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public static String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public static Set<String> getStringSet(String key, Set<String> defValues) {
        return preferences.getStringSet(key, defValues);
    }

    public static int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public static boolean contains(String key) {
        return preferences.contains(key);
    }

    private static SharedPreferences.Editor getEditor() {
        if (isChange || editor == null) {
            editor = preferences.edit();
            isChange = false;
        }
        return editor;
    }

    public static SharedPreferences.Editor putString(String key, String value) {
        return getEditor().putString(key, value);
    }

    public static SharedPreferences.Editor putStringSet(String key, Set<String> values) {
        return getEditor().putStringSet(key, values);
    }

    public static SharedPreferences.Editor putInt(String key, int value) {
        return getEditor().putInt(key, value);
    }

    public static SharedPreferences.Editor putLong(String key, long value) {
        return getEditor().putLong(key, value);
    }

    public static SharedPreferences.Editor putFloat(String key, float value) {
        return getEditor().putFloat(key, value);
    }

    public static SharedPreferences.Editor putBoolean(String key, boolean value) {
        return getEditor().putBoolean(key, value);
    }

    public static SharedPreferences.Editor remove(String key) {
        return getEditor().remove(key);
    }

    public static SharedPreferences.Editor clear() {
        return getEditor().clear();
    }

    public static boolean commit() {
        return getEditor().commit();
    }

    public void apply() {
        getEditor().apply();
    }
}
