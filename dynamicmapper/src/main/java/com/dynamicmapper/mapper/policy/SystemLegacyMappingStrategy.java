package com.dynamicmapper.mapper.policy;

import java.lang.reflect.Field;

public interface SystemLegacyMappingStrategy {

    void setProvider(Object provider);
    void setTarget(Field target);
    Object resolve();
}
