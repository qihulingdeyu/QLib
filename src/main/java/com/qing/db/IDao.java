package com.qing.db;

import java.util.List;

/**
 * Created by zwq on 2015/09/22 15:17.<br/><br/>
 */
public interface IDao<T> {

    public long insert(T entity);

    public long insert(List<T> list);

    public int deleteById(long key);

    public int delete(T entity);

    public int delete(List<T> list);

    public int deleteAll();

    public long update(T entity);

    public long update(List<T> list);

    public T findById(long key);

    public T find(String... args);

    public List<T> findAll(String... args);

    public List<T> findAll();

    public long getCounts();
}
