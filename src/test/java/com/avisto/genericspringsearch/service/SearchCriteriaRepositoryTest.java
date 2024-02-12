package com.avisto.genericspringsearch.service;


import com.avisto.genericspringsearch.FilterCriteria;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.FilterConfig;
import com.avisto.genericspringsearch.config.FilterSorterConfig;
import com.avisto.genericspringsearch.config.IFilterConfig;
import com.avisto.genericspringsearch.config.ISearchConfig;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.model.Page;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.model.TestEntity;
import com.avisto.genericspringsearch.model.TestEntity.TestEntityInList;
import com.avisto.genericspringsearch.operation.ObjectFilterOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.avisto.genericspringsearch.model.CriteriaTestEnum;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//faire les tests de base
//un search qui retourne rien, un search qui retourne quelque chose, un search sur une liste, un search entity graph et entity graph list
// faire un test pour tout les filteroperations et tous les groups filter
public class SearchCriteriaRepositoryTest {

//    private EntityManager entityManager;
    private EntityManager entityManager;

    private final SearchCriteriaRepository<TestEntity, CriteriaTestEnum> searchCriteriaRepository;

    CriteriaBuilder cb;
    CriteriaQuery cq;
    CriteriaQuery<Long> countQuery;
    TypedQuery tq;
    Expression<Long> el;
    Root root;

    public SearchCriteriaRepositoryTest() {

        this.entityManager = Mockito.mock(EntityManager.class);
        this.searchCriteriaRepository = new SearchCriteriaRepository<>(entityManager);

        cb = mock(CriteriaBuilder.class);
        cq = mock(CriteriaQuery.class);
        when(cb.createQuery(any(Class.class))).thenReturn(cq);
        countQuery = cb.createQuery(Long.class);
        tq = mock(TypedQuery.class);
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        el = mock(Expression.class);
        when(cb.countDistinct(any(Expression.class))).thenReturn(el);
        when(cq.from(any(Class.class))).thenReturn(mock(Root.class));
        when(cq.select(any())).thenReturn(cq);
        when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(tq);
        root = mock(Root.class);
        when(root.get(any(String.class))).thenReturn(mock(Path.class));
        when(countQuery.where(any(Expression.class))).thenReturn(cq);
    }

    @Test
    void search_withZeroLimit_shouldReturnEmptyPage() {
        when(tq.getSingleResult()).thenReturn(0L);

        Map<String, String> params = new HashMap<>();
        params.put("page", "0");
        params.put("size", "0");

        List<String> sorts = new ArrayList<>();

        Page<TestEntity> page = searchCriteriaRepository.search(CriteriaTestEnum.class, params, sorts, TestEntity.TestEntityInList::new);

        // Verify that the query was not executed
        verify(tq, never()).getResultList();

        // Verify that an empty page is returned
        assert page.elements().isEmpty();
        assert page.pageNumber() == 0;
        assert page.pageSize() == 0;
        assert page.totalElements() == 0L;
    }

    @Test
    void search_withValidSearchCriteria_shouldReturnResults() {
        // Init sorts
        List<String> sorts = new ArrayList<>();
        sorts.add("field1");
        sorts.add("asc");

        // Init params
        Map<String, String> params = new HashMap<>();
        params.put("page", "0");
        params.put("size", "10");
        params.put("field1", "1");

        when(tq.getSingleResult()).thenReturn(50L);

        // Perform the search operation
        Page<SearchableEntity> page = searchCriteriaRepository.search(CriteriaTestEnum.class, params, sorts, TestEntityInList::new);

        // Verify that the query was executed
        verify(tq).getResultList();

        // Verify the page content and pagination information
        assert page.elements().isEmpty();
        assert page.pageNumber() == 0;
        assert page.pageSize() == 10;
        assert page.totalElements() == 50L;

//        tq.getResultList();

//        String sqlQuery = tq.unwrap(Query.class).toString();
//        System.out.println("typedquery : " + sqlQuery);
    }

    @Test
    void search_withInvalidFilterKey_shouldThrowFieldNotInCriteriaException() {
        // Init sorts
        List<String> sorts = new ArrayList<>();

        // Init params
        Map<String, String> params = new HashMap<>();
        params.put("page", "0");
        params.put("size", "10");
        params.put("invalidFilterKey", "field1");

        // Perform the search operation and expect an exception to be thrown
        assertThrows(FieldNotInCriteriaException.class, () -> searchCriteriaRepository.search(CriteriaTestEnum.class, params, sorts, TestEntityInList::new));
    }

    @Test
    void search_withInvalidSorts_shouldThrowWrongElementNumberException() {
        // Init sorts
        List<String> sorts = new ArrayList<>();
        sorts.add("field1");

        // Init params
        Map<String, String> params = new HashMap<>();

        // Perform the search operation and expect an exception to be thrown
        assertThrows(WrongElementNumberException.class, () -> searchCriteriaRepository.search(CriteriaTestEnum.class, params, sorts, TestEntityInList::new));
    }

    /*

    @Test
    void search_withCannotSortException_shouldThrowCannotSortException() {
        // Create a sample search criteria with a sort key that cannot be sorted
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setSize(10);

        Map<String, String> rawValues = new HashMap<>();
        rawValues.put("filterKey", "filterValue");
        searchCriteria.setFilters(new HashSet<>(List.of(
                new FilterCriteria<>("filterKey", new String[]{"filterValue"}, String.class)
        )));

        List<String> sorts = List.of("sortKey,ASC");

        // Perform the search operation and expect an exception to be thrown
        assertThrows(CannotSortException.class, () -> searchCriteriaRepository.search(TestEntity.class, CriteriaTestEnum.class, rawValues, sorts));
    }*/
}
