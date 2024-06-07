package com.avisto.jpasearch.service;

import com.avisto.jpasearch.OrderCriteria;
import com.avisto.jpasearch.SearchCriteria;
import com.avisto.jpasearch.SearchableEntity;
import com.avisto.jpasearch.config.ISorterConfig;
import com.avisto.jpasearch.operation.ListObjectFilterOperation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

public class MultiThreadingSearchTask<R extends SearchableEntity> implements Runnable {

    private TypedQuery<Tuple> typedQuery;
    private CriteriaBuilder cb;
    private Class<R> rootClazz;
    private SearchCriteria searchCriteria;
    private Map<String, ISorterConfig<R>> sorterMap;
    private String entityGraphName;
    private volatile List<R> results;
    private final EntityManager entityManager;

    public MultiThreadingSearchTask(TypedQuery typedQuery, CriteriaBuilder cb, Class<R> rootClazz, SearchCriteria searchCriteria, Map<String, ISorterConfig<R>> sorterMap, String entityGraphName, EntityManager entityManager) {
        this.typedQuery = typedQuery;
        this.cb = cb;
        this.rootClazz = rootClazz;
        this.searchCriteria = searchCriteria;
        this.sorterMap = sorterMap;
        this.entityGraphName = entityGraphName;
        this.entityManager = entityManager;
    }

    @Override
    public void run() {
        List<Object> ids = typedQuery.getResultList().stream().map(tuple -> tuple.get(0)).toList();
        results = getResult(cb, rootClazz, ids, searchCriteria.getSorts(), sorterMap, entityGraphName);
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

    public List<R> getResults() {
        return results;
    }
}
