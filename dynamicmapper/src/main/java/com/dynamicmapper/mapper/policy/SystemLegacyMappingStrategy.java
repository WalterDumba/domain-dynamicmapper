package com.dynamicmapper.mapper.policy;

public interface SystemLegacyMappingStrategy {

    void setProvider(Object provider);
    void setTarget(Object obj);
    Object resolve();
}
