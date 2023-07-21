package com.avisto.genericspringsearch;

import com.avisto.genericspringsearch.config.AbstractCriteria;
import com.avisto.genericspringsearch.model.SortDirection;

public class OrderCriteria extends AbstractCriteria {

    private final SortDirection sortDirection;

    public OrderCriteria(String key, SortDirection sortDirection) {
        this.key = key;
        this.sortDirection = sortDirection;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
