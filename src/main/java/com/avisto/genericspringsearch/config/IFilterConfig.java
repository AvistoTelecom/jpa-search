package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.DOT;

public non-sealed interface IFilterConfig<R extends SearchableEntity, T> extends ISearchConfig<R> {
    Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R,?>> joins, T value);

    Class<T> getEntryClass(Class<R> rootClazz);

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
