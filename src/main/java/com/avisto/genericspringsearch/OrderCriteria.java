package com.avisto.genericspringsearch;

import com.avisto.genericspringsearch.config.AbstractCriteria;
import com.avisto.genericspringsearch.model.SortDirection;

/**
 * This class is used to sort the criteria query in a certain order.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
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
