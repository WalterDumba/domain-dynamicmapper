package com.dynamicmapper.mapper.policy;

import com.dynamicmapper.commons.Mappable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.dynamicmapper.commons.ReflectionUtils.invokeReflective;
import static com.dynamicmapper.commons.ReflectionUtils.lookupPropertyResolver;

public class FieldAnnotatedMapping implements SystemLegacyMappingStrategy {

    private Object provider;
    private Field target; //Might be Field Or Method it doesn't matter
    private Class<? extends Mappable> annotationClazz;


    public FieldAnnotatedMapping() {
        this(Mappable.class);
    }

    public FieldAnnotatedMapping(Class<?extends Mappable> annotationClazz ) {
        this.annotationClazz = annotationClazz;
    }
    @Override public void setProvider(Object provider) {
        this.provider = provider;
    }
    @Override public void setTarget(Field obj) {
        this.target = obj;
    }
    @Override public Object resolve() {
        String methodNameToLookup = target.getAnnotation(annotationClazz).methodName();
        Method methodToBeInvokedOnProvider = lookupPropertyResolver(methodNameToLookup, provider.getClass() );
        return invokeReflective(provider, methodToBeInvokedOnProvider);
    }
}
