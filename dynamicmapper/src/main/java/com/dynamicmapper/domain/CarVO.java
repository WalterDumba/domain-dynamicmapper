package com.dynamicmapper.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class CarVO implements Serializable {

    protected Map<String, Object> properties = new HashMap<>();

    private String BRAND ="brand";
    private String MODEL ="model";

    protected CarVO(String brand, String model){
       this.setProperty(BRAND, brand);
       this.setProperty(MODEL, model);
    }

    public String getBrand() {
        return (String) properties.get(BRAND);
    }

    public void setBrand(String brand) {
        this.setProperty(BRAND, brand);
    }

    public String getModel() {
        return (String) this.properties.get(MODEL);
    }

    public void setModel(String model) {
        this.setProperty(MODEL, model);
    }

    protected Object getProperty(String key){
        return this.properties.get(key);
    }
    protected void setProperty(String key, Object value){
        this.properties.put(key,value);
    }
}
