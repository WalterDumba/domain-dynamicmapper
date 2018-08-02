package com.dynamicmapper.domain;

public class Student extends Person {


    private String educationLevel;
    private Subject[] subjects;

    public Student(){}

    public Student(String educationLevel, Subject[] subjects) {
        this.educationLevel = educationLevel;
        this.subjects = subjects;
    }

    public Student(String name, int age, String educationLevel, Subject[] subjects) {
        super(name, age);
        this.educationLevel = educationLevel;
        this.subjects = subjects;
    }


    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public Subject[] getSubjects() {
        return subjects;
    }

    public void setSubjects(Subject[] subjects) {
        this.subjects = subjects;
    }
}
