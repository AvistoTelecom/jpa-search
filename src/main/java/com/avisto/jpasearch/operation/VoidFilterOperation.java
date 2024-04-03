package com.avisto.jpasearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Collection;

/**
 * This enum provides NOT_NULL, NULL and COLLECTION_IS_EMPTY operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum VoidFilterOperation implements IFilterOperation<Void> {

    NOT_NULL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Void value) {
            return cb.isNotNull(expression);
        }
    },

    NULL {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Void value) {
            return cb.isNull(expression);
        }
    },

    COLLECTION_IS_EMPTY {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Void value) {
            return cb.isEmpty((Expression<Collection<?>>) expression);
        }
    };

    @Override
    public boolean needsMultipleValues() {
        return false;
    }

    @Override
    public Class<Void> getOperationType() {
        return Void.class;
    }
}
