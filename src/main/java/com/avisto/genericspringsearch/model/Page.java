package com.avisto.genericspringsearch.model;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Page class for pagination.
 *
 * @param elements List of elements to be stored
 * @param pageNumber Page Number
 * @param pageSize Page Size
 * @param totalElements Number of elements
 * @param <T> Element type
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public record Page<T>(List<T> elements, int pageNumber, int pageSize, long totalElements) {

    /**
     * Return an empty page.
     *
     * @return Page
     * @param <T> Element type
     */
    public static <T> Page<T> empty() {
        return new Page<>(List.of(), 0, 0, 0L);
    }

    /**
     * Return a stream of elements contained in the page
     * @return Stream of elements
     */
    public Stream<T> stream() {
        return elements.stream();
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param mapper Entity mapper
     * @return A Page object containing the search results with pagination information.
     * @param <D> The type of the object that will be returned in the Page object.
     */
    public <D> Page<D> map(Function<T, D> mapper) {
        return new Page<>(elements.stream().map(mapper).collect(Collectors.toList()), pageNumber, pageSize, totalElements);
    }

}
