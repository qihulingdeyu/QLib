package com.qing.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by zwq on 2015/09/23 16:02.<br/><br/>
 */
public abstract class DaoSql<T> {

    private static final String TAG = DaoSql.class.getName();

    protected String tableName;

    public abstract Class<T> getTable();

    /**
     * 获取表名
     * @return
     */
    public final String getTableName() {
        if (tableName==null){
            tableName = getTable().getSimpleName();
        }
        return tableName;
    }

    /**
     * 设置表名
     * @param tableName
     */
    public void setTableName(String tableName) {
        if (tableName!=null && !tableName.trim().isEmpty()){
            this.tableName = tableName;
        }
    }

    /**
     * 创建表格的SQL语句
     * @return
     */
    public String createTableSql() {
        StringBuffer sb = new StringBuffer();
        sb.append("create table if not exists "+getTableName()+" (");
        Field[] fields = getTable().getFields();
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getModifiers() == Modifier.PUBLIC){
                sb.append(" "+field.getName()).append(" "+getFieldType(field.getType()));
                //sb.append(" primary key autoincrement")
                count++;
            }
        }
        if (count != 0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * 删除表格的SQL语句
     * @return
     */
    public String dropTableSql(){
        StringBuffer sb = new StringBuffer();
        sb.append("drop table if exists "+getTableName()+";");
        return sb.toString();
    }

    /**
     * 反射获取Field类型
     * @param type
     * @return
     */
    private String getFieldType(Class<?> type) {
        if (Integer.class.isAssignableFrom(type)){
            return "INTEGER";
        }else if(Long.class.isAssignableFrom(type)){
            return "LONG";
        }else if(Float.class.isAssignableFrom(type)){
            return "FLOAT";
        }else if(Double.class.isAssignableFrom(type)){
            return "DOUBLE";
        }else if(String.class.isAssignableFrom(type)){
            return "TEXT";
        }else if(Boolean.class.isAssignableFrom(type)){
            return "INTEGER";
        }else if(Date.class.isAssignableFrom(type)){
            return "TIMESTAMP";//TIME DATA DATETIME
        }else{
            return "VARCHAR(255)";
        }
    }

    /**
     * 将entity转换为ContentValues
     * @param entity
     * @return
     */
    public ContentValues entityToContentValues(T entity){
        if (entity == null) return null;

        ContentValues values = new ContentValues();
        Field[] fields = entity.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getModifiers() == Modifier.PUBLIC){
                setContentValues(entity, field, values);
            }
        }
        return values;
    }

    /**
     * 将field的值设置到ContentValues对应的字段中
     * @param entity
     * @param field
     * @param values
     */
    protected void setContentValues(T entity, Field field, ContentValues values) {
        Class<?> type = field.getType();
        try {
            if (Integer.class.isAssignableFrom(type)){
                values.put(field.getName(), field.getInt(entity));

            }else if(Long.class.isAssignableFrom(type)){
                values.put(field.getName(), field.getLong(entity));

            }else if(Float.class.isAssignableFrom(type)){
                values.put(field.getName(), field.getFloat(entity));

            }else if(Double.class.isAssignableFrom(type)) {
                values.put(field.getName(), field.getDouble(entity));

            }else if(String.class.isAssignableFrom(type)){
                values.put(field.getName(), field.get(entity).toString());

            }else if(Boolean.class.isAssignableFrom(type)) {
                values.put(field.getName(), field.getBoolean(entity)==true?1:0);

            }else if(Date.class.isAssignableFrom(type)){
                values.put(field.getName(), DateFormat.getDateInstance().parse(field.get(entity).toString()).getTime());

            }else{
                values.put(field.getName(), field.get(entity).toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将cursor内的值转换为对应的实体
     * @param cursor
     * @return
     */
    public T getValueFromCursor(Cursor cursor) {
        try {
            T t = getTable().newInstance();
            Field[] fields = t.getClass().getFields();
            int count = 0;
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getModifiers() == Modifier.PUBLIC){
                    setFieldValue(t, field, cursor);
                    count++;
                }
            }
            if (count != 0){
                return t;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将cursor内的值设置到对应的字段中
     * @param t
     * @param field
     * @param cursor
     */
    protected void setFieldValue(T t, Field field, Cursor cursor) {
        Class<?> type = field.getType();
        int index = cursor.getColumnIndex(field.getName());
        try {
            if (Integer.class.isAssignableFrom(type)){
                field.setInt(t, cursor.getInt(index));

            }else if(Long.class.isAssignableFrom(type)){
                field.setLong(t, cursor.getLong(index));

            }else if(Float.class.isAssignableFrom(type)){
                field.setFloat(t, cursor.getFloat(index));

            }else if(Double.class.isAssignableFrom(type)) {
                field.setDouble(t, cursor.getDouble(index));

            }else if(String.class.isAssignableFrom(type)){
                field.set(t, cursor.getString(index));

            }else if(Boolean.class.isAssignableFrom(type)){
                field.setBoolean(t, cursor.getInt(index)==1?true:false);

            }else if(Date.class.isAssignableFrom(type)){
                field.set(t, DateFormat.getDateInstance().parse(cursor.getString(index)));

            }else{
                field.set(t, cursor.getString(index));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
