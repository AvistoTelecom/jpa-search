package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.service.CastService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

public class MultiFilterConfig<R extends SearchableEntity, X> implements IFilterConfig<R, List<String>> {
    private final String key;
    private final IFilterConfig<R, X> filter;
    private final String joinPath;

    private MultiFilterConfig(String key, IFilterConfig filter, String joinPath) {
        this.key = key;
        this.filter = filter;
        this.joinPath = joinPath;
    }

    public static <R extends SearchableEntity, X> MultiFilterConfig<R, X> of(String key, IFilterConfig<R, X> filter, String joinPath) {
        return new MultiFilterConfig<>(key, filter, joinPath);
    }

    public static <R extends SearchableEntity, X> MultiFilterConfig<R, X> of(String key, IFilterConfig<R, X> filter) {
        return new MultiFilterConfig<>(key, filter, null);
    }

    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins, List<String> value) {
        Class<X> filterClazz = filter.getEntryClass(rootClazz);
        return cb.and(value
                .stream()
                .map(v -> {
                    if (joinPath != null) {
                        joins.put(joinPath, getJoin(root, joinPath));
                    }
                    return filter.getPredicate(rootClazz, root, cb, joins, CastService.cast(v, filterClazz));
                })
                .toArray(Predicate[]::new));

    }

    @Override
    public Class<List<String>> getEntryClass(Class<R> rootClazz) {
        return (Class<List<String>>) (Class)List.class;
    }

    @Override
    public boolean needJoin() {
        return filter.needJoin();
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public boolean needMultipleValues() {
        return true;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        //TODO : check if we need to check
    }
}
