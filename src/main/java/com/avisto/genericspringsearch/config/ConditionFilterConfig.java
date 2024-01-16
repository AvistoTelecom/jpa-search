package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.operation.IFilterOperation;

import java.util.List;

public class ConditionFilterConfig<R extends SearchableEntity> implements IFilterConfig<R, Boolean> {
    private final IFilterOperation<T> filterOperation;
    private final String key;
    private final List<String> paths;

    protected ConditionFilterConfig(String key, IFilterOperation<T> filterOperation, List<String> paths) {
        this.filterOperation = filterOperation;
        this.key = key;
        this.paths = paths;
    }
}
