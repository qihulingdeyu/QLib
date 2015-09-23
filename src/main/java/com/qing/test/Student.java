package com.qing.test;

/**
 * Created by zwq on 2015/09/22 15:25.<br/><br/>
 */
public class Student {

    private static final String TAG = Student.class.getName();
    private String name;
    private int age;

    public Student(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
