package com.avisto.genericspringsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import com.avisto.genericspringsearch.exception.FilterOperationException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.exception.WrongElementTypeException;
import com.avisto.genericspringsearch.service.SearchUtils;

public enum FilterOperation implements FilterOperationInterface {

    LIKE_IGNORE_CASE {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.like(cb.lower((Expression<String>) expression), "%" + SearchUtils.toRootLowerCase(getSingleParam(String.class, values)) + "%");
        }
    },

    LIKE_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.like(cb.function(unaccentFunction, String.class, cb.lower((Expression<String>) expression)), "%" + SearchUtils.toRootLowerCase(SearchUtils.normalizeAccentsAndDashes(getSingleParam(String.class, values))) + "%");
        }
    },

    START_WITH {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.like((Expression<String>) expression, getSingleParam(String.class, values) + "%");
        }
    },

    START_WITH_IGNORE_CASE {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.like(cb.lower((Expression<String>) expression), SearchUtils.toRootLowerCase(getSingleParam(String.class, values)) + "%");
        }
    },

    START_WITH_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.like(cb.function(unaccentFunction, String.class, cb.lower((Expression<String>) expression)), SearchUtils.toRootLowerCase(SearchUtils.normalizeAccentsAndDashes(getSingleParam(String.class, values))) + "%");
        }
    },

    EQUAL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            Object singleParam = getSingleParam(Object.class, values);
            if (singleParam == null) {
                return cb.isNull(expression);
            }
            return cb.equal(expression, singleParam);
        }
    },

    EQUAL_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            String singleParam = getSingleParam(String.class, values);
            if (singleParam == null) {
                return cb.isNull(expression);
            }
            return cb.equal(cb.function(unaccentFunction, String.class, cb.lower((Expression<String>) expression)), SearchUtils.toRootLowerCase(SearchUtils.normalizeAccentsAndDashes(singleParam)));
        }
    },

    NOT_NULL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.isNotNull(expression);
        }
    },

    COLLECTION_IS_EMPTY {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.isEmpty((Expression<Collection<?>>) expression);
        }
    },

    IN_EQUAL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            List<Predicate> predicates = new ArrayList<>();
            for (T value : values) {
                predicates.add(EQUAL.calculate(cb, expression, value));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        }

        @Override
        public boolean needsMultipleValues() {
            return true;
        }
    },

    IN_LIKE {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            List<Predicate> predicates = new ArrayList<>();
            for (T value : values
            ) {
                predicates.add(LIKE_IGNORE_CASE.calculate(cb, expression, value));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        }

        @Override
        public boolean needsMultipleValues() {
            return true;
        }
    },

    IN_EQUAL_IGNORE_CASE_IGNORE_ACCENT {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            List<Predicate> predicates = new ArrayList<>();
            for (T value : values) {
                predicates.add(EQUAL_IGNORE_CASE_IGNORE_ACCENT.calculate(cb, expression, value));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        }

        @Override
        public boolean needsMultipleValues() {
            return true;
        }
    },

    BETWEEN {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            List<Comparable> list = getBothParams(Comparable.class, values);
            if (list.get(0) == null && list.get(1) == null) {
                throw new FilterOperationException("Both elements of the between operation are null");
            }
            if (list.get(0) == null) {
                return LESS_THAN_OR_EQUAL.calculate(cb, expression, list.get(1));
            }
            if (list.get(1) == null) {
                return GREATER_THAN_OR_EQUAL.calculate(cb, expression, list.get(0));
            }
            return cb.between((Expression<Comparable>) expression, list.get(0), list.get(1));
        }

        @Override
        public boolean needsMultipleValues() {
            return true;
        }
    },

    GREATER_THAN_OR_EQUAL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.greaterThanOrEqualTo((Expression<Comparable>) expression, getSingleParam(Comparable.class, values));
        }
    },
    GREATER_THAN_OR_EQUAL_OR_NULL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(GREATER_THAN_OR_EQUAL.calculate(cb, expression, values));
            predicates.add(cb.isNull(expression));
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        public <T> Predicate calculate(CriteriaBuilder cb, Expression<?> expression, T... values) {
            return cb.lessThanOrEqualTo((Expression<Comparable>) expression, getSingleParam(Comparable.class, values));
        }
    };

    private static String unaccentFunction = "unaccent";

    private static <T> T getSingleParam(Class<T> clazz, Object... values) {
        return getParams(clazz, 1, values).get(0);
    }

    private static <T> List<T> getBothParams(Class<T> clazz, Object... values) {
        return getParams(clazz, 2, values).stream().limit(2).collect(Collectors.toList());
    }

    private static <T> List<T> getParams(Class<T> clazz, int paramNumber, Object... values) {
        if (values == null) {
            if (paramNumber > 1) {
                throw new WrongElementNumberException(String.format("Expected %s parameters, but got null", paramNumber));
            }
            return Collections.nCopies(1, null);
        }
        if (values.length != paramNumber) {
            throw new WrongElementNumberException(String.format("Expected %s parameters, but got %s", paramNumber, values.length));
        }
        Arrays.stream(values).forEach(o -> {
            if (o != null && !clazz.isAssignableFrom(o.getClass())) {
                throw new WrongElementTypeException(String.format("%s is not assignable to %s", o.getClass().getSimpleName(), clazz.getSimpleName()));
            }
        });
        return (List<T>) Arrays.asList(values);
    }

    @Override
    public boolean needsMultipleValues() {
        return false;
    }
}
