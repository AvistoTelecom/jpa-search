package com.avisto.genericspringsearch.config;

import java.util.List;
import java.util.stream.Collectors;

import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.model.FieldPathObject;

public interface SearchConfigInterface {

    FilterConfig getFilterConfig();

    OrderCriteria getDefaultOrderCriteria();

    default String getFilterKey() {
        return getFilterConfig().getKey();
    }

    default List<String> getFilterPaths() {
        return getFilterConfig().getPaths();
    }

    default boolean needsJoin() {
        return getFilterPaths().stream().anyMatch(path -> path != null && path.contains("["));
    }

    default List<FieldPathObject> getDefaultFieldPath() {
        return getFilterPaths().stream().map(FieldPathObject::of).collect(Collectors.toList());
    }

}
