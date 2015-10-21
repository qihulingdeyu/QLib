package com.qing.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Created by zwq on 2015/09/22 15:17.<br/><br/>
 */
public abstract class AbstractDao<T> extends SqlHelper<T> implements IDao<T> {

    private static final String TAG = AbstractDao.class.getName();
    protected SQLiteOpenHelper helper;
    protected SQLiteDatabase db;
    protected Cursor cursor;

    public final String FIND_ALL = "select * from "+getTableName();
    public final String FIND_BY_ID = FIND_ALL +" where _id=?";
    public final String COUNT_ROWS = "select count(*) as row_count from "+getTableName();

    public AbstractDao(Context context) {

    }

    @Override
    public int deleteById(long key) {
        db = helper.getWritableDatabase();
        int r = db.delete(getTableName(), "_id=?", new String[]{""+key});
        closeAll(null, db, null);
        return r;
    }

    @Override
    public int delete(T entity) {
        return 0;
    }

    @Override
    public int delete(List<T> list) {
        return 0;
    }

    @Override
    public int deleteAll() {
        db = helper.getWritableDatabase();
        int r = db.delete(getTableName(), null, null);
        closeAll(null, db, null);
        return r;
    }

    @Override
    public int update(List<T> list) {
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
        db = helper.getReadableDatabase();
        cursor = db.rawQuery(COUNT_ROWS, null);
        int row_count = 0;
        if (cursor != null){
            cursor.moveToFirst();
            row_count = cursor.getInt(cursor.getColumnIndex("row_count"));
        }
        closeAll(null, db, cursor);
        return row_count;
    }

    public void closeAll(SQLiteOpenHelper helper, SQLiteDatabase db, Cursor cursor){
        if (cursor != null){
            cursor.close();
            cursor = null;
        }
        if (db != null){
            db.close();
            db = null;
        }
        if (helper != null){
            helper.close();
            helper = null;
        }
    }
}
