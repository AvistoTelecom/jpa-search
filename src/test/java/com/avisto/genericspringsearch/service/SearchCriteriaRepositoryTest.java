package com.avisto.genericspringsearch.service;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.model.Page;
import com.avisto.genericspringsearch.model.TestEntity;
import com.avisto.genericspringsearch.model.TestEntity.TestEntityInList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.avisto.genericspringsearch.model.CriteriaTestEnum;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchCriteriaRepositoryTest {
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

    @Test
    void sort_withFilterInsteadOfSorter_shouldThrowFieldNotInCriteriaException() {
        // Init sorts
        List<String> sorts = new ArrayList<>();
        sorts.add("field2");
        sorts.add("asc");

        // Init params
        Map<String, String> params = new HashMap<>();

        // Perform the search operation and expect an exception to be thrown
        assertThrows(FieldNotInCriteriaException.class, () -> searchCriteriaRepository.search(CriteriaTestEnum.class, params, sorts, TestEntityInList::new));
    }
}
