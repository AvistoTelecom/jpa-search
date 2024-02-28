package com.avisto.genericspringsearch.model;

import com.avisto.genericspringsearch.config.FilterConfig;
import com.avisto.genericspringsearch.operation.ObjectFilterOperation;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.FilterSorterConfig;
import com.avisto.genericspringsearch.config.ISearchConfig;
import com.avisto.genericspringsearch.config.ISearchCriteriaConfig;
import com.avisto.genericspringsearch.operation.StringFilterOperation;

public enum CriteriaTestEnum implements ISearchCriteriaConfig<TestEntity> {
    ID(FilterConfig.of("id", ObjectFilterOperation.EQUAL, "id")),
    FIELD1(FilterSorterConfig.of("field1", ObjectFilterOperation.EQUAL, "field1")),
    FIELD2(FilterConfig.of("field2", StringFilterOperation.LIKE_IGNORE_CASE, "field2"));

    final ISearchConfig<TestEntity> searchConfig;

    CriteriaTestEnum(ISearchConfig<TestEntity> searchConfig) {
        this.searchConfig = searchConfig;
    }

    @Override
    public ISearchConfig<TestEntity> getSearchConfig() {
        return FilterSorterConfig.of("field1", ObjectFilterOperation.EQUAL, "field1");
    }

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria("field1", SortDirection.ASC);
    }

    @Override
    public Class<TestEntity> getRootClass() {
        return TestEntity.class;
    }
}
