package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.service.SearchUtils;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

public interface ISearchConfig<R extends SearchableEntity> {
    String getKey();

    boolean needMultipleValues();

    default Path<String> getPath(From<R, ?> from, String fieldPath) {
        if (SearchUtils.isBlank(fieldPath)) {
            return (From<R, String>) from;
        }
        String[] paths = fieldPath.split(REGEX_DOT);
        Path<String> entityPath = null;
        for (String path : paths) {
            if (entityPath == null) {
                entityPath = from.get(path);
            } else {
                entityPath = entityPath.get(path);
            }
        }
        return entityPath;
    }
}
