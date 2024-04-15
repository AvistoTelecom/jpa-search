package com.avisto.jpasearch.config;

import com.avisto.jpasearch.exception.FilterOperationException;
import com.avisto.jpasearch.exception.WrongDataTypeException;
import com.avisto.jpasearch.operation.IFilterOperation;
import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.model.FieldPathObject;
import com.avisto.jpasearch.service.CastService;
import com.avisto.jpasearch.service.SearchUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.avisto.jpasearch.service.SearchConstants.Strings.REGEX_DOT;

/**
 * This class contains the FilterOperation, the key and the paths to create a filter that you can call with the search method.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 * @param <T> Filter type. For example, if the filter searches for a name, the value will be String.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FilterConfig<R extends SearchableEntity, T> implements IFilterConfig<R, T> {

    private final IFilterOperation<T> filterOperation;
    private final String key;
    private final List<String> paths;

    protected FilterConfig(String key, IFilterOperation<T> filterOperation, List<String> paths) {
        this.filterOperation = filterOperation;
        this.key = key;
        this.paths = paths;
    }

    public static <R extends SearchableEntity, T> FilterConfig<R, T> of(String key, IFilterOperation<T> filterOperation, String pathFirst, String... paths) {
        List<String> result = new ArrayList<>();
        result.add(pathFirst);
        if (paths != null) {
            result.addAll(List.of(paths));
        }
        return new FilterConfig<>(key, filterOperation, result);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean needMultipleValues() {
        return filterOperation.needsMultipleValues();
    }

    /**
     * Check that you're not trying to sort two objects of different type, or a wrong operation for a field.
     * @param rootClazz Class to be analyzed
     */
    @Override
    public void checkConfig(Class<R> rootClazz) {
        Class<T> entryClazz = getEntryClass(rootClazz);
        Class<?> targetClazz = getTargetClass(rootClazz);
        Class<?> sanitizeEntryClazz = entryClazz;
        if (SearchUtils.isPrimitiveType(entryClazz)) {
            sanitizeEntryClazz = SearchUtils.getObjectTypeFromPrimitiveType(entryClazz);
        }
        if (filterOperation.getOperationType() != Void.class && !filterOperation.getOperationType().isAssignableFrom(sanitizeEntryClazz)) {
            throw new FilterOperationException(String.format("Filter Operation with operation type %s cannot be assigned to %s", filterOperation.getOperationType(), entryClazz));
        }
        if (paths.stream().anyMatch(path -> SearchUtils.getEntityClass(rootClazz, path.split(REGEX_DOT)) != targetClazz)) {
            throw new WrongDataTypeException("Filter config cannot filter on 2 different object types");
        }
    }

    private List<FieldPathObject> getDefaultFieldPath() {
        return this.paths.stream().map(FieldPathObject::of).toList();
    }

    public String getFirstPath() {
        return this.paths.get(0);
    }

    /**
     * This method returns a predicate by applying a filter
     *
     * @param rootClazz Class to be analyzed
     * @param root Root
     * @param cb Criteria Builder
     * @param joins joins
     * @param value Value use to filter
     * @return Predicate
     */
    @Override
    public Predicate getPredicate(Class<R> rootClazz, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins, T value) {
        List<Predicate> orPredicates = new ArrayList<>();
        getDefaultFieldPath().forEach(
                fieldPath -> {
                    String stringBasePath = fieldPath.getLeft();
                    Path<String> path;
                    if (fieldPath.needsJoin()) {
                        if (!joins.containsKey(stringBasePath)) {
                            joins.put(stringBasePath, getJoin(root, stringBasePath));
                        }
                        path = SearchUtils.getPath(joins.get(stringBasePath), fieldPath.getRight());
                    }
                    else {
                        path = SearchUtils.getPath(root, stringBasePath);
                    }
                    if (filterOperation.needsMultipleValues()) {
                        Class<?> targetClazz = getTargetClass(rootClazz);
                        orPredicates.add(filterOperation.calculate(
                                cb,
                                path,
                                (T) ((List<String>) value).stream().map(v -> CastService.cast(v, targetClazz)).toList())
                        );
                    } else {
                        orPredicates.add(filterOperation.calculate(
                                cb,
                                path,
                                value
                        ));
                    }
                }
        );
        return cb.or(orPredicates.toArray(Predicate[]::new));
    }

    /**
     * Get the entryClass to access a field
     *
     * @param rootClazz Class to be analyzed
     * @return EntryClass
     */
    @Override
    public Class<T> getEntryClass(Class<R> rootClazz) {
        if (needMultipleValues()) {
            return (Class<T>) List.class;
        }
        return (Class<T>) getTargetClass(rootClazz);
    }

    /**
     * Get the targetClass in which the field is located.
     *
     * @param rootClazz Class to be analyzed
     * @return TargetClass
     */
    private Class<?> getTargetClass(Class<R> rootClazz) {
        return SearchUtils.getEntityClass(rootClazz, getFirstPath().split(REGEX_DOT));
    }

    @Override
    public boolean needJoin() {
        // check if contains ] or 1 point (need to check in further releases that a single point targets a foreign)
        return paths.stream().anyMatch(path -> path.lastIndexOf(']') >= 2 || path.contains("."));
    }
}
