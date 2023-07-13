package com.avisto.genericspringsearch;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

interface FilterOperationInterface {
    <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values);

    boolean needsMultipleValues();
}
