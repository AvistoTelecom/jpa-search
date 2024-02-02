package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.TypeNotHandledException;
import com.avisto.genericspringsearch.service.CastService;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupFilterConfig<R extends SearchableEntity> implements IFilterConfig<R, Map<String, String>> {
    private final String key;
    private final List<IFilterConfig> filters;

    private GroupFilterConfig(String key, List<IFilterConfig> filters) {
        this.key = key;
        this.filters = filters;
    }

    public static <R extends SearchableEntity> GroupFilterConfig<R> of(String key, IFilterConfig firstFilter, IFilterConfig... filters) {
        List<IFilterConfig> result = new ArrayList<>();
        result.add(firstFilter);
        if (filters != null) {
            result.addAll(List.of(filters));
        }
        return new GroupFilterConfig<>(key, result);
    }
    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins, Map<String, String> value) {
        if (value == null) {
            throw new TypeNotHandledException("Cannot group null or empty filters");
        }
        return cb.and(filters
                .stream()
                .map(filter -> {
                    Class<?> filterClazz = filter.getEntryClass(rootClazz);
                    return filter.getPredicate(rootClazz, root, cb, joins, CastService.cast(value.get(filter.getKey()), filterClazz));
                })
                .toArray(Predicate[]::new));

    }

    @Override
    public Class<Map<String, String>> getEntryClass(Class<R> rootClazz) {
        return (Class<Map<String,String>>) (Class)Map.class;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public boolean needMultipleValues() {
        return false;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        //TODO : check if we need to check
    }

    @Override
    public boolean needJoin() {
        return filters.stream().anyMatch(IFilterConfig::needJoin);
    }
}
