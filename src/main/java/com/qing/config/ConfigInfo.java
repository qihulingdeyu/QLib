package com.qing.config;

/**
 * Created by zwq on 2016/06/03 14:54.<br/><br/>
 */
public enum ConfigInfo implements ConfigBase.IConfigInfo {

    Test1("test1", 1),
    Test2("test2", "test"),
    Test3("test3", true);

    public String key;
    public Object value;
    public Object defaultValue;
    public int valueType = -1;//值的类型

    ConfigInfo(String key, Object value) {
        this.key = key;
        this.value = value;
        this.defaultValue = value;
        this.valueType = checkValueType(value);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int getValueType() {
        return valueType;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public void setValueAndCheckType(Object value) {
        if (checkValueType(value) == this.valueType) {
            this.value = value;
        }
    }

    @Override
    public int checkValueType(Object value) {
        int type = -1;
        if (value != null) {
            if (value instanceof Integer) {
                type = 0;
            } else if (value instanceof String) {
                type = 1;
            } else if (value instanceof Boolean) {
                type = 2;
            }
        }
        return type;
    }

    @Override
    public int getInt() {
        return getInt(false);
    }

    @Override
    public int getInt(boolean isDefault) {
        if (valueType == 0) {
            return isDefault ? (Integer) defaultValue : (Integer) value;
        }
        return 0;
    }

    @Override
    public String getString() {
        return getString(false);
    }

    @Override
    public String getString(boolean isDefault) {
        if (valueType == 1) {
            return isDefault ? defaultValue.toString() : value.toString();
        }
        return "";
    }

    @Override
    public boolean getBoolean() {
        return getBoolean(false);
    }

    @Override
    public boolean getBoolean(boolean isDefault) {
        if (valueType == 2) {
            return isDefault ? (Boolean) defaultValue : (Boolean) value;
        }
        return false;
    }
}
