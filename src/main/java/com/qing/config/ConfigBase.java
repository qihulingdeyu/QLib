package com.qing.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zwq on 2016/06/03 11:52.<br/><br/>
 */
public abstract class ConfigBase {

    protected SharedPreferences mSharedPreferences = null;
    private SharedPreferences.Editor mEditor = null;

    public interface IConfigInfo {
        String getKey();

        Object getValue();

        Object getDefaultValue();

        void setValue(Object value);

        void setValueAndCheckType(Object value);

        int checkValueType(Object value);

        int getValueType();

        int getInt();

        int getInt(boolean isDefault);

        String getString();

        String getString(boolean isDefault);

        boolean getBoolean();

        boolean getBoolean(boolean isDefault);
    }

    public abstract ConfigBase initSharedPreferences(Context context);

    public abstract IConfigInfo[] getConfigs();

    public void initAllConfig() {
        if (mSharedPreferences == null) {
            return;
        }
        for (int i = 0; getConfigs() != null && i < getConfigs().length; i++) {
            IConfigInfo config = getConfigs()[i];
            if (config.getValueType() == 0) {
                config.setValue(mSharedPreferences.getInt(config.getKey(), config.getInt(true)));

            } else if (config.getValueType() == 1) {
                config.setValue(mSharedPreferences.getString(config.getKey(), config.getString(true)));

            } else if (config.getValueType() == 2) {
                config.setValue(mSharedPreferences.getBoolean(config.getKey(), config.getBoolean(true)));
            }
        }
    }

    private void saveConfig(IConfigInfo config, boolean apply) {
        if (mSharedPreferences == null) {
            return;
        }
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        if (config.getValueType() == 0) {
            mEditor.putInt(config.getKey(), config.getInt());

        } else if (config.getValueType() == 1) {
            mEditor.putString(config.getKey(), config.getString());

        } else if (config.getValueType() == 2) {
            mEditor.putBoolean(config.getKey(), config.getBoolean());
        }
        if (apply) {
//            mEditor.commit();
            mEditor.apply();
        }
    }

    public void saveConfig(IConfigInfo config) {
        saveConfig(config, true);
    }

    public void saveConfig(IConfigInfo config, Object value) {
        if (config != null) {
            config.setValueAndCheckType(value);
            saveConfig(config, true);
        }
    }

    public void saveAllConfig() {
        int len = getConfigs() == null ? 0 : getConfigs().length;
        IConfigInfo config = null;
        for (int i = 0; i < len; i++) {
            config = getConfigs()[i];
            saveConfig(config, i == len - 1 ? true : false);
        }
    }

    public void resetConfig() {
        int len = getConfigs() == null ? 0 : getConfigs().length;
        IConfigInfo config = null;
        for (int i = 0; i < len; i++) {
            config = getConfigs()[i];
            config.setValue(config.getDefaultValue());
            saveConfig(config, i == len - 1 ? true : false);
        }
    }
}
