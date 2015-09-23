package com.qing.test;

import com.qing.db.AbstractDao;

import java.util.List;

/**
 * Created by zwq on 2015/09/22 15:26.<br/><br/>
 */
public class DaoTest extends AbstractDao<Student> {

    private static final String TAG = DaoTest.class.getName();

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
    public long deleteById(long key) {
        return 0;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public long update(Student entity) {
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
