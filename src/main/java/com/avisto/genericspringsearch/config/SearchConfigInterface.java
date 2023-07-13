package com.avisto.genericspringsearch.config;

import java.util.List;
import java.util.stream.Collectors;

import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.model.Pair;

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

    default List<Pair<String, String>> getDefaultFieldPath() {
        return splitFieldListPath().stream().map(path -> (Pair<String, String>) switch (path.length) {
            case 1 -> Pair.of(path[0], null);
            case 2 -> Pair.of(path[0], path[1]);
            default -> Pair.of(null, null);
        }).collect(Collectors.toList());
    }

    default List<String> getInsideListPath() {
        return splitFieldListPath().stream().map(path -> {
            if (path.length > 1) {
                return path[1];
            }
            return null;
        }).collect(Collectors.toList());
    }

    default List<String[]> splitFieldListPath() {
        return getFilterPaths().stream().map(path -> path.split("\\[|\\]")).collect(Collectors.toList());
    }

}
