package com.dynamicmapper.filters;

import java.util.Collection;

/**
 * A common interface for filters used on ModelMapper
 *
 * @authored by: walter.dumba
 */

public interface Criteria<T>{

    Collection<T> meetCriteria(Collection<T> items);
}
