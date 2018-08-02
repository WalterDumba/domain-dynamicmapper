package com.dynamicmapper.domain;

import com.dynamicmapper.commons.Mappable;

public class Subject {


    @Mappable(methodName = "getShortDescription")
    private String shortDescription;

    @Mappable(methodName = "getFullDescription")
    private String fullDescription;
    @Mappable(methodName = "getSemester")
    private int semester;
    @Mappable(methodName = "getCredits")
    private int credits;

    public Subject() {
    }

    public Subject(String shortDescription, String fullDescription, int semester) {
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.semester = semester;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}