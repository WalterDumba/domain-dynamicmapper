package com.dynamicmapper.domain;

import com.dynamicmapper.commons.Mappable;

public class StudentVO extends PersonVO{




    @Mappable(methodName = "getSubjects")
    private Subject [] subjects;
    @Mappable(methodName = "getEducationLevel")
    private String scolarship;


    public Subject[] getSubjects() {
        return subjects;
    }

    public void setSubjects(Subject[] subjects) {
        this.subjects = subjects;
    }

    public String getScolarship() {
        return scolarship;
    }

    public void setScolarship(String scolarship) {
        this.scolarship = scolarship;
    }
}
