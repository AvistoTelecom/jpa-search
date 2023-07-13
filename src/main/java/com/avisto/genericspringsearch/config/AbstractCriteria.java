package com.avisto.genericspringsearch.config;

import java.util.Objects;

public abstract class AbstractCriteria {
    protected String key;

    public String getKey() {
        return key;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCriteria)) return false;
        AbstractCriteria that = (AbstractCriteria) o;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }
}
