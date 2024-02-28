package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public enum ComparableFilterOperation implements IFilterOperation<Comparable> {

    GREATER_THAN_OR_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Comparable value) {
            return cb.greaterThanOrEqualTo((Expression<Comparable>) expression, value);
        }
    },
    GREATER_THAN_OR_EQUAL_OR_NULL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Comparable value) {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(GREATER_THAN_OR_EQUAL.calculate(cb, expression, value));
            predicates.add(cb.isNull(expression));
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    },
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
