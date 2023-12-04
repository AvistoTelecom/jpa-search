package com.avisto.genericspringsearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

interface FilterOperationInterface {
    <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values);

    boolean needsMultipleValues();
}