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
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.avisto.genericspringsearch.FilterCriteria;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.IFilterConfig;
import com.avisto.genericspringsearch.config.ISearchCriteriaConfig;
import com.avisto.genericspringsearch.config.ISorterConfig;
import com.avisto.genericspringsearch.exception.EmptyCriteriaException;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongDataTypeException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.model.Page;
import com.avisto.genericspringsearch.model.SortDirection;

import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.PAGE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SIZE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SORTS;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.COMMA;

/**
 * This class provides a generic search criteria repository to perform search operations on JPA entities. It supports filtering,
 * sorting, and pagination based on the provided search criteria.
 *
 * @param <R> The type of the entity that is searchable and used for search operations.
 * @param <E> The enum type that implements {@link ISearchCriteriaConfig<R>}, providing search configuration for the entity.
 * @author Gabriel Revelli
 * @version 1.0
 */
@Named
public class SearchCriteriaRepository<R extends SearchableEntity, E extends Enum<E> & ISearchCriteriaConfig<R>> {

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

    public Page<R> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts) {
        return search(configClazz, format(configClazz, rawValues, sorts), Function.identity(), false, null);
    }

    public <D> Page<D> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, Function<R, D> mapper) {
        return search(configClazz, format(configClazz, rawValues, sorts), mapper, false, null);
    }

    public Page<R> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, boolean needsGroupBy) {
        return search(configClazz, format(configClazz, rawValues, sorts), Function.identity(), needsGroupBy, null);
    }

    public <D> Page<D> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, Function<R, D> mapper, boolean needsGroupBy) {
        return search(configClazz, format(configClazz, rawValues, sorts), mapper, needsGroupBy, null);
    }

    public Page<R> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, String entityGraphName) {
        return search(configClazz, format(configClazz, rawValues, sorts), Function.identity(), false, entityGraphName);
    }

    public <D> Page<D> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, Function<R, D> mapper, String entityGraphName) {
        return search(configClazz, format(configClazz, rawValues, sorts), mapper, false, entityGraphName);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param searchCriteria   The SearchCriteria object containing filtering, sorting, and pagination details.
     * @param needsGroupBy  A boolean indicating whether the search result needs to be grouped by any field.
     * @param <D> The type of the object that will be returned in the Page object.
     * @return A Page object containing the search results with pagination information.
     */
    public <D> Page<D> search(Class<E> configClazz, SearchCriteria searchCriteria, Function<R, D> mapper, boolean needsGroupBy, String entityGraphName) {

        // Create CriteriaBuilder and CriteriaQuery
        E[] configurations = configClazz.getEnumConstants();
        if (configurations.length == 0) {
            throw new EmptyCriteriaException(String.format("%s is empty : Cannot declare an empty criteria", configClazz.getName()));
        }
        Class<R> rootClazz = configurations[0].getRootClass();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = cb.createQuery(rootClazz);
        Root<R> root = criteriaQuery.from(rootClazz);
        Map<String, Join<R, ?>> joins = new HashMap<>();

        // Get the predicate for filtering the search results
        criteriaQuery.where(getPredicates(searchCriteria, rootClazz, configurations, root, cb, joins));

        int limit = searchCriteria.getSize();

        // Get the total count of results to create the Page object
        Long count = getCount(cb, rootClazz, configurations, searchCriteria);

        // Check if the "limit" is set to zero (size is zero)
        if (limit > 0) {
            // Set sorting and grouping (if needed) in the CriteriaQuery
            setGroupBy(rootClazz, configurations, needsGroupBy, joins, searchCriteria.getSorts(), criteriaQuery, root);
            List<Order> orders = searchCriteria.getSorts()
                    .stream()
                    .map(sort ->
                            SearchUtils.getSearchConfig(configurations, sort.getKey(), ISorterConfig.class).
                                    getOrder(root, cb, sort.getSortDirection()))
                    .toList();
            criteriaQuery.orderBy(orders);

            // Execute the query with pagination settings
            TypedQuery<R> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setFirstResult(searchCriteria.getPageNumber() * limit);
            typedQuery.setMaxResults(limit);
            if (entityGraphName != null) {
                typedQuery.setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph(entityGraphName));
            }

            List<R> results = typedQuery.getResultList();

            // Return the Page object with the search results and pagination information
            return new Page<>(results.stream().map(mapper).toList(), searchCriteria.getPageNumber(), limit, count);
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

    private Predicate getPredicates(SearchCriteria searchCriteria, Class<R> rootClazz, E[] configurations, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins) {
        return cb.and(searchCriteria.getFilters()
                .stream()
                .map(filter -> {
                    IFilterConfig filterConfig = SearchUtils.getSearchConfig(configurations, filter.getKey(), IFilterConfig.class);
                    Class<?> filterClazz = filterConfig.getEntryClass(rootClazz);
                    return filterConfig.getPredicate(rootClazz, root, cb, joins, Arrays.stream(filter.getValues()).map(value -> CastService.cast(value, filterClazz)).toArray());
                })
                .toArray(Predicate[]::new));
    }

    /**
     * Formats the search criteria based on the provided class, enum class, raw values, and sorts.
     *
     * @param rawValues The raw values for the search criteria.
     * @param sorts The sorting criteria for the search.
     * @return The formatted search criteria.
     */
    private SearchCriteria format(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts) {
        final SearchCriteria searchCriteria = new SearchCriteria();
        if (rawValues.containsKey(PAGE)) {
            searchCriteria.setPageNumber(getOneElement(PAGE, Integer.class, rawValues.remove(PAGE)));
        }
        if (rawValues.containsKey(SIZE)) {
            searchCriteria.setSize(getOneElement(SIZE, Integer.class, rawValues.remove(SIZE)));
        }
        rawValues.remove(SORTS);
        if (sorts == null || sorts.isEmpty()) {
            searchCriteria.setSorts(List.of(getDefaultOrderCriteria(configClazz)));
        } else {
            searchCriteria.setSorts(getSorts(sorts));
        }
        if (rawValues.isEmpty()) {
            searchCriteria.setFilters(new HashSet<>());
        } else {
            searchCriteria.setFilters(getFilters(configClazz.getEnumConstants(), rawValues));
        }
        return searchCriteria;
    }


    private Set<FilterCriteria> getFilters(E[] enums, Map<String, String> rawValues) {
        Set<FilterCriteria> filters = new HashSet<>();
        if (!rawValues.isEmpty()) {
            for (ISearchCriteriaConfig<R> e : enums) {
                String key = e.getKey();
                if (rawValues.containsKey(key)) {
                    String value = rawValues.remove(key);
                    String[] values;
                    if (!SearchUtils.isBlank(value) && e.needMultipleValues()) {
                        // TODO : Split on the right COMMA, use CAST on list
                        values = value.split(COMMA, -1);
                    } else {
                        values = new String[]{value};
                    }
                    filters.add(new FilterCriteria(key, values));
                }
            }
            if (!rawValues.isEmpty()) {
                throw new FieldNotInCriteriaException(String.format("Field %s is not in criteria", rawValues.keySet().iterator().next()));
            }
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

    private Long getCount(CriteriaBuilder cb, Class<R> rootClazz, E[] configurations, SearchCriteria searchCriteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<R> root = countQuery.from(rootClazz);
        countQuery.select(cb.count(root)).where(getPredicates(searchCriteria, rootClazz, configurations, root, cb, new HashMap<>()));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void setGroupBy(Class<R> clazz, E[] configurations, boolean needsGroupBy, Map<String, Join<R, ?>> joins, List<OrderCriteria> sorts, CriteriaQuery<R> criteriaQuery, Root<R> root) {
        if (needsGroupBy && !joins.isEmpty()) {
            List<Expression<?>> groupByList = new ArrayList<>(getGroupByFieldName(root, clazz));
            groupByList.addAll(sorts.stream()
                                       .map(orderCriteria -> {
                                           ISorterConfig<R> sorterConfig = SearchUtils.getSearchConfig(configurations, orderCriteria.getKey(), ISorterConfig.class);
                                           return (Expression<?>) SearchUtils.getPath(root, sorterConfig.getSortPath());
                                       })
                                       .toList());
            criteriaQuery.groupBy(groupByList);
        }
    }

    private List<Expression<?>> getGroupByFieldName(Root<R> root, Class<?> clazz) {
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
}