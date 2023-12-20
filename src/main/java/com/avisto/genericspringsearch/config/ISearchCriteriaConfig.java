package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchableEntity;

public interface ISearchCriteriaConfig<R extends SearchableEntity> {

    ISearchConfig<R> getSearchConfig();

    OrderCriteria getDefaultOrderCriteria();

    Class<R> getRootClass();

    default String getKey() {
        return getSearchConfig().getKey();
    }

    default boolean needMultipleValues() {
        return getSearchConfig().needMultipleValues();
    }
}
