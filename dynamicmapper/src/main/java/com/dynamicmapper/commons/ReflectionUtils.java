package com.dynamicmapper.commons;

import com.dynamicmapper.filters.Criteria;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;


/**
 * A little API for Reflection used in ModelMapper
 *
 * Authored by: Walter Dumba
 *
 */
public final class ReflectionUtils {




    //LRU Caches
    private LRUCache< Class<?>, List<Field>  > cachedFieldsLRUCache;
    private LRUCache< Class<?>, List<Method> > cachedMethodsLRUCache;

    //Lazily initialized
    private static ReflectionUtils instance;

    private static final int CACHE_DEFAULT_CAPACITY = 100;

    private static final Comparator<Method> COMPARE_METHOD_BY_NAME = new Comparator<Method>() {
        @Override
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };


    private ReflectionUtils(){
        this.cachedFieldsLRUCache   = new LRUCache<>(CACHE_DEFAULT_CAPACITY);
        this.cachedMethodsLRUCache  = new LRUCache<>(CACHE_DEFAULT_CAPACITY);
    }



    /**
     * Get this class instance
     *
     * using "Double-Checked Locking" idiom
     *
     * @return the singleton of this class
     */
    public static ReflectionUtils getInstance(){

        if(instance == null){
            synchronized ( ReflectionUtils.class ){
                if(instance == null){
                    instance = new ReflectionUtils();
                }
            }
        }
        return instance;
    }

    /**
     * Create a new instance of class clazz reflective
     *
     * @param clazz
     * @param <D>
     * @return
     *
     * FIXME: when clazz is an Enum things doesn't goes well
     */
    public static <D> D newInstanceOf(Class<D> clazz){
        Constructor<D> c;
        try {
            c = clazz.getConstructor();
            if( !c.isAccessible()){
                c.setAccessible(true);
            }
            return  c.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(String
                    .format("Cannot instantiate reflective object from class %s: No default constructor provided",
                            clazz));
        }
    }

    /**
     *
     * @param clazz
     * @param annotationToScan
     * @param <T>
     * @return
     */
    public static <T>List<Field> getDeclaredFieldsAnnotatedWithTraversingClazzHierarchy(Class<?> clazz, Class<? extends Annotation>annotationToScan){
        return getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByCriteria(clazz, new FieldAnnotationCriteria(annotationToScan));
    }

    /**
     *
     * @param clazz
     *
     * @see #getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByCriteria(Class, Criteria[])
     *
     * @return
     */
    public static List<Field> getClazzFieldsAlongTheHierarchy(Class<?>clazz){
        return getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByCriteria(clazz, null);
    }


    /**
     * Walk into give class hierarchy and retrieve its declared methods as it go until reach
     * object class
     *
     * This method uses cache to be fast on calls
     *
     * @param clazz
     * @return - All Methods from this class and its ancestors
     */
    public static List<Method> getClazzDeclaredMethodsAlongTheHierarchy(Class<?> clazz) {

        List<Method> collectedMethods = getInstance().cachedMethodsLRUCache.get( clazz );
        if(collectedMethods == null){
            collectedMethods = new ArrayList<>();
            Class<?>clazzNode = clazz;
            Class<?> root     = Object.class;
            do{
                collectedMethods.addAll( Arrays.asList( clazzNode.getDeclaredMethods() ) );
                clazzNode = clazzNode.getSuperclass();
            }while( clazzNode!=null && clazzNode!= root);

            getInstance().cachedMethodsLRUCache.put( clazz, collectedMethods );
        }
        return collectedMethods;
    }

    /**
     * Check if object is a typical immutable class form JDK
     *
     * @see #classIsAWellKnownImmutableClassFromJDK(Class)
     *
     * @param obj
     * @return
     */
    public static boolean objectClassIsAwellKnownImmutableClassFromJDK(Object obj) {
        return obj!= null && classIsAWellKnownImmutableClassFromJDK( obj.getClass() );
    }


    /**
     * Check if given field type is classic Immutable from JDK e.g: String, Number and Boolean
     *
     * @See #classIsAWellKnownImmutableClassFromJDK(Class)
     *
     * @param field
     * @return
     */
    public static boolean fieldTypeIsAwellKnownImmutableClazzFromJDK(Field field) {
        return classIsAWellKnownImmutableClassFromJDK( field.getType() );
    }

    /**
     * Lookup a method from a given targetClazz
     *
     * @param propertyAccessor
     * @param targetClazz
     * @return
     *
     */
    public static Method lookupPropertyResolver(String propertyAccessor, Class<?> targetClazz){

        List<Method> methodList;
        if( !getInstance().cachedMethodsLRUCache.containsKey(targetClazz) ){
            methodList = ReflectionUtils.getClazzDeclaredMethodsAlongTheHierarchy( targetClazz );
            Collections.sort(methodList, COMPARE_METHOD_BY_NAME);
            getInstance().cachedMethodsLRUCache.put(targetClazz, methodList);
        }
        List<Method>targetClazzMethods = getInstance().cachedMethodsLRUCache.get(targetClazz);
        Method found = binarySearch(propertyAccessor, targetClazzMethods, 0, targetClazzMethods.size()-1);
        return found;
    }

    /**
     *
     * @param propertyAccessor
     * @param methodList
     * @return
     */
    public static Method lookupPropertyResolver(String propertyAccessor, List<Method> methodList){
        return binarySearch(propertyAccessor,methodList, 0, methodList.size()-1);
    }




    /**
     *
     * @param underlyingObject
     * @param target
     * @return
     */
    public static Object invokeReflective(Object underlyingObject, Method target){
        try {
            return target.invoke(underlyingObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException( String.format("Error trying invoke method %s reflective", target ));
        }
    }

    /**
     * Get all getters from the given class
     *
     * @param clazz
     * @param regex
     * @return
     */
    public static List<Method> gettersOf(Class<?> clazz, final String regex){

        Criteria<Method>filterByGettersWhoseName = new MethodDecoratorCriteria( new MethodGetterCriteria() ){
            @Override
            public List<Method> meetCriteria(List<Method> items) {
                MethodNamePatternCriteria decoratee = new MethodNamePatternCriteria(Pattern.compile(regex));
                List<Method> preFiltered = super.meetCriteria(items);
                return decoratee.meetCriteria( preFiltered );
            }
        };
        return filterByGettersWhoseName.meetCriteria( getClazzDeclaredMethodsAlongTheHierarchy(clazz) );
    }

    /**======================================== PRIVATE PARTS ======================================================= **/

    /**
     *
     * @param clazz
     * @param criterias
     * @return
     */
    private static List<Field> getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByCriteria(Class<?>clazz, Criteria<?>...criterias){

        if(clazz == null){
            return Collections.emptyList();
        }
        List<Field> collectedFields = getInstance().cachedFieldsLRUCache.get( clazz );
        boolean notAlreadyCached = collectedFields == null;
        Class depth = Object.class;
        if( notAlreadyCached ){
            collectedFields = new ArrayList<>();
            Field[] clazzFields;
            do{
                clazzFields = clazz.getDeclaredFields();
                collectedFields.addAll(Arrays.asList(clazzFields));
                clazz = clazz.getSuperclass();
            }while( clazz!=null && clazz!= depth);

            getInstance().cachedFieldsLRUCache.put( clazz, collectedFields);
        }
        //Check if caller want us to filter by annotations
        if(criterias!=null && criterias.length > 0){
            for(Criteria curr: criterias)
                collectedFields = curr.meetCriteria(collectedFields);
        }
        return collectedFields;
    }

    /**
     * Check if given clazz type is classic Immutable from JDK e.g: String, Number and Boolean
     *
     * @param clazz such clazz we are testing
     * @return true if a class is a PrimitiveType Wrapper, String, a Number or Boolean
     */
    private static boolean classIsAWellKnownImmutableClassFromJDK(Class clazz){
        return clazz.isPrimitive()
                || String.class.equals(clazz)
                || Number.class.equals(clazz.getSuperclass())
                || Boolean.class.equals(clazz);
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

        if(endIdx< startIdx || methodList == null){
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

    /**===================================== CRITERIA FILTERS ==================================================== **/



    static abstract class MethodDecoratorCriteria implements Criteria<Method>{

        private Criteria<Method> delegate;

        public MethodDecoratorCriteria(Criteria<Method> delegate) {
            this.delegate = delegate;
        }
        @Override
        public List<Method> meetCriteria(List<Method> items) {
            return this.delegate.meetCriteria(items);
        }
    }

    static abstract class MethodCompoundCriteria implements Criteria<Method>{

        private Criteria<Method>[] criteriaChildren;

        public MethodCompoundCriteria(Criteria<Method>... criteriaChildren) {
            this.criteriaChildren = criteriaChildren;
        }

        @Override
        public List<Method> meetCriteria(List<Method> items) {
            List<Method> filteredResult = items;
            for(Criteria<Method>currentCriteria: criteriaChildren){
                filteredResult = currentCriteria.meetCriteria(filteredResult);
            }
            return filteredResult;
        }
    }

    /** Concret Filters **/

    static class MethodNamePatternCriteria implements Criteria<Method> {

        private Pattern methodPattern;

        public MethodNamePatternCriteria(Pattern methodPattern) {
            this.methodPattern = methodPattern;
        }

        @Override
        public List<Method> meetCriteria(List<Method> items) {

            if(methodPattern==null){
                throw new RuntimeException("methodPattern not initialized");
            }
            List<Method> filtered = new ArrayList<>();
            for(Method curr: items){
                if( methodPattern.matcher(curr.getName()).find()){
                    filtered.add(curr);
                }
            }
            return filtered;
        }
    }

    static class MethodGetterCriteria extends MethodCompoundCriteria{

        private static final String METHOD_REGEX ="get[A-Za-z0-9]+|is[A-Za-z0-9]+";

        public MethodGetterCriteria() {
            super(new MethodNamePatternCriteria(Pattern.compile(METHOD_REGEX)),
                    new MethodModifiersCriteria(Modifier.PUBLIC),
                    new Criteria<Method>() {
                        @Override public List<Method> meetCriteria(List<Method> items) {
                            List<Method> methodsWithNoArgs= new ArrayList<>();
                            for(Method curr: items){
                                if(curr.getParameterTypes().length==0){
                                    methodsWithNoArgs.add(curr);
                                }
                            }
                            return methodsWithNoArgs;
                        }
                    }
            );
        }
    }
    static class MethodSetterCriteria extends  MethodCompoundCriteria{

        private static final String METHOD_REGEX ="set[A-Za-z0-9]+";

        public MethodSetterCriteria() {
            super(new MethodNamePatternCriteria(Pattern.compile(METHOD_REGEX)),
                    new MethodModifiersCriteria(Modifier.PUBLIC));
        }
    }

    static class MethodModifiersCriteria implements Criteria<Method>{

        int methodModifiers;
        public MethodModifiersCriteria(int methodModifiers) {
            this.methodModifiers = methodModifiers;
        }
        @Override
        public List<Method> meetCriteria(List<Method> items) {
            List<Method> filtered= new ArrayList<>();
            for(Method curr: items){
                if(curr.getModifiers() == methodModifiers){
                    filtered.add(curr);
                }
            }
            return filtered;
        }
    }

    static class FieldAnnotationCriteria implements Criteria<Field>{

        private List<Class<? extends Annotation>> annotations;

        public FieldAnnotationCriteria(Class<?extends Annotation>... annotations) {
            this.annotations = Arrays.asList(annotations);
        }
        @Override
        public List<Field> meetCriteria(List<Field> items) {

            List<Field> filtered = new ArrayList<>();
            for(Field currField: items){
                for(Annotation fieldAnnotation: currField.getDeclaredAnnotations()){
                    if(annotations.contains( fieldAnnotation.annotationType() ) ){
                        filtered.add( currField );
                        break;
                    }
                }
            }
            return filtered;
        }
    }
}