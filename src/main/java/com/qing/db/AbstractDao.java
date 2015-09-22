package com.qing.db;

import java.util.List;

/**
 * Created by zwq on 2015/09/22 15:17.<br/><br/>
 */
public abstract class AbstractDao<T> implements IDao<T> {

    private static final String TAG = AbstractDao.class.getName();

    public String createTable(String tableName){
        StringBuffer sb = new StringBuffer();
//        sb.append()
        return "";
    }

    public String dropTable(String tableName){
        return "";
    }

    @Override
    public long delete(T entity) {
        return 0;
    }

    @Override
    public long delete(List<T> list) {
        return 0;
    }

    @Override
    public long update(List<T> list) {
        return 0;
    }

    @Override
    public T find(String... args) {
        return null;
    }

    @Override
    public List<T> findAll(String... args) {
        return null;
    }

    @Override
    public long getCounts() {
        return 0;
    }
}
