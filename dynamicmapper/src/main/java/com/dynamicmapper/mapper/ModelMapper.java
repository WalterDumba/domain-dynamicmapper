package com.dynamicmapper.mapper;

import com.dynamicmapper.commons.CollectionFactory;
import com.dynamicmapper.commons.LRUCache;
import com.dynamicmapper.commons.ReflectionUtils;
import com.dynamicmapper.exceptions.DeepCopyTypesMissMatchException;
import com.dynamicmapper.mapper.policy.*;

import java.lang.reflect.*;
import java.util.*;

import static com.dynamicmapper.commons.ReflectionUtils.*;


/**
 *
 * A little API for mapping domain objects that's will save us a lot work as well as avoid
 *
 * such predictable boiler plate code you know...
 *
 *
 *
 *
 * Release future: Allowing as well doing mappings
 * using only getters and setters following java POJO standards
 *
 * @authored by : walter.dumba
 *
 */
public class ModelMapper{



    @Deprecated
    private static LRUCache<Class<?>, List<Method>> cachedMethodAccessorsLRUCache;
    private static final int CACHE_CAPACITY = 100;

    static {
        cachedMethodAccessorsLRUCache = new LRUCache<>(CACHE_CAPACITY);
    }


    /**
     * Map an object to another object from different class
     *
     * @see #map(Object, Class, Map)
     *
     */
    public static <S, D> D map(S sourceObj, Class<D> dstClazz) {

        D dstObject = map( sourceObj, dstClazz, new HashMap<Integer, Object>() );
        return dstObject;
    }


    /**
     * Map an object to another object from different class
     *
     * @param sourceObj source object which we want to map to
     * @param dstClazz  class of destiny object
     * @param alreadyMappedObjects a variable that holds the tracking during the mapping to prevent infinite loop in mappings
     * @param <S>
     * @param <D>
     * @return a mapped object from Class<D>
     */
    private static <S, D> D map(S sourceObj, Class<D> dstClazz, Map<Integer, Object> alreadyMappedObjects){

        if(sourceObj == null || dstClazz == null){
            return null;
        }
        //INTROSPECT DESTINY CLASS FIELDS TO FIGURE OUT WHICH MAPPING STRATEGY WILL BE USED
        Collection<Field> dstAnnotatedFields = ReflectionUtils.getClazzFieldsAlongTheHierarchy( dstClazz );
        D dstObject = newInstanceOf( dstClazz );
        for(Field dstField: dstAnnotatedFields){

            SystemLegacyMappingStrategy mapping = MappingManager.discovery( dstField );
            mapping.setProvider( sourceObj );
            Object value = mapping.resolve();
            Object clone;
            if( alreadyMappedObjects.get( Objects.hashCode(value) )!= null ){
                continue;
            }
            if( objectIsEligibleToBeClonedAndAssignedToField(dstField, value) ){
                clone = recursiveReflectiveDeepCopy( value, dstField.getType());
            }
            else{
                alreadyMappedObjects.put(Objects.hashCode(value), value);
                clone = map(value, dstField.getType(), alreadyMappedObjects);
            }
            setField( dstField, dstObject, clone );
        }
        return dstObject;
    }


    /**
     * Map tje srcList to a List of a given Class Type
     *
     *
     * @param srcList
     * @param dstListType
     * @param <S>
     * @param <D>
     * @return
     */
    public static <S, D> List<D> mapList(List<S> srcList, Class<D>dstListType){

        if(srcList == null|| dstListType == null){
            return null;
        }
        List<D> mappedList = new ArrayList<>(srcList.size());
        for(S elem: srcList){
            D currMappedObj = map(elem, dstListType);
            mappedList.add( currMappedObj );
        }
        return mappedList;
    }



    /**
     * Deep copy the following object
     * @param obj
     * @param <R>
     * @return
     */
    public static <R> R deepCopyOf(Object obj){
        return (R) recursiveReflectiveDeepCopy(obj, obj.getClass());
    }


    /**############################################## PRIVATE PART  ################################################**/


    /**
     *
     * Set the field value on underlyingObject
     *
     * @param field
     * @param underlyingObject
     * @param value
     * @param <D>
     * TODO: catch IllegalArgumentException and rethrow IllegalPropertyValueException with a more descriptive message
     */
    private static <D> void setField(Field field, D underlyingObject, Object value) {
        try {
            field.setAccessible( true );
            field.set(underlyingObject, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Error trying set field value reflective on object %s with value %s", underlyingObject, value));
        }
    }

    /**
     * Get the field value of underlyingObject
     *
     * @param field
     * @param underlyingObject
     * @return
     */
    private static Object getFieldValue(Field field, Object underlyingObject){
        Object value;
        try {
            value = field.get(underlyingObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Error trying get field value reflective on object %s",underlyingObject));
        }
        return value;
    }



    /**
     *  Deep copy the object given in parameters
     *  if at some point come across with a property of immutable class from JDK the reference only will be copied
     *  this copy doesn't recompute objects fields during the process, a cache is used to lookup the fields
     *  so the performance might increase meaningfully
     *
     *
     * @param srcObj
     * @param dstClazz
     * @param <S>
     * @param <D>
     * @return
     * @throws DeepCopyTypesMissMatchException
     */
    private static <S,D> D recursiveReflectiveDeepCopy(S srcObj, Class<D> dstClazz ) throws DeepCopyTypesMissMatchException {


            if(srcObj == null){
                return null;
            }
            if( objectClassIsAwellKnownImmutableClassFromJDK( srcObj ) ){
                return (D) srcObj;
            }
            Object clone;
            Class objClazz = dstClazz;

            //ARRAY TYPES
            if( srcObj.getClass().isArray() ){
                clone = Array.newInstance( srcObj.getClass().getComponentType(), Array.getLength(srcObj) );
                for(int i=0; i< Array.getLength(srcObj); ++i){
                    Object arrElement = Array.get(srcObj, i);
                    Object arrElementClone = recursiveReflectiveDeepCopy( arrElement, srcObj.getClass().getComponentType() );
                    Array.set(clone, i, arrElementClone);
                }
            }
            //COLLECTION TYPES
            else if(Collection.class.isAssignableFrom( srcObj.getClass() )){
                Collection<?> collection = (Collection<?>) srcObj;
                Collection temp = (Collection<?>) CollectionFactory.newInstanceOf( srcObj.getClass().getCanonicalName() );
                for(Object curr: collection){
                    Object currCloned = recursiveReflectiveDeepCopy( curr, curr.getClass() );
                    temp.add(currCloned);
                }
                clone = temp;
                objClazz = null;
            }

            else {
                clone = newInstanceOf( dstClazz );
            }
            //Case: We want assure we are copying objects from the same class so...
            if(srcObj.getClass() == dstClazz){
                for (Field field : ReflectionUtils.getClazzFieldsAlongTheHierarchy( objClazz )) {
                    field.setAccessible( true );
                    if( getFieldValue(field, srcObj) == null || Modifier.isFinal(field.getModifiers()) ){
                        continue;
                    }
                    //WE DON'T WANT DEEP COPYING IMMUTABLE OBJECTS
                    if( fieldTypeIsAwellKnownImmutableClazzFromJDK( field ) ){
                        setField(field, clone,  getFieldValue(field, srcObj));
                    }else{
                        Object childObj = getFieldValue(field, srcObj);
                        if(childObj == srcObj){
                            setField(field, clone, clone);
                        }else{
                            setField(field, clone, recursiveReflectiveDeepCopy(childObj, field.getType()));
                        }
                    }
                }
            }
            return (D) clone;
    }

    /**
     * Test if value is eligible to be assigned to the following field
     * @param dstField
     * @param value
     * @return
     */
    private static boolean objectIsEligibleToBeClonedAndAssignedToField(Field dstField, Object value) {
        return value!= null && value.getClass()== dstField.getType() || dstField.getType().isPrimitive();
    }

}
