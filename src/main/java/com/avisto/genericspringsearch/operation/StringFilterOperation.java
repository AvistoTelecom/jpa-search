package com.avisto.genericspringsearch.operation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import com.avisto.genericspringsearch.service.SearchUtils;

/**
 * This enumeration is used to compare strings.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public enum StringFilterOperation implements IFilterOperation<String> {

    CONTAIN {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return cb.like((Expression<String>) expression, "%" + value + "%");
        }
    },

    CONTAIN_IGNORE_CASE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return CONTAIN.calculate(cb, cb.lower((Expression<String>) expression), SearchUtils.toRootLowerCase(value));
        }
    },

    CONTAIN_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return CONTAIN_IGNORE_CASE.calculate(cb, cb.function(unAccentFunctionPath, String.class, expression), SearchUtils.normalizeAccentsAndDashes(value));
        }
    },

    START_WITH {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return cb.like((Expression<String>) expression, value + "%");
        }
    },

    START_WITH_IGNORE_CASE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return START_WITH.calculate(cb, cb.lower((Expression<String>) expression), SearchUtils.toRootLowerCase(value));
        }
    },

    START_WITH_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return START_WITH_IGNORE_CASE.calculate(cb, cb.function(unAccentFunctionPath, String.class, expression), SearchUtils.normalizeAccentsAndDashes(value));
        }
    },

    EQUAL_IGNORE_CASE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return ObjectFilterOperation.EQUAL.calculate(cb, cb.lower((Expression<String>) expression), SearchUtils.toRootLowerCase(value));
        }
    },

    EQUAL_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return EQUAL_IGNORE_CASE.calculate(cb, cb.function(unAccentFunctionPath, String.class, cb.lower((Expression<String>) expression)), SearchUtils.normalizeAccentsAndDashes(value));
        }
    };

    /**
     * Name of your unaccent function in your database manager
     */
    private static final String unAccentFunctionPath = getUnAccentFunctionPath();

    @Override
    public boolean needsMultipleValues() {
        return false;
    }

    @Override
    public Class<String> getOperationType() {
        return String.class;
    }

    private static String getUnAccentFunctionPath() {
        return System.getProperty("UNACCENT_FUNCTION_PATH") != null ? System.getProperty("UNACCENT_FUNCTION_PATH") : "unaccent";
    }
}
