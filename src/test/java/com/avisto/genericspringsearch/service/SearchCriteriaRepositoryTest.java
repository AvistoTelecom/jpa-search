package com.avisto.genericspringsearch.service;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.avisto.genericspringsearch.FilterOperation;
import com.avisto.genericspringsearch.config.FilterConfig;
import com.avisto.genericspringsearch.config.SearchConfigInterface;
import com.avisto.genericspringsearch.model.CriteriaTestEnum;

import com.avisto.genericspringsearch.FilterCriteria;
import com.avisto.genericspringsearch.OrderCriteria;
import com.avisto.genericspringsearch.SearchCriteria;
import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.WrongElementNumberException;
import com.avisto.genericspringsearch.model.Page;
import com.avisto.genericspringsearch.model.Pair;
import com.avisto.genericspringsearch.model.SortDirection;
import com.avisto.genericspringsearch.model.TestEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchCriteriaRepositoryTest {

    private SearchCriteriaRepository<SearchableEntity, CriteriaTestEnum> searchCriteriaRepository;
    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery<SearchableEntity> criteriaQuery;
    private Root<SearchableEntity> root;
    private TypedQuery<SearchableEntity> typedQuery;

    /*er = mock(CriteriaBuilder.class);

        searchCriteriaRepository = new SearchCriteriaRepository<>(entityManager);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(SearchableEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(SearchableEntity.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
    }

    @Test
    void search_withZeroLimit_shouldReturnEmptyPage() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setSize(0);

        Page<SearchableEntity> page = searchCriteriaRepository.search(TestEntity.class, CriteriaTestEnum.class, null, null);

        // Verify that the query was not executed
        verify(typedQuery, never()).getResultList();

        // Verify that an empty page is returned
        assert page.elements().isEmpty();
        assert page.pageNumber() == 0;
        assert page.pageSize() == 0;
        assert page.totalElements() == 0L;
    }

    @Test
    void search_withValidSearchCriteria_shouldReturnResults() {
        // Create a sample search criteria with filters and sorts
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setSize(10);

        Map<String, String> rawValues = new HashMap<>();
        rawValues.put("filterKey1", "filterValue1");
        rawValues.put("filterKey2", "filterValue2");
        searchCriteria.setFilters(new HashSet<>(List.of(
                new FilterCriteria<>("filterKey1", new String[]{"filterValue1"}, String.class),
                new FilterCriteria<>("filterKey2", new String[]{"filterValue2"}, String.class)
        )));

        List<String> sorts = List.of("sortKey1,ASC", "sortKey2,DESC");
        searchCriteria.setSorts(List.of(
                new OrderCriteria("sortKey1", SortDirection.ASC),
                new OrderCriteria("sortKey2", SortDirection.DESC)
        ));

        // Create mocks for SearchConfigInterface enums (e.g., SearchConfigInterface.CONFIG1, SearchConfigInterface.CONFIG2)
        CriteriaTestEnum config1 = mock(CriteriaTestEnum.class);
        when(config1.getFilterKey()).thenReturn("filterKey1");
        when(config1.getFilterConfig()).thenReturn(FilterConfig.of(FilterOperation.EQUAL, "key1", "fieldPath1"));
        when(config1.getDefaultFieldPath()).thenReturn(List.of(Pair.of("fieldPath1", "fieldPath2")));

        SearchConfigInterface config2 = mock(SearchConfigInterface.class);
        when(config2.getFilterKey()).thenReturn("filterKey2");
        when(config1.getFilterConfig()).thenReturn(FilterConfig.of(FilterOperation.EQUAL, "key1", "fieldPath1"));
        when(config1.getDefaultFieldPath()).thenReturn(List.of(Pair.of("fieldPath1", "fieldPath2")));

        // Create mocks for SearchConfigInterface enum classes
        Class<SearchConfigInterface> enumClazz = SearchConfigInterface.class;
        when(enumClazz.getEnumConstants()).thenReturn(new SearchConfigInterface[]{config1, config2});

        // Create a mock for the Join object returned by getJoin method
        Join<SearchableEntity, ?> join = mock(Join.class);
        when(searchCriteriaRepository.getJoin(root, "joinPath")).thenReturn(join);

        // Create a mock for the Predicate object returned by getPredicate method
        Predicate predicate = mock(Predicate.class);
        when(searchCriteriaRepository.getPredicate(any(), any(), any(), any(), any())).thenReturn(predicate);

        // Create a mock for the count query and its typed query
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(50L);

        // Perform the search operation
        Page<SearchableEntity> page = searchCriteriaRepository.search(searchCriteria, false);

        // Verify that the query was executed
        verify(typedQuery).getResultList();

        // Verify the page content and pagination information
        assert !page.getData().isEmpty();
        assert page.getPageNumber() == 0;
        assert page.getPageSize() == 10;
        assert page.getTotalCount() == 50L;
    }

    @Test
    void search_withInvalidFilterKey_shouldThrowFieldNotInCriteriaException() {
        // Create a sample search criteria with an invalid filter key
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setSize(10);

        Map<String, String> rawValues = new HashMap<>();
        rawValues.put("invalidFilterKey", "filterValue");
        searchCriteria.setFilters(new HashSet<>(List.of(
                new FilterCriteria<>("filterKey", new String[]{"filterValue"}, String.class)
        )));

        // Create mocks for SearchConfigInterface enums (e.g., SearchConfigInterface.CONFIG1)
        SearchConfigInterface config1 = mock(SearchConfigInterface.class);
        when(config1.getFilterKey()).thenReturn("filterKey");
        when(config1.getFilterConfig()).thenReturn(FilterConfig1.INSTANCE);
        when(config1.getDefaultFieldPath()).thenReturn(List.of("fieldPath"));

        // Create a mock for the SearchConfigInterface enum class
        Class<SearchConfigInterface> enumClazz = SearchConfigInterface.class;
        when(enumClazz.getEnumConstants()).thenReturn(new SearchConfigInterface[]{config1});

        // Perform the search operation and expect an exception to be thrown
        assertThrows(FieldNotInCriteriaException.class, () -> searchCriteriaRepository.search(searchCriteria, false));
    }

    @Test
    void search_withInvalidSorts_shouldThrowWrongElementNumberException() {
        // Create a sample search criteria with an invalid sorts list
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setSize(10);

        // Create an invalid sorts list with an odd number of elements
        List<String> invalidSorts = List.of("sortKey1,ASC", "sortKey2");

        // Perform the search operation and expect an exception to be thrown
        assertThrows(WrongElementNumberException.class, () -> searchCriteriaRepository.search(searchCriteria, false));
    }

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
