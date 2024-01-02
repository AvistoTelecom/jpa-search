package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

public enum ListObjectFilterOperation implements IFilterOperation<List<Object>> {
    IN_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<Object> value) {
            return cb.or(value.stream().map(v -> ObjectFilterOperation.EQUAL.calculate(cb, expression, v)).toArray(Predicate[]::new));
        }
    };


    @Override
    public boolean needsMultipleValues() {
        return true;
    }
}
