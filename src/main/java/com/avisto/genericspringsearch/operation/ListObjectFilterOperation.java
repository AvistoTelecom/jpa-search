package com.avisto.genericspringsearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

public enum ListObjectFilterOperation implements IFilterOperation<List<Object>> {
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
