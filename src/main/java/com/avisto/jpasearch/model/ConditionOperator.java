package com.avisto.jpasearch.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.util.function.BiFunction;

/**
 * @author Martin Rech
 * @version 1.0
 */
public enum ConditionOperator {
    AND(CriteriaBuilder::and),
    OR(CriteriaBuilder::or);

    private final BiFunction<CriteriaBuilder, Predicate[], Predicate> conditionFunction;

    ConditionOperator(BiFunction<CriteriaBuilder, Predicate[], Predicate> conditionFunction) {
        this.conditionFunction = conditionFunction;
    }

    public Predicate applyCondition(CriteriaBuilder cb, Predicate... predicates) {
        return conditionFunction.apply(cb, predicates);
    }
}
