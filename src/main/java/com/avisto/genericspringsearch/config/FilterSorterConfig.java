package com.avisto.genericspringsearch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.avisto.genericspringsearch.operation.IFilterOperation;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

public class FilterSorterConfig<R extends SearchableEntity, T> extends FilterConfig<R, T> implements ISorterConfig<R> {

    private FilterSorterConfig(IFilterOperation<T> filterOperation, String key, List<String> paths) {
        super(filterOperation, key, paths);
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
    public void checkConfig(Class<R> rootClazz) {
        super.checkConfig(rootClazz);
        if (needJoin()) {
            throw new CannotSortException("Cannot sort on a Collection");
        }
    }

    @Override
    public Order getOrder(Root<R> root, CriteriaBuilder criteriaBuilder, SortDirection sortDirection) {
        return sortDirection.getOrder(criteriaBuilder, SearchUtils.getPath(root, getSortPath()));
    }

    @Override
    public String getSortPath() {
        return getFirstPath();
    }
}
