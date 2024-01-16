package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.WrongDataTypeException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;
import java.util.function.Function;

public class TransformFilterConfig<R extends SearchableEntity, T, X> implements IFilterConfig<R, T> {
    private final String key;
    private final IFilterConfig<R, X> filter;
    private final Class<T> entryClazz;
    private final Function<T, X> mapper;
    private final Class<X> exitClazz;

    private TransformFilterConfig(String key, IFilterConfig<R, X> filter, Class<T> entryClazz, Class<X> exitClazz, Function<T, X> mapper) {
        this.key = key;
        this.filter = filter;
        this.entryClazz = entryClazz;
        this.exitClazz = exitClazz;
        this.mapper = mapper;
    }

    public static <R extends SearchableEntity, T, X> TransformFilterConfig<R, T, X> of(String key, IFilterConfig<R, X> filter, Class<T> entryClazz, Class<X> exitClazz, Function<T, X> mapper) {
        return new TransformFilterConfig<>(key, filter, entryClazz, exitClazz, mapper);
    }

    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins, T value) {
        return filter.getPredicate(rootClazz, root, cb, joins, mapper.apply(value));
    }

    @Override
    public Class<T> getEntryClass(Class<R> rootClazz) {
        return this.entryClazz;
    }

    @Override
    public boolean needMultipleValues() {
        return this.filter.needMultipleValues();
    }

    @Override
    public boolean needJoin() {
        return this.filter.needJoin();
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        if (filter.getEntryClass(rootClazz) != exitClazz) {
            throw new WrongDataTypeException("Transform filter config cannot transform data to this type");
        }
    }
}
