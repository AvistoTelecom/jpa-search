package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.model.SortDirection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

/**
 * This class ISorterConfig is the interface of the Sorter with which you can sort you search.
 *
 * @param <R>
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public non-sealed interface ISorterConfig<R extends SearchableEntity> extends ISearchConfig<R> {

    /**
     * Return the order of the sorter, Ascending or Descending.
     *
     * @param root
     * @param criteriaBuilder Criteria Builder
     * @param sortDirection
     * @return Order of the sorter, ASC or DESC
     */
    Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection);

    /**
     * Return the path to the field that you want to sort.
     * @return String path to the field that you want to sort.
     */
    String getSortPath();
}
