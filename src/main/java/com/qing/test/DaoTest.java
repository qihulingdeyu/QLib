package com.qing.test;

import android.content.Context;

import com.qing.db.AbstractDao;

import java.util.List;

/**
 * Created by zwq on 2015/09/22 15:26.<br/><br/>
 */
public class DaoTest extends AbstractDao<Student> {

    private static final String TAG = DaoTest.class.getName();

    public DaoTest(Context context) {
        super(context);
    }

    @Override
    public Class<Student> getTable() {
        return Student.class;
    }


    @Override
    public long insert(Student entity) {
        entityToContentValues(entity);
//        entity.
        return 0;
    }

    @Override
    public long insert(List<Student> list) {
        return 0;
    }

    @Override
    public int deleteById(long key) {
        return 0;
    }

    @Override
    public int deleteAll() {

        return 0;
    }

    @Override
    public int update(Student entity) {
        return 0;
    }

    @Override
    public Student findById(long key) {
        return null;
    }

    @Override
    public List<Student> findAll() {
        return null;
    }
}
