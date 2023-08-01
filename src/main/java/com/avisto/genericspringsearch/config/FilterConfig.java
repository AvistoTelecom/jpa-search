package com.avisto.genericspringsearch.config;

import java.util.ArrayList;
import java.util.List;

import com.avisto.genericspringsearch.FilterOperation;

public class FilterConfig {

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

    public FilterOperation getFilterOperation() {
        return filterOperation;
    }

    public String getKey() {
        return key;
    }

    public List<String> getPaths() {
        return paths;
    }
}
