package com.dynamicmapper.domain;

public class Teacher extends Person {

    private String[] classes;
    private String degree;


    public Teacher(String name, int age, String[] classes, String degree) {
        super(name, age);
        this.classes = classes;
        this.degree = degree;
    }

    public String[] getClasses() {
        return classes;
    }

    public void setClasses(String[] classes) {
        this.classes = classes;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }
}
