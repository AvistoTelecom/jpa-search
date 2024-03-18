package com.avisto.jpasearch.config;

import com.avisto.jpasearch.OrderCriteria;
import com.avisto.jpasearch.SearchableEntity;

/**
 * This interface is used to create CriteriaEnum where you will reference all your filters.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public interface ISearchCriteriaConfig<R extends SearchableEntity> {

    ISearchConfig<R> getSearchConfig();
    OrderCriteria getDefaultOrderCriteria();
    Class<R> getRootClass();

    default IFilterConfig<R, ?> getFilterConfig() {
        return (IFilterConfig<R, ?>) getSearchConfig();
    }

    default String getKey() {
        return getSearchConfig().getKey();
    }

    default boolean needMultipleValues() {
        return getFilterConfig().needMultipleValues();
    }
}
