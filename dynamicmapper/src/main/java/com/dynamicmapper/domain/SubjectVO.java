package com.dynamicmapper.domain;

public class SubjectVO {



    private String shortDescription;
    private String fullDescription;
    private int semester;
    private int credits;



    public SubjectVO(){}


    public SubjectVO(String shortDescription, String fullDescription, int semester, int credits) {
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.semester = semester;
        this.credits = credits;
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
