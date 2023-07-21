package com.avisto.genericspringsearch.model;

import com.avisto.genericspringsearch.SearchableEntity;

import java.util.List;

public class TestEntity implements SearchableEntity {
    private String field1;
    private Integer field2;
    private TestNested nestedEntity;
    private List<TestNested> nestedList;
}
