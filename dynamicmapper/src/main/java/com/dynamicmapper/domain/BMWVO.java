package com.dynamicmapper.domain;

public class BMWVO extends CarVO {


    private String SERIE="serie";


    public BMWVO(){
        super(null,null);
    }

    public BMWVO(String brand, String model) {
        super(brand, model);
    }

    public BMWVO(String brand, String model, String serie) {
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
