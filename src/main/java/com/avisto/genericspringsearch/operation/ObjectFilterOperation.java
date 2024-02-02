package com.avisto.genericspringsearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public enum ObjectFilterOperation implements IFilterOperation<Object> {

    EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Object value) {
            if (value == null) {
                return cb.isNull(expression);
            }
            return cb.equal(expression, value);
        }
    };


    @Override
    public boolean needsMultipleValues() {
        return false;
    }

    @Override
    public Class<Object> getOperationType() {
        return Object.class;
    }
}
