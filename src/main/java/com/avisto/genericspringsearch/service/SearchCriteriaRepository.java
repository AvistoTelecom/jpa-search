package com.avisto.genericspringsearch.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import com.avisto.genericspringsearch.FilterCriteria;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.SearchConfigInterface;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongDataTypeException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.model.Page;
import com.avisto.genericspringsearch.model.SortDirection;

import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.PAGE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SIZE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SORTS;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.COMMA;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.DOT;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

/**
 * This class provides a generic search criteria repository to perform search operations on JPA entities. It supports filtering,
 * sorting, and pagination based on the provided search criteria.
 *
 * @param <T> The type of the entity that is searchable and used for search operations.
 * @param <E> The enum type that implements {@link SearchConfigInterface}, providing search configuration for the entity.
 * @author Gabriel Revelli
 * @version 1.0
 */
@Named
public class SearchCriteriaRepository<T extends SearchableEntity, E extends Enum<E> & SearchConfigInterface> {

    private final EntityManager entityManager;

    /**
     * Constructs a new SearchCriteriaRepository with the given entity manager, entity class, and enum class.
     *
     * @param entityManager The entity manager to be used for executing queries.
     */
    @Inject
    public SearchCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), Function.identity(), false, null);
    }

    public <D> Page<D> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, Function<T, D> mapper) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), mapper, false, null);
    }

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, boolean needsGroupBy) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), Function.identity(), needsGroupBy, null);
    }

    public <D> Page<D> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, Function<T, D> mapper, boolean needsGroupBy) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), mapper, needsGroupBy, null);
    }

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, String entityGraphName) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), Function.identity(), false, entityGraphName);
    }

    public <D> Page<D> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, Function<T, D> mapper, String entityGraphName) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), mapper, false, entityGraphName);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param searchCriteria   The SearchCriteria object containing filtering, sorting, and pagination details.
     * @param needsGroupBy  A boolean indicating whether the search result needs to be grouped by any field.
     * @param <D> The type of the object that will be returned in the Page object.
     * @return A Page object containing the search results with pagination information.
     */
    public <D> Page<D> search(Class<T> clazz, Class<E> enumClazz, SearchCriteria searchCriteria, Function<T, D> mapper, boolean needsGroupBy, String entityGraphName) {

        // Create CriteriaBuilder and CriteriaQuery
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(clazz);
        Root<T> root = criteriaQuery.from(clazz);
        Map<String, Join<T, ?>> joins = getJoins(root, enumClazz, searchCriteria);

        // Get the predicate for filtering the search results
        Predicate predicate = getPredicate(enumClazz.getEnumConstants(), searchCriteria.getFilters(), root, cb, joins);
        criteriaQuery.where(predicate);

        int limit = searchCriteria.getSize();

        // Get the total count of results to create the Page object
        Long count = getCount(cb, clazz, enumClazz, searchCriteria);

        // Check if the "limit" is set to zero (size is zero)
        if (limit > 0) {
            // Set sorting and grouping (if needed) in the CriteriaQuery
            List<OrderCriteria> sorts = searchCriteria.getSorts();
            setGroupBy(clazz, enumClazz, needsGroupBy, joins, sorts, criteriaQuery, root);
            setOrders(enumClazz.getEnumConstants(), sorts, criteriaQuery, root, cb);

            // Execute the query with pagination settings
            TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setFirstResult(searchCriteria.getPageNumber() * limit);
            typedQuery.setMaxResults(limit);
            if (entityGraphName != null) {
                typedQuery.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(entityGraphName));
            }

            List<T> results = typedQuery.getResultList();

            // Return the Page object with the search results and pagination information
            return new Page<>(results.stream().map(mapper).collect(Collectors.toList()), searchCriteria.getPageNumber(), limit, count);
        } else if (limit == 0) {
            // Return the Page object with the search results and pagination information
            return new Page<>(Collections.emptyList(), searchCriteria.getPageNumber(), limit, count);
        } else {
            throw new WrongDataTypeException("Limit cannot be negative");
        }
    }

    /*
        PRIVATE
     */

    /**
     * Formats the search criteria based on the provided class, enum class, raw values, and sorts.
     *
     * @param rawValues The raw values for the search criteria.
     * @param sorts The sorting criteria for the search.
     * @return The formatted search criteria.
     */
    private SearchCriteria format(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts) {
        final SearchCriteria searchCriteria = new SearchCriteria();
        if (rawValues.containsKey(PAGE)) {
            searchCriteria.setPageNumber(getOneElement(PAGE, Integer.class, rawValues.remove(PAGE)));
        }
        if (rawValues.containsKey(SIZE)) {
            searchCriteria.setSize(getOneElement(SIZE, Integer.class, rawValues.remove(SIZE)));
        }
        rawValues.remove(SORTS);
        if (sorts == null || sorts.isEmpty()) {
            searchCriteria.setSorts(List.of(getDefaultOrderCriteria(enumClazz)));
        } else {
            searchCriteria.setSorts(getSorts(sorts));
        }
        if (rawValues.isEmpty()) {
            searchCriteria.setFilters(new HashSet<>());
        } else {
            searchCriteria.setFilters(getFilters(clazz, enumClazz.getEnumConstants(), rawValues));
        }
        return searchCriteria;
    }

    /**
     * Retrieves the joins required for the search based on the provided root entity, enum class, and search criteria.
     *
     * @param root The root entity representing the starting point of the search query.
     * @param enumClazz The Class object representing the enum type implementing {@link SearchConfigInterface},
     *                  which provides search configuration for the entity.
     * @param searchCriteria The SearchCriteria object containing filtering, sorting, and pagination details.
     * @return A map of joins with their associated field paths required for the search operation.
     * @throws CannotSortException If sorting is not allowed for a field that is associated with a "ToMany" relation
     *                             or if multiple field paths are specified for sorting.
     */
    private Map<String, Join<T, ?>> getJoins(Root<T> root, Class<E> enumClazz, SearchCriteria searchCriteria) {
        Set<String> tmpFieldPaths = new HashSet<>();
        Arrays.stream(enumClazz.getEnumConstants()).forEach(e -> {
            if (searchCriteria.sortsContainsKey(e.getFilterKey())) {
                if (e.needsJoin()) {
                    throw new CannotSortException(String.format("Cannot sort by %s : fields goes into a ToMany relation", e.getFilterKey()));
                }
                if (e.getFilterPaths().size() > 1) {
                    throw new CannotSortException(String.format("Cannot sort by %s : multiple field paths are specified", e.getFilterKey()));
                }
            }
            e.getDefaultFieldPath().forEach(
                    path -> {
                        if (searchCriteria.filtersContainsKey(e.getFilterKey()) && path.needsJoin()) {
                            tmpFieldPaths.add(path.getLeft());
                        }
                    }
            );
        });
        Map<String, Join<T, ?>> res = new HashMap<>();
        tmpFieldPaths.forEach(path -> res.put(path, getJoin(root, path)));
        return res;
    }

    private Set<FilterCriteria<?>> getFilters(Class<T> clazz, E[] enums, Map<String, String> rawValues) {
        Set<FilterCriteria<?>> filters = new HashSet<>();
        for (SearchConfigInterface config : enums) {
            String key = config.getFilterKey();
            if (rawValues.isEmpty()) {
                break;
            }
            if (rawValues.containsKey(key)) {
                String value = rawValues.remove(key);
                String[] values = null;
                if (!SearchUtils.isBlank(value) && config.getFilterConfig().getFilterOperation().needsMultipleValues()) {
                    values = value.split(COMMA, -1);
                } else {
                    values = new String[]{value};
                }
                filters.add(new FilterCriteria<>(key, values, SearchUtils.getEntityClass(clazz, config.getFilterPaths().get(0).split(REGEX_DOT))));
            }
        }
        if (!rawValues.isEmpty()) {
            throw new FieldNotInCriteriaException(String.format("Field %s is not in criteria", rawValues.keySet().iterator().next()));
        }
        return filters;
    }

    private OrderCriteria getDefaultOrderCriteria(Class<E> enumClazz) {
        return enumClazz.getEnumConstants()[0].getDefaultOrderCriteria();
    }

    private <X> X getOneElement(String key, Class<X> clazz, String... values) {
        if (values.length != 1) {
            throw new WrongElementNumberException(String.format("Only one element is expected for filter %s", key));
        }
        return CastService.cast(values[0], clazz);
    }

    private List<OrderCriteria> getSorts(List<String> sorts) {
        if (sorts.size() % 2 != 0) {
            throw new WrongElementNumberException("Sorts must be a pairs of key and direction : odd number of sorts");
        }
        List<OrderCriteria> ordersCriteria = new ArrayList<>();
        for (int i = 0; i < sorts.size(); i++) {
            ordersCriteria.add(new OrderCriteria(sorts.get(i++), SortDirection.of(sorts.get(i))));
        }
        return ordersCriteria;
    }

    private Long getCount(CriteriaBuilder cb, Class<T> clazz, Class<E> enumClazz, SearchCriteria searchCriteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        Predicate predicate = getPredicate(enumClazz.getEnumConstants(), searchCriteria.getFilters(), root, cb, getJoins(root, enumClazz, searchCriteria));
        countQuery.select(cb.count(root)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void setOrders(E[] enums, List<OrderCriteria> sorts, CriteriaQuery<T> criteriaQuery, Root<T> root, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        sorts.forEach(sort -> orders.add(sort.getSortDirection().getOrder(cb, getPath(root, getSearchConfigInterfaceBySearchField(enums, sort.getKey()).getFirstFilterPath()))));
        criteriaQuery.orderBy(orders);
    }

    private void setGroupBy(Class<T> clazz, Class<E> enumClazz, boolean needsGroupBy, Map<String, Join<T, ?>> joins, List<OrderCriteria> sorts, CriteriaQuery<T> criteriaQuery, Root<T> root) {
        if (needsGroupBy && !joins.isEmpty()) {
            List<Expression<?>> groupByList = new ArrayList<>(getGroupByFieldName(root, clazz));
            groupByList.addAll(sorts.stream()
                                       .map(orderCriteria -> getPath(root, getSearchConfigInterfaceBySearchField(enumClazz.getEnumConstants(), orderCriteria.getKey()).getFirstFilterPath()))
                                       .toList());
            criteriaQuery.groupBy(groupByList);
        }
    }

    private SearchConfigInterface getSearchConfigInterfaceBySearchField(E[] enums, String key) {
        return Arrays.stream(enums)
                .filter(
                        (SearchConfigInterface searchConfig)
                                -> searchConfig.getFilterKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new FieldNotInCriteriaException(String.format("Field %s is not specified in criteria", key)));
    }

    Predicate getPredicate(E[] enums, Set<FilterCriteria<?>> filters, Root<T> root, CriteriaBuilder cb, Map<String, Join<T, ?>> joins) {
        List<Predicate> predicates = new ArrayList<>();
        filters.forEach(filter -> {
            SearchConfigInterface searchConfigInterface = getSearchConfigInterfaceBySearchField(enums, filter.getKey());
            List<Predicate> orPredicates = new ArrayList<>();
            searchConfigInterface.getDefaultFieldPath().forEach(fieldPath -> {
                Path<String> path;
                if (joins.containsKey(fieldPath.getLeft())) {
                    path = getPath(joins.get(fieldPath.getLeft()), fieldPath.getRight());
                } else {
                    path = getPath(root, fieldPath.getLeft());
                }
                orPredicates.add(searchConfigInterface.getFilterConfig().getFilterOperation()
                                         .calculate(
                                                 cb,
                                                 path,
                                                 filter.getValues()
                                         )
                );
            });
            predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
        });
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private List<Expression<?>> getGroupByFieldName(Root<T> root, Class<?> clazz) {
        List<Expression<?>> groupBy = new ArrayList<>();
        if (clazz != null && (clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class))) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(EmbeddedId.class)) {
                    Path<String> path = root.get(field.getName());
                    return getGroupByFieldNameEmbeddedId(path, field.getType());
                }
                if (field.isAnnotationPresent(Id.class)) {
                    return List.of(root.get(field.getName()));
                }
            }
            groupBy.addAll(getGroupByFieldName(root, clazz.getSuperclass()));
        }
        return groupBy;
    }

    private List<Expression<?>> getGroupByFieldNameEmbeddedId(Path<String> path, Class<?> clazz) {
        List<Expression<?>> groupBy = new ArrayList<>();
        if (clazz != null && (clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class))) {
            for (Field field : clazz.getDeclaredFields()) {
                groupBy.add(path.get(field.getName()));
            }
            groupBy.addAll(getGroupByFieldNameEmbeddedId(path, clazz.getSuperclass()));
        }
        return groupBy;
    }

    private Path<String> getPath(From<T, ?> from, String fieldPath) {
        if (SearchUtils.isBlank(fieldPath)) {
            return (From<T, String>) from;
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

    private Join<T, ?> getJoin(From<T, ?> from, String toPath) {
        int firstIndex = toPath.indexOf(DOT);
        if (firstIndex == -1) {
            return from.join(toPath, JoinType.LEFT);
        }
        String joinFromKey = toPath.substring(0, firstIndex);
        String joinToKey = toPath.substring(firstIndex + 1);
        return getJoin(from.join(joinFromKey, JoinType.LEFT), joinToKey);
    }

    private Join<T, ?> getJoin(Join<T, ?> join, String toPath) {
        return getJoin((From<T, ?>) join, toPath);
    }
}