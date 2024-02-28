package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Collection;

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
