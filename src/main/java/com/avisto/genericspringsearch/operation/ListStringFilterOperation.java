package com.avisto.genericspringsearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

public enum ListStringFilterOperation implements IFilterOperation<List<String>> {

    IN_LIKE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<String> value) {
            return cb.or(value.stream().map(v -> StringFilterOperation.LIKE_IGNORE_CASE.calculate(cb, expression, v)).toArray(Predicate[]::new));
        }
    },

    IN_EQUAL_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<String> value) {
            return cb.or(value.stream().map(v -> StringFilterOperation.EQUAL_IGNORE_CASE_IGNORE_ACCENT.calculate(cb, expression, v)).toArray(Predicate[]::new));
        }
    };


    @Override
    public boolean needsMultipleValues() {
        return true;
    }

    @Override
    public Class<List<String>> getOperationType() {
        return (Class<List<String>>) (Class)List.class;
    }
}
