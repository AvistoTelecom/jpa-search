package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.TypeNotHandledException;
import com.avisto.genericspringsearch.service.CastService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class allows you to use a filter group that you may or may not have previously defined.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
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

    /**
     * This method returns a predicate by applying a filter
     *
     * @param rootClazz Class to be analyzed
     * @param root Root
     * @param cb Criteria Builder
     * @param joins joins
     * @param value Value use to filter
     * @return Predicate
     */
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

    /**
     * Get the entryClass to access a field
     *
     * @param rootClazz Class to be analyzed
     * @return EntryClass
     */
    @Override
    public Class<Map<String, String>> getEntryClass(Class<R> rootClazz) {
        return (Class<Map<String,String>>) (Class)Map.class;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * Specify if the filter needs multiple values to access a field.
     * @return boolean
     */
    @Override
    public boolean needMultipleValues() {
        return false;
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
    }

    @Override
    public boolean needJoin() {
        return filters.stream().anyMatch(IFilterConfig::needJoin);
    }
}
