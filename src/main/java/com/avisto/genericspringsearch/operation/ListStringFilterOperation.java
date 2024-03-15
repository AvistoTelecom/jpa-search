package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * This enum provides IN_LIKE and IN_EQUAL_IGNORE_CASE_IGNORE_ACCENT operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum ListStringFilterOperation implements IFilterOperation<List<String>> {

    /**
     * Checks that the expression contains at least one of the following values (LIKE) (Ignore Case)
     */
    IN_CONTAIN {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<String> value) {
            return cb.or(value.stream().map(v -> StringFilterOperation.CONTAIN_IGNORE_CASE.calculate(cb, expression, v)).toArray(Predicate[]::new));
        }
    },
    /**
     * Checks that the expression contains at least one of the following values (EQUAL) (Ignore Case, Ignore Accent)
     */
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
