package com.avisto.genericspringsearch;

import java.util.Arrays;

import com.avisto.genericspringsearch.config.AbstractCriteria;
import com.avisto.genericspringsearch.service.CastService;

public class FilterCriteria<X> extends AbstractCriteria {

    private final X[] values;

    public FilterCriteria(String key, String[] values, Class<X> clazz) {
        this.key = key;
        this.values = values != null ? (X[]) Arrays.stream(values).map(str -> CastService.cast(str, clazz)).toArray() : null;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public X[] getValues() {
        return values;
    }
}
