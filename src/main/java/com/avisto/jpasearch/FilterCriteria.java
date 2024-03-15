package com.avisto.jpasearch;

import com.avisto.jpasearch.config.AbstractCriteria;

/**
 This class contains all information relating to the criteria used to perform a filter.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FilterCriteria extends AbstractCriteria {

    private final String[] values;

    public FilterCriteria(String key, String[] values) {
        this.key = key;
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String[] getValues() {
        return values;
    }
}
