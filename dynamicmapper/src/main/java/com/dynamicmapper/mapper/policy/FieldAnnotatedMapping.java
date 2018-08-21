package com.dynamicmapper.mapper.policy;

import com.dynamicmapper.commons.Mappable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.dynamicmapper.commons.ReflectionUtils.invokeReflective;
import static com.dynamicmapper.commons.ReflectionUtils.lookupPropertyResolver;

public class FieldAnnotatedMapping implements SystemLegacyMappingStrategy {

    private Object provider;
    private Field target; //Might be Field Or Method it doesn't matter
    Class<? extends Mappable> annotationClazz;

    public FieldAnnotatedMapping(Class<?extends Mappable> annotationClazz ) {
        this.annotationClazz = annotationClazz;
    }
    @Override public void setProvider(Object provider) {
        this.provider = provider;
    }
    @Override public void setTarget(Object obj) {

        if(obj == null || !Field.class.isAssignableFrom(obj.getClass()) ){
            throw new RuntimeException( String.format("Parameter is null or is not instance of Field") );
        }
        this.target = (Field) obj;
    }
    @Override public Object resolve() {
        String methodNameToLookup = target.getAnnotation(annotationClazz).methodName();
        Method methodToBeInvokedOnProvider = lookupPropertyResolver(methodNameToLookup, provider.getClass() );
        return invokeReflective(provider, methodToBeInvokedOnProvider);
    }
}
