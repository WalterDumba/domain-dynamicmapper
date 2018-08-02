package com.dynamicmapper.commons;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Annoation to be used on destination classes interested on Using ModelMapper
 *
 * authored by: walter.dumba
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mappable {

    String methodName();
}
