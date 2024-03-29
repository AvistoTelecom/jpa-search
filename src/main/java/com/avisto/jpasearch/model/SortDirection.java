package com.avisto.jpasearch.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import java.util.function.BiFunction;

/**
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum SortDirection {
    ASC(CriteriaBuilder::asc),
    DESC(CriteriaBuilder::desc);

    private final BiFunction<CriteriaBuilder, Expression<?>, Order> orderFunction;

    SortDirection(BiFunction<CriteriaBuilder, Expression<?>, Order> orderFunction) {
        this.orderFunction = orderFunction;
    }

    public Order getOrder(CriteriaBuilder cb, Expression<?> expression) {
        return orderFunction.apply(cb, expression);
    }
    public static SortDirection of(String sort) {
        if (sort == null) {
            return null;
        }
        return SortDirection.valueOf(sort.toUpperCase());
    }
}
