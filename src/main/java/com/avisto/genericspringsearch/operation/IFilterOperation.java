package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public interface IFilterOperation<T> {
    Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T value);

    boolean needsMultipleValues();

    Class<T> getOperationType();
}
