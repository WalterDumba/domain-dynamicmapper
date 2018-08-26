package com.dynamicmapper.domain;

public class Address {

    private String description;
    private int number;
    private String zipCode;


    public Address(String description, int number, String zipCode) {
        this.description = description;
        this.number = number;
        this.zipCode = zipCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
