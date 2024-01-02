package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

public enum ListStringFilterOperation implements IFilterOperation<List<String>> {
    IN_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<String> value) {
            return cb.or(value.stream().map(v -> ObjectFilterOperation.EQUAL.calculate(cb, expression, v)).toArray(Predicate[]::new));
        }
    },

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
}
