package com.avisto.jpasearch.config;

import java.util.ArrayList;
import java.util.List;

import com.avisto.jpasearch.operation.IFilterOperation;
import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.exception.CannotSortException;
import com.avisto.jpasearch.model.SortDirection;
import com.avisto.jpasearch.service.SearchUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
     * @deprecated
     * This method will be replaced by testConfig in 1.0.0
     * <p> Use {@link FilterSorterConfig#testConfig(Class)} instead.
     *
     * @param rootClazz Class to be analyzed
     */
    @Override
    @Deprecated(since = "0.0.3", forRemoval = true)
    public void checkConfig(Class<R> rootClazz) {
        super.checkConfig(rootClazz);
        if (getSortPath().contains("[")) {
            throw new CannotSortException("Cannot sort on a Collection");
        }
    }

    /**
     * Check that you're not trying to sort two objects of different type, or a wrong operation for a field.
     *
     * @return Boolean True if it's ok and false if not
     * @param rootClazz Class to be analyzed
     */
    @Override
    public Boolean testConfig(Class<R> rootClazz) {
        if (super.testConfig(rootClazz).equals(FALSE) || getSortPath().contains("[")) {
            return FALSE;
        }
        return TRUE;
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
