package com.avisto.genericspringsearch.model;

import com.avisto.genericspringsearch.FilterOperation;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.FilterConfig;
import com.avisto.genericspringsearch.config.ISearchConfig;
import com.avisto.genericspringsearch.config.ISearchCriteriaConfig;

public enum CriteriaTestEnum implements ISearchCriteriaConfig<SearchableEntity> {
    TEST;

    @Override
    public ISearchConfig getSearchConfig() {
        return FilterConfig.of(FilterOperation.EQUAL, "field1", "field1");
    }

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria("field1", SortDirection.ASC);
    }
}
