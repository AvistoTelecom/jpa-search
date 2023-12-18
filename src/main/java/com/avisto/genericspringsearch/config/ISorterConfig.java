package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.model.SortDirection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

public interface ISorterConfig<R extends SearchableEntity> extends ISearchConfig<R> {
    Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection);
}
