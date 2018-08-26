package com.dynamicmapper.filters;

import java.util.Collection;
import java.util.List;

/**
 * A common interface for filters used on ModelMapper
 *
 * @authored by: walter.dumba
 */

public interface Criteria<T>{

    List<T> meetCriteria(List<T> items);
}
