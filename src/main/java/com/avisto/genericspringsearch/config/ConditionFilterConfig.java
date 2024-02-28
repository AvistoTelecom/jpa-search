package com.avisto.genericspringsearch.config;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.FilterOperationException;
import com.avisto.genericspringsearch.exception.WrongDataTypeException;
import com.avisto.genericspringsearch.operation.IFilterOperation;

import com.avisto.genericspringsearch.service.SearchUtils;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ConditionFilterConfig<R extends SearchableEntity, T, X> implements IFilterConfig<R, Boolean> {
    private final String key;
    private final IFilterConfig<R, T> filterTrue;
    private final IFilterConfig<R, X> filterFalse;
    private final T defaultValueTrue;
    private final X defaultValueFalse;

    private ConditionFilterConfig(String key, IFilterConfig<R, T> filterTrue, IFilterConfig<R, X> filterFalse,
        T defaultValueTrue, X defaultValueFalse) {
        this.filterTrue = filterTrue;
        this.filterFalse = filterFalse;
        this.key = key;
        this.defaultValueTrue = defaultValueTrue;
        this.defaultValueFalse = defaultValueFalse;
    }

    public static <R extends SearchableEntity, T, X> ConditionFilterConfig<R, T, X> of(String key, IFilterConfig<R, T> filterTrue, IFilterConfig<R, X> filterFalse,
        T defaultValueTrue, X defaultValueFalse) {
        return new ConditionFilterConfig<>(key, filterTrue, filterFalse, defaultValueTrue, defaultValueFalse);
    }

    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb,
        Map<String, Join<R, ?>> joins, Boolean value) {
        if (value) {
            return filterTrue.getPredicate(rootClazz, root, cb, joins, defaultValueTrue);
        }
        return filterFalse.getPredicate(rootClazz, root, cb, joins, defaultValueFalse);
    }

    @Override
    public Class<Boolean> getEntryClass(Class<R> rootClazz) {
        return Boolean.class;
    }

    @Override
    public boolean needMultipleValues() {
        return filterTrue.needMultipleValues() && filterFalse.needMultipleValues();
    }

    @Override
    public boolean needJoin() {
        return filterTrue.needJoin() && filterFalse.needJoin();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        filterTrue.checkConfig(rootClazz);
        filterFalse.checkConfig(rootClazz);
    }
}
