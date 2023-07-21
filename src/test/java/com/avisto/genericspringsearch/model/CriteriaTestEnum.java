package com.avisto.genericspringsearch.model;

import com.avisto.genericspringsearch.FilterOperation;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.config.FilterConfig;
import com.avisto.genericspringsearch.config.SearchConfigInterface;

public enum CriteriaTestEnum implements SearchConfigInterface {
    TEST;

    @Override
    public FilterConfig getFilterConfig() {
        return FilterConfig.of(FilterOperation.EQUAL, "field1", "field1");
    }

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria("field1", SortDirection.ASC);
    }
}
