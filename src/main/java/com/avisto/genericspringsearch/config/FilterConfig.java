package com.avisto.genericspringsearch.config;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.avisto.genericspringsearch.FilterOperation;
import com.avisto.genericspringsearch.SearchableEntity;
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

public class FilterConfig<R extends SearchableEntity, T> implements IFilterConfig<R, T, T>, ISorterConfig<R> {
    private final FilterOperation filterOperation;
    private final String key;
    private final List<String> paths;

    private FilterConfig(FilterOperation filterOperation, String key, List<String> paths) {
        this.filterOperation = filterOperation;
        this.key = key;
        this.paths = paths;
    }

    public static FilterConfig of(FilterOperation filterOperation, String key, String pathFirst, String... paths) {
        List<String> result = new ArrayList<>();
        result.add(pathFirst);
        if (paths != null) {
            result.addAll(List.of(paths));
        }
        return new FilterConfig(filterOperation, key, result);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean needMultipleValues() {
        return filterOperation.needsMultipleValues();
    }

    private List<FieldPathObject> getDefaultFieldPath() {
        return this.paths.stream().map(FieldPathObject::of).collect(Collectors.toList());
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
                        path = getPath(joins.get(stringBasePath), fieldPath.getRight());
                    }
                    else {
                        path = getPath(root, stringBasePath);
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
    public Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection) {
        return sortDirection.getOrder(criteriaBuilder, getPath(root, getFirstFilterPath()));
    }
}
