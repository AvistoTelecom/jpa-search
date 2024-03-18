package com.avisto.jpasearch.config;

import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.exception.CannotSortException;
import com.avisto.jpasearch.model.SortDirection;
import com.avisto.jpasearch.service.SearchUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

/**
 * SorterConfig allows you to sort the search response
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
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

    /**
     * Check that you not try to sort on a collection.
     * @param rootClazz The entity class
     */
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
