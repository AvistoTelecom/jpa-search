package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

public class SorterConfig<R extends SearchableEntity> implements ISorterConfig<R> {

    private final String key;
    private final String path;

    private SorterConfig(String key, String path) {
        this.key = key;
        this.path = path;
    }

    public static <R extends SearchableEntity> SorterConfig<R> of(String key, String path) {
        return new SorterConfig<>(key, path);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean needMultipleValues() {
        // TODO PUT IN FILTER CONFIG INSTEAD OF SEARCH CONFIG
        return false;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        if (getSortPath().contains("[")) {
            throw new CannotSortException("Cannot sort on a Collection");
        }
    }

    @Override
    public Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection) {
        return sortDirection.getOrder(criteriaBuilder, SearchUtils.getPath(root, path));
    }

    @Override
    public String getSortPath() {
        return path;
    }
}
