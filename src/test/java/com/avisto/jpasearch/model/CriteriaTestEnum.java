package com.avisto.jpasearch.model;

import com.avisto.jpasearch.config.FilterConfig;
import com.avisto.jpasearch.operation.ObjectFilterOperation;
import com.avisto.jpasearch.OrderCriteria;
import com.avisto.jpasearch.config.FilterSorterConfig;
import com.avisto.jpasearch.config.ISearchConfig;
import com.avisto.jpasearch.config.ISearchCriteriaConfig;
import com.avisto.jpasearch.operation.StringFilterOperation;

public enum CriteriaTestEnum implements ISearchCriteriaConfig<TestEntity> {
    ID(FilterConfig.of("id", ObjectFilterOperation.EQUAL, "id")),
    FIELD1(FilterSorterConfig.of("field1", ObjectFilterOperation.EQUAL, "field1")),
    FIELD2(FilterConfig.of("field2", StringFilterOperation.CONTAIN_IGNORE_CASE, "field2"));

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
