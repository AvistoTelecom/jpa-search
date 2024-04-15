package com.avisto.jpasearch.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import com.avisto.jpasearch.FilterCriteria;
import com.avisto.jpasearch.OrderCriteria;
import com.avisto.jpasearch.SearchCriteria;
import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.config.IFilterConfig;
import com.avisto.jpasearch.config.ISearchCriteriaConfig;
import com.avisto.jpasearch.config.ISorterConfig;
import com.avisto.jpasearch.exception.EmptyCriteriaException;
import com.avisto.jpasearch.exception.FieldNotInCriteriaException;
import com.avisto.jpasearch.exception.WrongDataTypeException;
import com.avisto.jpasearch.exception.WrongElementNumberException;
import com.avisto.jpasearch.model.Page;
import com.avisto.jpasearch.model.SortDirection;
import com.avisto.jpasearch.operation.ListObjectFilterOperation;

import static com.avisto.jpasearch.service.SearchConstants.KeyWords.PAGE;
import static com.avisto.jpasearch.service.SearchConstants.KeyWords.SIZE;
import static com.avisto.jpasearch.service.SearchConstants.KeyWords.SORTS;

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

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param configClazz Criteria Class
     * @param rawValues Search parameters
     * @param sorts Sorter for your search
     * @return A Page object containing the search results with pagination information.
     */
    public Page<R> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts) {
        return search(configClazz, format(configClazz, rawValues, sorts), null, null);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param configClazz Criteria Class
     * @param rawValues Search parameters
     * @param sorts Sorter for your search
     * @param mapper The entity mapper
     * @return A Page object containing the search results with pagination information.
     * @param <D> The type of the object that will be returned in the Page object.
     */
    public <D> Page<D> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, Function<R, D> mapper) {
        return search(configClazz, format(configClazz, rawValues, sorts), mapper, null);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param configClazz Criteria Class
     * @param rawValues Search parameters
     * @param sorts Sorter for your search
     * @param entityGraphName Name of the entityGraph
     * @return A Page object containing the search results with pagination information.
     */
    public Page<R> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, String entityGraphName) {
        return search(configClazz, format(configClazz, rawValues, sorts), null, entityGraphName);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param configClazz Criteria Class
     * @param rawValues Search parameters
     * @param sorts Sorter for your search
     * @param mapper The entity mapper
     * @param entityGraphName Name of the entityGraph
     * @return A Page object containing the search results with pagination information.
     * @param <D> The type of the object that will be returned in the Page object.
     */
    public <D> Page<D> search(Class<E> configClazz, Map<String, String> rawValues, List<String> sorts, Function<R, D> mapper, String entityGraphName) {
        return search(configClazz, format(configClazz, rawValues, sorts), mapper, entityGraphName);
    }

    /**
     * Performs a search operation based on the provided search criteria and returns the results as a pageable list.
     *
     * @param configClazz Criteria Class
     * @param searchCriteria The SearchCriteria object containing filtering, sorting, and pagination details.
     * @param mapper The entity mapper
     * @param entityGraphName Name of the entityGraph
     * @return A Page object containing the search results with pagination information.
     * @param <D> The type of the object that will be returned in the Page object.
     */
    public <D> Page<D> search(Class<E> configClazz, SearchCriteria searchCriteria, Function<R, D> mapper, String entityGraphName) {

        // Create CriteriaBuilder and CriteriaQuery
        E[] configurations = configClazz.getEnumConstants();
        if (configurations.length == 0) {
            throw new EmptyCriteriaException(String.format("%s is empty : Cannot declare an empty criteria", configClazz.getName()));
        }
        Class<R> rootClazz = configurations[0].getRootClass();
        Map<String, IFilterConfig<R, ?>> filterMap = SearchUtils.getSearchConfigMap(configurations, searchCriteria.getFilterKeys(), (Class<IFilterConfig<R, ?>>)(Class)IFilterConfig.class);
        Map<String, ISorterConfig<R>> sorterMap = SearchUtils.getSearchConfigMap(configurations, searchCriteria.getSorterKeys(), (Class<ISorterConfig<R>>)(Class)ISorterConfig.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        String stringIdPath = SearchUtils.getIdStringPath(rootClazz);

        // Get the total count of results to create the Page object
        Long count = getCount(cb, rootClazz, filterMap, searchCriteria, stringIdPath);

        Page<R> page;
        if (filterMap.values().stream().anyMatch(IFilterConfig::needJoin)) {
            page = doubleRequest(cb, rootClazz, searchCriteria, filterMap, sorterMap, count, stringIdPath, entityGraphName);
        } else {
            page = simpleRequest(cb, rootClazz, searchCriteria, filterMap, sorterMap, count, entityGraphName);
        }
        if (mapper != null) {
            return page.map(mapper);
        }
        return (Page<D>) page;
    }

    /*
        PRIVATE
     */

    private Page<R> simpleRequest(
            CriteriaBuilder cb,
            Class<R> rootClazz,
            SearchCriteria searchCriteria,
            Map<String, IFilterConfig<R, ?>> filterMap,
            Map<String, ISorterConfig<R>> sorterMap,
            long count,
            String entityGraphName
    ) {
        CriteriaQuery<R> criteriaQuery = cb.createQuery(rootClazz);
        Root<R> root = criteriaQuery.from(rootClazz);
        Map<String, Join<R, ?>> joins = new HashMap<>();

        // Get the predicate for filtering the search results
        criteriaQuery.where(getPredicates(searchCriteria, rootClazz, filterMap, root, cb, joins));

        int limit = searchCriteria.getSize();

        // Check if the "limit" is set to zero (size is zero)
        if (limit > 0) {
            // Set sorting in the CriteriaQuery
            List<Order> orders = searchCriteria.getSorts()
                    .stream()
                    .map(sort -> sorterMap.get(sort.getKey()).getOrder(root, cb, sort.getSortDirection()))
                    .toList();
            criteriaQuery.orderBy(orders);

            // Execute the query with pagination settings
            TypedQuery<R> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setHint("org.hibernate.readOnly", true);
            typedQuery.setFirstResult(searchCriteria.getPageNumber() * limit);
            typedQuery.setMaxResults(limit);
            if (entityGraphName != null) {
                typedQuery.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(entityGraphName));
            }

            List<R> results = typedQuery.getResultList();

            // Return the Page object with the search results and pagination information
            return new Page<>(results, searchCriteria.getPageNumber(), limit, count);
        } else if (limit == 0) {
            // Return the Page object with the search results and pagination information
            return new Page<>(Collections.emptyList(), searchCriteria.getPageNumber(), limit, count);
        } else {
            throw new WrongDataTypeException("Limit cannot be negative");
        }

    }

    private Page<R> doubleRequest( // NOSONAR
            CriteriaBuilder cb,
            Class<R> rootClazz,
            SearchCriteria searchCriteria,
            Map<String, IFilterConfig<R, ?>> filterMap,
            Map<String, ISorterConfig<R>> sorterMap,
            long count,
            String stringIdPath,
            String entityGraphName
    ) {
        CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
        criteriaQuery.distinct(true);
        Root<R> root = criteriaQuery.from(rootClazz);
        Map<String, Join<R, ?>> joins = new HashMap<>();

        // Get the predicate for filtering the search results
        criteriaQuery.where(getPredicates(searchCriteria, rootClazz, filterMap, root, cb, joins));

        int limit = searchCriteria.getSize();

        // Check if the "limit" is set to zero (size is zero)
        if (limit > 0) {
            List<Selection<?>> selections = new ArrayList<>();
            selections.add(root.get(stringIdPath));

            // Set sorting and select elements in the CriteriaQuery
            List<Order> orders = searchCriteria.getSorts()
                    .stream().map(
                            sort -> {
                                ISorterConfig<R> sorterConfig = sorterMap.get(sort.getKey());
                                String sorterStringPath = sorterConfig.getSortPath();
                                if (!stringIdPath.equals(sorterStringPath)) {
                                    selections.add(SearchUtils.getPath(root, sorterStringPath));
                                }
                                return sorterConfig.getOrder(root, cb, sort.getSortDirection());
                            }
                    )
                    .toList();

            criteriaQuery.multiselect(selections);
            criteriaQuery.orderBy(orders);

            // Execute the query with pagination settings
            TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setHint("org.hibernate.readOnly", true);
            typedQuery.setFirstResult(searchCriteria.getPageNumber() * limit);
            typedQuery.setMaxResults(limit);

            List<Object> ids = typedQuery.getResultList().stream().map(tuple -> tuple.get(0)).toList();

            List<R> results = getResult(cb, rootClazz, ids, searchCriteria.getSorts(), sorterMap, entityGraphName);

            // Return the Page object with the search results and pagination information
            return new Page<>(results, searchCriteria.getPageNumber(), limit, count);
        } else if (limit == 0) {
            // Return the Page object with the search results and pagination information
            return new Page<>(Collections.emptyList(), searchCriteria.getPageNumber(), limit, count);
        } else {
            throw new WrongDataTypeException("Limit cannot be negative");
        }
    }

    private List<R> getResult(CriteriaBuilder cb, Class<R> rootClazz, List<Object> ids, List<OrderCriteria> sorts, Map<String, ISorterConfig<R>> sorterMap, String eg) {
        CriteriaQuery<R> cq = cb.createQuery(rootClazz);
        Root<R> r = cq.from(rootClazz);
        cq.where(ListObjectFilterOperation.IN_EQUAL.calculate(cb, r.get(SearchUtils.getIdStringPath(rootClazz)), ids));
        cq.orderBy(sorts.stream()
                .map(sort -> sorterMap.get(sort.getKey()).getOrder(r, cb, sort.getSortDirection()))
                .toList());

        TypedQuery<R> tq = entityManager.createQuery(cq);
        tq.setHint("org.hibernate.readOnly", true);

        if (eg != null) {
            tq.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(eg));
        }
        return tq.getResultList();
    }

    private Predicate getPredicates(SearchCriteria searchCriteria, Class<R> rootClazz, Map<String, IFilterConfig<R, ?>> filterMap, Root<R> root, CriteriaBuilder cb, Map<String, Join<R, ?>> joins) {
        return cb.and(searchCriteria.getFilters()
                .stream()
                .map(filter -> {
                    IFilterConfig filterConfig = filterMap.get(filter.getKey());
                    Class<?> filterClazz = filterConfig.getEntryClass(rootClazz);
                    if (filterConfig.needMultipleValues()) {
                        return filterConfig.getPredicate(rootClazz, root, cb, joins, List.of(filter.getValues()));
                    }
                    String value = SearchUtils.isEmpty(filter.getValues()) ? null : filter.getValues()[0];
                    return filterConfig.getPredicate(rootClazz, root, cb, joins, CastService.cast(value, filterClazz));
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
                        values = (String[]) CastService.cast(value, List.class).toArray(String[]::new);
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
        int sortSize = sorts.size();
        if (sortSize % 2 != 0) {
            throw new WrongElementNumberException("Sorts must be a pairs of key and direction : odd number of sorts");
        }
        List<OrderCriteria> ordersCriteria = new ArrayList<>();
        int i = 0;
        while (i < sortSize) {
            ordersCriteria.add(new OrderCriteria(sorts.get(i++), SortDirection.of(sorts.get(i++))));
        }
        return ordersCriteria;
    }

    private Long getCount(CriteriaBuilder cb, Class<R> rootClazz, Map<String, IFilterConfig<R, ?>> filterMap, SearchCriteria searchCriteria, String idPath) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<R> root = countQuery.from(rootClazz);
        countQuery.select(cb.countDistinct(root.get(idPath))).where(getPredicates(searchCriteria, rootClazz, filterMap, root, cb, new HashMap<>()));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}