package com.avisto.jpasearch.config;

import com.avisto.jpasearch.SearchableEntity;

/**
 * This interface is the parent of FilterConfig and SorterConfig, and therefore describes the methods common to these two classes.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public sealed interface ISearchConfig<R extends SearchableEntity> permits IFilterConfig, ISorterConfig {

    String getKey();
    void checkConfig(Class<R> rootClazz);
}
