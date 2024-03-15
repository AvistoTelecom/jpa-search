package com.avisto.genericspringsearch;

import com.avisto.genericspringsearch.config.AbstractCriteria;

import java.util.List;
import java.util.Set;

/**
 * This class contains the filters, the sorts, the size and the page number for performing searches with pagination.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class SearchCriteria {
    private int pageNumber = 0;
    private int size = Integer.MAX_VALUE;
    private Set<FilterCriteria> filters;
    private List<OrderCriteria> sorts;

    public List<String> getFilterKeys() {
        return filters.stream().map(AbstractCriteria::getKey).toList();
    }

    public List<String> getSorterKeys() {
        return sorts.stream().map(AbstractCriteria::getKey).toList();
    }

    /**
     * This method returns True or False if the filter contains the key passed as an argument.
     * @param key to be compared
     * @return boolean : True or false
     */
    public boolean filtersContainsKey(String key) {
        return filters.stream().anyMatch(filterCriteria -> filterCriteria.getKey().equals(key));
    }

    /**
     * This method returns True or False if the sorter contains the key passed as an argument.
     * @param key to be compared
     * @return boolean : True or false
     */
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
