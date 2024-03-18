package com.avisto.jpasearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

/**
 * This enum provide in_equal operation.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum ListObjectFilterOperation implements IFilterOperation<List<Object>> {

    /**
     * Checks that the expression is different from the value passed as a parameter.
     */
    IN_EQUAL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<Object> value) {
            CriteriaBuilder.In<Object> inClause = cb.in(expression);
            value.forEach(inClause::value);
            return inClause;
        }
    };


    @Override
    public boolean needsMultipleValues() {
        return true;
    }

    @Override
    public Class<List<Object>> getOperationType() {
        return (Class<List<Object>>) (Class)List.class;
    }
}
