package com.dynamicmapper.mapper.policy;

import com.dynamicmapper.commons.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PropertyAccessorMapping implements SystemLegacyMappingStrategy {

    private Object provider;
    private Field target;

    @Override public void setProvider(Object provider) {
        this.provider = provider;

    }
    @Override public void setTarget(Field obj) {
        this.target = obj;
    }
    @Override public Object resolve() {

        final String methodName = methodNameToLookup();
        List<Method> targetAccessibleMethods = ReflectionUtils.gettersOf(provider.getClass(), methodName);
        Method propertyAccessor = ReflectionUtils.lookupPropertyResolver(methodName, targetAccessibleMethods);
        return ReflectionUtils.invokeReflective(provider, propertyAccessor);
    }

    private String methodNameToLookup() {
        int initIndex=0;
        String fieldName = target.getName();
        char firstLetterInCapital   = Character.toUpperCase(fieldName.charAt(initIndex));
        String getterForCurrentField=firstLetterInCapital+fieldName.substring(initIndex+1);
        if(target.getType() == Boolean.class){
            getterForCurrentField="is"+getterForCurrentField;
        }
        else{
            getterForCurrentField="get"+getterForCurrentField;
        }
        return getterForCurrentField;
    }

}
