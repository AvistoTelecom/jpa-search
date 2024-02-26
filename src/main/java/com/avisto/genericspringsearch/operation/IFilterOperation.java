package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public interface IFilterOperation<T> {

    /**
     *
     * @param cb CriteriaBuilder
     * @param expression Filter
     * @param value
     * @return
     */
    Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T value);

    /**
     * Return if the operation need multiple values to work
     * @return Return True/False
     */
    boolean needsMultipleValues();

    /**
     * Get operation apply in the filter
     * @return return the operation apply in the filter
     */
    Class<T> getOperationType();
}
