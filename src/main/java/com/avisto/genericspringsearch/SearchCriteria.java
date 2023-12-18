package com.avisto.genericspringsearch;

import java.util.List;
import java.util.Set;

public class SearchCriteria {
    private int pageNumber = 0;
    private int size = Integer.MAX_VALUE;
    private Set<FilterCriteria> filters;
    private List<OrderCriteria> sorts;

    public boolean filtersContainsKey(String key) {
        return filters.stream().anyMatch(filterCriteria -> filterCriteria.getKey().equals(key));
    }

    public boolean sortsContainsKey(String key) {
        return sorts.stream().anyMatch(filterCriteria -> filterCriteria.getKey().equals(key));
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getSize() {
        return size;
    }

    public Set<FilterCriteria> getFilters() {
        return filters;
    }

    public List<OrderCriteria> getSorts() {
        return sorts;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setFilters(Set<FilterCriteria> filters) {
        this.filters = filters;
    }

    public void setSorts(List<OrderCriteria> sorts) {
        this.sorts = sorts;
    }
}
