package com.avisto.jpasearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * This enum provide EQUAL operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum ObjectFilterOperation implements IFilterOperation<Object> {

    /**
     * Verify that two objects are equal.
     */
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
