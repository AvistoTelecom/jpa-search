package com.avisto.jpasearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * This interface is implemented in all operations. It provides the basic methods.
 *
 * @param <T> Filter type. For example, if the filter searches for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public interface IFilterOperation<T> {

    /**
     *
     * @param cb CriteriaBuilder
     * @param expression Filter
     * @param value Value to compared in the operation.
     * @return Predicate that describe the operation.
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
