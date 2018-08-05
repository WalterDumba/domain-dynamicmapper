package com.dynamicmapper.mapper;

import com.dynamicmapper.commons.CollectionFactory;
import com.dynamicmapper.commons.LRUCache;
import com.dynamicmapper.commons.Mappable;
import com.dynamicmapper.commons.ReflectionUtils;
import com.dynamicmapper.exceptions.DeepCopyTypesMissMatchException;

import java.lang.reflect.*;
import java.util.*;

import static com.dynamicmapper.commons.ReflectionUtils.fieldTypeIsAwellKnownImmutableClazzFromJDK;
import static com.dynamicmapper.commons.ReflectionUtils.newInstanceOf;
import static com.dynamicmapper.commons.ReflectionUtils.objectClassIsAwellKnownImmutableClassFromJDK;


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




    private static LRUCache<Class<?>, List<Method>> cachedMethodAccessorsLRUCache;
    private static int CACHE_CAPACITY = 100;


    private static final Comparator<Method> COMPARE_METHOD_BY_NAME = new Comparator<Method>() {
        @Override
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    static {
        cachedMethodAccessorsLRUCache = new LRUCache<>(CACHE_CAPACITY);
    }



    /**
     * Perform a deep copy from source object to a dstClazz object
     * @param sourceObj
     * @param dstClazz
     * @param <S>
     * @param <D>
     * @return
     */
    public static <S, D> D map(S sourceObj, Class<D> dstClazz) {

        if(sourceObj == null || dstClazz == null){
            return null;
        }
        //INTROSPECT DESTINY CLASS TO FIGURE OUT WHICH PROPERTY WILL BE MAPPED
        Collection<Field> dstAnnotatedFields = ReflectionUtils.getDeclaredFieldsAnnotatedWithTraversingClazzHierarchy(dstClazz, Mappable.class, Object.class );
        List<Method> propertyAccessors       = cachedMethodAccessorsLRUCache.get( sourceObj.getClass() );

        //DOESN'T EXIST ON CACHE
        if( propertyAccessors == null ){
            Collection<String> propertyAccessorNameList   = ReflectionUtils.collectPropertyAccessorNamesFromMetaData( dstAnnotatedFields );
            propertyAccessors = ReflectionUtils.getMethodsByNameCriteria( sourceObj.getClass(), propertyAccessorNameList );

            //SORT PROPERTIES TO BOOST THE PERFORMANCE ON LATER LOOKUP
            Collections.sort( propertyAccessors, COMPARE_METHOD_BY_NAME );
            cachedMethodAccessorsLRUCache.put( sourceObj.getClass(), propertyAccessors );
        }

        D dstObject = newInstanceOf( dstClazz );
        for(Field dstField: dstAnnotatedFields){

            Method srcMethodGetter = lookupPropertyResolver( dstField.getAnnotation( Mappable.class ).methodName(), sourceObj.getClass());
            Object value = invokeReflective( sourceObj, srcMethodGetter );
            Object clone;

            if(value!= null && dstField.getType().isPrimitive() || value!= null && value.getClass()== dstField.getType()){
                clone = recursiveReflectiveDeepCopy( value, dstField.getType());
            }
            else{
                //WE HAVE A SUB MAPPING
                clone = map(value, dstField.getType());
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
     *
     * @param underlyingObject
     * @param target
     * @return
     */
    private static Object invokeReflective(Object underlyingObject, Method target){
        try {
            return target.invoke(underlyingObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException( String.format("Error trying invoke method %s reflective", target ));
        }
    }

    /**
     * Lookup a method from a given targetClazz
     *
     * @param propertyAccessor
     * @param targetClazz
     * @return
     */
    private static Method lookupPropertyResolver(String propertyAccessor, Class<?> targetClazz) {

        List<Method> methodList = cachedMethodAccessorsLRUCache.get(targetClazz);
        Method found = binarySearch(propertyAccessor, methodList, 0, methodList.size()-1);
        return found;
    }

    /**
     * Perform a binary search on given methodList
     * @param methodName - Method which will searched for
     * @param methodList - The target List holding methods which will be "binary searched"
     * @param startIdx   - start index
     * @param endIdx     - endIdx
     *
     * @return - A method if found on <param>methodList</param> or else null if a method
     *  given by name doesn't exist on methodList
     *
     *  @NOTE:
     */
    private static Method binarySearch(String methodName, List<Method> methodList, int startIdx, int endIdx) {

        if(endIdx< startIdx){
            return null;
        }
        int midIndex = (endIdx+startIdx)>> 1;
        if(methodName.compareTo( methodList.get(midIndex).getName() ) == 0){
            return methodList.get( midIndex );
        }
        else if(methodName.compareTo( methodList.get(midIndex).getName() ) < 0){
            return binarySearch( methodName, methodList, startIdx, midIndex-1 );
        }
        else if(methodName.compareTo( methodList.get(midIndex).getName()) > 0){
            return binarySearch(methodName, methodList, midIndex+1, endIdx);
        }
        return null;
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
    public static <S,D> D recursiveReflectiveDeepCopy(S srcObj, Class<D> dstClazz ) throws DeepCopyTypesMissMatchException {


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

}
