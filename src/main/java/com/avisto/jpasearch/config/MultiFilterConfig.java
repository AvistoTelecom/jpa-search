package com.avisto.jpasearch.config;

import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.service.CastService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

/**
 * MultiFilterConfig lets you apply a filter several times. For example, you can search for an Employee who has two pets, a dog and a cat.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 * @param <X> Parent filter type. For example, if the parent filter is looking for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
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

    /**
     * Get the entryClass to access a field
     *
     * @param rootClazz Class to be analyzed
     * @return EntryClass
     */
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

    /**
     * Specify if the filter needs multiple values to access a field.
     * @return boolean
     */
    @Override
    public boolean needMultipleValues() {
        return true;
    }

    /**
     * Check the link with the other entity.
     * @param rootClazz Entity class
     */
    @Override
    public void checkConfig(Class<R> rootClazz) {
        //TODO : check if we need to check
    }
}
