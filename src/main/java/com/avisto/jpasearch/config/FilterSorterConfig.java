package com.avisto.jpasearch.config;

import java.util.ArrayList;
import java.util.List;

import com.avisto.jpasearch.operation.IFilterOperation;
import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.exception.CannotSortException;
import com.avisto.jpasearch.model.SortDirection;
import com.avisto.jpasearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

/**
 * This class is a combination of a FilterOperation and a SorterOperation. You can use it as a filter, a sorter or both.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 * @param <T> Filter type. For example, if the filter searches for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FilterSorterConfig<R extends SearchableEntity, T> extends FilterConfig<R, T> implements ISorterConfig<R> {

    private FilterSorterConfig(IFilterOperation<T> filterOperation, String key, List<String> paths) {
        super(key, filterOperation, paths);
    }

    public static <R extends SearchableEntity, T> FilterSorterConfig<R, T> of(String key, IFilterOperation<T> filterOperation, String pathFirst, String... paths) {
        List<String> result = new ArrayList<>();
        result.add(pathFirst);
        if (paths != null) {
            result.addAll(List.of(paths));
        }
        return new FilterSorterConfig<>(filterOperation, key, result);
    }

    /**
     * Check that you're not trying to sort on a collection, or two objects of different type, or a wrong operation for a field.
     * @param rootClazz Class to be analyzed
     */
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
