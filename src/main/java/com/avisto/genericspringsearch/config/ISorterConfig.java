package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.model.SortDirection;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public non-sealed interface ISorterConfig<R extends SearchableEntity> extends ISearchConfig<R> {
    Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection);

    String getSortPath();
}
