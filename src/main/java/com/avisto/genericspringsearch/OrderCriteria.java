package com.avisto.genericspringsearch;

import org.springframework.data.domain.Sort;

import com.avisto.genericspringsearch.config.AbstractCriteria;

public class OrderCriteria extends AbstractCriteria {

    private final Sort.Direction sortDirection;

    public OrderCriteria(String key, Sort.Direction sortDirection) {
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

    public Sort.Direction getSortDirection() {
        return sortDirection;
    }
}
