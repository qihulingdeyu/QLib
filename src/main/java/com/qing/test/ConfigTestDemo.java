package com.qing.test;

import android.content.Context;

import com.qing.config.ConfigBase;
import com.qing.config.ConfigInfo;

/**
 * Created by zwq on 2016/06/03 11:35.<br/><br/>
 *

 ConfigTestDemo.getInstance().initSharedPreferences(mContext).initAllConfig();
 int value = ConfigInfo.Test1.getInt();
 ConfigTestDemo.getInstance().saveConfig(ConfigInfo.Test1, value+1);
 Log.i(TAG, "value:"+value);

 */
public class ConfigTestDemo extends ConfigBase {

    private static final String TAG = ConfigTestDemo.class.getName();
    private final String configName = "config_test";

    private static ConfigTestDemo instance;

    public static ConfigTestDemo getInstance() {
        if (instance == null) {
            synchronized (ConfigTestDemo.class) {
                if (instance == null) {
                    instance = new ConfigTestDemo();
                }
            }
        }
        return instance;
    }

    private ConfigTestDemo() {
    }

    @Override
    public ConfigTestDemo initSharedPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(configName, Context.MODE_PRIVATE);
        return this;
    }

    @Override
    public IConfigInfo[] getConfigs() {
        return ConfigInfo.values();
    }
}
