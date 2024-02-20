package com.avisto.genericspringsearch.operation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import com.avisto.genericspringsearch.service.SearchUtils;

public enum StringFilterOperation implements IFilterOperation<String> {

    LIKE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return cb.like((Expression<String>) expression, "%" + value + "%");
        }
    },

    LIKE_IGNORE_CASE {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            return LIKE.calculate(cb, cb.lower((Expression<String>) expression), SearchUtils.toRootLowerCase(value));
        }
    },

    LIKE_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, String value) {
            String unaccentFunction = System.getProperty("UNACCENT_FUNCTION_NAME") != null ? System.getenv("UNACCENT_FUNCTION_NAME") : "unaccent";
            String schemaUnAccentFunction = System.getProperty("SCHEMA_UNACCENT_FUNCTION_NAME") != null ? System.getenv("SCHEMA_UNACCENT_FUNCTION_NAME") : "public";
            return LIKE_IGNORE_CASE.calculate(cb, cb.function(unaccentFunction, String.class, expression), SearchUtils.normalizeAccentsAndDashes(value));
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
            String unaccentFunction = System.getProperty("UNACCENT_FUNCTION_NAME") != null ? System.getenv("UNACCENT_FUNCTION_NAME") : "unaccent";
            String schemaUnAccentFunction = System.getProperty("SCHEMA_UNACCENT_FUNCTION_NAME") != null ? System.getenv("SCHEMA_UNACCENT_FUNCTION_NAME") : "public";
            return START_WITH_IGNORE_CASE.calculate(cb, cb.function(unaccentFunction, String.class, expression), SearchUtils.normalizeAccentsAndDashes(value));
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
            String unaccentFunction = System.getProperty("UNACCENT_FUNCTION_NAME") != null ? System.getenv("UNACCENT_FUNCTION_NAME") : "unaccent";
            String schemaUnAccentFunction = System.getProperty("SCHEMA_UNACCENT_FUNCTION_NAME") != null ? System.getenv("SCHEMA_UNACCENT_FUNCTION_NAME") : "public";
            return EQUAL_IGNORE_CASE.calculate(cb, cb.function(schemaUnAccentFunction + "." + unaccentFunction, String.class, cb.lower((Expression<String>) expression)), SearchUtils.normalizeAccentsAndDashes(value));
        }
    };

//    private static String unaccentFunction = System.getProperty("UNACCENT_FUNCTION_NAME") != null ? System.getenv("UNACCENT_FUNCTION_NAME") : "unaccent";
//    private static String schemaUnAccentFunction = System.getProperty("SCHEMA_UNACCENT_FUNCTION_NAME") != null ? System.getenv("SCHEMA_UNACCENT_FUNCTION_NAME") : "public";

    @Override
    public boolean needsMultipleValues() {
        return false;
    }

    @Override
    public Class<String> getOperationType() {
        return String.class;
    }
}
