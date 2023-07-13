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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.avisto.genericspringsearch.FilterCriteria;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.SearchConfigInterface;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;

import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.PAGE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SIZE;
import static com.avisto.genericspringsearch.service.SearchConstants.KeyWords.SORTS;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.COMMA;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.DOT;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

public class SearchCriteriaRepository<T extends SearchableEntity, E extends SearchConfigInterface> {

    public SearchCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private final EntityManager entityManager;

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

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), false);
    }

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, Map<String, String> rawValues, List<String> sorts, boolean needsGroupBy) {
        return search(clazz, enumClazz, format(clazz, enumClazz, rawValues, sorts), needsGroupBy);
    }

    public Page<T> search(Class<T> clazz, Class<E> enumClazz, SearchCriteria searchCriteria, boolean needsGroupBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<T> root = criteriaQuery.from(clazz);
        Map<String, Join<T, ?>> joins = getJoins(root, enumClazz, searchCriteria);

        Predicate predicate = getPredicate(enumClazz.getEnumConstants(), searchCriteria.getFilters(), root, criteriaBuilder, joins);
        criteriaQuery.where(predicate);

        List<OrderCriteria> sorts = searchCriteria.getSorts();
        setGroupBy(clazz, enumClazz, needsGroupBy, joins, sorts, criteriaQuery, root);
        setOrders(enumClazz.getEnumConstants(), sorts, criteriaQuery, root, criteriaBuilder);

        int limit = searchCriteria.getSize();

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(searchCriteria.getPageNumber() * limit);
        typedQuery.setMaxResults(limit);

        Long count = getCount(predicate, criteriaBuilder, clazz, enumClazz, searchCriteria);

        if (limit == 0) {
            // Do not get data
            return new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), count);
        } else {
            return new PageImpl<>(typedQuery.getResultList(),
                                  PageRequest.of(searchCriteria.getPageNumber(), limit),
                                  count);
        }
    }

    /*
        PRIVATE
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
            if (searchCriteria.filtersContainsKey(e.getFilterKey()) && e.needsJoin()) {
                e.splitFieldListPath().forEach(path -> {
                    if (path.length > 1) {
                        tmpFieldPaths.add(path[0]);
                    }
                });
            }
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
        List<OrderCriteria> orderCriterias = new ArrayList<>();
        for (int i = 0; i < sorts.size(); i++) {
            orderCriterias.add(new OrderCriteria(sorts.get(i++), Sort.Direction.valueOf(sorts.get(i).toUpperCase())));
        }
        return orderCriterias;
    }

    private Long getCount(Predicate predicate, CriteriaBuilder criteriaBuilder, Class<T> clazz, Class<E> enumClazz, SearchCriteria searchCriteria) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        getJoins(root, enumClazz, searchCriteria);
        countQuery.select(criteriaBuilder.count(root)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void setOrders(E[] enums, List<OrderCriteria> sorts, CriteriaQuery<T> criteriaQuery, Root<T> root, CriteriaBuilder criteriaBuilder) {
        List<Order> orders = new ArrayList<>();
        sorts.forEach(sort ->
                              orders.add(
                                      getOrder(sort,
                                               getSearchConfigInterfaceBySearchField(enums, sort.getKey()).getFilterPaths().get(0),
                                               root,
                                               criteriaBuilder
                                      )
                              )
        );
        criteriaQuery.orderBy(orders);
    }

    private void setGroupBy(Class<T> clazz, Class<E> enumClazz, boolean needsGroupBy, Map<String, Join<T, ?>> joins, List<OrderCriteria> sorts, CriteriaQuery<T> criteriaQuery, Root<T> root) {
        if (needsGroupBy && !joins.isEmpty()) {
            List<Expression<?>> groupByList = new ArrayList<>(getGroupByFieldName(root, clazz));
            groupByList.addAll(sorts.stream()
                                       .map(orderCriteria -> getPath(root, getSearchConfigInterfaceBySearchField(enumClazz.getEnumConstants(), orderCriteria.getKey()).getFilterPaths().get(0)))
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

    private Order getOrder(OrderCriteria sort, String path, Root<T> root, CriteriaBuilder criteriaBuilder) {
        if (sort.getSortDirection().equals(Sort.Direction.DESC)) {
            return criteriaBuilder.desc(getPath(root, path));
        }
        return criteriaBuilder.asc(getPath(root, path));
    }

    private Predicate getPredicate(E[] enums, Set<FilterCriteria<?>> filters, Root<T> root, CriteriaBuilder criteriaBuilder, Map<String, Join<T, ?>> joins) {
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
                                                 criteriaBuilder,
                                                 path,
                                                 filter.getValues()
                                         )
                );
            });
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        });
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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
        int firstIndex = toPath.indexOf(DOT);
        if (firstIndex == -1) {
            return join.join(toPath, JoinType.LEFT);
        }
        String joinFromKey = toPath.substring(0, firstIndex);
        String joinToKey = toPath.substring(firstIndex + 1);
        return getJoin(join.join(joinFromKey, JoinType.LEFT), joinToKey);
    }
}