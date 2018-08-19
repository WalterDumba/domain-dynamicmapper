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
 */
public final class ReflectionUtils {




    //LRU Caches
    private LRUCache< Class<?>, Collection<Field>  > cachedFieldsLRUCache;
    private LRUCache< Class<?>, Collection<Method> > cachedMethodsLRUCache;

    //Lazily initialized
    private static ReflectionUtils instance;



    private static final int CACHE_DEFAULT_CAPACITY = 100;


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
    public static <T>Collection<Field> getDeclaredFieldsAnnotatedWithTraversingClazzHierarchy(Class<?> clazz, Class<? extends Annotation>annotationToScan){
        return getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByAnnotationTypes(clazz, annotationToScan);
    }

    /**
     *
     * @param clazz
     *
     * @see #getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByAnnotationTypes(Class, Class[])
     *
     * @return
     */
    public static Collection<Field> getClazzFieldsAlongTheHierarchy(Class<?>clazz){
        return getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByAnnotationTypes(clazz, null);
    }

    /**
     *
     * @param clazz
     * @param propertyInitializersNameList
     * @return
     */
    public static List<Method> getMethodsByNameCriteria(Class<? extends Object> clazz, Collection<String> propertyInitializersNameList) {

        Collection<Method> clazzGettersForAnnotatedFields = getClazzDeclaredMethodsAlongTheHierarchy( clazz );
        PropertyAccessorByNameCriteria propertyAccessorByNameCriteria = new PropertyAccessorByNameCriteria(
                new MethodGetterCriteria(),
                propertyInitializersNameList
        );
        clazzGettersForAnnotatedFields = propertyAccessorByNameCriteria.meetCriteria(clazzGettersForAnnotatedFields);
        if(clazzGettersForAnnotatedFields.size()!= propertyInitializersNameList.size()){
            throw new RuntimeException("Not all methods were found during methods collection phase");
        }
        return (List<Method>) clazzGettersForAnnotatedFields;
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




    /**======================================== PRIVATE PARTS ======================================================= **/

    /**
     *
     * @param clazz
     * @param annotationsToScan
     * @return
     */
    private static Collection<Field> getDeclaredFieldsAlongTheHierarchyOptionallyFilteringByAnnotationTypes(Class<?>clazz, Class<?extends Annotation>...annotationsToScan){

        if(clazz == null){
            return Collections.emptyList();
        }
        Collection<Field> collectedFields = getInstance().cachedFieldsLRUCache.get( clazz );
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
        if(annotationsToScan!=null && annotationsToScan.length > 0){
            return new FieldAnnotationCriteria(annotationsToScan).meetCriteria(collectedFields);
        }
        else{
            return collectedFields;
        }
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
    private static Collection<Method> getClazzDeclaredMethodsAlongTheHierarchy(Class<?> clazz) {

        Collection<Method> collectedMethods = getInstance().cachedMethodsLRUCache.get( clazz );
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


    /**===================================== CRITERIA FILTERS ==================================================== **/



    static abstract class MethodDecoratorCriteria implements Criteria<Method>{

        private Criteria<Method> delegate;

        public MethodDecoratorCriteria(Criteria<Method> delegate) {
            this.delegate = delegate;
        }
        @Override
        public Collection<Method> meetCriteria(Collection<Method> items) {
            return this.delegate.meetCriteria(items);
        }
    }

    static abstract class MethodCompoundCriteria implements Criteria<Method>{

        private Criteria<Method>[] criteriaChildren;

        public MethodCompoundCriteria(Criteria<Method>... criteriaChildren) {
            this.criteriaChildren = criteriaChildren;
        }

        @Override
        public Collection<Method> meetCriteria(Collection<Method> items) {
            Collection<Method> filteredResult = items;
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
        public Collection<Method> meetCriteria(Collection<Method> items) {

            if(methodPattern==null){
                throw new RuntimeException("methodPattern not initialized");
            }
            Collection<Method> filtered = new ArrayList<>();
            for(Method curr: items){
                if( methodPattern.matcher(curr.getName()).find()){
                    filtered.add(curr);
                }
            }
            return filtered;
        }
    }
    static class MethodGetterCriteria extends MethodCompoundCriteria{

        public MethodGetterCriteria() {
            super(  new MethodNamePatternCriteria(Pattern.compile("get[A-Za-z0-9]+|is[A-Za-z0-9]+")),
                    new MethodModifiersCriteria(Modifier.PUBLIC));
        }
    }
    static class MethodSetterCriteria extends  MethodCompoundCriteria{

        public MethodSetterCriteria() {
            super(new MethodNamePatternCriteria(Pattern.compile("set[A-Za-z0-9]+")),
                    new MethodModifiersCriteria(Modifier.PUBLIC));
        }
    }
    static class MethodModifiersCriteria implements Criteria<Method>{

        int methodModifiers;
        public MethodModifiersCriteria(int methodModifiers) {
            this.methodModifiers = methodModifiers;
        }
        @Override
        public Collection<Method> meetCriteria(Collection<Method> items) {
            Collection<Method> filtered= new ArrayList<>();
            for(Method curr: items){
                if(curr.getModifiers() == methodModifiers){
                    filtered.add(curr);
                }
            }
            return filtered;
        }
    }
    static class PropertyAccessorByNameCriteria extends MethodDecoratorCriteria{

        private String methodsNamesOrExpression;

        public PropertyAccessorByNameCriteria(Criteria<Method> delegate, Collection<String> accessorNameList) {
            super(delegate);

            StringBuilder methodsNameRegex = new StringBuilder();
            for( String currMethodName: accessorNameList ){
                methodsNameRegex.append(currMethodName).append("|");
            }
            methodsNameRegex.deleteCharAt( methodsNameRegex.length()-1 );
            this.methodsNamesOrExpression =  methodsNameRegex.toString();
        }
        @Override
        public Collection<Method> meetCriteria(Collection<Method> items) {
            Collection<Method>preFiltered = super.meetCriteria(items);
            MethodNamePatternCriteria methodNamesCriteria = new MethodNamePatternCriteria(
                    Pattern.compile(this.methodsNamesOrExpression)
            );
            return methodNamesCriteria.meetCriteria(preFiltered);
        }

    }
    static class FieldAnnotationCriteria implements Criteria<Field>{

        private List<Class<? extends Annotation>> annotations;

        public FieldAnnotationCriteria(Class<?extends Annotation>... annotations) {
            this.annotations = Arrays.asList(annotations);
        }
        @Override
        public Collection<Field> meetCriteria(Collection<Field> items) {

            Collection<Field> filtered = new ArrayList<>();
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