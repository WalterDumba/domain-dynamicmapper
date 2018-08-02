package com.dynamicmapper.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Course {


    private String title;

    private List<Student> students;


    private LinkedList<Student> nightlyStudents;

    private Set<Student> studentWorkers;



    public Course() {
    }


    public Course(String title, List<Student> students) {
        this.title = title;
        this.students = students;
    }


    public Course(String title, List<Student> students, LinkedList<Student> nightlyStudents) {
        this.title = title;
        this.students = students;
        this.nightlyStudents = nightlyStudents;
    }


    public Course(String title, List<Student> students, LinkedList<Student> nightlyStudents, Set<Student> studentWorkers) {
        this.title              = title;
        this.students           = students;
        this.nightlyStudents    = nightlyStudents;
        this.studentWorkers     = studentWorkers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }


    public LinkedList<Student> getNightlyStudents() {
        return nightlyStudents;
    }

    public void setNightlyStudents(LinkedList<Student> nightlyStudents) {
        this.nightlyStudents = nightlyStudents;
    }
}
