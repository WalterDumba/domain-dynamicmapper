package com.dynamicmapper.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 * A Collection factory used by Mapper to abstracting when a specific subtypes or Interface on Mappings
 *
 *
 * @authored by: Walter Dumba
 */
public class CollectionFactory {




    enum FrameWorkSupportedTypesEnum {

        SET("java.util.Set",                    HashSet.class       ),
        COLLECTION("java.util.Collection",      ArrayList.class     ),
        ARRAYLIST("java.util.Arrays.ArrayList", ArrayList.class     ),
        LINKEDLIST("java.util.LinkedList",      LinkedList.class    ),
        HASHSET("java.util.HashSet",            HashSet.class       );

        private final String className;
        private final Class<?> type;


        FrameWorkSupportedTypesEnum(String clazzFullQualifiedName, Class<?>type ) {
            this.className  = clazzFullQualifiedName;
            this.type       = type;
        }

        public String getClassName() {
            return className;
        }

        public Class<?> getType() {
            return type;
        }

        public static FrameWorkSupportedTypesEnum forClazzFullQualifiedName(String clazzFqN){
            for(FrameWorkSupportedTypesEnum curr: values()){
                if(curr.getClassName().equals(clazzFqN)){
                    return curr;
                }
            }
            throw new RuntimeException(String.format("Unable to lookup implementation for:%s", clazzFqN));
        }
    }

    public static <D> D newInstanceOf(String clazzName){

        FrameWorkSupportedTypesEnum impl = FrameWorkSupportedTypesEnum.forClazzFullQualifiedName( clazzName );
        Constructor<D> c;
        try {
            c = (Constructor<D>) impl.getType().getConstructor();
            if( !c.isAccessible()){
                c.setAccessible(true);
            }
            return  c.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(String
                    .format("Cannot instantiate reflective object from class %s: No default constructor provided",
                            clazzName));
        }
    }



}
