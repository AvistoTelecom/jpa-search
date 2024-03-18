package com.avisto.jpasearch.config;

import com.avisto.jpasearch.SearchableEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Map;

import static com.avisto.jpasearch.service.SearchConstants.Strings.DOT;

/**
 * This interface is used in FilterConfig, FilterSorterConfig, GroupFilterConfig and MultiFilterConfig.
 * It implements the common methods of this class.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 * @param <T> Filter type. For example, if the filter searches for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public non-sealed interface IFilterConfig<R extends SearchableEntity, T> extends ISearchConfig<R> {
    Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R,?>> joins, T value);

    Class<T> getEntryClass(Class<R> rootClazz);

    boolean needMultipleValues();

    boolean needJoin();

    default Join<R, ?> getJoin(From<R, ?> from, String toPath) {
        int firstIndex = toPath.indexOf(DOT);
        if (firstIndex == -1) {
            return from.join(toPath, JoinType.LEFT);
        }
        String joinFromKey = toPath.substring(0, firstIndex);
        String joinToKey = toPath.substring(firstIndex + 1);
        return getJoin(from.join(joinFromKey, JoinType.LEFT), joinToKey);
    }
}
