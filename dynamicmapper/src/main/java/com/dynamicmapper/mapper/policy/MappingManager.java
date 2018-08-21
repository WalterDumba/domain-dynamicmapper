package com.dynamicmapper.mapper.policy;

import com.dynamicmapper.commons.Mappable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MappingManager {

    private static Map<MappingPolicy, SystemLegacyMappingStrategy> mappers;

    static {
        mappers = new HashMap<>();
        mappers.put(MappingPolicy.FIELD_ANNOTATION,   new FieldAnnotatedMapping(Mappable.class) );
        mappers.put(MappingPolicy.PROPERTY_ACCESSOR,  new PropertyAccessorMapping()             );
    }

    public static SystemLegacyMappingStrategy discovery(Field field) {

        SystemLegacyMappingStrategy selectedMapper;
        if( field.isAnnotationPresent(Mappable.class) ){
            selectedMapper= mappers.get(MappingPolicy.FIELD_ANNOTATION);
        }
        else{
            selectedMapper = mappers.get(MappingPolicy.PROPERTY_ACCESSOR);
        }
        selectedMapper.setTarget(field);
        return selectedMapper;
    }
}
