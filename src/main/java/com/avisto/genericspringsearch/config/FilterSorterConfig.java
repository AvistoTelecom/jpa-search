package com.avisto.genericspringsearch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.avisto.genericspringsearch.operation.IFilterOperation;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.model.FieldPathObject;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.service.CastService;
import com.avisto.genericspringsearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

public class FilterSorterConfig<R extends SearchableEntity, T> implements IFilterConfig<R, T>, ISorterConfig<R> {
    private final IFilterOperation<T> filterOperation;
    private final String key;
    private final List<String> paths;

    private FilterSorterConfig(IFilterOperation<T> filterOperation, String key, List<String> paths) {
        this.filterOperation = filterOperation;
        this.key = key;
        this.paths = paths;
    }

    public static <R extends SearchableEntity, T> FilterSorterConfig<R, T> of(IFilterOperation<T> filterOperation, String key, String pathFirst, String... paths) {
        List<String> result = new ArrayList<>();
        result.add(pathFirst);
        if (paths != null) {
            result.addAll(List.of(paths));
        }
        return new FilterSorterConfig<>(filterOperation, key, result);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean needMultipleValues() {
        return filterOperation.needsMultipleValues();
    }

    @Override
    public void checkConfig(Class<R> rootClazz) {
        paths.forEach(path -> SearchUtils.getEntityClass(rootClazz, path.split(REGEX_DOT)));
        if (getSortPath().contains("[")) {
            throw new CannotSortException("Cannot sort on a Collection");
        }
    }

    private List<FieldPathObject> getDefaultFieldPath() {
        return this.paths.stream().map(FieldPathObject::of).toList();
    }

    private String getFirstFilterPath() {
        return this.paths.get(0);
    }


    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins, T value) {
        List<Predicate> orPredicates = new ArrayList<>();
        getDefaultFieldPath().forEach(
                fieldPath -> {
                    String stringBasePath = fieldPath.getLeft();
                    Path<String> path;
                    if (fieldPath.needsJoin()) {
                        if (!joins.containsKey(stringBasePath)) {
                            joins.put(stringBasePath, getJoin(root, stringBasePath));
                        }
                        path = SearchUtils.getPath(joins.get(stringBasePath), fieldPath.getRight());
                    }
                    else {
                        path = SearchUtils.getPath(root, stringBasePath);
                    }
                    if (filterOperation.needsMultipleValues()) {
                        Class<?> targetClazz = getTargetClass(rootClazz);
                        orPredicates.add(filterOperation.calculate(
                                cb,
                                path,
                                (T) ((List<String>) value).stream().map(v -> CastService.cast(v, targetClazz)).toList())
                        );
                    } else {
                        orPredicates.add(filterOperation.calculate(
                                cb,
                                path,
                                value
                        ));
                    }
                }
        );
        return cb.or(orPredicates.toArray(Predicate[]::new));
    }

    @Override
    public Class<T> getEntryClass(Class<R> rootClazz) {
        if (needMultipleValues()) {
            return (Class<T>) List.class;
        }
        return (Class<T>) getTargetClass(rootClazz);
    }

    private Class<?> getTargetClass(Class<R> rootClazz) {
        return SearchUtils.getEntityClass(rootClazz, paths.get(0).split(REGEX_DOT));
    }

    @Override
    public Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection) {
        return sortDirection.getOrder(criteriaBuilder, SearchUtils.getPath(root, getFirstFilterPath()));
    }

    @Override
    public String getSortPath() {
        return paths.get(0);
    }

    @Override
    public boolean needJoin() {
        return paths.stream().anyMatch(path -> path.contains("["));
    }
}
