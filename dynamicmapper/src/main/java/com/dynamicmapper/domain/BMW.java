package com.dynamicmapper.domain;

public class BMW extends Car {


    private String SERIE="serie";



    public BMW(String brand, String model) {
        super(brand, model);
    }

    public BMW(String brand, String model, String serie) {
        super(brand, model);
        this.setProperty(SERIE, serie);
    }
    public void setSerie(String serie){
        setProperty(SERIE, serie);
    }

    public String getSerie(){
        return (String) getProperty(SERIE);
    }


}
