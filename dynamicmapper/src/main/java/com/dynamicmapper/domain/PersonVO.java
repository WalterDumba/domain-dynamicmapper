package com.dynamicmapper.domain;

import com.dynamicmapper.commons.Mappable;

import java.util.List;

public class PersonVO{


    @Mappable(methodName = "getName")
    private String firstName;
    @Mappable(methodName = "getAge")
    private int age;

    @Mappable(methodName = "getParent")
    private PersonVO parent;

    private Gender gender;
    private Address address;
    private List<Car> carList;


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

    public PersonVO getParent() {
        return parent;
    }

    public void setParent(PersonVO parent) {
        this.parent = parent;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Car> getCarList() {
        return carList;
    }

    public void setCarList(List<Car> carList) {
        this.carList = carList;
    }
}
