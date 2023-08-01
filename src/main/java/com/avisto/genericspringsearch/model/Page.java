package com.avisto.genericspringsearch.model;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Page<T>(List<T> elements, int pageNumber, int pageSize, long totalElements) {

    public static <T> Page<T> empty() {
        return new Page<>(List.of(), 0, 0, 0L);
    }

    public Stream<T> stream() {
        return elements.stream();
    }

    public <D> Page<D> map(Function<T, D> mapper) {
        return new Page<>(elements.stream().map(mapper).collect(Collectors.toList()), pageNumber, pageSize, totalElements);
    }

}
