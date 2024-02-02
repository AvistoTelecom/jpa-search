package com.avisto.genericspringsearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public interface IFilterOperation<T> {
    Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T value);

    boolean needsMultipleValues();

    Class<T> getOperationType();
}
