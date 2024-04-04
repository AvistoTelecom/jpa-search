package com.avisto.jpasearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * This interface is implemented in all operations. It provides the basic methods.
 * If you want to create a custom IFilterOperation, you can easily create one like this :
 * <pre>
 * {@code
 * public enum CustomFilterOperation implements IFilterOperation<Object> {
 *     NOT_EQUAL {
 *         public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Object value) {
 *             return value == null ? cb.isNotNull(expression) : cb.notEqual(expression, value);
 *         }
 *     };
 *
 *     private CustomFilterOperation() {
 *     }
 *
 *     public boolean needsMultipleValues() {
 *         return false;
 *     }
 *
 *     public Class<Object> getOperationType() {
 *         return Object.class;
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> Filter type. For example, if the filter searches for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 *
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
