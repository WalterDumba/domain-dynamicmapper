package com.dynamicmapper.domain;

import com.dynamicmapper.commons.Mappable;

public class PersonVO{


    @Mappable(methodName = "getName")
    private String firstName;
    @Mappable(methodName = "getAge")
    private int age;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
