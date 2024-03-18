package com.avisto.jpasearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * This IFilterOperation : ComparableFilterOperation can compare a field and the filter.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum ComparableFilterOperation implements IFilterOperation<Comparable> {

    /**
     * Compares a field with the filter and returns whether the filter is greater than or equal for the field.
     */
    GREATER_THAN_OR_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Comparable value) {
            return cb.greaterThanOrEqualTo((Expression<Comparable>) expression, value);
        }
    },

    /**
     * Compares a field with the filter and returns whether the filter is greater than or equal or null for the field.
     */
    GREATER_THAN_OR_EQUAL_OR_NULL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Comparable value) {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(GREATER_THAN_OR_EQUAL.calculate(cb, expression, value));
            predicates.add(cb.isNull(expression));
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    },

    /**
     * Compares a field with the filter and returns whether the filter is less than or equal for the field.
     */
    LESS_THAN_OR_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Comparable value) {
            return cb.lessThanOrEqualTo((Expression<Comparable>) expression, value);
        }
    };

    @Override
    public boolean needsMultipleValues() {
        return false;
    }

    @Override
    public Class<Comparable> getOperationType() {
        return Comparable.class;
    }
}
