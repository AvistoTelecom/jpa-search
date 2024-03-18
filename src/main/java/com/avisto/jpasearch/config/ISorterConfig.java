package com.avisto.jpasearch.config;

import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.model.SortDirection;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

/**
 * This class ISorterConfig is the interface of the Sorter with which you can sort you search.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public non-sealed interface ISorterConfig<R extends SearchableEntity> extends ISearchConfig<R> {

    Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection);

    String getSortPath();
}
