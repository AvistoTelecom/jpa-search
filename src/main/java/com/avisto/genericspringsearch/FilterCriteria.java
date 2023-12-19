package com.avisto.genericspringsearch;

import com.avisto.genericspringsearch.config.AbstractCriteria;

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
