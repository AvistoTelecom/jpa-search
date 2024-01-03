package com.avisto.genericspringsearch.model;

import com.avisto.genericspringsearch.operation.ObjectFilterOperation;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.FilterSorterConfig;
import com.avisto.genericspringsearch.config.ISearchConfig;
import com.avisto.genericspringsearch.config.ISearchCriteriaConfig;

public enum CriteriaTestEnum implements ISearchCriteriaConfig<SearchableEntity> {
    TEST;

    @Override
    public ISearchConfig<SearchableEntity> getSearchConfig() {
        return FilterSorterConfig.of("field1", ObjectFilterOperation.EQUAL, "field1");
    }

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria("field1", SortDirection.ASC);
    }

    @Override
    public Class<SearchableEntity> getRootClass() {
        return SearchableEntity.class;
    }
}
