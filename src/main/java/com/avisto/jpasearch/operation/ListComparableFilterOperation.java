package com.avisto.jpasearch.operation;

import com.avisto.jpasearch.exception.FilterOperationException;
import com.avisto.jpasearch.exception.WrongElementNumberException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * This enum provide between operation.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum ListComparableFilterOperation implements IFilterOperation<List<Comparable>> {

    /**
     * Check if expression is between two values.
     */
    BETWEEN {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, List<Comparable> value) {
            if (value.size() != 2) {
                throw new WrongElementNumberException(String.format("Expected 2 parameters, but got %s", value.size()));
            }
            if (value.get(0) == null && value.get(1) == null) {
                throw new FilterOperationException("Both elements of the between operation are null");
            }
            if (value.get(0) == null) {
                return ComparableFilterOperation.LESS_THAN_OR_EQUAL.calculate(cb, expression, value.get(1));
            }
            if (value.get(1) == null) {
                return ComparableFilterOperation.GREATER_THAN_OR_EQUAL.calculate(cb, expression, value.get(0));
            }
            return cb.between((Expression<Comparable>) expression, value.get(0), value.get(1));
        }
    };

    @Override
    public boolean needsMultipleValues() {
        return true;
    }

    @Override
    public Class<List<Comparable>> getOperationType() {
        return (Class<List<Comparable>>) (Class)List.class;
    }
}
