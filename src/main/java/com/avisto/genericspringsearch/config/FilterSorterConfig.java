package com.avisto.genericspringsearch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.avisto.genericspringsearch.FilterOperation;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.model.FieldPathObject;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

public class FilterSorterConfig<R extends SearchableEntity, T> implements IFilterConfig<R, T>, ISorterConfig<R> {
    private final FilterOperation filterOperation;
    private final String key;
    private final List<String> paths;

    private FilterSorterConfig(FilterOperation filterOperation, String key, List<String> paths) {
        this.filterOperation = filterOperation;
        this.key = key;
        this.paths = paths;
    }

    public static <R extends SearchableEntity, T> FilterSorterConfig<R, T> of(FilterOperation filterOperation, String key, String pathFirst, String... paths) {
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
    public Predicate getPredicate(Root<R> root, CriteriaBuilder criteriaBuilder, Map<String, Join<R, ?>> joins, T... values) {
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
                    orPredicates.add(filterOperation.calculate(
                            criteriaBuilder,
                            path,
                            values
                    ));
                }
        );
        return criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));
    }

    @Override
    public Class<?> getEntryClass(Class<R> rootClazz) {
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
}
