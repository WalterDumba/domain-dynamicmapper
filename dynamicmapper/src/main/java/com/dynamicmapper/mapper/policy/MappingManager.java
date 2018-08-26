package com.dynamicmapper.mapper.policy;

import com.dynamicmapper.commons.Mappable;
import com.dynamicmapper.commons.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MappingManager {

    private static Map<MappingPolicy, Class<? extends SystemLegacyMappingStrategy> > mappers;

    static {
        mappers = new HashMap<>();
        mappers.put(MappingPolicy.FIELD_ANNOTATION,   FieldAnnotatedMapping.class   );
        mappers.put(MappingPolicy.PROPERTY_ACCESSOR,  PropertyAccessorMapping.class );
    }

    public static SystemLegacyMappingStrategy discovery(Field field) {

        SystemLegacyMappingStrategy selectedMapper;
        Class<? extends SystemLegacyMappingStrategy > mapperClazz;

        if( field.isAnnotationPresent(Mappable.class) ){
            mapperClazz = mappers.get(MappingPolicy.FIELD_ANNOTATION);
        }
        else{
            mapperClazz = mappers.get(MappingPolicy.PROPERTY_ACCESSOR);
        }
        selectedMapper = ReflectionUtils.newInstanceOf(mapperClazz);
        selectedMapper.setTarget( field );
        return selectedMapper;
    }
}
